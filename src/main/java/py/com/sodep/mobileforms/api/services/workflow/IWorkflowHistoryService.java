package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.WorkflowHistoryDTO;
import py.com.sodep.mobileforms.api.entities.core.User;

public interface IWorkflowHistoryService {
	
	/**
	 * Saves a workflow history entry for a given document
	 * 
	 * @param user
	 * 			who change document state
	 * @param workflowHistoryData
	 * 			workflow history registry data
	 * @return
	 */
	MFOperationResult save(User user,  WorkflowHistoryDTO history);
	
	List<Map<String, Object>> listWorkflowHistoryBy(Long stateId);


	List<Map<String, Object>> listWorkflowHistory(Long docId, Long formId, Long formVersion);

}
