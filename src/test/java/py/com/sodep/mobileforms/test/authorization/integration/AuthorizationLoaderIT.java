package py.com.sodep.mobileforms.test.authorization.integration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.AuthorizationConfiguration;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.impl.services.auth.AuthorizationLoader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test Authorizations loading from json file
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class AuthorizationLoaderIT {
	{
		URL confURL = AuthorizationLoaderIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Autowired
	private AuthorizationLoader loader;

	@Autowired
	private IAuthorizationService authService;
	
	/**
	 * This method loading authorizations from json file
	 * and verify if they was loaded, visibility and authorizations
	 * dependencies
	 */
	@Test
	public void testLoadingAuthorizations() throws IllegalArgumentException, ClassNotFoundException,
			IllegalAccessException, JsonParseException, JsonMappingException, IOException {
		loader.insertAndCheckAuthorizations();
		ObjectMapper mapper = new ObjectMapper();
		InputStream in = AuthorizationLoaderIT.class.getResourceAsStream("/authorizations_conf_test.json");
		AuthorizationConfiguration configuration = mapper.readValue(in, AuthorizationConfiguration.class);
		in.close();
		loader.insertAuthorizationConfiguration(configuration);
		authService.reloadAuthorizations();
		Authorization auth = authService.get("form.edit");
		Assert.assertNotNull(auth);
		// We know from authorizations_conf_test.json that form.edit must be
		// non visible (this is just an example for testing)
		Assert.assertFalse(auth.getVisible());

		auth = authService.get("project.edit");
		Assert.assertNotNull(auth);
		// project.edit is not on the list of hidden authorization on the file
		// authorizations_conf_test.json
		Assert.assertTrue(auth.getVisible());

		List<Authorization> dependencies = auth.getDependentAuthorizations();

		boolean haveFormEdit = false;
		for (Authorization dependant : dependencies) {
			if (dependant.getName().equals("form.edit")) {
				haveFormEdit = true;
			}
		}
		Assert.assertTrue("Test file configured the relation project.edit -> form.edit, but it's not present",
				haveFormEdit);
	}

}
