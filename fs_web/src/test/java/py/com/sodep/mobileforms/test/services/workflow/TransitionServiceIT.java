package py.com.sodep.mobileforms.test.services.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TransitionServiceIT {

	@Autowired
	private ITransitionService transitionService;
	
	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private WorkflowServiceStubFactory stubFactory;
	
	@Autowired
	private IGroupService groupService;

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
	public void shouldNotListTransitionsIfUserHasNoRoleOrGroup() {
	
		
		RoleDTO unAssignedRole = workflowStub.newRole(app, user);
		
		// 2. Creamos un solo estado con el rol asignado al grupo
		workflowStub.newTransition(form, unAssignedRole);
		workflowStub.newTransition(form, unAssignedRole);
		workflowStub.newTransition(form, unAssignedRole);
		workflowStub.newTransition(form, unAssignedRole);
		
		
		List<Transition> tramsitions = transitionService.listTransitionsForUser(user, form);
		Assert.assertNotNull("List of transitions should NOT be null", tramsitions);
		Assert.assertEquals("List should have the expected size", 0, tramsitions.size());
	}

	@Test
	public void shouldListTransitionsByUserWithRole() {
		// 1. Asignamos el rol al grupo
		RoleDTO assignedRoleToUser = workflowStub.assignRole(app, user, form);
				
		// 2. Creamos un solo estado con el rol asignado al grupo
		workflowStub.newTransition(form, assignedRoleToUser);
		workflowStub.newTransition(form, assignedRoleToUser);
		workflowStub.newTransition(form, assignedRoleToUser);
		workflowStub.newTransition(form, assignedRoleToUser);
		
		
		List<Transition> tramsitions = transitionService.listTransitionsForUser(user, form);
		Assert.assertNotNull("List of transitions should NOT be null", tramsitions);
		Assert.assertEquals("List should have the expected size", 4, tramsitions.size());
	}
	
	@Test
	public void shouldListTransitionsByUserInGroup() {
		// 1. Metemos a un usuario a un grupo
		Group group = createGroupWithUser(app, user);
		
		// 2. Asignamos el rol al grupo
		RoleDTO assignedRoleToGroup = workflowStub.assignRole(app, user, group, form);
				
		// 3. Creamos un solo estado con el rol asignado al grupo
		workflowStub.newTransition(form, assignedRoleToGroup);
		
		List<Transition> tramsitions = transitionService.listTransitionsForUser(user, form);
		Assert.assertNotNull("List of transitions should NOT be null", tramsitions);
		Assert.assertEquals("List should have the expected size", 1, tramsitions.size());
	}
	

	private Group createGroupWithUser(Application app, User user) {
		Group group = new Group();
		group.setApplication(app);
		group.setName(TestDataFactory.TEST_GROUP_NAME);
		
		Set<User> users = new HashSet<>();
		users.add(user);
		
		group.setUsers(users);
		
		return groupService.save(user, group);
	}
	
}
