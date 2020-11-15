package py.com.sodep.mobileforms.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.ComputedAuthorizationDTO;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
/**
 * This is a controller that groups several request that the client (browser) will make after a sucessful login
 * @author danicricco
 *
 */
public class StartupController {

	@Autowired
	private IAuthorizationControlService authService;

	@RequestMapping("/auth/computedAuthorization.ajax")
	@ResponseBody
	public ComputedAuthorizationDTO obtainComputedAuthorization(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		ComputedAuthorizationDTO computedAuth = authService.obtainComputedAuth(manager.getApplication(),user);
		return computedAuth;
	}

	@RequestMapping("/auth/loadAppConfiguration.ajax")
	@ResponseBody
	public AppConfiguration appConfiguration(HttpServletRequest request) {
		SessionManager manager = new SessionManager(request);
		// If the launcher is null the "home".js" will start the first entry on
		// the menu
		AppConfiguration appConf = new AppConfiguration(manager.getApplication().getId(), null);
		return appConf;
	}
}
