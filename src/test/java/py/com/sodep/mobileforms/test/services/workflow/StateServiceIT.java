package py.com.sodep.mobileforms.test.services.workflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class StateServiceIT {

	@Autowired
	private IStateService stateService;
	
	@Autowired
	private IStateRoleService stateRoleService;
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private WorkflowServiceStubFactory stubFactory;
	
	private WorkflowServiceStub workflowStub;

	@Autowired
	private IGroupService groupService;
	
	private User user;
	
	private FormDTO formDto;

	private Application app;
	
	@Before
	public void setup() {
		app = stub.getTestApplication();
		user = stub.getTestApplicationOwner();
		workflowStub = stubFactory.get(app, user);
		formDto = workflowStub.newTestForm(app, user);
	}
	
	@Test
	public void shouldListStates() {
		// Asignamos el rol al usuario
		RoleDTO role = workflowStub.assignRole(app, user, formDto);
	
		// Creamos estados con el mismo rol
		newState(formDto, "state1", role);
		newState(formDto, "state2", role);
		newState(formDto, "state3", role);
		
		List<State> states = stateService.listStatesForUser(user, formDto);
		Assert.assertNotNull("List of states should NOT be null", states);
		Assert.assertEquals("List should have the expected size", 3, states.size());
	}

	@Test
	public void shouldListStatesForAssigendRoles() {
		// Asignamos el rol al usuario
		RoleDTO roleAssignedToUser = workflowStub.assignRole(app, user, formDto);
	
		RoleDTO anotherRoleDto = createAnotherRole(app, user);
		
		// Creamos un solo estado con el rol asignado al usuario
		newState(formDto, "state1", roleAssignedToUser);
		
		// Otros estados con roles que no tiene
		newState(formDto, "state2", anotherRoleDto);
		newState(formDto, "state3", anotherRoleDto);
		
		
		List<State> states = stateService.listStatesForUser(user, formDto);
		Assert.assertNotNull("List of states should NOT be null", states);
		Assert.assertEquals("List should have the expected size", 1, states.size());
	}
	
	@Test
	public void shouldListAllStatesRegardlessAssignedRoles() {
		// Asignamos el rol al usuario
		RoleDTO roleAssignedToUser = workflowStub.assignRole(app, user, formDto);
	
		RoleDTO anotherRoleDto = createAnotherRole(app, user);
		
		// Creamos un solo estado con el rol asignado al usuario
		newState(formDto, "state1", roleAssignedToUser);
		newState(formDto, "state2", roleAssignedToUser);
		newState(formDto, "state3", anotherRoleDto);
		newState(formDto, "state4", anotherRoleDto);
		
		
		List<State> states = stateService.listAllStates(formDto);
		Assert.assertNotNull("List of states should NOT be null", states);
		Assert.assertEquals("List should have the expected size", 4, states.size());
	}
	
	
	@Test
	public void shouldListStatesByGroupWithRole() {
		// 1. Metemos a un usuario a un grupo
		Group group = createGroupWithUser(app, user);
		
		// 2. Asignamos el rol al grupo
		RoleDTO roleAssignedToGroup = workflowStub.assignRole(app, user, group, formDto);
		
		// 3. Creamos un solo estado con el rol asignado al grupo
		newState(formDto, "state1", roleAssignedToGroup);

		List<State> states = stateService.listStatesForUser(user, formDto);
		Assert.assertNotNull("List of states should NOT be null", states);
		Assert.assertEquals("List should have the expected size", 1, states.size());
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

	private RoleDTO createAnotherRole(Application testApp, User user) {
		RoleDTO dto = new RoleDTO();
		dto.setName("another role");
		dto.setDescription("another description");
		
		Role role = roleService.createProjectRole(testApp, user, dto);
		dto.setId(role.getId());
		return dto;
	}

	private StateDTO newState(FormDTO formDto, String name, RoleDTO role) {
		StateDTO newState = new StateDTO();
		newState.setInitial(true);
		newState.setName(name);
		newState.setFormId(formDto.getId());
		StateDTO savedState = stateService.saveState(app, user, newState);
		
		stateRoleService.assignRoles(app, user, savedState.getId(), Arrays.asList(role.getId()));
		
		return savedState;
	}
}
