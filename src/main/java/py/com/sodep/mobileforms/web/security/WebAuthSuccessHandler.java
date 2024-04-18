package py.com.sodep.mobileforms.web.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.log.LoginType;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.logging.IDBLogging;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.JsonSerializer;
import py.com.sodep.mobileforms.web.json.LoginInfoJSON;
import py.com.sodep.mobileforms.web.session.SessionManager;
import py.com.sodep.mobileforms.web.utils.RequestUtils;

/**
 * Strategy used to handle a successful user authentication. Implementations can
 * do whatever they want but typical behaviour would be to control the
 * navigation to the subsequent destination (using a redirect or a forward). For
 * example, after a user has logged in by submitting a login form, the
 * application needs to decide where they should be redirected to afterwards
 * (see AbstractAuthenticationProcessingFilter and subclasses). Other logic may
 * also be included if required.
 *
 */
public class WebAuthSuccessHandler extends
		SimpleUrlAuthenticationSuccessHandler {
	private static final Logger logger = Logger
			.getLogger(WebAuthSuccessHandler.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IApplicationService appService;
	
	@Autowired
	private IDBLogging dbLogging;

	private boolean jsonResponse;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		String mail = (String) authentication.getPrincipal();
		logger.debug("Logging successful for user: " + mail);
		SessionManager manager = new SessionManager(request);
		manager.start();

		AuthorizationAspect.shouldCheckAuthorization(false);
		User user = userService.findByMail(mail);
		manager.setUser(user);
		boolean isRoot = user.isRootUser();
		if (!isRoot) {
			// #198 #218 Bind Application Information to Session
			// User logins to his/hers default application
			List<Application> apps = userService.listActiveApplications(user);
			manager.setAvailableApplications(apps);

			Long applicationId = parseApplicationId(request);
			if (applicationId == null) {
				// #2673 & #3405
				Application app = getFirstActiveApplicationOrThrow(user, apps);
				manager.setApplication(app);
			} else {
				Application app = appService.findById(applicationId);
				if (app != null) {
					if (!appService.isActive(applicationId)) {
						throw new AuthorizationException(String.format(
								"The application #%d is not active",
								applicationId));
					}
					// #2673 & #3405
					if (userService.isMember(applicationId, user)) {
						manager.setApplication(app);
					} else {
						// TODO i18n
						// FIXME This will generate HTML error page. The request
						// may be expecting JSON
						throw new AuthorizationException(
								String.format(
										"The user %s is not a member of the application #%d",
										user.getMail(), applicationId));
					}
				} else {
					manager.invalidate();
					if (jsonResponse) {
						sendLoginFailedAsJson(response,
								"Invalid application id #" + applicationId);
						return;
					} else {
						throw new AuthorizationException(String.format(
								"The application #%d doesn't exist",
								applicationId));
					}
				}
			}
		}

		if (jsonResponse) {
			// log login
			sendLoginSuccessAsJson(request, response);
			Application app = isRoot ? null : manager.getApplication();
			
			logLogin(user, app, request);
			// We are calling this method because it is called within
			// super.onAuthenticationSuccess
			clearAuthenticationAttributes(request);
		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
		logger.debug("onAuthenticationSuccess took "
				+ (System.currentTimeMillis() - startTime) + " ms");
	}

	private void logLogin(User user, Application app, HttpServletRequest request) {
		String typeValue = RequestUtils.parseLoginType(request);
		dbLogging.logLogin(user.getId(), app == null ? -1L : app.getId(), LoginType.fromValue(typeValue));
	}

	private void sendLoginFailedAsJson(HttpServletResponse response,
			String message) {
		JsonResponse<Object> json = new JsonResponse<Object>();
		json.setMessage(message);
		json.setSuccess(false);
		JsonSerializer.writeJSON(response, json);
	}

	/**
	 * Returns the user's default application if it's active else, the first
	 * active app that's in the list
	 * 
	 * Throws an AuthorizationException if no active application is found
	 * 
	 * @param user
	 * @param apps
	 * @return
	 */
	private Application getFirstActiveApplicationOrThrow(User user,
			List<Application> apps) {
		Application application = user.getApplication();
		if (application.getActive() != null && application.getActive()) {
			return application;
		}
		if (apps != null) {
			for (Application app : apps) {
				if (app.getActive() != null && app.getActive()) {
					return app;
				}
			}
		}
		throw new AuthorizationException("No active application");
	}

	private Long parseApplicationId(HttpServletRequest request) {
		String parameter = request.getParameter("applicationId");
		if (parameter != null) {
			try {
				return Long.parseLong(parameter);
			} catch (NumberFormatException e) {
				logger.warn("Invalid application id in login " + parameter);
			}
		}
		return null;
	}

	private void sendLoginSuccessAsJson(HttpServletRequest request,
			HttpServletResponse response) {
		JsonResponse<LoginInfoJSON> json = new JsonResponse<LoginInfoJSON>();
		json.setSuccess(true);
		LoginInfoJSON loginInfo;
		// I18nManager i18n = I18nManager.getI18n(request);
		// JsonResponse<String> response = new JsonResponse<String>();
		// response.setMessage(i18n.getMessage("web.login.controller.loggedin"));
		// // FIXME same as login method. Make the home page a parameter
		// response.addContent("redirect", );
		// response.setSuccess(true);
		// return response;

		// FIXME What sense in executing the exact same code in the if and the
		// else?
		String deviceStr = request.getParameter("device");
		if (deviceStr != null && deviceStr.equals("true")) {
			// the else implementation is sending a 3xx response that the
			// device is automatically following. Therefore, the device
			// missed the chance to catch the session cookie
			// FIXME I'm planing to change the authentication mechanis of
			// the web interface and just send the result though ajax. It
			// doesn't make sense to make the redirect.
			// loginInfo = new LoginInfoJSON(request.getContextPath() +
			// "/home/home.mob");
			loginInfo = null;
		} else {
			// add customization for the web
			loginInfo = new LoginInfoJSON(request.getContextPath()
					+ "/home/home.mob");
		}

		json.setObj(loginInfo);
		JsonSerializer.writeJSON(response, json);
	}

	public void setJsonResponse(boolean jsonResponse) {
		this.jsonResponse = jsonResponse;
	}

}
