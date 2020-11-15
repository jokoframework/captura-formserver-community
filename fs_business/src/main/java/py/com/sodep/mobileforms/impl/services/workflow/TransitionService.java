package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionRoleService;
import py.com.sodep.mobileforms.api.services.workflow.ITransitionService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("TransitionService")
@Transactional
public class TransitionService extends BaseService<Transition> implements ITransitionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TransitionService.class);

	@Autowired
	private IStateService stateService;

	@Autowired
	private ITransitionRoleService transitionRoleService;
	
	@Autowired
	private IAuthorizationControlService authService;

	@Autowired
	private IFormService formService;
	
	public TransitionService() {
		super(Transition.class);
	}

	@Override
	public Transition findById(Long transitionId) {
		Transition findById = super.findById(transitionId);
		return findById;
	}
	
	@Override
	public List<Transition> listTransitionsForUser(User user, FormDTO formDto) {
		List<Transition> formTransitionList = super.listByPropertyEquals("formId", formDto.getId());
		return filterAllowedOnly(user, formDto, formTransitionList);
	}


	@Override
	@Authorizable(value = AuthorizationNames.App.WORKFLOW_ADMIN)
	public Transition saveTransition(Application app, User user, Transition newTransition) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		return super.save(newTransition);
	}

	@Override
	public Transition deleteTransition(Application app, User user, Long transitionId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		return super.logicalDelete(transitionId);
	}

	@Override
	public Transition editTransition(Application app, User user, Transition editTransition) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		return super.save(editTransition);
	}
	
	@Override
	public boolean isValid(FormDTO formDto, Long originStateId, Long targetStateId) {
		List<Transition> formTransactionList = super.listByPropertyEquals("formId", formDto.getId());
		for(Transition t: formTransactionList) {
			if (match(originStateId, targetStateId, t)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Transition> listTransitionsByOriginState(User user, FormDTO formDto, Long originId) {
		State originState = stateService.findById(originId);
		List<Transition> transitionList = super.listByPropertyEquals("originState", originState);
		return filterAllowedOnly(user, formDto, transitionList);
	}

	@Override
	public boolean canMakeIt(User user, FormDTO formDto, Long originStateId, Long targetStateId) {
		Transition t = findByFromTo(formDto, originStateId, targetStateId);
		if (t != null) {
			Form form = formService.getForm(formDto.getId(), formDto.getVersion());
			Project project = form.getProject();
			return canMakeIt(user, t, project);
		} else {
			LOG.warn("Could not find transition for: form #{}, originState #{}, targetState #{}", new Object[]{formDto.getId(), originStateId, targetStateId});
		}
		
		return false;
	}
	
	private void checkAccess(Application app, User user, String auth) {
		if (!authService.hasAppLevelAccess(app.getId(), user, auth)) {
			throw new AuthorizationException(auth);
		}
	}

	private List<Transition> filterAllowedOnly(User user, FormDTO formDto, List<Transition> formTransitionList) {
		List<Transition> allowedTransitions = new ArrayList<>();
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		Project project = form.getProject();
		for (Transition transition : formTransitionList) {
			if (canMakeIt(user, transition, project)) {
				allowedTransitions.add(transition);
			}
		}
		return allowedTransitions;
	}
	
	private boolean match(Long originStateId, Long targetStateId, Transition t) {
		State originState = t.getOriginState();
		State targetState = t.getTargetState();
		if (originState != null) {
			return originState.getId().longValue() == originStateId.longValue() 
					&& targetState.getId().longValue() == targetStateId.longValue();
		} else {
			return targetState.getId().longValue() == targetStateId.longValue()
					&& targetState.getInitial().booleanValue() == true;
		}
	}
	
	private boolean canMakeIt(User user, Transition t, Project project) {
		List<Long> listRoles = transitionRoleService.listRoles(t.getId());
		return hasAnyRoles(user, project, listRoles);
	}

	private boolean hasAnyRoles(User user, Project project, List<Long> listRoles) {
		List<Role> assignedRoles = authService.listAssignedRoles(project, user, null);
		assignedRoles = addGroupRoles(project, assignedRoles, user.getId());
		
		for (Role r : assignedRoles) {
			if (listRoles.contains(r.getId())) {
				return true;
			}
		}
		
		return false;
	}

	private List<Role> addGroupRoles(Project project, List<Role> assignedRoles, Long userId) {
		List<Role> newAssignedRoles = new ArrayList<>(assignedRoles);
		
		// To avoid lazy-loading exception, we fetch the user and get its groups.
		User userInTrx = em.find(User.class, userId);
		Set<Group>  groups = userInTrx.getGroups();
		
		if (groups != null) {
			for(Group g : groups) {
				List<Role> roles = authService.listAssignedRoles(project, g, null);
				newAssignedRoles.addAll(roles);
			}
		}
		return newAssignedRoles;
	}

	private Transition findByFromTo(FormDTO formDto, Long originStateId, Long targetStateId) {
		List<Transition> formTransactionList = super.listByPropertyEquals("formId", formDto.getId());
		for(Transition t: formTransactionList) {
			if (match(originStateId, targetStateId, t)) {
				return t;
			}
		}
		return null;
	}

}
