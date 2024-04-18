package py.com.sodep.mobileforms.test.data.integration;

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
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * Check the integration between lookup table and mongo DB
 * 
 * @author danicricco
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class LookupTableIT {

	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	/**
	 * Creates a test data set definition that has following fields:
	 * <ul>
	 * <li>Number,ID</li>
	 * <li>String,name</li>
	 * </ul>
	 * 
	 * @return
	 */
	public static MFLoookupTableDefinition getDataSetDef() {
		MFLoookupTableDefinition def = new MFLoookupTableDefinition();
		MFField pkField = new MFField(FIELD_TYPE.NUMBER, "ID");
		def.addField(pkField);
		def.addField(new MFField(FIELD_TYPE.STRING, "name"));
		LookupTableDTO info = new LookupTableDTO();
		info.setIdentifier("iden2");
		info.setName("Any name");
		info.setAcceptRESTDMLs(true);
		def.setInfo(info);

		return def;
	}

	@Test
	/**
	 * Save a lookup table and checks that the fields of the stored definition are the same than the original
	 */
	public void saveLookupTable() {

		Application defaultApp = mockContainer.getTestApplication();
		User user = mockContainer.getDummyUser(defaultApp);

		User ownerUser = mockContainer.getTestApplicationOwner();

		MFLoookupTableDefinition lookupDef = getDataSetDef();

		AuthorizationAspect.setUserInRequest(ownerUser);
		MFLoookupTableDefinition defStored;
		try {
			defStored = service.createLookupTable(defaultApp, user, lookupDef);
			Assert.assertNotNull(defStored.getInfo().getPk());
			MFDataSetDefinition mfStoredDefinition = service.getLookupTableDefinition(defStored.getInfo().getPk());

			Assert.assertNotNull(mfStoredDefinition);
			Assert.assertNotNull(mfStoredDefinition.getMetaDataRef());
			Assert.assertNotNull(mfStoredDefinition.getVersion());
			Assert.assertEquals(lookupDef.getFields(), mfStoredDefinition.getFields());

			List<LookupTableDTO> listOfLookups = service.listAvailableLookupTables(defaultApp);
			Assert.assertEquals(1, listOfLookups.size());
		} catch (LookupTableDefinitionException e) {
			// this is a fail since we are expecting that this lookup table
			// doesn't exists
			Assert.fail(e.getMessage());
		}

		List<LookupTableDTO> listOfLookups = service.listAvailableLookupTables(defaultApp, lookupDef.getInfo()
				.getIdentifier());
		Assert.assertEquals(1, listOfLookups.size());

		// We are going to check that it is not possible to store a lookup table
		// with the same identifier
		try {
			defStored = service.createLookupTable(defaultApp, user, lookupDef);
			Assert.fail("We were expecting to receive a " + LookupTableDefinitionException.class.getName()
					+ ", but we didn't get any exception");
		} catch (LookupTableDefinitionException e) {
			// this isn't a fail, since we are actually expected this to happen

		}

	}
}
