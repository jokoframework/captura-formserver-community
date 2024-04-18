package py.com.sodep.mobileforms.api.services.metadata.forms.elements;

import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mobileforms.api.dtos.FormProcessItemInfoDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IElementPrototypeService {

	/**
	 * Get the label in the desired language. If there is no translation on the
	 * desired language the label on the default language will be returned
	 * 
	 * @param elementId
	 * @param language
	 * @return
	 */
	String getLabel(Long elementId, String language);

	String getLabelLastestVersion(Long rootId, String language);

	ElementPrototype create(ElementPrototype ep, Pool pool, String defaultLanguage, String label);

	ElementPrototype update(ElementPrototype ep, Long rootId, Pool pool, String label);

	Select createSelect(Application app, Pool pool, Select entity, List<Map<String, String>> options, User user,
			String defaultLanguage, String label);

	Select updateSelect(Application app, Pool pool, Select entity, Long rootId, List<Map<String, String>> options,
			User user, String label);

	/**
	 * This method returns the MFField associated to the value column of the
	 * select
	 * 
	 * @param selectId
	 * @return
	 */
	public MFField getDefinitionOfValueColumn(Long selectId);

	/**
	 * The list of ElementPrototype (Process Items) whose instantiability is
	 * template and are visible only in the application.
	 * 
	 * This kind of ElementPrototypes are used as templates to create embedded
	 * Process Items.
	 * 
	 * @return
	 */
	List<ElementPrototype> applicationTemplatePrototypes(Application app);

	List<Option> listSelectOptions(Long selectId, String language);

	/**
	 * This method will list the select options where its value has the
	 * associated parameters
	 * 
	 * @param selectId
	 * @param language
	 * @param column
	 *            any of the columns on the underlying lookuptable
	 * @param value
	 *            the value to search for using string equals
	 * @return
	 */
	List<Option> listSelectOptions(Long selectId, String language, String value);

	List<Map<String, String>> getSelectOptions(Long id);

	List<ElementPrototype> findAll(User user, Pool pool, String auth);

	PagedData<List<ElementPrototype>> findAll(String language,User user, Pool pool, String auth, int page, int pageSize);

	PagedData<List<ElementPrototype>> findAll(String language,User user, Application application, String auth, int page, int pageSize);
	public PagedData<List<ElementPrototype>> findAll(String language,User user, Pool pool, String auth, int page, int pageSize,String orderBy,boolean asc) ;

	PagedData<List<ElementPrototype>> findAllNotInPool(String language,User user, Application application, Pool poolToExclude,
			String auth, int page, int pageSize);

	PagedData<List<ElementPrototype>> findByLabel(User user, Application application, String auth, String value,
			int page, int pageSize, String language);

	PagedData<List<ElementPrototype>> findByLabel(User user, Pool pool, String auth, String value, int page,
			int pageSize, String language);

	PagedData<List<ElementPrototype>> findByLabelNotInPool(User user, Application application, Pool poolToExclude,
			String auth, String value, int page, int pageSize, String language);

	PagedData<List<FormProcessItemInfoDTO>> formsUsingProcessItem(Long rootId, String orderBy, boolean ascending,
			Integer page, Integer rows, String language);

	List<Form> formsUsingProcessItem(Long rootId);

	ElementPrototype findLastVersion(Long rootId);

	ElementPrototype importElementPrototypeAndSave(Application app, Long elementRootId, Long destPoolId, User user,
			String language);

	/**
	 * The list of ElementPrototype (Process Items) whose instantiability is
	 * template and are visible system wide.
	 * 
	 * This kind of ElementPrototypes are used as templates to create embedded
	 * Process Items.
	 * 
	 * @return
	 */
	List<ElementPrototype> systemTemplatePrototypes();

	/**
	 * The list of Input whose instantiability type is "template" and are
	 * visible system wide.
	 * 
	 * @return
	 */
	List<Input> systemInputFields();

	ElementPrototype logicalDelete(ElementPrototype prototype);

	String getDefaultLanguageOfProcessItem(Long rootId);
}
