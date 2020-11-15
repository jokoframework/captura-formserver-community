package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.List;

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
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.JsonResponseObjectCreated;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This controller implements the server method that are being invoked by the JS
 * module new-project-crud-amd.js
 * 
 * @author danicricco
 * 
 */
@Controller
public class ProjectCrudController extends SodepController {

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IFormModificationService formModificationService;

	private ProjectDTO getProjectDTO(String id, String label, String description, String language, Long ownerId,
			Long applicationId, Boolean active) {
		ProjectDTO dto = new ProjectDTO();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			dto.setId(Long.parseLong(id));
		}

		dto.setLabel(label);
		dto.setDescription(description);
		dto.setLanguage(language);
		dto.setOwnerId(ownerId);
		dto.setApplicationId(applicationId);
		dto.setActive(active);
		return dto;
	}

	@RequestMapping("/cruds/projects/forms/paging/read.ajax")
	public @ResponseBody
	PagedData<List<FormDTO>> listForms(HttpServletRequest request,
			@RequestParam(value = "projectId", required = true) Long projectId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) {

		boolean asc = order != null ? order.equalsIgnoreCase("asc") : true;

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User user = getUser(request);

		Project project = null;
		if (projectId != null) {
			project = projectService.findById(projectId);
		} else {
			return null;
		}

		return this.formService.findFormsOfProject(project, user, AuthorizationNames.Form.READ_WEB, null, page, rows,
				i18n.getSelectedLanguage(), orderBy, asc);

	}

	@RequestMapping(value = "/cruds/projects/get.ajax", method = RequestMethod.POST)
	@ResponseBody
	ProjectDTO getProject(HttpServletRequest request, @RequestParam(value = "projectId") Long projectId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		return projectService.getProject(projectId, i18n.getSelectedLanguage());
	}

	@RequestMapping(value = "/cruds/projects/save.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponseObjectCreated<String> saveProject(HttpServletRequest request, @RequestBody ProjectSaveRequest p) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User currentUser = getUser(request);
		Application app = mgr.getApplication();
		JsonResponseObjectCreated<String> response = new JsonResponseObjectCreated<String>();
		ProjectDTO dto = getProjectDTO(p.getProjectId(), p.getLabel(), p.getDescription(), i18n.getSelectedLanguage(),
				currentUser.getId(), app.getId(), true);
		try {
			Project project = null;
			if (dto.getId() != null) {
				project = this.projectService.edit(dto.getId(), currentUser, dto);
			} else {
				project = this.projectService.createNew(app, currentUser, dto);
				response.setObj(project.getId().toString());
				response.setComputedAuthorizations(authorizationControlService.obtainComputedAuth(mgr.getApplication(),
						currentUser));
			}
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.project.saved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.project.saved.message", dto.getLabel()));
		} catch (OptimisticLockException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.project.notsaved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.project.notsaved.message", dto.getLabel()));
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	@RequestMapping(value = "/cruds/projects/saveEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> saveEntityRolesInProject(HttpServletRequest request, @RequestBody EntityRolesInProjectRequest p) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		authorizationControlService.assignProjectRoleToEntity(p.projectId, p.rolesId, p.entityId);

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.project.roles.saved.title"));
		response.setMessage("");
		return response;
	}

	@RequestMapping(value = "/cruds/projects/removeEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeEntityRolesInProject(HttpServletRequest request,
			@RequestParam(value = "projectId", required = true) Long projectId,
			@RequestParam(value = "entityId", required = true) Long entityId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Project project = this.projectService.findById(projectId);
		List<Role> assignedRoles = authorizationControlService.listAssignedRoles(project, entityId, null);
		List<Long> rolesId = new ArrayList<Long>();
		for (Role role : assignedRoles) {
			rolesId.add(role.getId());
		}
		this.authorizationControlService.deleteRolesFromEntityOnProject(projectId, rolesId, entityId);

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.project.roles.deleted.title"));
		response.setMessage("");
		return response;
	}

	@RequestMapping(value = "/cruds/projects/importForm.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponseObjectCreated<String> importForm(HttpServletRequest request,
			@RequestParam(value = "projectId") Long projectId, @RequestParam(value = "formId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponseObjectCreated<String> response = new JsonResponseObjectCreated<String>();
		String label = formService.getLabel(formId, i18n.getSelectedLanguage());
		Project project = projectService.findById(projectId);
		User user = mgr.getUser();
		try {
			formModificationService.copyIntoProject(user, project, formId);
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.project.form.imported.title"));
			response.setMessage(i18n.getMessage("admin.cruds.project.form.imported.message", label));
			response.setComputedAuthorizations(authorizationControlService.obtainComputedAuth(mgr.getApplication(),
					user));
		} catch (OptimisticLockException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.notsaved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.notsaved.message", label));
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}
		return response;
	}

	// FIXME this method and ProjectCrudController's userAuth are almost a
	// copy-paste
	@RequestMapping("/cruds/projects/userAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<UserDTO>> userAuth(HttpServletRequest request,
			@RequestParam(value = "projectId", required = true) Long projectId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String sort) {

		Project project = projectService.findById(projectId);

		boolean ascending = sort != null ? sort.equalsIgnoreCase("asc") : true;

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = User.MAIL;
		}

		PagedData<List<User>> data = authorizationControlService.listUsersInProjectWithoutOwner(project, orderBy,
				ascending, pageNumber, pageSize);
		List<User> users = data.getData();
		List<UserDTO> dtos = userDTOWithRoles(project, users);
		return new PagedData<List<UserDTO>>(dtos, data.getTotalCount(), data.getPageNumber(), data.getPageSize(),
				data.getAvailable());
	}

	@RequestMapping("/cruds/projects/delete.ajax")
	public @ResponseBody
	JsonResponse<String> deleteProject(HttpServletRequest request, @RequestParam(value = "id") Long projectId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();

		Project p = projectService.findById(projectId);
		ProjectDetails details = projectService.loadDetails(p.getId(), i18n.getSelectedLanguage());
		try {
			projectService.logicalDelete(p);
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.project.deleted.title"));
			response.setMessage(i18n.getMessage("admin.cruds.project.deleted.message", details.getLabel()));
		} catch (Exception e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setMessage(i18n.getMessage("admin.cruds.project.deleted.error", details.getLabel()));
		}
		return response;
	}

	private List<UserDTO> userDTOWithRoles(Project project, List<User> users) {
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User u : users) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(project, u, null);
			UserDTO dto = UserViewUtils.translate(u);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	// FIXME this method and ProjectCrudController's groupAuth are almost a
	// copy-paste
	@RequestMapping("/cruds/projects/groupAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<GroupDTO>> groupAuth(HttpServletRequest request,
			@RequestParam(value = "projectId", required = true) Long projectId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) {

		Project project = projectService.findById(projectId);
		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}
		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = Group.NAME;
		}

		PagedData<List<Group>> data = authorizationControlService.listGroupsInProject(project, orderBy, ascending,
				pageNumber, pageSize);
		List<Group> groups = data.getData();
		List<GroupDTO> dtos = groupDTOWithRoles(project, groups);
		return new PagedData<List<GroupDTO>>(dtos, data.getTotalCount(), data.getPageNumber(), data.getPageSize(),
				data.getAvailable());
	}

	private List<GroupDTO> groupDTOWithRoles(Project project, List<Group> groups) {
		List<GroupDTO> dtos = new ArrayList<GroupDTO>();
		for (Group g : groups) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(project, g, null);
			GroupDTO dto = GroupViewUtils.translate(g);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	public static class EntityRolesInProjectRequest {
		private Long projectId;
		private Long entityId;
		private List<Long> rolesId;

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
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

	public static class ProjectSaveRequest {

		private String projectId;

		private String label;

		private String description;

		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}
}
