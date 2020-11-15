package py.com.sodep.mobileforms.test.data.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionService;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

import com.mongodb.DB;
import com.mongodb.DBCollection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
/**
 * This is a regression test for The bug #2746
 * Ref. http://gohan/redmine/issues/2746
 * @author danicricco
 *
 */
public class DownloadDataWhileModifyingIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Autowired
	private TransactionManager txManager;

	@Autowired
	private SynchronizationService synchService;

	@Autowired
	private IDataAccessServiceMock dataService;

	/**
	 * Try download lookuptable data while
	 * modifying it (inserting) at same time
	 * @throws InterruptedException
	 * @throws LookupTableDefinitionException
	 * @throws IOException
	 */
	@Test
	public void testDownloadWhilePendingTX() throws InterruptedException, LookupTableDefinitionException, IOException {
		// Creates a new lookup Table
		// Insert data
		// check the number of inserted data
		// and obtain the inserted data and compare them to the original data
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition storedLookup = service.createLookupTable(defaultApp, ownerUser, lookupDef);

		MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(storedLookup.getInfo().getPk());

		Assert.assertNotNull(mfStoredDefinition);
		Assert.assertNotNull(mfStoredDefinition.getMetaDataRef());
		Assert.assertNotNull(mfStoredDefinition.getVersion());
		Assert.assertEquals(lookupDef.getFields(), mfStoredDefinition.getFields());

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		String countries[] = new String[] { "Paraguay", "Brasil", "Argentina", "Uruguay", "Chile" };
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap<String, Comparable> map = new HashMap<String, Comparable>();
			map.put("ID", new Integer(i));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}
		// Insert some test data
		MFOperationResult storedData = service.insertData(defaultApp, storedLookup.getInfo().getPk(), rows, false);
		Assert.assertEquals(countries.length, storedData.getNumberOfAffectedRows());

		// Download the inserted data in order to get the information of the
		// last transaction
		MFDMLTransport transprotData = synchService.downloadData(storedLookup, null, null, 100);

		Assert.assertNotNull(transprotData);
		Assert.assertEquals(countries.length, transprotData.getData().size());

		DB db = dataService.getMongoConnection();

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db,
				storedLookup.getMetaDataRef(), storedLookup.getVersion());

		// Start a new transansaction for the lookup table

		Transaction tx = dataService.startTransaction(definition, OPERATION.INSERT);
		// Insert some data in the transaction. This is required because the bug
		// only appears if there is a pending transaction with pending data to
		// be commited

		String dataCollectionName = DataDefinitionService.getCollectionOfMetadataRef(storedLookup.getMetaDataRef());
		DBCollection collection = db.getCollection(dataCollectionName);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ID", 10);
		map.put("name", "USA");

		dataService._insertData(new Long(countries.length), collection, db, tx.getId(), storedLookup,
				new ArrayList<String>(), new ArrayList<String>(), map, null);

		TXInfo txInfo = transprotData.getTxInfo();

		// Note that the next transaction has not end yet. The system was
		// throwing an exception here because there was a NPE (the startRow of
		// the transaction was null)
		transprotData = synchService.downloadData(storedLookup, txInfo.getTx(), txInfo.getEndRow(), 100);
		Assert.assertNotNull(transprotData);
		// Even-though there is a pending transaction at this point the system
		// should see it as synchronized
		Assert.assertTrue(transprotData.isSynch());
	}
}
