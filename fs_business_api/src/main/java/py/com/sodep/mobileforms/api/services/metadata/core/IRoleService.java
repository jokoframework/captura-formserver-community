package py.com.sodep.mobileforms.api.services.metadata.core;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * This interface handle the lifecyle of a role:creating a role, adding
 * authorization to it and querying it
 * 
 * @author danicricco
 * 
 */
public interface IRoleService {

	/**
	 * Assign to the role a given authorization
	 * 
	 * @param roleId
	 * @param authorization
	 */
	void addAuths(Long roleId, String authorization);

	/**
	 * Obtain the list of authorizations assigned to a given role
	 * 
	 * @param roleId
	 * @return
	 */
	List<Authorization> getRoleAuths(Long roleId);

	RoleDTO getRole(Long roleId);

	Role createApplicationRole(Application app, User user, RoleDTO dto);

	Role createProjectRole(Application app, User user, RoleDTO dto);

	Role createFormRole(Application app, User user, RoleDTO dto);

	Role createPoolRole(Application app, User user, RoleDTO dto);

	Role createSystemRole(Application app, User user, RoleDTO dto);

	Role editRole(Application app, User user, RoleDTO dto);

	/**
	 * This method receive a set of authorizations and assign them to the role.
	 * If the role has an authorization that is not on the provided list, then
	 * it will be removed
	 * 
	 * @param roleId
	 * @param authsId
	 */
	void setAuthorizations(Long roleId, List<String> authsId);

	Role getOrCreateRole(String roleName, Integer level);

	/**
	 * <p>
	 * This method will search for a role that doesn't belong to any application
	 * and has its name equal to the provided roleName
	 * </p>
	 * .
	 * <p>
	 * This method is useful to obtain the default roles that are added by the
	 * class
	 * </p>
	 * AuthorizationInitializer
	 * 
	 * @param roleName
	 * @return
	 */
	public Role getDefaultRole(String roleName);

	public PagedData<List<Role>> findByName(Application app, String name, int pageNumber, int pageSize);

	public PagedData<List<Role>> findAllOrderByName(Application app, boolean ascending, int pageNumber, int pageSize);

	Role findById(Long roleId);

	PagedData<List<Role>> findAll(Application app, String orderBy, boolean ascending, Integer page, Integer rows);

	PagedData<List<Role>> findByProperty(Application app, String searchField, String searchOper, Long val,
			String orderBy, boolean ascending, Integer page, Integer rows);

	PagedData<List<Role>> findByProperty(Application app, String searchField, String searchOper, String searchString,
			String orderBy, boolean ascending, Integer page, Integer rows);

	Role findById(Application app, Long id);

	Role logicalDelete(Role role);

	/**
	 * Returns the list of roles that are valid at a given level.
	 * 
	 * A role is valid if it can be assigned to an AuthorizableEntity at that
	 * level. E.g. a Role that has Form level is valid at Project level because
	 * it can be assigned to a project, which means that the AuthorizableEntity
	 * will have that role over all forms of the project.
	 * 
	 * @param app
	 * @param level
	 * @param nameLike
	 * @return
	 */
	List<Role> listValidRolesAtLevel(Application app, int level, String nameLike);

	void clearAuthorizationOfNonEditableRoles();

	void clearAssignationOfNonEditableAuthorizations();
}
