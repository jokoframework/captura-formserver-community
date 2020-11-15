package py.com.sodep.mobileforms.test.data.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFManagedDataBasic;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

/**
 * This class test data insertion in 
 * single or multiple transactions. Also,
 * test getting all data or by batch 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DataTrackingIT {

	@Autowired
	private IDataAccessServiceMock service;

	@Autowired
	private SynchronizationService synchService;

	@Before
	public void postConstruct() {
		service.dropDatabase();
	}

	@Test
	public void testDummy() {

	}

	/**
	 * Insert some register in a single transaction and query them with a size
	 * lower than the number of inserted data
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void insertAndCheckSingleTransactionSmallBatch() throws InterruptedException {

		int numberOfRowsOnTx1 = 10;

		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		MFDataSetDefinitionMongo ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);
		// insert 10 registers
		insertTestData(ddl.getMetaDataRef(), ddl.getVersion(), 0, numberOfRowsOnTx1);

		int firstBatchSize = 2;
		MFDMLTransport dmlTransport = synchService.downloadData(ddl, null, null, firstBatchSize);
		Assert.assertNotNull(dmlTransport);
		Assert.assertFalse(dmlTransport.isFinal());
		Assert.assertFalse(dmlTransport.isSynch());
		List<MFManagedDataBasic> data = dmlTransport.getData();
		Assert.assertNotNull(data);
		Assert.assertEquals(firstBatchSize, data.size());

		TXInfo txInfo = dmlTransport.getTxInfo();
		Assert.assertNotNull(txInfo);
		Assert.assertNotNull(txInfo.getTx());
		Assert.assertEquals(OPERATION.INSERT, txInfo.getOperation());
		// the first row is zero because we have created a new dataSet and we
		// are sure that there are no other insert transaction running at the
		// same time. The same holds for the endRow
		Assert.assertEquals(new Long(0), txInfo.getStartRow());
		Assert.assertEquals(new Long(1), txInfo.getEndRow());

		dmlTransport = synchService.downloadData(ddl, txInfo.getTx(), txInfo.getEndRow(), numberOfRowsOnTx1
				- firstBatchSize);
		Assert.assertNotNull(dmlTransport);
		Assert.assertTrue(dmlTransport.isFinal());
		Assert.assertFalse(dmlTransport.isSynch());
		txInfo = dmlTransport.getTxInfo();
		Assert.assertEquals(new Long(firstBatchSize), txInfo.getStartRow());
		Assert.assertEquals(new Long(numberOfRowsOnTx1 - 1), txInfo.getEndRow());
		data = dmlTransport.getData();
		Assert.assertNotNull(data);
		Assert.assertEquals(numberOfRowsOnTx1 - firstBatchSize, data.size());

		dmlTransport = synchService.downloadData(ddl, txInfo.getTx(), txInfo.getEndRow(), numberOfRowsOnTx1);
		Assert.assertNotNull(dmlTransport);
		Assert.assertTrue(dmlTransport.isSynch());
		Assert.assertNull(dmlTransport.getData());
	}
	
	/**
	 * Insert some register in multiple transactions and query them with a size
	 * equal than the number of inserted data
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void insertAndCheckMultipleTransactions() throws InterruptedException {

		int numberOfRowsPerTransaction = 10;

		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		MFDataSetDefinitionMongo ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);
		// insert data on two separate transactions
		insertTestData(ddl.getMetaDataRef(), ddl.getVersion(), 0, numberOfRowsPerTransaction);
		insertTestData(ddl.getMetaDataRef(), ddl.getVersion(), numberOfRowsPerTransaction - 1,
				numberOfRowsPerTransaction);

		MFDMLTransport dmlTransport = synchService.downloadData(ddl, null, null, numberOfRowsPerTransaction);
		TXInfo tx1Info = dmlTransport.getTxInfo();
		Assert.assertTrue(dmlTransport.isFinal());
		Assert.assertFalse(dmlTransport.isSynch());

		// with the next download we should move to the next transaction
		dmlTransport = synchService.downloadData(ddl, tx1Info.getTx(), tx1Info.getEndRow(), numberOfRowsPerTransaction);
		TXInfo tx2Info = dmlTransport.getTxInfo();
		Assert.assertTrue(dmlTransport.isFinal());
		Assert.assertFalse(dmlTransport.isSynch());
		if (tx1Info.getTx().equals(tx2Info.getTx())) {
			// we were expecting to move to data from the next transaction, but
			// we have remained in the same place
			Assert.fail("Expected data from the next transaction");
		}

		dmlTransport = synchService.downloadData(ddl, tx2Info.getTx(), tx2Info.getEndRow(), numberOfRowsPerTransaction);
		Assert.assertNotNull(dmlTransport);
		Assert.assertTrue(dmlTransport.isSynch());
		Assert.assertNull(dmlTransport.getData());
	}

	@SuppressWarnings("unchecked")
	public MFOperationResult insertTestData(String metaDataRef, Long version, int startI, int length)
			throws InterruptedException {
		ArrayList<MFIncomingDataBasic> tx1Data = new ArrayList<MFIncomingDataBasic>();
		// insert data for the tx1
		for (int i = startI; i < startI + length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", "" + i);
			tx1Data.add(new MFIncomingDataBasic(i, map));
		}
		StoreResult storedData = service.storeData(metaDataRef, version, tx1Data, true, true);
		return storedData.getMfOperationResult();
	}
}
