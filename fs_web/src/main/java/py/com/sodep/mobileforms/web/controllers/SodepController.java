package py.com.sodep.mobileforms.web.controllers;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.SodepServiceOriginatedException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class SodepController {

	public static Logger logBaseClass = LoggerFactory.getLogger(SodepController.class);

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	protected IAuthorizationControlService authorizationControlService;

	protected User getUser(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		return manager.getUser();
	}

	protected Map<String, String> getValidationMessages(HttpServletRequest request,
			Set<? extends ConstraintViolation<?>> violations) {
		I18nManager i18n = I18nManager.getI18n(request);
		Map<String, String> messages = new HashMap<String, String>();
		for (ConstraintViolation<?> violation : violations) {
			String message = i18n.getMessage(violation.getMessage());
			messages.put(violation.getPropertyPath().toString(), message);
		}
		return messages;
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

	protected JsonResponse<String> noAuthJsonResponse(I18nManager i18n) {
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(false);
		response.setTitle(i18n.getMessage("web.generic.error"));
		response.setMessage(i18n.getMessage("web.generic.not.enough.permissions"));
		return response;
	}

	/**
	 * This method set the basic error properties and the content
	 * 
	 * @param request
	 * @param i18n
	 * @param response
	 * @param e
	 */
	protected void sodepServiceExceptionToJsonResponse(HttpServletRequest request, I18nManager i18n,
			JsonResponse<?> response, SodepServiceOriginatedException e) {
		response.setSuccess(false);
		if (e.getMessages() != null && !e.getMessages().isEmpty()) {
			response.setContent(translateI18nMessages(request, e.getMessages()));
		}
		response.setTitle(i18n.getMessage("web.generic.error"));
		String message = e.getMessage();
		if (message != null) {
			response.setMessage(i18n.getMessage(message));
		} else {
			response.setMessage(i18n.getMessage("web.generic.error"));
		}
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

}
