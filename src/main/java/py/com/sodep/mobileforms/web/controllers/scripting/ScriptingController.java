package py.com.sodep.mobileforms.web.controllers.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.scripting.SodepScript;
import py.com.sodep.mobileforms.api.services.scripting.IScriptingService;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.json.JsonResponse;

@Controller
public class ScriptingController extends SodepController {

	private static Logger logger = LoggerFactory.getLogger(ScriptingController.class);

	@Autowired
	private IScriptingService scriptingService;

	@RequestMapping(value = "/scripting/scripting.mob", method = RequestMethod.GET)
	public ModelAndView get(HttpSession session, HttpServletRequest request) {
		User user = getUser(request);
		ModelAndView mav = new ModelAndView("/scripting/scripting.ftl");
		mav.addObject("script", "");
		mav.addObject("out", "");
		mav.addObject("tabIndex", 0);
		List<SodepScript> scripts = scriptingService.listScripts(user);

		mav.addObject("scripts", scripts);
		return mav;
	}

	@RequestMapping(value = "/scripting/execute_script.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<Object> executeScript(HttpSession session, HttpServletRequest request) {
		// User user = getUser(request);
		String script = request.getParameter("script");
		logger.debug("Execute Script:");
		logger.debug(script);
		String out = null;
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(true);
		try {
			out = scriptingService.executeScript(null, script);
		} catch (Exception e) {
			out = e.getMessage();
			if (out != null) {
				out = out + "\n" + StringUtils.getStackTraceAsString(e);
			}
		}
		response.addContent("script", script);
		response.addContent("out", out);
		response.addContent("tabIndex", 1);
		//List<SodepScript> scripts = scriptingService.listScripts(user);
		//result.addContent("scripts", scripts);

		return response;
	}

	@RequestMapping(value = "/scripting/save_script.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<List<SodepScript>> saveScript(HttpSession session, HttpServletRequest request) {
		User user = getUser(request);
		String script = request.getParameter("script");
		String scriptName = request.getParameter("scriptName");

		JsonResponse<List<SodepScript>> response = new JsonResponse<List<SodepScript>>();

		SodepScript saved = scriptingService.saveScript(user, script, scriptName);
		List<SodepScript> scripts = scriptingService.listScripts(user);
		if (scriptName == null || scriptName.trim().length() < 3 || script == null || script.trim().length() == 0) {
			response.setSuccess(false);
			response.setMessage("Nombre de Script o Script inválido/vacío");
			return response;
		}
		for (SodepScript s : scripts) {
			s.setUser(null);
		}
		response.setSuccess(true);

		response.addContent("scripts", scripts);
		response.setMessage("Se guardó el Script " + saved.getId());

		return response;
	}

	@RequestMapping(value = "/scripting/list_scripts.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<List<SodepScript>> listScripts(HttpSession session, HttpServletRequest request) {
		User user = getUser(request);
		JsonResponse<List<SodepScript>> response = new JsonResponse<List<SodepScript>>();

		List<SodepScript> scripts = scriptingService.listScripts(user);
		for (SodepScript s : scripts) {
			s.setUser(null);
		}
		response.setSuccess(true);
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("scripts", new ArrayList<Object>());

		return response;
	}

	@RequestMapping(value = "/scripting/get_script.ajax", method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<SodepScript> getScript(HttpSession session, HttpServletRequest request,
			@RequestParam(value = "id") Long id) {
		JsonResponse<SodepScript> response = new JsonResponse<SodepScript>();
		User user = getUser(request);

		SodepScript script = scriptingService.getScript(user, id);
		response.setSuccess(true);
		response.addContent("script", script);

		return response;
	}

}
