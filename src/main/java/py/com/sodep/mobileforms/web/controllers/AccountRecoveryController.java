package py.com.sodep.mobileforms.web.controllers;

import javax.servlet.http.HttpServletRequest;

import nl.captcha.Captcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class AccountRecoveryController {

	@Autowired
	private IUserService userService;

	@RequestMapping("/account/recovery.mob")
	public ModelAndView index(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/account/recovery.ftl");
		return mav;
	}

	@RequestMapping(value = "/account/recover.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<String> recover(HttpServletRequest request, @RequestParam String mail,
			@RequestParam("captcha") String captchaStr) {
		SessionManager mgr = new SessionManager(request);
		JsonResponse<String> response = new JsonResponse<String>();
		I18nManager i18n = I18nManager.getI18n(request);

		// Captcha challenge must be the first control on the request.
		// Otherwise, a bot can detect by brute force the registered users
		Captcha captcha = mgr.getCaptcha();
		if (!captcha.isCorrect(captchaStr)) {

			response.setSuccess(false);
			response.setUnescapedMessage(i18n.getMessage("web.registration.error.catpcha"));
			return response;
		}

		User user = userService.findByMail(mail);
		if (user != null) {

			if (user.getActive() != null && user.getActive()) {
				boolean queued = userService.queuePasswordSetupMail(null, null, user);
				response.setSuccess(queued);
				response.setObj("OK");
				response.setUnescapedMessage(i18n.getMessage("web.account.recover.mailSent", mail));
			} else {
				response.setSuccess(false);
				String url = request.getContextPath() + "/registration/register.mob";
				response.setObj("INACTIVE");
				response.setUnescapedMessage(i18n.getMessage("web.account.recover.inactiveAccount", url));
			}
		} else {
			response.setObj("INVALID");
			response.setSuccess(false);
			response.setUnescapedMessage(i18n.getMessage("web.account.recover.invalidUser", mail));
		}
		return response;
	}

}
