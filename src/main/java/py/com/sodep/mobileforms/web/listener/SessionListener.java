package py.com.sodep.mobileforms.web.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.web.session.SessionManager;

public class SessionListener implements HttpSessionListener {

	private static Logger logger = LoggerFactory.getLogger(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		logger.debug("Session Created " + se.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();

		// ServletContext servletContext = session.getServletContext();

		SessionManager sessionManger = new SessionManager(session);
		User user = sessionManger.getUser();
		if (user != null) {
			logger.debug("Session Destroyed. Session " + se.getSession().getId() + " User" + user + " ");
		}

		// The user authorization are kept in memory while the user is logged in
		// the system, so we need to release this allocated memory once the user
		// has logged out (#1461)
		// WebApplicationContext appContext = (WebApplicationContext)
		// servletContext
		// .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		// IAuthorizationControlService authControlService =
		// appContext.getBean(IAuthorizationControlService.class);
		// danicricco ( I have commented this, because it is having some
		// unwanted side-effects, see #2496
		// Moreover, the user might be using the same user from several browser
		// and login-out from one browser will invalidate the user authorization
		// cache.
		// authControlService.clearUserAccess(user);
	}

}
