package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.data.WorkflowStoreResult;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowActionService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;

@Service
public class WorkflowActionServiceImpl implements IWorkflowActionService {

	private IWorkflowDataService workflowService;
	
	@Autowired
	public WorkflowActionServiceImpl(IWorkflowDataService workflowService) {
		this.workflowService = workflowService;
	}
	
	@Override
	public StoreResult changeState(User user, DocumentDTO documentDto, StateDTO targetState) {
		failFastFirst(user, documentDto, targetState);
		
		WorkflowStoreResult result = doChangeState(user, documentDto, targetState);
		
		return new StoreResult(result.getMfOperationResult(), result.getLastStoredRowId());
	}

	private void failFastFirst(User user, DocumentDTO documentDto, StateDTO targetState) {
		Object documentIdOrIds = documentDto.getDocId() != null ? documentDto.getDocId() : documentDto.getDocIds(); 
		Assert.notNull(user, "User argument is required");
		Assert.notNull(documentDto, "Document argument is required");
		Assert.notNull(targetState, "Target state argument is required");
		Assert.notNull(targetState.getId(), "Target state id argument is required");
		Assert.notNull(documentIdOrIds, "Document id(s) is required");
		Assert.isTrue(documentDto.getForm().getId().longValue() > 0, "Form id should exists and be greater than zero");
		Assert.isTrue(documentDto.getForm().getVersion().longValue() > 0, "Form version should exists and be greater than zero");
	}
	
	private WorkflowStoreResult doChangeState(User user, DocumentDTO documentDto, StateDTO targetState) {
		DocumentDTO current = workflowService.getDocById(documentDto.getForm(), documentDto.getDocId());
		
		MFOperationResult updateResult = workflowService.updateState(user, documentDto.getForm(), current, targetState);
		
		return new WorkflowStoreResult(updateResult, current.getDocId(), current.getState().getId(), targetState.getId());
	}

	@Override
	public StoreResult changeStateForMultipleDocs(User user,
			DocumentDTO documentDto, StateDTO targetState) {
		failFastFirst(user, documentDto, targetState);
		
		WorkflowStoreResult result = doChangeStateForMultiple(user, documentDto, targetState);
		
		return new StoreResult(result.getMfOperationResult(), result.getLastStoredRowId());
	}

	private WorkflowStoreResult doChangeStateForMultiple(User user,
			DocumentDTO documentDto, StateDTO targetState) {
		List<Long> docIds = documentDto.getDocIds();
		// CAP-439 Dentro de este metodo se setea el campo de los id de los documentos
		DocumentDTO lastDocument = workflowService.getDocByIdAndSettingDocIds(documentDto.getForm(), docIds.get(docIds.size() - 1), docIds);
		
		MFOperationResult updateResult = workflowService.updateStateForMultipleDocs(user, documentDto.getForm(), lastDocument, targetState);
		
		return new WorkflowStoreResult(updateResult, lastDocument.getDocId(), lastDocument.getState().getId(), targetState.getId());
	}

	
}
