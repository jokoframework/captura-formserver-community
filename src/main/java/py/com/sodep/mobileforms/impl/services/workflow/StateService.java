package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.workflow.IStateRoleService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("StateService")
@Transactional
public class StateService extends BaseService<State> implements IStateService{

	private static final Logger LOG = LoggerFactory.getLogger(StateService.class);
	
	@Autowired
	private IStateRoleService stateRoleService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IAuthorizationControlService authService;
	
	public StateService() {
		super(State.class);
	}
	
	@Override
	public State findById(Long stateId) {
        return super.findById(stateId);
    }

	@Override
	public List<State> listStatesForUser(User user, FormDTO formDto) {
		List<State> states = listByPropertyEquals("formId", formDto.getId());
		List<State> allowedStates = new ArrayList<>();
		Form form = formService.getForm(formDto.getId(), formDto.getVersion());
		Project project = form.getProject();
		for (State state : states) {
			if (canAccessIt(user, project, state.getId())) {
				allowedStates.add(state);
			}
		}
		return allowedStates;
	}
	


	@Override
	public List<State> listAllStates(FormDTO formDto) {
		List<State> states = listByPropertyEquals("formId", formDto.getId());
		return states;
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.WORKFLOW_ADMIN)
	public StateDTO saveState(Application app, User user, StateDTO newState) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		State state = fromDTO(newState);
		State saved = super.save(state);
		return toDTO(saved);
	}

	

	@Override
	public State deleteState(Application app, User user, Long stateId) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		return super.logicalDelete(stateId);
	}

	@Override
	public State editState(Application app, User user, State editState) {
		checkAccess(app, user, AuthorizationNames.App.WORKFLOW_ADMIN);
		return super.save(editState);
	}
	
	@Override
	public StateDTO getInitialFor(User user, FormDTO formDto) {
		List<State> formStateList = this.listAllStates(formDto);
		if (formStateList != null && !formStateList.isEmpty()) {
			for(State s : formStateList) {
				if (s.getInitial() != null 
						&& s.getInitial().booleanValue()) {
					return toDTO(s);
				}
			}
		}
		return null;
	}

	private void checkAccess(Application app, User user, String auth) {
		if (!authService.hasAppLevelAccess(app.getId(), user, auth)) {
			throw new AuthorizationException(auth);
		}
	}
	
	private StateDTO toDTO(State s) {
		StateDTO stateDTO = new StateDTO(s.getId());
		stateDTO.setName(s.getName());
		stateDTO.setInitial(s.getInitial());
		stateDTO.setDescription(s.getDescription());
		stateDTO.setFormId(s.getFormId());
		return stateDTO;
	}
	
	private State fromDTO(StateDTO stateDto) {
		State state = new State();
		state.setDescription(stateDto.getDescription());
		state.setName(stateDto.getName());
		state.setInitial(stateDto.getInitial());
		state.setFormId(stateDto.getFormId());
		return state;
	}
	

	private boolean hasAnyRolesOnProject(User user, Project project, List<Long> listRoles) {
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
		
		// To avoid lazy-loading exceptions, we fetch the user to get its groups.
		User userInTrx = em.find(User.class, userId);
		Set<Group> groups = userInTrx.getGroups();
		
		if (groups != null) {
			for (Group g: groups) {
				List<Role> groupRoles = authService.listAssignedRoles(project, g, null);
				newAssignedRoles.addAll(groupRoles);
			}
		}
		return newAssignedRoles;
		
	}

	public boolean canAccessIt(User user, Project project, Long stateId) {
		State s = this.findById(stateId);
		if (s != null) {
			List<Long> listRoles = stateRoleService.listRoles(s.getId());
			return hasAnyRolesOnProject(user, project, listRoles);
		} else {
			LOG.warn("Could not find state for: project #{}, state #{}", project.getId(), stateId);
		}
		
		return false;
	}

	@Override
	public boolean hasInitialState(User user, FormDTO formDto) {
		StateDTO initialFor = this.getInitialFor(user, formDto);
		return initialFor != null;
	}

	@Override
	public State findByName(Long formId, String name) {
		TypedQuery<State> q = em.createQuery(
                "SELECT DISTINCT(s) FROM State s WHERE s.name=:name AND s.formId=:formId AND s.deleted=false ",
                State.class);
        q.setParameter("formId", formId);
        q.setParameter("name", name);
        try {
            State s = q.getSingleResult();
            return s;
        } catch (NoResultException e) {
        	LOG.warn("Could not find state for: name '{}', form id '{}'", name, formId);
            return null;
        } catch (NonUniqueResultException e) {
        	LOG.warn("There is more than one result from the query with: state name '{}', form id '{}'", name, formId);
            return null;
        }
	}
	
}
