package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.dtos.ProcessItemDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.exceptions.ElementPrototypeInUseException;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.forms.model.ElementPrototypeUtils;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.JsonResponseObjectCreated;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This controller implements the server method that are being invoked by the JS
 * module new-pool-crud-amd.js
 * 
 * @author rodrigovz
 * 
 */

@Controller
public class PoolCrudController extends SodepController {

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IFormService formService;

	@RequestMapping("/cruds/pools/processItem/paging/read.ajax")
	protected @ResponseBody
	PagedData<List<ProcessItemDTO>> readProcessItem(HttpServletRequest request,
			@RequestParam(value = "poolId") Long poolId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order) {

		Pool pool = poolService.findById(poolId);

		boolean asc = (order != null) ? order.equalsIgnoreCase("asc") : true;

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = mgr.getUser();

		PagedData<List<ElementPrototype>> serviceData = elementPrototypeService.findAll(i18n.getSelectedLanguage(),
				currentUser, pool, AuthorizationNames.Pool.READ, page, rows, orderBy, asc);
		PagedData<List<ProcessItemDTO>> controllerData = new PagedData<List<ProcessItemDTO>>();

		List<ProcessItemDTO> data = new ArrayList<ProcessItemDTO>();

		for (ElementPrototype e : serviceData.getData()) {
			ProcessItemDTO p = new ProcessItemDTO();
			p.setId(e.getRoot().getId());
			p.setLabel(elementPrototypeService.getLabel(e.getId(), i18n.getSelectedLanguage()));
			p.setType(i18n.getMessage("admin.form.processitem.type." + ElementPrototypeUtils.getName(e)));
			data.add(p);
		}

		BeanUtils.mapBean(serviceData, controllerData);
		controllerData.setData(data);

		return controllerData;
	}

	private PoolDTO getPoolDTO(String id, String name, String description, Boolean active, Application app) {
		PoolDTO poolDTO = new PoolDTO();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			poolDTO.setId(Long.parseLong(id));
		}
		poolDTO.setName(name);
		poolDTO.setDescription(description);
		poolDTO.setActive(active);
		poolDTO.setApplicationId(app.getId());
		return poolDTO;
	}

	@RequestMapping(value = "/cruds/pools/get.ajax", method = RequestMethod.POST)
	@ResponseBody
	PoolDTO getPool(HttpServletRequest request, @RequestParam(value = "poolId") Long poolId) {
		return this.poolService.getPool(poolId);
	}

	@RequestMapping(value = "/cruds/pools/save.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponseObjectCreated<String> savePool(HttpServletRequest request, @RequestBody PoolSaveRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = getUser(request);
		JsonResponseObjectCreated<String> response = new JsonResponseObjectCreated<String>();
		PoolDTO poolDTO = this.getPoolDTO(r.poolId, r.name, r.description, true, mgr.getApplication());
		Pool pool = null;
		try {
			if (poolDTO.getId() != null) {
				pool = this.poolService.edit(poolDTO.getId(), currentUser, poolDTO);
			} else {
				pool = this.poolService.createNew(mgr.getApplication(), currentUser, poolDTO);
				response.setObj(pool.getId().toString());
				response.setComputedAuthorizations(authControlService.obtainComputedAuth(mgr.getApplication(),
						currentUser));
			}
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.pool.saved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.pool.saved.message", pool.getName()));
		} catch (OptimisticLockException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.pool.notsaved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.pool.notsaved.message", poolDTO.getLabel()));
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	@RequestMapping(value = "/cruds/pools/importProcessItem.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> importProcessItem(HttpServletRequest request, @RequestParam(value = "poolId") Long poolId,
			@RequestParam(value = "elementId") Long elementId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = getUser(request);
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();
		String label = elementPrototypeService.getLabelLastestVersion(elementId, i18n.getSelectedLanguage());
		try {
			elementPrototypeService.importElementPrototypeAndSave(app, elementId, poolId, currentUser,
					i18n.getSelectedLanguage());
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.pool.processItem.imported.title"));
			response.setMessage(i18n.getMessage("admin.cruds.pool.processItem.imported.message", label));
		} catch (OptimisticLockException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.processItem.notsaved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.processItem.notsaved.message", label));
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	@RequestMapping(value = "/cruds/pools/saveEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> saveEntityRolesInPool(HttpServletRequest request, @RequestBody EntityRolesInPoolRequest p) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		authorizationControlService.assignPoolRoleToEntity(p.poolId, p.rolesId, p.entityId);
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.pool.roles.saved.title"));
		response.setMessage("");
		return response;
	}

	@RequestMapping(value = "/cruds/pools/removeEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeEntityRolesInPool(HttpServletRequest request,
			@RequestParam(value = "poolId", required = true) Long poolId,
			@RequestParam(value = "entityId", required = true) Long entityId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Pool pool = this.poolService.findById(poolId);
		List<Role> assignedRoles = authorizationControlService.listAssignedRoles(pool, entityId, null);
		List<Long> rolesId = new ArrayList<Long>();
		for (Role role : assignedRoles) {
			rolesId.add(role.getId());
		}
		this.authorizationControlService.deleteRolesFromEntityOnPool(poolId, rolesId, entityId);

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.pool.roles.deleted.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping("/cruds/pools/userAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<UserDTO>> userAuth(HttpServletRequest request, @RequestParam(value = "poolId") Long poolId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String sort) {

		Pool pool = poolService.findById(poolId);

		boolean ascending = sort != null ? sort.equalsIgnoreCase("asc") : true;

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = User.MAIL;
		}

		PagedData<List<User>> data = authorizationControlService.listUsersInPoolWithoutOwner(pool, orderBy, ascending,
				pageNumber, pageSize);
		List<User> users = data.getData();
		List<UserDTO> dtos = userDTOWithRoles(pool, users);
		return new PagedData<List<UserDTO>>(dtos, data.getTotalCount(), data.getPageNumber(), data.getPageSize(),
				data.getAvailable());

	}

	private List<UserDTO> userDTOWithRoles(Pool project, List<User> users) {
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User u : users) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(project, u, null);
			UserDTO dto = UserViewUtils.translate(u);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	@RequestMapping("/cruds/pools/groupAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<GroupDTO>> groupAuth(HttpServletRequest request, @RequestParam(value = "poolId") Long poolId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String sort) {

		Pool pool = poolService.findById(poolId);

		boolean ascending = sort != null ? sort.equalsIgnoreCase("asc") : true;

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = Group.NAME;
		}

		PagedData<List<Group>> data = authorizationControlService.listGroupsInPool(pool, orderBy, ascending,
				pageNumber, pageSize);
		List<Group> users = data.getData();
		List<GroupDTO> dtos = groupDTOWithRoles(pool, users);
		return new PagedData<List<GroupDTO>>(dtos, data.getTotalCount(), data.getPageNumber(), data.getPageSize(),
				data.getAvailable());
	}

	private List<GroupDTO> groupDTOWithRoles(Pool pool, List<Group> groups) {
		List<GroupDTO> dtos = new ArrayList<GroupDTO>();
		for (Group g : groups) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(pool, g, null);
			GroupDTO dto = GroupViewUtils.translate(g);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	public static class EntityRolesInPoolRequest {
		private Long poolId;
		private Long entityId;
		private List<Long> rolesId;

		public Long getPoolId() {
			return poolId;
		}

		public void setPoolId(Long poolId) {
			this.poolId = poolId;
		}

		public Long getEntityId() {
			return entityId;
		}

		public void setEntityId(Long entityId) {
			this.entityId = entityId;
		}

		@JsonDeserialize(as = List.class, contentAs = Long.class)
		public List<Long> getRolesId() {
			return rolesId;
		}

		public void setRolesId(List<Long> rolesId) {
			this.rolesId = rolesId;
		}
	}

	public static class PoolSaveRequest {
		private String poolId; // FIXME why is poolId a String?
		private String name;
		private String description;

		public String getPoolId() {
			return poolId;
		}

		public void setPoolId(String poolId) {
			this.poolId = poolId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	@RequestMapping(value = "/pools/list.ajax", method = RequestMethod.POST)
	public @ResponseBody
	List<PoolDTO> listPools(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		return listPools(app, user, i18n.getSelectedLanguage());
	}

	public List<PoolDTO> listPools(Application app, User user, String language) {

		List<Pool> pools = authorizationControlService.listPoolsByAuth(app, user, AuthorizationNames.Pool.READ);

		List<PoolDTO> dtos = new ArrayList<PoolDTO>();
		for (Pool p : pools) {
			PoolDTO dto = new PoolDTO();
			dto.setId(p.getId());
			dto.setDescription(p.getDescription());
			dto.setLabel(p.getName());
			dto.setActive(p.getActive());
			dtos.add(dto);
		}
		return dtos;
	}

	@RequestMapping("/cruds/pools/delete.ajax")
	public @ResponseBody
	JsonResponse<String> deletePool(HttpServletRequest request, @RequestParam(value = "id") Long poolId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		Pool pool = poolService.findById(poolId);

		try {
			poolService.logicalDelete(pool);
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.pool.deleted.title"));
			response.setUnescapedMessage(i18n.getMessage("admin.cruds.pool.deleted.message", pool.getName()));
		} catch (ElementPrototypeInUseException e) {
			// A WF is using an ElementPrototype that's in this pool, so it
			// can't be deleted
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			Map<Form, List<ElementPrototype>> map = e.getMapFormPrototypes();
			String language = i18n.getSelectedLanguage();
			Set<Form> keySet = map.keySet();
			StringBuilder sb = new StringBuilder();
			for (Form f : keySet) {
				FormDTO formDTO = formService.getFormDTO(f, language);

				sb.append(i18n.getMessage("admin.cruds.pool.delete.form.depends.on", formDTO.getLabel()));
				List<ElementPrototype> list = map.get(f);
				for (int i = 0; i < list.size() - 1; i++) {
					ElementPrototype proto = list.get(i);
					String protoLabel = elementPrototypeService.getLabel(proto.getId(), language);
					sb.append(protoLabel);
					sb.append(", ");
				}
				ElementPrototype proto = list.get(list.size() - 1);
				String protoLabel = elementPrototypeService.getLabel(proto.getId(), language);
				sb.append(protoLabel);
				sb.append("<br />");
			}
			response.setUnescapedMessage(sb.toString());
		} catch (Exception e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setUnescapedMessage(i18n.getMessage("admin.cruds.pool.deleted.error", pool.getName()));
		}
		return response;
	}
}
