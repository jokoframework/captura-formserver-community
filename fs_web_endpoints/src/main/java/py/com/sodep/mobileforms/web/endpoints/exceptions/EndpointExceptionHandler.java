package py.com.sodep.mobileforms.web.endpoints.exceptions;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mf.exchange.objects.error.ErrorResponse;
import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.web.exceptions.ExceptionHandler;
import py.com.sodep.mobileforms.web.json.JsonSerializer;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Component
public class EndpointExceptionHandler extends ExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(EndpointExceptionHandler.class);

	@Autowired
	private I18nBundle i18nBundle;

	private static final String DEFAULT_MESSAGE_KEY = "web.api.exception.standardMessage";

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		// FIXME hardcoded path to web api
		if (!path.startsWith("/api/")) {
			return null;
		}

		logger.debug("Exception in call to Web API");
		SessionManager sessionManager = new SessionManager(request);

		String key = DEFAULT_MESSAGE_KEY;
		String[] params = null;

		ErrorResponse errorResponse = new ErrorResponse();

		if (ex instanceof EndpointOperationException) {
			EndpointOperationException endpointOperationException = (EndpointOperationException) ex;

			if (endpointOperationException.isLogToDB()) {
				UncaughtException error = logToDB(ex, handler, request, sessionManager);
				errorResponse.setLogId(error.getId());
			}

			if (endpointOperationException.getKey() != null) {
				key = endpointOperationException.getKey();
				params = endpointOperationException.getParams();
			}

			Integer statusCode = endpointOperationException.getResponseStatusCode();
			if (statusCode != null) {
				response.setStatus(statusCode);
				// http://gohan.sodep.com.py/redmine/issues/3555
				if(statusCode == HttpServletResponse.SC_UNAUTHORIZED){
					 response.setHeader("WWW-Authenticate", "Basic realm=\"none\"");
				}
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			errorResponse.setErrorType(endpointOperationException.getErrorType());
		} else {
			UncaughtException error = logToDB(ex, handler, request, sessionManager);
			errorResponse.setLogId(error.getId());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		errorResponse.setMessage(ex.getMessage());
		errorResponse.setI18NMessages(getMessages(key, params));

		JsonSerializer.writeJSON(response, errorResponse);
		return new ModelAndView();
	}

	private Map<String, String> getMessages(String key, String[] params) {
		Map<String, String> messages = new HashMap<String, String>();
		String[] languages = getAllLanguages();

		for (String language : languages) {
			messages.put(language, i18nBundle.getLabel(language, key, params));
		}
		return messages;
	}

	// FIXME all possible languages
	private String[] getAllLanguages() {
		return new String[] { "en", "es" };
	}
}