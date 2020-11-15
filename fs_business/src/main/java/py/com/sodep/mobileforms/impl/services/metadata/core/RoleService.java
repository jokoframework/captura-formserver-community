package py.com.sodep.mobileforms.impl.services.metadata.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.AuthorizationDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.impl.services.metadata.AppAwareBaseService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

//@Service("RoleService") This class doesn't have @Service on purpose. It is being declared in the "business-applicationContext.xml" in order to have different implementation, one for testing and this one for production.
@Transactional
public class RoleService extends AppAwareBaseService<Role> implements IRoleService {

	@Autowired
	private IAuthorizationService authService;

	protected RoleService() {
		super(Role.class);
	}

	@Override
	public List<Authorization> getRoleAuths(Long roleId) {
		Role role = this.findById(roleId);
		if (role != null) {
			return new ArrayList<Authorization>(role.getGrants());
		}
		return Collections.emptyList();
	}

	@Override
	public void addAuths(Long roleId, String authName) {
		Role role = this.findById(roleId);
		Authorization authorization = authService.get(authName);
		// the method authService.get might return a detached object, because it
		// is using an internal cache
		if (!em.contains(authorization)) {
			authorization = em.find(Authorization.class, authorization.getName());
		}
		if (authorization == null) {
			throw new IllegalArgumentException("unknown authorization " + authName);
		}
		if (role.getAuthLevel() > authorization.getLevel()) {
			// it is only feasible to assign to a role an authorization level
			// that is from a lower level. For example, an authorization of form
			// to application, but the opposite is not valid
			throw new AuthorizationException("Can't assign authorization " + authName + " (level #"
					+ authorization.getLevel() + ") to Rol #" + roleId + " (level #" + role.getAuthLevel() + ")");
		}
		List<Authorization> grants = role.getGrants();
		if (grants == null) {
			grants = new ArrayList<Authorization>();
			role.setGrants(grants);
		}
		if (!grants.contains(authorization)) {
			grants.add(authorization);
		}
	}

	@Override
	public RoleDTO getRole(Long roleId) {
		Role role = this.findById(roleId);
		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(role.getId());
		roleDTO.setName(role.getName());
		roleDTO.setDescription(role.getDescription());
		roleDTO.setActive(role.getActive());
		for (Authorization a : role.getGrants()) {
			AuthorizationDTO authDTO = AuthorizationHelper.translateAuthorizationToDTO(a);
			roleDTO.addGrant(authDTO);
		}
		return roleDTO;
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_CREATE)
	public Role createApplicationRole(Application app, User user, RoleDTO dto) {
		return createNewRole(app, dto, Authorization.LEVEL_APP);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_CREATE)
	public Role createProjectRole(Application app, User user, RoleDTO dto) {
		return createNewRole(app, dto, Authorization.LEVEL_PROJECT);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_CREATE)
	public Role createFormRole(Application app, User user, RoleDTO dto) {
		return createNewRole(app, dto, Authorization.LEVEL_FORM);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_CREATE)
	public Role createPoolRole(Application app, User user, RoleDTO dto) {
		return createNewRole(app, dto, Authorization.LEVEL_POOL);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_CREATE)
	public Role createSystemRole(Application app, User user, RoleDTO dto) {
		return createNewRole(null, dto, Authorization.LEVEL_SYSTEM);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_EDIT)
	public Role editRole(Application app, User user, RoleDTO dto) {
		return editRole(app, dto);
	}

	protected Role createNewRole(Application app, RoleDTO dto, int level) {
		validateRoleExistence(app, dto);
		Role role = new Role();
		role.setApplication(app);
		role.setActive(true);
		role.setEditable(true);
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		role.setAuthLevel(level);
		em.persist(role);
		return role;
	}

	private Role editRole(Application app, RoleDTO dto) {
		validateRoleExistence(app, dto);
		Role role = findById(dto.getId());
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		return role;
	}

	private void validateRoleExistence(Application app, RoleDTO dto) {
		String queryStr = "SELECT COUNT(r) FROM " + Role.class.getSimpleName() + " r "
				+ " WHERE r.deleted = false AND r.name = :name ";
		if (app != null) {
			queryStr += " AND r.application = :app ";
		} else { // it's a system role
			queryStr += " AND r.application is null ";
		}

		if (dto.getId() != null) {
			queryStr += " and r.id != :rId";
		}

		Query query = em.createQuery(queryStr);
		query.setParameter("name", dto.getName());
		if (app != null) {
			query.setParameter("app", app);
		}
		if (dto.getId() != null) {
			query.setParameter("rId", dto.getId());
		}

		Long singleResult = (Long) query.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("admin.cruds.role.invalid.duplicated");
			ex.addMessage("form", "admin.cruds.role.invalid.duplicated");
			throw ex;
		}
	}

	public PagedData<List<Role>> findAll(Application app, String orderBy, boolean ascending, int pageNumber,
			int pageSize) {
		return super.findAll(app, orderBy, ascending, pageNumber, pageSize);
	}

	@Override
	public void setAuthorizations(Long roleId, List<String> authsId) {
		// delete all authorizations
		Query deleteQuery = em.createNativeQuery("Delete from core.role_grants_authorization where role_id=:roleId");
		deleteQuery.setParameter("roleId", roleId);
		deleteQuery.executeUpdate();
		for (String auth : authsId) {
			addAuths(roleId, auth);
		}

	}

	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	@Override
	public Role getDefaultRole(String roleName) {
		Query query = em.createQuery("From " + Role.class.getName()
				+ " A where A.name=:roleName and A.application is null");
		query.setParameter("roleName", roleName);
		@SuppressWarnings("unchecked")
		List<Role> list = query.getResultList();
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public Role getOrCreateRole(String roleName, Integer authLevel) {
		Query query = em.createQuery("From " + Role.class.getName()
				+ " A where A.name=:roleName and A.authLevel=:authLevel");
		query.setParameter("roleName", roleName);
		query.setParameter("authLevel", authLevel);
		@SuppressWarnings("unchecked")
		List<Role> list = query.getResultList();
		if (list.size() <= 0) {
			Role r = new Role();
			r.setName(roleName);
			r.setAuthLevel(authLevel);
			r = save(r);
			return r;
		} else {
			return list.get(0);
		}
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public PagedData<List<Role>> findByName(Application app, String name, int pageNumber, int pageSize) {
		PagedData<List<Role>> r = findByProperty(app, Role.NAME, BaseService.OPER_CONTAINS, name, Role.NAME, true,
				pageNumber, pageSize);
		return r;
	}

	@Override
	public PagedData<List<Role>> findAllOrderByName(Application app, boolean ascending, int pageNumber, int pageSize) {
		PagedData<List<Role>> r = findAll(app, Role.NAME, ascending, pageNumber, pageSize);
		return r;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// TODO (DANICRICCO) It might be better to deprecate this method in favor of
	// findByID(Application,roleId)
	public Role findById(Long roleId) {
		return super.findById(roleId);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public PagedData<List<Role>> findAll(Application app, String orderBy, boolean ascending, Integer pageNumber,
			Integer pageSize) {
		return super.findAll(app, orderBy, ascending, pageNumber, pageSize);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public PagedData<List<Role>> findByProperty(Application app, String searchField, String searchOper, Long val,
			String orderBy, boolean ascending, Integer pageNumber, Integer pageSize) {
		return super.find(app, searchField, searchOper, val, orderBy, ascending, pageNumber, pageSize, false, null);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public PagedData<List<Role>> findByProperty(Application app, String searchField, String searchOper,
			String searchString, String orderBy, boolean ascending, Integer pageNumber, Integer pageSize) {
		return super.find(app, searchField, searchOper, searchString, orderBy, ascending, pageNumber, pageSize, false,
				null);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public Role findById(Application app, Long id) {
		return super.findById(app, id);
	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_LIST)
	public List<Role> listValidRolesAtLevel(Application app, int level, String nameLike) {
		boolean nameFilter = nameLike != null && !(nameLike = nameLike.trim()).isEmpty();
		String qStr = "SELECT r FROM " + Role.class.getName()
				+ " r WHERE r.deleted=false AND r.application = :app AND r.authLevel in ( :level ) ";
		if (nameFilter) {
			qStr += " AND lower(r.name) LIKE :nameLike";
		}
		TypedQuery<Role> query = em.createQuery(qStr, Role.class);
		query.setParameter("app", app);

		ArrayList<Integer> l=new ArrayList<Integer>();
		if (level == 1) {
			// application level should shown all available roles
			l.add(1);
			l.add(2);
			l.add(3);
			l.add(4);
		} else if (level == 2) {
			// project level should show project and form roles
			l.add(2);
			l.add(3);
		} else {
			//Form, pool and system should only show roles assigned to those levels
			l.add(level);
		}
		
		query.setParameter("level", l);
		if (nameFilter) {
			query.setParameter("nameLike", "%" + nameLike.toLowerCase() + "%");
		}
		return query.getResultList();

	}

	@Override
	@Authorizable(AuthorizationNames.App.ROLES_EDIT)
	public Role logicalDelete(Role role) {
		return super.logicalDelete(role);
	}

	@Override
	public void clearAuthorizationOfNonEditableRoles() {
		// This authorization will be added again using the default
		// configuration on the CSV. We are going to delete them just to be sure
		// that the last information on the CSV is what the default roles are
		// granted
		Query q = em
				.createNativeQuery("delete from core.role_grants_authorization where role_id in (select id from core.roles where editable=false)");
		q.executeUpdate();
	}

	@Override
	public void clearAssignationOfNonEditableAuthorizations() {
		Query q = em
				.createNativeQuery("delete from core.role_grants_authorization where authorization_name  in (select name from core.authorizations where visible=false)");
		q.executeUpdate();
	}
}
