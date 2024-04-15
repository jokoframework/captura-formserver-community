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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionRoleService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
@Api(value = "transitions", description = "Operations to define workflow Transitions")
public class TransitionEndpoint extends EndpointController{
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private ITransitionService transitionService;
	
	@Autowired
	private ITransitionRoleService transitionRoleService;
	
	@ApiOperation(value = "Returns the list of transitions of a given form")
	@RequestMapping(value = "/workflow/transitions", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, Object>> getTransitionsByForm(HttpServletRequest request,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		FormDTO formDto = new FormDTO();
		formDto.setId(formId);
		formDto.setVersion(version);
		
		List<Transition> transitions = transitionService.listTransitionsForUser(user, formDto);
		
		ArrayList<Map<String, Object>> transitionList = new ArrayList<Map<String, Object>>();
		for (Transition transition : transitions) {
			Map<String, Object> transitionMap = new HashMap<String, Object>();
			transitionMap.put("id", transition.getId());
			transitionMap.put("description", transition.getDescription());
			if (transition.getOriginState() != null) {
				transition.getOriginState().setStateRoles(null);
			}
			transitionMap.put("originState", transition.getOriginState());
			transition.getTargetState().setStateRoles(null);
			transitionMap.put("targetState", transition.getTargetState());
			transitionList.add(transitionMap);
		}

		return transitionList;
	}
	
	@ApiOperation(value = "Create a new transition to form")
	@RequestMapping(value = "/workflow/transitions", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<Transition> createTransition(HttpServletRequest request, 
			@RequestParam(value = "formId") Long formId,
			@RequestParam(value = "description", required = false) String description, @RequestParam(value = "originState", required = false) Long originStateId,
			@RequestParam(value = "targetState") Long targetStateId) {
		JsonResponse<Transition> jsonResponse = new JsonResponse<Transition>();
		SessionManager sessionManager = new SessionManager(request);
		Application app = sessionManager.getApplication();
		User user = sessionManager.getUser();
		
		State originState = null;
		if (originStateId != null) {
			originState = stateService.findById(originStateId);
		}
		State targetState = stateService.findById(targetStateId);
		Transition newTransition = new Transition(formId, description, originState, targetState);
		Transition savedTransition = transitionService.saveTransition(app, user, newTransition);
		savedTransition.setTransitionRoles(null);
		if (savedTransition.getOriginState() != null) {
			savedTransition.getOriginState().setStateRoles(null);
		}
		savedTransition.getTargetState().setStateRoles(null);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(savedTransition);
		
		return jsonResponse;
	}
	
	@ApiOperation(value = "Deletes a transition")
	@RequestMapping(value = "/workflow/transitions/{transitionId}", method = RequestMethod.DELETE)
	public @ResponseBody JsonResponse<Object> deleteTransition(HttpServletRequest request, @PathVariable(value = "transitionId") Long transitionId) {
		SessionManager sessionManager = new SessionManager(request);
		Application app = sessionManager.getApplication();
		User user = sessionManager.getUser();
		
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		Transition deletedTransition = transitionService.deleteTransition(app, user, transitionId);
		deletedTransition.setTransitionRoles(null);
		if (deletedTransition.getOriginState() != null) {
			deletedTransition.getOriginState().setStateRoles(null);
		}
		deletedTransition.getTargetState().setStateRoles(null);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(deletedTransition);
		
		return jsonResponse;
	}
	
	@ApiOperation(value = "Edit a transition")
	@RequestMapping(value = "/workflow/transitions", method = RequestMethod.PUT)
	public @ResponseBody JsonResponse<Object> editTransition(HttpServletRequest request,
			@RequestParam(value = "id") Long transitionId,
			@RequestParam(value = "formId") Long formId,
			@RequestParam(value = "description", required = false) String description, @RequestParam(value = "originState", required = false) Long originStateId,
			@RequestParam(value = "targetState") Long targetStateId) {
		SessionManager sessionManager = new SessionManager(request);
		Application app = sessionManager.getApplication();
		User user = sessionManager.getUser();
		
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		State originState = null;
		if (originStateId != null) {
			originState = stateService.findById(originStateId);
		}
		State targetState = stateService.findById(targetStateId);
		Transition editTransition = new Transition(formId, description, originState, targetState);
		editTransition.setId(transitionId);
		Transition editedTransition = transitionService.editTransition(app, user, editTransition);
		editedTransition.setTransitionRoles(null);
		if (editedTransition.getOriginState() != null) {
			editedTransition.getOriginState().setStateRoles(null);
		}
		editedTransition.getTargetState().setStateRoles(null);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(editedTransition);
		
		return jsonResponse;
	}
	
	static class TransitionRolesRequest {

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
	
	@ApiOperation(value = "Assign/unassign roles to/from a transition")
	@RequestMapping(value = "/workflow/transitions/{transitionId}/roles", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<String> mapTransitionToRoles(HttpServletRequest request, @PathVariable(value = "transitionId") Long transitionId,
			@ApiParam(value = "Pass 'assign': true for assign roles or 'assign': false for unassign. Pass 'rolesId' as a list")
			@RequestBody TransitionRolesRequest assignAndRolesId) {
		SessionManager sessionManager = new SessionManager(request);
		Application app = sessionManager.getApplication();
		User user = sessionManager.getUser();
		
		JsonResponse<String> jsonResponse = new JsonResponse<String>();
		Transition assignRolesSuccess;
		List<Long> rolesId = assignAndRolesId.getRolesId();
		Boolean assign = assignAndRolesId.getAssign();
		
		if (assign) {
			assignRolesSuccess = transitionRoleService.assignRoles(app, user, transitionId, rolesId);
		} else {
			assignRolesSuccess = transitionRoleService.unassignRoles(app, user, transitionId, rolesId);
		}
		jsonResponse.setSuccess(assignRolesSuccess != null);
		
		return jsonResponse;
		
	}

	@ApiOperation(value = "Return a list of roles of a given transition")
	@RequestMapping(value = "/workflow/transitions/{transitionId}/roles", method = RequestMethod.GET)
	public @ResponseBody JsonResponse<Object> getRolesByTransition(@PathVariable(value = "transitionId") Long transitionId) {
		JsonResponse<Object> jsonResponse = new JsonResponse<Object>();
		List<Long> listRolesId = transitionRoleService.listRoles(transitionId);
		jsonResponse.setSuccess(true);
		jsonResponse.setObj(listRolesId);
		
		return jsonResponse;
		
	}
	
	@ApiOperation(value = "Returns the list of transitions by an origin state")
	@RequestMapping(value = "/workflow/transitions/{originId}", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, Object>> getTransitionsByOriginState(HttpServletRequest request,
			@PathVariable(value = "originId") Long originId,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		FormDTO formDto = new FormDTO();
		formDto.setId(formId);
		formDto.setVersion(version);
		List<Transition> transitions = transitionService.listTransitionsByOriginState(user, formDto, originId);
		ArrayList<Map<String, Object>> transitionList = new ArrayList<Map<String, Object>>();
		for (Transition transition : transitions) {
			Map<String, Object> transitionMap = new HashMap<String, Object>();
			transitionMap.put("id", transition.getId());
			transitionMap.put("description", transition.getDescription());
			if (transition.getOriginState() != null) {
				transition.getOriginState().setStateRoles(null);
			}
			transitionMap.put("originState", transition.getOriginState());
			transition.getTargetState().setStateRoles(null);
			transitionMap.put("targetState", transition.getTargetState());
			transitionList.add(transitionMap);
		}

		return transitionList;
	}
}
