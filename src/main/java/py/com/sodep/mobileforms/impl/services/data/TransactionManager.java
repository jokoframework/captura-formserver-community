package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.api.services.data.Transaction.TX_GLOBAL_STATE;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * This class encapsulate the method to generate a transaction id during
 * insertion of data to a dataset. Transactions are unique for a host and will
 * be kept on the collection {@value #COL_TRANSACTION}
 * 
 * @author danicricco
 * 
 */
public class TransactionManager {

	private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

	public static final String COL_TRANSACTION = "mf_transactions";

	private final String hostIdentifier;

	public TransactionManager(String hostIdentifier) {
		this.hostIdentifier = hostIdentifier;
	}

	public String getHostIdentifier() {
		return hostIdentifier;
	}

	/**
	 * A transaction is generated with the pattern
	 * hostIdentifier_[dataSetId]_[milliseconds]. There is a very low
	 * probability that the same transaction id can be generated, so the
	 * algorithm will check if its active and just retry for
	 * MAX_TRANSACTION_GENERATION_LOOP times. If its unable to generate a
	 * transaction, a {@link MFTransactionBlockException} will be thrown.
	 * 
	 * @param db
	 * @param dataSetId
	 * @return
	 */
	public Transaction startTransaction(DB db, MFDataSetDefinitionMongo def, OPERATION operationType) {
		if (def == null || def.getObjectId() == null) {
			throw new MFDataAccessException("Can't start a transaction without a registered ddl");
		}
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		collection.setWriteConcern(WriteConcern.SAFE);
		Transaction t = new Transaction();
		t.setHostIdentifier(hostIdentifier);
		t.setDataSetDefinitionId(def.getObjectId());
		t.setMetaDataSetId(def.getMetaDataRef());
		t.setStartTime(new Date());
		t.setType(operationType);
		t.setState(TX_GLOBAL_STATE.PENDING);
		Long revision = DataDefinitionService.incrementRevisionNumber(db, def.getMetaDataRef());
		t.setOrder(revision);
		DBObject mongoObj = t.toMongo();
		collection.insert(mongoObj);

		Transaction t2 = new Transaction();
		t2.fromMongo(mongoObj);
		return t2;
		// logger.error("Couldn't generate a unique transaction");
		// throw new MFTransactionBlockException();

	}

	/**
	 * 
	 * The method should be called only from a safe context, such as at startup.
	 * The method will iterate over all transactions, and do the following: If
	 * the transaction is PENDING or ROLLINB_BACK then rollback it If the
	 * transaction is commiting, finish the commit
	 */
	public void processActiveTransactions(DB db) {
		logger.debug("Process Active transactions of host " + this.hostIdentifier);
		// An active transaction is basically a transaction that is not on state
		// DONE
		DBObject query = BasicDBObjectBuilder
				.start()
				.add(Transaction.FIELD_HOST_IDENTIFIER, this.hostIdentifier)
				.add(Transaction.FIELD_STATE,
						BasicDBObjectBuilder.start().add("$ne", TX_GLOBAL_STATE.DONE.ordinal()).get()).get();
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		DBCursor cursor = collection.find(query);
		while (cursor.hasNext()) {
			DBObject data = cursor.next();
			Transaction tx = new Transaction();
			tx.fromMongo(data);
			if (tx.getState().equals(TX_GLOBAL_STATE.COMMITING)) {
				logger.info("Self healing. Commit tx " + tx.getDataSetDefinitionId());
				commit(db, tx, null);
				markTransactionAsDone(db, tx);
			} else {
				logger.info("Self healing. Rollback tx " + tx.getDataSetDefinitionId());
				rollback(db, tx);
				cleanTransaction(db, tx);
			}

		}

	}

	/**
	 * This is a method for backwards compatibility at the time we didn't
	 * support two phase commit. All data without tx_state information will be
	 * considered committed
	 */
	public void commitDataWithoutTxInformation(DB db, String metadaSetId) {
		DBCollection collection = db.getCollection(DataDefinitionService.getCollectionOfMetadataRef(metadaSetId));
		// update({"tx_state":{"$exists":false}},{"$set":{"tx_state":1}});
		DBObject query = BasicDBObjectBuilder.start()
				.add(DataContainer.FIELD_TXSTATE, BasicDBObjectBuilder.start().add("$exists", false).get()).get();
		DBObject update = BasicDBObjectBuilder
				.start()
				.add("$set",
						BasicDBObjectBuilder.start()
								.add(DataContainer.FIELD_TXSTATE, DataContainer.TX_STATE.CONFIRMED.ordinal()).get())
				.get();
		WriteResult writeResult = collection.update(query, update, false, true);
		if (writeResult.getN() > 0) {
			logger.info("Upgrading data to support two phase commit. DataSet " + metadaSetId
					+ " , upgraded performed: " + writeResult.getN());
		}
	}

	/**
	 * Get a transaction object or throws an {@link MFDataAccessException} if
	 * the object doesn't exists
	 * 
	 * @param db
	 * @param tx
	 * @return
	 */
	public Transaction getTransaction(DB db, String tx) {
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		DBObject query = BasicDBObjectBuilder.start().add(MFStorable._ID, new ObjectId(tx)).get();
		DBObject data = collection.findOne(query);
		if (data != null) {
			Transaction t = new Transaction();
			t.fromMongo(data);
			return t;
		} else {
			throw new MFDataAccessException("Unable to obtain " + tx + " . The object was not found");
		}
	}

	/**
	 * This method receive a transaction and returns the collection that it is
	 * affecting (or affected).
	 * 
	 * @param db
	 * @param tx
	 * @return
	 */
	public DBCollection getCollectionAffectedByTransaction(DB db, Transaction tx) {
		// get the object MFDataSetDefinitionMongo that correspond to the
		// transaction
		MFDataSetDefinitionMongo ddl = DataDefinitionService.getDataSetDefinition(db, tx.getDataSetDefinitionId());
		// get the collection were we store all data from the metadataRef
		DBCollection collection = db.getCollection(DataDefinitionService.getCollectionOfMetadataRef(ddl
				.getMetaDataRef()));
		return collection;
	}

	public void commit(DB db, Transaction tx, Integer nExpected) {

		DBCollection collection = getCollectionAffectedByTransaction(db, tx);
		// update({"tx":"t1_505ce6af3004406d320c6afa_1348265648125"},{"$set":{"tx_state":0},"$unset":{"tx":1}},false,true);
		DBObject query = BasicDBObjectBuilder.start().add(DataContainer.FIELD_TX, tx.getId()).get();
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		DBObject set = BasicDBObjectBuilder
				.start()
				.add("$set",
						BasicDBObjectBuilder.start()
								.add(DataContainer.FIELD_TXSTATE, DataContainer.TX_STATE.CONFIRMED.ordinal()).get())
				.get();

		WriteResult writeResult = collection.update(query, set, false, true);
		int n = writeResult.getN();
		if (nExpected != null && !nExpected.equals(n)) {
			String msg = "The transaction " + tx + " has inserted " + nExpected + ", but  " + n + " were commited";
			logger.error(msg);
			throw new MFDataAccessException(msg);
		}

	}

	public void rollback(DB db, Transaction tx) {

		DBCollection collection = getCollectionAffectedByTransaction(db, tx);
		DBObject query = BasicDBObjectBuilder.start().add(DataContainer.FIELD_TX, tx.getId()).get();
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		WriteResult writeResult = collection.remove(query);
		int n = writeResult.getN();
		logger.debug("The transaction " + tx + " was marked to roolback and " + n + " uncommited register were deleted");

	}

	/**
	 * This method should be called after a rollback to delete the transaction
	 * itself
	 * 
	 * @param db
	 * @param tx
	 */
	public void cleanTransaction(DB db, Transaction tx) {
		logger.trace("Releasing transaction " + tx.getId());
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		collection.setWriteConcern(WriteConcern.SAFE);
		WriteResult writeResult = collection.remove(tx.toMongo());
		logger.trace("Removed tx" + tx.getId() + ". Number of objects removed =" + writeResult.getN() + " objects");
	}

	public int countTransactions(DB db) {
		DBObject query = BasicDBObjectBuilder.start().add(Transaction.FIELD_HOST_IDENTIFIER, this.hostIdentifier).get();
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		return collection.find(query).count();
	}

	public Transaction markTransactionAsCommiting(DB db, Transaction tx, long startRow, long endRow) {
		if (tx.getState().equals(TX_GLOBAL_STATE.PENDING)) {
			DBCollection collection = db.getCollection(COL_TRANSACTION);
			collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
			DBObject oldTx = tx.toMongo();
			tx.setState(TX_GLOBAL_STATE.COMMITING);

			tx.setRowStart(startRow);
			tx.setRowEnd(endRow);

			tx.setEndTime(new Date());
			DBObject newObj = tx.toMongo();
			collection.update(oldTx, newObj);

			Transaction t2 = new Transaction();
			t2.fromMongo(newObj);
			return t2;
		} else {
			throw new IllegalStateException("Can't advance a transaction from " + tx.getState() + " to "
					+ TX_GLOBAL_STATE.COMMITING);
		}
	}

	public Transaction markTransactionAsDone(DB db, Transaction tx) {
		if (tx.getState().equals(TX_GLOBAL_STATE.COMMITING)) {
			DBCollection collection = db.getCollection(COL_TRANSACTION);
			collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
			DBObject oldTx = tx.toMongo();
			tx.setState(TX_GLOBAL_STATE.DONE);
			DBObject newObj = tx.toMongo();
			collection.update(oldTx, newObj);

			Transaction t2 = new Transaction();
			t2.fromMongo(newObj);
			return t2;
		} else {
			throw new IllegalStateException("Can't advance a transaction from " + tx.getState() + " to "
					+ TX_GLOBAL_STATE.DONE);
		}
	}

	public Transaction markTransactionAsRollingBack(DB db, Transaction tx) {
		// we are not controlling here that the transaction is in state PENDING
		// on purpose. There shouldn't be any error after commiting data, but if
		// there is an unexpected error this will allow as to rollback the data
		// and at least keep the database in a consistent state
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		DBObject oldTx = tx.toMongo();
		tx.setState(TX_GLOBAL_STATE.ROLLING_BACK);
		DBObject newObj = tx.toMongo();
		collection.update(oldTx, tx.toMongo());
		Transaction t2 = new Transaction();
		t2.fromMongo(newObj);
		return t2;

	}

	public List<Transaction> getAllDoneTransactions(DB db, MFDataSetDefinitionMongo ddl) {
		return getAllDoneTransactions(db, ddl, null);
	}

	public List<Transaction> getAllDoneTransactions(DB db, MFDataSetDefinitionMongo ddl, Integer numberOfRows) {
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		// FIXME We don't need to depend depend on receiving a
		// MFDataSetDefinitionMongo
		DBObject query = BasicDBObjectBuilder.start().add(Transaction.FIELD_DATASET_DEFINITION, ddl.getObjectId())
				.add(Transaction.FIELD_STATE, TX_GLOBAL_STATE.DONE.ordinal()).get();
		DBCursor cursor = collection.find(query);
		if (numberOfRows != null) {
			cursor = cursor.limit(numberOfRows);
		}
		cursor = cursor.sort(BasicDBObjectBuilder.start().add(Transaction.FIELD_ORDER, 1).get());
		ArrayList<Transaction> list = new ArrayList<Transaction>();
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			Transaction t = new Transaction();
			t.fromMongo(obj);
			list.add(t);
		}
		return list;
	}

	public Transaction getNextTransaction(DB db, String txId) {
		Transaction tx = getTransaction(db, txId);

		DBCollection collection = db.getCollection(COL_TRANSACTION);
		DBObject query = BasicDBObjectBuilder.start()
				.add(Transaction.FIELD_DATASET_DEFINITION, tx.getDataSetDefinitionId())
				.add(Transaction.FIELD_STATE, Transaction.TX_GLOBAL_STATE.DONE.ordinal())
				.add(Transaction.FIELD_ORDER, BasicDBObjectBuilder.start().add("$gt", tx.getOrder()).get()).get();
		DBObject obj = collection.findOne(query);
		if (obj != null) {
			Transaction tN = new Transaction();
			tN.fromMongo(obj);
			return tN;
		}
		return null;

	}

	public Transaction getFirstTransaction(DB db, MFDataSetDefinitionMongo ddl) {
		List<Transaction> transactions = getAllDoneTransactions(db, ddl, 1);
		if (transactions.size() > 0) {
			return transactions.get(0);
		}
		return null;
	}

	public void createIndexes(DB db) {
		DBCollection collection = db.getCollection(COL_TRANSACTION);
		collection.createIndex(BasicDBObjectBuilder.start().add(Transaction.FIELD_DATASET_DEFINITION, 1).get());

	}
}
