package py.com.sodep.mobileforms.test.data.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.NAME_PROPERTY;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.NAME_VALUE;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.STATE;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.forWorkflow;
import static py.com.sodep.mobileforms.test.common.TestDataFactory.getDataSetDef;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.workflow.IStateDataAccess;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.impl.services.workflow.StateDataAccessImpl;
import py.com.sodep.mobileforms.test.common.WorkflowData;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
public class StateDataAccessImplIT {

	private IStateDataAccess stateAccess;
	
	@Autowired
	private IDataAccessServiceMock dataAccessService;
	
	private Long testVersion = null;
	
	private String testMetadataRef = null;

	private WorkflowData workflow= forWorkflow();;

	
	@Before
	public void setUp() {
		dataAccessService.dropDatabase();
		stateAccess = new StateDataAccessImpl(dataAccessService);
		MFDataSetDefinition ddl = getDataSetDef();
		MFDataSetDefinitionMongo define = dataAccessService.define(ddl);
		testVersion = define.getVersion();
		testMetadataRef = define.getMetaDataRef();
	}
	
	
	@Test
	public void storingDocumentWithAStateShouldSucceed() throws InterruptedException {
		List<MFIncomingDataI> rows = workflow.getIncoming();
		StoreResult res = stateAccess.storeData(testMetadataRef, testVersion, rows);
		
		assertNotNull("Store operation result was excpected not to be null", res);
		assertEquals(MFOperationResult.RESULT.SUCCESS, res.getResult());
		assertEquals(1, res.getNumberOfAffectedRows());
	}


	@Test
	public void listingDocumentsShouldReturnState() throws InterruptedException {
		List<MFIncomingDataI> rows = workflow.getIncoming();
		stateAccess.storeData(testMetadataRef, testVersion, rows);
		
		List<MFManagedData> list = stateAccess.listData(testMetadataRef, testVersion, STATE);
		Long stateId = (Long) list.get(0).getMetaData().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		
		assertEquals(1, list.size());	
		assertEquals(STATE, stateId);
	}
	
	@Test
	public void listingDocumentsWithFilterShouldReturnState() throws InterruptedException {
		List<MFIncomingDataI> rows = workflow.getIncoming();
		stateAccess.storeData(testMetadataRef, testVersion, rows);
		
		ConditionalCriteria filter = workflow.getFilterByName();
		
		List<MFManagedData> list = stateAccess.listData(testMetadataRef, testVersion, STATE, filter, new OrderBy("name", true));
		Long stateId = (Long) list.get(0).getMetaData().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
		
		assertEquals(1, list.size());	
		assertEquals(STATE, stateId);
		assertEquals(NAME_VALUE, list.get(0).getString(NAME_PROPERTY));
	}


	
	
	
	

	
}
