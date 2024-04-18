package py.com.sodep.mobileforms.test.data.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.MFManagedDataBasic;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test data insertion and update on lookuptable
 * and download by batch size in unique transaction
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DownloadDataWithUpdateIT {

	@Autowired
	private SynchronizationService synchService;

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Autowired
	private IDataAccessService dataService;

	/**
	 * Insert and update data on lookuptable and
	 * download them by batch with transactions. 
	 * @throws InterruptedException
	 * @throws LookupTableDefinitionException
	 */
	@Test
	public void testInsertAndUpdateDataAndTracking() throws InterruptedException, LookupTableDefinitionException {
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

		MFOperationResult storedData = service.insertData(defaultApp, storedLookup.getInfo().getPk(), rows, false);
		if (storedData.hasSucceeded()) {
			Assert.assertEquals(countries.length, storedData.getNumberOfAffectedRows());
			ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
			int minId = 1;
			criteria.add(new Criteria("ID", MFRestriction.OPERATOR.GT, minId));
			List<MFManagedData> fileredData = service.listData(defaultApp, storedLookup.getInfo().getPk(), criteria);
			for (MFManagedData managedData : fileredData) {
				Long id = managedData.getLong("ID");
				if (id <= minId) {
					Assert.fail("ILookupTableService#listData returned an object lower or equal than " + minId);
				}
			}

		} else {
			Assert.fail("Couldn't insert data for a lookup table");
		}

		// --GENERATE AN UPDATE TRANSACTION TO THE THIRD COUNTRY
		int countryToChange = 2;
		String replaceName = countries[countryToChange] + " cambiado!";
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ID", new Integer(countryToChange));
		map.put("name", replaceName);
		MFIncomingDataBasic newData = new MFIncomingDataBasic(0, map);

		ConditionalCriteria c = new ConditionalCriteria(CONDITION_TYPE.AND);
		c.add(new Criteria("ID", OPERATOR.EQUALS, new Integer(countryToChange)));

		service.updateData(defaultApp, storedLookup.getInfo().getPk(), newData, c);

		List<MFManagedData> list = service.listAllData(defaultApp, storedLookup.getInfo().getPk());
		Assert.assertEquals(rows.size(), list.size());

		MFDataSetDefinitionMongo ddl = dataService.getDataSetDefinition(storedLookup.getMetaDataRef(),
				storedLookup.getVersion());

		MFDMLTransport transport = synchService.downloadData(ddl, null, 0l, countries.length + 10);
		// We have inserted countries.length, and we are asking 10 rows more
		// than countries.lenghth. Therefore, we should receive all the rows of
		// the
		// transactions (but no more than than)
		Assert.assertEquals(countries.length, transport.getData().size());
		// Since we have obtained all rows from the transaction the transaction
		// should be marked as final
		Assert.assertEquals(true, transport.isFinal());

		int txSize = countries.length / 2;
		transport = synchService.downloadData(ddl, null, 0l, txSize);
		// we shouldn't get more data than we have asked
		Assert.assertEquals(txSize, transport.getData().size());
		Assert.assertEquals(false, transport.isFinal());

		// we should receive the remaining data of the first transaction
		transport = synchService.downloadData(ddl, transport.getTxInfo().getTx(), transport.getTxInfo().getEndRow(),
				countries.length - txSize);
		Assert.assertEquals(countries.length - txSize, transport.getData().size());
		Assert.assertEquals(true, transport.isFinal());

		// The next transaction should be an update
		transport = synchService
				.downloadData(ddl, transport.getTxInfo().getTx(), transport.getTxInfo().getEndRow(), 10);

		Assert.assertEquals(OPERATION.UPDATE, transport.getTxInfo().getOperation());
		Assert.assertEquals(1, transport.getData().size());
		MFManagedDataBasic row = transport.getData().get(0);
		Assert.assertEquals(true, transport.isFinal());
		Assert.assertEquals(false, transport.isSynch());
		String value = row.getString("name");
		Assert.assertEquals(replaceName, value);

	}

}
