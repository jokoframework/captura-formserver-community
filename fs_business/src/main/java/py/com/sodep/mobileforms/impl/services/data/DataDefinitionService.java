package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.MFStorable;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * This is a class that implement some of the functionality exposed by
 * {@link DataAccessService} regarding definition of data
 * 
 * @author danicricco
 * 
 */
public class DataDefinitionService {

	public static final String COL_DATASET_METADATA = "mf_data_set_metadata";
	public static final String COL_DDL = "mf_ddl";
	public static final String COL_WORKFLOW_HISTORY = "mf_workflow_history";
	
	/**
	 * Defines a new DataSetMetadata and its first version for the ddl
	 * 
	 * @param db
	 * @param ddl
	 * @return
	 */
	public static MFDataSetDefinitionMongo define(DB db, MFDataSetDefinition ddl) {

		DBCollection collection = db.getCollection(COL_DATASET_METADATA);
		collection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		DataSetMetadata dataSetInfo = new DataSetMetadata();
		DBObject mongoObj = dataSetInfo.toMongo();

		collection.insert(mongoObj);
		// at this point the data was securely written on disk because we are
		// using writeConcern.JOURNAL_SAFE

		DataSetMetadata savedDef = new DataSetMetadata();
		savedDef.fromMongo(mongoObj);

		MFDataSetDefinitionMongo savedDDL = storeDDL(db, savedDef.getObjectId(), ddl);

		String dataCollectionName = getCollectionOfMetadataRef(savedDDL.getMetaDataRef());
		DBCollection dataCollection = db.getCollection(dataCollectionName);
		// create the unique constraint over the set of columns mark as primary
		// keys
		BasicDBObjectBuilder keyIndexBuilder = BasicDBObjectBuilder.start();
		List<MFField> fields = ddl.getFields();
		boolean hasPK = false;
		for (MFField f : fields) {
			if (f.isPk()) {
				keyIndexBuilder.add(MFStorable.FIELD_DATA + "." + f.getColumnName(), 1);
				hasPK = true;
			}
		}

		if (hasPK) {
			// define the unique index
			
			dataCollection.createIndex(keyIndexBuilder.get(),
					BasicDBObjectBuilder.start().add("unique", true).add("name", "mf_pk").get());
		}
		// index the lookup based on the transaction so it can be fast to use
		// listDataOfTx
		dataCollection.createIndex(BasicDBObjectBuilder.start().add(DataContainer.FIELD_TX, 1).get());
		return savedDDL;
	}

	/**
	 * This method will always store a new version of the DDL, no matter if
	 * there were previous version stored.
	 * 
	 * @param db
	 * @param metadataRef
	 * @param ddl
	 */
	public static MFDataSetDefinitionMongo storeDDL(DB db, String metadataRef, MFDataSetDefinition ddl) {
		MFDataSetDefinitionMongo ddlMongoCapable;
		if (ddl instanceof MFDataSetDefinitionMongo) {
			ddlMongoCapable = (MFDataSetDefinitionMongo) ddl;
		} else {
			ddlMongoCapable = new MFDataSetDefinitionMongo(ddl);
		}
		// TODO check DDL consistency

		// 1) ATOMICALLY INCREMENT THE LAST VERSION OF THE DATA SET (refereced
		// by the _id metadataRef)
		Long lastVersion = incrementDDLVersion(db, metadataRef);

		// 2) INSERT THE DDL

		ddlMongoCapable.setVersion(lastVersion);
		ddlMongoCapable.setMetaDataRef(metadataRef);
		DBCollection colDDL = db.getCollection(COL_DDL);

		BasicDBObject mongoOBK = ddlMongoCapable.toMongo();
		colDDL.insert(mongoOBK);
		ddlMongoCapable = new MFDataSetDefinitionMongo();
		ddlMongoCapable.fromMongo(mongoOBK);
		return ddlMongoCapable;

	}

	/**
	 * Atomically increment the revision number of a MetaDataSet.
	 * 
	 * @param db
	 * @param metadataRef
	 * @return
	 */
	public static Long incrementRevisionNumber(DB db, String metadataRef) {
		DBCollection colMetadata = db.getCollection(COL_DATASET_METADATA);
		colMetadata.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		DBObject query = BasicDBObjectBuilder.start().add(MFStorable._ID, new ObjectId(metadataRef))
				.get();
		DBObject incVersion = BasicDBObjectBuilder.start()
				.add("$inc", BasicDBObjectBuilder.start().add(DataSetMetadata.FIELD_REVISION, 1).get()).get();
		DBObject mongoDBMetadata = colMetadata.findAndModify(query, incVersion);
		if (mongoDBMetadata == null) {
			// This is a truly unexpected error. Somebody is trying to increment
			// the revision of a non existing DataSetMetadata
			throw new MFDataAccessException("Couldn't find the dataSet " + metadataRef);
		}
		DataSetMetadata meta = new DataSetMetadata();
		meta.fromMongo(mongoDBMetadata);
		return meta.getRevision();
	}

	/**
	 * Atomically increment the ddl version of a MetaDataSet
	 * 
	 * @param db
	 * @param metadataRef
	 * @return
	 */
	private static Long incrementDDLVersion(DB db, String metadataRef) {
		DBCollection colMetadata = db.getCollection(COL_DATASET_METADATA);
		colMetadata.setWriteConcern(WriteConcern.JOURNAL_SAFE);
		DBObject query = BasicDBObjectBuilder.start().add(MFStorable._ID, new ObjectId(metadataRef))
				.get();
		DBObject incVersion = BasicDBObjectBuilder.start()
				.add("$inc", BasicDBObjectBuilder.start().add(DataSetMetadata.FIELD_LAST_VERSION, 1).get()).get();
		DBObject mongoDBMetadata = colMetadata.findAndModify(query, incVersion);
		if (mongoDBMetadata == null) {
			// This is a truly unexpected error. Somebody is trying to insert a
			// DDL for an non exiting DataSetMetadata
			throw new MFDataAccessException("Couldn't find the dataSet " + metadataRef);
		}
		DataSetMetadata meta = new DataSetMetadata();
		meta.fromMongo(mongoDBMetadata);
		return meta.getLastVersion();

	}

	/**
	 * Search for the definition of the metadaRef in version
	 * 
	 * @param db
	 * @param metadaRef
	 *            the Id of the metadataref
	 * @param version
	 *            the version of the dataset
	 * @return
	 */
	public static MFDataSetDefinitionMongo getDataSetDefinition(DB db, String metadaRef, Long version) {
		// query={metadataRef:$id,version:$version}
		DBObject query = BasicDBObjectBuilder.start().add(MFDataSetDefinitionMongo.FIELD_METADATA_REF, metadaRef)
				.add(MFDataSetDefinitionMongo.FIELD_VERSION, version).get();

		DBCollection collection = db.getCollection(COL_DDL);
		DBObject defMongo = collection.findOne(query);
		if (defMongo == null) {
			return null;
		}

		return new MFDataSetDefinitionMongo(defMongo);

	}

	/**
	 * Loads a definition by its id. Note that this is different from
	 * {@link #getDataSetDefinition(DB, String, Long)}, because the id here
	 * means the id of the {@link MFDataSetDefinitionMongo} itself
	 * 
	 * @param db
	 * @param id
	 * @return
	 */
	public static MFDataSetDefinitionMongo getDataSetDefinition(DB db, String id) {
		DBObject query = BasicDBObjectBuilder.start().add(MFDataSetDefinitionMongo._ID, new ObjectId(id))
				.get();
		DBCollection collection = db.getCollection(COL_DDL);
		DBObject defMongo = collection.findOne(query);
		if (defMongo == null) {
			return null;
		}

		return new MFDataSetDefinitionMongo(defMongo);
	}

	public static DataSetMetadata getDataSetMetadata(DB db, String objectId) {
		DBCollection collection = db.getCollection(COL_DATASET_METADATA);
		DBObject query = BasicDBObjectBuilder.start().add(MFStorable._ID, new ObjectId(objectId)).get();
		DBObject data = collection.findOne(query);
		if (data != null) {
			DataSetMetadata meta = new DataSetMetadata();
			meta.fromMongo(data);
			return meta;
		}
		return null;
	}

	public static DataSetDeleteReport deleteDataSetMetaData(DB db, String dataSetDefinition) {

		int dataBag = 0;
		// drop the data bag
		DBCollection collection = db.getCollection(getCollectionOfMetadataRef(dataSetDefinition));
		if (collection != null) {
			dataBag = 1;
			collection.drop();
		}

		// drops all associated DML
		DBCollection colDDLs = db.getCollection(COL_DDL);
		DBObject query = BasicDBObjectBuilder.start()
				.add(MFDataSetDefinitionMongo.FIELD_METADATA_REF, dataSetDefinition).get();
		WriteResult writeResult = colDDLs.remove(query);
		int deletedDDLs = writeResult.getN();
		// drop the dataSet
		DataSetMetadata dataSet = getDataSetMetadata(db, dataSetDefinition);
		int deletedDataSet = 0;
		if (dataSet != null) {
			DBCollection colDataSet = db.getCollection(COL_DATASET_METADATA);
			DBObject obj = dataSet.toMongo();
			WriteResult result = colDataSet.remove(obj);
			deletedDataSet = result.getN();
		}
		return new DataSetDeleteReport(deletedDataSet, deletedDDLs, dataBag);

	}

	public static String getCollectionOfMetadataRef(String metaDataRef) {
		return "mf_d_" + metaDataRef;
	}

	public static DataDefinitionReport getDataSetReport(DB db, String dataSetDefinition) {
		int dataBag = 0;
		Set<String> collections = db.getCollectionNames();
		String dataBagName = getCollectionOfMetadataRef(dataSetDefinition);
		if (collections.contains(dataBagName)) {
			dataBag = 1;
		}

		DBCollection colDDLs = db.getCollection(COL_DDL);
		DBObject query = BasicDBObjectBuilder.start()
				.add(MFDataSetDefinitionMongo.FIELD_METADATA_REF, dataSetDefinition).get();
		int ddls = colDDLs.find(query).count();

		DataSetMetadata metaData = getDataSetMetadata(db, dataSetDefinition);
		int dataSet = 0;
		if (metaData != null) {
			dataSet = 1;
		}
		return new DataDefinitionReport(dataBag, dataSet, ddls);
	}

	

	/**
	 * Search on a {@link MFDataSetDefinition} the fields declared as type
	 * 
	 * @param definition
	 * @return a list of mfields' name declared as type
	 */

	public static List<String> getFieldsOfType(MFDataSetDefinition definition, FIELD_TYPE type) {
		List<MFField> fields = definition.getFields();
		ArrayList<String> binFields = new ArrayList<String>();
		for (MFField field : fields) {
			if (field.getType().equals(type)) {
				binFields.add(field.getColumnName());
			}
		}
		return binFields;
	}
	
	public static Long getNextSequence(DB db, String metadataRef) {
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

}
