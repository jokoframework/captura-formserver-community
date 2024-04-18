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

import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.MFPage;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.GroupDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.JsonResponseObjectCreated;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

// TODO use authorizationControlService to check on permissions
// FIXME Bugs about languages 
@Controller
public class FormCrudController extends SodepController {

	@Autowired
	private IFormService formService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IAuthorizationControlService authControlService;
	
	@Autowired
	private IFormModelService formModel;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IWorkflowDataService workflowService;

	private FormDTO getFormDTO(String id, String label, Project project, Boolean active) {
		FormDTO dto = new FormDTO();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			dto.setId(Long.parseLong(id));
		}
		dto.setLabel(label);
		dto.setProjectId(project.getId());
		dto.setActive(active);
		return dto;
	}

	@RequestMapping(value = "/cruds/forms/getLastVersion.ajax", method = RequestMethod.POST)
	@ResponseBody
	FormDTO getForm(HttpServletRequest request, @RequestParam(value = "formId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();
		FormDTO formDTO = formService.getFormLastVersion(formId, i18n.getSelectedLanguage());
		formDTO.setWorkflow(workflowService.workflowEnabled(user, formDTO));

		return formDTO;
	}

	// FIXME #738
	// A forged Request may allow a logged user to modify Forms that don't
	// belong to the application
	@RequestMapping(value = "/cruds/forms/save.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponseObjectCreated<FormDTO> saveForm(HttpServletRequest request, @RequestBody FormSaveRequest fr) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponseObjectCreated<FormDTO> response = new JsonResponseObjectCreated<FormDTO>();
		Project project = projectService.findById(new Long(fr.projectId));
		FormDTO dto = getFormDTO(fr.rootId, fr.label, project, true);

		try {
			if (dto.getId() == null) { // it's new
				dto = formModificationService.create(project, dto, mgr.getUser(), i18n.getSelectedLanguage());
				response.setObj(dto);
				response.setComputedAuthorizations(authControlService.obtainComputedAuth(mgr.getApplication(),mgr.getUser()));
			} else { // it's editing
				dto = formModificationService.updateLabel(dto);
				response.setObj(dto);
			}
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.form.saved.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.saved.message", dto.getLabel()));
		} catch (InvalidEntityException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.invalid.errorSaving"));
			response.setMessage(i18n.getMessage(e.getMessage()));
		} catch (OptimisticLockException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setMessage(i18n.getMessage("admin.cruds.form.invalid.lockError.creation"));
		}
		return response;
	}

	@RequestMapping(value = "/cruds/forms/saveEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> saveEntityRolesInForm(HttpServletRequest request, @RequestBody EntityRolesInFormRequest f) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Long formId = f.getRootId();
		Form form = formService.findById(formId);
		authorizationControlService.assignFormRoleToEntity(form, f.getRolesId(), f.getEntityId());

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.form.roles.saved.title"));
		response.setMessage("");

		return response;
	}

	@RequestMapping(value = "/cruds/forms/removeEntityRoles.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<String> removeEntityRolesInProject(HttpServletRequest request,
			@RequestParam(value = "rootId", required = true) Long formId,
			@RequestParam(value = "entityId", required = true) Long entityId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Form form = this.formService.findById(formId);
		List<Role> assignedRoles = authorizationControlService.listAssignedRoles(form, entityId, null);
		List<Long> rolesId = new ArrayList<Long>();
		for (Role role : assignedRoles) {
			rolesId.add(role.getId());
		}
		this.authorizationControlService.deleteRolesFromEntityOnForm(formId, rolesId, entityId);

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.form.roles.deleted.title"));
		response.setMessage("");

		return response;
	}

	// FIXME this method and ProjectCrudController's userAuth are almost a
	// copy-paste
	@RequestMapping("/cruds/forms/userAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<UserDTO>> userAuth(HttpServletRequest request, @RequestParam(value = "rootId") Long formId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String sort) {

		Form form = formService.findById(formId);

		boolean ascending = sort != null ? sort.equalsIgnoreCase("asc") : true;

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = User.MAIL;
		}

		PagedData<List<User>> data = authorizationControlService.listUsersInFormWithoutOwner(form, orderBy, ascending,
				pageNumber, pageSize);
		List<User> users = data.getData();
		List<UserDTO> dtos = userDTOWithRoles(form, users);
		PagedData<List<UserDTO>> usersPageDTO = new PagedData<List<UserDTO>>(dtos, data.getTotalCount(),
				data.getPageNumber(), data.getPageSize(), data.getAvailable());
		return usersPageDTO;
	}

	private List<UserDTO> userDTOWithRoles(Form form, List<User> users) {
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User u : users) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(form, u, null);
			UserDTO dto = UserViewUtils.translate(u);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	// FIXME this method and ProjectCrudController's groupAuth are almost a
	// copy-paste
	@RequestMapping("/cruds/forms/groupAuth/paging/read.ajax")
	public @ResponseBody
	PagedData<List<GroupDTO>> groupAuth(HttpServletRequest request,
			@RequestParam(value = "rootId", required = true) Long formId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) {

		Form form = formService.findById(formId);

		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}
		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = Group.NAME;
		}

		PagedData<List<Group>> data = authorizationControlService.listGroupsInForm(form, orderBy, ascending,
				pageNumber, pageSize);
		List<Group> groups = data.getData();
		List<GroupDTO> dtos = groupDTOWithRoles(form, groups);
		return new PagedData<List<GroupDTO>>(dtos, data.getTotalCount(), data.getPageNumber(), data.getPageSize(),
				data.getAvailable());
	}

	private List<GroupDTO> groupDTOWithRoles(Form form, List<Group> groups) {
		List<GroupDTO> dtos = new ArrayList<GroupDTO>();
		for (Group g : groups) {
			List<Role> assignedRoles = authorizationControlService.listAssignedRoles(form, g, null);
			GroupDTO dto = GroupViewUtils.translate(g);
			dto.setRoles(RoleViewUtils.translate(assignedRoles));
			dtos.add(dto);
		}
		return dtos;
	}

	@RequestMapping(value = "/cruds/forms/publish.ajax", method = RequestMethod.POST)
	@ResponseBody
	// FIXME need to change on the JS side the parameter name to formId
	JsonResponse<FormDTO> publish(HttpServletRequest request, @RequestParam(value = "rootId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<FormDTO> response = new JsonResponse<FormDTO>();
		String language = mgr.getI18nManager().getSelectedLanguage();

		FormDTO formDTO = formService.getFormLastVersion(formId, language);
		MFForm model = formModel.getMFForm(formId, formDTO.getVersion(), language);
		List<MFPage> pages = model.getPages();
		
		if (pages == null || pages.isEmpty()) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.notPublished.noPages.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.notPublished.noPages.message", formDTO.getLabel()));
		} else if (!hasElements(model)) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.notPublished.noElements.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.notPublished.noElements.message", formDTO.getLabel()));
		} else {
			formModificationService.publish(formId);
			formDTO = formService.getFormLastVersion(formId, language);
			response.setObj(formDTO);
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("admin.cruds.form.published.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.published.message", formDTO.getLabel()));
			// #4060
			recomputeUserAuthorizations(mgr.getApplication());
		}
		
		
		return response;
	}

	// #4060
	private void recomputeUserAuthorizations(Application app) {
		List<UserDTO> listAllActiveUsers = userService.listAllActiveUsers(app);
		for(UserDTO dto : listAllActiveUsers ){
			User u = userService.findById(dto.getId());
			authControlService.computeUserAccess(u, app);
		}
	}

	private boolean hasElements(MFForm form) {
		return !form.listAllElements().isEmpty();
	}

	@RequestMapping(value = "/cruds/forms/unpublish.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<FormDTO> unpublish(HttpServletRequest request, @RequestParam(value = "rootId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<FormDTO> response = new JsonResponse<FormDTO>();
		formModificationService.unpublish(formId);
		FormDTO formDTO = formService.getFormLastVersion(formId, i18n.getSelectedLanguage());
		// FIXME why do we need to send the whole object at this point ?
		response.setObj(formDTO);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.form.unpublished.title"));
		response.setMessage(i18n.getMessage("admin.cruds.form.unpublished.message", formDTO.getLabel()));
		return response;
	}

	@RequestMapping("/cruds/forms/delete.ajax")
	public @ResponseBody
	JsonResponse<String> deleteForm(HttpServletRequest request, @RequestParam(value = "id") Long formId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> response = new JsonResponse<String>();
		String language = i18n.getSelectedLanguage();
		FormDTO dto = formService.getFormLastVersion(formId, language);
		try {
			if (!dto.isPublished()) {
				formService.logicalDelete(dto);
				response.setSuccess(true);
				response.setTitle(i18n.getMessage("admin.cruds.form.deleted.title"));
				response.setMessage(i18n.getMessage("admin.cruds.form.deleted.message", dto.getLabel()));
			} else {
				response.setSuccess(false);
				response.setTitle(i18n.getMessage("admin.cruds.form.cannot.delete.published.title"));
				response.setMessage(i18n.getMessage("admin.cruds.form.cannot.delete.published.message", dto.getLabel()));
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setMessage(i18n.getMessage("admin.cruds.form.deleted.error", dto.getLabel()));
		}
		return response;
	}

	public static class EntityRolesInFormRequest {
		private Long rootId;
		private Long entityId;
		private List<Long> rolesId;

		public Long getRootId() {
			return rootId;
		}

		public void setRootId(Long rootId) {
			this.rootId = rootId;
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

	public static class FormSaveRequest {
		private String projectId; // FIXME Why is projectId a String?
		private String rootId; // FIXME Why is rootId a String? what does it
								// refer to?
		private String label;
		private String description;

		public String getRootId() {
			return rootId;
		}

		public void setRootId(String rootId) {
			this.rootId = rootId;
		}

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
