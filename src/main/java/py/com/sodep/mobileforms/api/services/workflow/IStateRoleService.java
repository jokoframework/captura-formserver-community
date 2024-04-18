package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;


public interface IStateRoleService {

	boolean assignRoles(Application app, User user, Long stateId, List<Long> rolesId);

	boolean unassignRoles(Application app, User user, Long stateId, List<Long> rolesId);

	List<Long> listRoles(Long stateId);
}
