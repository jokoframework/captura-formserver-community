package py.com.sodep.mobileforms.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {

	@RequestMapping("loadTest.mob")
	public ModelAndView loadTest(){
		ModelAndView mav = new ModelAndView("/test.ftl");
		return mav;
	}
	
	@RequestMapping("/test/users.mob")
	public ModelAndView loadTestUsers(){
		ModelAndView mav = new ModelAndView("/test-user.ftl");
		return mav;
	}
	
	@RequestMapping("/test/roles.mob")
	public ModelAndView loadTestRoles(){
		ModelAndView mav = new ModelAndView("/test-roles.ftl");
		return mav;
	}
}
