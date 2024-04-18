package py.com.sodep.mobileforms.api.services.i18n;

import java.util.List;
import java.util.Map;

/**
 * An implementation of this interface must provide a cache for i18n labels.
 * 
 * 
 * @author Miguel
 * 
 */
public interface I18nBundle {

	public abstract String getLabel(String language, String key, String... params);

	public abstract String getLabel(String language, String key, Map<String, String> params);

	public abstract void clearCache();

	public abstract Map<String, String> getLanguages();

	public Map<String, String> getLabels(String language, List<String> keys);
	
	public void reloadLanguages();

}
