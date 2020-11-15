package py.com.sodep.mobileforms.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.MFPage;
import py.com.sodep.mf.form.model.prototype.MFInput.Type;
import py.com.sodep.mf.form.model.prototype.MFPrototype;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.dtos.PoolModelDTO;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.forms.model.ExecResponse;
import py.com.sodep.mobileforms.api.services.forms.model.ICommandService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormModificationService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.web.editor.ToolbarConfig;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponseObjectCreated;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class EditorController extends SodepController {

	private static Logger logger = LoggerFactory.getLogger(EditorController.class);

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IElementPrototypeService elementPrototypeService;

	@Autowired
	private IFormModelService formModel;

	@Autowired
	private IFormService formService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormModificationService formModificationService;

	@Autowired
	private ICommandService commandModelService;

	@Autowired
	private IAuthorizationControlService authControlService;
	
	@Autowired
	private IUserService userService;

	// Pool info and process item info
	@RequestMapping(value = "/editor/toolbox/config.ajax")
	public @ResponseBody
	JsonResponse<ToolbarConfig> toolbox(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();
		Application app = manager.getApplication();

		List<PoolModelDTO> poolsDTOs = new ArrayList<PoolModelDTO>();

		addSystemTemplates(app, i18n, language, poolsDTOs);
		addApplicationTemplates(app, i18n, language, poolsDTOs);
		addUserPools(app, manager.getUser(), i18n, language, poolsDTOs);

		JsonResponse<ToolbarConfig> resp = new JsonResponse<ToolbarConfig>();
		resp.setSuccess(true);
		ToolbarConfig config = new ToolbarConfig();
		config.setSections(poolsDTOs);
		config.setTitle(i18n.getMessage("web.editor.toolbox.title"));
		resp.setObj(config);
		return resp;
	}

	private void addUserPools(Application app, User user, I18nManager i18n, String language,
			List<PoolModelDTO> poolModelDTOs) {
		List<PoolDTO> poolDTOs = poolService.listPools(app, user, language, AuthorizationNames.Pool.READ);
		for (PoolDTO poolDTO : poolDTOs) {
			PoolModelDTO poolModelDTO = new PoolModelDTO(poolDTO);
			poolModelDTOs.add(poolModelDTO);
			Pool pool = poolService.findById(poolDTO.getId());
			List<ElementPrototype> elementPrototypes = elementPrototypeService.findAll(user, pool,
					AuthorizationNames.Pool.READ);
			List<MFPrototype> prototypesDTOs = toDTOList(app, i18n, language, elementPrototypes);
			poolModelDTO.setPrototypes(prototypesDTOs);
		}
	}

	private void addApplicationTemplates(Application app, I18nManager i18n, String language,
			List<PoolModelDTO> poolsDTOs) {
		// #384 In the Form Editor, show process items that aren't in a pool
		List<ElementPrototype> applicationPool = elementPrototypeService.applicationTemplatePrototypes(app);
		if (!applicationPool.isEmpty()) {
			PoolModelDTO applicationPoolDTO = new PoolModelDTO();
			applicationPoolDTO.setActive(true);
			applicationPoolDTO.setId(null);
			applicationPoolDTO.setName(i18n.getMessage("web.editor.toolbox.applicationPool"));
			applicationPoolDTO.setDescription(i18n.getMessage("web.editor.toolbox.applicationPool"));
			List<MFPrototype> prototypesDTOs = toDTOList(app, i18n, language, applicationPool);
			applicationPoolDTO.setPrototypes(prototypesDTOs);
			poolsDTOs.add(applicationPoolDTO);
		}
	}

	private void addSystemTemplates(Application app, I18nManager i18n, String language, List<PoolModelDTO> poolsDTOs) {
		List<ElementPrototype> systemPool = elementPrototypeService.systemTemplatePrototypes();

		if (!systemPool.isEmpty()) {
			PoolModelDTO systemPoolDTO = new PoolModelDTO();
			systemPoolDTO.setActive(true);
			systemPoolDTO.setId(null);
			systemPoolDTO.setName(i18n.getMessage("web.editor.toolbox.systemPool"));
			systemPoolDTO.setDescription(i18n.getMessage("web.editor.toolbox.systemPool"));
			List<MFPrototype> prototypesDTOs = toDTOList(app, i18n, language, systemPool);
			systemPoolDTO.setPrototypes(prototypesDTOs);
			poolsDTOs.add(systemPoolDTO);
		}
	}

	private List<MFPrototype> toDTOList(Application app, I18nManager i18n, String language,
			List<ElementPrototype> prototypes) {
		List<MFPrototype> prototypesDTOs = new ArrayList<MFPrototype>();
		for (ElementPrototype proto : prototypes) {
			if (!proto.getDeleted()) {
				// #2258 Hide process item of type password
				if (proto instanceof Input) {
					Input input = (Input) proto;
					if (input.getType() != Type.PASSWORD) {
						MFPrototype dto = formModel.buildMFPrototype(app, proto, language);
						prototypesDTOs.add(dto);
					}
				} else {
					MFPrototype dto = formModel.buildMFPrototype(app, proto, language);
					prototypesDTOs.add(dto);
				}

			}
		}
		return prototypesDTOs;
	}

	/**
	 * The model of the last version of the form
	 * @param request
	 * @param formId
	 * @return
	 */
	// Form Model to render in the editor
	@RequestMapping(value = "/editor/form/model.ajax")
	public @ResponseBody
	JsonResponse<MFForm> model(HttpServletRequest request, @RequestParam(value = "formId") Long formId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();

		// obtain the last version of the form
		Long lastVersion = formService.getLastVersion(formId);

		JsonResponse<MFForm> response = new JsonResponse<MFForm>();

		MFForm model = formModel.getMFForm(formId, lastVersion, language);
		response.setSuccess(true);
		response.setObj(model);
		return response;
	}

	@RequestMapping(value = "/editor/form/save.ajax")
	public @ResponseBody
	JsonResponse<Object> save(HttpServletRequest request, @RequestParam(value = "formId") Long formId,
			@RequestBody Command[] commands) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();
		User user = manager.getUser();

		JsonResponse<Object> response = new JsonResponse<Object>();
		try {
			ExecResponse execResponse = commandModelService.executeSave(user, formId, commands);
			if (execResponse == null) {
				response.setSuccess(true);
				response.setTitle(i18n.getMessage("web.editor.noChanges.Title"));
				response.setMessage(i18n.getMessage("web.editor.noChanges"));
				return response;
			}

			boolean success = execResponse.isSuccess();
			response.setSuccess(success);
			if (success) {
				Long lastVersion = formService.getLastVersion(formId);
				MFForm model = formModel.getMFForm(formId, lastVersion, language);
				response.setObj(model);
				response.setTitle(i18n.getMessage("web.editor.saved"));
				response.setMessage(i18n.getMessage("web.editor.saved"));
			} else {
				Exception thrown = execResponse.getExceptionThrown();
				response.setTitle(i18n.getMessage("web.generic.error"));
				if (thrown != null) {
					Command offendingCommand = execResponse.getCmd();
					if (offendingCommand != null) {
						response.addContent("cmd", offendingCommand);
					}
					response.setMessage(thrown.getMessage());
				} else {
					response.setMessage(i18n.getMessage("web.generic.unknownError"));
				}
			}

		} catch (Exception e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setMessage(i18n.getMessage("web.generic.unknownError"));
			logger.debug(e.getMessage(), e);
		}
		return response;
	}

	@RequestMapping(value = "/editor/form/saveAs.ajax")
	public @ResponseBody
	JsonResponseObjectCreated<Object> saveAs(HttpServletRequest request, @RequestParam(value = "formId") Long formId,
			@RequestParam(value = "formLabel") String formLabel, @RequestParam(value = "projectId") Long projectId,
			@RequestBody Command[] commands) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();
		User user = manager.getUser();
		
		JsonResponseObjectCreated<Object> response = new JsonResponseObjectCreated<Object>();
		try {
			Project project = projectService.findById(projectId);
			ExecResponse execResponse = commandModelService.executeSaveAs(user, project, formId, formLabel, commands);

			boolean success = execResponse.isSuccess();
			response.setSuccess(success);
			if (success) {
				Long newFormId = execResponse.getFormId();
				MFForm model = formModel.getMFForm(newFormId, 1L, language);
				response.setObj(model);
				response.setTitle(i18n.getMessage("web.editor.saved"));
				response.setMessage(i18n.getMessage("web.editor.saved"));
				response.setComputedAuthorizations(authControlService.obtainComputedAuth(manager.getApplication(),user));
			} else {
				Exception thrown = execResponse.getExceptionThrown();
				response.setTitle(i18n.getMessage("web.generic.error"));
				if (thrown != null) {
					Command offendingCommand = execResponse.getCmd();
					if (offendingCommand != null) {
						response.addContent("cmd", offendingCommand);
					}
					response.setMessage(thrown.getMessage());
				} else {
					response.setMessage(i18n.getMessage("web.generic.unknownError"));
				}
			}

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

	//FIXME copy paste from FormCrudController!
	@RequestMapping(value = "/editor/publish.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<MFForm> publish(HttpServletRequest request, @RequestParam(value = "formId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		String language = i18n.getSelectedLanguage();

		JsonResponse<MFForm> response = new JsonResponse<MFForm>();
		
		FormDTO formDTO = formService.getFormLastVersion(formId, language);
		MFForm model = formModel.getMFForm(formId, formDTO.getVersion(), language);
		List<MFPage> pages = model.getPages();
		
		if (pages == null || pages.isEmpty()) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.notPublished.noPages.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.notPublished.noPages.message", formDTO.getLabel()));
		} else if(!hasElements(model)) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.form.notPublished.noElements.title"));
			response.setMessage(i18n.getMessage("admin.cruds.form.notPublished.noElements.message", formDTO.getLabel()));
		} else {
			formModificationService.publish(formId);
			//FIXME The model changes after the publication
			model = formModel.getMFForm(formId, formDTO.getVersion(), language);
			response.setObj(model);
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
		for (UserDTO dto : listAllActiveUsers) {
			User u = userService.findById(dto.getId());
			authControlService.computeUserAccess(u, app);
		}
	}
	
	private boolean hasElements(MFForm form){
		return !form.listAllElements().isEmpty();
	}

	@RequestMapping(value = "/editor/unpublish.ajax", method = RequestMethod.POST)
	@ResponseBody
	JsonResponse<MFForm> unpublish(HttpServletRequest request, @RequestParam(value = "formId") Long formId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		String language = i18n.getSelectedLanguage();

		JsonResponse<MFForm> response = new JsonResponse<MFForm>();
		FormDTO formDTO = formService.getFormLastVersion(formId, language);

		formModificationService.unpublish(formId);

		MFForm model = formModel.getMFForm(formId, formDTO.getVersion(), language);
		response.setObj(model);
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("admin.cruds.form.unpublished.title"));
		response.setMessage(i18n.getMessage("admin.cruds.form.published.message", formDTO.getLabel()));
		return response;
	}

	@RequestMapping(value = "/editor/properties/instance.ajax")
	public @ResponseBody
	JsonResponse<?> instanceProperties(HttpServletRequest request, @RequestParam Long formId,
			@RequestParam Long instanceId) {
		return null;
	}

	// this method list the projects where the user has authorization to create projects
	@RequestMapping(value = "/editor/projects/list.ajax", method = RequestMethod.POST)
	public @ResponseBody
	List<ProjectDTO> listProjects(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();
		// #2544
		return projectService.listProjects(app, user, i18n.getSelectedLanguage(), AuthorizationNames.Project.CREATE_FORM);
	}
}
