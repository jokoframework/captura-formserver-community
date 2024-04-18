package py.com.sodep.mobileforms.api.entities.forms.common.interfaces;

import java.util.Map;

/**
 * Interface implemented by entities that should have labels indexed by language
 * (http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)
 * 
 * @author Miguel
 * 
 */
public interface I18nLabels {

	Map<String, String> getLabels();

	void setLabels(Map<String, String> labels);

	/**
	 * Return the label on the target language. If there is no translation on
	 * the target language then the label of the default language will be used
	 * */
	String getLabel(String language);

	void setLabel(String language, String text);

}
