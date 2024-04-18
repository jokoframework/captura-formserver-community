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
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.entities.workflow.TransitionRole;
import py.com.sodep.mobileforms.api.entities.workflow.TransitionRoleId;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionRoleService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;

@Service("TransitionRoleService")
@Transactional
public class TransitionRoleService implements ITransitionRoleService {
	
	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;
	
	@Autowired
	private ITransitionService transitionService;

	@Autowired
	private IAuthorizationControlService authService;
	
	@Override
	public Transition assignRoles(Application app, User user, Long transitionId, List<Long> rolesId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		Transition transition = transitionService.findById(transitionId);
		List<TransitionRole> roles = new ArrayList<>();
        for (Long roleId : rolesId) {
            TransitionRoleId transitionRoleId = new TransitionRoleId(transitionId, roleId);
            TransitionRole transitionRole = new TransitionRole(transitionRoleId, transition);
            em.persist(transitionRole);
            roles.add(transitionRole);
        }
        transition.setTransitionRoles(roles);
        return transition;
	}

	@Override
	public Transition unassignRoles(Application app, User user, Long transitionId, List<Long> rolesId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		Transition transition = transitionService.findById(transitionId);
        for (Long roleId : rolesId) {
            TransitionRoleId transitionRoleId = new TransitionRoleId(transitionId, roleId);
            TransitionRole transitionRole = new TransitionRole(transitionRoleId, transition);
            em.remove(em.contains(transitionRole) ? transitionRole : em.merge(transitionRole));
        }
        
        return transition;
	}

	@Override
	public List<Long> listRoles(Long transitionId) {
		Transition transition = transitionService.findById(transitionId);
		List<TransitionRole> transitionRoles = transition.getTransitionRoles();
		List<Long> rolesId = new ArrayList<Long>();
		for (TransitionRole transitionRole : transitionRoles) {
			Long roleId = transitionRole.getId().getRoleId();
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
