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
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * Check that the inserted data of a lookup table can be returned totally and
 * with page mode
 * 
 * @author danicricco
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class LookupTableDataIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Test
	public void testInsertData() throws InterruptedException, LookupTableDefinitionException {
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

		} else {
			Assert.fail("Couldn't insert data for a lookup table");
		}

		List<MFManagedData> list = service.listAllData(defaultApp, storedLookupTable.getInfo().getPk());
		Assert.assertEquals(rows.size(), countries.length);

		for (Iterator<MFManagedData> iterator = list.iterator(); iterator.hasNext();) {
			MFManagedData mfManagedData = iterator.next();
			Map<String, ?> userData = mfManagedData.getUserData();
			Integer id = (Integer) userData.get("ID");
			String expectedName = (String) userData.get("name");
			if (id < countries.length) {
				// check that the country name is the same as the original
				Assert.assertEquals(expectedName, countries[id]);
			} else {
				Assert.fail("Unexpected Id " + id + ". The number of countries is " + countries.length);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPageableData() throws InterruptedException, LookupTableDefinitionException {
		// Creates a new lookup Table
		// insert 104 data and query them by batches of 10
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();

		int numberOfData = 104;
		Integer pageSize = 10;
		String namePrefix = "D-";

		MFLoookupTableDefinition lookupDef = LookupTableIT.getDataSetDef();
		MFLoookupTableDefinition storedLookupTable = service.createLookupTable(defaultApp, ownerUser, lookupDef);

		MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(storedLookupTable.getInfo().getPk());

		Assert.assertNotNull(mfStoredDefinition);
		Assert.assertNotNull(mfStoredDefinition.getMetaDataRef());
		Assert.assertNotNull(mfStoredDefinition.getVersion());
		Assert.assertEquals(lookupDef.getFields(), mfStoredDefinition.getFields());

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		// insert numberOfData data
		for (int i = 0; i < numberOfData; i++) {
			@SuppressWarnings("rawtypes")
			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", namePrefix + i);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupTable.getInfo().getPk(), rows, false);
		if (storedData.hasSucceeded()) {
			Integer pageNumber = 1;
			int numberOfPages = numberOfData / pageSize;
			if (numberOfData % pageSize != 0) {
				// one more page for the last page
				numberOfPages++;
			}
			for (pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
				PagedData<List<MFManagedData>> data = service.listData(defaultApp, storedLookupTable.getInfo().getPk(),
						null, pageNumber, pageSize, "ID", true);
				// check the number of data returned on the PagedData

				if (numberOfData % pageSize == 0 || pageNumber < numberOfPages) {
					Assert.assertEquals(pageSize, data.getAvailable());
				} else {
					// the last page will have a different number of rows than
					// the previous one if numberOfData%pageSize!=0
					Assert.assertEquals(new Integer(numberOfData % pageSize), data.getAvailable());
				}
				Assert.assertEquals(pageNumber, data.getPageNumber());

				List<MFManagedData> pageData = data.getData();
				Long expectedID = new Long((pageNumber - 1) * pageSize);
				for (MFManagedData row : pageData) {
					Long id = row.getLong("ID");
					String name = row.getString("name");
					String expectedName = namePrefix + expectedID;
					Assert.assertEquals(expectedID, id);
					Assert.assertEquals(expectedName, name);
					expectedID++;
				}
			}

		} else {
			Assert.fail("Unable to save data to a lookuptable");
		}
	}

	@Test
	public void testWrongDataTypeInsertion() {
		// Tries to insert a collection with wrong data types
	}

}
