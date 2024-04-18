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
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test lookuptable data insertion and
 * query them
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class QueryLookupTablesIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Test
	public void testInsertAndQueryData() throws InterruptedException, LookupTableDefinitionException {
		// Creates a new lookup Table
		// Insert data
		// check the number of inserted data
		// and obtain the inserted data and compare them to the original data
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition storedLookupTable = service.createLookupTable(defaultApp, ownerUser, lookupDef);

		MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(storedLookupTable.getInfo().getPk());

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

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupTable.getInfo().getPk(), rows, false);
		if (storedData.hasSucceeded()) {
			Assert.assertEquals(countries.length, storedData.getNumberOfAffectedRows());
			ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
			int minId = 1;
			criteria.add(new Criteria("ID", MFRestriction.OPERATOR.GT, minId));
			List<MFManagedData> fileredData = service.listData(defaultApp, storedLookupTable.getInfo().getPk(),
					criteria);
			for (MFManagedData managedData : fileredData) {
				Long id = managedData.getLong("ID");
				if (id <= minId) {
					Assert.fail("ILookupTableService#listData returned an object lower or equal than " + minId);
				}
			}

			criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
			minId = 2;
			int maxId = 4;
			criteria.add(new Criteria("ID", MFRestriction.OPERATOR.GT, minId));
			criteria.add(new Criteria("ID", MFRestriction.OPERATOR.LT, maxId));
			fileredData = service.listData(defaultApp, storedLookupTable.getInfo().getPk(), criteria);
			int expectedNumberOfData = maxId - minId - 1;
			Assert.assertEquals(expectedNumberOfData, fileredData.size());
		} else {
			Assert.fail("Couldn't insert data for a lookup table");
		}

	}
}
