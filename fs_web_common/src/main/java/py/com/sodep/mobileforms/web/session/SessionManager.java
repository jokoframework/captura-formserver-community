package py.com.sodep.mobileforms.web.session;

import static py.com.sodep.mobileforms.web.constants.Attributes.*;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;

import org.springframework.security.core.context.SecurityContextHolder;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.persistence.constants.ContantSystemParams;
import py.com.sodep.mobileforms.web.i18n.I18nManager;


public class SessionManager {

	private HttpServletRequest request;
	private HttpSession session;

	public SessionManager(HttpServletRequest request) {
		this.request = request;
	}

	public SessionManager(HttpSession session) {
		this.session = session;
	}

	public boolean isOpen() {
		if (session != null) {
			return true;
		}
		return this.request.getSession(false) != null;
	}

	public void invalidate() {
		// Invalidate the session and spring context.
		// Reference:
		// http://stackoverflow.com/questions/1013032/programmatic-use-of-spring-security
		HttpSession currentSession = getSession();
		if (currentSession != null) {
			currentSession.invalidate();
		}
		SecurityContextHolder.clearContext();
	}

	public boolean start() {
		HttpSession s = this.request.getSession();
		return s != null;
	}

	public User getUser() {
		HttpSession session = getSession();
		if (session == null) {
			return null;
		}
		return (User) session.getAttribute(ATTRIBUTE_USER);
	}

	public Application getApplication() {
		return (Application) getSession().getAttribute(ATTRIBUTE_APPLICATION);
	}

	public void setApplication(Application app) {
		getSession().setAttribute(ATTRIBUTE_APPLICATION, app);
	}

	public void setUser(User user) {
		getSession().setAttribute(ATTRIBUTE_USER, user);
	}

	public I18nManager getI18nManager() {
		if (getSession() == null || getSession().getAttribute(ATTRIBUTE_I18N) == null)
			return (I18nManager) request.getAttribute(ATTRIBUTE_I18N);
		return (I18nManager) getSession().getAttribute(ATTRIBUTE_I18N);
	}

	public void setI18nManager(I18nManager i18nManager) {
		getSession().setAttribute(ATTRIBUTE_I18N, i18nManager);
	}

	private HttpSession getSession() {
		if (session != null) {
			return session;
		}
		return request.getSession(false);
	}

	@SuppressWarnings("unchecked")
	public List<Application> availableApplications() {
		return (List<Application>) getSession().getAttribute(ATTRIBUTE_AVAILABLE_APPLICATIONS);
	}

	public void setAvailableApplications(List<Application> apps) {
		getSession().setAttribute(ATTRIBUTE_AVAILABLE_APPLICATIONS, apps);
	}

	public void changeLanguage(String language) {
		I18nManager i18nManager = getI18nManager();
		if (i18nManager == null) {
			// this should never happen,since the i18nFilter should guarantee
			// an i18nManger on the user session
			throw new ApplicationException("I18nManager was nullduring change of language");
		}
		if (ContantSystemParams.supportedLanguages.contains(language)) {
			// Creates an i18nManager that has the same internal properties but
			// a
			// different language
			I18nManager cloneI18n = new I18nManager(i18nManager, language);
			setI18nManager(cloneI18n);
		}

	}

	public void setCaptchaChallenge(Captcha captcha) {
		getSession().setAttribute(ATTRIBUTE_CAPTCHA, captcha);

	}

	public Captcha getCaptcha() {
		return (Captcha) getSession().getAttribute(ATTRIBUTE_CAPTCHA);
	}
	
	public void setPositionRowsMap(Map<Integer, Long> mapPosToId) {
		getSession().setAttribute("posToId", mapPosToId);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Long> getPositionRowsMap() {
		return (Map<Integer, Long>) getSession().getAttribute("posToId");
	}
}
