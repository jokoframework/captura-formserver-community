package py.com.sodep.mobileforms.web.controllers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.logging.IDBLogging;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.web.entitybuilders.ProcessItemBuilder;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
/**
 * This is the class that has the starting point of every page.
 * @author danicricco
 *
 */
public class HomeController extends SodepController {

	@Autowired
	private IUserService userService;

	@Autowired
	private IApplicationService appService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	protected ILookupTableService lookupService;
	
	@Autowired
	private ISystemParametersBundle systemParameter;
	
	@Autowired
	private IDBLogging dbLogging;

	@RequestMapping(value = "/home/home.mob")
	public ModelAndView home(HttpServletRequest request, @RequestParam(value = "appId", required = false) Long appId) {
		SessionManager mgr = new SessionManager(request);
		
		User user = mgr.getUser();
		ModelAndView mav = null;
		if (!user.isRootUser()) {
			mav = new ModelAndView("/home/home.ftl");
			// a request to change the App
			if (appId != null) {
				if (userService.isMember(appId, user)) {
					if(!appService.isActive(appId)){
						throw new AuthorizationException("Application is not active");
					}
					
					List<Application> apps = userService.listActiveApplications(user);
					Application app = appService.findById(appId);
					mgr.setAvailableApplications(apps);
					mgr.setApplication(app);
					dbLogging.logLogin(user.getId(), appId);
				} else {
					throw new AuthorizationException("You shall not PAAAAAASS!");
				}
			}

			Application app = mgr.getApplication();
			//FIXME User entity is being exposed to the view layer
			mav.addObject("user", user);
			// if the user made a full refresh of the page, then we are going to
			// recomputed the authorizations. This is a key point since the
			// computed
			// authorization will be requested by the browser after reloading
			// the home
			authControlService.computeUserAccess(user, app);
			if (app != null) {
				boolean hasAppSettingsPermissions = authControlService
						.has(app, user, AuthorizationNames.App.APP_CONFIG);
				mav.addObject("askForAppSettings", !app.isInitialSetupReady() && hasAppSettingsPermissions);
			} else {
				// TODO see ticket #475
				throw new RuntimeException("There is no support for users without application");
			}
		} else {
			mav = new ModelAndView("/admin/admin-home.ftl");
		}
		String deployId = systemParameter.getStrValue(DBParameters.SYS_DEPLOY_ID);
		mav.addObject("deployId", deployId);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		mav.addObject("year", year);
		return mav;
	}

	@RequestMapping(value = "/home/pages/editor.mob")
	public ModelAndView editor(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);

		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/editor.ftl");
		// Create a map using the "flatted" process item type list
		Map<String, String> elementPrototypes = getElementPrototypes(i18n);
		mav.addObject("elementPrototypes", elementPrototypes);

		return mav;
	}

	private Map<String, String> getElementPrototypes(I18nManager i18n) {
		String[] typeList = ProcessItemBuilder.getTypeList();
		Map<String, String> elementPrototypes = new LinkedHashMap<String, String>();
		for (String type : typeList) {
			String key = type.toLowerCase();
			String value = i18n.getMessage("admin.form.processitem.type." + key);
			elementPrototypes.put(key, value);
		}
		return elementPrototypes;
	}

	@RequestMapping(value = "/home/pages/data/data-input.mob")
	public ModelAndView dataInput(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/data/data-input.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/data/data-import.mob")
	public ModelAndView dataImport(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/data/data-import.ftl");
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Map<String, String> textQualifiers = getTextQualifiers(i18n);
		mav.addObject("textQualifiers", textQualifiers);
		return mav;
	}

	private Map<String, String> getTextQualifiers(I18nManager i18n) {
		Map<String, String> qualifiers = new HashMap<String, String>();
		qualifiers.put("doublequote", "\"");
		qualifiers.put("quote", "'");
		qualifiers.put("none", i18n.getMessage("web.dataimport.lookuptable.qualifier.none"));
		qualifiers.put("all", i18n.getMessage("web.dataimport.lookuptable.qualifier.all"));
		return qualifiers;
	}

	@RequestMapping(value = "/home/pages/data/lookuptable.mob")
	public ModelAndView lookupTable(HttpServletRequest request, HttpServletResponse response) {

		ModelAndView mav = new ModelAndView("/home/pages/data/lookuptable.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/data/lookuptable-data.mob")
	public ModelAndView lookupTableData(HttpServletRequest request, HttpServletResponse response) {

		ModelAndView mav = new ModelAndView("/home/pages/data/lookuptable-data.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/user.mob")
	public ModelAndView users(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/user-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/group.mob")
	public ModelAndView groups(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/group-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/project.mob")
	public ModelAndView projects(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/project-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/form.mob")
	public ModelAndView forms(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/form-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/process-item.mob")
	public ModelAndView processItems(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		ModelAndView mav = new ModelAndView("/home/pages/admin/process-item-crud.ftl");
		I18nManager i18n = mgr.getI18nManager();

		Map<String, String> elementPrototypes = getElementPrototypes(i18n);
		mav.addObject("elementPrototypes", elementPrototypes);

		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/role.mob")
	public ModelAndView roles(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/role-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/pool.mob")
	public ModelAndView pools(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/pool-crud.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/processes/processes.mob")
	public ModelAndView processes(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/v2/processes/processes.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/misc/not-yet-implemented.mob")
	public ModelAndView notYetImplemented(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/v2/misc/not-yet-implemented.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/users-and-groups/users-and-groups.mob")
	public ModelAndView usersAndGroups(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/v2/users-and-groups/users-and-groups.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/role-permissions.mob")
	public ModelAndView rolePermissions(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/admin/role-permissions.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/new-project-crud.mob")
	public ModelAndView newProject(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-project-crud.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.project.new.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/edit-project-crud.mob")
	public ModelAndView editProject(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-project-crud.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.project.edit.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/new-form-crud.mob")
	public ModelAndView newForm(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User user = mgr.getUser();
		Application app = mgr.getApplication();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-form-crud.ftl");
		// list the projects where the user can actually create a form. It
		// doesn't make sense to show all projects if he can't choose some of
		// them
		List<ProjectDTO> projects = projectService.listProjects(app, user, i18n.getSelectedLanguage(),
				AuthorizationNames.Project.CREATE_FORM);
		mav.addObject("projects", projects);
		mav.addObject("title", i18n.getMessage("admin.cruds.form.new.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/edit-form-crud.mob")
	@Deprecated
	public ModelAndView editForm(HttpServletRequest request, HttpServletResponse response) {
		// TODO this seems to be a duplicate of newForm
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		User user = mgr.getUser();
		Application app = mgr.getApplication();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-form-crud.ftl");
		List<ProjectDTO> projects = projectService.listProjects(app, user, i18n.getSelectedLanguage(),
				AuthorizationNames.Project.READ_WEB);
		mav.addObject("projects", projects);
		mav.addObject("title", i18n.getMessage("admin.cruds.form.edit.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/users-and-groups/new-group.mob")
	public ModelAndView newGroup(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/v2/users-and-groups/new-group.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.group.new.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/users-and-groups/edit-group.mob")
	public ModelAndView editGroup(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/v2/users-and-groups/new-group.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.group.edit.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/users-and-groups/new-user.mob")
	public ModelAndView newUser(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/v2/users-and-groups/new-user.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.user.new.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/v2/users-and-groups/edit-user.mob")
	public ModelAndView editUser(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/v2/users-and-groups/new-user.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.user.edit.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/new-pool-crud.mob")
	public ModelAndView newPool(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-pool-crud.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.pool.new.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/admin/edit-pool-crud.mob")
	public ModelAndView editPool(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/admin/new-pool-crud.ftl");
		mav.addObject("title", i18n.getMessage("admin.cruds.pool.edit.title"));
		return mav;
	}

	@RequestMapping(value = "/home/pages/process-item/process-item.mob")
	public ModelAndView editProcessItem(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/process-item/process-item.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/reports/reports.mob")
	public ModelAndView reports(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/reports/reports.ftl");
		return mav;
	}

	@RequestMapping(value = "/home/pages/reports/query.mob")
	public ModelAndView reportQuery(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/reports/query.ftl");
		return mav;
	}

	
	@RequestMapping(value = "/home/pages/v2/devices/devices.mob")
	public ModelAndView devices(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/home/pages/v2/devices/devices.ftl");
		return mav;
	}
	
}
