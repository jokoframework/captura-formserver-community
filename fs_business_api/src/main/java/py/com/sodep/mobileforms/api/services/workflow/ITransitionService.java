package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;

public interface ITransitionService {
	
	Transition findById(Long transitionId);
	
	Transition deleteTransition(Application app, User user, Long transitionId);

	Transition editTransition(Application app, User user, Transition editTransition);

	List<Transition> listTransitionsByOriginState(User user, FormDTO formDto, Long originId);

	boolean isValid(FormDTO formDto, Long originStateId, Long targetStateId);

	boolean canMakeIt(User user, FormDTO formDto, Long originStateId, Long targetStateId);

	List<Transition> listTransitionsForUser(User user, FormDTO formDto);

	Transition saveTransition(Application app, User usr, Transition newTransition);

}
