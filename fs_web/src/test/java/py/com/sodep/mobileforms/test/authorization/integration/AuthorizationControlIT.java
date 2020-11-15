package py.com.sodep.mobileforms.test.authorization.integration;

import java.net.URL;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.RoleServiceOnlyForTesting;

/**
 * This class test assign different roles at different levels,
 * with or without authorizations
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class AuthorizationControlIT {

	{
		URL confURL = AuthorizationControlIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IApplicationService appService;

	@Autowired
	private RoleServiceOnlyForTesting roleService;

	@Autowired
	private MockObjectsContainer mockContainer;

	/**
	 * This method creates a role at the specified level, assign the
	 * authorization.Then it will grant this role to a user at the level
	 * grantedObjectLevel over the objectId. Finally it will check if the user
	 * has the required authorization over the given object.
	 * 
	 * @param roleLevel
	 * @param authorizations
	 * @param grantedObjectLevel
	 * @param objectId
	 * @param expectedToSuccess
	 */
	public void templateTest(int roleLevel, String authorizations[], int grantedObjectLevel, Long objectId,
			boolean expectedToSuccess) {

		Application defaultApp = mockContainer.getTestApplication();
		RoleDTO dto = RoleLifeCycleIT.getRole();
		Role r = roleService.createNewRoleAtLevel(defaultApp, dto, roleLevel);

		for (int i = 0; i < authorizations.length; i++) {
			// assign the authorizations to the role
			roleService.addAuths(r.getId(), authorizations[i]);
		}

		User dummyUser = mockContainer.getDummyUser(defaultApp);

		authControlService.assignRoleToEntity(dummyUser.getId(), r.getId(), grantedObjectLevel, objectId);
		authControlService.computeUserAccess(dummyUser);
		// check that the computed authorization has the required access
		for (int i = 0; i < authorizations.length; i++) {
			boolean hasAccess = authControlService.has(defaultApp,dummyUser, authorizations[i], grantedObjectLevel, objectId);
			Assert.assertEquals(expectedToSuccess, hasAccess);

		}

	}

	/**
	 * Assign to the system level (0) an authorization of system level. Then,
	 * grant the user access to this role at a system level
	 */
	@Test
	public void testAssignSystemRoleToSystemLevel() {
		int roleLevel = 0;
		int objectLevel = 0;
		Long objectId = null;
		boolean shouldWork = true;
		templateTest(roleLevel, new String[] { AuthorizationNames.System.SYS_ALLMIGHTY, }, objectLevel, objectId,
				shouldWork);
	}

	public void testAssignAppAuthroizationToSystemRole() {
		// Assigning an app level to a system role should not grant anything.
		// Although this is conceptually possible we have decided that
		// any permission from app level should never be assigned at a system
		// level. Otherwise, it might be possible to have an "all mighty user"
		// that can access everywhere (security paranoia!)
		int roleLevel = 0;
		int objectLevel = 0;
		Long objectId = null;
		boolean shouldWork = false;
		templateTest(roleLevel, new String[] { AuthorizationNames.App.POOL_LIST, }, objectLevel, objectId, shouldWork);
	}

	/**
	 * Assign an application level role to application level.
	 */
	@Test
	public void testAssignAppRoleToAppLevel() {
		List<Application> app = appService.findAll();
		Application defaultApp = app.get(0);
		int roleLevel = 1;
		int objectLevel = 1;
		Long objectId = defaultApp.getId();
		boolean shouldWork = true;
		templateTest(roleLevel, new String[] { AuthorizationNames.App.PROJECT_CANCREATE }, objectLevel, objectId,
				shouldWork);
	}

	/**
	 * Try to assign a System role to app level. This shouldn't be possible, and
	 * the test will fail if the {@link IAuthorizationControlService} let us do
	 * it
	 */
	@Test
	public void testAssignSystemRoleToAppLevel() {
		// this shouldn't be possible, since system level is bigger than app
		// level

		List<Application> app = appService.findAll();
		Application defaultApp = app.get(0);
		int roleLevel = 0;
		int objectLevel = 1;
		Long objectId = defaultApp.getId();
		boolean shouldWork = false;
		try {
			templateTest(roleLevel, new String[] { AuthorizationNames.App.PROJECT_CANCREATE }, objectLevel, objectId,
					shouldWork);
			Assert.fail("It shouldn't be possible to assign a system role to an app level");
		} catch (AuthorizationException e) {
			// do nothing, because we are waiting this to happen

		}
	}

	/**
	 * Assign system level authorizations to an app level. This shouldn't be
	 * possible, and the test will fail if the
	 * {@link IAuthorizationControlService} let us do it
	 */
	@Test
	@Ignore
	public void testCreateAppRoleWithSystemPermission() {
		List<Application> app = appService.findAll();
		Application defaultApp = app.get(0);

		int roleLevel = 1;
		int objectLevel = 1;
		Long objectId = defaultApp.getId();
		boolean shouldWork = false;
		try {
			templateTest(roleLevel, new String[] { AuthorizationNames.System.SYS_ALLMIGHTY,
					AuthorizationNames.App.PROJECT_CANCREATE }, objectLevel, objectId, shouldWork);
			Assert.fail("It shouldn't be possible to assign a system role to an app level");
		} catch (AuthorizationException e) {
			// do nothing, because we are waiting this to happen

		}
	}
}
