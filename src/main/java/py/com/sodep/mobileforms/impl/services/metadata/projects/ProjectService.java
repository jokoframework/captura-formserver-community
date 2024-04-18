package py.com.sodep.mobileforms.impl.services.metadata.projects;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.services.metadata.AppAwareBaseService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("ProjectService")
@Transactional
class ProjectService extends AppAwareBaseService<Project> implements IProjectService {
	private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

	@Autowired
	private IAuthorizationControlService controlService;

	@Autowired
	private ISystemParametersBundle parameterBundle;

	@Autowired
	private IAuthorizationControlService authorizationControlService;

	protected ProjectService() {
		super(Project.class);
	}

	/**
	 * Load the details of the project in the specified language, if there isn't
	 * any null will be returned.
	 * 
	 * @param projectId
	 * @param language
	 * @return
	 */
	private ProjectDetails loadDetailsOnTheSpecifiedLanguage(Long projectId, String language) {
		TypedQuery<ProjectDetails> q = em.createQuery("SELECT d FROM Project p JOIN p.details d "
				+ " WHERE p.deleted = false AND p.id=:projectId AND d.language=:language", ProjectDetails.class);
		q.setParameter("projectId", projectId);
		q.setParameter("language", language);
		try {
			ProjectDetails details = q.getSingleResult();
			return details;
		} catch (NoResultException e) {
			return null;
		}

	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.READ_WEB, projectParam = 0)
	public ProjectDetails loadDetails(Long projectId, String language) {
		ProjectDetails details = loadDetailsOnTheSpecifiedLanguage(projectId, language);

		if (details == null) {
			Project project = findById(projectId);
			if (project == null) {
				throw new IllegalArgumentException("Asked to obtain the details of an unexisting project");
			}
			details = loadDetailsOnTheSpecifiedLanguage(projectId, project.getDefaultLanguage());
			if (details == null) {
				throw new IllegalArgumentException("The project #" + projectId
						+ " doesn't contain details on its default language");
			}
			return details;
		}
		return details;
	}

	private String fieldNameWithPrefix(final String field) {
		String f = "p.id";
		if (field.matches("active|created")) {
			f = "p." + field;
		} else if (field.matches("language|defaultlanguage|defaultLanguage")) {
			f = "p.defaultlanguage";
		} else if (field.matches("ownerId|owner_id")) {
			f = "p.owner_id";
		} else if (field.equals("mail")) {
			f = "u.mail";
		} else if (field.matches("label|description")) {
			f = "pd." + field;
		}
		return f;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public List<ProjectDTO> listProjects(Application app, User user, String language, String auth) {
		List<Project> projects = authorizationControlService.listProjectsByAuth(app, user, auth);
		List<ProjectDTO> dtos = new ArrayList<ProjectDTO>();
		for (Project p : projects) {
			dtos.add(getProject(p.getId(), language));
		}
		return dtos;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ProjectDTO>> findProjectsByLabelAndExcludeProject(final User user, final Application app,
			Project projectToExclude, final String auth, String label, int pageNumber, int pageSize,
			final String language) {
		List<Project> authProjects = controlService.listProjectsByAuth(app, user, auth);
		// create a list of the projects where the user has enough access rights
		List<Long> ids = new ArrayList<Long>();
		for (Project p : authProjects) {
			if (projectToExclude == null || p != projectToExclude) {
				ids.add(p.getId());
			}
		}

		if (ids.isEmpty()) {
			// if the user doesn't have access rights to any projects, it
			// doesn't even make sense to query the projects
			return new PagedData<List<ProjectDTO>>(Collections.<ProjectDTO> emptyList(), 0L, pageNumber, pageSize, 0);
		}
		// query the database over the selected projects using the a constraint
		// over the label field
		return executeFindByProperty(ProjectDetails.LABEL, BaseService.OPER_CONTAINS, label, ProjectDetails.LABEL,
				true, pageNumber, pageSize, language, ids);
	}

	/**
	 * This is a method that returns a list of {@link ProjectDTO} constrained by
	 * one of the project properties. The method makes internal concatenation of
	 * string to create a dynamic query that can be used for any properties,
	 * therefore the field value should only be used with controlled input to
	 * avoid SQL injection
	 * 
	 * @param propertyName
	 * @param oper
	 * @param value
	 * @param orderBy
	 * @param ascending
	 * @param pageNumber
	 * @param pageSize
	 * @param language
	 * @param ids
	 * @return
	 */
	private PagedData<List<ProjectDTO>> executeFindByProperty(final String propertyName, final String oper,
			Object value, final String orderBy, boolean ascending, int pageNumber, int pageSize, String language,
			List<Long> ids) {

		String fromWhereClause = "From projects.projects p left outer join projects.projects_details pd on p.id=pd.project_id and pd.language=:language "
				+ "join projects.projects_details pd2 on p.id=pd2.project_id and pd2.language=p.defaultlanguage "
				+ " where p.deleted=false AND p.id IN (:projectIds) ";

		if (oper != null) {
			if (propertyName.equals(ProjectDetails.LABEL)) {
				// At first we try to compare against the target language and
				// fall back to the default language. The query use a COALESCE
				// to return the first not null label.
				fromWhereClause += " and lower(COALESCE(pd.label,pd2.label)) like lower(:value)";
			} else {
				fromWhereClause += " and " + whereClause(fieldNameWithPrefix(propertyName), oper, value);
			}

			if (oper.equals(OPER_CONTAINS) || oper.equals(OPER_NOT_CONTAINS)) {
				value = "%" + value.toString().trim().toLowerCase() + "%";
			}
		}
		String selectQueryStr = "SELECT p.* " + fromWhereClause + " ORDER BY " + fieldNameWithPrefix(orderBy)
				+ (ascending ? " ASC" : " DESC");

		Query q = em.createNativeQuery(selectQueryStr, Project.class);
		q.setParameter("value", value);
		q.setParameter("language", language);
		q.setParameter("projectIds", ids);
		q.setMaxResults(pageSize);
		q.setFirstResult((pageNumber - 1) * pageSize);

		@SuppressWarnings("unchecked")
		List<Project> projects = q.getResultList();

		String countQueryStr = "SELECT COUNT(p.*) " + fromWhereClause;

		Query countQuery = em.createNativeQuery(countQueryStr);

		countQuery.setParameter("value", value);
		countQuery.setParameter("language", language);
		countQuery.setParameter("projectIds", ids);
		BigInteger countRes = (BigInteger) countQuery.getSingleResult();
		Long count = countRes.longValue();

		List<ProjectDTO> dtos = new ArrayList<ProjectDTO>();

		for (Project p : projects) {
			ProjectDTO dto = getProject(p, language);
			dtos.add(dto);
		}

		PagedData<List<ProjectDTO>> pagedData = new PagedData<List<ProjectDTO>>(dtos, count, pageNumber, pageSize,
				dtos.size());

		return pagedData;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.READ_WEB, projectParam = 0)
	public ProjectDTO getProject(Long projectId, String language) {
		Project project = this.findById(projectId);
		return getProject(project, language);
	}

	private ProjectDTO getProject(Project project, String language) {

		ProjectDTO projectDTO = new ProjectDTO();
		projectDTO.setId(project.getId());
		projectDTO.setActive(project.getActive());

		ProjectDetails details = this.loadDetails(project.getId(), language);
		projectDTO.setLabel(details.getLabel());
		projectDTO.setDescription(details.getDescription());

		return projectDTO;
	}

	/**
	 * A project is unique in the application where it belongs if there are no
	 * other project with the same label on the same language.
	 * 
	 * @param dto
	 */
	private void validateProjectUniquenessInApplication(ProjectDTO dto) {
		String label = dto.getLabel();
		Long projectId = dto.getId();
		Long applicationId = dto.getApplicationId();
		String language = dto.getLanguage();

		String queryString = "select count(1) " + "from projects.projects as project "
				+ "inner join projects.projects_details as detail on (project.id = detail.project_id) "
				+ "where project.deleted = false " + "and detail.label = :labelValue "
				+ "and detail.language = :language ";

		
		if (applicationId != null) {
			queryString += " and project.application_id = :applicationId";
		}
		if (projectId != null) {
			queryString += " and project.id != :projectId";
		}

		Query query = em.createNativeQuery(queryString);
		query.setParameter("labelValue", label);
		query.setParameter("language", language);
		if (applicationId != null) {
			query.setParameter("applicationId", applicationId);
		}
		if (projectId != null) {
			query.setParameter("projectId", projectId);
		}

		BigInteger singleResult = (BigInteger) query.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("admin.cruds.project.invalid.duplicated");
			ex.addMessage("form", "admin.cruds.project.invalid.duplicated");
			throw ex;
		}
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public String getOwnerDefaultRole() {
		String roleName = parameterBundle.getStrValue(DBParameters.DEFAULT_ROLE_PROJECT_OWNER);
		if (roleName == null) {
			throw new ApplicationException(
					"The default role for project owner is not on the DB. Please check system parameter "
							+ DBParameters.DEFAULT_ROLE_PROJECT_OWNER);
		}
		return roleName;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.EDIT, projectParam = 0)
	public Project edit(Long projectId, User owner, ProjectDTO dto) {
		Project project = findById(projectId);
		return saverOrUpdate(project.getApplication(), owner, dto);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.PROJECT_CANCREATE)
	public Project createNew(Application app, User owner, ProjectDTO dto) {
		return saverOrUpdate(app, owner, dto);
	}

	/**
	 * This is an internal method that is used both for saving or updating an
	 * existing project
	 * 
	 * @param app
	 * @param owner
	 * @param dto
	 * @return
	 */
	private Project saverOrUpdate(Application app, User owner, ProjectDTO dto) {
		logger.debug("Saving a project");

		// Validate that the project label
		validateProjectUniquenessInApplication(dto);

		Project p = null;
		boolean isNew = dto.getId() == null;
		if (isNew) {
			p = new Project();
			p.setApplication(app);
			// the owner should never be changed, only set it the first time
			// when it is being created
			p.setOwner(owner);
			// the default language of the project should not be changed
			p.setDefaultLanguage(dto.getLanguage());
		} else {
			p = em.find(Project.class, dto.getId());
		}

		p.setActive(true);

		// the label and the language are assigned to the project details
		ProjectDetails details = null;
		if (isNew) {
			details = new ProjectDetails();
			p.addProjectDetails(details);
		} else {
			// if its an update needs to locate the language of the project on
			// the details to edit it
			for (ProjectDetails d : p.getDetails()) {
				// an update is always over the default language
				if (d.getLanguage().equals(p.getDefaultLanguage())) {
					details = d;
					break;
				}
			}

			if (details == null) {
				details = new ProjectDetails();
				p.addProjectDetails(details);
			}
		}

		details.setDescription(dto.getDescription());
		details.setLabel(dto.getLabel());
		// an update will only change the label on the default language of the
		// project (see #2280)
		details.setLanguage(p.getDefaultLanguage());

		if (isNew) {
			em.persist(p);
		} else {
			// For an update of the modified time to increase version
			p.preUpdate();
		}
		em.flush();
		if (isNew) {
			// since this method is being used for save an update we need to
			// take special care to only assign the owner role during creating a
			// new project
			authorizationControlService.assignRoleToEntity(app,owner.getId(), getOwnerDefaultRole(),
					Authorization.LEVEL_PROJECT, p.getId());

		}
		return p;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.EDIT)
	public Project logicalDelete(Project project) {
		return super.logicalDelete(project);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public Project findById(Long projectId) {
		return super.findById(projectId);
	}

}
