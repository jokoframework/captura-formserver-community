package py.com.sodep.mobileforms.test.data.integration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
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
import py.com.sodep.mobileforms.impl.services.data.DataInsertUnexpecteException;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

import com.mongodb.DB;

/**
 * This class test transactions on mongo, to insert data and commit or
 * rollback them
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DataAccessCommitAndRollbackIT {

	private static final Logger logger = Logger.getLogger(DataAccessCommitAndRollbackIT.class);

	@Autowired
	private IDataAccessServiceMock service;

	@Autowired
	private TransactionManager txManager;

	private static final String countries[] = new String[] { "Paraguay", "Brasil", "Argentina", "Uruguay", "Chile" };

	public static int numberOfData() {
		return countries.length;
	}

	public DataAccessCommitAndRollbackIT() {
		URL confURL = DataAccessCommitAndRollbackIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Before
	public void postConstruct() {
		service.dropDatabase();
	}

	/**
	 * Insert data and commit them
	 * @throws InterruptedException
	 */
	@Test
	public void insertAndCommit() throws InterruptedException {
		insert(service, txManager, false, 3);
	}
	
	/**
	 * Insert data and rollback them
	 * @throws InterruptedException
	 */
	@Test
	public void insertAndRollback() throws InterruptedException {
		insert(service, txManager, true, 3);
	}

	/**
	 * Insert the data of countries and rollback or commit depending on the
	 * parameter rollback. If rollback is true will check that no data were
	 * left. If rollback is false will check that the number of commited data
	 * match the number of inserted data. Finally it will check that no
	 * transaction has left, no matter if rollback is true or false
	 * 
	 * @param rollback
	 * @param rowToFail
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public static MFDataSetDefinitionMongo insert(IDataAccessServiceMock service, TransactionManager txManager,
			boolean rollback, int rowToFail) throws InterruptedException {

		service.setFail(rollback);
		service.setRowToFail(rowToFail);

		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		MFDataSetDefinitionMongo ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);

		ArrayList<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		for (int i = 0; i < countries.length; i++) {
			@SuppressWarnings("rawtypes")
			HashMap map = new HashMap();
			map.put("ID", new Integer(i));
			map.put("name", countries[i]);
			rows.add(new MFIncomingDataBasic(i, map));
		}
		try {
			StoreResult storedData = service.storeData(ddl.getMetaDataRef(), 0l, rows, true,true);

			if (rollback) {
				Assert.fail("Rollback was expected but the data was commited");
			}
			if (storedData.hasSucceeded()) {
				logger.debug("insert of data has success");
				List<MFManagedData> data = service.listAllData(ddl.getMetaDataRef(), ddl.getVersion(), null);
				Assert.assertEquals(countries.length, data.size());

				List<Transaction> listTransaction = service.getDoneTransactions(ddl);
				Assert.assertEquals(1, listTransaction.size());
				Transaction t = listTransaction.get(0);
				Assert.assertEquals(OPERATION.INSERT, t.getType());
				Assert.assertEquals(TX_GLOBAL_STATE.DONE, t.getState());
				Assert.assertEquals(txManager.getHostIdentifier(), t.getHostIdentifier());
			} else {
				Assert.fail("Data check has failed, but it was expected to success or rollback");
			}

		} catch (DataInsertUnexpecteException e) {
			if (!rollback) {
				Assert.fail("No rollback was expected, but an uxpecpected exception happened. Exception cause: "
						+ e.getCause());
			}
			List<MFManagedData> data = service.listAllData(ddl.getMetaDataRef(), ddl.getVersion(), null);
			Assert.assertEquals(0, data.size());

			// check that there are no pending transactions
			DB db = service.getMongoConnection();
			int numberOfTx = txManager.countTransactions(db);
			Assert.assertEquals(0, numberOfTx);

		}

		service.setFail(false);
		return ddl;
	}
}
