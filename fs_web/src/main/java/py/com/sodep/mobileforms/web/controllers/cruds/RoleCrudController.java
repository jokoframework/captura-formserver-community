package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.dtos.AuthorizationGroupDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.jqgrid.ColModel;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
/**
 * This controller implements the functionality of the pages "Room Administration" (role-crud-amd.js) and assigning authorization to a role (role-permission-amd.js)
 * @author danicricco
 *
 */
public class RoleCrudController extends CrudController {

	private static final String AUTHS = "auths";

	private static final String APPLICATION = "Application";
	private static final String PROJECT = "Project";
	private static final String FORM = "Form";
	private static final String POOL = "Pool";

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IAuthorizationService authorizationService;

	@Override
	@RequestMapping("/cruds/roles/columninfo.ajax")
	protected @ResponseBody
	JsonResponse<?> columnInfo(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Map<String, Object> content = new HashMap<String, Object>();
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(true);
		response.setContent(content);

		String[] cols = { Role.ID, Role.NAME, Role.DESCRIPTION, Role.LEVEL, AUTHS };
		String[] colNames = { i18n.getMessage("admin.cruds.role.cols.id"),
				i18n.getMessage("admin.cruds.role.cols.name"), i18n.getMessage("admin.cruds.role.cols.description"),
				i18n.getMessage("admin.cruds.role.cols.level"), i18n.getMessage("admin.cruds.role.cols.auths") };

		content.put("cols", cols);
		content.put("colNames", colNames);

		List<ColModel> colModel = new ArrayList<ColModel>();
		colModel.add(new ColModel().name(cols[0]).index(cols[0]).hidden(true));
		colModel.add(new ColModel().name(cols[1]).index(cols[1]).editable(true).required(true).width("150"));
		colModel.add(new ColModel().name(cols[2]).index(cols[2]).editable(true).width("180"));

		ColModel levelCol = new ColModel().name(cols[3]).index(cols[3]).edittype("select").width("100");
		String levels = allLevels(i18n);
		levelCol.addEditoption("value", levels);
		colModel.add(levelCol);

		colModel.add(new ColModel().name(cols[4]).index(cols[4]).editable(false).search(false).sortable(false)
				.width("70").align("center").formatter("custom"));

		content.put("colModel", colModel);

		content.put("sortorder", "asc");
		content.put("sortname", "name");
		content.put("addCaption", i18n.getMessage("admin.cruds.role.form.addCaption"));
		content.put("editCaption", i18n.getMessage("admin.cruds.role.form.editCaption"));
		return response;
	}

	private String allLevels(I18nManager i18n) {
		StringBuilder sb = new StringBuilder();

		sb.append(APPLICATION);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.role.level.application"));
		sb.append(';');

		sb.append(PROJECT);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.role.level.project"));
//		sb.append(';');

//		sb.append(FORM);
//		sb.append(':');
//		sb.append(i18n.getMessage("admin.cruds.role.level.form"));
//		sb.append(';');

//		sb.append(POOL);
//		sb.append(':');
//		sb.append(i18n.getMessage("admin.cruds.role.level.pool"));

		return sb.toString();
	}

	@Override
	@RequestMapping("/cruds/roles/paging/read.ajax")
	public @ResponseBody
	PagedData<List<Map<String, Object>>> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false, defaultValue = "") String _search,
			@RequestParam(value = "filters", required = false) String filters,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString) {

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = Authorization.NAME;
		}

		boolean search = _search.equals("true");
		Application app = mgr.getApplication();
		PagedData<List<Role>> serviceData = null;
		if (!search) {
			serviceData = roleService.findAll(app, orderBy, ascending, page, rows);
		} else {
			if (searchField.equals("id")) {
				Long val = Long.parseLong(searchString);
				serviceData = roleService.findByProperty(app, searchField, searchOper, val, orderBy, ascending, page,
						rows);
			} else {
				serviceData = roleService.findByProperty(app, searchField, searchOper, searchString, orderBy,
						ascending, page, rows);
			}
		}
		PagedData<List<Map<String, Object>>> controllerData = new PagedData<List<Map<String, Object>>>();

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

		for (Role r : serviceData.getData()) {
			Map<String, Object> entry = new HashMap<String, Object>();
			entry.put(Role.ID, r.getId());
			entry.put(Role.NAME, r.getName());
			entry.put(Role.DESCRIPTION, r.getDescription());
			if (r.getAuthLevel() == Authorization.LEVEL_APP) {
				entry.put(Role.LEVEL, i18n.getMessage("admin.cruds.role.level.application"));
			} else if (r.getAuthLevel() == Authorization.LEVEL_PROJECT) {
				entry.put(Role.LEVEL, i18n.getMessage("admin.cruds.role.level.project"));
			} else if (r.getAuthLevel() == Authorization.LEVEL_FORM) {
				entry.put(Role.LEVEL, i18n.getMessage("admin.cruds.role.level.form"));
			} else if (r.getAuthLevel() == Authorization.LEVEL_POOL) {
				entry.put(Role.LEVEL, i18n.getMessage("admin.cruds.role.level.pool"));
			}
			data.add(entry);
		}

		BeanUtils.mapBean(serviceData, controllerData);
		controllerData.setData(data);

		return controllerData;
	}

	@Override
	@RequestMapping("/cruds/roles/paging/edit.ajax")
	public @ResponseBody
	JsonResponse<String> edit(HttpServletRequest request, @RequestParam(value = "oper") String oper,
			@RequestParam(value = "id", required = false) String id) {
		Map<String, String> params = getParametersMap(request);
		Boolean active = Boolean.parseBoolean(params.get(Role.ACTIVE));
		return edit(request, oper, id, params.get(Role.NAME), params.get(Role.DESCRIPTION), params.get(Role.LEVEL),
				active);
	}

	private JsonResponse<String> edit(HttpServletRequest request, String oper, String id, String name,
			String description, String level, Boolean active) {

		User currentUser = getUser(request);
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();

		JsonResponse<String> response = new JsonResponse<String>();
		RoleDTO dto = getRoleDTO(id, name, description, level, active);

		try {
			if (!oper.equals("del")) {
				// TODO Validation...
				if (oper.equals("add")) {
					if (dto.getLevel().equals(APPLICATION)) {
						roleService.createApplicationRole(app, currentUser, dto);
					} else if (dto.getLevel().equals(PROJECT)) {
						roleService.createProjectRole(app, currentUser, dto);
					} else if (dto.getLevel().equals(FORM)) {
						roleService.createFormRole(app, currentUser, dto);
					} else if (dto.getLevel().equals(POOL)) {
						roleService.createPoolRole(app, currentUser, dto);
					}
				} else if (oper.equals("edit")) {
					roleService.editRole(app, currentUser, dto);
				}
				response.setSuccess(true);
				response.setTitle(i18n.getMessage("admin.cruds.role.saved.title"));
				response.setMessage(i18n.getMessage("admin.cruds.role.saved.message", dto.getName()));
			} else {
				Role role = roleService.findById(app, dto.getId());
				roleService.logicalDelete(role);
				response.setTitle(i18n.getMessage("admin.cruds.role.deleted.title"));
				response.setMessage(i18n.getMessage("admin.cruds.role.deleted.message", role.getName()));
				response.setSuccess(true);
			}
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}

		return response;
	}

	private RoleDTO getRoleDTO(String id, String name, String description, String level, Boolean active) {
		RoleDTO dto = new RoleDTO();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			dto.setId(Long.parseLong(id));
		}
		dto.setName(name);
		dto.setDescription(description);
		dto.setLevel(level);
		dto.setActive(active);
		return dto;
	}

	@RequestMapping(value = "/crud/role/get.ajax", method = RequestMethod.POST)
	public @ResponseBody
	RoleDTO getRole(HttpServletRequest request, @RequestParam(value = "roleId") Long roleId) {
		return roleService.getRole(roleId);
	}

	@RequestMapping(value = "/authorizationGroup/list.ajax", method = RequestMethod.POST)
	public @ResponseBody
	List<AuthorizationGroupDTO> getAuthorizationGroups(HttpServletRequest request,
			@RequestParam(value = "roleId") Long roleId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		return this.authorizationService.getAuthorizationGroups(roleId, i18n.getSelectedLanguage());
	}

	@RequestMapping(value = "/cruds/roles/saveAuths.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> mergeRoleAuths(HttpServletRequest request, @RequestBody RoleAuthsRequest r) {

		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		Role role = this.roleService.findById(new Long(r.getRoleId()));
		roleService.setAuthorizations(new Long(r.getRoleId()), r.getAuthsId());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.role.permission.saved.title"));
		response.setMessage(i18n.getMessage("admin.cruds.role.permission.saved.message", role.getName()));
		return response;
	}

	/**
	 * This inner class is used to parse the JSON send by
	 * "role-permission-amd.js" to assign authorizations to a given role
	 * 
	 * @author danicricco
	 * 
	 */
	public static class RoleAuthsRequest {
		private Integer roleId;
		private List<String> authsId;

		public Integer getRoleId() {
			return roleId;
		}

		public void setRoleId(Integer roleId) {
			this.roleId = roleId;
		}

		public List<String> getAuthsId() {
			return authsId;
		}

		public void setAuthsId(List<String> authsId) {
			this.authsId = authsId;
		}
	}

}
