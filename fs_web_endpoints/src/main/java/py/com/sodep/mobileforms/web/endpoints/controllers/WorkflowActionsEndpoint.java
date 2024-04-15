package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.InvalidTransitionException;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowActionService;
import py.com.sodep.mobileforms.web.endpoints.json.WorkflowActionRequest;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
@Api(value = "workflow-actions", description = "Operations to update states within a Workflow", position = 6)
public class WorkflowActionsEndpoint extends EndpointController {

	private IWorkflowActionService workflowActionService;

	@Autowired
	public WorkflowActionsEndpoint(IWorkflowActionService workflowService) {
		this.workflowActionService = workflowService;
	}
	
	@ApiOperation(value = "Changes a document state")
	@RequestMapping(value = "/workflow/documents/{docId}/states/{targetState}", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<String> changeState(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable(value = "docId") Long docId,
			@PathVariable(value = "targetState") Long targetState,
			@ApiParam(value = "Information about the document and the new desired state") @RequestBody WorkflowActionRequest requestParam) throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		
		try {
			StoreResult insertSummary = changeDocState(docId, targetState,
					requestParam, user);
			JsonResponse<Object> jsonObj = toJsonResponse(insertSummary.getMfOperationResult(), insertSummary.getLastStoredRowId());
			
			if (insertSummary.hasSucceeded()) {
				sendObject(response, jsonObj, HttpServletResponse.SC_CREATED);
			} else {
				sendObject(response, jsonObj, HttpServletResponse.SC_CONFLICT);
			}
		} catch (InvalidTransitionException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		}
		return null;
	}

	private StoreResult changeDocState(Long docId, Long targetState,
			WorkflowActionRequest requestParam, User user) {
		DocumentDTO documentDto = buildDocumentFromRequest(docId, targetState, requestParam);
		StateDTO stateDto = buildStateFromRequest(targetState, requestParam);
		StoreResult insertSummary = workflowActionService.changeState(user, documentDto, stateDto);
		return insertSummary;
	}


	public static JsonResponse<Object> toJsonResponse(MFOperationResult mfOperationResult, Long lastStoredRowId) {
		JsonResponse <Object> json = new JsonResponse<>();
		json.setSuccess(mfOperationResult.hasSucceeded());
		json.setMessage(mfOperationResult.getMsg());
		json.addContent("docId", lastStoredRowId);
		json.addContent("numberOfAffectedRows", mfOperationResult.getNumberOfAffectedRows());
		return json;
	}

	private StateDTO buildStateFromRequest(Long targetState, WorkflowActionRequest requestParam) {
		return new StateDTO(targetState, requestParam.getWorkflowComment());
	}


	private DocumentDTO buildDocumentFromRequest(Long docId, Long targetState, WorkflowActionRequest requestParam) {
		FormDTO formDto = new FormDTO();
		formDto.setId(requestParam.getFormId());
		formDto.setVersion(requestParam.getFormVersion());
		return DocumentDTO.builder()
				.state(new StateDTO(targetState))
				.form(formDto)
				.docId(docId)
				.build();
	}
	
	@ApiOperation(value = "Change state of multiple documents")
	@RequestMapping(value = "/workflow/documents/states/{targetState}", method = RequestMethod.POST)
	public @ResponseBody JsonResponse<Object> changeMultipleState(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable(value = "targetState") Long targetState,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "formVersion", required = true) Long formVersion,
			@RequestParam(value = "comment", required = false, defaultValue="") String comment,
			@ApiParam(value = "Document list ids") @RequestParam(value = "docIds[]", required = true) List<Long> docIds) throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		WorkflowActionRequest wAR;
		
		wAR = new WorkflowActionRequest();
		wAR.setFormId(formId);
		wAR.setFormVersion(formVersion);
		wAR.setWorkflowComment(comment);
		
		try {
			StoreResult insertSummary = changeDocsState(docIds, targetState, wAR, user);
			JsonResponse<Object> jsonObj = toJsonResponse(insertSummary.getMfOperationResult(), insertSummary.getLastStoredRowId());
			
			if (insertSummary.hasSucceeded()) {
				sendObject(response, jsonObj, HttpServletResponse.SC_CREATED);
			} else {
				sendObject(response, jsonObj, HttpServletResponse.SC_CONFLICT);
			}
		} catch (InvalidTransitionException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		}
		
		return null;
	}
	
	private StoreResult changeDocsState(List<Long> docIds, Long targetState,
			WorkflowActionRequest requestParam, User user) {
		DocumentDTO documentDto = buildDocumentsFromRequest(docIds, targetState, requestParam);
		StateDTO stateDto = buildStateFromRequest(targetState, requestParam);
		StoreResult insertSummary = workflowActionService.changeStateForMultipleDocs(user, documentDto, stateDto);
		return insertSummary;
	}
	
	private DocumentDTO buildDocumentsFromRequest(List<Long> docIds, Long targetState, WorkflowActionRequest requestParam) {
		FormDTO formDto = new FormDTO();
		formDto.setId(requestParam.getFormId());
		formDto.setVersion(requestParam.getFormVersion());
		return DocumentDTO.builder()
				.state(new StateDTO(targetState))
				.form(formDto)
				.docIds(docIds)
				.build();
	}

}
