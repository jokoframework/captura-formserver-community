package py.com.sodep.mobileforms.web.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.dtos.PendingRegistrationDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.dtos.stats.DataUsage;
import py.com.sodep.mobileforms.api.dtos.stats.FailedDocument;
import py.com.sodep.mobileforms.api.dtos.stats.UsageStats;
import py.com.sodep.mobileforms.api.dtos.widgets.SimpleTableDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.exceptions.TooManyApplicationsException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.control.ITokenService;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.notifications.INotificationManager;
import py.com.sodep.mobileforms.api.services.stats.IStatsService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.license.MFFormServerLicense;
import py.com.sodep.mobileforms.utils.TemporalUtils;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class AdminHomeController extends SodepController {

	private static final Logger logger = Logger.getLogger(AdminHomeController.class);

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IApplicationService appService;

	@Autowired
	private IUserService userService;

	@Autowired
	private ITokenService tokenService;

	@Autowired
	private MFLicenseManager licenseManager;
	
	@Autowired
	private IStatsService statsService;
	
	@Autowired
	private I18nBundle i18nBundle;
	
	@Autowired
	private ISystemParametersBundle systemParameter;
	
	@Autowired
	private INotificationManager notificationManager;

	@RequestMapping(value = "/admin/app.mob")
	public ModelAndView enterApp(HttpServletRequest request, @RequestParam(value = "appId") Long appId) {
		SessionManager mgr = new SessionManager(request);
		User user = getRootUserOrThrowException(mgr);

		ModelAndView mav = new ModelAndView("/home/home.ftl");
		String deployId = systemParameter.getStrValue(DBParameters.SYS_DEPLOY_ID);
		mav.addObject("deployId", deployId);
		Application application = applicationService.findById(appId);
		mgr.setApplication(application);

		authControlService.clearUserAccess(user);
		authControlService.computeUserAccess(user, mgr.getApplication());
		authControlService.assignRoleToEntity(mgr.getApplication(), user.getId(), "ROLE_APP_ADMIN",
				Authorization.LEVEL_APP, application.getId());

		// FIXME User entity is being exposed to the view layer
		mav.addObject("user", user);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		mav.addObject("year", year);
		return mav;
	}

	
	@RequestMapping(value = "/admin/pendingRegistrations.mob")
	public ModelAndView pendingRegistrations(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		ModelAndView mav = new ModelAndView("/admin/pending-registrations.ftl");
		return mav;
	}
	
	@RequestMapping(value = "/admin/dataUsage.mob")
	public ModelAndView dataUsagePage(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		ModelAndView mav = new ModelAndView("/admin/dataUsage.ftl");
		return mav;
	}
	
	@RequestMapping(value = "/admin/failedDocuments.mob")
	public ModelAndView failedDocumentsPage(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		ModelAndView mav = new ModelAndView("/admin/failedDocuments.ftl");
		return mav;
	}

	@RequestMapping(value = "/admin/cancelPendingRegistration.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<Boolean> cancelPendingRegistration(HttpServletRequest request,
			@RequestParam(value = "id") Long id) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);

		boolean tokenDeactivated = tokenService.deactivateToken(id);
		JsonResponse<Boolean> jsonResponse = new JsonResponse<Boolean>();
		jsonResponse.setSuccess(tokenDeactivated);
		I18nManager i18n = mgr.getI18nManager();
		if (tokenDeactivated) {
			jsonResponse.setTitle(i18n.getMessage("web.root.pendingRegistration.cancelled.title"));
			jsonResponse.setMessage(i18n.getMessage("web.root.pendingRegistration.cancelled.message"));
		} else {
			jsonResponse.setTitle(i18n.getMessage("web.root.pendingRegistration.not_cancelled.title"));
			jsonResponse
					.setMessage(i18n.getMessage("web.root.pendingRegistration.not_cancelled.message", id.toString()));
		}
		return jsonResponse;
	}

	@RequestMapping(value = "/admin/pendingRegistrations/list.ajax")
	@ResponseBody
	public PagedData<List<PendingRegistrationDTO>> listPendingRegistrations(HttpServletRequest request,
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
		getRootUserOrThrowException(mgr);

		return userService.listPendingRegistrations(page, rows);
	}

	@RequestMapping(value = "/admin/users/allActive.ajax")
	@ResponseBody
	public List<UserDTO> listAllActiveUsers(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);

		return userService.listAllActiveUsers();
	}

	@RequestMapping("/admin/createApp.ajax")
	public @ResponseBody
	JsonResponse<String> createApp(HttpServletRequest request, @RequestParam("appName") String appName,
			@RequestParam Long ownerId) {
		I18nManager i18n = I18nManager.getI18n(request);
		JsonResponse<String> response = new JsonResponse<String>();
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		if (user.isRootUser()) {
			try {
				User owner = user;
				if (ownerId != null && ownerId > 0) {
					owner = userService.findById(ownerId);
				}
				Application app = initAppWithOwner(appName, i18n, owner);
				if (app != null) {
					response.setTitle(i18n.getMessage("web.root.createApp.success.title"));
					response.setMessage(i18n.getMessage("web.root.createApp.success.msg", appName));
					response.setSuccess(true);
				} else {
					response.setTitle(i18n.getMessage("web.root.createApp.error.title"));
					response.setMessage(i18n.getMessage("web.root.createApp.error.message", appName));
					response.setSuccess(false);
				}
			} catch (TooManyApplicationsException e) {
				response.setTitle(i18n.getMessage("web.root.createApp.error.title"));
				response.setMessage(i18n.getMessage("web.root.max_apps.reached", Long.toString(e.getMaxCount())));
				response.setSuccess(false);
			} catch (DuplicateEntityException e) {
				response.setTitle(i18n.getMessage("web.root.createApp.error.title"));
				response.setMessage(i18n.getMessage("web.root.app.duplicate", appName));
				response.setSuccess(false);
			}
			return response;
		} else {
			throw new AuthorizationException("User #" + user.getId() + " can't create apps");
		}
	}

	private Object initAppLock = new Object();

	private Application initAppWithOwner(String appName, I18nManager i18n, User owner) {
		synchronized (initAppLock) {
			Application app = appService.initAppWithOwner(appName, owner, true, i18n.getSelectedLanguage());
			return app;
		}
	}

	private User getRootUserOrThrowException(SessionManager mgr) {
		User user = mgr.getUser();
		if (user.isRootUser()) {
			return user;
		}
		throw new AuthorizationException("Not a system administrator");
	}

	@RequestMapping("/admin/showServerInfo.ajax")
	public @ResponseBody
	List<SimpleTableDTO> showServerInfo(HttpServletRequest request) {
		List<SimpleTableDTO> vars = new ArrayList<SimpleTableDTO>();
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		if (user.isRootUser()) {
			MFFormServerLicense license = licenseManager.getFormServerLicense();
			vars = getServerInfo(session.getApplication(), license, session.getI18nManager());
			return vars;
		} else {
			throw new AuthorizationException("User #" + user.getId() + " can't see Server Info");
		}
	}
	
	@RequestMapping("/admin/appStats.ajax")
	public @ResponseBody
	UsageStats appStats(HttpServletRequest request, @RequestParam("appId") Long appId) {
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		UsageStats stats = statsService.getAppUsageStats(appId);
		return stats;
	}
	
	@RequestMapping("/admin/dataUsage.ajax")
	public @ResponseBody 
	List<DataUsage> dataUsage(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false, defaultValue ="false") Boolean _search){
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		String searchValue = "";
		if (_search) {
			searchValue = request.getParameter("name").trim();
		}
		return statsService.getAllAppsDataUsage(_search, searchValue, page, rows, orderBy, order);
	}
	
	@RequestMapping("/admin/failedDocuments.ajax")
	public @ResponseBody 
	List<FailedDocument> failedDocuments(HttpServletRequest request){
		SessionManager mgr = new SessionManager(request);
		getRootUserOrThrowException(mgr);
		return statsService.getFailedDocuments(30);
	}
	
	@RequestMapping(value = "/admin/systemParameters.mob")
	public ModelAndView parameters(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/admin/systemParameters.ftl");
		return mav;
	}
	
	@RequestMapping(value = "/admin/uncaughtException.mob")
	public ModelAndView unCaughtException(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/admin/uncaughtException.ftl");
		return mav;
	}
	
	@RequestMapping(value = "/admin/reloadi18n.ajax")
	@ResponseBody
	public JsonResponse<String> reloadi18n() {
		i18nBundle.reloadLanguages();
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle("i18n reloaded");
		response.setMessage("I18n were reloaded from the DB");
		return response;
	}

	@RequestMapping(value = "/admin/reloadParameters.ajax")
	@ResponseBody
	JsonResponse<String> reloadParameters(HttpServletRequest request) {
		systemParameter.reload();
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle("Parameters reloaded");
		response.setMessage("Parameters where successfully reloaded");

		return response;
	}
	
	@RequestMapping(value = "/admin/disableNotifications.ajax")
	@ResponseBody
	public JsonResponse<String> disableNotifications(HttpServletRequest request, @RequestParam("disable") Boolean disable) {
		// method call
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		notificationManager.disableNotifications(user, disable);
		I18nManager i18n = I18nManager.getI18n(request);
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		if (disable) {
			response.setMessage(i18n.getMessage("web.admin.failed_document.notification_disabled")); 
		} else {
			response.setMessage(i18n.getMessage("web.admin.failed_document.notification_enabled", systemParameter.getStrValue(DBParameters.SYS_NOTIFICATION_SUPPORT_MAIL_ADDRESS))); 
		}
		return response;
	}
	
	@RequestMapping(value = "/admin/getAppsSummary.ajax")
	@ResponseBody
	public JsonResponse<String> getAppsSummary(HttpServletRequest request) {
		JsonResponse<String> response = new JsonResponse<String>();
	
		Long totalApps = applicationService.count();
		Long activeApps = applicationService.countActive();
		Long inactiveApps = totalApps - activeApps;
		
		response.addContent("totalApps", totalApps);
		response.addContent("activeApps", activeApps);
		response.addContent("inactiveApps", inactiveApps);
		response.setSuccess(true);
		
		return response;
	}
	
	@RequestMapping(value = "/admin/getNotificationsStatus.ajax")
	@ResponseBody
	public JsonResponse<String> getNotificationStatus(HttpServletRequest request) {
		Boolean disabled = notificationManager.getNotificationsStatus();
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setObj(disabled.toString());
		I18nManager i18n = I18nManager.getI18n(request);
		if (disabled) {
			response.setMessage(i18n.getMessage("web.admin.failed_document.notification_disabled")); 
		} else {
			response.setMessage(i18n.getMessage("web.admin.failed_document.notification_enabled", systemParameter.getStrValue(DBParameters.SYS_NOTIFICATION_SUPPORT_MAIL_ADDRESS))); 
		}
		return response;
	}
	
	
	private List<SimpleTableDTO> getServerInfo(Application app, final MFFormServerLicense license, I18nManager i18n) {
		List<SimpleTableDTO> vars = new ArrayList<SimpleTableDTO>();
		loadServerLicenseProperties(license, i18n, vars);
		if (license.getDefaultApplicationLicense() != null) {
			loadDefaultApplicationLicenseProperties(license.getDefaultApplicationLicense(), i18n, vars);
		}
		logger.debug("Returning server properties, found:  " + vars.size());
		return vars;
	}

	private void loadDefaultApplicationLicenseProperties(MFApplicationLicense mfApplicationLicense, I18nManager i18n,
			List<SimpleTableDTO> vars) {
		Integer validDays = mfApplicationLicense.getValidDays() != null ? mfApplicationLicense.getValidDays()
				.intValue() : 0;
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.application.validDays"), validDays));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.application.maxDevices"), mfApplicationLicense
				.getMaxDevices()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.application.maxUsers"), mfApplicationLicense
				.getMaxUsers()));
	}

	private void loadServerLicenseProperties(final MFFormServerLicense license, I18nManager i18n,
			List<SimpleTableDTO> vars) {
		Date expirationDate = TemporalUtils.shiftDays(license.getCreationDate(), license.getValidDays().intValue());
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.serverId"), license.getServerId()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.creationDate"), TemporalUtils.formatDate(license
				.getCreationDate())));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.validDays"), license.getValidDays()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.expirationDate"), TemporalUtils
				.formatDate(expirationDate)));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.maxApplications"), license.getMaxApplications()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.applicationsInUse"), applicationService.count()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.applicationsRemaining"), license
				.getMaxApplications() - applicationService.count()));
		vars.add(new SimpleTableDTO(i18n.getMessage("web.root.license.otherProperties"), license.getProperties()));
	}

}
