package py.com.sodep.mobileforms.test.endpoints;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.RoleDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.Transition;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.test.common.TestDataFactory;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStub;
import py.com.sodep.mobileforms.test.services.WorkflowServiceStubFactory;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.endpoints.controllers.WorkflowActionsEndpoint;
import py.com.sodep.mobileforms.web.endpoints.json.WorkflowActionRequest;
import py.com.sodep.mobileforms.web.json.JsonResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml", "/test-web-applicationContext.xml" })
@WebAppConfiguration
@Transactional
public class WorkflowActionsEndpointIT {
	
	protected MockMvc mvc;

	@Autowired
	private WebApplicationContext webContext;
	
	@Autowired
	private MockObjectsContainer stub;
	
	@Autowired
	private WorkflowServiceStubFactory workflowStubFactory;
	
	@Autowired
	private IDataAccessServiceMock dataAccessService;

	private User user;

	private FormDTO form;

	private ObjectMapper mapper = new ObjectMapper();

	private RoleDTO role;

	private Application app;

	private WorkflowServiceStub workflowStub;
	
	@Before
    public void setUp() {
    	mvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    	app = stub.getTestApplication();
    	user = stub.getTestApplicationOwner();
    	workflowStub = workflowStubFactory.get(app, user);
    	form = workflowStub.newTestForm(app, user);
		role = workflowStub.assignRole(app, user, form);
	}

	@After
	public void tearDown() {
		dataAccessService.dropDatabase();
	}
	
	@Test
	public void shouldSendRequestToChangeState() throws Exception {
		Transition transition = workflowStub.newTransition(form, role);
		StateDTO stateDto = new StateDTO(transition.getOriginState().getId());
		StoreResult result = workflowStub.saveInWorkflow(user, form, stateDto);
		
		WorkflowActionRequest request = buildRequest();
		String jsonBody = mapToJson(request);
		JsonResponse<Object> excpected = buildExpected(1, RESULT.SUCCESS, result.getLastStoredRowId());
		
		mvc.perform(post("/workflow/documents/{docId}/states/{targetState}", result.getLastStoredRowId(), transition.getTargetState().getId())
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody))
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(content().string(mapToJson(excpected)));
	}
	
	@Test
	public void shouldSendRequestToAssignFirstStateToDocument() throws Exception {
		Transition transition = workflowStub.newInitialTransition(form, role);
		StoreResult result = workflowStub.save(user, form);
		
		WorkflowActionRequest request = buildRequest();
		String jsonBody = mapToJson(request);
		JsonResponse<Object> excpected = buildExpected(1, RESULT.SUCCESS, result.getLastStoredRowId());
		
		mvc.perform(post("/workflow/documents/{docId}/states/{targetState}", result.getLastStoredRowId(), transition.getTargetState().getId())
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody))
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(content().string(mapToJson(excpected)));
	}
	
	@Test
	public void shouldSendRequestToChangeToSameState() throws Exception {
		Transition transition = workflowStub.newSelfTransition(form, role);
		StateDTO stateDto = new StateDTO(transition.getOriginState().getId());
		StoreResult result = workflowStub.saveInWorkflow(user, form, stateDto);
		
		WorkflowActionRequest request = buildRequest();
		String jsonBody = mapToJson(request);
		JsonResponse<Object> excpected = buildExpected(1, RESULT.SUCCESS, result.getLastStoredRowId());
		
		mvc.perform(post("/workflow/documents/{docId}/states/{targetState}", result.getLastStoredRowId(), transition.getTargetState().getId())
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody))
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(content().string(mapToJson(excpected)));
	}
	
	
	@Test
	public void shouldSendRequestToChangeStateAndFailForInvalidTransition() throws Exception {
		Transition transition = workflowStub.newTransition(form, role);
		Transition anotherTransition = workflowStub.newTransition(form, role);
		
		StateDTO stateDto = new StateDTO(transition.getOriginState().getId());
		StoreResult result = workflowStub.saveInWorkflow(user, form, stateDto);
		
		WorkflowActionRequest request = buildRequest();
		String jsonBody = mapToJson(request);
		
		mvc.perform(post("/workflow/documents/{docId}/states/{targetState}", result.getLastStoredRowId(), anotherTransition.getTargetState().getId())
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody))
		.andDo(print())
		.andExpect(status().isBadRequest());
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSendRequestToChangeStateAndFailIfNotAllowed() throws Exception {
		Transition transition = workflowStub.newTransition(form, role);
		
		StateDTO stateDto = new StateDTO(transition.getOriginState().getId());
		StoreResult result = workflowStub.saveInWorkflow(user, form, stateDto);
		
		WorkflowActionRequest request = buildRequest();
		String jsonBody = mapToJson(request);
		
		workflowStub.unAssignWorkflowRole(user, form, role);
		
		
		mvc.perform(post("/workflow/documents/{docId}/states/{targetState}", result.getLastStoredRowId(), transition.getTargetState().getId())
				.sessionAttr(Attributes.ATTRIBUTE_USER, user)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody))
		.andDo(print())
		.andExpect(status().isBadRequest());
	}
	
	private JsonResponse<Object> buildExpected(int expectedAffectedRows, RESULT expectedResult, Long lastStoredRowId) {
		MFOperationResult result = new MFOperationResult(expectedAffectedRows);
		result.setResult(expectedResult);
		JsonResponse<Object> jsonResponse = WorkflowActionsEndpoint.toJsonResponse(result, lastStoredRowId);
		return jsonResponse;
	}

	private WorkflowActionRequest buildRequest() {
		WorkflowActionRequest req = new WorkflowActionRequest();
		req.setFormId(form.getId());
		req.setFormVersion(form.getVersion());
		req.setWorkflowComment(TestDataFactory.COMMENT_VALUE);
		return req;
	}

	protected String mapToJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

}
