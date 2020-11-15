package py.com.sodep.mobileforms.impl.services.metadata.forms;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.page.Page;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;

/**
 * All the methods that affect the Form are encapuslated here. For a service to
 * query the Form see FormService
 * 
 * @author danicricco
 * 
 */
@Service("form.FormModificationService")
@Transactional
public class FormModificationService implements IFormModificationService {

	private static Logger logger = LoggerFactory.getLogger(FormModificationService.class);

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Autowired
	private IFormService formService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IAuthorizationControlService authorizationControlService;

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	private void validateFormUniquenessInProject(Form entity, String language) {
		String label = entity.getLabel(language);

		String queryString = "select count(1) from forms.forms as form "
				+ " inner join forms.forms_labels as form_label on (form.id = form_label.form_id) "
				+ " where form.deleted = false AND form_label.value = :labelValue and form_label.language = :language "
				+ " AND form.project_id = :projectId  AND form.root_id = form.id ";

		if (entity.getId() != null) {
			queryString += " and form.id != :id";
		}

		Query query = em.createNativeQuery(queryString);
		query.setParameter("labelValue", label);
		query.setParameter("language", language);
		query.setParameter("projectId", entity.getProject().getId());

		if (entity.getId() != null) {
			query.setParameter("id", entity.getId());
		}

		BigInteger singleResult = (BigInteger) query.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("admin.cruds.form.invalid.duplicated");
			ex.addMessage("form", "admin.cruds.form.invalid.duplicated");
			throw ex;
		}

	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.CREATE_FORM)
	public FormDTO create(Project project, FormDTO formDTO, User owner, String language) {
		Form form = new Form();
		form.setLabel(language, formDTO.getLabel());
		Project storedProject = projectService.findById(project.getId());
		form.setProject(storedProject);
		form.setDefaultLanguage(language);
		form.setActive(true);
		form.setRoot(form);
		validateFormUniquenessInProject(form, form.getDefaultLanguage());
		em.persist(form);

		if (!em.contains(project)) {
			project = em.find(Project.class, project.getId());
		}

		List<Form> forms = project.getForms();
		if (forms == null) {
			forms = new ArrayList<Form>();
			project.setForms(forms);
		}
		forms.add(form);
		// 'Touch' modification time for the project
		project.preUpdate();

		authorizationControlService.assignRoleToEntity(storedProject.getApplication(),owner.getId(), formService.getOwnerDefaultRole(),
				Authorization.LEVEL_FORM, form.getId());

		return formService.getFormLastVersion(form.getRoot().getId(), language);
	}

	private void updateLabelInner(FormDTO formDTO) {
		Form rootForm = this.formService.findById(formDTO.getId());
		rootForm.setLabel(rootForm.getDefaultLanguage(), formDTO.getLabel());
		validateFormUniquenessInProject(rootForm, rootForm.getDefaultLanguage());
		Project project = rootForm.getProject();
		if (project != null) {
			// 'Touch' modification time for the project
			project = em.merge(project);
			project.preUpdate();
		} else {
			logger.warn("Could not 'touch' project while saving form " + rootForm.getId() + ". The project is null");
		}

	}

	/**
	 * Publish the form on the given version and return the instance of the form
	 * that was publisheds
	 * 
	 * @param formId
	 * @param version
	 * @return
	 */
	private Form publish(Long formId, Long version) {
		Form form = formService.getForm(formId, version);
		if (form == null) {
			throw new IllegalArgumentException("There is no version " + version + " for form #" + formId);
		}
		logger.debug("Publishing form #" + formId + " on verion #" + version);
		Form formRoot = formService.findById(formId);
		// since we have found a version of the form the original form should
		// also exists, so we don't need to do any preventing check

		if (!form.getWasPublished()) {
			formService.defineDataSet(form);
		}
		// The original form should point to the version of the form that was
		// published
		formRoot.setFormPublished(form.getId());
		// formRoot.setWasPublished(true);
		form.setWasPublished(true);
		form.setPublished(true);
		Date now = new Date();
		form.setPublishedDate(new Timestamp(now.getTime()));
		return form;
	}

	@Override
	@Authorizable(formParam = 0, value = AuthorizationNames.Form.PUBLISH)
	public void unpublish(Long formId) {
		Form form = formService.findById(formId);
		if (form.getFormPublished() != null) {
			Form publishedForm = formService.findById(form.getFormPublished());
			publishedForm.setPublished(false);
		}
		form.setFormPublished(null);
	}

	private Form newVersion(Long formId) {
		Long lastVersion = formService.getLastVersion(formId);
		Form lastVersionOfForm = formService.getForm(formId, lastVersion);
		try {
			Form clonedForm = lastVersionOfForm.clone();
			clonedForm.setVersion(lastVersion + 1);
			em.persist(clonedForm);
			return clonedForm;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	// FIXME We might merge this with "update". Rodrigo has proposed that the
	// external services should only call "update" and as a consequence of
	// calling update the Form might advance its own version
	@Authorizable(AuthorizationNames.Form.READ_WEB)
	public Form createNewVersion(Long formId) {
		// TODO do not create a new version if there are no changes to the
		// fields of the form
		Form newForm = newVersion(formId);
		return newForm;

	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.EDIT, formParam = 0)
	public FormDTO updateLabel(FormDTO dto) {
		updateLabelInner(dto);
		return formService.getFormLastVersion(dto.getId(),null);
	}

	@Override
	@Authorizable(formParam = 0, value = AuthorizationNames.Form.PUBLISH)
	public void publish(Long formId) {
		Long lastVersion = formService.getLastVersion(formId);
		publish(formId, lastVersion);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 2)
	// FIXME we shall also check for authorization to modify the target projects
	public Form copyIntoProject(User owner, Project project, Long formId) {
		return copyIntoProject(owner, project, formId, null);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 2)
	// FIXME we shall also check for authorization to modify the target projects
	public Form copyIntoProject(User owner, Project project, Long formId, String label) {
		if (!em.contains(project)) {
			project = em.find(Project.class, project.getId());
		}

		try {
			Form rootForm = formService.findById(formId).getRoot();
			Form latestVersionForm = formService.getForm(formId, formService.getLastVersion(formId));

			Form copiedForm = copiedForm(project, label, rootForm, latestVersionForm);
			validateFormUniquenessInProject(copiedForm, copiedForm.getDefaultLanguage());
			em.persist(copiedForm);
			formService.defineDataSet(copiedForm); // FIXME is it oK?
			authorizationControlService.assignRoleToEntity(copiedForm.getProject().getApplication(),owner.getId(), formService.getOwnerDefaultRole(),
					Authorization.LEVEL_FORM, copiedForm.getId());
			return copiedForm;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private Form copiedForm(Project project, String label, Form rootForm, Form latestVersionForm)
			throws CloneNotSupportedException {
		Form clonedForm = rootForm.clone();
		clonedForm.setRoot(clonedForm);
		clonedForm.setVersion(1L);
		clonedForm.setPublished(false);
		clonedForm.setPublishedDate(null);
		clonedForm.setFormPublished(null);
		clonedForm.setDataSetDefinition(null);
		clonedForm.setDatasetVersion(null);
		clonedForm.setProject(project);
		if (label != null) {
			clonedForm.setLabels(new HashMap<String, String>());
			clonedForm.setLabel(clonedForm.getDefaultLanguage(), label);
		}

		if (latestVersionForm != null) {
			List<Page> pages = latestVersionForm.getPages();
			List<Page> clonedPages = new ArrayList<Page>();
			for (Page p : pages) {
				Page clonedPage = p.clone();
				clonedPage.setForm(clonedForm);
				clonedPages.add(clonedPage);

			}
			clonedForm.setPages(clonedPages);
		}

		return clonedForm;
	}

	@Override
	public void upgradeElementPrototypeInForm(Long formId, Long epRootId) {
		Form form = newVersion(formId);
		ElementPrototype ep = elementPrototypeService.findLastVersion(epRootId);
		for (Page p : form.getPages()) {
			for (ElementInstance ei : p.getElements()) {
				if (ei.getPrototype().getRoot().getId().longValue() == epRootId.longValue()) {
					if (ei.getPrototype().getId().longValue() != ep.getId().longValue()) {
						ei.setPrototype(ep);
					}
				}
			}
		}
	}

	@Override
	public void upgradeElementPrototypeInAllForms(Long epRootId) {
		List<Form> forms = elementPrototypeService.formsUsingProcessItem(epRootId);
		ElementPrototype ep = elementPrototypeService.findLastVersion(epRootId);
		for (Form f : forms) {
			Form form = newVersion(f.getRoot().getId());
			for (Page p : form.getPages()) {
				for (ElementInstance ei : p.getElements()) {
					if (ei.getPrototype().getRoot().getId().longValue() == epRootId.longValue()) {
						if (ei.getPrototype().getId().longValue() != ep.getId().longValue()) {
							ei.setPrototype(ep);
						}
					}
				}
			}
		}
	}
}
