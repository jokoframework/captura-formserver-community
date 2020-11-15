package py.com.sodep.mobileforms.api.services.auth;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * <p>
 * This class has methods to check if a user has enough access rights. There are
 * four different levels of authorization: System,Application,Project,Form. If a
 * user has an authorization at level x, then it propagates down to the x+1
 * level
 * </p>
 * An implementation of this interface should be used in Services and
 * Controllers to test the authorization of a User or to get the projects and
 * forms a user has access to
 * 
 * @author Miguel
 * 
 */
public interface IAuthorizationControlService {

	void computeUserAccess(User u);

	ComputedAuthorizations computeUserAccess(User u, Application app);

	/**
	 * Check that the user has been granted the authorization a given level for
	 * a particular object. The method won't check at any other level but only
	 * the provided level
	 * 
	 * @param user
	 * @param auth
	 * @param level
	 * @param objId
	 * @return
	 */
	boolean has(Application app, User user, String auth, int level, Long objId);

	// OK
	/**
	 * Checks if the user has the named authorization (auth) at a System level.
	 * Checks levels: System
	 * 
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean has(User user, String auth);

	// OK
	/**
	 * Checks if the user has the named authorization (auth) at an application
	 * level.
	 * 
	 * The authorizations propagate down. So, if the user has the authorization
	 * at a System level, it means it has that same authorization in all
	 * applications where he/she participates. Checks levels:
	 * Application->System
	 * 
	 * @param app
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean has(Application app, User user, String auth);

	/**
	 * Checks if the user has the named authorization (auth) at a project level.
	 * Checks levels: Projects->Application->System
	 * 
	 * @param project
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean has(Project project, User user, String auth);

	/**
	 * Checks if he user has the named authorization (auth) at a form level.
	 * Check levels: Form-> Project -> Application -> System
	 * 
	 * @param form
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean has(Form form, User user, String auth);

	boolean has(Pool pool, User user, String auth);

	/**
	 * Returs a list of all projects where the user has access. The project list
	 * will be sorted by id asc
	 * 
	 * @param user
	 * @param authorization
	 * @return
	 */
	List<Project> listProjectsByAuth(Application app, User user, String auth);

	/**
	 * Returns all forms within the project on which the user has the given
	 * authorization
	 * 
	 * @param user
	 * @param projectId
	 * @param name
	 * @return
	 */
	List<Form> listFormsByAuth(Long projectId, User user, String auth);

	/**
	 * Returns all pools within the project on which the user has the given
	 * authorization
	 * 
	 * @param app
	 * @param user
	 * @param auth
	 * @return
	 */
	List<Pool> listPoolsByAuth(Application app, User user, String auth);

	/**
	 * This method receives the rolename and then will invoke
	 * {@link #assignRoleToEntity(Long, Long, int, Long)}. The method should
	 * only be used to assign default roles. A Default role is a Role that was
	 * not added by the user, it is part of the system configuration.
	 * 
	 * @param entityId
	 * @param roleName
	 * @param level
	 * @param objId
	 */
	void assignRoleToEntity(Application app, Long entityId, String roleName, int level, Long objId);

	/**
	 * Assign a given role and doesn't make a full recompute of the
	 * authorization, it only adds the authorization to the (assumed) already
	 * computed authorization
	 * 
	 * @param app
	 * @param entityId
	 * @param roleId
	 * @param level
	 * @param objId
	 */
	void assignRoleToEntity(Application app, Long entityId, Long roleId, int level, Long objId);

	/**
	 * Assign an role to an entity and make a full recompute of the
	 * authorization for the given entity
	 * 
	 * @param entityId
	 * @param roleId
	 * @param level
	 * @param objId
	 */
	public void assignRoleToEntity(Long entityId, Long roleId, int level, Long objId);

	void assignApplicationRoleToEntity(Long applicationId, List<Long> roles, Long entityId);

	int deleteRolesFromEntityOnApplication(Long applicationId, List<Long> roles, Long entityId);

	void assignProjectRoleToEntity(Long projectId, List<Long> roles, Long entityId);

	void assignProjectRoleToEntity(Project project, List<Long> roles, Long entityId);

	int deleteRolesFromEntityOnProject(Long projectId, List<Long> roles, Long entityId);

	void assignFormRoleToEntity(Form form, List<Long> roles, Long entityId);

	int deleteRolesFromEntityOnForm(Long formId, List<Long> roles, Long entityId);

	void assignPoolRoleToEntity(Pool pool, List<Long> roles, Long entityId);

	void assignPoolRoleToEntity(Long poolId, List<Long> roles, Long entityId);

	int deleteRolesFromEntityOnPool(Long poolId, List<Long> roles, Long entityId);

	/**
	 * This method is just a wrapper over
	 * {@link #has(Application, User, String)}
	 * 
	 * @param appId
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean hasAppLevelAccess(Long appId, User user, String auth);

	/**
	 * This method is just a wrapper over {@link #has(Project, User, String)}
	 * 
	 * @param projectId
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean hasProjectLevelAccess(Long projectId, User user, String auth);

	/**
	 * This method is just a wrapper over {@link #has(Form, User, String)}
	 * 
	 * @param formId
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean hasFormLevelAccess(Long formId, User user, String auth);

	/**
	 * This method is just a wrapper over {@link #has(Pool, User, String)}
	 * 
	 * @param poolID
	 * @param user
	 * @param auth
	 * @return
	 */
	boolean hasPoolLevelAccess(Long poolID, User user, String auth);

	PagedData<List<User>> listUsersInProjectWithoutOwner(Project project, String orderBy, boolean ascending, int page,
			int pageSize);

	PagedData<List<Group>> listGroupsInProject(Project project, String orderBy, boolean ascending, int page,
			int pageSize);

	PagedData<List<User>> listUsersInFormWithoutOwner(Form form, String orderBy, boolean ascending, int page,
			int pageSize);

	PagedData<List<User>> listUsersInPoolWithoutOwner(Pool pool, String orderBy, boolean ascending, int page,
			int pageSize);

	PagedData<List<Group>> listGroupsInForm(Form form, String orderBy, boolean ascending, int page, int pageSize);

	PagedData<List<Group>> listGroupsInPool(Pool pool, String orderBy, boolean ascending, int page, int pageSize);

	// FIXME do we want to add pagination to these methods?
	/**
	 * Returns the list of roles that the entity has in the application
	 * 
	 * @param app
	 * @param entity
	 * @param nameLike
	 * @return
	 */
	List<Role> listAssignedRoles(Application app, AuthorizableEntity entity, String nameLike);

	/**
	 * Returns the list of roles that the entity has in the project
	 * 
	 * @param app
	 * @param entity
	 * @param nameLike
	 * @return
	 */
	List<Role> listAssignedRoles(Project project, AuthorizableEntity entity, String nameLike);

	List<Role> listAssignedRoles(Project project, Long entityId, String nameLike);

	/**
	 * Returns the list of roles that the entity has in the form
	 * 
	 * @param app
	 * @param entity
	 * @param nameLike
	 * @return
	 */
	List<Role> listAssignedRoles(Form form, AuthorizableEntity entity, String nameLike);

	List<Role> listAssignedRoles(Form form, Long entityId, String nameLike);

	/**
	 * Returns the list of roles that the entity has within the pool
	 * 
	 * @param pool
	 * @param entity
	 * @param nameLike
	 * @return
	 */
	List<Role> listAssignedRoles(Pool pool, AuthorizableEntity entity, String nameLike);

	List<Role> listAssignedRoles(Pool pool, Long entityId, String nameLike);

	/**
	 * Returns the list of roles that could be assigned to an AuthorizableEntity
	 * at application level.
	 * 
	 * A role is said to be assignable if it is valid and the entity doesn't
	 * have that role already at the level (application level in this case)
	 * 
	 * @param application
	 * @param entity
	 * @param nameLike
	 * @return
	 */
	List<Role> listAssignableRoles(Application application, AuthorizableEntity entity, String nameLike);

	/**
	 * 
	 * @param u
	 * @return
	 */
	public ComputedAuthorizationDTO obtainComputedAuth(Application app, User u);

	public void clearUserAccess(User u);
	
	/**
	 * Checks if the application has a feature enabled.
	 */
	boolean hasFeature(Application app, String feature);

	void setUpRootAuthorizations(User user, Application application);

}
