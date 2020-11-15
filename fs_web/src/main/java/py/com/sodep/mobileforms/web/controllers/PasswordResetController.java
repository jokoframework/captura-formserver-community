package py.com.sodep.mobileforms.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.control.ITokenService;
import py.com.sodep.mobileforms.api.services.control.TokenPurpose;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.controllers.cruds.UserCrudController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class PasswordResetController {

	@Autowired
	private IUserService userService;

	@Autowired
	private ITokenService tokenService;

	@Autowired
	private ISystemParametersBundle systemParametersBundle;

	@RequestMapping(value = "/public/password/reset/{mail}/{resetToken}")
	public ModelAndView resetPage(HttpServletRequest request, @PathVariable("mail") String mail,
			@PathVariable("resetToken") String resetToken) {
		I18nManager i18n = I18nManager.getI18n(request);
		ModelAndView mav = new ModelAndView("/password/reset.ftl");
		User user = userService.findByMail(mail);
		boolean valid = isValid(resetToken, user);
		mav.addObject("validToken", valid);
		if (valid) {
			String fullname = user.getMail() + " (" + user.getFirstName() + " " + user.getLastName() + ")";
			mav.addObject("legend", i18n.getMessage("web.controller.password_reset.greetings", fullname));

			mav.addObject("resetToken", resetToken);
			mav.addObject("done", i18n.getMessage("web.password_reset.done"));
			mav.addObject("mail", mail);
			mav.addObject("error", i18n.getMessage("web.generic.error"));
		} else {
			mav.addObject("invalidMessage", i18n.getMessage("web.controller.password_reset.invalid"));
		}

		return mav;
	}

	private boolean isValid(String resetToken, User user) {
		return (user != null && user.getActive() != null && user.getActive())
				&& tokenService.isValid(user, resetToken, TokenPurpose.PASSWORD_RESET);
	}

	@RequestMapping(value = "/password/reset.ajax")
	public @ResponseBody
	JsonResponse<String> reset(HttpServletRequest request, @RequestParam("mail") String mail,
			@RequestParam("resetToken") String resetToken,
			@RequestParam(value = "password1", required = false) String password1,
			@RequestParam(value = "password2", required = false) String password2) {
		I18nManager i18n = I18nManager.getI18n(request);
		User user = userService.findByMail(mail);
		JsonResponse<String> response = new JsonResponse<String>();
		if (isValid(resetToken, user)) {
			boolean error = false;
			if (password1 == null || password1.trim().isEmpty()) {
				response.setUnescapedMessage(i18n.getMessage("web.generic.empty_password"));
				error = true;
			}

			if (password2 == null || password2.trim().isEmpty()) {
				response.setUnescapedMessage(i18n.getMessage("web.generic.empty_password"));
				error = true;
			}
			
			if (!error && password1.length() < UserDTO.MIN_PASSWORD_LENGTH) {
				response.setUnescapedMessage(i18n.getMessage("web.validation.user.password"));
				error = true;
			}

			if (!error && !password1.equals(password2)) {
				response.addContent("password2", i18n.getMessage("web.generic.password_not_equal"));
				response.setUnescapedMessage(i18n.getMessage("web.home.myaccount.password.error.confirm"));
				error = true;
			}

			if (!error) {
				boolean result = changePassword(user, resetToken, password1);
				response.setSuccess(result);
				if (result) {

					String contextPath = systemParametersBundle.getStrValue(DBParameters.CONTEXT_PATH);
					if (contextPath.endsWith("/")) {
						contextPath = contextPath.substring(0, contextPath.length() - 1);
					}
					String loginURL = contextPath + "/login/login.mob";
					response.setUnescapedMessage(i18n.getMessage("web.password_reset.success.message", loginURL));
				} else {
					response.setUnescapedMessage(i18n.getMessage("web.controller.password_reset.fail"));

				}
			} else {
				response.setSuccess(false);

			}
		} else {
			response.setSuccess(false);
			response.setUnescapedMessage(i18n.getMessage("web.controller.password_reset.invalid"));
		}
		return response;
	}

	private boolean changePassword(User user, String resetToken, String newPassword) {
		boolean useToken = tokenService.useToken(user, resetToken);
		if (useToken) {
			return userService.changePassword(user, newPassword);
		}
		return false;
	}

	@RequestMapping(value = "/password/reset/mail.ajax")
	public @ResponseBody
	JsonResponse<Object> mail(HttpServletRequest request, @RequestParam("id") Long userId) {
		SessionManager mgr = new SessionManager(request);
		User currentUser = mgr.getUser();
		Application app = mgr.getApplication();
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<Object> response = new JsonResponse<Object>();
		User user = userService.findById(userId);
		if (user != null) {
			boolean result = false;
			String resetPassUrl = "";
			if (user.getMail().matches(UserCrudController.MAIL_REG_EXP)) {
				result = userService.queuePasswordSetupMail(app, currentUser, user);
			} else {
				resetPassUrl = userService.getResetPasswordUrl(currentUser, user);
				result = true;
			}
			response.setSuccess(result);
			if (result) {
				if (resetPassUrl.isEmpty()) {
					response.setTitle(i18n.getMessage("admin.cruds.user.password_reset.ok.title"));
					response.setMessage(i18n.getMessage("web.controller.password_reset.mailSent", user.getMail()));
				} else {
					response.addContent("url", resetPassUrl);
				}
			} else {
				response.setTitle(i18n.getMessage("admin.cruds.user.password_reset.error.title"));
				response.setMessage(i18n.getMessage("web.controller.password_reset.mailNotSent"));
			}
		} else {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("admin.cruds.user.password_reset.error.title"));
			response.setMessage(i18n.getMessage("web.controller.password_reset.invalidId"));
		}
		return response;
	}

}
