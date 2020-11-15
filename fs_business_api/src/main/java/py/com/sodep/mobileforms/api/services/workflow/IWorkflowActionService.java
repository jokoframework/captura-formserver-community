package py.com.sodep.mobileforms.api.services.workflow;

import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.StoreResult;

public interface IWorkflowActionService {

	/**
	 * Changes a state of a document.
	 * 
	 * @param user
	 * @param documentDto the document to change state
	 * @param targetState the new state information
	 * @return
	 */
	public StoreResult changeState(User user, DocumentDTO documentDto, StateDTO targetState);

	/**
	 * Changes a state for one or more documents
	 * @param user
	 * @param documentDto
	 * @param stateDto
	 * @return
	 */
	public StoreResult changeStateForMultipleDocs(User user,
			DocumentDTO documentDto, StateDTO stateDto);
	
}
