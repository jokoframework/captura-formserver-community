package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.WorkflowHistoryDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowHistoryService;

@Service
public class WorkflowHistoryServiceImpl implements IWorkflowHistoryService {


	private IDataAccessService dataAccessService;

	@Autowired
	public WorkflowHistoryServiceImpl(IDataAccessService dataAccessService) {
		this.dataAccessService = dataAccessService;
	}
	
	@Override
	public MFOperationResult save(User user, WorkflowHistoryDTO history) {
		Map<String, Object> workflowHistoryData = getWorkflowHistoryData(user, history);
		
		// 1. Guardar los datos en mongo
		return dataAccessService.storeWorkflowHistoryData(workflowHistoryData);
	}

	private Map<String, Object> getWorkflowHistoryData(User user, WorkflowHistoryDTO history) {
		Map<String, Object> workflowHistoryData = new HashMap<String, Object>();
		Long userId = user.getId();
		
		if (history.getOldStateId() != null) {
			workflowHistoryData.put("oldStateId", history.getOldStateId());
		}
		workflowHistoryData.put("newStateId", history.getNewStateId());
		workflowHistoryData.put("updatedAt", new Date());
		workflowHistoryData.put("changedBy", userId);
		workflowHistoryData.put("docId", history.getDocId());
		workflowHistoryData.put("formId", history.getFormId());
		workflowHistoryData.put("formVersion", history.getFormVersion());
		workflowHistoryData.put("comment", history.getComment());
		
		
		return workflowHistoryData;
	}


	@Override
	public List<Map<String, Object>> listWorkflowHistoryBy(Long stateId) {

		return dataAccessService.listDataByStateId(stateId);
	}

	@Override
	public List<Map<String, Object>> listWorkflowHistory(Long docId, Long formId, Long formVersion) {

		return dataAccessService.listDataByDocIdAndFormData(docId, formId, formVersion);
	}

}
