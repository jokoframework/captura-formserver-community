package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.FormQueryDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.dtos.WorkflowHistoryDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.InvalidTransitionException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.workflow.IStateDataAccess;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowHistoryService;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.impl.ConditionalCriteriaBuilder;
import py.com.sodep.mobileforms.impl.services.data.DataAccessHelper;

@Service
@Transactional
public class WorkflowDataServiceImpl implements IWorkflowDataService {

	private IFormService formService;

	private IStateDataAccess stateDataAccess;

	private IDataAccessService dataAccessService;

	private IAuthorizationControlService authService;

	private ITransitionService transitionService;

	private IWorkflowHistoryService historyService;
	
	private IStateService stateService;


	@Autowired
	public WorkflowDataServiceImpl(IFormService formService, 
			IStateDataAccess stateDataAccess,
			IDataAccessService dataAccessService,
			IAuthorizationControlService authService,
			ITransitionService transitionService,
			IWorkflowHistoryService historyService,
			IStateService stateService) {
		super();
		this.formService = formService;
		this.stateDataAccess = stateDataAccess;
		this.dataAccessService = dataAccessService;
		this.authService = authService;
		this.transitionService = transitionService;
		this.historyService = historyService;
		this.stateService = stateService;
	}

	@Override
	public StoreResult save(User user, DocumentDTO documentDto) throws InterruptedException {
		failFastBeforSave(user, documentDto);
		Long stateId = getIdOrFail(documentDto);
		
		FormDTO formDto = documentDto.getForm();
		Form form = formService.getForm(documentDto.getForm().getId(), documentDto.getForm().getVersion());
		checkAccess(user, formDto, AuthorizationNames.Form.TRANSITION_WORKFLOW);
		
		List<MFIncomingDataI> rows = DataAccessHelper.newIncoming(user, documentDto, getFormFields(form));
		
		StoreResult result = stateDataAccess.storeData(form.getDataSetDefinition(), form.getDatasetVersion(), rows);
		if (result.hasSucceeded()) {
			saveHistory(user, formDto, result.getLastStoredRowId(), stateId);
		}
		
		return result;
	}

	@Override
	public DocumentDTO getDocById(FormDTO formDto, Long docId) {
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		
		MFManagedData row = this.dataAccessService.getRow(form.getDataSetDefinition(), form.getDatasetVersion(), docId);
		Long currentStateId = (Long) row.getMetaData().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		String comment = (String) row.getMetaData().get(MFIncominDataWorkflow.META_FIELD_COMMENT);
		StateDTO state = new StateDTO(currentStateId, comment);
		return DocumentDTO.builder()
				.docId(row.getRowId())
				.data(row.getUserData())
				.metaData(row.getMetaData())
				.state(state)
				.build();
	}
	
	
	@Override
	@Authorizable(value=AuthorizationNames.Form.READ_WORKFLOW, formParam=1)
	public List<MFManagedData> list(User user, FormDTO formDto, Long stateId) {
		failFastBeforeQuery(user, formDto, stateId);
		checkAccess(user, formDto, AuthorizationNames.Form.READ_WORKFLOW);
		
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		
		return stateDataAccess.listData(form.getDataSetDefinition(), form.getVersion(), stateId);
	}
	
	@Override
	public List<MFManagedData> listBy(User user, FormQueryDTO queryDto) {
		failFastBeforeQuery(user, queryDto.getFormDto(), queryDto.getStateId());
		Assert.notNull(queryDto.getRestrictions(), "Restrictions should not be null");
		checkAccess(user, queryDto.getFormDto(), AuthorizationNames.Form.READ_WORKFLOW);
		
		Form form = formService.getForm(queryDto.getFormId(), queryDto.getVersion());
		
		return stateDataAccess.listData(
				form.getDataSetDefinition(), 
				form.getDatasetVersion(), 
				queryDto.getStateId(), 
				queryDto.getRestrictions(), 
				queryDto.getOrderBy());
	}
	

	@Override
	public MFOperationResult updateState(User user, FormDTO formDto, DocumentDTO documentDto, StateDTO targetState) {
		
		verifyValidUpdate(user, formDto, documentDto.getState().getId(), targetState.getId());
		
		//DocumentDTO currentDoc = this.getDocById(formDto, documentDto.getDocId());
		//MFIncomingDataI incomingData = new MFIncominDataWorkflow(currentDoc.getData(), currentDoc.getMeta(), targetState.getId(), targetState.getComment());
		
		
		ConditionalCriteria selector = ConditionalCriteriaBuilder.get()
				.equals(MFStorable._ID, documentDto.getDocId())
				.build();
		
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		MFOperationResult result = dataAccessService.updateStateForMultiple(form.getDataSetDefinition(), form.getDatasetVersion(), targetState, selector);
		
		if (result.hasSucceeded()) {
			saveHistory(user, formDto, documentDto.getDocId(), documentDto.getState().getId(), targetState.getId(), targetState.getComment());
		}
		
		
		return result;
	}



	private void verifyValidUpdate(User user, FormDTO formDto, Long currentStateId, Long targetId) {
		if (!canChangeState(user, formDto, currentStateId, targetId)) {
			throw new InvalidTransitionException("User #" + user.getId() + ", is not allowed to transition from states #" + currentStateId + " to #" + targetId + ". Form #" + formDto.getId());
		}
		
		if (!isValidTransition(formDto, currentStateId, targetId)) {
			throw new InvalidTransitionException("Not valid transition from  states #" + currentStateId + " to #" + targetId);
		}
	}

	private boolean canChangeState(User user, FormDTO formDto, Long currentStateId, Long targetId) {
		return transitionService.canMakeIt(user, formDto, currentStateId, targetId);
	}

	private boolean isValidTransition(FormDTO formDto, Long originStateId, Long targetId) {
		return transitionService.isValid(formDto, originStateId, targetId);
	}	


	private List<MFField> getFormFields(Form form) {
		MFDataSetDefinitionMongo def = dataAccessService.getDataSetDefinition(form.getDataSetDefinition(),
				form.getDatasetVersion());
		return def.getFields();
	}

	private void failFastBeforeQuery(User user, FormDTO form, Long stateId) {
		Assert.notNull(user, "User argument is required");
		Assert.notNull(form, "Form argument is required");
		Assert.state(hasFormId(form.getId()), "Form id should exists and be greater than zero");
		Assert.state(hasVersion(form.getVersion()), "Form version should exists and be greater than zero");
		Assert.state(stateId > 0, "State id should be greater than zsero");
	}

	
	
	private void failFastBeforSave(User user, DocumentDTO documentDto) {
		Assert.notNull(user, "User argument is required");
		Assert.notNull(documentDto, "Document argument is required");
		Assert.state(hasFormId(documentDto.getForm().getId()), "Form id should exists and be greater than zero");
		Assert.state(hasVersion(documentDto.getForm().getVersion()), "Form version should exists and be greater than zero");
		Assert.notNull(documentDto.getDataList(), "Data is required");
		if (documentDto.getDataList().isEmpty()) {
			Assert.notEmpty(documentDto.getData(), "Data should not be empty");
		}
	}

	private boolean hasFormId(Long formId) {
		return formId.longValue() > 0L;
	}
	
	private boolean hasVersion(Long version) {
		return version.longValue() > 0L;
	}

	private void checkAccess(User user, FormDTO formDto, String auth) {
		
		if (!authService.hasFormLevelAccess(formDto.getId(), user, auth)) {
			throw new AuthorizationException(auth);
		}
	}


	private void saveHistory(User user, FormDTO formDto, Long docId, Long oldStateId, Long newtStateId, String comment) {
		WorkflowHistoryDTO history = WorkflowHistoryDTO.builder()
				.oldStateId(oldStateId)
				.newStateId(newtStateId)
				.changedBy(user.getId())
				.docId(docId)
				.formId(formDto.getId())
				.formVersion(formDto.getVersion())
				.comment(comment)
				.build();
		historyService.save(user, history);
	}
	
	private void saveHistory(User user, FormDTO formDto, Long docId, Long stateId) {
		WorkflowHistoryDTO history = WorkflowHistoryDTO.builder()
				.oldStateId(null)
				.newStateId(stateId)
				.changedBy(user.getId())
				.docId(docId)
				.formId(formDto.getId())
				.formVersion(formDto.getVersion())
				.comment("")
				.build();
		historyService.save(user, history);	
	}
	

	private Long getIdOrFail(DocumentDTO documentDto) {
		Map<String, Object> meta = documentDto.getMeta();
		Long stateId = null;
		
		if (meta.containsKey(MFIncominDataWorkflow.META_FIELD_STATE_ID)) {
			stateId = (Long) meta.get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		} else if (documentDto.getState() != null) {
			stateId = documentDto.getState().getId();
		}
		
		Assert.notNull(stateId, "Could not find state id parameter. It is required");
		return stateId;
	}

	@Override
	public StoreResult initIn(User user, DocumentDTO documentDto) throws InterruptedException {
		failFastBeforSave(user, documentDto);
		Long stateId = getIdOrFail(documentDto);
		
		FormDTO formDto = documentDto.getForm();
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		
		List<MFIncomingDataI> rows = DataAccessHelper.newIncoming(user, documentDto, getFormFields(form));
		
		StoreResult result = stateDataAccess.storeData(form.getDataSetDefinition(), form.getDatasetVersion(), rows);
		if (result.hasSucceeded()) {
			saveHistory(user, formDto, result.getLastStoredRowId(), stateId);
		}
		
		return result;
	}

	
	@Override
	public boolean shouldSaveInWorkflow(User user, FormDTO formDto) {
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		Application app = form.getProject().getApplication();
		// Solamente en este lugar tenemos que verificar dinamicamente el flag
		// de esta forma. 
		// En otros accesos se debe user authService.computeUserAccess
		// y authService.hasFeature.
		return app.getHasWorkflow() != null & app.getHasWorkflow() 
				&& stateService.hasInitialState(user, formDto); 
	}

	@Override
	public boolean workflowEnabled(User user, FormDTO formDto) {
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		Application app = form.getProject().getApplication();
		return authService.hasFeature(app, AuthorizationNames.Feature.WORKFLOW) 
				&& stateService.hasInitialState(user, formDto); 
	}

	@Override
	public DocumentDTO getDocByIdAndSettingDocIds(FormDTO formDto, Long docId, List<Long> docIds) {
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		
		MFManagedData row = this.dataAccessService.getRow(form.getDataSetDefinition(), form.getDatasetVersion(), docId);
		Long currentStateId = (Long) row.getMetaData().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		String comment = (String) row.getMetaData().get(MFIncominDataWorkflow.META_FIELD_COMMENT);
		StateDTO state = new StateDTO(currentStateId, comment);
		return DocumentDTO.builder()
				.docId(docId)
				.docIds(docIds)
				.data(row.getUserData())
				.metaData(row.getMetaData())
				.state(state)
				.build();
	}

	@Override
	public MFOperationResult updateStateForMultipleDocs(User user,
			FormDTO formDto, DocumentDTO documentDto, StateDTO targetState) {
		verifyValidUpdate(user, formDto, documentDto.getState().getId(), targetState.getId());
		
		ConditionalCriteria selector = ConditionalCriteriaBuilder.get()
				.in(MFStorable._ID, documentDto.getDocIds())
				.build();
		
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		MFOperationResult result = dataAccessService.updateStateForMultiple(form.getDataSetDefinition(), form.getDatasetVersion(), targetState, selector);
		
		if (result.hasSucceeded()) {
			for (Long docId : documentDto.getDocIds()) {
				saveHistory(user, formDto, docId, documentDto.getState().getId(), targetState.getId(), targetState.getComment());
			}	
		}
		
		
		return result;
	}
	
}
