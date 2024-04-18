package py.com.sodep.mobileforms.web.controllers;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class LoginController extends SodepController {
	
	@Autowired
	private ISystemParametersBundle systemParameter;

	@RequestMapping("/login/login.mob")
	public String getLogin(HttpServletRequest request, HttpServletResponse response) {
		User user = null;
		SessionManager mgr = new SessionManager(request);
		

		if (mgr != null) {
			user = mgr.getUser();
		}
		if (mgr != null && mgr.isOpen() && user != null) {
			/* The user is logged in :) */// What's funny about that?
			// Is not funny but I would be very happy if somebody finally uses
			// this ;)
			// XD
			return "forward:/home/home.mob";
		} else {
			return "/login/login.ftl";
		}
	}

	@RequestMapping("/login/logout.mob")
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {

		SessionManager sessionManager = new SessionManager(request);

		ModelAndView mav = null;
		User user = sessionManager.getUser();
		if (!user.isRootUser()) {
			sessionManager.invalidate();
			// Show logout FTL that has a javascript that "redirects" to the
			// "home page" of the application.
			mav = new ModelAndView("/login/logout.ftl");
		} else {
			Application app = sessionManager.getApplication();
			if (app == null) {
				sessionManager.invalidate();
				mav = new ModelAndView("/login/logout.ftl");
			} else {
				sessionManager.setApplication(null);
				mav = new ModelAndView("/admin/admin-home.ftl");
				String deployId = systemParameter.getStrValue(DBParameters.SYS_DEPLOY_ID);
				mav.addObject("deployId", deployId);
				int year = Calendar.getInstance().get(Calendar.YEAR);
				mav.addObject("year", year);
			}
		}
		return mav;
	}
}
