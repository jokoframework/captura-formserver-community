package py.com.sodep.mobileforms.web.controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Token;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.control.ITokenService;
import py.com.sodep.mobileforms.api.services.control.TokenPurpose;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.acme.ACMEUnescapedString;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class InvitationController extends SodepController {

	@Autowired
	private IUserService userService;

	@Autowired
	private ITokenService tokenService;

	@RequestMapping("/public/invitation/accept/{mail}/{invitationToken}")
	public ModelAndView acceptInvitation(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("mail") String mail, @PathVariable("invitationToken") String invitationToken)
			throws ServletException, IOException {
		I18nManager i18n = I18nManager.getI18n(request);
		ModelAndView mav = new ModelAndView("/registration/activate.ftl");
		User user = userService.findByMail(mail);

		boolean showInvitationToLogin = true;
		if (user != null) {
			String contextPath = request.getContextPath();
			if (tokenService.isValid(user, invitationToken, TokenPurpose.APP_INVITATION)) {
				Token token = tokenService.getToken(invitationToken);
				Application app = token.getApplication();
				// If the user is inactive, lets activate it
				if (user.getActive() != null && !user.getActive()) {
					user.setActive(true);
					userService.save(user);
				}
				boolean invitationAccepted = userService.acceptInvitation(mail, invitationToken);

				List<Application> applications = userService.listApplications(user);
				if (user.getApplication() == null) {
					// if the user doesn't have a defualt application then
					// choose one. At least he will have the application were he
					// was invited
					// This should fix #2898 . A user that was registered, never
					// activate his account and then was invited to an
					// application
					user.setApplication(applications.get(0));
					userService.save(user);
				}
				if (invitationAccepted) {
					if (user.getPassword() != null && user.getPassword().trim().isEmpty()) {
						mav.addObject("message",
								i18n.getMessage("web.controller.invitation.setPasswordMail", app.getName()));
						Calendar inst = Calendar.getInstance();
						inst.add(Calendar.HOUR, 1);
						Token passToken = tokenService.createNewToken(user, user, TokenPurpose.PASSWORD_RESET,
								inst.getTime());

						String passResetURL = "/api/public/password/reset/" + user.getMail() + "/"
								+ passToken.getToken();
						RequestDispatcher requestDispatcher = request.getRequestDispatcher(passResetURL);
						requestDispatcher.forward(request, response);
						return null;
					} else {

						if (applications.size() == 1) {
							// The user has accepted the invitation and only
							// belongs to this application
							// The user has a password so it must have been set
							// by the one who invited
							User granter = token.getGranter();
							String granterFullName = granter.getFirstName() + " " + granter.getLastName() + "("
									+ granter.getMail() + ")";
							mav.addObject(
									"message",
									new ACMEUnescapedString(i18n.getMessage(
											"web.controller.invitation.accepted_no_password", app.getName(),
											granterFullName)));
						} else {
							// The user belongs to several applications
							// So, the user must already know/have the password
							mav.addObject(
									"message",
									new ACMEUnescapedString(i18n.getMessage("web.controller.invitation.accepted",
											app.getName())));
						}
						mav.addObject("loginURI", contextPath + "/login/login.mob");
					}
				} else {
					// TODO check why the invitation wasn't accepted
					mav.addObject("message", i18n.getMessage("web.controller.invitation.notAccepted"));
				}

			} else {
				showInvitationToLogin = false;
				mav.addObject("message", i18n.getMessage("web.controller.invitation.token.invalid"));
			}
		}
		mav.addObject("showInvitationToLogin", showInvitationToLogin);
		return mav;
	}

	@RequestMapping(value = "/invitation/mail.ajax")
	public @ResponseBody
	JsonResponse<Object> mail(HttpServletRequest request, @RequestParam("id") Long userId) {
		SessionManager mgr = new SessionManager(request);
		User currentUser = mgr.getUser();
		Application app = mgr.getApplication();
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<Object> response = new JsonResponse<Object>();
		User user = userService.findById(userId);
		if (user != null) {
			boolean result = userService.queueInvitationMail(app, currentUser, user);
			response.setSuccess(result);
			if (result) {
				response.setTitle(i18n.getMessage("web.controller.invitation.sent.ok"));
				response.setMessage(i18n.getMessage("web.controller.invitation.mailSent", user.getMail()));
			} else {
				response.setTitle(i18n.getMessage("web.controller.invitation.sent.error"));
				response.setMessage(i18n.getMessage("web.controller.invitation.mailNotSent"));
			}
		} else {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.controller.invitation.sent.error"));
			response.setMessage(i18n.getMessage("web.controller.invitation.invalidId"));
		}
		return response;
	}

}
