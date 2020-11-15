package py.com.sodep.mobileforms.test.services;

import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.impl.services.metadata.core.RoleService;
import py.com.sodep.mobileforms.test.authorization.integration.AuthorizationControlIT;

/**
 * This is a class that should be used only for testing purposes. It goals is to
 * expose the protected method "createNewRole" in order to perform some generic
 * test methods
 * 
 * @See {@link AuthorizationControlIT}
 * @author danicricco
 * 
 */

@Transactional
public class RoleServiceOnlyForTestingImpl extends RoleService implements RoleServiceOnlyForTesting {

	@Override
	public Role createNewRoleAtLevel(Application app, RoleDTO dto, int level) {
		return createNewRole(app, dto, level);
	}
}
