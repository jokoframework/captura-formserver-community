package py.com.sodep.mobileforms.web.filters;

import static py.com.sodep.mobileforms.api.constants.WebParameters.PARAM_LANGUAGE;
import static py.com.sodep.mobileforms.web.constants.Attributes.ATTRIBUTE_I18N;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.ContantSystemParams;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

public class I18nFilter implements Filter {

	private I18nBundle i18nBundle;

	private ISystemParametersBundle systemParametersBundle;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig
				.getServletContext());
		i18nBundle = wac.getBean(I18nBundle.class);
		systemParametersBundle = wac.getBean(ISystemParametersBundle.class);
	}

	private boolean isSupported(String language) {
		return ContantSystemParams.supportedLanguages.contains(language);
	}

	/**
	 * This method will do its best to find a suitable language for the user. At
	 * first it will check if the isoLanguage is present. Second it will try to
	 * use the locale of the user Finally it will fault back to the default
	 * langague
	 */
	private String getBestLanguageForUnauthenticatedUser(HttpServletRequest request) {

		// First try to see if the user request the parameter
		String lang = request.getParameter(PARAM_LANGUAGE);
		if (lang != null && isSupported(lang)) {
			return lang;

		}

		// Try to use the user's locale
		Locale locale = request.getLocale();
		lang = locale.getLanguage();
		if (lang != null && isSupported(lang)) {
			return lang;
		}
		// If we still don't have a language, then we should use the default
		IParameter p = getDefaultLanguageParameter();
		lang = p.getValue();
		if (lang != null && isSupported(lang)) {
			return lang;
		} else {
			if (lang == null) {
				throw new RuntimeException("No default language");
			} else {
				throw new RuntimeException("The language '" + lang + "' is not supported");
			}

		}

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		SessionManager sessionManager = new SessionManager(httpRequest);
		if (sessionManager.isOpen()) {
			// if there's a user in the current session, set the session
			// language to the suser's language
			User user = sessionManager.getUser();
			I18nManager i18n = sessionManager.getI18nManager();
			if ((i18n == null && user != null && user.getLanguage() != null)
					|| (i18n != null && user != null && user.getLanguage() != null && !i18n.getSelectedLanguage()
							.equals(user.getLanguage()))) {
				i18n = new I18nManager(i18nBundle, user.getLanguage());
				sessionManager.setI18nManager(i18n);
				chain.doFilter(httpRequest, response);
				return;
			} else if (i18n != null) {
				chain.doFilter(httpRequest, response);
				return;
			}
		}

		String lang = getBestLanguageForUnauthenticatedUser(httpRequest);
		I18nManager i18n = new I18nManager(i18nBundle, lang);
		request.setAttribute(ATTRIBUTE_I18N, i18n);

		chain.doFilter(httpRequest, response);
	}

	private IParameter getDefaultLanguageParameter() {

		IParameter p = systemParametersBundle.getParameter(DBParameters.LANGUAGE);
		return p;
	}

	@Override
	public void destroy() {

	}

}
