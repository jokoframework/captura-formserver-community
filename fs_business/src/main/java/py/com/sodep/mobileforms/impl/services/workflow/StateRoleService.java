package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.entities.workflow.StateRole;
import py.com.sodep.mobileforms.api.entities.workflow.StateRoleId;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.workflow.IStateRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;

@Service("StateRoleService")
@Transactional
public class StateRoleService implements IStateRoleService{

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;
	
	@Autowired
	private IStateService stateService;

	@Autowired
	private IAuthorizationControlService authService;
	
	@Override
	public boolean assignRoles(Application app, User user, Long stateId, List<Long> rolesId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		State state = stateService.findById(stateId);
		List<StateRole> stateRoles = new ArrayList<>();
        for (Long roleId : rolesId) {
            StateRoleId stateRoleId = new StateRoleId(stateId, roleId);
            StateRole stateRole = new StateRole(stateRoleId, state);
            em.persist(stateRole);
            stateRoles.add(stateRole);
        }
        state.setStateRoles(stateRoles);
        return true;
	}
	
	@Override
	public boolean unassignRoles(Application app, User user, Long stateId, List<Long> rolesId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		State state = stateService.findById(stateId);
        for (Long roleId : rolesId) {
            StateRoleId stateRoleId = new StateRoleId(stateId, roleId);
            StateRole stateRole = new StateRole(stateRoleId, state);
            em.remove(em.contains(stateRole) ? stateRole : em.merge(stateRole));
        }
        return true;
	}

	@Override
	public List<Long> listRoles(Long stateId) {
		State state = stateService.findById(stateId);
		List<StateRole> stateRoles = state.getStateRoles();
		List<Long> rolesId = new ArrayList<Long>();
		for (StateRole stateRole : stateRoles) {
			Long roleId = stateRole.getId().getRoleId();
			rolesId.add(roleId);
		}
		return rolesId;
	}
	
	private void checkAccess(Application app, User user, String auth) {
		if (!authService.hasAppLevelAccess(app.getId(), user, auth)) {
			throw new AuthorizationException(auth);
		}
	}
}
