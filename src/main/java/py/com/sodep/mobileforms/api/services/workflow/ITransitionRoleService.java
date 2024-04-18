package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;

public interface ITransitionRoleService {
	
	Transition assignRoles(Application app, User user, Long transitionId, List<Long> rolesId);

	Transition unassignRoles(Application app, User user, Long transitionId, List<Long> rolesId);

	List<Long> listRoles(Long transitionId);

}
