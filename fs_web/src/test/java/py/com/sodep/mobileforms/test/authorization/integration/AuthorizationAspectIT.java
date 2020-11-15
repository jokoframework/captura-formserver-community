package py.com.sodep.mobileforms.test.authorization.integration;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.services.metadata.core.IDemoAuthService;

/**
 * This class actually doesn't contain valid junits tests it only contains some
 * print-like test that I (danicricco) wrote during the development of the
 * authorization framework
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class AuthorizationAspectIT {

	{
		URL confURL = AuthorizationControlIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Autowired
	private IDemoAuthService demoAuthService;

	@Test
	public void dummy() {

	}

	//@Test
	public void testAspectInvocation() {

		Application app = new Application();
		Application al2 = new Application();
		app.setId(20l);
		al2.setId(50l);
		System.out.println("Doing something at applicaiton level");
		demoAuthService.doSomething(app);
		System.out.println("Done something at app level");
		//
		// Project p = new Project();
		// p.setId(8l);
		// System.out.println("Before doing someting at project level");
		// demoAuthService.doSomething(p);
		//
		// Form f = new Form();
		// f.setId(4l);
		// demoAuthService.doSomething(f);
		// System.out.println("Done something with form");
		//
		// Pool pool = new Pool();
		// pool.setId(44l);
		// demoAuthService.doSomething(pool);
		// System.out.println("Done something with pool");
		//
		// System.out.println("------");
		// demoAuthService.doSomething(3, (char) 3, "ddd");
	}
}
