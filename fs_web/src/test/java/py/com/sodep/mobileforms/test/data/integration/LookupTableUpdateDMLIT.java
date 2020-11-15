package py.com.sodep.mobileforms.test.data.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test lookuptable data creation, insert and
 * update; later, verify correct operations
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class LookupTableUpdateDMLIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Test
	public void testInsertAndUpdateData() throws InterruptedException, LookupTableDefinitionException {
		// Creates a new lookup Table
		// Insert data
		// check the number of inserted data
		// and obtain the inserted data and compare them to the original data
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
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
			map.put("ID", new Integer(i));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupDef.getInfo().getPk(), rows, false);
		if (storedData.hasSucceeded()) {
			Assert.assertEquals(countries.length, storedData.getNumberOfAffectedRows());
			ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
			int minId = 1;
			criteria.add(new Criteria("ID", MFRestriction.OPERATOR.GT, minId));
			List<MFManagedData> fileredData = service.listData(defaultApp, storedLookupDef.getInfo().getPk(), criteria);
			for (MFManagedData managedData : fileredData) {
				Long id = managedData.getLong("ID");
				if (id <= minId) {
					Assert.fail("ILookupTableService#listData returned an object lower or equal than " + minId);
				}
			}

		} else {
			Assert.fail("Couldn't insert data for a lookup table");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		String newName = "Paraguay 2";
		int idToChange = 0;
		map.put("ID", idToChange);
		map.put("name", newName);
		MFIncomingDataBasic newData = new MFIncomingDataBasic(0, map);

		ConditionalCriteria c = new ConditionalCriteria(CONDITION_TYPE.AND);
		c.add(new Criteria("ID", OPERATOR.EQUALS, new Integer(0)));

		MFOperationResult l = service.updateData(defaultApp, storedLookupDef.getInfo().getPk(), newData, c);
		Assert.assertNotNull(l);
		Assert.assertTrue(l.hasSucceeded());
		Assert.assertEquals(1, l.getNumberOfAffectedRows());

		// List and check that the data has actually changed
		List<MFManagedData> list = service.listAllData(defaultApp, storedLookupDef.getInfo().getPk());
		Assert.assertEquals(rows.size(), list.size());

		for (Iterator<MFManagedData> iterator = list.iterator(); iterator.hasNext();) {
			MFManagedData mfManagedData = iterator.next();
			Map<String, ?> userData = mfManagedData.getUserData();
			Integer id = (Integer) userData.get("ID");
			String obtainedName = (String) userData.get("name");
			if (id == idToChange) {
				Assert.assertEquals(newName, obtainedName);
			} else {
				Assert.assertEquals(countries[id], obtainedName);
			}
		}

	}
}
