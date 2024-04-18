package py.com.sodep.mobileforms.test.services.workflow;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionRoleService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TransitionRoleServiceIT {

	@Autowired
	private ITransitionRoleService transitionRoleService;
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private ITransitionService transitionService;
	
	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private WorkflowServiceStubFactory stubFactory;
	
	private WorkflowServiceStub workflowStub;

	private User user;

	private FormDTO form;

	private Application app;

	@Before
	public void setup() {
		app = stub.getTestApplication();
		user = stub.getTestApplicationOwner();
		workflowStub = stubFactory.get(app, user);
		form = workflowStub.newTestForm(app, user);
	}
	
	@Test
	public void shouldSaveTransition() {
		
		Transition t = saveTransition();
		
		t = transitionService.saveTransition(app, user, t);
		Assert.assertNotNull(t);
	}
	

	@Test
	public void shouldAssignRoleToTransition() {
		Transition t = saveTransition();
		
		
		Role role = roleService.getOrCreateRole(TestDataFactory.TEST_ROLE_NAME, Authorization.LEVEL_FORM);
		t = transitionRoleService.assignRoles(app, user, t.getId(), Arrays.asList(role.getId()));
		
		Assert.assertNotNull(t);
		Assert.assertNotNull("Transition should have roles assigned", t.getTransitionRoles());
	}

	
	
	@Test
	public void shouldListTransitionRoles() {
		
		Transition t = saveTransition();
		
		Role role = roleService.getOrCreateRole(TestDataFactory.TEST_ROLE_NAME, Authorization.LEVEL_FORM);
		transitionRoleService.assignRoles(app, user, t.getId(), Arrays.asList(role.getId()));
		
		
		List<Long> listRoles = transitionRoleService.listRoles(t.getId());
		
		Assert.assertNotNull(listRoles);
		Assert.assertNotNull("Transition should have roles assigned and NOT be null", listRoles);
		Assert.assertEquals("Transition should have roles assigned and NOT be empty", 1, listRoles.size());
		
		
	}
	
	private Transition saveTransition() {
		StateDTO targetState = new StateDTO();
		targetState.setFormId(form.getId());
		targetState.setName("Target State");
		targetState = stateService.saveState(app, user, targetState);
		
		Transition t = new Transition();
		t.setFormId(form.getId());
		State targetStateEntity = stateService.findById(targetState.getId());
		t.setOriginState(targetStateEntity);
		t.setTargetState(targetStateEntity);
		return transitionService.saveTransition(app, user, t);
	}
}

