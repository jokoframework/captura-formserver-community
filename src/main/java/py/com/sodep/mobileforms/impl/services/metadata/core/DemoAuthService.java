package py.com.sodep.mobileforms.impl.services.metadata.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.metadata.core.IDemoAuthService;

@Service("demo.AuthService")
@Transactional
public class DemoAuthService implements IDemoAuthService {

	private static Logger logger = LoggerFactory.getLogger(DemoAuthService.class);

	@Override
	@Authorizable("")
	public void doSomething(Application app) {
		logger.debug("Doing something at app layer");

	}

	@Override
	@Authorizable("")
	public void doSomething(Application app, Application app2) {
		logger.debug("Do something with two apps");
	}

	@Override
	@Authorizable("")
	public void doSomething(Project project) {
		logger.debug("Doing something at project layer");

	}

	@Override
	@Authorizable("")
	public void doSomething(Form f) {
		logger.debug("Doing something at form layer");

	}

	@Override
	@Authorizable("")
	public void doSomething(Pool f) {
		// TODO Auto-generated method stub

	}

	@Override
	@Authorizable("")
	public void doSomething(int a, char b, String l) {

	}

	@Override
	public void doSomething() {

	}
}
