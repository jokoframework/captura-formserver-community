package py.com.sodep.mobileforms.test.data.integration;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionReport;
import py.com.sodep.mobileforms.impl.services.data.DataSetDeleteReport;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

/**
 * This class test dataset creation on lookuptable, 
 * insert data and check that. Also, delete data and 
 * check correct operation
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DataAccessDefinitionAndInsertIT {

	@Autowired
	private IDataAccessServiceMock service;
	private final String countries[] = new String[] { "Paraguay", "Brasil", "Argentina", "Uruguay", "Chile" };

	/**
	 * This test will define a new dataSet and then check that the structure on
	 * the mongo DB was correctly created
	 * 
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void defineDataSetAndCheck() throws InterruptedException {

		// Define the dataSEt
		MFDataSetDefinition ddl = LookupTableIT.getDataSetDef();
		ddl = service.define(ddl);

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		DataDefinitionReport dataSetReport = service.getDataSetReport(ddl.getMetaDataRef());
		Assert.assertEquals(1, dataSetReport.getDataSet());
		Assert.assertEquals(1, dataSetReport.getDdls());
		// Originally the dataBag was lazily created with the first insert, so
		// zero was expected from dataSetReport.getDataBag().
		// After adding the capacity of defining a pk the databag is created the
		// first time if we have defined at least one PK. Since we haven't
		// defined any PK we are expecting zero now
		// danicricco (11/07/2012): Now this is again back to "1" since an index
		// over the transaction_id is createdo on every databag
		Assert.assertEquals(1, dataSetReport.getDataBag());
		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}

		service.storeData(ddl.getMetaDataRef(), 0l, rows, true, true);
		dataSetReport = service.getDataSetReport(ddl.getMetaDataRef());
		Assert.assertEquals(1, dataSetReport.getDataSet());
		Assert.assertEquals(1, dataSetReport.getDdls());
		// the databag should exist, because we have already inserted data
		Assert.assertEquals(1, dataSetReport.getDataBag());

		DataSetDeleteReport deleteDataSet = service.deleteDataSet(ddl.getMetaDataRef());
		// Check that the delete report has in fact delete the expected data
		Assert.assertEquals(1, deleteDataSet.getDataSet());
		Assert.assertEquals(1, deleteDataSet.getDdls());
		Assert.assertEquals(1, deleteDataSet.getDataBag());

		// check again that the data no longer exist
		dataSetReport = service.getDataSetReport(ddl.getMetaDataRef());
		Assert.assertEquals(0, dataSetReport.getDataSet());
		Assert.assertEquals(0, dataSetReport.getDdls());
		Assert.assertEquals(0, dataSetReport.getDataBag());
	}
}
