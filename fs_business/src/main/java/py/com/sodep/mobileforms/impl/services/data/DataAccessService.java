package py.com.sodep.mobileforms.impl.services.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.QueryOperators;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLocationData;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.MFLocationMongo;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.impl.services.data.DataContainer.TX_STATE;

/**
 * This class handle the data stored in the mongo, either lookup tables or
 * forms.
 * <p>
 * The basic unit of data is a {@link DataSetMetadata}. A DataSetMetadata can
 * have different versions (with different {@link MFField}) , and each version
 * is stored in {@link MFDataSetDefinition}.
 * </p>
 * <p>
 * The DatSetMetada has a unique "data bag", no matter from which version, so we
 * can easily merge them in the future. The data bag is a collection generated
 * in runtime that follows this convention:
 * </p>
 * 
 * <pre>
 * mf_d_[DataSetMetadata.id]
 * </pre>
 */
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
// Since this service will never be invoked directly by the web interafce it can
// have an authorization of NONE, it should only be invoked within other
// services
public class DataAccessService implements IDataAccessService {

	private static final Logger logger = LoggerFactory.getLogger(DataAccessService.class);
	public static final int MAX_ROW_TRANSACTION = 10000;
	private String database;

	private int port;

	private String host;

	private Mongo mongo;

	protected final TransactionManager transactionManager;
	private String user;
	private String pwd;
	private boolean useAuthentication;

	public DataAccessService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
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
	
	@PostConstruct
	public void postConstruct() throws UnknownHostException, MongoException {

		MongoClientOptions options = MongoClientOptions.builder()
				.maxWaitTime(1000 * 30)
				.build();

		if (this.useAuthentication) {
			MongoCredential credential = MongoCredential.createScramSha1Credential(user, database, pwd.toCharArray());
			mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential), options);
		} else {
			mongo = new MongoClient(new ServerAddress(host, port), options);
		}
		
		DB db = mongo.getDB(database);
		
		// self healing to delete data after a crash
		logger.info("Self healing process of data");
		this.transactionManager.processActiveTransactions(db);

		createIndexes();
		// FIXME
		// scan over data that is not pointed by the postgres and release them
		// (we should call this method vacuum)

	}

	private void createIndexes() {
		DB db = mongo.getDB(database);
		this.transactionManager.createIndexes(db);
		

	}

	@Override
	public MFDataSetDefinitionMongo define(MFDataSetDefinition ddl) {
		DB db = mongo.getDB(database);
		return DataDefinitionService.define(db, ddl);
	}

	@Override
	public MFDataSetDefinitionMongo addDefinition(String metadataRef, MFDataSetDefinition ddl) {
		DB db = mongo.getDB(database);
		// we don't need to check if the metadataRef exists because the method
		// storeDDL will do it in an atomically operation
		return DataDefinitionService.storeDDL(db, metadataRef, ddl);

	}

	@Override
	public MFDataSetDefinitionMongo getDataSetDefinition(String id, Long version) {
		DB db = mongo.getDB(database);
		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, id, version);
		if (definition == null) {
			throw new MFDataAccessException("Didn't find the dataset " + id + " , version = " + version);
		}
		return definition;
	}

	/**
	 * Search on a {@link MFDataSetDefinition} the fields declared as type
	 * 
	 * @param definition
	 * @return a list of mfields' name declared as type
	 */

	private List<String> getFieldsOfType(MFDataSetDefinition definition, FIELD_TYPE type) {
		List<MFField> fields = definition.getFields();
		ArrayList<String> binFields = new ArrayList<String>();
		for (MFField field : fields) {
			logger.trace("Parsing: field[columnName=" + field.getColumnName() + ", type=" + field.getType() + "]. For type: " + type);
			if (field.getType().equals(type)) {
				binFields.add(field.getColumnName());
			}
		}
		return binFields;
	}

	/**
	 * Update a given row on a dataSet. The restriction must have a single or no
	 * document to update. If there are more documents to update then the method
	 * won't do anything
	 * 
	 * @param metadataRef
	 * @param version
	 * @param incomingData
	 * @param selector
	 * @return
	 */
	public MFOperationResult update(String metadataRef, Long version, MFIncomingDataI incomingData,
			ConditionalCriteria selector) {
		DB db = mongo.getDB(database);

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		if (definition == null) {
			throw new MFDataAccessException("Can't find DDL for data set " + metadataRef + "-" + version);
		}

		// select the documents that match the criteria
		DBCursor cursor = obtainDataCursor(metadataRef, version, selector, null);
		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);

		ArrayList<MFManagedData> machingRows = obtainList(cursor, binaryFields, locationFields);

		MFOperationResult result = new MFOperationResult();
		if (machingRows.size() == 1) {
			MFManagedData changingRow = machingRows.get(0);

			Transaction tx = transactionManager.startTransaction(db, definition, OPERATION.UPDATE);
			DBObject q = BasicDBObjectBuilder.start().add(MFDataSetDefinitionMongo._ID, changingRow.getRowId()).get();

			// this is a safe downcast because DataContainer are the
			// implementation of MFManagedData used inside DataAccessService
			DataContainer container = (DataContainer) changingRow;
			Map<String, ?> newDataMap = incomingData.getData();
			// create the new map of data
			List<MFField> fields = definition.getFields();
			HashMap<String, Object> row = new HashMap<String, Object>();
			for (MFField f : fields) {
				row.put(f.getColumnName(), newDataMap.get(f.getColumnName()));
			}
			
			// change the new map of meta
			Map<String, ?> meta = incomingData.getMeta();
			if (meta != null) {
				Map<String, Object> metaData = new HashMap<String, Object>();
				Set<String> keySet = meta.keySet();
				for(String key: keySet) {
					Object val = meta.get(key);
					metaData.put(key, val);
				}
				container.setMetaData(metaData);
			}
			
			// container.setTxState(TX_STATE.PENDING);
			// change the new set of data
			container.setUserData(row);
			container.setVersion(container.getVersion()+1);
			// Change the data on mongo
			String dataCollectionName = DataDefinitionService.getCollectionOfMetadataRef(definition.getMetaDataRef());
			DBCollection collection = db.getCollection(dataCollectionName);
			collection.update(q, container.toMongo());

			// commit the transaction
			transactionManager.markTransactionAsCommiting(db, tx, changingRow.getRowId(), changingRow.getRowId());
			// we didn't change the state of the transaction to pending so we
			// need to call "commit" with 0. Otherwise, it will fail
			transactionManager.commit(db, tx, 0);
			transactionManager.markTransactionAsDone(db, tx);
			result.setResult(RESULT.SUCCESS);
			result.setNumberOfAffectedRows(1);
		} else {
			if (machingRows.size() > 1) {
				result.setResult(RESULT.FAIL);
				result.setMsg("Updates can only be performed if the selector is over a single object");
			} else {
				// making an update to zero row is no problem
				result.setResult(RESULT.SUCCESS);
				result.setNumberOfAffectedRows(0);
			}

		}

		return result;
	}

	@Override
	public MFOperationResult updateState(String metadataRef, Long version, StateDTO stateDto, ConditionalCriteria selector) {
		DB db = mongo.getDB(database);

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		if (definition == null) {
			throw new MFDataAccessException("Can't find DDL for data set " + metadataRef + "-" + version);
		}

		// select the documents that match the criteria
		DBCursor cursor = obtainDataCursor(metadataRef, version, selector, null);
		MFOperationResult result = new MFOperationResult();
		if (cursor.count() == 1) {
			Long changingRowId = null; 
			Long changingVersion =  null;
			{
				DBObject dbObject = cursor.next();
				MFManagedData changingRow = toMFManagedData(dbObject);
				changingRowId = changingRow.getRowId();
				changingVersion = changingRow.getVersion();
			}
			
			Transaction tx = transactionManager.startTransaction(db, definition, OPERATION.UPDATE);
			DBObject query = BasicDBObjectBuilder.start().add(MFDataSetDefinitionMongo._ID, changingRowId).get();

			DBObject stateMetaObject = toMongoFromState(stateDto, changingVersion);
			
			// Change the data on mongo
			String dataCollectionName = DataDefinitionService.getCollectionOfMetadataRef(definition.getMetaDataRef());
			DBCollection collection = db.getCollection(dataCollectionName);
			BasicDBObject setQuery = new BasicDBObject();
		    setQuery.append("$set", stateMetaObject);

			collection.update(query, setQuery);


			// commit the transaction
			transactionManager.markTransactionAsCommiting(db, tx, changingRowId, changingRowId);
			// we didn't change the state of the transaction to pending so we
			// need to call "commit" with 0. Otherwise, it will fail
			transactionManager.commit(db, tx, 0);
			transactionManager.markTransactionAsDone(db, tx);
			result.setResult(RESULT.SUCCESS);
			result.setNumberOfAffectedRows(1);
		} else {
			if (cursor.count() > 1) {
				result.setResult(RESULT.FAIL);
				result.setMsg("Updates can only be performed if the selector is over a single object");
			} else {
				// making an update to zero row is no problem
				result.setResult(RESULT.SUCCESS);
				result.setNumberOfAffectedRows(0);
			}

		}

		return result;
	}

	private DBObject toMongoFromState(StateDTO stateDto, Long changingVersion) {
		BasicDBObject obj = new BasicDBObject();
		obj.put(MFStorable.FIELD_VERSION, changingVersion + 1);
		
		obj.put(MFStorable.FIELD_META + "." + MFIncominDataWorkflow.META_FIELD_STATE_ID, stateDto.getId());
		if (stateDto.getComment() != null 
				&& stateDto.getComment().trim().length() > 0) {
			obj.put(MFStorable.FIELD_META + "." + MFIncominDataWorkflow.META_FIELD_COMMENT, stateDto.getComment());
		}
		
		return obj;
	}

	private void deleteRows(DB db, String metadataRef, Long version, long startRow, long endRow) {
		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		Transaction tx = transactionManager.startTransaction(db, definition, OPERATION.DELETE);
		String colName = DataDefinitionService.getCollectionOfMetadataRef(metadataRef);
		DBCollection collection = db.getCollection(colName);

		BasicDBObjectBuilder qBuilder = BasicDBObjectBuilder.start();
		qBuilder.add(MFDataSetDefinitionMongo._ID,
				BasicDBObjectBuilder.start().add("$gte", startRow).add("$lte", endRow).get());
		collection.remove(qBuilder.get(), WriteConcern.JOURNAL_SAFE);
		transactionManager.markTransactionAsCommiting(db, tx, startRow, endRow);
		transactionManager.commit(db, tx, 0);
		transactionManager.markTransactionAsDone(db, tx);

	}

	public MFOperationResult delete(String metadataRef, Long version, ConditionalCriteria selector) {
		DB db = mongo.getDB(database);

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		if (definition == null) {
			throw new MFDataAccessException("Can't find DDL for data set " + metadataRef + "-" + version);
		}

		// select the documents that match the criteria
		DBCursor cursor = obtainDataCursor(metadataRef, version, selector, new OrderBy(MFDataSetDefinitionMongo._ID,
				true));
		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);

		ArrayList<MFManagedData> machingRows = obtainList(cursor, binaryFields, locationFields);
		long lastRow = -1;
		long startRow = -1;
		long endRow = -1;
		int i = 0;

		// we are going to search consecutive row IDs that match the criteria
		// and delete them within a tracking transaction
		// The idea is to minimize further roundtrips that devices might require
		// to check the rows to delete
		// For example: a delete that might look something similar to delete
		// from myLookup where ID>3 and ID<100;
		// will be translated into delete transactions for every consecutive row
		// identified. Note that the rows ID are always a unique and serial
		// number within the dataSet, and has nothing to do with the user's PK
		while (i < machingRows.size()) {
			MFManagedData row = machingRows.get(i);
			if (lastRow + 1 == row.getRowId() || lastRow == -1) {
				// this is either the first row or
				lastRow = row.getRowId();
				if (startRow == -1) {
					startRow = lastRow;
				}
				i++;
			} else {
				endRow = lastRow;
			}

			// delete rows
			if (endRow > 0) {
				// found a group of consecutive rows to delete
				deleteRows(db, metadataRef, version, startRow, endRow);
				lastRow = -1;
				startRow = -1;
				endRow = -1;
			}

		}

		// The last row or a group of consecutive rows at the end
		deleteRows(db, metadataRef, version, startRow, lastRow);

		MFOperationResult result = new MFOperationResult();
		result.setResult(RESULT.SUCCESS);
		result.setNumberOfAffectedRows(machingRows.size());
		return result;
	}

	@Override
	public StoreResult storeData(String metadataRef, Long version, List<? extends MFIncomingDataI> rows,
			boolean fastFail, boolean failOnDuplicate) throws InterruptedException {
		DB db = mongo.getDB(database);
		if (rows.size() > MAX_ROW_TRANSACTION) {
			throw new DataInsertUnexpecteException("Can't insert more than " + MAX_ROW_TRANSACTION
					+ " on the same transaction");
		}
		logger.trace("Getting dataset: version="+ version + ", metadataRef=" + metadataRef);
		
		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		
		if (definition == null) {
			throw new MFDataAccessException("Can't find DDL for: data set=" + metadataRef + ", version=" + version);
		}
		logger.trace("Got dataset: objectId="+ definition.getObjectId());
		
		ArrayList<RowCheckError> errors = checkErrors(rows, definition, fastFail);
		if (errors.size() > 0) {
			// There is at least one row with errors, avoid the insert
			return new StoreResult(errors);
		}

		Transaction tx = transactionManager.startTransaction(db, definition, OPERATION.INSERT);

		Throwable ex = null;
		InsertReport insertReport = null;
		try {

			insertReport = insertData(db, tx.getId(), definition, rows, failOnDuplicate);
			logger.trace("Insert report : sequenceStart=" + insertReport.getSequenceStart() + ", sequenceEnd=" + insertReport.getSequenceEnd());
			
			

			// It is important to store first the DMLTracker than commiting the
			// data. Doing the other way around might introduce phantom reads if
			// there is an unexpected error in the middle. For example, if only
			// some of the data were committed
			transactionManager.markTransactionAsCommiting(db, tx, insertReport.getSequenceStart(),
					insertReport.getSequenceEnd());
			transactionManager.commit(db, tx, insertReport.geAffectedRows());
			transactionManager.markTransactionAsDone(db, tx);

		} catch (Exception e) {
			logger.trace("Unable to insert data on " + metadataRef, e);
			
			ex = e;
			// If there is an exception it is an unexpected, because we haven't
			// create any constraint on the database, so there is no expected
			// exception
			transactionManager.markTransactionAsRollingBack(db, tx);
			transactionManager.rollback(db, tx);
			transactionManager.cleanTransaction(db, tx);

		}
		// if something went wrong before the endTransaction, the data will be
		// rolled back on the next clean

		if (ex != null) {
			logger.debug("Unable to insert data on " + metadataRef, ex);
			throw new DataInsertUnexpecteException("Unable to insert data on " + metadataRef, ex);
		}
		
		return insertReport.toStoreResult();

	}

	/**
	 * This method checks that the data on rows match the definition provided as
	 * parameter. The method will return a list with all possible errors if
	 * fastFail = false
	 * 
	 * @param rows
	 * @param definition
	 * @param fastFail
	 *            if true quick return with the first error
	 * @return
	 */
	private ArrayList<RowCheckError> checkErrors(List<? extends MFIncomingDataI> rows, MFDataSetDefinition definition,
			boolean fastFail) {
		List<MFField> fields = definition.getFields();
		// check that all rows match the definition
		ArrayList<RowCheckError> errors = new ArrayList<RowCheckError>();

		for (MFIncomingDataI row : rows) {
			List<ColumnCheckError> columnErrors = DataAccessHelper.checkFieldsConsistency(fields, row.getData());
			if (columnErrors.size() > 0) {
				// This indicates that there is an error in the row
				RowCheckError rowCheck = new RowCheckError(row.getHandle(), columnErrors);
				errors.add(rowCheck);
				if (fastFail) {
					return errors;
				}

			}

		}
		return errors;
	}

	/**
	 * Get a sequence number for the bag of a MetadataSet. This is the unique id
	 * of the inserted row
	 * 
	 * @param db
	 * @param metadataRef
	 * @return
	 */
	private Long getNextSequence(DB db, String metadataRef) {
		DBCollection colMetadata = db.getCollection(DataDefinitionService.COL_DATASET_METADATA);
		DBObject query = BasicDBObjectBuilder.start().add(MFStorable._ID, new ObjectId(metadataRef))
				.get();
		// 1) ATOMICALLY INCREMENT THE SEQUENCE
		DBObject incVersion = BasicDBObjectBuilder.start()
				.add("$inc", BasicDBObjectBuilder.start().add(DataSetMetadata.FIELD_SEQUENCE, 1).get()).get();
		DBObject mongoDBMetadata = colMetadata.findAndModify(query, incVersion);
		if (mongoDBMetadata == null) {
			throw new MFDataAccessException("Couldn't find the dataSet " + metadataRef);
		}

		DataSetMetadata meta = new DataSetMetadata();
		meta.fromMongo(mongoDBMetadata);
		return meta.getSequence();
	}

	/**
	 * This method will insert a binary data using gridFS and return an updated.
	 * The first option is the method getData of the blob, if this return null
	 * the stream will be used {@link MFBlob}
	 * 
	 * @param db
	 * @param blob
	 * @return
	 */
	private MFBlob insertGridFS(DB db, MFBlob blob) throws IOException {
		GridFS fs = new GridFS(db);
		GridFSInputFile mongoFile;
		if (blob.getData() != null) {
			mongoFile = fs.createFile(blob.getData());
			logger.trace("Mongo file created from data, file: " + mongoFile.getFilename() + ", uploadDate: " + mongoFile.getUploadDate());
		} else {
			if (blob.getStream() != null) {
				mongoFile = fs.createFile(blob.getStream(), false);
				logger.trace("Mongo file created from inputstream, file: {}, uploadDate: {}", mongoFile.getFilename() , mongoFile.getUploadDate());
				
				
			} else {
				// this might corrupt some data, because the insertion will be
				// abruptly stopped. However,
				// this shouldn't happened because the method check
				// DataAccessHelper.checkFieldsConsistency will make this check
				// before trying to insert binary data.
				// Anyway, it is good to have a bit of defensive programming
				throw new IOException("The blob can't be opened. It should have either a data[] or an inpusStream");
			}
		}

		mongoFile.setFilename(blob.getFileName());
		mongoFile.setContentType(blob.getContentType());
		mongoFile.save();

		blob.setFileId(mongoFile.getId().toString());
		return blob;

	}

	/**
	 * This method will insert Binary Data and change each of the {@link MFBlob}
	 * to its mongo representation. If the definition doesn't contain binary
	 * data it won't have any side effects, it will just return the original map
	 * 
	 * @param data
	 * @param binaryFields
	 * @return
	 * @throws IOException
	 */
	private Map<String, Object> insertBinaryData(DB db, Map<String, Object> data, List<String> binaryFields)
			throws IOException {
		Map<String, Object> modifiedData = data;
		logger.trace("Inserting binary data for binaryFields: " + binaryFields);
		if (binaryFields.size() > 0) {
			// clone the map, because we are going to modify it with the binary
			// information
			modifiedData = new HashMap<String, Object>(data);
			for (String binField : binaryFields) {
				MFBlob blob = (MFBlob) modifiedData.get(binField);
				// A binary field may be optional - jmpr
				if (blob != null) {
					logger.trace("Inserting blob: name= {}, data={}, stream={}", new Object[] {blob.getFileName(), blob.getData(), blob.getStream()});
					blob = insertGridFS(db, blob);
					DBObject mongoBlob = blob.toMongo();
					modifiedData.put(binField, mongoBlob);
				}
			}
		}
		return modifiedData;
	}

	private InsertReport insertData(DB db, String tx, MFDataSetDefinition dataSetDef,
			List<? extends MFIncomingDataI> rows, boolean failOnDuplicate) throws IOException {
		// Each dataSet has a unique collection where the data is stored. this
		// collection is formed by the following pattern mf_d_+$metaDataRef
		String dataCollectionName = DataDefinitionService.getCollectionOfMetadataRef(dataSetDef.getMetaDataRef());
		DBCollection collection = db.getCollection(dataCollectionName);

		// inserting data we use JOURNAL_SAFE to be pretty sure that the data is
		// written on disk.
		// We are aware that this might delay the transaction, but it is an
		// accepted tread off.
		// Lot of data insertion will only happen during importing lookup table.
		// Since this process is not performed regularly, we sacrifice a bit of
		// perfomance vs. safety
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		// obtain the list of fields that were declared as binary
		// this fields can't be save directly, we need to use GridFs for those
		// fields
		List<String> binaryFields = DataDefinitionService.getFieldsOfType(dataSetDef, FIELD_TYPE.BLOB);
		List<String> locationFields = DataDefinitionService.getFieldsOfType(dataSetDef, FIELD_TYPE.LOCATION);
		BeanToPropertyValueTransformer dataTransformer = new BeanToPropertyValueTransformer("data");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>(CollectionUtils.collect(rows,
				dataTransformer));

		BeanToPropertyValueTransformer metaTransformer = new BeanToPropertyValueTransformer("meta");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> metaList = new ArrayList<Map<String, Object>>(CollectionUtils.collect(rows,
				metaTransformer));
		
		int n = 0;
		long startRow = -1, endRow = -1;
		// insert every row
		Long seq = null;
		for (n = 0; n < dataList.size(); n++) {
			Map<String, Object> data = dataList.get(n);
			Map<String, Object> meta = metaList.get(n);
			if (checkError(n, data, meta)) {
				if (seq == null) {
					seq = DataDefinitionService.getNextSequence(db, dataSetDef.getMetaDataRef());
				}

				if (n == 0) {
					// save the sequence of the first row used
					startRow = seq;
				}

				try {
					insertData(seq, collection, db, tx, dataSetDef, binaryFields, locationFields, data, meta);
					// save the sequence of the last inserted row
					endRow = seq;
					seq = null;// set to null in order to have a new sequence on
								// the next iteration
				} catch (DuplicateDocumentException e) {
					// if the document failed then the previous sequence can be
					// used
					logger.trace("Duplicate documents: " + e);
					if (failOnDuplicate) {
						throw e;
					}
				}
			}

		}
		return new InsertReport(startRow, endRow);

	}

	/**
	 * This is a method that can be specialized in order to perform custom error
	 * check on every row. If the method return true the data will be inserted,
	 * otherwise the data will simple be skipped
	 * 
	 * @param n
	 * @param data
	 * @param meta
	 * @return
	 */
	protected boolean checkError(int n, Map<String, Object> data, Map<String, ?> meta) {
		return true;
	}

	protected DBObject insertData(Long seq, DBCollection collection, DB db, String tx, MFDataSetDefinition dataSetDef,
			List<String> binaryFields, List<String> locationFields, Map<String, Object> data, Map<String, Object> meta)
			throws IOException {

		Long version = dataSetDef.getVersion();
		DataContainer container = new DataContainer();
		if (binaryFields.size() > 0) {
			data = insertBinaryData(db, data, binaryFields);
		}
		if (locationFields.size() > 0) {
			// At this point we convert the MFLocationData to Mongo
			for (String field : locationFields) {
				// this is a safe down casting since the type of a location must
				// be of type MFLacation, and at this point we have already
				// checked it (see DataAccessHelper#checkFieldsConsistency)
				if (data.get(field) != null) {
					MFLocationMongo mongoLoc = promoteToMongoLoc((MFLocationData) data.get(field));
					data.put(field, mongoLoc.toMongo());
				}
			}
		}
		container.setUserData(data);

		if (meta != null) {
			Object metaLocation = meta.get(MFIncomingDataI.META_FIELD_LOCATION);
			if (metaLocation != null) {
				MFLocationMongo mongoLoc = promoteToMongoLoc((MFLocationData) metaLocation);
				meta.put(MFIncomingDataI.META_FIELD_LOCATION, mongoLoc.toMongo());
			}
		}

		container.setMetaData(meta);
		container.setRowId(seq);
		// we are inserting data, so the first version must be zero
		container.setVersion(0l);
		container.setTx(tx);
		container.setTxState(TX_STATE.PENDING);
		container.setDataSetVersion(version);
		DBObject mongoObj = container.toMongo();
		try {
			collection.insert(mongoObj);
		} catch (MongoException e) {
			// check if this exception is due to a duplicate key
			if (e.getCode() == 11000) {
				// duplicate key
				// http://www.mongodb.org/about/contributors/error-codes/
                DuplicateDocumentException documentException = new DuplicateDocumentException(dataSetDef, data, e);
				throw documentException;
			} else {
				throw e;
			}
		}
		return mongoObj;
	}

	/**
	 * Check if an MFLocationData is of mongo type and automatically promotes
	 * 
	 * @param location
	 * @return
	 */
	private static MFLocationMongo promoteToMongoLoc(MFLocationData location) {

		MFLocationMongo mongoLoc;
		if (!(location instanceof MFLocationMongo)) {
			mongoLoc = new MFLocationMongo(location);
		} else {
			mongoLoc = (MFLocationMongo) location;
		}
		return mongoLoc;
	}

	@Override
	public List<MFManagedData> listAllData(String dataSetDef, Long version, OrderBy orderBy) {
		return listData(dataSetDef, version, null, orderBy);
	}

	@Override
	public List<MFManagedData> listData(String dataSetDef, Long ddlVersion, ConditionalCriteria restriction,
			OrderBy orderBy) {
		DBCursor cursor = obtainDataCursor(dataSetDef, ddlVersion, restriction, orderBy);
		MFDataSetDefinition definition = getDataSetDefinition(dataSetDef, ddlVersion);

		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);
		ArrayList<MFManagedData> data = obtainList(cursor, binaryFields, locationFields);
		return data;
	}

	@Override
	public PagedData<List<MFManagedData>> listData(String dataSetDef, Long ddlVersion, ConditionalCriteria restriction,
			OrderBy orderBy, int pageNumber, int pageSize) {
		// obtain the cursor that will query the data
		DBCursor cursor = obtainDataCursor(dataSetDef, ddlVersion, restriction, orderBy);
		// the total number of of objects that the cursor will return
		long numberOfObjects = cursor.count();

		// Need to open another cursor with the same query to gather the data.
		// We can't use the previous cursor, otherwise an exception is thrown
		// from Mongo
		cursor = obtainDataCursor(dataSetDef, ddlVersion, restriction, orderBy);

		MFDataSetDefinition definition = getDataSetDefinition(dataSetDef, ddlVersion);
		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);
		// the number of rows to skip
		int skip = (pageNumber - 1) * pageSize;
		cursor = cursor.skip(skip).limit(pageSize);
		ArrayList<MFManagedData> data = obtainList(cursor, binaryFields, locationFields);

		PagedData<List<MFManagedData>> pagedData = new PagedData<List<MFManagedData>>(data, numberOfObjects,
				pageNumber, pageSize, data.size());
		return pagedData;

	}
	
	@Override
	public List<MFManagedData> listDataByRowId(String dataSetDef, Long ddlVersion, long initialRowId, int rows) {
		BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$gte", initialRowId));
		DB db = mongo.getDB(database);
		DBCollection collection = db.getCollection("mf_d_" + dataSetDef);
		if (ddlVersion != null) {
			query.append(DataContainer.FIELD_DATASET_VERSION, ddlVersion);
		}
		DBCursor cursor = collection.find(query);
		
		MFDataSetDefinition definition = getDataSetDefinition(dataSetDef, ddlVersion);
		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);
		
		cursor = cursor.limit(rows);
		return obtainList(cursor, binaryFields, locationFields);
	}

	/**
	 * Obtain the rows that were inserted with the transaction tx
	 * 
	 * @param tx
	 * @param startRow
	 * @param maxNumberOfData
	 * @return
	 */
	public List<MFManagedData> listDataOfTx(Transaction tx, long startRow, int maxNumberOfData, boolean asc) {
		DB db = mongo.getDB(database);

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db,
				tx.getDataSetDefinitionId());

		DBCollection col = transactionManager.getCollectionAffectedByTransaction(db, tx);

		DBObject query = null;
		int numberOfData = maxNumberOfData;
		if (tx.getType().equals(OPERATION.INSERT)) {
			query = BasicDBObjectBuilder.start().add(DataContainer.FIELD_TX, tx.getId())
					.add(DataContainer._ID, BasicDBObjectBuilder.start().add("$gte", startRow).get()).get();
		} else if (tx.getType().equals(OPERATION.UPDATE)) {
			// updates can only affect one row at a time, so it doesn't make
			// sense to put more rows availalbes
			numberOfData = 1;
			// the transaction of the update will point to the modified row
			query = BasicDBObjectBuilder.start().add(DataContainer._ID, tx.getRowStart()).get();
		} else if (tx.getType().equals(OPERATION.DELETE)) {
			return null;
		}
		// {"tx":tx,"_id":{"$gte":startRow}}

		int order = 1;
		if (!asc) {
			order = -1;
		}
		DBCursor cursor = col.find(query).limit(numberOfData)
				.sort(BasicDBObjectBuilder.start().add(DataContainer._ID, order).get());

		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);
		ArrayList<MFManagedData> data = obtainList(cursor, binaryFields, locationFields);
		return data;

	}

	@Override
	public MFBlob getFile(MFBlob blob) {
		DB db = mongo.getDB(database);
		GridFS fs = new GridFS(db);
		GridFSDBFile gridFile = fs.findOne(new BasicDBObject("_id", new ObjectId(blob.getFileId())));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			gridFile.writeTo(out);
			blob.setData(out.toByteArray());
			blob.setContentType(gridFile.getContentType());
			blob.setFileName(gridFile.getFilename());
			out.close();
			return blob;
		} catch (IOException e) {
			// probably this will mean a fatal error, otherwise there shouldn't
			// be problem writing to the memory. Therefore I (danicrico) have
			// decided to throw a runtimeexception.
			throw new RuntimeException(e);
		}

	};

	@Override
	public MFFileStream getFileLazy(MFBlob blob) {
		if (blob.getFileId() == null) {
			throw new IllegalArgumentException("Only a saved blob can be used to return a file (blob.fileId is null)");
		}
		DB db = mongo.getDB(database);
		GridFS fs = new GridFS(db);
		GridFSDBFile gridFSDBFile = fs.findOne(new BasicDBObject("_id", new ObjectId(blob.getFileId())));
		return new MFFileStreamMongo(gridFSDBFile);

	}

	/**
	 * This method reads the data from a cursor, and transform each of the rows
	 * to a {@link DataContainer}
	 * 
	 * @param cursor
	 * @return
	 */
	private ArrayList<MFManagedData> obtainList(DBCursor cursor, List<String> binaryFields, List<String> locationFields) {
		ArrayList<MFManagedData> table = new ArrayList<MFManagedData>();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			MFManagedData row = toMFManagedData(binaryFields, locationFields, o);
			table.add(row);
		}
		return table;
	}

	private MFManagedData toMFManagedData(DBObject o) {
		return this.toMFManagedData(new ArrayList<String>(), new ArrayList<String>(), o);
	}
	
	private MFManagedData toMFManagedData(List<String> binaryFields, List<String> locationFields, DBObject o) {
		DataContainer row = new DataContainer();
		row.fromMongo(o);

		if (binaryFields.size() > 0) {
			// if there are binary fields, we need to replace on the map the
			// fields corresponding to blobs
			Map<String, Object> data = row.getUserData();
			for (String field : binaryFields) {
				DBObject mongoBlob = (DBObject) data.get(field);
				// But those binary fields could be optional - jmpr
				if (mongoBlob != null) {
					MFBlob blob = new MFBlob();
					blob.fromMongo(mongoBlob);
					data.put(field, blob);
				}
			}
		}
		if (locationFields.size() > 0) {
			Map<String, Object> data = row.getUserData();
			for (String field : locationFields) {
				DBObject object = (DBObject) data.get(field);
				if (object != null) {
					data.put(field, new MFLocationMongo(object));
				}
			}

		}

		if (row.getMetaData() != null) {
			// translate the location stored as a DBOject to an MFLocationData
			Object location = row.getMetaData().get(MFIncomingDataI.META_FIELD_LOCATION);
			if (location != null) {
				DBObject mongoObj = (DBObject) location;
				row.getMetaData().put(MFIncomingDataI.META_FIELD_LOCATION, new MFLocationMongo(mongoObj));
			}
		}
		return row;
	}

	/**
	 * 
	 * Build a cursor that queries the data of a given {@link DataSetMetadata}
	 * 
	 * @param dataSetDef
	 *            the identifier of the {@link DataSetMetadata}
	 * @param ddlVersion
	 *            the version of the {@link DataSetMetadata}
	 * @param restriction
	 * @param orderBy
	 *            The name of the field that will be used for the sorting
	 * @return
	 */
	private DBCursor obtainDataCursor(String dataSetDef, Long ddlVersion, ConditionalCriteria restriction,
			OrderBy orderBy) {
		DBObject query = MongoQueryBuilder.getQuery(restriction);
		if (ddlVersion != null) {
			query.put(DataContainer.FIELD_DATASET_VERSION, ddlVersion);
		}
		DB db = mongo.getDB(database);
		DBCollection collection = db.getCollection("mf_d_" + dataSetDef);
		logger.trace("Listing " + dataSetDef + " using " + query);
		DBCursor cursor = collection.find(query);
		if (orderBy != null) {
			String namespace = orderBy.getNamespace();
			String field = orderBy.getField();
			if (field != null) {
				String orderStr = (namespace != null) ? namespace + "." : "";
				orderStr += field;
				boolean asc = orderBy.isAscending();
				DBObject orderByStatement = BasicDBObjectBuilder.start().add(orderStr, asc ? 1 : -1).get();
				cursor = cursor.sort(orderByStatement);
			}
		}
		return cursor;

	}

	@PreDestroy
	public void preDestroy() {
		mongo.close();
	}

	/**
	 * This isnt't actually a useful method, it is just a documentation of
	 * things that should be checked for consistency. In the future this checks
	 * might covert into a check process
	 */
	public void checkConsistency() {
		// 1) DataSetMetadata without ddl (There should be at least one)
		// 2) DDL Without dataSets (
	}

	@Override
	public MFManagedData getRow(String dataSetDefinition, Long datasetVersion, Long rowId) {
		DB db = mongo.getDB(database);
		DBCollection collection = db.getCollection("mf_d_" + dataSetDefinition);
		DBObject query = new BasicDBObject();
		query.put(DataContainer.FIELD_DATASET_VERSION, datasetVersion);
		query.put("_id", rowId);
		DBObject dbObject = collection.findOne(query);

		MFDataSetDefinitionMongo definition = getDataSetDefinition(dataSetDefinition, datasetVersion);
		List<String> binaryFields = getFieldsOfType(definition, FIELD_TYPE.BLOB);
		List<String> locationFields = getFieldsOfType(definition, FIELD_TYPE.LOCATION);
		return toMFManagedData(binaryFields, locationFields, dbObject);
	}

	public DB getMongoConnection() {
		return mongo.getDB(database);
	}

	@Override
	public MFOperationResult storeWorkflowHistoryData(
			Map<String, Object> workflowHistoryData) {
		DB db = mongo.getDB(database);
		InsertReport insertReport = null;
		insertReport = insertWorkflowHistoryData(db, workflowHistoryData);
		logger.trace("Insert report : sequenceStart=" + insertReport.getSequenceStart() + ", sequenceEnd=" + insertReport.getSequenceEnd());
		return insertReport.toStoreResult().getMfOperationResult();
	}

	private InsertReport insertWorkflowHistoryData(DB db,
			Map<String, Object> workflowHistoryData) {
		DBCollection collection = db.getCollection(DataDefinitionService.COL_WORKFLOW_HISTORY);
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		long startRow = 0, endRow = 0;
		DBObject mongoObj = toMongoObject(workflowHistoryData);
		collection.insert(mongoObj);
		return new InsertReport(startRow, endRow);
	}

	private DBObject toMongoObject(Map<String, Object> workflowHistoryData) {
		BasicDBObject obj = new BasicDBObject();
		if (workflowHistoryData.containsKey("oldStateId")) {
			obj.put("oldStateId", workflowHistoryData.get("oldStateId"));
		}
		obj.put("newStateId", workflowHistoryData.get("newStateId"));
		obj.put("updatedAt", workflowHistoryData.get("updatedAt"));
		obj.put("changedBy", workflowHistoryData.get("changedBy"));
		obj.put("docId",  workflowHistoryData.get("docId"));
		obj.put("formId",  workflowHistoryData.get("formId"));
		obj.put("formVersion",  workflowHistoryData.get("formVersion"));
		obj.put("comment", workflowHistoryData.get("comment"));
		return obj;
	}

	@Override
	public List<Map<String, Object>> listDataByStateId(Long stateId) {
		BasicDBObject query = new BasicDBObject("newStateId", stateId);
		DB db = mongo.getDB(database);
		DBCollection collection = db.getCollection(DataDefinitionService.COL_WORKFLOW_HISTORY);
		
		DBCursor cursor = collection.find(query);

		return obtainMapList(cursor);
	}

	private List<Map<String, Object>> obtainMapList(DBCursor cursor) {
		List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map<String, Object> row = toMapData(o);
			table.add(row);
		}
		return table;
	}

	private Map<String, Object> toMapData(DBObject o) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		String comment = "";
		if (o.containsField("oldStateId")) {
			dataMap.put("oldStateId", (Long) o.get("oldStateId"));
		}
		dataMap.put("newStateId", (Long) o.get("newStateId"));
		dataMap.put("updatedAt", (Date) o.get("updatedAt"));
		dataMap.put("changedBy", (Long) o.get("changedBy"));
		dataMap.put("formId", (Long) o.get("formId"));
		dataMap.put("docId", (Long) o.get("docId"));
		dataMap.put("formVersion", (Long) o.get("formVersion"));
		if (o.containsField("comment")) {
			comment = (String) o.get("comment");
		}
		dataMap.put("comment", comment);
		
		return dataMap;
	}

	@Override
	public List<Map<String, Object>> listDataByDocIdAndFormData(Long docId, Long formId, Long formVersion) {
		BasicDBObject query = new BasicDBObject("docId", docId);
		query.append("formId", formId);
		query.append("formVersion", formVersion);
		DB db = mongo.getDB(database);
		DBCollection collection = db.getCollection(DataDefinitionService.COL_WORKFLOW_HISTORY);
		BasicDBObject orderBy = new BasicDBObject("updatedAt", -1);
		DBCursor cursor = collection.find(query).sort(orderBy);

		return obtainMapList(cursor);
	}

	@Override
	public MFOperationResult updateStateForMultiple(String metadataRef,
			Long version, StateDTO stateDto, ConditionalCriteria selector) {
		DB db = mongo.getDB(database);

		MFDataSetDefinitionMongo definition = DataDefinitionService.getDataSetDefinition(db, metadataRef, version);
		if (definition == null) {
			throw new MFDataAccessException("Can't find DDL for data set " + metadataRef + "-" + version);
		}

		// select the documents that match the criteria
		DBCursor cursor = obtainDataCursor(metadataRef, version, selector, null);
		MFOperationResult result = new MFOperationResult();
		int count = cursor.count();
		if (count >= 1) {
			Long changingRowId = null;
			Long startChangingRowId = null;
			Long endChangingRowId = null;
			Long changingVersion =  null;
			List<Long> changingRowIds = new ArrayList<Long>();
			
			int i = 0;
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				i++;
				MFManagedData changingRow = toMFManagedData(dbObject);
				changingRowId = changingRow.getRowId();
				changingVersion = changingRow.getVersion();
				changingRowIds.add(changingRowId);
				if (i == 1) {
					startChangingRowId = changingRowId;
				}
				
				if (i == count) {
					endChangingRowId = changingRowId;
				}
				
			}
				
			Transaction tx = transactionManager.startTransaction(db, definition, OPERATION.UPDATE);
			
			DBObject query = BasicDBObjectBuilder.start().add(MFDataSetDefinitionMongo._ID, getListValue(changingRowIds)).get();

			DBObject stateMetaObject = toMongoFromState(stateDto, changingVersion);
			
			// Change the data on mongo
			String dataCollectionName = DataDefinitionService.getCollectionOfMetadataRef(definition.getMetaDataRef());
			DBCollection collection = db.getCollection(dataCollectionName);
			BasicDBObject setQuery = new BasicDBObject();
		    setQuery.append("$set", stateMetaObject);

			collection.updateMulti(query, setQuery);


			// commit the transaction
			transactionManager.markTransactionAsCommiting(db, tx, startChangingRowId, endChangingRowId);
			// we didn't change the state of the transaction to pending so we
			// need to call "commit" with 0. Otherwise, it will fail
			transactionManager.commit(db, tx, 0);
			transactionManager.markTransactionAsDone(db, tx);
			result.setResult(RESULT.SUCCESS);
			result.setNumberOfAffectedRows(count);
		} else {
			// making an update to zero row is no problem
			result.setResult(RESULT.SUCCESS);
			result.setNumberOfAffectedRows(0);
		}

		return result;
	}

	private DBObject getListValue(List<Long> changingRowIds) {
		DBObject fieldValue = BasicDBObjectBuilder.start().add(QueryOperators.IN, changingRowIds).get();
		return fieldValue;
	}

}
