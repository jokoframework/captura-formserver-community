package py.com.sodep.mobileforms.web.exceptions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This Component handles all uncaught exceptions that came from the web page.
 * 
 * Any uncaught exception that comes from the API shouldn't be handled here.
 * APIExceptionHandler should take care of the latter.
 * 
 * @author jmpr
 * 
 */
@Component
public class WebGeneralExceptionHandler extends ExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(WebGeneralExceptionHandler.class);

	@Autowired
	private IParametersService paramService;

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		// FIXME hardcoded path to web api
		if (path.startsWith("/api/")) {
			return null;
		}
		
		ModelAndView mav = new ModelAndView();
		if (ex instanceof AuthorizationException) {
			logger.warn("Authorization exception");
			mav.setViewName("/exceptions/authorization.ftl");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		} else if ((ex instanceof HttpMessageNotReadableException) && (ex.getCause() instanceof JsonMappingException)
				&& (ex.getCause().getCause() instanceof XssInputContentException)) {
			logger.warn("XSS input Exception");
			mav.setViewName("/exceptions/authorization.ftl");
			// FIXME, what response code should we use?
			response.setStatus(499);
		} else {
			String msg = "Uncaught Exception ." + ex.getClass().getName() + ": " + ex.getMessage();
			logger.error(msg);
			logger.debug(msg, ex);
			response.setStatus(500);
			mav.setViewName("/exceptions/exception.ftl");
			mav.addObject("exceptionType", ex.getClass().getName());
			mav.addObject("message", ex.getMessage());
			mav.addObject("stackTrace", StringUtils.getStackTraceAsString(ex));
		}

		SessionManager sessionManager = new SessionManager(request);
		UncaughtException error = logToDB(ex, handler, request, sessionManager);
		response.setHeader("mf_error", Long.toString(error.getId()));

		IParameter shouldLogoutParam = paramService.getParameter(DBParameters.LOGOUT_AFTER_ERROR);

		if (shouldLogoutParam.getValueAsBoolean()) {
			// invalidate the session of the user that got an unexpected error.
			// There might be more possibilities to success if he starts a new
			// session :D
			logger.debug("Invalidating session after an unexpected error");
			sessionManager.invalidate();
		}

		return mav;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

}
