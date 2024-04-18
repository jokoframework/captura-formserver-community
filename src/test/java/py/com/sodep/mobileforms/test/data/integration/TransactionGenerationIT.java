package py.com.sodep.mobileforms.test.data.integration;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import junit.framework.Assert;
import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableModificationRequest.OPERATION_TYPE;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.api.services.data.Transaction.TX_GLOBAL_STATE;
import py.com.sodep.mobileforms.impl.services.data.InsertReport;
import py.com.sodep.mobileforms.impl.services.data.MFDataAccessException;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;
import py.com.sodep.mobileforms.test.services.IDataAccessServiceMock;

/**
 * This is a test to check the operation performed by the class
 * {@link TransactionManager} The test will always delete the content of the
 * transactions before starting
 * 
 * @author danicricco
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TransactionGenerationIT {

	private Mongo mongo;
	private String database;
	private int port;
	private String host;
	private String hostIdentifier = "h1";

	@Autowired
	private IDataAccessServiceMock service;
	private TransactionManager txManager = new TransactionManager(hostIdentifier);

	private MFDataSetDefinitionMongo ddl;
	private String user;
	private String pwd;
	private boolean useAuthentication;

	public TransactionGenerationIT() {
		URL confURL = TransactionGenerationIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}

	@Value("${mongo.database}")
	public void setDatabase(String database) {
		this.database = database;
	}

	@Value("${mongo.port}")
	public void setPort(int port) {
		this.port = port;
	}

	@Value("${mongo.host}")
	public void setHost(String host) {
		this.host = host;
	}
	
	@Value("${mongo.user}")
	public void setUser(String user) {
		this.user = user;
	}
	
	@Value("${mongo.pwd}")
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Value("${mongo.useAuthentication:false}")
	public void setUseAuthentication(boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
	}


	@Before
	public void preparation() throws UnknownHostException, MongoException {
		if (this.useAuthentication) {
			MongoCredential credential = MongoCredential.createScramSha1Credential(user, database, pwd.toCharArray());
			mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
		} else {
			mongo = new MongoClient(new ServerAddress(host, port));
		}
		DB db = mongo.getDB(database);
		db.dropDatabase();

		MFDataSetDefinition ddlTmp = LookupTableIT.getDataSetDef();
		ddl = (MFDataSetDefinitionMongo) service.define(ddlTmp);

	}

	/**
	 * Call the {@link TransactionManager} twice to start transactions and check
	 * that the number are different. Although this doesn't fully prove that the
	 * {@link TransactionManager} is not producing duplicate transaction ids, it
	 * is a straightforward test
	 */
	@Test
	public void testTransactionUniqueness() {
		Assert.assertNotNull(mongo);

		// generate a transaction

		DB db = mongo.getDB(database);

		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);
		Assert.assertNotNull(tx);
		Assert.assertNotNull(tx.getHostIdentifier());
		Assert.assertNotNull(tx.getStartTime());
		Assert.assertNotNull(tx.getDataSetDefinitionId());
		Assert.assertNotNull(tx.getOrder());
		Assert.assertEquals(TX_GLOBAL_STATE.PENDING, tx.getState());

		String tId = tx.getId();

		// Since we have delete the transaction for this host during the test
		// startup, it is expected that only one transaction is in pending state
		int count = txManager.countTransactions(db);
		Assert.assertEquals(1, count);
		Transaction tx2 = txManager.startTransaction(db, ddl, OPERATION.INSERT);
		Assert.assertNotNull(tx2);
		Assert.assertNotNull(tx2.getHostIdentifier());
		Assert.assertNotNull(tx2.getStartTime());
		Assert.assertNotNull(tx2.getDataSetDefinitionId());
		Assert.assertNotNull(tx2.getOrder());
		Assert.assertEquals(TX_GLOBAL_STATE.PENDING, tx2.getState());

		String tId2 = tx2.getId();
		Assert.assertTrue("Duplicate transaction generated", !tId.equals(tId2));

		count = txManager.countTransactions(db);
		Assert.assertEquals(2, count);
	}

	/**
	 * Test a transaction that after calling the method
	 * {@link TransactionManager#markTransactionAsCommiting(DB, Transaction, InsertReport)}
	 * the transaction change its status. It also checks for the expected not
	 * null fields after calling the methods
	 * {@link TransactionManager#startTransaction(DB, String, OPERATION_TYPE)}
	 */
	@Test
	public void testTransactionCommiting() {

		// generate a transaction
		DB db = mongo.getDB(database);

		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);

		InsertReport report = new InsertReport(4l, 10l);
		Transaction txP = txManager.markTransactionAsCommiting(db, tx, report.getSequenceStart(),
				report.getSequenceEnd());

		Transaction txC = txManager.getTransaction(db, tx.getId());
		// we are talking about the same transaction that we have just moved
		// from state
		Assert.assertEquals(tx.getId(), txC.getId());
		Assert.assertEquals(report.getSequenceStart(), txC.getRowStart());
		Assert.assertEquals(report.getSequenceEnd(), txC.getRowEnd());
		Assert.assertEquals(TX_GLOBAL_STATE.COMMITING, txC.getState());
		Assert.assertEquals(txC, txP);
	}

	/**
	 * Test that it is valid to move a transaction from state
	 * {@link TX_GLOBAL_STATE#COMMITING} to {@link TX_GLOBAL_STATE#DONE}
	 */
	@Test
	public void testTransactionToDone() {
		DB db = mongo.getDB(database);
		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);

		tx = txManager.markTransactionAsCommiting(db, tx, 1, 2);
		Transaction txP = txManager.markTransactionAsDone(db, tx);
		Transaction txC = txManager.getTransaction(db, tx.getId());
		// we are talking about the same transaction that we have just moved
		// from state
		Assert.assertEquals(tx.getId(), txC.getId());
		Assert.assertEquals(TX_GLOBAL_STATE.DONE, txC.getState());
		Assert.assertEquals(txC, txP);
	}

	/**
	 * If someone is trying to move a transaction from pending to done an
	 * {@link IllegalStateException} should be thrown
	 */
	@Test
	public void testWrongPath() {
		DB db = mongo.getDB(database);
		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);
		try {
			txManager.markTransactionAsDone(db, tx);
			Assert.fail("We were expecting an IllegalArgumentException but nothing happened");
		} catch (IllegalStateException e) {
			// do nothing we are actually expecting this exception
		}
	}

	/**
	 * Verify if transaction was effectively rolled back
	 */
	@Test
	public void testTransactionToRollingBack() {
		DB db = mongo.getDB(database);
		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);
		Transaction txP = txManager.markTransactionAsRollingBack(db, tx);
		Transaction txC = txManager.getTransaction(db, tx.getId());
		// we are talking about the same transaction that we have just moved
		// from state
		Assert.assertEquals(tx.getId(), txC.getId());
		Assert.assertEquals(TX_GLOBAL_STATE.ROLLING_BACK, txC.getState());
		Assert.assertEquals(txC, txP);
	}
	
	/**
	 * Verify the transaction no longer exists after cleaning that
	 */
	@Test
	public void testCleanTransaction() {
		DB db = mongo.getDB(database);
		Transaction tx = txManager.startTransaction(db, ddl, OPERATION.INSERT);
		Transaction tx2 = txManager.getTransaction(db, tx.getId());
		Assert.assertEquals(tx, tx2);
		txManager.cleanTransaction(db, tx);
		try {
			tx2 = txManager.getTransaction(db, tx.getId());
			Assert.fail("The transaction " + tx.getId() + " has been deleted but it still exists!");
		} catch (MFDataAccessException e) {
			// do nothing we are actually expecting this expection
		}

	}
}
