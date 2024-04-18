package py.com.sodep.mobileforms.test.data.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.impl.services.data.DataContainer;
import py.com.sodep.mobileforms.impl.services.data.DataContainer.TX_STATE;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionService;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * This test simulates that some data were neither commit nor rollback and run
 * the self-healing process to check if everything goes back to normal
 * 
 * @author danicricco
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TransactionSelfHealingIT {

	@Autowired
	private IDataAccessServiceMock service;

	@Autowired
	private TransactionManager txManager;

	/**
	 * 1. Insert and commit data on mongo. 
	 * 2. Put two rows to pending.
	 * 3. Compare all data number with pending data
	 * 4. Rolled back pending transactions
	 * 5. And, finally, compare them again
	 * @throws InterruptedException
	 */
	@Test
	public void testSelfHealingTx() throws InterruptedException {
		MFDataSetDefinitionMongo ddl = DataAccessCommitAndRollbackIT.insert(service, txManager, false, 0);
		// at this point we have data and everything has been commited. Now we
		// need to simulate that there are some pending transactions
		DB db = service.getMongoConnection();
		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);

		// access directly to the databag to simulate that some data were
		// neither commit nor rollback
		DBCollection col = db.getCollection(DataDefinitionService.getCollectionOfMetadataRef(ddl.getMetaDataRef()));
		int numberOfPendingData = 2;
		DBCursor cursor = col.find();
		int i = 0;
		while (cursor.hasNext() && i < numberOfPendingData) {
			DBObject obj = cursor.next();
			DataContainer data = new DataContainer();
			data.fromMongo(obj);
			data.setTx(tx.getId());
			data.setTxState(TX_STATE.PENDING);
			col.update(obj, data.toMongo());
			i++;
		}
		List<MFManagedData> rows = service.listAllData(ddl.getMetaDataRef(), ddl.getVersion(), null);
		// the expected number of data is the total data inserted minus the
		// number of data that we have marked as "pending"
		int numberOfInsertedData = DataAccessCommitAndRollbackIT.numberOfData();
		int expectedNumberOfData = numberOfInsertedData - numberOfPendingData;
		Assert.assertEquals(expectedNumberOfData, rows.size());

		// Although the data was not returned by calling the method listAllData
		// we check with the following query that the rows are in the mongo DB
		int dataBagSize = col.find().count();
		Assert.assertEquals(numberOfInsertedData, dataBagSize);

		// commit what needs to be commited or rollback. In this case it will be
		// rolled back
		txManager.processActiveTransactions(db);

		// check that the data non commited data are not comming (same as
		// before)
		rows = service.listAllData(ddl.getMetaDataRef(), ddl.getVersion(), null);
		Assert.assertEquals(expectedNumberOfData, rows.size());

		// Note that the data on the databag doesn't longer exist.
		dataBagSize = col.find().count();
		Assert.assertEquals(expectedNumberOfData, dataBagSize);

	}
}
