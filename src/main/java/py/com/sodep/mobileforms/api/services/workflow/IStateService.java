package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;

public interface IStateService {
	
	State findById(Long stateId);

	StateDTO saveState(Application app, User user, StateDTO newState);

	State deleteState(Application app, User user, Long stateId);

	State editState(Application app, User user, State editState);

	StateDTO getInitialFor(User user, FormDTO formDto);

	List<State> listAllStates(FormDTO formDto);

	List<State> listStatesForUser(User user, FormDTO formDto);

	/** Returns <code>true</code> if the form has an initial state associated. */
	boolean hasInitialState(User user, FormDTO formDto);

	State findByName(Long formId, String value);

}
