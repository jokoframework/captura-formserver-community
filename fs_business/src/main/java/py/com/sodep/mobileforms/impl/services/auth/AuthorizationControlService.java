package py.com.sodep.mobileforms.impl.services.auth;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntityAuthorization;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.ComputedAuthorizationDTO;
import py.com.sodep.mobileforms.api.services.auth.ComputedAuthorizations;
import py.com.sodep.mobileforms.api.services.auth.DefaultRoles;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.services.metadata.core.GroupValidator;
import py.com.sodep.mobileforms.impl.services.metadata.core.UserServiceValidator;
import py.com.sodep.mobileforms.utils.StringUtils;

@Service("AuthorizationControlService")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
class AuthorizationControlService implements IAuthorizationControlService {

	private static final long SYSTEM_OBJ = 0l;

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationControlService.class);

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Autowired
	private AuthorizationLoader authLoader;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IApplicationService appService;
	
	// A map of the form [app_id]_[user.email] = ComputedAuthorizations
	// The map contains the computed authorization on an application basis
	private ConcurrentHashMap<String, ComputedAuthorizations> userAuthorizationCache = new ConcurrentHashMap<String, ComputedAuthorizations>();

	// A map of the form [user.email] = ComputedAuthorizations
	private ConcurrentHashMap<String, ComputedAuthorizations> systemWideAuthorization = new ConcurrentHashMap<String, ComputedAuthorizations>();

	
	// A map of the form [app.id] = ComputedAuthorizations
	//
	private ConcurrentHashMap<Long, List<String>> appFeatureAuthorization = new ConcurrentHashMap<Long, List<String>>();

	
	@Autowired
	private IGroupService groupService;

	public ComputedAuthorizationDTO obtainComputedAuth(Application app, User u) {
		ComputedAuthorizations computedAuth = getAuthorization(u, app);
		return computedAuth.toDTO();

	}

	@Override
	public void computeUserAccess(User u) {
		User user = userService.findByMail(u.getMail());
		List<Application> apps;
		if (user.isRootUser()) {
			apps = appService.findAll();
		} else {
			apps = userService.listApplications(user);
		}

		if (apps != null) {
			// FIXME I don't know if this loop is correct.
			// The method computeAccess does not use the app as parameter for
			// the computations
			// and those computations are very expensive .
			// Perhaps what we should do is compute first the authorizations
			// and then loop the applications to store the computed
			// authorizations in userAuthorizationCache.
			// 15-07-2015. rvillalba
			//for (Application app : apps) {
			//	computeUserAccess(user, app);
			//}
			
			// CAP-173 The solution applied to solve the problem explained
			// in the previous fixme.
			ComputedAuthorizations computeUserAccess = computeUserAccess(user, null);
			for (Application app: apps) {
				// FIXME  For now, this solution will yield better performance for this
				// method.
				// Although we realized with danicricco that we don't actually need
				// to app.id as part of the key for the userAuthorizationCache.
				// The app.id is already stored as an object in values of this
				// cache.
				// For this reason, in a future refactor would be ideal to stop
				// using the app.id as a key
				// and only use the user.id.
				userAuthorizationCache.put(app.getId() + "_" + user.getMail(), computeUserAccess);
			}
		}

	}

	/**
	 * This method pre-computes the user access rights so the system doesn't
	 * need to query the DB all the time to check for user access. If an
	 * authorization of a higher level have been granted to an upper layer, it
	 * will bubble down to all objects contained within the assigned level. For
	 * example an authorization of level 'form' granted to an 'application' will
	 * mean that all forms contained within the application will be granted the
	 * given access.
	 */
	@SuppressWarnings("unchecked")
	public ComputedAuthorizations computeUserAccess(User u, Application app) {
		
		logger.trace("[computeUserAccess] Computing user access: user=" + u.getId());

		ComputedAuthorizations computedAuth = new ComputedAuthorizations(Authorization.MAX_NUMBER_OF_LEVELS);

		// This query will not load deleted objects from the DB
		// This should improve performance during computation because it won't
		// be required to check for every authorization if the object is still
		// valid (see #2930)
		Query query = em
				.createNativeQuery(
						"select ae.*,app.name,proj.deleted,proj.id,ae.project_id ,proj.deleted from core.authorizable_entities_authorizations  ae "
								+ "left outer join applications.applications app on ae.application_id =app.id "
								+ "left outer join projects.projects proj on ae.project_id =proj.id "
								+ "left outer join forms.forms form on ae.form_id =form.id "
								+ "left outer join pools.pools pool on ae.pool_id =pool.id "
								+ "where authorizable_entity_id=:entityId "
								+ "and (ae.application_id is null or  app.deleted=false) "
								+ "and (ae.project_id is  null or proj.deleted=false) "
								+ "and (ae.form_id is null or form.deleted=false) "
								+ "and (ae.pool_id is null or pool.deleted=false) ",
						AuthorizableEntityAuthorization.class);

		query.setParameter("entityId", u.getId());

		List<AuthorizableEntityAuthorization> authToEntity = query.getResultList();
		
		for (AuthorizableEntityAuthorization authOnMFObj : authToEntity) {
			// grant all the authorization that have been granted directly to
			// the
			// user
			grantAuthorizationToUser(u, computedAuth, authOnMFObj);

		}
		
		
		// FIXME just implemented the method listGroupIds
		// I realized that the Access Rights are computed for all groups
		// without taking into account the application the user has login to.
		// If this is not wrong it might, at least, mean some extra computation
		// on objects that have a high chance of not being used.
		// 11/11/2012 - jmpr
		List<Long> groups = userService.listGroupsIds(u);
		for (Long groupId : groups) {
			query.setParameter("entityId", groupId);
			authToEntity = query.getResultList();
			for (AuthorizableEntityAuthorization authOnMFObj : authToEntity) {
				// grant all the authorization that have been granted to the
				// group where a user belongs
				grantAuthorizationToUser(u, computedAuth, authOnMFObj);

			}
		}

		if (app != null) {
			// CAP-173 This change is applied to delay the storage
			// of the app.id as a key in this cache.
			userAuthorizationCache.put(app.getId() + "_" + u.getMail(), computedAuth);
			appFeatureAuthorization.put(app.getId(), getFeatures(app));
			
		}
		// system wide authorization are the same across all authorization, so
		// we can just keep the latest computatin whenever the application
		// authorizations are computed
		// In addition, the method #has(User user, String auth) doesn't need to
		// receive the application in order to check system level authorizations
		systemWideAuthorization.put(u.getMail(), computedAuth);
		
		
		logger.trace("[computeUserAccess] User access computation done.");
		
		return computedAuth;
	}

	private List<String> getFeatures(Application app) {
		List<String> features = new CopyOnWriteArrayList<>();
		if (app.getHasWorkflow()) {
			features.add(AuthorizationNames.Feature.WORKFLOW);
		}
		return features;
	}

	private ComputedAuthorizations getAuthorization(User user, Application app) {
		if (user == null) {
			throw new AuthorizationException("Can't determine authorization for an empty user");
		}
		if (app == null) {
			throw new AuthorizationException("Can't determine authorization for an empty app");
		}

		ComputedAuthorizations computedAuthorizations = userAuthorizationCache.get(app.getId() + "_" + user.getMail());
		if (computedAuthorizations == null) {

			computedAuthorizations = computeUserAccess(user,app);
		}
		return computedAuthorizations;
	}

	public void clearUserAccess(User u) {
		userAuthorizationCache.remove(u.getMail());
	}

	private boolean isLivingObject(int grantedLevel, long obj) {
		switch (grantedLevel) {
		case Authorization.LEVEL_FORM:
			Form f = formService.findById(obj);
			if (f != null && !f.getDeleted()) {
				return true;
			}

		case Authorization.LEVEL_PROJECT:
			Project project = projectService.findById(obj);
			if (project != null && !project.getDeleted()) {
				return true;
			}

		case Authorization.LEVEL_POOL:
			Pool pool = poolService.findById(obj);
			if (pool != null && !pool.getDeleted()) {
				return true;
			}

		case Authorization.LEVEL_APP:
			Application app = appService.findById(obj);
			if (app != null && !app.getDeleted()) {
				return true;
			}

		}
		return false;
	}

	// FIXME This method takes way too long to execute and needs a refactor.
	// It's the root cause for CAP-173 and CAP-127, which were solved in upper layers 
	// to avoid changes in this method.
	private void grantAuthorizationToUser(User u, ComputedAuthorizations computedAuth,
			AuthorizableEntityAuthorization authOnMFObj) {
		Integer grantedLevel = authOnMFObj.getAuthLevel();

		AuthorizationLevelConf authConf = authLoader.getConfLevel(grantedLevel);
		String column = authConf.getColumn();
		long obj;
		try {

			if (!column.equals("")) {
				String objStr = BeanUtils.getProperty(authOnMFObj, column);
				obj = Long.parseLong(objStr);
			} else {
				obj = SYSTEM_OBJ;
			}

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		// grant to the user all authorizations that the role gives
		List<Authorization> authorizations = authOnMFObj.getRole().getGrants();
		for (Authorization auth : authorizations) {
			// grant authorization to the user
			grantAuthorization(u, computedAuth, grantedLevel, obj, auth);
			// grant automatically all dependent authorization

			List<Authorization> dependencies = auth.getDependentAuthorizations();
			for (Authorization dependantAuth : dependencies) {
				if (grantedLevel <= dependantAuth.getLevel()) /*
															 * #
															 * assingedToAHigherObject
															 */{
					// if the authorization is over an object of the same level
					// or bellow, then the authorization can be granted directly
					// to the object
					// This is the case of form.edit or project.edit granted at
					// project or application level
					grantAuthorization(u, computedAuth, grantedLevel, obj, dependantAuth);
				} else {
					// However, if the authorization was granted to a lower
					// object, then we need to identify the adequate object in
					// order to assign the dependent authorization
					// This is the case of form.edit->project.edit granted at
					// form level
					switch (grantedLevel) {
					case Authorization.LEVEL_FORM:
						Form f = formService.findById(obj);
						Project p = f.getProject();
						Application app = p.getApplication();
						if (dependantAuth.getLevel() == Authorization.LEVEL_PROJECT) {
							// if the dependent authorization is of level
							// project, then grant access to the project of the
							// form
							grantAuthorization(u, computedAuth, Authorization.LEVEL_PROJECT, p.getId(), dependantAuth);
						} else if (dependantAuth.getLevel() == Authorization.LEVEL_APP) {
							// if the dependent authorization is of level app,
							// then grant access to the app where the form
							// belongs
							grantAuthorization(u, computedAuth, Authorization.LEVEL_APP, app.getId(), dependantAuth);
						}
						break;
					case Authorization.LEVEL_PROJECT:
						Project project = projectService.findById(obj);
						if (dependantAuth.getLevel() == Authorization.LEVEL_APP) {
							// Identify the app whre the project belongs to and
							// assign the dependent authorization to the app
							// level
							grantAuthorization(u, computedAuth, Authorization.LEVEL_APP, project.getApplication()
									.getId(), dependantAuth);
						} else {
							logger.warn("Avoding transitive grant to " + dependantAuth
									+ " because of wrong dependency. " + auth + " -> " + dependantAuth
									+ " . Granted at level " + grantedLevel);
						}

						break;
					case Authorization.LEVEL_POOL:
						Pool pool = poolService.findById(obj);
						if (dependantAuth.getLevel() == Authorization.LEVEL_APP) {
							// Identify the app where the pool belongs to and
							// assign the dependent authorization to the app
							// level
							grantAuthorization(u, computedAuth, Authorization.LEVEL_APP, pool.getApplication().getId(),
									dependantAuth);
						} else {
							logger.warn("Avoding transitive grant to " + dependantAuth
									+ " because of wrong dependency. " + auth + " -> " + dependantAuth
									+ " . Granted at level " + grantedLevel);
						}

						break;
					case Authorization.LEVEL_APP:
					case Authorization.LEVEL_SYSTEM:
						// This shouldn't happen, since this are top level
						// authorizations. Look at the label
						// #assingedToAHigherObject
					default:
						throw new IllegalStateException(
								"Unable to traverse object hierarchy in order to assign dependent authorizations. Relation "
										+ auth + " -> " + dependantAuth + ". Granted at level " + grantedLevel);
					}
				}
			}
		}

	}

	private void grantAuthorization(User u, ComputedAuthorizations computedAuth, int grantedLevel, long obj,
			Authorization auth) {
		if (grantedLevel == auth.getLevel()) {
			// The authorization has been granted to an object of the
			// same level
			// For example, an authorization of level project granted to
			// a project
			computedAuth.addAuthorization(auth.getLevel(), obj, auth.getName());
			// #1479
			// If a user has an authorization assigned over a WF, the user
			// should also be granted READ access on the project
			// After implementing #2471 this is no longer required
			// if (auth.getLevel() == Authorization.LEVEL_FORM) {
			// grantAccessOnParentProject(computedAuth, obj);
			// }
		} else {
			if (grantedLevel < auth.getLevel()) {
				// only authorizations that are from a lower level (high
				// number) can be
				// assigned to the upper level (lower number). The
				// opposite
				// is not true.
				// For example, an authorization of level APP can't be
				// associated to a form level.
				// This shouldn't actually happen, but it is an extra
				// check
				// in case somebody got access to the DB and assigned
				// itself
				// some extra roles

				// We start by checking at project level, because
				// authorization of app level and system level can only
				// be assigned to their coresponding levels
				if (auth.getLevel() == Authorization.LEVEL_PROJECT) {
					// An authorization of level project can be granted
					// to the whole application
					if (grantedLevel == Authorization.LEVEL_APP) {
						// if the authorization was granted over an app,
						// then grant the authorization to all projects
						// on
						// the
						// application
						List<Long> projectIds = listProjectIds(obj);
						for (Long projectId : projectIds) {
							computedAuth.addAuthorization(Authorization.LEVEL_PROJECT, projectId, auth.getName());
						}
					}

				} else if (auth.getLevel() == Authorization.LEVEL_FORM) {
					// an authorization of level form can be granted to
					// an application or to a project
					List<Long> formIds = null;

					if (grantedLevel == Authorization.LEVEL_APP) {
						// grant the authorization to all forms within
						// the application
						formIds = listFormIds(obj, null);
					}
					if (grantedLevel == Authorization.LEVEL_PROJECT) {
						// grant the authorization to all forms within
						// the given project
						formIds = listFormIds(null, obj);
					}
					if (formIds != null) {
						for (Long formId : formIds) {
							computedAuth.addAuthorization(Authorization.LEVEL_FORM, formId, auth.getName());
							grantAccessOnParentProject(computedAuth, formId);
						}
					}
				} else if (auth.getLevel() == Authorization.LEVEL_POOL) {
					// an authorization of level pool can be granted to
					// an application level
					if (grantedLevel == Authorization.LEVEL_APP) {
						// grant authorization to all pools within the
						// application
						List<Long> poolIds = listPoolIds(obj);
						if (poolIds != null) {
							for (Long poolId : poolIds) {
								computedAuth.addAuthorization(Authorization.LEVEL_POOL, poolId, auth.getName());
							}
						}
					}
				}
			} else {
				logger.warn("The user " + u + " has the access to the authorization " + auth.getName() + " at a "
						+ grantedLevel + ", but the authorization" + auth.getName() + " is meant for level #"
						+ auth.getLevel());
			}
		}
	}

	/**
	 * Grants READ access on the project the Form is in.
	 * 
	 * @param computedAuth
	 * @param formId
	 */
	// related to bug #1479
	private void grantAccessOnParentProject(ComputedAuthorizations computedAuth, long formId) {
		Form f = formService.findById(formId);
		Project p = f.getProject();
		computedAuth.addAuthorization(Authorization.LEVEL_PROJECT, p.getId(), AuthorizationNames.Project.READ_WEB);
		// computedAuth.addAuthorization(Authorization.LEVEL_PROJECT, p.getId(),
		// AuthorizationNames.Project.READ_MOBILE); // ?
	}

	private void computeEntityAccess(Long entityId) {
		computeEntityAccess(entityId, null, null);
	}

	/**
	 * Whenever an entity access rights have been modified, the computed access
	 * needs to be modified. If the parameters application and authOnMFObj are
	 * not null and the entity is a user it will only add the granted access and
	 * not make a full recompute. Since this method could be invoked after a
	 * user has created an object it is very expensive to recompute everything
	 * just to grant access to a particular object (see #2934)
	 * 
	 * 
	 * @param entity
	 */
	private void computeEntityAccess(Long entityId, Application app, AuthorizableEntityAuthorization authOnMFObj) {
		AuthorizableEntity entity = em.find(AuthorizableEntity.class, entityId);
		if (entity instanceof User) {
			User u = (User) entity;
			// only
			if (app != null && authOnMFObj != null) {
				ComputedAuthorizations computedAuth = getAuthorization(u, app);
				grantAuthorizationToUser(u, computedAuth, authOnMFObj);
			}else{
				computeUserAccess(u);
			}
			
		} else if (entity instanceof Group) {
			Group g = (Group) entity;
			// Remember that g.getUsers(); will return also logically deleted
			// users
			// Set<User> users = g.getUsers();
			List<User> users = groupService.listUsers(g);
			for (User u : users) {
				// computed the user access for all users that belong to the
				// group
				//
				computeUserAccess(u);
			}
		}
		logger.trace("Entity access computed for entityId=" + entityId);
		
	}

	/**
	 * Checks if the user has the auth, system-wide
	 */
	@Override
	public boolean has(User user, String auth) {

		ComputedAuthorizations computedAuth = systemWideAuthorization.get(user.getMail());
		return computedAuth.hasAccess(Authorization.LEVEL_SYSTEM, SYSTEM_OBJ, auth);

	}

	/**
	 * Checks if the application has a feature enabled.
	 */
	@Override
	public boolean hasFeature(Application app, String feature) {
		List<String> features = appFeatureAuthorization.get(app.getId());

		return features != null && features.contains(feature);
	}
	
	@Override
	public boolean hasAppLevelAccess(Long appId, User user, String auth) {
		// TODO check directly with the app id on the computed object
		Application application = em.find(Application.class, appId);
		if (application == null) {
			throw new IllegalArgumentException("Couldn't find application #" + appId);
		}
		return has(application, user, auth);
	}

	@Override
	public boolean hasProjectLevelAccess(Long projectId, User user, String auth) {
		Project project = em.find(Project.class, projectId);
		if (project == null) {
			throw new IllegalArgumentException("Couldn't find project #" + projectId);
		}
		return has(project, user, auth);
	}

	@Override
	public boolean hasFormLevelAccess(Long formId, User user, String auth) {
		Form form = em.find(Form.class, formId);
		if (form == null) {
			throw new IllegalArgumentException("Couldn't find form #" + formId);
		}
		return has(form, user, auth);
	}

	@Override
	public boolean hasPoolLevelAccess(Long poolID, User user, String auth) {
		Pool pool = em.find(Pool.class, poolID);
		if (pool == null) {
			throw new IllegalArgumentException("Couldn't find pool #" + poolID);
		}
		return has(pool, user, auth);
	}

	@Override
	public boolean has(Application app, User user, String auth) {

		ComputedAuthorizations computedAuth = getAuthorization(user, app);
		return computedAuth.hasAccess(Authorization.LEVEL_APP, app.getId(), auth)
				|| computedAuth.hasAccess(Authorization.LEVEL_SYSTEM, SYSTEM_OBJ, auth);

	}

	@Override
	public boolean has(Project project, User user, String auth) {
		ComputedAuthorizations computedAuth = getAuthorization(user, project.getApplication());
		return computedAuth.hasAccess(Authorization.LEVEL_PROJECT, project.getId(), auth)
				|| computedAuth.hasAccess(Authorization.LEVEL_SYSTEM, SYSTEM_OBJ, auth);
	}

	@Override
	public boolean has(Form form, User user, String auth) {
		ComputedAuthorizations computedAuth = getAuthorization(user, form.getProject().getApplication());
		return computedAuth.hasAccess(Authorization.LEVEL_FORM, form.getId(), auth)
				|| computedAuth.hasAccess(Authorization.LEVEL_SYSTEM, SYSTEM_OBJ, auth);
	}

	@Override
	public boolean has(Pool pool, User user, String auth) {
		ComputedAuthorizations computedAuth = getAuthorization(user, pool.getApplication());
		return computedAuth.hasAccess(Authorization.LEVEL_POOL, pool.getId(), auth)
				|| computedAuth.hasAccess(Authorization.LEVEL_SYSTEM, SYSTEM_OBJ, auth);
	}

	@Override
	public boolean has(Application app, User user, String auth, int level, Long objId) {
		ComputedAuthorizations computedAuth = getAuthorization(user, app);
		Long o = objId;
		if (objId == null) {
			o = SYSTEM_OBJ;
		}
		return computedAuth.hasAccess(level, o, auth);
	}

	/**
	 * Returns a list of roles that the entity has at the level (over the objId)
	 * 
	 * @param app
	 * @param entity
	 * @param level
	 * @param objId
	 * @param nameLike
	 * @return
	 */
	private List<Role> listAssignedRolesOverObj(Application app, Long entityId, int level, long objId, String nameLike) {
		boolean nameFilter = nameLike != null && !(nameLike = nameLike.trim()).isEmpty();
		List<Role> roles = Collections.emptyList();
		AuthorizationLevelConf confLevel = authLoader.getConfLevel(level);

		String qStr = "SELECT a.role FROM "
				+ AuthorizableEntityAuthorization.class.getName()
				+ " a WHERE a.role.deleted = false AND a.entityId = :entityId AND a.role.application = :app AND a.authLevel = :level "
				+ " AND a." + confLevel.getColumn() + " = :objId";

		if (nameFilter) {
			qStr += " AND lower(a.role.name) LIKE :nameLike";
		}

		TypedQuery<Role> query = em.createQuery(qStr, Role.class);
		query.setParameter("app", app);
		query.setParameter("entityId", entityId);
		query.setParameter("level", level);
		query.setParameter("objId", objId);

		if (nameFilter) {
			query.setParameter("nameLike", "%" + nameLike.toLowerCase() + "%");
		}
		roles = query.getResultList();
		return roles;
	}

	@Override
	public List<Role> listAssignedRoles(Application app, AuthorizableEntity entity, String nameLike) {
		return listAssignedRolesOverObj(app, entity.getId(), Authorization.LEVEL_APP, app.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Project project, AuthorizableEntity entity, String nameLike) {
		Application app = project.getApplication();
		return listAssignedRolesOverObj(app, entity.getId(), Authorization.LEVEL_PROJECT, project.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Project project, Long entityId, String nameLike) {
		Application app = project.getApplication();
		return listAssignedRolesOverObj(app, entityId, Authorization.LEVEL_PROJECT, project.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Form form, AuthorizableEntity entity, String nameLike) {
		Application app = form.getProject().getApplication();
		return listAssignedRolesOverObj(app, entity.getId(), Authorization.LEVEL_FORM, form.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Form form, Long entityId, String nameLike) {
		Application app = form.getProject().getApplication();
		return listAssignedRolesOverObj(app, entityId, Authorization.LEVEL_FORM, form.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Pool pool, AuthorizableEntity entity, String nameLike) {
		Application app = pool.getApplication();
		return listAssignedRolesOverObj(app, entity.getId(), Authorization.LEVEL_POOL, pool.getId(), nameLike);
	}

	@Override
	public List<Role> listAssignedRoles(Pool pool, Long entityId, String nameLike) {
		Application app = pool.getApplication();
		return listAssignedRolesOverObj(app, entityId, Authorization.LEVEL_POOL, pool.getId(), nameLike);
	}

	// FIXME this could be refactored to make it work for project and form level
	// but it's not necessary right now - jmpr 06/11/2012
	@Override
	public List<Role> listAssignableRoles(Application app, AuthorizableEntity entity, String nameLike) {
		boolean nameFilter = nameLike != null && !(nameLike = nameLike.trim()).isEmpty();
		if (!em.contains(entity)) {
			entity = em.find(AuthorizableEntity.class, entity.getId());
		}

		List<Role> assignedRoles = listAssignedRoles(app, entity, nameLike);

		String qStr = "SELECT r FROM " + Role.class.getName() + " r "
				+ " WHERE r.deleted=false AND r.application = :app AND r.authLevel >= :minLevel ";
		boolean hasAssignedRoles = assignedRoles != null && !assignedRoles.isEmpty();
		if (hasAssignedRoles) {
			qStr += " AND r NOT IN :assignedRoles";
			;
		}

		// The roles that are applicable at application level that the user
		// doesn't already have
		if (nameFilter) {
			qStr += " AND lower(r.name) LIKE :nameLike";
		}

		TypedQuery<Role> query = em.createQuery(qStr, Role.class);
		query.setParameter("app", app);
		query.setParameter("minLevel", Authorization.LEVEL_APP);
		if (hasAssignedRoles) {
			query.setParameter("assignedRoles", assignedRoles);
		}
		if (nameFilter) {
			query.setParameter("nameLike", "%" + nameLike.toLowerCase() + "%");
		}
		return query.getResultList();
	}

	/**
	 * This method should only be used for default roles. It is just a wrapper
	 * over {@link #assignRoleToEntity(Long, Long, int, Long)}
	 * 
	 * @param entityId
	 * @param roleName
	 * @param level
	 * @param objId
	 */
	@Override
	public void assignRoleToEntity(Application app, Long entityId, String roleName, int level, Long objId) {
		Role role = roleService.getDefaultRole(roleName);
		if (role == null) {
			throw new ApplicationException("The role " + roleName
					+ " doesn't exists. Check the class DefaultRolesLoader for tips about configuring it");
		}
		assignRoleToEntity(app,entityId, role.getId(), level, objId);

	}

	/**
	 * This is just a wrapper over
	 * {@link #assignRolesToEntity(Long, List, int, Long)}
	 * 
	 * @param entityId
	 * @param roles
	 * @param level
	 * @param objId
	 */
	private void assignRolesToEntity(Long entityId, List<Long> roles, int level, Long objId) {
		for (Long roleId : roles) {
			doAssignRoleToEntity(entityId, roleId, level, objId);
		}
	}

	private int removeRolesFromEntity(Long entityId, List<Long> rolesId, int level, Long objId) {
		AuthorizationLevelConf conf = authLoader.getConfLevel(level);
		String columnName = conf.getColumn();
		String objField = "";
		if (columnName != null && !columnName.trim().equals("")) {
			objField = " and A." + columnName + " = :objId";
		}
		Query query = em.createQuery("Delete from " + AuthorizableEntityAuthorization.class.getName()
				+ " A where A.entityId=:entityId AND A.role.id IN (:rolesId) " + objField);
		if (!objField.equals("")) {
			query.setParameter("objId", objId);
		}
		query.setParameter("entityId", entityId);
		query.setParameter("rolesId", rolesId);
		int deletedRows = query.executeUpdate();
		computeEntityAccess(entityId);
		return deletedRows;
	}

	@Override
	public void assignRoleToEntity(Application app, Long entityId, Long roleId, int level, Long objId) {
		AuthorizableEntityAuthorization access = doAssignRoleToEntity(entityId, roleId, level, objId);
		computeEntityAccess(entityId, app, access);

	}

	@Override
	public void assignRoleToEntity(Long entityId, Long roleId, int level, Long objId) {
		doAssignRoleToEntity(entityId, roleId, level, objId);
		computeEntityAccess(entityId);
	}
	
	/**
	 * Assign a role to a given entity that will has effect on the given level
	 * only over the objId. If objId is null it means that all authorization
	 * associated with the role will be granted to the user over all objects of
	 * the given level. This method is idempotent
	 * 
	 * @param entityId
	 * @param roleId
	 * @param level
	 * @param objId
	 */

	private AuthorizableEntityAuthorization doAssignRoleToEntity(Long entityId, Long roleId, int level, Long objId) {
		// AuthorizableEntityAuthorization
		AuthorizationLevelConf conf = authLoader.getConfLevel(level);
		String columnName = conf.getColumn();
		Role role = roleService.findById(roleId);
		if (role.getAuthLevel() < level) {
			// It is not possible to assign a role that contains permission from
			// a broader level to an object of a lower level
			throw new AuthorizationException("Can't assign the role " + role.getName() + " to the level " + level);
		}

		String objField = "";
		if (columnName != null && !columnName.trim().equals("")) {
			objField = " and A." + columnName + " = :objId";
		}
		TypedQuery<AuthorizableEntityAuthorization> query = em.createQuery("From "
				+ AuthorizableEntityAuthorization.class.getName()
				+ " A where A.entityId=:entityId and A.role.id=:role " + objField,
				AuthorizableEntityAuthorization.class);

		if (!objField.equals("")) {
			query.setParameter("objId", objId);
		}
		query.setParameter("entityId", entityId);
		query.setParameter("role", roleId);

		List<AuthorizableEntityAuthorization> resultList = query.getResultList();
		if (resultList.size() <= 0) {
			// We will only insert if the role has not been previously assigned
			AuthorizableEntityAuthorization bean = new AuthorizableEntityAuthorization();
			try {
				// assign the column with rtti because we don't know the
				// fieldName in advance
				BeanUtils.setProperty(bean, columnName, objId);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(e);
			}
			bean.setRole(role);
			bean.setEntityId(entityId);
			bean.setAuthLevel(level);
			em.persist(bean);
			return bean;
		} else {
			return resultList.get(0);
		}
	}

	@Override
	public List<Project> listProjectsByAuth(Application app, User user, String auth) {
		if (!em.contains(user)) {
			user = em.find(User.class, user.getId());
		}

		String query = "FROM Project p WHERE p.deleted = false AND p.active = true AND p.application.id=:appId ";

		if (!has(app, user, auth)) {
			// if the user doesn't have permission at at an application level or
			// system level, then show only the projects where the user has been
			// specifically assigned to
			ComputedAuthorizations computedAuth = getAuthorization(user, app);
			Set<Long> projectIds = computedAuth.getGrantedObjects(Authorization.LEVEL_PROJECT, auth);
			if (projectIds.size() == 0) {
				return Collections.emptyList();
			}
			String allowedProjects = StringUtils.toStringWithSeparator(projectIds, ",");
			query += " and p.id in ( " + allowedProjects + " )";
		}
		query += " order by p.id asc";

		TypedQuery<Project> q = em.createQuery(query, Project.class);
		q.setParameter("appId", app.getId());
		List<Project> projects = q.getResultList();
		return projects;

	}

	@Override
	public List<Form> listFormsByAuth(Long projectId, User user, String auth) {
		if (!em.contains(user)) {
			user = em.find(User.class, user.getId());
		}

		Project project = projectService.findById(projectId);

		String query = "SELECT f FROM Form f "
				+ " WHERE f.deleted = false AND f.active = true AND f.project = :project and f.id=f.root.id ";

		if (!has(project, user, auth)) {
			// if the user doesn't have permission at a project level, app level
			// or system level, then show only the projects where the user has
			// access
			ComputedAuthorizations computedAuth = getAuthorization(user, project.getApplication());
			Set<Long> formIds = computedAuth.getGrantedObjects(Authorization.LEVEL_FORM, auth);
			if (formIds.size() == 0) {
				return Collections.emptyList();
			}
			String allowedForms = StringUtils.toStringWithSeparator(formIds, ",");
			query += " and f.root in ( " + allowedForms + " )";
		}
		query += " order by f.id asc";

		TypedQuery<Form> q = em.createQuery(query, Form.class);
		q.setParameter("project", project);
		List<Form> forms = q.getResultList();
		return forms;

	}

	@Override
	public List<Pool> listPoolsByAuth(Application app, User user, String auth) {
		if (!em.contains(user)) {
			user = em.find(User.class, user.getId());
		}

		String query = "FROM Pool p WHERE p.deleted = false AND p.active = true AND p.application=:app ";

		if (!has(app, user, auth)) {
			// If the user doesn't have authorization at an application level,
			// then we shall check with pools have been specifically granted
			ComputedAuthorizations computedAuth = getAuthorization(user, app);
			Set<Long> poolIds = computedAuth.getGrantedObjects(Authorization.LEVEL_POOL, auth);
			if (poolIds.size() == 0) {
				return Collections.emptyList();
			}
			String allowdPools = StringUtils.toStringWithSeparator(poolIds, ",");
			query += " and p.id in ( " + allowdPools + " )";
		}
		query += " order by p.id asc";

		TypedQuery<Pool> q = em.createQuery(query, Pool.class);
		q.setParameter("app", app);
		List<Pool> pools = q.getResultList();
		return pools;

	}

	private <T extends AuthorizableEntity> PagedData<List<T>> listAuthorizableEntitiesInLevel(Class<T> clazz,
			Application app, int level, long objId, Role roleToExclude, String orderBy, boolean ascending, int page,
			int pageSize) {
		AuthorizationLevelConf confLevel = authLoader.getConfLevel(level);
		boolean exludeRole = roleToExclude != null;
		TypedQuery<T> query = em.createQuery(
				"Select distinct E From " + AuthorizableEntityAuthorization.class.getName() + " A , " + clazz.getName()
						+ " E where A.entityId=E.id and A." + confLevel.getColumn() + " = :objId "
						+ (exludeRole ? " and A.role!=:role " : "") + "order by E." + orderBy + " "
						+ (ascending ? "asc" : "desc"), clazz);
		query.setParameter("objId", objId);
		if (exludeRole) {
			query.setParameter("role", roleToExclude);
		}
		query.setFirstResult((page - 1) * pageSize);
		query.setMaxResults(pageSize);
		List<T> entities = query.getResultList();

		Query queryCount = em.createQuery("Select count(distinct E) From "
				+ AuthorizableEntityAuthorization.class.getName() + " A , " + clazz.getName()
				+ " E where A.entityId=E.id and A." + confLevel.getColumn() + " = :objId "
				+ (exludeRole ? " and A.role!=:role " : ""));
		queryCount.setParameter("objId", objId);
		if (exludeRole) {
			queryCount.setParameter("role", roleToExclude);
		}
		Long count = (Long) queryCount.getSingleResult();

		PagedData<List<T>> entitiesPage = new PagedData<List<T>>(entities, count, page, pageSize, entities.size());
		return entitiesPage;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.READ_WEB)
	public PagedData<List<User>> listUsersInProjectWithoutOwner(Project project, String orderBy, boolean ascending,
			int page, int pageSize) {
		String defaultRole = projectService.getOwnerDefaultRole();
		Role role = roleService.getDefaultRole(defaultRole);
		Application app = project.getApplication();
		return listAuthorizableEntitiesInLevel(User.class, app, Authorization.LEVEL_PROJECT, project.getId(), role,
				UserServiceValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB)
	public PagedData<List<User>> listUsersInFormWithoutOwner(Form form, String orderBy, boolean ascending, int page,
			int pageSize) {
		String defaultRole = formService.getOwnerDefaultRole();
		Role role = roleService.getDefaultRole(defaultRole);
		Application app = form.getProject().getApplication();
		return listAuthorizableEntitiesInLevel(User.class, app, Authorization.LEVEL_FORM, form.getId(), role,
				UserServiceValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.READ)
	public PagedData<List<User>> listUsersInPoolWithoutOwner(Pool pool, String orderBy, boolean ascending, int page,
			int pageSize) {
		String defaultRole = poolService.getOwnerDefaultRole();
		Role role = roleService.getDefaultRole(defaultRole);
		Application app = pool.getApplication();
		return listAuthorizableEntitiesInLevel(User.class, app, Authorization.LEVEL_POOL, pool.getId(), role,
				UserServiceValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	public PagedData<List<Group>> listGroupsInProject(Project project, String orderBy, boolean ascending, int page,
			int pageSize) {
		Application app = project.getApplication();
		return listAuthorizableEntitiesInLevel(Group.class, app, Authorization.LEVEL_PROJECT, project.getId(), null,
				GroupValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	public PagedData<List<Group>> listGroupsInForm(Form form, String orderBy, boolean ascending, int page, int pageSize) {
		Application app = form.getProject().getApplication();
		return listAuthorizableEntitiesInLevel(Group.class, app, Authorization.LEVEL_FORM, form.getId(), null,
				GroupValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	public PagedData<List<Group>> listGroupsInPool(Pool pool, String orderBy, boolean ascending, int page, int pageSize) {
		Application app = pool.getApplication();
		return listAuthorizableEntitiesInLevel(Group.class, app, Authorization.LEVEL_POOL, pool.getId(), null,
				GroupValidator.sanitizeOrderBy(orderBy), ascending, page, pageSize);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.USER_EDIT)
	public void assignApplicationRoleToEntity(Long applicationId, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_APP, applicationId);
		computeEntityAccess(entityId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.USER_EDIT, applicationParam = 0)
	public int deleteRolesFromEntityOnApplication(Long applicationId, List<Long> roles, Long entityId) {
		return removeRolesFromEntity(entityId, roles, Authorization.LEVEL_APP, applicationId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.EDIT)
	public void assignProjectRoleToEntity(Project project, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_PROJECT, project.getId());
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.EDIT, projectParam = 0)
	public void assignProjectRoleToEntity(Long projectId, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_PROJECT, projectId);
		computeEntityAccess(entityId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Project.EDIT, projectParam = 0)
	public int deleteRolesFromEntityOnProject(Long projectId, List<Long> roles, Long entityId) {
		return removeRolesFromEntity(entityId, roles, Authorization.LEVEL_PROJECT, projectId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.EDIT)
	public void assignFormRoleToEntity(Form form, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_FORM, form.getId());
		computeEntityAccess(entityId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.EDIT, formParam = 0)
	public int deleteRolesFromEntityOnForm(Long formId, List<Long> roles, Long entityId) {
		return removeRolesFromEntity(entityId, roles, Authorization.LEVEL_FORM, formId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.EDIT)
	public void assignPoolRoleToEntity(Pool pool, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_POOL, pool.getId());
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.EDIT, poolParam = 0)
	public void assignPoolRoleToEntity(Long poolId, List<Long> roles, Long entityId) {
		assignRolesToEntity(entityId, roles, Authorization.LEVEL_POOL, poolId);
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.EDIT, poolParam = 0)
	public int deleteRolesFromEntityOnPool(Long poolId, List<Long> roles, Long entityId) {
		return removeRolesFromEntity(entityId, roles, Authorization.LEVEL_POOL, poolId);
	}

	@Override
	public void setUpRootAuthorizations(User user, Application application) {
		this.clearUserAccess(user);
		this.computeUserAccess(user, application);
		this.assignRoleToEntity(application, user.getId(), DefaultRoles.ROLE_APP_ADMIN.toString(),
				Authorization.LEVEL_APP, application.getId());
	}
	
	/**
	 * 
	 * @return a list of the projects that belong to a given application
	 */
	private List<Long> listProjectIds(Long appId) {
		TypedQuery<Long> query = em.createQuery("Select A.id From " + Project.class.getName()
				+ " A where A.deleted=false and A.application.id=:appId ", Long.class);
		query.setParameter("appId", appId);
		return query.getResultList();
	}

	/**
	 * A list of all forms that belong to the projects that are within the given
	 * application
	 * 
	 * @param formIds
	 * @return
	 */
	private List<Long> listFormIds(Long appId, Long projectId) {
		// TODO this might be rewritten to use the "criteria" API
		if (appId == null && projectId == null) {
			// This is just some defensive programming to avoid a potential bug
			// if somebody uses wrong the method and list all forms
			throw new IllegalArgumentException(
					"Either constraint over application or project should be used. Both can't be null at the same time");
		}
		String appConstraint = "";
		if (appId != null) {
			appConstraint = "and F.project.application.id=:appId ";
		}
		String projecConstraint = "";
		if (projectId != null) {
			projecConstraint = " and F.project.id=:projectId ";
		}

		TypedQuery<Long> query = em.createQuery("Select F.id from " + Form.class.getName()
				+ " F where F.deleted=false  " + appConstraint + projecConstraint, Long.class);
		if (appId != null) {
			query.setParameter("appId", appId);
		}
		if (projectId != null) {
			query.setParameter("projectId", projectId);
		}

		return query.getResultList();
	}

	/**
	 * A list of all forms that belong to the projects that are within the given
	 * application
	 * 
	 * @param formIds
	 * @return
	 */
	private List<Long> listPoolIds(Long appId) {
		TypedQuery<Long> query = em.createQuery("Select A.id From " + Pool.class.getName()
				+ " A where A.deleted=false and A.application.id=:appId ", Long.class);
		query.setParameter("appId", appId);
		return query.getResultList();
	}

	

	

}
