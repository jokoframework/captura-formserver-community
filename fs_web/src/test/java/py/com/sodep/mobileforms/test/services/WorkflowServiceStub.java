package py.com.sodep.mobileforms.test.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.services.auth.DefaultRoles;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionRoleService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.impl.services.data.DataAccessHelper;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.common.WorkflowData;


@Component
@Transactional
public class WorkflowServiceStub {
	
	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private ITransitionService transitionService;
	
	@Autowired
	private IWorkflowDataService workflowService;
	
	@Autowired
	private ITransitionRoleService trnasitionRoleService;
	
	@Autowired
	private IRoleService roleService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IFormService formService;
	
	@Autowired
	private IDataAccessService dataAccessService;

	private User user;
	
	private Application app;

	public static WorkflowData workflowFactory = TestDataFactory.forWorkflow();
	
	
	public WorkflowServiceStub postInitialize(Application app, User user) {
		this.user = user;
		this.app = app;
		authControlService.assignRoleToEntity(app, user.getId(), DefaultRoles.ROLE_APP_OWNER.toString(),
				Authorization.LEVEL_APP, app.getId());
		return this;
	}
	

	public FormDTO newTestForm(Application app, User user) {
		FormDTO form = stub.createTestForm(app, user);
		
		stub.defineForm(form, user);
		
		// Definimos y guardamos lo
		// necesario para trabajar con Mongo
		stub.defineTestDataSet(form);
		
		return form;
	}
	
	public RoleDTO assignRole(Application testApp, User user, FormDTO formDto) {
		return assignRole(testApp, user, user, formDto);
	}
	
	public RoleDTO assignRole(Application testApp, User user, AuthorizableEntity groupOrUser, FormDTO formDto) {
		RoleDTO dto = newRole(testApp, user);
		// Asignamos el rol al usuario, para que tenga autorización sobre el form
		assignRole(groupOrUser, formDto, dto);
		
		return dto;
	}

	public RoleDTO newRole(Application testApp, User user) {
		RoleDTO dto = TestDataFactory.getRole();
		
		// Save a new Role
		Role role = roleService.createProjectRole(testApp, user, dto);
		
		// Asignamos autorizaciones (permisos)
		String[] auths = {AuthorizationNames.Form.READ_WORKFLOW, AuthorizationNames.Form.TRANSITION_WORKFLOW};
		for(String auth: auths) {
			roleService.addAuths(role.getId(), auth);
		}
		
		dto.setId(role.getId());
		return dto;
	}

	public void assignRole(AuthorizableEntity groupOrUser, FormDTO formDto, RoleDTO roleDto) {
		Form formEntity = formService.findById(formDto.getId());
		
		authControlService.assignProjectRoleToEntity(formEntity.getProject().getId(), Arrays.asList(roleDto.getId()), groupOrUser.getId());
	}


	public Transition newTransition(FormDTO form, RoleDTO role) {
		StateDTO originState = new StateDTO();
		originState.setFormId(form.getId());
		originState.setName("Origin State");
		StateDTO targetState = new StateDTO();
		targetState.setFormId(form.getId());
		targetState.setName("Target State");
		originState = stateService.saveState(app, user, originState);
		targetState = stateService.saveState(app, user, targetState);
		
		Transition t = new Transition();
		t.setFormId(form.getId());
		State originStateEntity = stateService.findById(originState.getId());
		t.setOriginState(originStateEntity);
		State targetStateEntity = stateService.findById(targetState.getId());
		t.setTargetState(targetStateEntity);
		

		t = transitionService.saveTransition(app, user, t);
		assignRoleToTransition(t, role);
		
		return t;
	}

	public Transition newInitialTransition(FormDTO form, RoleDTO role) {
		StateDTO targetState = new StateDTO();
		targetState.setFormId(form.getId());
		targetState.setName("Target State");
		targetState.setInitial(true);
		targetState = stateService.saveState(app, user, targetState);
		
		Transition t = new Transition();
		t.setFormId(form.getId());
		State targetStateEntity = stateService.findById(targetState.getId());
		
		t.setTargetState(targetStateEntity);
		
		t = transitionService.saveTransition(app, user, t);
		assignRoleToTransition(t, role);
		
		return t;
	}

	
	public Transition newSelfTransition(FormDTO form, RoleDTO role) {
		StateDTO targetState = new StateDTO();
		targetState.setFormId(form.getId());
		targetState.setName("Target State");
		targetState = stateService.saveState(app, user, targetState);
		
		Transition t = new Transition();
		t.setFormId(form.getId());
		
		State targetStateEntity = stateService.findById(targetState.getId());
		t.setOriginState(targetStateEntity);
		t.setTargetState(targetStateEntity);
		
		t = transitionService.saveTransition(app, user, t);
		assignRoleToTransition(t, role);
		
		return t;
	}
	
	
	public void saveBeforeQuery(User user, FormDTO form) throws InterruptedException {
		List<Map<String,Object>> dataList = stub.asSearchableDataList(form, workflowFactory.getData());
		DocumentDTO documentDto = DocumentDTO.builder()
				.form(form)
				.dataList(dataList)
				.metaData(workflowFactory.getMeta())
				.build();
		workflowService.save(user, documentDto);
	}
	

	public StoreResult save(User user, FormDTO formDto) throws InterruptedException {
		List<Map<String, Object>> dataList = stub.asSearchableDataList(formDto, workflowFactory.getData());
		DocumentDTO documentDto = DocumentDTO.builder()
				.form(formDto)
				.dataList(dataList)
				.build();
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		List<MFField> formFields = getFormFields(form);
		List<? extends MFIncomingDataI> rows = DataAccessHelper.newIncoming(user, documentDto, formFields);
		return dataAccessService.storeData(form.getDataSetDefinition(), form.getDatasetVersion(), rows , true, true);
	}
	
	public StoreResult saveInWorkflow(User user, FormDTO form, StateDTO stateDto)
			throws InterruptedException {
		List<Map<String, Object>> dataList = stub.asSearchableDataList(form, workflowFactory.getData());
		workflowFactory.getMeta().put(MFIncominDataWorkflow.META_FIELD_STATE_ID, stateDto.getId());
		workflowFactory.getMeta().put(MFIncominDataWorkflow.META_FIELD_COMMENT, stateDto.getComment());
		
		DocumentDTO documentDto = DocumentDTO.builder()
				.form(form)
				.dataList(dataList)
				.metaData(workflowFactory.getMeta())
				.build();
		return workflowService.save(user, documentDto);
	}




	private void assignRoleToTransition(Transition t, RoleDTO role) {
		trnasitionRoleService.assignRoles(app, user, t.getId(), Arrays.asList(role.getId()));
	}


	private List<MFField> getFormFields(Form form) {
		MFDataSetDefinitionMongo def = dataAccessService.getDataSetDefinition(form.getDataSetDefinition(),
				form.getDatasetVersion());
		return def.getFields();
	}

	public void unAssignWorkflowRole(User user, FormDTO form, RoleDTO role) {
		Long formId = form.getId();
		List<Long> roles = Arrays.asList(role.getId());
		authControlService.deleteRolesFromEntityOnProject(form.getProjectId(), roles, user.getId());
		authControlService.deleteRolesFromEntityOnForm(formId, roles, user.getId());
	}

	

}
