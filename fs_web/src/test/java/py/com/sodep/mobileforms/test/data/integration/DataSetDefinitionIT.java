package py.com.sodep.mobileforms.test.data.integration;

import junit.framework.Assert;

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
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

/**
 * This class test dataset definition on mongo
 * and getting that later
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DataSetDefinitionIT {

	@Autowired
	private IDataAccessServiceMock service;
	
	/**
	 * Create and save dataset on mongo, getting that later
	 */
	@Test
	public void testDataSetDefAndGet() {
		MFDataSetDefinition def = new MFDataSetDefinition();
		def.addField(new MFField(FIELD_TYPE.NUMBER, "ID"));
		def.addField(new MFField(FIELD_TYPE.STRING, "name"));
		def.addField(new MFField(FIELD_TYPE.DATE, "birthdate"));
		def.addField(new MFField(FIELD_TYPE.BOOLEAN, "b"));
		def = service.define(def);

		MFDataSetDefinitionMongo def2 = service.getDataSetDefinition(def.getMetaDataRef(), def.getVersion());
		Assert.assertEquals(def.getFields(), def2.getFields());
	}
}
