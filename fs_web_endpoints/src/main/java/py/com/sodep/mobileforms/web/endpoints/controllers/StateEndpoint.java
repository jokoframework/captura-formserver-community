package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.services.workflow.IStateRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
@Api(value = "states", description = "Operations to define workflow States")
public class StateEndpoint extends EndpointController{
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private IStateRoleService stateRoleService;
	
	@ApiOperation(value = "Returns the list of states of a given form")
	@RequestMapping(value = "/workflow/states", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, Object>> getStatesByForm(HttpServletRequest request,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		
		FormDTO formDto = new FormDTO();
		formDto.setId(formId);
		formDto.setVersion(version);
		
		List<State> states = stateService
				.listStatesForUser(user,formDto);
		
		ArrayList<Map<String, Object>> stateList = new ArrayList<Map<String, Object>>();
		for (State state : states) {
			Map<String, Object> stateMap = new HashMap<String, Object>();
			stateMap.put("id", state.getId());
			stateMap.put("name", state.getName());
			stateMap.put("description", state.getDescription());
			stateMap.put("initial", state.getInitial());
			stateList.add(stateMap);
		}

		return stateList;
	}
	
	@ApiOperation(value = "Create a new state to form")
	@RequestMapping(value = "/workflow/states", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<StateDTO> createState(HttpServletRequest request, @RequestBody StateDTO newState) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		Application app = sessionManager.getApplication();
		
		JsonResponse<StateDTO> jsonResponse = new JsonResponse<>();
		StateDTO savedState = stateService.saveState(app, user, newState);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(savedState);
		
		return jsonResponse;
	}
	
	@ApiOperation(value = "Deletes a state")
	@RequestMapping(value = "/workflow/states/{stateId}", method = RequestMethod.DELETE)
	public @ResponseBody JsonResponse<Object> deleteState(HttpServletRequest request, @PathVariable(value = "stateId") Long stateId) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		Application app = sessionManager.getApplication();
		
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		State deletedState = stateService.deleteState(app, user, stateId);
		deletedState.setStateRoles(null);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(deletedState);
		
		return jsonResponse;
		
	}
	
	@ApiOperation(value = "Edit a state")
	@RequestMapping(value = "/workflow/states", method = RequestMethod.PUT)
	public @ResponseBody JsonResponse<Object> editState(HttpServletRequest request, @RequestBody State editState) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		Application app = sessionManager.getApplication();
		
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		State editedState = stateService.editState(app, user, editState);
		editedState.setStateRoles(null);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(editedState);
		
		return jsonResponse;
		
	}
	
	static class StateRolesRequest {

		private Boolean assign;
		
		private List<Long> rolesId;

		public Boolean getAssign() {
			return assign;
		}

		public void setAssign(Boolean assign) {
			this.assign = assign;
		}
		
		public List<Long> getRolesId() {
			return rolesId;
		}

		public void setRolesId(List<Long> rolesId) {
			this.rolesId = rolesId;
		}

	}
	
	@ApiOperation(value = "Assign/unassign roles to/from a state")
	@RequestMapping(value = "/workflow/states/{stateId}/roles", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<String> mapStateToRoles(HttpServletRequest request, @PathVariable(value = "stateId") Long stateId,
			@ApiParam(value = "Pass 'assign': true for assign roles or 'assign': false for unassign. Pass 'rolesId' as a list") 
			@RequestBody StateRolesRequest assignAndRolesId) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		Application app = sessionManager.getApplication();
		
		JsonResponse<String> jsonResponse = new JsonResponse<String>();
		boolean assignRolesSuccess = false;
		List<Long> rolesId =  assignAndRolesId.getRolesId();
		Boolean assign = assignAndRolesId.getAssign();
		if (assign) {
			assignRolesSuccess = stateRoleService.assignRoles(app, user, stateId, rolesId);
		} else {
			assignRolesSuccess = stateRoleService.unassignRoles(app, user, stateId, rolesId);
		}
		jsonResponse.setSuccess(assignRolesSuccess);
		
		return jsonResponse;
		
	}
	
	@ApiOperation(value = "Return a list of roles of a given state")
	@RequestMapping(value = "/workflow/states/{stateId}/roles", method = RequestMethod.GET)
	public @ResponseBody JsonResponse<Object> getRolesByState(@PathVariable(value = "stateId") Long stateId) {
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		List<Long> listRolesId = stateRoleService.listRoles(stateId);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(listRolesId);
		
		return jsonResponse;
		
	}

}
