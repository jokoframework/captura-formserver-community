package py.com.sodep.mobileforms.web.exceptions;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.logging.IDBLogging;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Component
public abstract class ExceptionHandler implements HandlerExceptionResolver, Ordered {

	@Autowired
	protected IDBLogging dbLogging;

	protected UncaughtException logToDB(Throwable ex, Object handler, HttpServletRequest request,
			SessionManager sessionManager) {
		String url = getUrl(request);
		String userAgent = getUserAgent(request);
		String exceptionType = getExceptionType(ex);
		String offendingClass = getOffendingClassName(handler);
		String stackTrace = getStackTrace(ex);
		stackTrace = ex.getMessage() + "\n" + stackTrace;
		
		UncaughtException uncaughtException = new UncaughtException();
		uncaughtException.setUrl(url);
		uncaughtException.setUserAgent(userAgent);
		uncaughtException.setExceptionType(exceptionType);
		uncaughtException.setOffendingClass(offendingClass);
		uncaughtException.setStackTrace(stackTrace);

		User user = sessionManager.getUser();
		if (user != null) {
			uncaughtException.setUserId(user.getId());
		}

		return dbLogging.logException(uncaughtException);
	}

	private String getStackTrace(Throwable ex) {
		String stackTrace = StringUtils.getStackTraceAsString(ex);
		if (stackTrace != null) {
			stackTrace = StringUtils.truncate(stackTrace, UncaughtException.MAX_STACKTRACE_LENGTH);
		}
		return stackTrace;
	}

	private String getUrl(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		if (url != null) {
			url = StringUtils.truncate(url, UncaughtException.MAX_URL_LENGTH);
		}
		return url;
	}

	private String getUserAgent(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if (userAgent != null) {
			userAgent = StringUtils.truncate(userAgent, UncaughtException.MAX_USERAGENT_LENGTH);
		}
		return userAgent;
	}

	private String getOffendingClassName(Object handler) {
		String offendingClass = null;
		if (handler != null) {
			offendingClass = handler.getClass().getCanonicalName();
			offendingClass = StringUtils.truncate(offendingClass, UncaughtException.MAX_OFFENDINGCLASS_LENGTH);
		}
		return offendingClass;
	}

	private String getExceptionType(Throwable ex) {
		String exceptionType = ex.getClass().getName();
		exceptionType = StringUtils.truncate(ex.getClass().getCanonicalName(),
				UncaughtException.MAX_EXCEPTIONTYPE_LENGTH);
		return exceptionType;
	}

}
