package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.FormQueryDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.StoreResult;

/**
 * Form operations for documents that have a workflow state.
 * 
 * @author rodrigovillalba
 *
 */
public interface IWorkflowDataService {

	/**
	 * Saves a new document with data provided with an instance of
	 * {@link DocumentDTO}
	 * 
	 * @param user
	 *            who has privileges to save a document
	 * @param documentDto
	 *            data to save
	 * @return
	 * @throws InterruptedException
	 */
	StoreResult save(User user, DocumentDTO documentDto) throws InterruptedException;

	
	/**
	 * Searches documents that are instance of a form and have a state.
	 * 
	 * @param user
	 *            who has privileges to search
	 * @param form
	 *            the from that documents searched should be instances of
	 * @param stateId
	 *            the desired state of the searched documents
	 * 
	 * @return
	 */
	List<MFManagedData> list(User user, FormDTO form, Long stateId);

	/**
	 * Searches documents with query parameters provided by an instance of
	 * {@link FormQueryDTO}.
	 * 
	 * @param user
	 *            who has privileges to search
	 * @param queryDto
	 *            the query parameters
	 * @return
	 */
	List<MFManagedData> listBy(User user, FormQueryDTO queryDto);


	// TODO
	/**
	 * 
	 * @param form
	 * @param docId
	 * @return
	 */
	DocumentDTO getDocById(FormDTO formDto, Long docId);

	// TODO
	/**
	 * 
	 * @param form
	 * @param docId
	 * @return
	 */
	DocumentDTO getDocByIdAndSettingDocIds(FormDTO formDto, Long docId, List<Long> docIds);
		
	// TODO
	/**
	 * 
	 * @param user
	 * @param form
	 * @param documentDto
	 * @param targetState
	 * @return
	 */
	MFOperationResult updateState(User user, FormDTO formDto, DocumentDTO documentDto, StateDTO targetState);

	// TODO
	/**
	 * 
	 * @param user
	 * @param dto
	 * @return
	 * @throws InterruptedException
	 */
	StoreResult initIn(User user, DocumentDTO dto) throws InterruptedException;


	/**
	 * If an application has the workflow feature enabled, returns
	 * <code>true</code> if the form has an initial state assigned.
	 * <p>
	 * Designed to be used only as mobile user when no authorizations were
	 * computed (i.e.
	 * {@link IAuthorizationControlService#computeUserAccess(User, py.com.sodep.mobileforms.api.entities.application.Application)
	 * computeUserAccess} was not called).
	 * <p>
	 * 
	 * @param user
	 * @param form
	 * @return
	 */
	boolean shouldSaveInWorkflow(User user, FormDTO form);

	/**
	 * If an application has the workflow feature enabled, returns
	 * <code>true</code> if the form has an initial state assigned.
	 * <p>
	 * Designed to be used only as web user when the authorizations for the user
	 * have already been computed (i.e.
	 * {@link IAuthorizationControlService#computeUserAccess(User, py.com.sodep.mobileforms.api.entities.application.Application)
	 * computeUserAccess} was called).
	 * <p>
	 * 
	 * @param user
	 * @param form
	 * @return
	 */
	boolean workflowEnabled(User user, FormDTO formDto);

	// TODO
	/**
	 * 
	 * @param user
	 * @param form
	 * @param documentDto
	 * @param targetState
	 * @return
	 */
	MFOperationResult updateStateForMultipleDocs(User user, FormDTO form,
			DocumentDTO documentDto, StateDTO targetState);
	
	
}
