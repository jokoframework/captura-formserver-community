package py.com.sodep.mobileforms.impl.services.metadata.pools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.exceptions.ElementPrototypeInUseException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.impl.services.metadata.AppAwareBaseService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("PoolService")
@Transactional
public class PoolService extends AppAwareBaseService<Pool> implements IPoolService {

	@Autowired
	private IAuthorizationControlService controlService;

	@Autowired
	private ISystemParametersBundle parameterBundle;

	@Autowired
	private IAuthorizationControlService authorizationControlService;

	protected PoolService() {
		super(Pool.class);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// since this method is using the authentication control service to obtain
	// the data, it is not mandatory to have any authorization check
	public List<PoolDTO> listPools(Application app, User user, String language, String auth) {
		List<Pool> pools = controlService.listPoolsByAuth(app, user, auth);
		List<PoolDTO> dtos = new ArrayList<PoolDTO>();
		for (Pool p : pools) {
			dtos.add(getPool(p));
		}
		return dtos;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// since this method is using the authentication control service to obtain
	// the data, it is not mandatory to have any authorization check
	public PagedData<List<PoolDTO>> findAll(User user, Application app, String auth, int page, int pageSize,
			String language) {
		return findByLabel(user, app, auth, null, page, pageSize, language);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// since this method is using the authentication control service to obtain
	// the data, it is not mandatory to have any authorization check
	public PagedData<List<PoolDTO>> findByLabel(User user, Application app, String auth, String label, int page,
			int pageSize, String language) {
		return findByLabelExcludePool(user, app, null, auth, label, page, pageSize, language);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// since this method is using the authentication control service to obtain
	// the data, it is not mandatory to have any authorization check
	public PagedData<List<PoolDTO>> findAllExcludePool(User user, Application app, Pool poolToExclude, String auth,
			int page, int pageSize, String language) {
		return findByLabelExcludePool(user, app, poolToExclude, auth, null, page, pageSize, language);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// since this method is using the authentication control service to obtain
	// the data, it is not mandatory to have any authorization check
	public PagedData<List<PoolDTO>> findByLabelExcludePool(User user, Application app, Pool poolToExclude, String auth,
			String label, int page, int pageSize, String language) {

		List<Pool> authPools = controlService.listPoolsByAuth(app, user, auth);

		List<Long> ids = new ArrayList<Long>();
		for (Pool pool : authPools) {
			if (poolToExclude == null || !pool.equals(poolToExclude)) {
				ids.add(pool.getId());
			}
		}

		if (ids.isEmpty()) {
			return new PagedData<List<PoolDTO>>(Collections.<PoolDTO> emptyList(), 0L, page, pageSize, 0);
		}

		PagedData<List<Pool>> pagedData = find(app, Pool.NAME, BaseService.OPER_CONTAINS, label, Pool.NAME, true, page,
				pageSize, false, ids);

		List<Pool> data = pagedData.getData();

		List<PoolDTO> dtos = new ArrayList<PoolDTO>();
		PagedData<List<PoolDTO>> retData = new PagedData<List<PoolDTO>>(dtos, pagedData.getTotalCount(),
				pagedData.getPageNumber(), pagedData.getPageSize(), pagedData.getAvailable());

		for (Pool pool : data) {
			dtos.add(getPool(pool));
		}
		return retData;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.READ, poolParam = 0)
	public PoolDTO getPool(Long id) {
		Pool pool = this.findById(id);
		return getPool(pool);
	}

	private PoolDTO getPool(Pool pool) {
		PoolDTO dto = new PoolDTO();
		dto.setId(pool.getId());
		dto.setName(pool.getName());
		dto.setDescription(pool.getDescription());
		dto.setActive(pool.getActive());
		return dto;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public Pool findById(Long poolId) {
		return super.findById(poolId);
	}

	@Override
	public String getOwnerDefaultRole() {
		String roleName = parameterBundle.getStrValue(DBParameters.DEFAULT_ROLE_POOL_OWNER);
		if (roleName == null) {
			throw new ApplicationException(
					"The default role for Pool owner is not on the DB. Please check system parameter "
							+ DBParameters.DEFAULT_ROLE_POOL_OWNER);
		}
		return roleName;
	}

	private void validatePoolUniquenessInApp(Application app, PoolDTO poolDTO) {
		String queryStr = "SELECT COUNT(p) FROM " + Pool.class.getSimpleName() + " p "
				+ " WHERE p.deleted = false AND p.application = :app AND p.name = :name ";

		Long id = poolDTO.getId();
		if (id != null) {
			queryStr += " AND p.id != :pid";
		}

		Query query = em.createQuery(queryStr);
		query.setParameter("app", app);
		query.setParameter("name", poolDTO.getName());
		if (id != null) {
			query.setParameter("pid", poolDTO.getId());
		}

		Long singleResult = (Long) query.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("admin.cruds.pool.invalid.duplicated");
			ex.addMessage("pool", "admin.cruds.pool.invalid.duplicated");
			throw ex;
		}

	}

	private Pool saveOrUpdate(Application app, User user, PoolDTO dto) {
		validatePoolUniquenessInApp(app, dto);

		Pool pool = null;
		boolean isNew = dto.getId() == null;
		if (isNew) {
			pool = new Pool();
			pool.setApplication(app);
			pool.setActive(true);
			// the owner should never be changed, only set it the first time
			// when it is being created
			pool.setOwner(user);
		} else {
			pool = findById(dto.getId());
		}

		pool.setName(dto.getName());
		pool.setDescription(dto.getDescription());

		if (isNew) {
			em.persist(pool);
		} else {
			// For an update of the modified time to increase version
			pool.preUpdate();
		}
		em.flush();
		if (isNew) {
			// since this method is being used for save an update we need to
			// take special care to only assign the owner role during creating a
			// new pool
			authorizationControlService.assignRoleToEntity(app,user.getId(), getOwnerDefaultRole(),
					Authorization.LEVEL_POOL, pool.getId());
	
		}
		return pool;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Pool.EDIT, poolParam = 0)
	public Pool edit(Long poolId, User user, PoolDTO dto) {
		Pool pool = findById(poolId);
		return saveOrUpdate(pool.getApplication(), user, dto);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.POOL_CANCREATE)
	public Pool createNew(Application app, User owner, PoolDTO dto) {
		return saveOrUpdate(app, owner, dto);
	}

	@Authorizable(value = AuthorizationNames.Pool.EDIT)
	public Pool logicalDelete(Pool pool) {
		if (canDeletePool(pool)) {
			return super.logicalDelete(pool);
		}
		return null;
	}

	private boolean canDeletePool(Pool pool) {
		// TODO Check the process items that are in this pool
		// If any of those process items is used in an existing Form,
		// the Pool should be deleted
		// WTF is wrong with this query?
//		Query q0 = em.createQuery("SELECT p.prototypes.elements FROM Pool "
//				+ " p JOIN p.prototypes proto JOIN proto.elements pe "
//				+ "WHERE p.id = :poolId AND p.deleted = false AND pe.deleted = false");
		
		String sql = "SELECT element.* " +
				" FROM pools.pools p " +
				" JOIN forms.element_prototypes proto ON p.id = proto.pool_id " + 
				" JOIN forms.element_instances element ON element.prototype_id = proto.id " +
				" WHERE p.id = :poolId and p.deleted = false AND element.deleted = false ";
		Query q0 = em.createNativeQuery(sql, ElementInstance.class);
		q0.setParameter("poolId", pool.getId());
		@SuppressWarnings("unchecked")
		//FIXME should only get the Elements from published WF?
		List<ElementInstance> elements = q0.getResultList();
		if (elements != null && !elements.isEmpty()) {
			ElementPrototypeInUseException e = new ElementPrototypeInUseException();
			
			Map<Form, List<ElementPrototype>> map = new HashMap<Form, List<ElementPrototype>>();
			
			//This fuc... query also doesn't work
//			TypedQuery<Form> q1 = em.createQuery("SELECT f FROM Form f JOIN f.pages p JOIN p.elements e" +
//					" WHERE e=:element ", Form.class);
			
			sql = "SELECT f.* FROM forms.forms f JOIN forms.pages p ON f.id = p.form_id JOIN forms.element_instances e ON p.id = e.page_id" +
					" WHERE e.id = :elementId";
			Query q1 = em.createNativeQuery(sql, Form.class);
			for (ElementInstance element : elements) {
				q1.setParameter("elementId", element.getId());
				Form form = (Form) q1.getSingleResult();
				List<ElementPrototype> list = map.get(form);
				if(list == null){
					list = new ArrayList<ElementPrototype>();
					map.put(form, list);
				}
				if (!list.contains(element.getPrototype())) {
					list.add(element.getPrototype());
				}
			}
			e.setMapFormPrototypes(map);
			
			throw e;
		}
		return true;
	}

}
