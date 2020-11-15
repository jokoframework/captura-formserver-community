package py.com.sodep.mobileforms.api.services.metadata.core;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;

public interface IDemoAuthService {

	public void doSomething(Application app);

	public void doSomething(Application app, Application app2);

	public void doSomething(Project project);

	public void doSomething(Form f);

	public void doSomething(Pool f);

	void doSomething(int a, char b, String l);

	void doSomething();

}
