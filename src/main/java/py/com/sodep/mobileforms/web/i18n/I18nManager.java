package py.com.sodep.mobileforms.web.i18n;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.session.SessionManager;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * This class lives in the user session and is in charge of associating the i18n
 * selected language with the {@link I18nBundle}. The class also implements the
 * {@link TemplateMethodModel} so it can be used in freemarker templates
 * 
 * @author danicricco
 * 
 */
public class I18nManager implements TemplateMethodModel, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3911913139144768172L;

	private static final Logger logger = LoggerFactory.getLogger(I18nManager.class);

	private I18nBundle i18nBundle;

	private final String selectedLanguage;

	private static final List<String> supportedLanguages = Collections.unmodifiableList(Arrays.asList(new String[] { "en", "es" }));
	
	public static List<String> getSupportedLanguages(){
		return supportedLanguages;
	}
	public I18nManager(I18nBundle i18nBundle, String language) {
		this.i18nBundle = i18nBundle;
		this.selectedLanguage = language;
	}

	/**
	 * This constructor is designed to clone the i18nmanager and assign it a new
	 * language. This way the selectedLanaguage can remain a final function that
	 * doesn't need to be synchronized
	 * 
	 * @param oldManager
	 * @param language
	 */
	public I18nManager(I18nManager oldManager, String language) {
		this.i18nBundle = oldManager.i18nBundle;
		this.selectedLanguage = language;
	}

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if (arguments.size() <= 0) {
			throw new TemplateModelException(
					"i18n method should be used with at least one key argument. Example: ${i18n(\"common.accept\")} ");
		}
		// Obtain the key of the message
		Object keyObj = arguments.get(0);
		if (!(keyObj instanceof String)) {
			throw new TemplateModelException(
					"The first parameter of method i18n must be a String. Example: ${i18n(\"common.accept\")} ");
		}
		String key = (String) keyObj;
		String msg = getMessage(key);
		return msg;
	}

	public String getMessage(String key, String... params) {
		String i18nMessage = i18nBundle.getLabel(selectedLanguage, key, params);
		if (key == null) {
			throw new IllegalArgumentException("It is not possible to use I18nManager.getMessage with a null key");
		}
		if (i18nMessage == null) {
			logger.warn("The key = '" + key + "' is not registered in the i18n table. Language=" + selectedLanguage);
			logger.debug("(lid, '"+key+"', 'msg: "+ key +"'),");
			return key;
		}

		return i18nMessage;
	}

	/***
	 * Obtain the manager of 18n for the given session. Wrapper of
	 * {@link #getI18nUser(HttpSession)}
	 * 
	 * @param request
	 * @return
	 */
	public static I18nManager getI18n(HttpServletRequest request) {
		I18nManager i18n = (I18nManager) request.getAttribute(Attributes.ATTRIBUTE_I18N);
		if (i18n != null) {
			return i18n;
		}
		SessionManager manager = new SessionManager(request);
		return manager.getI18nManager();
	}

	/***
	 * Obtain the manager of 18n for the given session.
	 * 
	 * @param request
	 * @return
	 */
	public static I18nManager getI18nUser(HttpSession session) {
		I18nManager obj = (I18nManager) session.getAttribute(Attributes.ATTRIBUTE_I18N);
		return obj;
	}

	public String getSelectedLanguage() {
		return selectedLanguage;
	}

	

}
