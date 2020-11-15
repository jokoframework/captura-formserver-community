package py.com.sodep.mobileforms.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class AcmeController {

	@Autowired
	private ISystemParametersBundle systemParametersBundle;

	@RequestMapping("/acme/vars.ajax")
	public @ResponseBody
	JsonResponse<Map<String, String>> language(HttpServletRequest request) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("contextPath", request.getContextPath());
		SessionManager mgr = new SessionManager(request);
		String language = null;
		int timeout = 0;
		if (mgr.isOpen()) {
			I18nManager i18n = mgr.getI18nManager();
			language = i18n.getSelectedLanguage();
			timeout = request.getSession().getMaxInactiveInterval() * 1000;
		} else {
			language = systemParametersBundle.getStrValue(DBParameters.LANGUAGE);
		}

		vars.put("language", language);
		vars.put("timeout", Integer.toString(timeout));

		JsonResponse<Map<String, String>> response = new JsonResponse<Map<String, String>>();
		response.setObj(vars);
		response.setSuccess(true);
		return response;
	}

	public static class DefaultValueRequest {

		private String forElement;

		private Map<String, String> params;

		public String getForElement() {
			return forElement;
		}

		public void setForElement(String forElement) {
			this.forElement = forElement;
		}

		public Map<String, String> getParams() {
			return params;
		}

		public void setParams(Map<String, String> params) {
			this.params = params;
		}

	}
}
