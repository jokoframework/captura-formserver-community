package py.com.sodep.mobileforms.test.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;

@Component
public class WorkflowServiceStubFactory {
	
	@Autowired
	private WorkflowServiceStub stub;
	
	public WorkflowServiceStub get(Application app, User user) {
		return stub.postInitialize(app, user);
	}
}
