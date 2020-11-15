package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import py.com.sodep.mf.exchange.objects.error.ErrorResponse;
import py.com.sodep.mf.exchange.objects.error.ErrorType;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.DeviceBlacklistedException;
import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.web.endpoints.exceptions.EndpointOperationException;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public abstract class EndpointController {

	private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);

	protected final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	protected IAuthorizationControlService authControlService;

	@Autowired
	protected IApplicationService appService;

	public static Logger logBaseClass = LoggerFactory.getLogger(EndpointController.class);

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	protected IAuthorizationControlService authorizationControlService;

	protected User getUser(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		return manager.getUser();
	}

	protected Map<String, Object> translateI18nMessages(HttpServletRequest request, Map<String, String> messages) {
		I18nManager i18n = I18nManager.getI18n(request);
		Set<Entry<String, String>> entrySet = messages.entrySet();
		Map<String, Object> ret = new HashMap<String, Object>();
		for (Entry<String, String> entry : entrySet) {
			ret.put(entry.getKey(), i18n.getMessage(entry.getValue()));
		}
		return ret;
	}

	protected Map<String, String> getParametersMap(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String key = paramNames.nextElement();
			params.put(key, request.getParameter(key));
		}
		return params;
	}

	protected void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				logBaseClass.error("", e);
			}
		}
	}

	protected void throwApplicationIsInactive() {
		EndpointOperationException ex = new EndpointOperationException(true);
		ex.setKey("web.api.authorization.inactiveApplication");
		ex.setInvalidateSession(true);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setErrorType(ErrorType.APP_INACTIVE);
		throw ex;
	}

	protected void throwDeviceNotAssociated() {
		EndpointOperationException ex = new EndpointOperationException(true);
		ex.setKey("web.api.authorization.deviceNotAssociated");
		ex.setInvalidateSession(true);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setErrorType(ErrorType.DEVICE_NOT_ASSOCIATED);
		throw ex;
	}

	protected void throwLicenseExpired() {
		EndpointOperationException ex = new EndpointOperationException();
		ex.setKey("web.api.authorization.licenseExpired");
		ex.setInvalidateSession(true);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setErrorType(ErrorType.LICENSE_EXPIRED);
		throw ex;
	}

	protected void checkIfApplicationIsActive(User user, Long applicationId) {
		boolean appActive = applicationService.isActive(applicationId);

		if (!appActive) {
			logBaseClass.warn("Application #" + applicationId + " is Inactive. User : #" + user.getId() + " "
					+ user.getMail());
			throwApplicationIsInactive();
		}
	}

	protected void throwLicenseException(LicenseException ex) {
		EndpointOperationException endpointOperationException = new EndpointOperationException(ex.getMessage(), ex);
		endpointOperationException.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		endpointOperationException.setKey("web.api.authentication.licenseException");
		throw endpointOperationException;
	}

	protected void throwDeviceBlacklisted(DeviceBlacklistedException ex) {
		EndpointOperationException endpointOperationException = new EndpointOperationException(ex.getMessage(), ex);
		endpointOperationException.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		endpointOperationException.setKey("web.api.authentication.verification.blacklisted");
		throw endpointOperationException;
	}

	protected void throwNotMember() {
		EndpointOperationException ex = new EndpointOperationException(true);
		ex.setErrorType(ErrorType.NOT_MEMBER);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setKey("web.api.deviceVerification.notMember");
		throw ex;
	}
	
	protected void throwNoAuthorization(){
		EndpointOperationException ex = new EndpointOperationException(true);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setKey("web.api.noauth");
		throw ex;
	}

	/**
	 * Check that the given user has authorization on the application. If it
	 * does the application will be returned otherwise a convenient
	 * {@link AuthorizationException} will be thrown. If the application doesn't
	 * exist the method will send to the {@link HttpServletResponse} an
	 * {@link HttpServletResponse#SC_NOT_FOUND}
	 * 
	 * @param u
	 * @param appId
	 * @param authorization
	 * @return
	 * @throws IOException
	 */
	protected Application checkAppAccessOrSendError(HttpServletResponse response, User u, Long appId,
			String authorization) throws IOException {

		Application app = appService.findById(appId);
		if (app == null) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "App #" + appId + "Not found");
			return null;
		}
		// check if the user has authorization on the given application
		boolean hasAuthorization = authControlService.has(app, u, authorization);
		if (!hasAuthorization) {
			throw new AuthorizationException();

		}
		return app;
	}

	protected <T> T parseOrSendError(HttpServletResponse response, Class<T> t, InputStream input) throws IOException {
		try {
			T obj = objectMapper.readValue(input, t);
			return obj;
		} catch (JsonParseException e) {
			sendMsg(response, "Unable to read json body. Expected " + t.getName(), HttpServletResponse.SC_BAD_REQUEST);
		} catch (JsonMappingException e) {
			sendMsg(response, "Unable to read json body. Expected " + t.getName(), HttpServletResponse.SC_BAD_REQUEST);
		} catch (java.io.EOFException e) {

			sendMsg(response, "Unable to read json body, Expected " + t.getName() + " but got nothing",
					HttpServletResponse.SC_BAD_REQUEST);
		}
		return null;
	}

	protected <T> T parseOrSendError(HttpServletResponse response, Class<T> t, String json) throws IOException {
		try {
			T obj = objectMapper.readValue(json, t);
			return obj;
		} catch (JsonParseException e) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unable to read json body. Expected " + t.getName());
		} catch (JsonMappingException e) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unable to read json body. Expected " + t.getName());
		}
		return null;
	}

	protected void sendObject(HttpServletResponse response, Object json, int httpCode) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		response.setStatus(httpCode);
		if (json != null) {
			// response.setHeader("x-sodep-mf-class", json.getClass().getName());
			response.setContentType("application/json");
			objectMapper.writeValue(out, json);
		}

	}

	protected static String readString(InputStream in, String charset) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte buff[] = new byte[1024];
		int readBytes = 0;
		while ((readBytes = in.read(buff)) > 0) {
			outStream.write(buff, 0, readBytes);
		}
		String s = new String(outStream.toByteArray(), charset != null ? charset : "UTF-8");
		outStream.close();
		return s;
	}

	protected void sendMsg(HttpServletResponse response, String msg, int httpCode) throws IOException {
		response.setStatus(httpCode);
		String msgToSend = msg;
		if (msgToSend == null) {
			// default message is empty
			msgToSend = "";
		}

		response.setContentType("text/plain");
		response.getWriter().write(msgToSend);
	}

	protected void sendObject(HttpServletResponse response, Object json) throws IOException {
		sendObject(response, json, HttpServletResponse.SC_OK);
	}

	protected void sendError(HttpServletResponse response, int httpCode, String msg) throws IOException {
		sendObject(response, new ErrorResponse(msg), httpCode);
	}

	protected void sendError(HttpServletResponse response, int httpCode, int userLevelError, String msg)
			throws IOException {
		sendObject(response, new ErrorResponse(msg, userLevelError), httpCode);
	}

	protected Long convertToLongOrSendError(HttpServletResponse response, String parameterName, String valueStr)
			throws IOException {
		try {
			Long value = Long.parseLong(valueStr);
			return value;
		} catch (NumberFormatException e) {
			sendMsg(response, parameterName + " must be of type Long", HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}

	protected Integer convertToIntOrSendError(HttpServletResponse response, String parameterName, String valueStr)
			throws IOException {
		try {
			Integer value = Integer.parseInt(valueStr);
			return value;
		} catch (NumberFormatException e) {
			sendMsg(response, parameterName + " must be of type Long", HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}

	protected void writeObject(HttpServletResponse response, Object object) {
		response.setStatus(200);
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			objectMapper.writeValue(out, object);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			close(out);
		}
	}

}
