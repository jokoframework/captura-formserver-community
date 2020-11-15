package py.com.sodep.mobileforms.api.services.forms.model;

import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.prototype.MFPrototype;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;

public interface IFormModelService {

	/**
	 * 
	 * @param formId
	 * @param version
	 * @param language if language is null will return the form on its default language
	 * @return
	 */
	MFForm getMFForm(Long formId, Long version, String language);

	MFPrototype buildMFPrototype(Application app, ElementPrototype prototype, String language);

	MFForm getMFForm(Long formId, Long version, String language, boolean includeMetadata);

	MFForm getMFForm(Form form, String language, boolean includeMetadata);

}
