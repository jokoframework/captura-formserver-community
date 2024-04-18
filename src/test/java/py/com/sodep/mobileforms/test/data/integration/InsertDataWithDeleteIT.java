package py.com.sodep.mobileforms.test.data.integration;

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
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR_MODIF;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test data insertion and deletion on lookuptable
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class InsertDataWithDeleteIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	/**
	 * Insert data on lookuptable and verify them. Later,
	 * delete them and verify too
	 * @throws InterruptedException
	 * @throws LookupTableDefinitionException
	 */
	@Test
	public void testInsertDataAndTracking() throws InterruptedException, LookupTableDefinitionException {
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
		Assert.assertEquals(countries.length, storedData.getNumberOfAffectedRows());
		rows = new ArrayList<MFIncomingDataBasic>();

		countries = new String[] { "Paraguay 2", "Brasil 2", "Argentina 2", "Uruguay 2", "Chile 2" };
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap<String, Comparable> map = new HashMap<String, Comparable>();
			map.put("ID", new Integer(i + 10));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}
		storedData = service.insertData(defaultApp, storedLookup.getInfo().getPk(), rows, false);
		rows = new ArrayList<MFIncomingDataBasic>();

		countries = new String[] { "Paraguay 3", "Brasil 3", "Argentina 3", "Uruguay 3", "Chile 3" };
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap<String, Comparable> map = new HashMap<String, Comparable>();
			map.put("ID", new Integer(i + 20));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		storedData = service.insertData(defaultApp, storedLookup.getInfo().getPk(), rows, false);

		//Relates all countries but Chile
		ConditionalCriteria c = new ConditionalCriteria(CONDITION_TYPE.AND);
		Criteria likeRes = new Criteria("name", OPERATOR.REGEX, "a");
		
		likeRes.addModificator(OPERATOR_MODIF.ANYWHERE);
		c.add(likeRes);
		MFOperationResult deleteSummary = service.deleteData(defaultApp, storedLookup.getInfo().getPk(), c);
		Assert.assertNotNull(deleteSummary);
		Assert.assertTrue(deleteSummary.hasSucceeded());
		Assert.assertEquals(12, deleteSummary.getNumberOfAffectedRows());

	}
}
