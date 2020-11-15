package py.com.sodep.mobileforms.test.services;

import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
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

public interface RoleServiceOnlyForTesting extends IRoleService {

	public Role createNewRoleAtLevel(Application app, RoleDTO dto, int level);
}
