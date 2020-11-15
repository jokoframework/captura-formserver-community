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
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test data insertion with primary key
 * on lookuptable with or without duplicate rows
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class LookupTablePrimaryKeyIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	/**
	 * Insert data without duplicates
	 * @throws LookupTableDefinitionException
	 * @throws InterruptedException
	 */
	@Test
	public void testSaveWithPK() throws LookupTableDefinitionException, InterruptedException {

		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
		// Set the column ID as Primary key
		lookupDef.fieldsMappedByName().get("ID").setPk(true);

		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition storedLookupDef = service.createLookupTable(defaultApp, ownerUser, lookupDef);

		MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(storedLookupDef.getInfo().getPk());

		Assert.assertNotNull(mfStoredDefinition);
		Assert.assertNotNull(mfStoredDefinition.getMetaDataRef());
		Assert.assertNotNull(mfStoredDefinition.getVersion());
		Assert.assertEquals(storedLookupDef.getFields(), mfStoredDefinition.getFields());

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		String countries[] = new String[] { "Paraguay", "Brasil", "Argentina", "Uruguay", "Chile" };
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap<String, Comparable> map = new HashMap<String, Comparable>();
			// map.put("ID", new Integer(i));
			map.put("ID", new Integer(1));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupDef.getInfo().getPk(), rows, true,
				false);
		if (storedData.hasSucceeded()) {
			// we have asked to ignore the duplicate during the insert, so only
			// the first row should have been inserted
			Assert.assertEquals(1, storedData.getNumberOfAffectedRows());
		} else {
			Assert.fail();
		}

		HashMap<String, Object> map2 = new HashMap<String, Object>();

		// Try to update a data that was not present. The system should
		// automatically insert it
		map2.put("ID", new Integer(2));
		map2.put("name", "Alemania");
		MFIncomingDataBasic newData2 = new MFIncomingDataBasic(0, map2);
		MFOperationResult updateOrInsertData = service.updateOrInsertData(defaultApp,
				storedLookupDef.getInfo().getPk(), newData2, true);
		Assert.assertNotNull(updateOrInsertData);
		Assert.assertEquals(1, updateOrInsertData.getNumberOfAffectedRows());

		List<MFManagedData> listOfAllData = service.listAllData(defaultApp, storedLookupDef.getInfo().getPk());
		Assert.assertEquals(2, listOfAllData.size());
	}
	
	/**
	 * Insert data with duplicate id
	 * @throws LookupTableDefinitionException
	 * @throws InterruptedException
	 */
	@Test
	public void testSaveWithDuplicateInTheMiddle() throws LookupTableDefinitionException, InterruptedException {
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
		// Set the column ID as Primary key
		lookupDef.fieldsMappedByName().get("ID").setPk(true);

		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition storedLookupDef = service.createLookupTable(defaultApp, ownerUser, lookupDef);

		MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(storedLookupDef.getInfo().getPk());

		Assert.assertNotNull(mfStoredDefinition);
		Assert.assertNotNull(mfStoredDefinition.getMetaDataRef());
		Assert.assertNotNull(mfStoredDefinition.getVersion());
		Assert.assertEquals(storedLookupDef.getFields(), mfStoredDefinition.getFields());

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		Integer ids[] = new Integer[] { 1, 2, 1, 3, 4 };
		String countries[] = new String[] { "Paraguay", "Brasil", "Argentina", "Uruguay", "Chile" };
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap<String, Comparable> map = new HashMap<String, Comparable>();
			map.put("ID", ids[i]);
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupDef.getInfo().getPk(), rows, true,
				false);
		if (storedData.hasSucceeded()) {
			// we have asked to ignore the duplicate during the insert. The row
			// corresponding to Argentina should be discarded as a duplicate
			Assert.assertEquals(4, storedData.getNumberOfAffectedRows());
		} else {
			Assert.fail();
		}
	}
}
