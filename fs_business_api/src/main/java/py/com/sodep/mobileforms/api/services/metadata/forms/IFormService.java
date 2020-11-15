package py.com.sodep.mobileforms.api.services.metadata.forms;

import java.util.List;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * This class handle the operation to the {@link Form} entity.
 * 
 * The operations can be divided into two groups:
 * <p>
 * Operations that increment the version of the form
 * </p>
 * These are the operations performed basically from the form editor.
 * Changing the definition of the form process items.
 * 
 * <p>
 * Operations that don't increment the version of the form
 * </p>
 * These are operations that will be applied only to the root of a form
 * hierarchy, such as change authorization, change the label of the form.
 * Therefore, a second version of the form will always have null values for the
 * label and the relations to the authorization. However, it will have a
 * different definition than the previous one.
 * 
 * A form and its version are stored together in the table mapped by
 * {@link Form}. If this class returns a {@link FormDTO} the field Id will
 * always represent the original form (the root of the hierarchy). If this class
 * returns a {@link Form} then the id will be the internal id of the form, the
 * field root can be used to reference the id of the original root.
 * 
 * As a rule of thumb, if a method is asking for a formId, the id provided
 * should be the id of the root form. The others id are just for internal use
 * 
 * @author Miguel
 * 
 */
public interface IFormService {
	// TODO change the name of the variable rootId to formId or originalFormId.
	// I don't change it right now because Vinchi is making lot of refactors,
	// and I don't want to make a huge merge for something not very important
	/**
	 * This method will define a new dataSet for the form if it didn't have one
	 * or the formFields have changed. If the formFields have changed it will
	 * create a new version of the dataSet, so data can be later linked
	 * together.
	 * 
	 * @param form
	 */
	void defineDataSet(Form form);

	/**
	 * Return a form on a given version.
	 * 
	 * @param formId
	 * @param version
	 * @return
	 */
	Form getForm(Long formId, Long version);

	/**
	 * Return the last version that was created of a given form
	 * 
	 * @param rootId
	 * @return
	 */
	Long getLastVersion(Long formId);

	/**
	 * 
	 * @param rootId
	 * @return true only if there is a publish version of the form
	 */
	boolean isPublished(Long formId);

	/**
	 * Since the label are lazy loaded we need this method to retrieve the label
	 * for a form on a given language.
	 * 
	 * @param formId
	 * @param language
	 * @return
	 */
	String getLabel(Long formId, String language);

	/**
	 * 
	 * @param formId
	 * @param language
	 *            if language is null the default language will be used
	 * @return
	 */
	FormDTO getFormLastVersion(Long formId, String language);

	FormDTO getFormDTO(Form form, String language);

	PagedData<List<FormDTO>> findByLabel(Application application, Project project, boolean excludeProject, User user,
			String auth, String label, int page, int pageSize, String language);

	PagedData<List<FormDTO>> findFormsOfProject(Project project, User user, String auth, String label, int page,
			int pageSize, String language);

	public PagedData<List<FormDTO>> findFormsOfProject(Project project, User user, String auth, String label, int page,
			int pageSize, String language, String orderBy, boolean asc);

	PagedData<List<FormDTO>> findFormsThatWereOrArePubished(Application application, Project project, User user,
			String auth, String label, int page, int pageSize, String language);

	PagedData<List<FormDTO>> findAllPublished(Application application, Project project, User user, String auth,
			String label, int page, int pageSize, String language);

	List<FormDTO> listAllPublished(Application application, Project project, User user, String auth, String language);

	/**
	 * This method computes all the form where a lookup table is being used.
	 * 
	 * @param lookupTableId
	 * @return
	 */
	public List<Long> getFormsUsingTheLookupTable(Long lookupTableId);

	/**
	 * This method will return the list of lookup tables Identifiers required
	 * for a given form/version form
	 * 
	 * @param formId
	 * @param version
	 * @return an array containing the list of lookup tables Ids that will be
	 *         used in a form or an empty list
	 */
	List<Long> getRequiredLookupTableIds(Long formId, Long version);

	/**
	 * Returns true only if the latest version of the form has no element
	 * prototypes
	 * 
	 * @param formId
	 * @return
	 */
	boolean isFormWithoutElementInstances(Long formId);

	Form findById(Long formId);

	String getOwnerDefaultRole();

	/**
	 * This is a key method because it is the basis of the mapping between the
	 * input element and their columns on the mongo DB. The data types on the
	 * mongo DB are fewer (for simplicity), so there are several input elements
	 * that map to the same data type. For example, decimal and integer both are
	 * mapped to NUMBER
	 * 
	 * @param mfform
	 * @return
	 */
	List<MFField> listFields(MFForm mfform);

	Form logicalDelete(FormDTO dto);

	Long getLastVersionInsecure(Long formId);
    
	boolean isWorkflowDefined(Long formId);
}
