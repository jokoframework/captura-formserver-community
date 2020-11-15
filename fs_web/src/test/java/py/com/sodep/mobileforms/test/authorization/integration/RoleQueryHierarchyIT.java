package py.com.sodep.mobileforms.test.authorization.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.impl.services.auth.AuthorizationInitializer;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test roles at distinct levels and query them at
 * corresponding level
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class RoleQueryHierarchyIT {

	@Autowired
	private IRoleService roleService;

	@Autowired
	private MockObjectsContainer mockContainer;

	private Application testApp;

	private User appOwner;
	
	@Before
	public void setup() {
		testApp = mockContainer.getTestApplication();
		appOwner = mockContainer.getTestApplicationOwner();
		
		// borramos el rol workflow si es que existe
		deleteWorkflowRoles();
	}

	private void deleteWorkflowRoles() {
		AuthorizationAspect.setUserInRequest(appOwner);
		List<Role> roles = roleService.listValidRolesAtLevel(testApp, Authorization.LEVEL_PROJECT, AuthorizationInitializer.roleName);
		if (roles.size() == 1) {
			Role role = roles.get(0);
			roleService.logicalDelete(role);
		}
	}

	@Test
	public void testListValidRolesAtLevel() {

		
		RoleDTO dto = new RoleDTO();
		dto.setName("app");
		dto.setDescription("app");
		// insert a new Role

		// Save a new application role
		AuthorizationAspect.setUserInRequest(appOwner);
		roleService.createApplicationRole(testApp, appOwner, dto);

		dto.setName("proj");
		dto.setDescription("proj");
		roleService.createProjectRole(testApp, appOwner, dto);

		dto.setName("form");
		dto.setDescription("form");
		roleService.createFormRole(testApp, appOwner, dto);

		dto.setName("pool");
		dto.setDescription("pool");
		roleService.createPoolRole(testApp, appOwner, dto);

		List<Role> roles = roleService.listValidRolesAtLevel(testApp, 1, null);
		Assert.assertEquals("Quering roles at app level should return all type of roles", 4, roles.size());

		roles = roleService.listValidRolesAtLevel(testApp, 2, null);
		Assert.assertEquals("Quering roles at project level should return project and form roles", 2, roles.size());

		roles = roleService.listValidRolesAtLevel(testApp, 3, null);
		Assert.assertEquals("Quering roles at form level should only return form roles", 1, roles.size());

		roles = roleService.listValidRolesAtLevel(testApp, 4, null);
		Assert.assertEquals("Quering roles at pool level should only return pool roles", 1, roles.size());
	}
}
