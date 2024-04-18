package py.com.sodep.mobileforms.test.metadata;

import java.net.URL;

import junit.framework.Assert;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test user saved on DB
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class UserServiceIT {

	@Autowired
	private IUserService userService;

	private static String testUserMail = "jgonzalezXXX@mobileforms.com";

	@Autowired
	private MockObjectsContainer mockService;

	public UserServiceIT() {
		URL confURL = UserServiceIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	public static User getTestUser() {
		User u = new User();
		u.setFirstName("Juan");
		u.setLanguage("en");
		u.setPassword("123456");
		u.setLastName("Gonzalez");
		u.setMail(testUserMail);
		return u;
	}

	@Test
	public void testSaveUser() {
		Application defaultApp = mockService.getTestApplication();
		User adminUser = mockService.getTestApplicationOwner();

		User u = getTestUser();
		u.setMail("aNonExistingEmail@nonExisitngDomains.com");
		try {
			User storedUser = userService.addNewUser(adminUser, defaultApp, u);
			User userBDCopy = userService.findByMail(u.getMail());
			Assert.assertNotNull(
					"The stored user is null but the method IUserService#registerUser hasn't returned any error",
					storedUser);

			Assert.assertEquals(storedUser, userBDCopy);
			Assert.assertEquals(storedUser, u);

		} catch (InvalidEntityException e) {
			org.junit.Assert.fail("InvalidEntityException:" + e.getMessage());
		}

	}

}
