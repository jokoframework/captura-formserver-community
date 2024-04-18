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

import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.jqgrid.ColModel;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Controller
public class GroupCrudController extends SodepController {

	@Autowired
	private IGroupService groupService;

	@RequestMapping("/cruds/groups/columninfo.ajax")
	public @ResponseBody
	JsonResponse<?> columnInfo(HttpServletRequest request) {

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Map<String, Object> content = new HashMap<String, Object>();
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(true);
		response.setContent(content);
		String[] cols = { Group.ID, Group.NAME, Group.DESCRIPTION, Group.ACTIVE, "actions" };
		String[] colNames = { i18n.getMessage("admin.cruds.group.cols.id"),
				i18n.getMessage("admin.cruds.group.cols.name"), i18n.getMessage("admin.cruds.group.cols.description"),
				i18n.getMessage("admin.cruds.group.cols.active"), "" };
		content.put("cols", cols);
		content.put("colNames", colNames);

		List<ColModel> colModel = new ArrayList<ColModel>();
		colModel.add(new ColModel().name(cols[0]).index(cols[0]).hidden(true));
		colModel.add(new ColModel().name(cols[1]).index(cols[1]).width("200px"));
		colModel.add(new ColModel().name(cols[2]).index(cols[2]).width("200px"));
		ColModel active = new ColModel().name(cols[3]).index(cols[3]).edittype("checkbox").width("30px").hidden(true);
		active.addEditoption("value", "true:false");
		colModel.add(active);
		colModel.add(new ColModel().name(cols[4]).index(cols[4]).editable(false).search(false).sortable(false).align("center")
				.formatter(ColModel.CUSTOM_FORMATTER).width("70px"));

		content.put("colModel", colModel);

		content.put("sortorder", "asc");
		content.put("sortname", "name");
		return response;
	}

	@RequestMapping("/cruds/groups/paging/read.ajax")
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
		// User currentUser = getUser(request);
		Application app = mgr.getApplication();
		
		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = Group.NAME;
		}

		boolean search = _search.equals("true");

		PagedData<List<Group>> serviceData = null;
		if (!search) {
			serviceData = groupService.findAll(app, orderBy, ascending, page, rows);
		} else {
			if (searchField.equals("id")) {
				Long val = Long.parseLong(searchString);
				serviceData = groupService.findByProperty(app, searchField, searchOper, val, orderBy, ascending, page,
						rows);
			} else {
				serviceData = groupService.findByProperty(app, searchField, searchOper, searchString, orderBy,
						ascending, page, rows);
			}
		}
		PagedData<List<Map<String, Object>>> controllerData = new PagedData<List<Map<String, Object>>>();

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

		for (Group g : serviceData.getData()) {
			Map<String, Object> entry = new HashMap<String, Object>();
			entry.put(Group.ID, g.getId());
			entry.put(Group.NAME, g.getName());
			entry.put(Group.DESCRIPTION, g.getDescription());
			entry.put(Group.ACTIVE, g.getActive());
			data.add(entry);
		}

		BeanUtils.mapBean(serviceData, controllerData);
		controllerData.setData(data);

		return controllerData;
	}

	private Group getGroup(String id, String name, String description, Boolean active, Application app) {
		Group group = new Group();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			group.setId(Long.parseLong(id));
		}
		group.setName(name);
		group.setDescription(description);
		group.setApplication(app);
		group.setActive(active);
		return group;
	}

	@RequestMapping(value = "/cruds/groups/get.ajax", method = RequestMethod.POST)
	@ResponseBody
	GroupDTO getGroup(HttpServletRequest request, @RequestParam(value = "groupId") Integer groupId) {
		return this.groupService.getGroup(new Long(groupId));
	}

	@RequestMapping(value = "/cruds/groups/save.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> saveGroup(HttpServletRequest request, @RequestBody GroupSaveRequest g) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = getUser(request);
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();
		try {
			Group group = this.getGroup(g.groupId, g.name, g.description, true, app);
			group = this.groupService.save(currentUser, group);
			response.setObj(group.getId().toString());
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.group.saved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.group.saved.message", group.getName()));
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	@RequestMapping(value = "/cruds/groups/delete.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> deleteGroup(HttpServletRequest request, @RequestParam(value = "id") Long groupId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<String> response = new JsonResponse<String>();
		Group g = groupService.findById(groupId);
		groupService.logicalDelete(g);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.group.deleted.title"));
		response.setMessage(i18n.getMessage("admin.cruds.group.deleted.message", g.getName()));
		return response;
	}

	@RequestMapping(value = "/cruds/groups/addUsers.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> addUsers(HttpServletRequest request, @RequestBody UsersGroupRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> usersId = r.usersId;
		Group group = groupService.addUsers(r.groupId, usersId);
		response.setObj(group.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.group.addUsers.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/groups/removeUsers.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeUsers(HttpServletRequest request, @RequestBody UsersGroupRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> usersId = r.usersId;
		Group group = groupService.removeUsers(r.groupId, usersId);
		response.setObj(group.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.group.removeUsers.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/groups/addRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> addRoles(HttpServletRequest request, @RequestBody RolesGroupRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> rolesId = r.rolesId;
		authorizationControlService.assignApplicationRoleToEntity(app.getId(), rolesId, r.groupId);
		Group group = groupService.findById(r.groupId);
		response.setObj(group.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.group.addRoles.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/groups/removeRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeRoles(HttpServletRequest request, @RequestBody RolesGroupRequest r) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		JsonResponse<String> response = new JsonResponse<String>();

		List<Long> rolesId = r.rolesId;
		authorizationControlService.deleteRolesFromEntityOnApplication(app.getId(), rolesId, r.groupId);
		Group group = this.groupService.findById(r.groupId);
		response.setObj(group.getId().toString());
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.group.removeRoles.title"));
		response.setMessage("");

		return response;
	}

	public static class RolesGroupRequest {
		
		private Long groupId;
		
		private List<Long> rolesId;

		public Long getGroupId() {
			return groupId;
		}

		public void setGroupId(Long groupId) {
			this.groupId = groupId;
		}

		@JsonDeserialize(as = List.class, contentAs = Long.class)
		public List<Long> getRolesId() {
			return rolesId;
		}

		public void setRolesId(List<Long> rolesId) {
			this.rolesId = rolesId;
		}
	}

	public static class UsersGroupRequest {
		
		private Long groupId;
		
		private List<Long> usersId;

		public Long getGroupId() {
			return groupId;
		}

		public void setGroupId(Long groupId) {
			this.groupId = groupId;
		}

		@JsonDeserialize(as = List.class, contentAs = Long.class)
		public List<Long> getUsersId() {
			return usersId;
		}

		public void setUsersId(List<Long> usersId) {
			this.usersId = usersId;
		}
	}

	public static class GroupSaveRequest {
		
		private String groupId;
		
		private String name;
		
		private String description;

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
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
}
