package py.com.sodep.mobileforms.test.authorization.integration;

import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * Test project, role and user creation
 * and assignment
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class ControlAccesstoProjectIT {

	{
		URL confURL = ControlAccesstoProjectIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private MockObjectsContainer mockContainer;

	/**
	 * This method creates project, user and role to the project level
	 * on default app. Assign that role to the user and confirm that
	 * assignment
	 */
	@Test
	public void saveProjectAndCheckAccess() {
		Application testApp = mockContainer.getTestApplication();
		User appOwner = mockContainer.getTestApplicationOwner();

		// ADMIN USER IS CREATING A PROJECT
		// simulates that the dummy user is making the requests
		AuthorizationAspect.setUserInRequest(appOwner);
		// create a test project
		ProjectDTO pDTO = MockObjectsContainer.getProjectDTP(testApp);
		Project p = projectService.createNew(testApp, appOwner, pDTO);
		Assert.assertNotNull(p);

		// ADMIN USER IS CREATING A ROLE of project level
		AuthorizationAspect.setUserInRequest(appOwner);
		RoleDTO dto = RoleLifeCycleIT.getRole();
		Role r = roleService.createProjectRole(testApp, appOwner, dto);
		String authorizationGranted = AuthorizationNames.Project.READ_WEB;
		roleService.addAuths(r.getId(), authorizationGranted);

		// creates a user in the default application
		User dummyUser = mockContainer.getDummyUser(testApp);

		// userService.addUserToApp(appOwner, testApp, dummyUser);
		// Just force a recompute to be sure that the dummyUser has the latest
		// update and it still has no access.

		authControlService.computeUserAccess(dummyUser);

		// We have created the role but we haven't assigned it to the dummy
		// user,
		// so there shouldn't be any visible project for this user

		List<Project> projects = authControlService.listProjectsByAuth(testApp, dummyUser, authorizationGranted);
		Assert.assertNotNull(projects);
		Assert.assertEquals(0, projects.size());

		authControlService.assignRoleToEntity(dummyUser.getId(), r.getId(), Authorization.LEVEL_PROJECT, p.getId());

		authControlService.computeUserAccess(dummyUser);
		projects = authControlService.listProjectsByAuth(testApp, dummyUser, authorizationGranted);
		Assert.assertNotNull(projects);
		Assert.assertEquals(1, projects.size());
	}

}
