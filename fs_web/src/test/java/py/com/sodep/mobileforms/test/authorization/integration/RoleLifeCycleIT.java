package py.com.sodep.mobileforms.test.authorization.integration;

import java.util.Arrays;
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

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;


/**
 * This class test role life cycle from creation
 * to authorization's assignment. After that, verify correct
 * authorization's assignment
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class RoleLifeCycleIT {

	@Autowired
	private IRoleService roleService;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Autowired
	private IFormService formService;

	@Autowired
	private IAuthorizationControlService authControlService;

	private Application testApp;

	private User appOwner;
	
	private static String roleName = "A test Role";
	private static String roleDesc = "Something long that the user can write to explain what this role is for";

	public static final RoleDTO getRole() {

		RoleDTO dto = new RoleDTO();
		dto.setName(roleName);
		dto.setDescription(roleDesc);
		return dto;
	}

	@Before
	public void setup() {
		testApp = mockContainer.getTestApplication();
		appOwner = mockContainer.getTestApplicationOwner();

		// before we save we have to do this
		AuthorizationAspect.setUserInRequest(appOwner);

	}
	
	@Test
	public void testInsertRole() {
		RoleDTO dto = getRole();
		// insert a new Role


		// Save a new Role
		Role r = roleService.createApplicationRole(testApp, appOwner, dto);
		Assert.assertNotNull(r);
		Assert.assertNotNull(r.getId());
		Assert.assertEquals(roleName, r.getName());
		Assert.assertEquals(r.getDescription(), roleDesc);

		// Grant authorizations to the role
		String authorizationGranted[] = new String[] { AuthorizationNames.App.PROJECT_CANCREATE,
				AuthorizationNames.App.GROUP_EDIT };
		boolean authorizationConfirmed[] = new boolean[authorizationGranted.length];

		for (int i = 0; i < authorizationConfirmed.length; i++) {
			roleService.addAuths(r.getId(), authorizationGranted[i]);
		}

		// Check that the authorizations have been granted
		List<Authorization> auth = roleService.getRoleAuths(r.getId());
		for (Authorization authorization : auth) {
			int j = 0;
			String authName = authorization.getName();
			for (j = 0; j < authorizationGranted.length; j++) {
				if (authName.equals(authorizationGranted[j])) {
					authorizationConfirmed[j] = true;
					break;
				}
				if (j >= authorizationGranted.length) {
					// the authorization was not on the list of originally
					// granted authorization, so it has appeared by magic! This
					// is a severe fail, because it means that the software is
					// granting more access that it has been told
					Assert.fail("The authorization " + authName
							+ " was granted to the role, but we haven't asked for it");
				}
			}
		}
		for (int i = 0; i < authorizationConfirmed.length; i++) {
			Assert.assertTrue("The authorization " + authorizationGranted[i]
					+ " was expected but the service didn't return it", authorizationConfirmed[i]);
		}
	}
	
	
	@Test
	public void checkIfUserHasRole() {
		RoleDTO dto = getRole();
		// insert a new Role

		// Save a new Role
		Role r = roleService.createFormRole(testApp, appOwner, dto);
		
		// Grant authorizations to the role
		String[] auths = {AuthorizationNames.Form.READ_WORKFLOW, AuthorizationNames.Form.TRANSITION_WORKFLOW};
		for(String auth: auths) {
			roleService.addAuths(r.getId(), auth);
		}
		

		// Asignamos el rol al usuario, para que tenga autorización sobre el
		// form
		FormDTO form = mockContainer.createTestForm(testApp, appOwner);
		Form formEntity = formService.findById(form.getId());

		authControlService.assignFormRoleToEntity(formEntity, Arrays.asList(r.getId()), appOwner.getId());
		
		
		List<Role> listAssignedRoles = authControlService.listAssignedRoles(formEntity, appOwner, null);
		Assert.assertNotNull(listAssignedRoles);
		Assert.assertEquals(1, listAssignedRoles.size());
		
	}
	
}
