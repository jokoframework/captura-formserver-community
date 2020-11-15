package py.com.sodep.mobileforms.impl.services.metadata.forms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;
import py.com.sodep.mobileforms.utils.StringUtils;

@Service("FormService")
@Transactional
class FormService extends BaseService<Form> implements IFormService {

	private static Logger logger = LoggerFactory.getLogger(FormService.class);

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IDataAccessService dataAccessService;

	@Autowired
	private IAuthorizationControlService controlService;

	@Autowired
	private ISystemParametersBundle parameterBundle;

	@Autowired
	private IFormModelService formModelService;

	protected FormService() {
		super(Form.class);
	}

	@Override
	@Authorizable(AuthorizationNames.Form.EDIT)
	public void defineDataSet(Form form) {
		if (!em.contains(form)) {
			form = em.find(Form.class, form.getId());
		}

		MFForm mfform = formModelService.getMFForm(form, form.getRoot().getDefaultLanguage(), false);

		List<MFField> fields = listFields(mfform);

		String dataSetId = form.getDataSetDefinition();
		Long dataSetVersion = form.getDatasetVersion();
		boolean defined = dataSetId != null && dataSetVersion != null;

		

		MFDataSetDefinition ddl = new MFDataSetDefinition(fields);

		if (!defined) {
			if (fields.size() > 0) {
				// Avoid to save a dataSet for the first time if it doesn't have
				// any fields. Note that this is a normal behaviour, since the
				// user first need to save the form from the "FormManager"
				// (without fields) and on a second step define the form fields
				// using the Form Editor
				// if this is the first form dataset, then create a new one
				ddl = dataAccessService.define(ddl);
				logger.debug("Defined a brand new dataSet for form " + form.getId());
			}
		} else {
			ddl = dataAccessService.addDefinition(form.getDataSetDefinition(), ddl);
			logger.debug("Defined a new dataSet version for form " + form.getId());
		}

		form.setDataSetDefinition(ddl.getMetaDataRef());
		form.setDatasetVersion(ddl.getVersion());
	}

	

	private List<Long> getFormVersionsPublished(Long formId) {

		Query q = em.createQuery("SELECT f.version FROM Form f WHERE f.deleted=false AND f.active=true "
				+ " AND f.root.id=:formId AND f.wasPublished=true ORDER BY f.version desc");
		q.setParameter("formId", formId);

		@SuppressWarnings("unchecked")
		List<Long> versions = (List<Long>) q.getResultList();

		return versions;
	}

	private FormDTO getForm(Long formId) {
		Form form = findById(formId);
		FormDTO formDTO = getFormDTO(form, form.getDefaultLanguage());
		return formDTO;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public Form getForm(Long formId, Long version) {
		TypedQuery<Form> q = em.createQuery("SELECT f FROM Form f WHERE f.root.id=:formId AND f.version=:version",
				Form.class);
		q.setParameter("formId", formId);
		q.setParameter("version", version);
		return q.getSingleResult();
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public Long getLastVersion(Long formId) {
		Query q = em
				.createQuery("SELECT MAX(f.version) FROM " + Form.class.getName() + " f where f.root.id = :formId ");
		q.setParameter("formId", formId);
		Long version = (Long) q.getSingleResult();
		return version;
	}

	@Override
	public Long getLastVersionInsecure(Long formId) {
		Query q = em
				.createQuery("SELECT MAX(f.version) FROM " + Form.class.getName() + " f where f.root.id = :formId ");
		q.setParameter("formId", formId);
		Long version = (Long) q.getSingleResult();
		return version;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public boolean isPublished(Long formId) {
		Form form = findById(formId);
		if (form.getFormPublished() != null) {
			return true;
		}
		return false;

	}

	@Override
	@Authorizable(formParam = 0, value = AuthorizationNames.Form.READ_WEB)
	public String getLabel(Long formId, String language) {
		Form form = findById(formId);
		return form.getLabel(language);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public FormDTO getFormLastVersion(Long formId, String language) {
		Long lastVersion = getLastVersion(formId);
		Form form = getForm(formId, lastVersion);
		FormDTO formDTO = getFormDTO(form, language);
		formDTO.setVersionsWasPublished(this.getFormVersionsPublished(formDTO.getId()));
		return formDTO;
	}

	public boolean isWorkflowDefined(Long formId) {
		Query q = em
		.createQuery("SELECT COUNT(*) FROM " + State.class.getName() + " s where s.formId = :formId and s.deleted = false");
		q.setParameter("formId", formId);
		Long count = (Long) q.getSingleResult();
		return count > 0;
	}

	private List<FormDTO> toDTO(List<Form> listForms, String language) {
		ArrayList<FormDTO> dtos = new ArrayList<FormDTO>();
		for (Form f : listForms) {
			FormDTO dto = getFormDTO(f, language);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public FormDTO getFormDTO(Form form, String language) {
		if (!em.contains(form)) {
			form = em.find(Form.class, form.getId());
		}

		FormDTO fdto = new FormDTO();

		Form rootForm = form.getRoot();
		Long rootId = rootForm.getId();

		fdto.setId(rootId);
		fdto.setLabel(rootForm.getLabel(language));
		fdto.setProjectId(rootForm.getProject().getId());
		ProjectDetails details = projectService.loadDetails(rootForm.getProject().getId(), language);
		if (details == null) {
			details = projectService.loadDetails(rootForm.getProject().getId(), rootForm.getDefaultLanguage());
		}
		fdto.setProjectName(details.getLabel());
		fdto.setVersion(getLastVersion(rootId));
		fdto.setActive(rootForm.getActive());
		if (rootForm.getFormPublished() != null) {
			// there is a form published
			fdto.setPublished(true);
			// The different versions of the form are stored within the same
			// table
			Form publishedForm = findById(rootForm.getFormPublished());
			fdto.setVersionPublished(publishedForm.getVersion());
		} else {
			fdto.setPublished(false);
		}

		return fdto;
	}

	/**
	 * Build a list of all forms where this user has access on the given project
	 * 
	 * @param p
	 * @param user
	 * @param auth
	 * @return
	 */
	private List<Long> getFormsWithAccess(Project p, User user, String auth) {
		ArrayList<Long> ids = new ArrayList<Long>();
		List<Form> formsInProject = controlService.listFormsByAuth(p.getId(), user, auth);
		for (Form f : formsInProject) {
			ids.add(f.getId());
		}
		return ids;
	}

	/**
	 * Builds a list of all forms where this user has the given access on the
	 * application
	 * 
	 * @param application
	 * @param user
	 * @param auth
	 * @return
	 */
	private List<Long> getFormsWithAccess(Application application, User user, String auth) {
		ArrayList<Long> ids = new ArrayList<Long>();
		List<Project> projects = application.getProjects();
		for (Project project : projects) {
			List<Long> formsInProjectIds = getFormsWithAccess(project, user, auth);
			ids.addAll(formsInProjectIds);
		}

		return ids;
	}

	@Authorizable(value = AuthorizationNames.App.FORM_LIST)
	private PagedData<List<FormDTO>> find(Application application, Project project, boolean excludeProject, User user,
			String auth, String label, boolean onlyCurrentlyPublished, boolean onlyPublished, int page, int pageSize,
			String language) {
		return find(application, project, excludeProject, user, auth, label, onlyCurrentlyPublished, onlyPublished,
				page, pageSize, language, null, true);
	}

	@Authorizable(value = AuthorizationNames.App.FORM_LIST)
	private PagedData<List<FormDTO>> find(Application application, Project project, boolean excludeProject, User user,
			String auth, String label, boolean onlyCurrentlyPublished, boolean onlyPublished, int page, int pageSize,
			String language, String orderBy, boolean asc) {
		if (!em.contains(application)) {
			application = em.find(Application.class, application.getId());
		}
		List<Long> ids;

		if (project == null) {
			ids = getFormsWithAccess(application, user, auth);
		} else {
			if (excludeProject) {
				ids = getFormsWithAccess(application, user, auth);
				List<Long> excludedIds = listFormsIds(project);
				ids.removeAll(excludedIds);
			} else {
				ids = getFormsWithAccess(project, user, auth);
			}
		}

		if (ids.isEmpty()) {
			return new PagedData<List<FormDTO>>(Collections.<FormDTO> emptyList(), 0L, page, pageSize, 0);
		}

		return executeFind(page, pageSize, language, label, onlyCurrentlyPublished, onlyPublished, ids, orderBy, asc);

	}

	private List<Long> listFormsIds(Project project) {
		List<Form> forms = project.getForms();
		List<Long> excludedIds = new ArrayList<Long>();
		for (Form f : forms) {
			excludedIds.add(f.getId());
		}
		return excludedIds;
	}

	@Authorizable(AuthorizationNames.App.FORM_LIST)
	public PagedData<List<FormDTO>> findByLabel(Application application, Project project, boolean excludeProject,
			User user, String auth, String label, int page, int pageSize, String language) {
		return find(application, project, excludeProject, user, auth, label, false, false, page, pageSize, language);
	}

	@Authorizable(AuthorizationNames.Project.READ_WEB)
	@Override
	public PagedData<List<FormDTO>> findFormsOfProject(Project project, User user, String auth, String label, int page,
			int pageSize, String language) {
		return find(project.getApplication(), project, false, user, auth, label, false, false, page, pageSize, language);
	}

	@Authorizable(AuthorizationNames.Project.READ_WEB)
	@Override
	public PagedData<List<FormDTO>> findFormsOfProject(Project project, User user, String auth, String label, int page,
			int pageSize, String language, String orderBy, boolean asc) {
		return find(project.getApplication(), project, false, user, auth, label, false, false, page, pageSize,
				language, orderBy, asc);
	}

	@Authorizable(AuthorizationNames.App.FORM_LIST)
	public PagedData<List<FormDTO>> findFormsThatWereOrArePubished(Application application, Project project, User user,
			String auth, String label, int page, int pageSize, String language) {
		return find(application, project, false, user, auth, label, false, true, page, pageSize, language);
	}

	@Authorizable(AuthorizationNames.App.FORM_LIST)
	public PagedData<List<FormDTO>> findAllPublished(Application application, Project project, User user, String auth,
			String label, int page, int pageSize, String language) {
		return find(application, project, false, user, auth, label, true, false, page, pageSize, language);
	}

	@Override
	@Authorizable(AuthorizationNames.App.FORM_LIST)
	public List<FormDTO> listAllPublished(Application application, Project project, User user, String auth,
			String language) {
		// This method is called from a Controller
		// I was getting an Exception failed to lazily initialize a collection
		// of role:
		// py.com.sodep.mobileforms.api.entities.application.Application.projects
		if (!em.contains(application)) {
			application = em.find(Application.class, application.getId());
		}
		List<Long> ids;
		if (project == null) {
			ids = getFormsWithAccess(application, user, auth);
		} else {
			ids = getFormsWithAccess(project, user, auth);
		}
		if (ids != null && !ids.isEmpty()) {
			TypedQuery<Form> q = em.createQuery("FROM " + Form.class.getName()
					+ " f WHERE f.deleted = false and f.id IN  (:ids) "
					+ " AND f.id = f.root.id AND f.formPublished IS NOT NULL ORDER BY f.id", Form.class);
			q.setParameter("ids", ids);
			return toDTO(q.getResultList(), language);
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param page
	 * @param pageSize
	 * @param language
	 * @param label
	 * @param onlyCurrentlyPublished
	 * @param onlyPublished
	 * @param ids
	 * @param orderBy
	 *            the keyword "label" is the only supported and it is the
	 *            default at the same time.
	 * @param asc
	 *            if orderBy is null this won't have any effect.
	 * @return
	 */
	private PagedData<List<FormDTO>> executeFind(int page, int pageSize, String language, String label,
			boolean onlyCurrentlyPublished, boolean onlyPublished, List<Long> ids, String orderBy, boolean asc) {

		String orderStr = "order by lower(COALESCE(l.value,l2.value)) ";
		if (orderBy != null) {
			if (orderBy.equals("label")) {
				// order by the form label
				orderStr = "order by lower(COALESCE(l.value,l2.value)) ";
			}
			// the only supported order is currently label, but we have leave
			// this in order to easily extend it with other columns if is
			// required
			orderStr += (asc) ? " asc " : " desc ";
		}
		String publishConstraint = "";
		if (onlyCurrentlyPublished) {
			// only include forms that are currently publish
			publishConstraint = " and f.published_version is not null ";
		} else {
			if (onlyPublished) {
				// include forms that were once published (they might not be
				// currently publish)
				// Note that this is a super set of onlyCurrentlyPublished
				publishConstraint = " and (f.was_published = true OR " + // the
																			// root
																			// form
																			// was
																			// published
																			// or
																			// one
																			// of
																			// its
																			// childs
						"(SELECT child.id FROM forms.forms child WHERE child.root_id = f.id AND child.was_published = true LIMIT 1) is not null) ";
			}
		}

		// At first we try to compare against the target language and fall back
		// to the default language. The query use a COALESCE to return the first
		// not null label.
		String fromWithWhereSQL = "From forms.forms f left outer join forms.forms_labels l on f.id = l.form_id and l.language=:language "
				+ "join forms.forms_labels l2 on f.id = l2.form_id and l2.language=f.default_language "
				+ "where f.id in ("
				+ StringUtils.toStringWithSeparator(ids, ",")
				+ ") "
				+ "and lower(COALESCE(l.value,l2.value)) like lower(:like) " + publishConstraint;

		Query q = em.createNativeQuery("SELECT * " + fromWithWhereSQL + " " + orderStr, Form.class);

		if (label != null && !label.isEmpty()) {
			label = "%" + label.toLowerCase() + "%";
		} else {
			label = "%";
		}
		q.setParameter("like", label);
		q.setParameter("language", language);
		q.setMaxResults(pageSize);
		q.setFirstResult((page - 1) * pageSize);
		@SuppressWarnings("unchecked")
		List<Form> forms = q.getResultList();
		List<FormDTO> data = toDTO(forms, language);

		// count the number of data
		Query qCount = em.createNativeQuery("Select count(1) " + fromWithWhereSQL);

		qCount.setParameter("like", label);
		qCount.setParameter("language", language);
		BigInteger count = (BigInteger) qCount.getSingleResult();

		return new PagedData<List<FormDTO>>(data, count.longValue(), page, pageSize, data.size());

	}

	public List<Long> getFormsUsingTheLookupTable(Long lookupTableId) {

		Query formsInDropdownSQL = em
				.createNativeQuery("SELECT distinct(f.root_id)  FROM  forms.forms f JOIN forms.pages p ON f.id = p.form_id "
						+ "JOIN forms.element_instances e ON e.page_id = p.id "
						+ "join forms.elements_selects s on e.prototype_id=s.id "
						+ "WHERE p.deleted=false AND e.deleted=false AND s.lookup_identifier=:lookupId "
						+ "group by f.root_id ");

		formsInDropdownSQL.setParameter("lookupId", lookupTableId);
		List<BigInteger> lookupsInDropdown = formsInDropdownSQL.getResultList();

		ArrayList<Long> formIds = new ArrayList<Long>();
		for (BigInteger bFId : lookupsInDropdown) {
			formIds.add(bFId.longValue());
		}

		return formIds;
	}

	@Override
	@Authorizable(value=AuthorizationNames.Form.MOBILE, formParam = 0)
	public List<Long> getRequiredLookupTableIds(Long formId, Long version) {
		String queryStr = "SELECT DISTINCT(identifier) FROM "
				+ " (SELECT DISTINCT(s.lookup_identifier) AS identifier FROM forms.elements_selects s, "
				+ " forms.forms f JOIN forms.pages p ON f.id = p.form_id "
				+ " JOIN forms.element_instances e ON e.page_id = p.id "
				+ " WHERE p.deleted=false AND e.deleted=false AND s.lookup_identifier IS NOT NULL AND "
				+ " s.id=e.prototype_id AND f.root_id=:root_id AND f.version=:version"
				+ " UNION "
				+ " SELECT DISTINCT(e.default_value_lookup) AS identifier FROM forms.forms f JOIN forms.pages p ON f.id = p.form_id"
				+ " JOIN forms.element_instances e ON e.page_id = p.id"
				+ " WHERE p.deleted=false AND e.deleted=false AND e.default_value_lookup IS NOT NULL"
				+ " AND f.root_id=:root_id AND f.version=:version" + " ) AS RequiredLookupTableIds";

		Query query = em.createNativeQuery(queryStr);
		query.setParameter("root_id", formId);
		query.setParameter("version", version);
		@SuppressWarnings("unchecked")
		List<BigInteger> result = query.getResultList();

		ArrayList<Long> l = new ArrayList<Long>();
		for (BigInteger d : result) {
			l.add(d.longValue());
		}
		return l;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public boolean isFormWithoutElementInstances(Long formId) {
		FormDTO formDTO = getForm(formId);
		// FIXME there's no need for this to be a Native query
		String queryStr = " SELECT COUNT(e)  FROM  forms.forms f JOIN forms.pages p ON f.id = p.form_id "
				+ " JOIN forms.element_instances e ON e.page_id = p.id"
				+ " WHERE f.root_id=:root_id AND f.version=:version AND e.deleted=false";
		Query query = em.createNativeQuery(queryStr);
		query.setParameter("root_id", formDTO.getId());
		query.setParameter("version", formDTO.getVersion());
		BigInteger result = (BigInteger) query.getSingleResult();
		Long longValue = result.longValue();
		return longValue == 0;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public Form findById(Long formId) {
		return super.findById(formId);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public String getOwnerDefaultRole() {
		String roleName = parameterBundle.getStrValue(DBParameters.DEFAULT_ROLE_FORM_OWNER);
		if (roleName == null) {
			throw new ApplicationException(
					"The default role for form owner is not on the DB. Please check system parameter "
							+ DBParameters.DEFAULT_ROLE_FORM_OWNER);
		}
		return roleName;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// TODO move this method to MFDataHelper
	public List<MFField> listFields(MFForm mfform) {
		List<MFField> mfFields = new ArrayList<MFField>();
		List<MFElement> elements = mfform.listAllElements();
		if (elements != null) {
			for (MFElement e : elements) {
				if (!e.getProto().isOutputOnly()) {
					MFField mfField = MFFormHelper.mfElementToField(e);
					mfFields.add(mfField);
				}
			}
		}

		return mfFields;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.EDIT)
	public Form logicalDelete(FormDTO dto) {
		Form form = null;
		long lastVersion = dto.getVersion();
		for (long v = 1; v <= lastVersion; v++) {
			form = getForm(dto.getId(), v);
			logicalDelete(form);
		}

		return form;
	}
}
