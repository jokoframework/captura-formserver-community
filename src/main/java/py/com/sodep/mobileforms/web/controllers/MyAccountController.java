package py.com.sodep.mobileforms.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class MyAccountController extends SodepController {

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IUserService userService;

	@RequestMapping(value = "/settings/my-account.mob")
	public ModelAndView myaccount(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		Application app = mgr.getApplication();
		I18nManager i18n = mgr.getI18nManager();
		ModelAndView mav = new ModelAndView("/home/pages/my-account/my-account.ftl");

		boolean appSettings = false;
		if (!user.isRootUser()) {
			appSettings = authControlService.has(app, user, AuthorizationNames.App.APP_CONFIG);
			mav.addObject("appConfigLinkText", i18n.getMessage("web.myaccount.myapps.settings.label", app.getName()));
		}

		mav.addObject("firstName", user.getFirstName());
		mav.addObject("lastName", user.getLastName());
		mav.addObject("mail", user.getMail());
		mav.addObject("appSettings", appSettings);

		return mav;
	}

	@RequestMapping(value = "/settings/loadSettings.ajax")
	public @ResponseBody
	UserSettingsJSON loadUserSettings(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		UserSettingsJSON settings = new UserSettingsJSON();
		settings.setLanguage(i18n.getSelectedLanguage());
		User user = mgr.getUser();
		Application application = user.getApplication();
		if (application != null) {
			settings.setDefaultApplicationId(application.getId());
		}
		return settings;
	}

	public static class ChangeSettingsResult {

		private boolean reload;

		public boolean isReload() {
			return reload;
		}

		public void setReload(boolean reload) {
			this.reload = reload;
		}
	}

	@RequestMapping(value = "/settings/change.ajax")
	public @ResponseBody
	JsonResponse<ChangeSettingsResult> changeSettings(HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserSettingsJSON userSettings) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();
		String newLanguage = userSettings.getLanguage();

		ChangeSettingsResult result = new ChangeSettingsResult();
		String message = i18n.getMessage("myAccount.success.message");

		if (newLanguage != null && !user.getLanguage().equals(newLanguage)) {
			user = userService.changeLanguage(user, newLanguage);
			mgr.setUser(user);
			result.setReload(true);
			message = i18n.getMessage("myAccount.success.languageChangedMessage");
		}

		Long defaultApplicationId = userSettings.getDefaultApplicationId();
		if (defaultApplicationId != null) {
			Application application = user.getApplication();
			if (application == null || !application.getId().equals(defaultApplicationId)) {
				user = userService.changeDefaultApplication(user.getId(), defaultApplicationId);
				mgr.setUser(user);
			}
		}

		JsonResponse<ChangeSettingsResult> jsonResponse = new JsonResponse<ChangeSettingsResult>();
		jsonResponse.setObj(result);
		jsonResponse.setSuccess(true);
		jsonResponse.setTitle(i18n.getMessage("myAccount.success.title"));
		jsonResponse.setMessage(message);
		return jsonResponse;
	}
	@RequestMapping(value = "/settings/changeUser.ajax")
	public @ResponseBody
	JsonResponse<ChangeSettingsResult> changeUser(HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserDTO newUserData) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		
		User user = mgr.getUser();
		user = userService.changeFullName(user, newUserData.getFirstName(), newUserData.getLastName());
		mgr.setUser(user);
						
		ChangeSettingsResult result = new ChangeSettingsResult();
		result.setReload(true);
		
		String message = i18n.getMessage("myAccount.userData.success.message");

		JsonResponse<ChangeSettingsResult> jsonResponse = new JsonResponse<ChangeSettingsResult>();
		jsonResponse.setObj(result);
		jsonResponse.setSuccess(true);
		jsonResponse.setTitle(i18n.getMessage("myAccount.success.title"));
		jsonResponse.setMessage(message);
		return jsonResponse;
	}
}
