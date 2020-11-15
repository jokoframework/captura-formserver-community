package py.com.sodep.mobileforms.test.services.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.WorkflowHistoryDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowHistoryService;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class WorkflowHistoryServiceImplIT {

	@Autowired
	private IWorkflowHistoryService workflowService;
	
	@Autowired
	private IDataAccessServiceMock dataAccessService;

	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private WorkflowServiceStubFactory stubFactory;
	
	private WorkflowServiceStub workflowStub;

	private User user;

	private FormDTO form;

	@Before
	public void setup() {
		dataAccessService.dropDatabase();
		Application app = stub.getTestApplication();
		user = stub.getTestApplicationOwner();
		workflowStub = stubFactory.get(app, user);
		form = workflowStub.newTestForm(app, user);
		workflowStub.assignRole(app, user, form);
	}
	
	@Test
	public void shouldInsertWorkflowHistoryData() {
		WorkflowHistoryDTO workflowHistoryRow = getWorkflowHistoryDataTest();
		
		MFOperationResult result = workflowService.save(user, workflowHistoryRow);
		
		assertNotNull("Save operation result was excpected to be NOT null", result);
		assertEquals(MFOperationResult.RESULT.SUCCESS, result.getResult());
		assertEquals(1, result.getNumberOfAffectedRows());
	}

	private WorkflowHistoryDTO getWorkflowHistoryDataTest() {
		Long oldStateId = TestDataFactory.STATE;
		Long newStateId = TestDataFactory.NEW_STATE;
		Long docId = 1L;
		
		return WorkflowHistoryDTO.builder()
				.oldStateId(oldStateId)
				.newStateId(newStateId)
				.docId(docId)
				.formId(form.getId())
				.build();
	}	

	@Test
	public void shouldListWorkflowHistoryRows() {
		saveWorkflowHistoryRowBeforeQuery();
		
		Long stateId = TestDataFactory.NEW_STATE;
		
		
		List<Map<String, Object>> list = workflowService.listWorkflowHistoryBy(stateId);
		
		assertNotNull("Listing workflow history rows should return not empty list", list);
		assertEquals(1, list.size());
	}

	private void saveWorkflowHistoryRowBeforeQuery() {
		WorkflowHistoryDTO workflowHistoryRow = getWorkflowHistoryDataTest();
		
		workflowService.save(user, workflowHistoryRow);
	}
	
}
