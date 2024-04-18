package py.com.sodep.mobileforms.api.services.metadata.forms;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;

public interface IFormModificationService {

	FormDTO create(Project p, FormDTO formDTO, User owner, String language);

	/**
	 * Creates a new Form in the project p based on the Form with id formId.
	 * 
	 * The new Form has the same structure (pages, elements) as the latest
	 * version of the form.
	 * 
	 * @param project
	 * @param formDTO
	 * @return
	 */
	Form copyIntoProject(User user, Project project, Long formId);

	/**
	 * Create a new Form in the project p based on the Form with id formId.
	 * 
	 * The new Form has the same structure (pages, elements, etc.) as the latest
	 * version of the form.
	 * 
	 * The new form's default label is set to label. The other labels are not
	 * copied.
	 * 
	 * @param project
	 * @param formId
	 * @param label
	 * @return
	 */
	Form copyIntoProject(User user, Project project, Long formId, String label);

	FormDTO updateLabel(FormDTO dto);

	/**
	 * Unpublish the latest publish version of a given form
	 * 
	 * @param formId
	 */
	void unpublish(Long formId);

	/**
	 * Publish the form on its latest version.
	 * 
	 * @param formId
	 */
	void publish(Long formId);

	// FIXME this method shouldn't return a Form, but changing this might lead
	// to several changes on CommandModelService
	Form createNewVersion(Long formId);

	void upgradeElementPrototypeInForm(Long formId, Long epRootId);

	void upgradeElementPrototypeInAllForms(Long epRootId);

}
