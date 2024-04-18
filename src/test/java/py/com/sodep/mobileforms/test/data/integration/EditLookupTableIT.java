package py.com.sodep.mobileforms.test.data.integration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.data.DBLookupTable;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test lookuptable dataset update definitions,
 * and version's increment
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class EditLookupTableIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	@Test
	/**
	 * This method will store a lookupTable, then obtain its definition and add a new version. It will check that :
	 * the last definition has been stored 
	 * its version is one more than the previous one (this is only true in this context since we are sure that 
	 * there are no other transaction running at the same time)
	 */
	public void editLookupTable() throws LookupTableDefinitionException {
		Application defaultApp = mockContainer.getTestApplication();
		User ownerUser = mockContainer.getTestApplicationOwner();
		MFLoookupTableDefinition ddl = LookupTableIT.getDataSetDef();
		DBLookupTable table = new DBLookupTable();

		table.setDefaultLanguage("en");

		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition storedLookupTable = service.createLookupTable(defaultApp, ownerUser, ddl);

		// modify the definition and add it as a new version
		MFDataSetDefinition ddl2 = LookupTableIT.getDataSetDef();
		ddl2.addField(new MFField(FIELD_TYPE.DATE, "birthdate"));
		DBLookupTable storedTable2 = service.createNewVersion(defaultApp, ownerUser, storedLookupTable.getInfo()
				.getPk(), ddl2);

		// check that the first version remains equal
		MFDataSetDefinition storedDDL1 = service.getLookupTableDefinition(storedLookupTable.getInfo().getPk());
		Assert.assertNotNull(storedDDL1);
		Assert.assertNotNull(storedDDL1.getMetaDataRef());
		Assert.assertNotNull(storedDDL1.getVersion());
		Assert.assertEquals(ddl.getFields(), storedDDL1.getFields());

		// check that the second version has the expected modification
		MFDataSetDefinition storedDDL2 = service.getLookupTableDefinition(storedTable2.getId());
		Assert.assertNotNull(storedDDL2);
		Assert.assertNotNull(storedDDL2.getMetaDataRef());
		Assert.assertNotNull(storedDDL2.getVersion());
		Assert.assertEquals(ddl2.getFields(), storedDDL2.getFields());

		// Finally checks that the version has been incremented
		long expectedVersion = storedDDL1.getVersion() + 1;
		Assert.assertEquals(expectedVersion, storedDDL2.getVersion().longValue());
		// The dataset reference should remains the same. This reflect that both
		// DDL share the same dataSet
		Assert.assertEquals(storedDDL1.getMetaDataRef(), storedDDL2.getMetaDataRef());

	}
}
