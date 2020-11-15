package py.com.sodep.mobileforms.impl.services.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;

/**
 * This class contains a map of the of form <Language, <Key,Message>> . Language
 * is a language code such as:"en","es","de"
 * (http://www.w3.org/International/O-charset-lang.html). Key is an arbitral
 * string that later can be referenced in code, and message is a string in the
 * target language.
 * 
 * The i18n keys are stored in the Database and loaded using the
 * {@link I18nServiceImpl}
 * 
 * @author danicricco
 * 
 */
@Service("I18nBundle")
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
class I18nBundleImpl implements I18nBundle {

	@Autowired
	private I18nServiceImpl i18nService;

	private ConcurrentHashMap<String, Map<String, String>> cache = new ConcurrentHashMap<String, Map<String, String>>();

	private Map<String, String> languagesMap = null;

	private Object loadSynchLock = new Object();

	@Override
	public String getLabel(String language, String key, String... params) {
		Map<String, String> i18nMap = getI18nMap(language);
		String message = i18nMap.get(key);

		if (message != null && params != null) {
			for (int i = 0; i < params.length; i++) {
				message = message.replaceAll("\\{\\s*" + i + "\\s*\\}", params[i]);
			}
		}
		return message;
	}

	@Override
	public String getLabel(String language, String key, Map<String, String> params) {
		Map<String, String> i18nMap = getI18nMap(language);
		String message = i18nMap.get(key);
		if (message != null && params != null) {
			Set<String> paramKeys = params.keySet();
			for (String paramKey : paramKeys) {
				if (paramKey.matches("\\s")) {
					// TODO log invalid key
				} else {
					String paramValue = params.get(paramKey);
					message = message.replaceAll("\\{\\s*:" + paramKey + "\\s*\\}", paramValue);
				}
			}
		}
		return message;
	}

	/**
	 * Return the map of a given language. If the map has not been loaded it
	 * will load it from the DB
	 * 
	 * @param language
	 * @return
	 */
	private Map<String, String> getI18nMap(String language) {
		Map<String, String> map = cache.get(language);
		if (map == null) {
			// To assure that it is loaded only once
			// (1)
			map = getLanguageMap(language, false);
		}
		return map;
	}

	/**
	 * Return a map from the cache or loads it from the Database
	 * 
	 * @param language
	 * @param forceReload
	 *            if true will always re-load from the DB
	 * @return
	 */
	private Map<String, String> getLanguageMap(String language, boolean forceReload) {
		synchronized (loadSynchLock) {
			// Although we are using a concurrenthashmap we need to check again
			// because several threads could be at
			// once at position (1) and will enter this block after
			Map<String, String> map = cache.get(language);
			if (map == null || forceReload) {
				map = i18nService.getLabels(language);
				map = Collections.unmodifiableMap(map);
				cache.put(language, map);
			}
			return map;
		}
	}

	public void reloadLanguages() {
		Set<String> languagesSet = cache.keySet();
		for (String language : languagesSet) {
			getLanguageMap(language, true);
		}
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

	@Override
	public Map<String, String> getLanguages() {
		if (languagesMap == null) {
			languagesMap = i18nService.listLanguages();
		}
		return languagesMap;
	}

	@Override
	public Map<String, String> getLabels(String isoLanguage, List<String> keys) {
		return i18nService.getLabels(isoLanguage, keys);

	}

}
