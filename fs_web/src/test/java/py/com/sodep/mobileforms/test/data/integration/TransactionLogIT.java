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
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.api.services.data.Transaction.TX_GLOBAL_STATE;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

import com.mongodb.DB;

/**
 * This test will insert some data and then check the log of the transactions to
 * see if the log match the transactions that we have generated
 * 
 * @author danicricco
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TransactionLogIT {

	@Autowired
	private IDataAccessServiceMock service;

	@Autowired
	private TransactionManager txManager;

	@Before
	public void postConstruct() {
		service.dropDatabase();
	}

	/**
	 * Insert to batch of data and check that the transactions fields are as
	 * expected
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertAndCheckTransaction() throws InterruptedException {
		Long numberOfDataOnTx1 = 5l, numberOfDataOnTx2 = 10l;

		DB db = service.getMongoConnection();
		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		MFDataSetDefinitionMongo ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);

		ArrayList<MFIncomingDataBasic> tx1Data = new ArrayList<MFIncomingDataBasic>();
		// insert data for the tx1
		for (int i = 0; i < numberOfDataOnTx1; i++) {

			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", "" + i);
			tx1Data.add(new MFIncomingDataBasic(i, map));
		}

		@SuppressWarnings("unused")
		StoreResult storedData = service.storeData(ddl.getMetaDataRef(), ddl.getVersion(), tx1Data, true, true);

		// insert data for the tx2
		ArrayList<MFIncomingDataBasic> tx2Data = new ArrayList<MFIncomingDataBasic>();

		for (int i = 0; i < numberOfDataOnTx2; i++) {

			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", "" + i);
			tx2Data.add(new MFIncomingDataBasic(i, map));
		}
		storedData = service.storeData(ddl.getMetaDataRef(), ddl.getVersion(), tx2Data, true, true);

		List<Transaction> transactions = txManager.getAllDoneTransactions(db, ddl);
		Assert.assertEquals(2, transactions.size());
		// check the expected data on tx1
		Transaction tx1 = transactions.get(0);
		Assert.assertEquals(TX_GLOBAL_STATE.DONE, tx1.getState());
		Assert.assertEquals(OPERATION.INSERT, tx1.getType());
		Assert.assertEquals(new Long(0), tx1.getRowStart());
		Assert.assertEquals(new Long(numberOfDataOnTx1 - 1), tx1.getRowEnd());

		// check the expected data on tx2
		Transaction tx2 = transactions.get(1);
		Assert.assertEquals(TX_GLOBAL_STATE.DONE, tx2.getState());
		Assert.assertEquals(OPERATION.INSERT, tx2.getType());
		Assert.assertEquals(numberOfDataOnTx1, tx2.getRowStart());
		Assert.assertEquals(new Long(numberOfDataOnTx1 + numberOfDataOnTx2 - 1), tx2.getRowEnd());

	}

	/**
	 * Insert data and check that it can be obtained back from the transaction
	 * information
	 * 
	 * @throws InterruptedException
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertAndReproduceData() throws InterruptedException {

		Long numberOfdata = 5l;
		int batchSize = 2;

		DB db = service.getMongoConnection();
		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		MFDataSetDefinitionMongo ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);

		ArrayList<MFIncomingDataBasic> tx1Data = new ArrayList<MFIncomingDataBasic>();
		// insert data for the tx1
		for (int i = 0; i < numberOfdata; i++) {

			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", "" + i);
			tx1Data.add(new MFIncomingDataBasic(i, map));
		}
		@SuppressWarnings("unused")
		StoreResult storedData = service.storeData(ddl.getMetaDataRef(), ddl.getVersion(), tx1Data, true, true);

		List<Transaction> transactions = txManager.getAllDoneTransactions(db, ddl);
		Assert.assertEquals(1, transactions.size());
		Transaction tx = transactions.get(0);
		long currentRow = tx.getRowStart();

		ArrayList<MFManagedData> l2 = new ArrayList<MFManagedData>();
		while (currentRow <= tx.getRowEnd()) {
			List<MFManagedData> dataList = service.listDataOfTx(tx, currentRow, batchSize,true);
			if (dataList.size() > batchSize) {
				Assert.fail("The method listDataOfTx returned more data than asked");
			}
			l2.addAll(dataList);
			currentRow += batchSize;
		}

		Assert.assertEquals(tx1Data.size(), l2.size());
		for (MFIncomingDataBasic row : tx1Data) {
			if (!tx1Data.contains(row)) {
				Assert.fail("The row " + row.getData().get("ID") + " was not on the returned list");
			}
		}
	}
}
