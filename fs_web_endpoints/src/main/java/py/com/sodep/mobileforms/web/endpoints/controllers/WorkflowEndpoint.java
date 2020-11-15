package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowHistoryService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
@Api(value = "workflow", description = "Operations to define Workflow", position = 5)
public class WorkflowEndpoint extends EndpointController{
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
	private IWorkflowHistoryService historyService;
	
	@ApiOperation(value = "Assign authorizations to a role")
	@RequestMapping(value = "/workflow/{roleId}/saveAuths", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<String> addAuthorizationsToRole(HttpServletRequest request, @PathVariable(value = "roleId") Long roleId, 
			@ApiParam(value = "List of string names of authorizations") @RequestBody List<String> authsId) {
		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();
		JsonResponse<String> jsonResponse = new JsonResponse<String>();
		Role role = this.roleService.findById(roleId);
		roleService.setAuthorizations(roleId, authsId);
		jsonResponse.setSuccess(true);
		jsonResponse.setMessage(i18n.getMessage("admin.cruds.role.permission.saved.message", role.getName()));
		
		return jsonResponse;
	}
	
	@ApiOperation(value = "Returns the workflow history for a document")
	@RequestMapping(value = "/workflow/history/{docId}/{formId}/{formVersion}", method = RequestMethod.GET)
	public @ResponseBody
	List<WorkflowHistoryResponse> getWorkflowHistoryForADoc(HttpServletRequest request,
			@ApiParam(value = "Document '_id' as was stored in mongo") @PathVariable(value = "docId") Long docId,
			@ApiParam(value = "Form id") @PathVariable(value = "formId") Long formId,
			@ApiParam(value = "Current form version") @PathVariable(value = "formVersion") Long formVersion) {
		SessionManager sessionManager = new SessionManager(request);
		sessionManager.getI18nManager();
		sessionManager.getUser();
		List<Map<String, Object>> listWorkflowHistory = historyService.listWorkflowHistory(docId, formId, formVersion);
		
		return toReturnObject(listWorkflowHistory);
	}
	
	private List<WorkflowHistoryResponse> toReturnObject(
			List<Map<String, Object>> listWorkflowHistory) {
		List<WorkflowHistoryResponse> workflowHistoryResponseList = new ArrayList<WorkflowHistoryResponse>();
		for (int i=0; i < listWorkflowHistory.size(); i++) {
			Map<String, Object> workflowHistoryRow = listWorkflowHistory.get(i);
			
			WorkflowHistoryResponse workflowHistoryResponse = convertToWorkflowHistoryResponse(workflowHistoryRow);
			workflowHistoryResponseList.add(workflowHistoryResponse);
		}
		
		return workflowHistoryResponseList;
	}

	private WorkflowHistoryResponse convertToWorkflowHistoryResponse(
			Map<String, Object> workflowHistoryRow) {
		WorkflowHistoryResponse workflowHistoryResponse = new WorkflowHistoryResponse();
		Long oldStateId = (Long) workflowHistoryRow.get("oldStateId");
		String oldState = "";
		if (oldStateId != null) {
			oldState = stateService.findById(oldStateId).getName();
			workflowHistoryResponse.setOldState(oldState);
		} else {
			workflowHistoryResponse.setOldState(oldState);
		}
		Long newStateId = (Long) workflowHistoryRow.get("newStateId");
		String newState = stateService.findById(newStateId).getName();
		String updatedAt = MFDataHelper.serialize(workflowHistoryRow.get("updatedAt"));
		Long userId = (Long) workflowHistoryRow.get("changedBy");
		String changedBy = userService.findById(userId).getMail();
		Long doc = (Long) workflowHistoryRow.get("docId");
		String comment = (String) workflowHistoryRow.get("comment");
		workflowHistoryResponse.setNewState(newState);
		workflowHistoryResponse.setUpdatedAt(updatedAt);
		workflowHistoryResponse.setChangedBy(changedBy);
		workflowHistoryResponse.setDoc(doc);
		workflowHistoryResponse.setComment(comment);
		
		return workflowHistoryResponse;
	}

	static class WorkflowHistoryResponse {
		
		private String oldState;
		
		private String newState;
		
		private String updatedAt;
		
		private String changedBy;
		
		private Long doc;
		
		private String comment;

		public String getOldState() {
			return oldState;
		}

		public void setOldState(String oldState) {
			this.oldState = oldState;
		}

		public String getNewState() {
			return newState;
		}

		public void setNewState(String newState) {
			this.newState = newState;
		}

		public String getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(String updatedAt) {
			this.updatedAt = updatedAt;
		}

		public String getChangedBy() {
			return changedBy;
		}

		public void setChangedBy(String changedBy) {
			this.changedBy = changedBy;
		}

		public Long getDoc() {
			return doc;
		}

		public void setDoc(Long doc) {
			this.doc = doc;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}
}
