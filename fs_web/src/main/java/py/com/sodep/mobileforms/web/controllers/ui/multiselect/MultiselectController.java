package py.com.sodep.mobileforms.web.controllers.ui.multiselect;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectManager;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectActionRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

/**
 * My first idea behind this class is to make it a front controller.
 * 
 * The multiselect jQuery widget, requests the data to show by specifying the
 * necessary parameters, including its id.
 * 
 * We keep a list of the different multiselect ids mapped with their
 * corresponding service. This service has the implementation of how to retrieve
 * and persist data
 * 
 * 
 * @author Miguel
 * 
 */
@Controller
public class MultiselectController extends SodepController {

	@Autowired
	private IMultiselectManager manager;

	@RequestMapping(value = "/ui/multiselect/loadModel/{id}")
	public @ResponseBody
	JsonResponse<MultiselectModel> loadModel(HttpServletRequest request, @PathVariable("id") String id,
			@RequestBody ParamRequest params) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		JsonResponse<MultiselectModel> jsonResponse = new JsonResponse<MultiselectModel>();
		MultiselectModel model = manager.loadModel(id, i18n.getSelectedLanguage(), params.params);
		if (model != null) {
			jsonResponse.setSuccess(true); 
			jsonResponse.setObj(model);
		} else {
			jsonResponse.setSuccess(false);
		}
		return jsonResponse;
	}

	@RequestMapping(value = "/ui/multiselect/listItems/{id}")
	public @ResponseBody
	MultiselectControllerResponse listItems(HttpServletRequest httpRequest, @PathVariable("id") String id,
			@RequestBody MultiselectReadRequest multiselectRequest) {
		setRequiredServerSideParams(httpRequest, multiselectRequest);

		MultiselectServiceResponse serviceResponse = manager.listItems(id, multiselectRequest);
		MultiselectControllerResponse controllerResponse = new MultiselectControllerResponse();
		controllerResponse.setSuccess(serviceResponse.getSuccess());
		controllerResponse.setItems(serviceResponse.getItems());

		return controllerResponse;
	}

	@RequestMapping(value = "/ui/multiselect/action/{id}")
	public @ResponseBody
	MultiselectControllerResponse action(HttpServletRequest httpRequest, @PathVariable("id") String id,
			MultiselectActionRequest multiselectActionRequest) {
		setRequiredServerSideParams(httpRequest, multiselectActionRequest);

		MultiselectServiceResponse serviceResponse = manager.doAction(id, multiselectActionRequest);
		MultiselectControllerResponse controllerResponse = new MultiselectControllerResponse();
		controllerResponse.setSuccess(serviceResponse.getSuccess());
		controllerResponse.setItems(serviceResponse.getItems());

		return controllerResponse;
	}

	private void setRequiredServerSideParams(HttpServletRequest httpRequest, MultiselectReadRequest multiselectRequest) {
		SessionManager mgr = new SessionManager(httpRequest);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();

		multiselectRequest.setUser(user);
		multiselectRequest.setApplication(mgr.getApplication());
		multiselectRequest.setLanguage(i18n.getSelectedLanguage());
	}
	
	public static class ParamRequest {
		Map<String,String> params;

		public Map<String, String> getParams() {
			return params;
		}

		public void setParams(Map<String, String> params) {
			this.params = params;
		}
	}

}
