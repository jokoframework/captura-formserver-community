package py.com.sodep.mobileforms.impl.services.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;

/**
 * This class will parse a CSV file of the form "role,authorization" and will
 * create the roles and assign the authorizations to the role
 * 
 * @author danicricco
 * 
 */
@Service("DefaultRolesLoader")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
public class DefaultRolesLoader {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRolesLoader.class);

	private String defaultSystemRolesPath = "/defaultSystemLevelRoles.csv";
	private String defaultAppRolesPath = "/defaultAppLevelRoles.csv";
	private String defaultProjectRolesPath = "/defaultProjectLevelRoles.csv";
	private String defaultFormRolesPath = "/defaultFormLevelRoles.csv";
	private String defaultPoolRolesPath = "/defaultPoolLevelRoles.csv";

	
	@Autowired
	private IRoleService roleService;

	/**
	 * This method will delete all the authorizations associated to the default
	 * roles and will ensure that no hidden authorization was granted to a role.
	 */
	public void cleanAuthorizationOfRoles() {

		roleService.clearAuthorizationOfNonEditableRoles();
		roleService.clearAssignationOfNonEditableAuthorizations();
		logger.info("Deleted authorizations of default roles");

	}

	public void loadRoles(String path, Integer level) {
		InputStream in = DefaultRolesLoader.class.getResourceAsStream(path);
		if (in != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			try {
				int line = 1;
				while ((strLine = br.readLine()) != null) {
					strLine = strLine.trim();
					if (!strLine.startsWith("#")) {
						String[] args = strLine.split(",");
						if (args.length >= 2) {
							AuthorizationAspect.shouldCheckAuthorization(false);
							Role role = roleService.getOrCreateRole(args[0], level);
							AuthorizationAspect.shouldCheckAuthorization(false);
							roleService.addAuths(role.getId(), args[1]);
						} else {
							logger.warn("The line " + line + " of " + path + " is invalid. Skipping it");
						}

					}
					line++;

				}
			} catch (IOException e) {
				logger.error("An IO error happened while reading " + path, e);
			}
		} else {
			logger.warn("Didn't find the default roles file " + path);
		}

	}

	public void insertDefaultAppLevelRoles() {
		loadRoles(defaultAppRolesPath, Authorization.LEVEL_APP);
	}

	public void insertDefaultProjectLevelRoles() {
		loadRoles(defaultProjectRolesPath, Authorization.LEVEL_PROJECT);
	}

	public void insertDefaultFormLevelRoles() {
		loadRoles(defaultFormRolesPath, Authorization.LEVEL_FORM);
	}

	public void insertDefaultPoolLevelRoles() {
		loadRoles(defaultPoolRolesPath, Authorization.LEVEL_POOL);
	}

	public void insertDefaultSystemLevelRoles() {
		loadRoles(defaultSystemRolesPath, Authorization.LEVEL_SYSTEM);
	}
}
