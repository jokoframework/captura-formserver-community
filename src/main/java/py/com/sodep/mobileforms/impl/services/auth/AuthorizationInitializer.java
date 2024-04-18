package py.com.sodep.mobileforms.impl.services.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.AuthorizationConfiguration;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;

/**
 * <p>
 * This class loads the authorization, the default roles and the root user.
 * 
 * </p>
 * <p>
 * Authorization are processed with the class {@link AuthorizationLoader}. The
 * default roles are useful to configure the authorization that a user shall
 * have after creating the different datomo objects (application, form,project,
 * pool).
 * </p>
 * <p>
 * Finally, a test application will be added if the parameter
 * {@link DBParameters#CREATE_TEST_APP} is on.
 * </p>
 * 
 * @see AuthorizationLoader
 */
@Component
public class AuthorizationInitializer {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationInitializer.class);

	private static final String ROLE_SYSADMIN = "SYS_ALL_MIGHTY";
	private static final String DEFAULT_ROOT_USER = "root@mobileforms.sodep.com.py";
	private static final String defaultPassword = "123456";

	private static final String TEST_USER = "admin@testappmf.sodep.com.py";
	private static final String TEST_APP = "TestAppMF";

	public static final String roleName = "ROL_WORKFLOW_ADMIN";

	private static final String roleDesc = "Rol con permisos de administración de workflow";

	@Autowired
	private AuthorizationLoader loader;

	@Autowired
	private DefaultRolesLoader rolesLoader;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	private IAuthorizationService authorizationService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Autowired
	private ISystemParametersBundle parameterBoundle;

	@Value("${workflow.enabled:false}")
	private boolean workflowEnabled;
	
	@Value("${workflow.appId:0}")
	private Long workflowAppId;
	
	@PostConstruct
	public void init() {
		try {
			// Insert authorization into the DB
			loader.insertAndCheckAuthorizations();
			logger.info("Authorization loaded");

			InputStream in = AuthorizationInitializer.class.getResourceAsStream("/authorizations_conf.json");
			ObjectMapper mapper = new ObjectMapper();
			AuthorizationConfiguration configuration = mapper.readValue(in, AuthorizationConfiguration.class);
			in.close();
			loader.insertAuthorizationConfiguration(configuration);
			logger.info("Authorization module configured");
			// load the authorizations in memory
			authorizationService.reloadAuthorizations();

			// After loading the authorizations in memory it is safe to insert
			// the default roles
			rolesLoader.cleanAuthorizationOfRoles();
			rolesLoader.insertDefaultSystemLevelRoles();
			rolesLoader.insertDefaultAppLevelRoles();
			rolesLoader.insertDefaultProjectLevelRoles();
			rolesLoader.insertDefaultFormLevelRoles();
			rolesLoader.insertDefaultPoolLevelRoles();

			logger.info("Default roles loaded");

			User adminUser = createAllMightyUser();

			logger.info("All mighty user loaded/created");
			
			// A test environment is useful for developers to have a quick way
			// to test the application. This might be disable in production mode
			Boolean createTestEnvironment = parameterBoundle.getBoolean(DBParameters.CREATE_TEST_APP);
			if (createTestEnvironment) {
				createTestEnvironment(adminUser);
			}

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (JsonParseException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a user that has authorization to access everything
	 */
	private User createAllMightyUser() {
		// obtain the admin user or create it
		AuthorizationAspect.shouldCheckAuthorization(false);
		User adminUser = userService.findByMail(DEFAULT_ROOT_USER);
		if (adminUser == null) {
			adminUser = new User();
			adminUser.setMail(DEFAULT_ROOT_USER);
			adminUser.setPassword(defaultPassword);
			adminUser.setLanguage("en");
			adminUser.setFirstName("admin");
			adminUser.setLastName("admin");
			adminUser.setRootUser(true);
			AuthorizationAspect.shouldCheckAuthorization(false);
			adminUser = userService.addNewUser(null, null, adminUser);
		}
		// create the default role and assign allmighty rights to it
		AuthorizationAspect.shouldCheckAuthorization(false);
		Role role = roleService.getOrCreateRole(ROLE_SYSADMIN, Authorization.LEVEL_SYSTEM);
		AuthorizationAspect.shouldCheckAuthorization(false);
		authControlService.assignRoleToEntity(adminUser.getId(), role.getId(), Authorization.LEVEL_SYSTEM, null);
		return adminUser;
	}

	private void createTestEnvironment(User adminUser) {
		logger.info("Configuring default test environment");

		// create an admin user for the application
		AuthorizationAspect.shouldCheckAuthorization(false);
		User testUser = userService.findByMail(TEST_USER);
		if (testUser == null) {
			testUser = new User();
			testUser.setMail(TEST_USER);
			testUser.setPassword(defaultPassword);
			testUser.setLanguage("en");
			testUser.setFirstName("admin");
			testUser.setLastName("admin");
			AuthorizationAspect.shouldCheckAuthorization(false);
			testUser = userService.addNewUser(adminUser, null, testUser);
		}
		AuthorizationAspect.shouldCheckAuthorization(false);
		List<Application> apps = applicationService.findByName(TEST_APP);
		Application testApp;
		// create the test application if it doesn't exists
		if (apps.size() > 0) {
			testApp = apps.get(0);
			logger.info("Test Application already existed.");
		} else {
			testApp = new Application();
			testApp.setName(TEST_APP);
			testApp.setDefaultLanguage("en");
			testApp.setOwner(adminUser);
			AuthorizationAspect.shouldCheckAuthorization(false);
			testApp = applicationService.initAppWithOwner(TEST_APP, testUser, "en");
			logger.info("Created Test Application");
		}

		if (workflowEnabled && workflowAppId.longValue() > 0) {
			createWorkflowRoles(adminUser, workflowAppId);
		}
	}

	private void createWorkflowRoles(User adminUser, Long appWithWorkflow) {
		logger.info("Workflow is enabled, creating roles por application #" + appWithWorkflow);
		
		AuthorizationAspect.shouldCheckAuthorization(false);
		Application application = applicationService.findById(appWithWorkflow);
		if (application == null) {
			logger.warn("Unable to create workflow role for application #" + appWithWorkflow + ". The application does not exist.");
			return;
		}
		
		RoleDTO dto = getWorkflowRole();
		AuthorizationAspect.shouldCheckAuthorization(false);
		List<Role> roles = roleService.listValidRolesAtLevel(application, Authorization.LEVEL_PROJECT, dto.getName());
		Role role = null;
		if (roles.size() == 1) {
			logger.info("Workflow Role with name '" + dto.getName() + "' already exists.");
			role = roles.get(0);
		} else {
			logger.info("Workflow Role with name '" + dto.getName() + "' does not exist. Creating it.");
			// Save a new Role
			AuthorizationAspect.shouldCheckAuthorization(false);
			role = roleService.createProjectRole(application, adminUser, dto);
		}
		
		// Asignamos autorizaciones (permisos)
		String[] auths = {AuthorizationNames.Form.READ_WORKFLOW, AuthorizationNames.Form.TRANSITION_WORKFLOW};
		for (String auth: auths) {
			AuthorizationAspect.shouldCheckAuthorization(false);
			roleService.addAuths(role.getId(), auth);
		}
		logger.info("Workflow role creation done.");
	}

	private RoleDTO getWorkflowRole() {
		RoleDTO dto = new RoleDTO();
		dto.setName(roleName);
		dto.setDescription(roleDesc);
		return dto;
	}
}
