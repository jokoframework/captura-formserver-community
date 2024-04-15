package py.com.sodep.mobileforms.web.endpoints.controllers;

import static py.com.sodep.mobileforms.web.endpoints.controllers.MFTranslatorHelper.toMf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.objects.auth.MFAuthenticationRequest;
import py.com.sodep.mf.exchange.objects.auth.MFAuthenticationResponse;
import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.error.ErrorType;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.log.LoginType;
import py.com.sodep.mobileforms.api.exceptions.DeviceBlacklistedException;
import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.api.services.auth.IAuthenticationService;
import py.com.sodep.mobileforms.api.services.logging.IDBLogging;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.endpoints.exceptions.EndpointOperationException;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;
import py.com.sodep.mobileforms.web.utils.RequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@Api(value = "authentication", description = "Operations to authenticate a user and verify a device", position = 2)
public class AuthenticationEndpoint extends EndpointController {

	@Autowired
	private IAuthenticationService authService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private IApplicationService applicationService;
	
	@Autowired
	private IDBLogging dbLogging;


	/**
	 * The verification should be done after the login.
	 * 
	 * In the verification phase, the device sends information to identify
	 * itself.
	 * 
	 * @param request
	 * @param response
	 * @param device
	 */
	@ApiOperation(value = "Device verification")
	@RequestMapping(value = "/authentication/verification", method = RequestMethod.POST)
	public void deviceVerification(HttpServletRequest request,
			HttpServletResponse response, @RequestBody MFDevice device) {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();

		Long applicationId = device.getApplicationId();

		checkIfApplicationIsActive(user, applicationId);

		if (userService.isMember(applicationId, user)) {
			try {
				deviceService.associate(user, device);
				// set the application
				mgr.setApplication(applicationService.findById(applicationId));
			} catch (LicenseException ex) {
				throwLicenseException(ex);
			} catch (DeviceBlacklistedException ex) {
				throwDeviceBlacklisted(ex);
			}
		} else {
			throwNotMember();
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * Authenticate a user.
	 * 
	 * The id of the Captura Application to be authenticated to may be passed.
	 * If no applicationId is provided this method will return a list of
	 * possible applications (if the user is a valid user) but no session will
	 * be started until the application id is passed.
	 * 
	 * Another parameter is the application Key. Several clients may user the
	 * API, and every client, has its application Key
	 * 
	 * If the user doesn't belong to the application or the credentials are
	 * wrong a 401 (Unauthorized) response is sent.
	 * 
	 * If the Application Key is invalid a 403 (Forbidden) is sent. If the
	 * application is invalid (not found or deleted) a 403 is sent.
	 * 
	 * Human readable messages are sent as part of the response
	 * 
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "User authentication", notes = "Authenticate a user")
	@RequestMapping(value = "/authentication/login", method = RequestMethod.POST)
	public @ResponseBody
	MFAuthenticationResponse authenticate(HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody MFAuthenticationRequest authenticationRequest) {
		MFAuthenticationResponse authResponse = new MFAuthenticationResponse();
		String mail = authenticationRequest.getUser();

		if (mail == null) {
			throwMailIsNull();
		}

		String password = authenticationRequest.getPassword();

		if (password == null) {
			throwPasswordIsNull();
		}

        User user = userService.findByMail(mail);
		boolean checkCredentials = user != null && authService.checkCredentials(user, password);
        if (!checkCredentials) {
			throwInvalidUser();
		}
		authResponse.setSuccess(true);

		SessionManager manager = new SessionManager(request);

		Long applicationId = authenticationRequest.getApplicationId();
		if (applicationId != null) {
			Application app = appService.findById(applicationId);
			if (app != null) {
				if (!appService.isActive(applicationId)) {
					throwApplicationIsInactive();
				}
				if (userService.isMember(applicationId, user)) {
					manager.start();
					manager.setApplication(app);
					authResponse.setApplication(toMf(app));
				} else {
					throwNotMember();
				}
			} else {
				throwInvalidApp();
			}
		} else {
			authResponse.setPossibleApplications(getApps(user));
		}
		// Add user to http session
		manager.start();
		manager.setUser(user);
		// Add user to Spring security context
		UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
				user, password, getGrantedAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(userToken);

		// CAP-198
		logLogin(request, user, applicationId, authResponse.getPossibleApplications());
		
		return authResponse;
	}

	private void logLogin(HttpServletRequest request, User user,
			Long applicationId, List<py.com.sodep.mf.exchange.objects.metadata.Application> possibleApps) {
		Long appId = null;
		if (applicationId == null) {
			if (possibleApps != null && possibleApps.size() == 1) {
				py.com.sodep.mf.exchange.objects.metadata.Application app = (py.com.sodep.mf.exchange.objects.metadata.Application) possibleApps.get(0);
				if (app != null) {
					appId = app.getId();
				}
			}
		} else {
			appId = applicationId;
		}
		
		dbLogging.logLogin(user.getId(), appId, parseLoginType(request));
	}

	
	private LoginType parseLoginType(
			HttpServletRequest request) {
		String type = RequestUtils.parseLoginType(request);
		return LoginType.fromValue(type);
	}

	@ApiOperation(value = "Logout", notes = "End current session (if any)")
	@RequestMapping(value = "/authentication/logout", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<Object> logout(HttpServletRequest request,
			HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		JsonResponse<Object> jsonResponse = new JsonResponse<>();
		if (mgr.isOpen()) {
			mgr.invalidate();
			jsonResponse.setSuccess(true);
			return jsonResponse;
		}
		return jsonResponse;
	}

	private Collection<GrantedAuthority> getGrantedAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		Collection<GrantedAuthority> unmodifiableCollection = Collections
				.unmodifiableCollection(authorities);
		return unmodifiableCollection;
	}

	private void throwInvalidApp() {
		EndpointOperationException ex = new EndpointOperationException(true);
		ex.setErrorType(ErrorType.APP_INVALID);
		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
		ex.setKey("web.api.authorization.invalidApplication");
		throw ex;
	}

//	private void throwDeviceNotInApplication() {
//		EndpointOperationException ex = new EndpointOperationException(true);
//		ex.setErrorType(ErrorType.DEVICE_NOT_IN_APPLICATION);
//		ex.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
//		ex.setKey("web.api.authorization.deviceNotInApplication");
//		throw ex;
//	}

//	private void throwAppNotSelected() {
//		EndpointOperationException ex = new EndpointOperationException(true);
//		ex.setErrorType(ErrorType.APP_NOT_SELECTED);
//		ex.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
//		ex.setKey("web.api.authorization.invalidApplication");
//		throw ex;
//	}

	private void throwInvalidUser() {
		EndpointOperationException ex = new EndpointOperationException();
		ex.setErrorType(ErrorType.BAD_CREDENTIALS);
		ex.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
		ex.setKey("web.api.authentication.invalidUser");
		throw ex;
	}

	private void throwPasswordIsNull() {
		EndpointOperationException ex = new EndpointOperationException();
		ex.setErrorType(ErrorType.BAD_CREDENTIALS);
		ex.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
		ex.setKey("web.api.authentication.passwordIsNull");
		throw ex;
	}

	private void throwMailIsNull() {
		EndpointOperationException ex = new EndpointOperationException();
		ex.setErrorType(ErrorType.BAD_CREDENTIALS);
		ex.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
		ex.setKey("web.api.authentication.userIsNull");
		throw ex;
	}

	private List<py.com.sodep.mf.exchange.objects.metadata.Application> getApps(
			User user) {
		List<Application> activeApps = userService.listActiveApplications(user);
		List<py.com.sodep.mf.exchange.objects.metadata.Application> apps = new ArrayList<>();
		for (Application app : activeApps) {
			py.com.sodep.mf.exchange.objects.metadata.Application mf = toMf(app);
			apps.add(mf);
		}
		return apps;
	}
}
