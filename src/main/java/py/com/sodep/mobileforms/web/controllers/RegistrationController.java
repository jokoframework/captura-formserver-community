package py.com.sodep.mobileforms.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import nl.captcha.Captcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.exceptions.TooManyApplicationsException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.control.ITokenService;
import py.com.sodep.mobileforms.api.services.control.TokenPurpose;
import py.com.sodep.mobileforms.api.services.mail.MailService;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class RegistrationController extends SodepController {

	@Autowired
	private IUserService userService;

	@Autowired
	private Validator validator;

	@Autowired
	private ISystemParametersBundle systemParams;

	@Autowired
	private ITokenService tokenService;

	@Autowired
	private IApplicationService appService;

	@Autowired
	private MailService mailService;

	public enum REGISTRATION_RESULT {
		NONE, REGISTERED, INACTIVE, ACTIVE
	};

	@RequestMapping("/registration/register.mob")
	public ModelAndView getRegister(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("/registration/register.ftl");
		Boolean registrationDisabled = isRegistrationDisabled(request);
		mav.addObject("registrationDisabled", registrationDisabled);
		return mav;
	}

	private Boolean isRegistrationDisabled(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		boolean open = manager.isOpen();
		if (open) {
			User user = manager.getUser();
			if (user != null) {
				if (user.isRootUser()) {
					return false;
				}
			}
		}

		Boolean registrationDisabled = systemParams.getBoolean(DBParameters.SYS_REGISTRATION_DISABLED);
		if (registrationDisabled == null) {
			registrationDisabled = false;
		}

		return registrationDisabled;
	}

	@RequestMapping(value = "/registration/register.ajax")
	public @ResponseBody
	JsonResponse<?> register(HttpServletRequest request, HttpServletResponse httpResponse,
			@RequestBody RegistrationRequest reqistrationRequest) {
		Boolean registrationDisabled = isRegistrationDisabled(request);
		JsonResponse<Object> response = new JsonResponse<Object>();
		I18nManager i18n = I18nManager.getI18n(request);
		if (registrationDisabled) {
			response.setSuccess(false);
			httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setMessage(i18n.getMessage("web.registration.disabled"));
			return response;
		}

		HashMap<String, Object> content = new HashMap<String, Object>();
		response.setContent(content);

		SessionManager sessionManager = new SessionManager(request);
		Captcha captcha = sessionManager.getCaptcha();
		if (!captcha.isCorrect(reqistrationRequest.getCaptchaValue())) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setUnescapedMessage(i18n.getMessage("web.registration.error.catpcha"));
			content.put("registrationResult", REGISTRATION_RESULT.NONE);
			return response;
		}
		UserDTO userDTO = reqistrationRequest.getUserDTO();

		Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

		if (violations.size() != 0) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setUnescapedMessage(i18n.getMessage("web.controller.register.invalid"));
			Map<String, String> messages = getValidationMessages(request, violations);
			content.put("messages", messages);
			content.put("registrationResult", REGISTRATION_RESULT.NONE);
			return response;
		}

		try {
			User user = userService.findByMail(userDTO.getMail());
			String message = null;
			String title = null;
			boolean success = true;

			Boolean sendActivationMailParam = systemParams
					.getBoolean(DBParameters.SEND_ACTIVATION_MAIL_AFTER_REGISTRATION);
			boolean sendActivationMail = sendActivationMailParam == null ? false : sendActivationMailParam;

			if (user == null) {
				user = addInactiveUser(userDTO);

				content.put("registrationResult", REGISTRATION_RESULT.REGISTERED);
				if (sendActivationMail) {
					// we send an activation mail to the user
					userService.queueActivationMail(null, null, user);
					title = i18n.getMessage("web.controller.register.userCreated");
					message = i18n.getMessage("web.controller.register.ok");
				} else {
					// the activation process is done by approval
					// #2839 requests to send a mail as a notification when a
					// user registers
					String notificationMail = systemParams.getStrValue(DBParameters.REGISTRATION_NOTIFICATION_MAIL);
					if (notificationMail != null) {
						sendNewRegistrationMail(notificationMail, i18n, user);
					}

					userService.createActivationToken(null, user);
					title = i18n.getMessage("web.controller.register.userCreated");
					message = i18n.getMessage("web.controller.register.awaiting_approval");
				}
			} else {
				if (user.getActive() != null && !user.getActive()) {
					title = i18n.getMessage("web.generic.error");
					if (sendActivationMail) {
						title = i18n.getMessage("web.controller.register.pendingActivation");
						message = i18n.getMessage("web.controller.register.confirmResend");
						content.put("registrationResult", REGISTRATION_RESULT.INACTIVE);
					} else {
						title = i18n.getMessage("web.controller.register.pendingApproval");
						message = i18n.getMessage("web.controller.register.awaiting_approval_again");
						content.put("registrationResult", REGISTRATION_RESULT.INACTIVE);
					}
				} else {
					success = false;
					title = i18n.getMessage("web.generic.information");
					message = i18n.getMessage("web.controller.register.alreadyActive", userDTO.getMail());
					content.put("registrationResult", REGISTRATION_RESULT.ACTIVE);
				}
			}

			content.put("mail", userDTO.getMail());
			response.setTitle(title);
			response.setSuccess(success);
			response.setUnescapedMessage(message);
		} catch (InvalidEntityException e) {
			response.setSuccess(false);
			if (e.getMessages() != null && !e.getMessages().isEmpty()) {
				content.put("messages", translateI18nMessages(request, e.getMessages()));
			}

			response.setUnescapedMessage(i18n.getMessage(e.getMessage()));
		}
		return response;
	}

	@RequestMapping("/registration/resend.ajax")
	public @ResponseBody
	JsonResponse<String> resend(HttpServletRequest request, @RequestParam("mail") String mail) {
		I18nManager i18n = I18nManager.getI18n(request);
		JsonResponse<String> response = new JsonResponse<String>();

		User user = userService.findByMail(mail);

		Boolean sendActivationMailParam = systemParams.getBoolean(DBParameters.SEND_ACTIVATION_MAIL_AFTER_REGISTRATION);
		boolean sendActivationMail = sendActivationMailParam == null ? false : sendActivationMailParam;

		String message = null;
		if (sendActivationMail && user != null && user.getActive() != null && !user.getActive()) {
			response.setSuccess(true);
			response.setTitle(i18n.getMessage("web.generic.ok"));
			userService.queueActivationMail(null, null, user);
			message = i18n.getMessage("web.controller.register.mailResent", mail);
		} else if (sendActivationMail) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			if (user == null) {
				message = i18n.getMessage("web.controller.register.noSuchUser");
			} else {
				message = i18n.getMessage("web.controller.register.alreadyActive", mail);
			}
		} else {
			// the activation process is done by approval and no mail should be
			// resend
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.generic.error"));
			message = i18n.getMessage("web.controller.register.by_approval_only");
		}

		response.setUnescapedMessage(message);
		return response;
	}

	/**
	 * Adds a new inactive User (active = false)
	 * 
	 * @param userDTO
	 * @return
	 */
	private User addInactiveUser(UserDTO userDTO) {
		User user = BeanUtils.createAndMapBean(userDTO, User.class);
		user.setActive(false);
		// FIXME get language from request
		user.setLanguage("en");
		// when a user registers an account, he or she is not yet bound to a
		// default app. OK?
		// Ye
		user = userService.addNewUser(null, null, user);
		return user;
	}

	@RequestMapping(value = "/public/activate/{mail}/{activationToken}")
	public ModelAndView activate(HttpServletRequest request, @PathVariable("mail") String mail,
			@PathVariable("activationToken") String activationToken) {
		I18nManager i18n = I18nManager.getI18n(request);
		ModelAndView mav = new ModelAndView("/registration/activate.ftl");
		User user = userService.findByMail(mail);
		Boolean showInvitationToLogin = true;
		if (user != null && !user.getActive()) {
			// the user exists and isn't active
			if (tokenService.isValid(user, activationToken, TokenPurpose.ACTIVATION)) {
				// there's a valid activation token for that user
				String appName = getInitialAppName(user);
				try {
					appService.initAppWithOwner(appName, user, user.getLanguage());
					userService.activateUser(user);
					tokenService.useToken(user, activationToken);
					mav.addObject("message", i18n.getMessage("web.controller.activation.activated"));
				} catch (TooManyApplicationsException e) {
					// the user can't activate the account because the server has reach its max application number
					mav.addObject("message", i18n.getMessage("web.controller.activation.tooManyApplications"));
					mav.addObject("hintOnError", i18n.getMessage("web.controller.activation.tooManyApplications.hint"));
					
					String notificationMail = systemParams.getStrValue(DBParameters.REGISTRATION_NOTIFICATION_MAIL);
					if (notificationMail != null) {
						sendRegistrationFailedTooManyApplications(notificationMail, i18n, user);
					}
					showInvitationToLogin = false;
				}
			} else {
				// the activation token is invalid
				mav.addObject("message", i18n.getMessage("web.controller.activation.invalid"));
			}

		} else if (user != null && user.getActive()) {
			// the user is already active
			tokenService.useToken(user, activationToken); // #2895
			mav.addObject("message", i18n.getMessage("web.controller.activation.isActive"));
			mav.addObject("loginURI", "/login/login.mob");
		} else {
			// no valid user
			showInvitationToLogin = false;
			mav.addObject("message", i18n.getMessage("web.controller.activation.invalid"));
			mav.addObject("hintOnError", i18n.getMessage("web.controller.activation.invalid.hint"));
		}
		mav.addObject("showInvitationToLogin", showInvitationToLogin);
		return mav;
	}

	private void sendRegistrationFailedTooManyApplications(String recipient, I18nManager i18n, User user) {
		String mailFrom = systemParams.getStrValue(DBParameters.SYSTEM_MAIL_ADDRESS);
		mailService.queueMail(
				mailFrom,
				recipient,
				i18n.getMessage("web.controller.register.notification.fail.tooMayApplications.title"),
				i18n.getMessage("web.controller.register.notification.fail.tooMayApplications.subject", user.getFirstName(),
						user.getLastName(), user.getMail()));
	}
	
	private void sendNewRegistrationMail(String recipient, I18nManager i18n, User user) {
		String mailFrom = systemParams.getStrValue(DBParameters.SYSTEM_MAIL_ADDRESS);
		mailService.queueMail(
				mailFrom,
				recipient,
				i18n.getMessage("web.controller.register.notification.subject"),
				i18n.getMessage("web.controller.register.notification.body", user.getFirstName(),
						user.getLastName(), user.getMail()));
	}

	private String getInitialAppName(User user) {
		int index = 0;
		String appName = user.getMail();
		while (appService.appExists(appName) && index < 100) {
			index++;
			appName = user.getMail() + "_" + index;
		}

		if (index == 100) {
			throw new RuntimeException("Unable to get a name for the App!");
		}

		return appName;
	}

}
