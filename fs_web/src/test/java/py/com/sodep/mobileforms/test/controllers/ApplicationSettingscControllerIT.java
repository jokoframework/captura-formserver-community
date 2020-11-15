package py.com.sodep.mobileforms.test.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.i18n.I18nManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml", "/test-web-applicationContext.xml" })
@WebAppConfiguration
@Transactional
public class ApplicationSettingscControllerIT {

	protected MockMvc mvc;
	
	@Autowired
	private WebApplicationContext webContext;
	
	@Autowired
	private MockObjectsContainer stub;

	@Autowired
	private I18nBundle i18nBundle;
	
	@Autowired
	private IAuthorizationControlService authService;

	private Application app;

	private User user;

	private ObjectMapper mapper = new ObjectMapper();

	private I18nManager i18nManager;
	
	private static final boolean WORKFLOW_ENABLED = true;
	
	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(webContext).build();
		app = stub.getTestApplication();
		user = stub.getTestApplicationOwner();
		i18nManager = new I18nManager(i18nBundle, user.getLanguage());
	}
	
	@Test
	public void requestingSaveAppWithWorkflowShouldReturnOK() throws JsonProcessingException, Exception {
		
		mvc.perform(post("/application/admin/settings/save.ajax")
				.param("appId", String.valueOf(app.getId()))
				.param("hasWorkflow", String.valueOf(WORKFLOW_ENABLED))
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.sessionAttr(Attributes.ATTRIBUTE_I18N, i18nManager))
		.andDo(print())
		.andExpect(status().isOk());
		
	}
	
	@Test
	public void requestingSaveAppWithWorkflowShouldEnableFeatureForRoot() throws JsonProcessingException, Exception {
		User rootUser = stub.getRootUser();
		
		// save app with workfow enabled
		mvc.perform(post("/application/admin/settings/save.ajax")
				.param("appId", String.valueOf(app.getId()))
				.param("hasWorkflow", String.valueOf(WORKFLOW_ENABLED))
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.sessionAttr(Attributes.ATTRIBUTE_I18N, i18nManager))
		.andReturn();
		
		// We emulate entering application as root
		authService.setUpRootAuthorizations(rootUser, app);
		
		boolean workflowEnabled = authService.hasFeature(app, AuthorizationNames.Feature.WORKFLOW);
		Assert.assertTrue("Root user should have WORKFLOW enabled", workflowEnabled);
		
	}
	
	@Test
	public void requestingSaveAppWithWorkflowShouldEnableFeatureForUser() throws JsonProcessingException, Exception {
		
		// save app with workfow enabled
		mvc.perform(post("/application/admin/settings/save.ajax")
				.param("appId", String.valueOf(app.getId()))
				.param("hasWorkflow", String.valueOf(WORKFLOW_ENABLED))
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.sessionAttr(Attributes.ATTRIBUTE_I18N, i18nManager))
		.andReturn();
		
		// We emulate entering application as a regular user
		authService.setUpRootAuthorizations(user, app);
		
		boolean workflowEnabled = authService.hasFeature(app, AuthorizationNames.Feature.WORKFLOW);
		Assert.assertTrue("The user should have WORKFLOW enabled", workflowEnabled);
		
	}
	
	protected String mapToJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
}
