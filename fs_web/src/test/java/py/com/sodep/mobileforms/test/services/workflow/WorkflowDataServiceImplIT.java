package py.com.sodep.mobileforms.test.services.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.COMMENT_VALUE;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.NAME_PROPERTY;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.NAME_VALUE;
import static py.com.sodep.mobileforms.test.services.WorkflowServiceStub.workflowFactory;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.FormQueryDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class WorkflowDataServiceImplIT {
	
	@Autowired
	private IWorkflowDataService workflowService;
	
	@Autowired
	private IDataAccessServiceMock dataAccessService;

	@Autowired
	private MockObjectsContainer stub;

	@Autowired
	private WorkflowServiceStubFactory stubFactory;
	
	@Autowired
	private IAuthorizationControlService authService;

	private WorkflowServiceStub workflowStub;

	private User user;

	private FormDTO form;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private RoleDTO role;

	private Application app;

	
	@Before
	public void setup() {
		dataAccessService.dropDatabase();
		app = stub.getTestApplication();
		user = stub.getTestApplicationOwner();
		workflowStub = stubFactory.get(app, user);
		form = workflowStub.newTestForm(app, user);
		role = workflowStub.assignRole(app, user, form);
	}
	
	@Test
	public void shouldSaveDocumentWithState() throws InterruptedException {
		
		DocumentDTO documentDto = DocumentDTO.builder()
				.form(form)
				.dataList(workflowFactory.getDataList())
				.metaData(workflowFactory.getMeta())
				.build();
		
		StoreResult result = workflowService.save(user, documentDto);
		
		assertNotNull("Save operation result was excpected to be NOT null", result);
		assertEquals(MFOperationResult.RESULT.SUCCESS, result.getResult());
		assertEquals(1, result.getNumberOfAffectedRows());	
	}
	
		
	@Test
	public void shouldListDocumentsWithState() throws InterruptedException {
		workflowStub.saveBeforeQuery(user, form);
		
		Long stateId = (Long) workflowFactory.getMeta().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		FormQueryDTO query = FormQueryDTO.builder()
				.formId(form.getId())
				.stateId(stateId)
				.version(form.getVersion())
				.build();
		
		List<MFManagedData> list = workflowService.list(user, query.getFormDto(), query.getStateId());
		
		assertNotNull("Listing documents with state should return not empty list", list);
		assertEquals(1, list.size());
	}

	@Test
	public void shouldNotListDocumentsWithNotExistingState() throws InterruptedException {
		workflowStub.saveBeforeQuery(user, form);
		
		FormQueryDTO query = FormQueryDTO.builder()
				.formId(form.getId())
				.stateId(Long.MAX_VALUE)
				.version(form.getVersion())
				.build();
		
		List<MFManagedData> list = workflowService.list(user, query.getFormDto(), query.getStateId());
		
		assertNotNull("Listing documents with state should return not empty list", list);
		assertEquals(0, list.size());
	}
	

	@Test
	public void shoultListDocumentsUsingFilter() throws InterruptedException {
		StateDTO stateDto = new StateDTO(TestDataFactory.STATE);
		workflowStub.saveInWorkflow(user, form, stateDto);
		
		ConditionalCriteria filterBy = stub.getFilterBy(form, NAME_PROPERTY, NAME_VALUE);
		FormQueryDTO query = FormQueryDTO.builder()
				.formId(form.getId())
				.stateId(stateDto.getId())
				.version(form.getVersion())
				.restrictions(filterBy)
				.build();
		
		List<MFManagedData> list = workflowService.listBy(user, query);
		
		assertNotNull("Listing documents with state should return not empty list", list);
		assertEquals(1, list.size());
	}
	
	@Test
	public void shoultListDocumentsByMetaComment() throws InterruptedException {
		StateDTO stateDto = new StateDTO(TestDataFactory.STATE, TestDataFactory.COMMENT_VALUE);
		workflowStub.saveInWorkflow(user, form, stateDto);
		ConditionalCriteria filterBy = stub.getMetaFilterBy(form, MFIncominDataWorkflow.META_FIELD_COMMENT, COMMENT_VALUE);
	
		FormQueryDTO query = FormQueryDTO.builder()
				.formId(form.getId())
				.stateId(stateDto.getId())
				.version(form.getVersion())
				.restrictions(filterBy)
				.build();
		
		List<MFManagedData> list = workflowService.listBy(user, query);
		
		assertNotNull("Listing documents with state should return not empty list", list);
		assertEquals(1, list.size());
	}
	
	@Test
	public void shouldNotListIfNotAllowed() throws InterruptedException {
		Long stateId = (Long) workflowFactory.getMeta().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		
		// Le quitamos el rol que le da
		// permisos de lectura en workflow
		// sobre el formulario
		workflowStub.unAssignWorkflowRole(user, form, role);
		
		AuthorizationException ex = new AuthorizationException(AuthorizationNames.Form.READ_WORKFLOW);
		thrown.expect(AuthorizationException.class);
		thrown.expectMessage(ex.getMessage());
		
		workflowService.list(user, form, stateId);
	}

	@Test
	public void shouldNotSaveIfNotAllowed() throws InterruptedException {
		DocumentDTO documentDto = DocumentDTO.builder()
				.form(form)
				.dataList(workflowFactory.getDataList())
				.metaData(workflowFactory.getMeta())
				.build();
		workflowStub.unAssignWorkflowRole(user, form, role);
		
		AuthorizationException ex = new AuthorizationException(AuthorizationNames.Form.TRANSITION_WORKFLOW);
		thrown.expect(AuthorizationException.class);
		thrown.expectMessage(ex.getMessage());
		
		workflowService.save(user, documentDto);
	}
	
	@Test
	public void shouldCheckSuccessfullyIfWorkflowIsEnabled() {
		// Habilita el workflow para la app
		app.setHasWorkflow(true);
		
		// asigna el formulario un estado inicial
		workflowStub.newInitialTransition(form, role);
		
		// Guarda en cache los features de esta app
		authService.computeUserAccess(user, app);
	
		assertTrue("Should return TRUE when application has workflow and form has initial state", workflowService.shouldSaveInWorkflow(user, form));
	}

	@Test
	public void shouldFailCheckingWorkflowFeatureIfFormDoesNotHaveIniitalState() {
		// Habilita el workflow para la app
		app.setHasWorkflow(true);
		
		// Guarda en cache los features de esta app
		authService.computeUserAccess(user, app);
	
		assertFalse("Should return FALSE when form does not have initial state", workflowService.shouldSaveInWorkflow(user, form));
	}

	
}
