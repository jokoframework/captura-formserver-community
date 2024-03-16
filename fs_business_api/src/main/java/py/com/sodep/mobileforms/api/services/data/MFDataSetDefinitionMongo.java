package py.com.sodep.mobileforms.api.services.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MFDataSetDefinitionMongo extends MFDataSetDefinition implements MFStorable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FIELD_FIELDS = "fields";
	public static final String FIELD_VERSION = "version";
	public static final String FIELD_METADATA_REF = "metadataRef";

	// This is the unique ID generated by mongo

	private String objectId;

	public MFDataSetDefinitionMongo(DBObject obj) {
		fromMongo(obj);
	}

	public MFDataSetDefinitionMongo() {

	}

	public MFDataSetDefinitionMongo(MFDataSetDefinition ddl) {
		super(ddl.getFields(), ddl.getVersion(), ddl.getMetaDataRef());
	}

	public BasicDBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		if (this.objectId != null) {
			obj.put(MFStorable._ID, new ObjectId(objectId));
		}
		List<DBObject> fieldsDbs = new ArrayList<DBObject>();
		for (MFField mfField : fields) {
			MFFieldMongo mToMongo = new MFFieldMongo(mfField);
			fieldsDbs.add(mToMongo.toMongo());
		}

		obj.put(FIELD_FIELDS, fieldsDbs);
		obj.put(FIELD_METADATA_REF, metaDataRef);
		obj.put(FIELD_VERSION, version);

		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {

		this.objectId = ((ObjectId) o.get(MFStorable._ID)).toString();
		ArrayList<MFField> fields = new ArrayList<MFField>();
		List<DBObject> dbFields = (List<DBObject>) o.get(FIELD_FIELDS);
		for (DBObject f : dbFields) {
			fields.add(new MFFieldMongo(f));
		}
		this.fields = fields;
		this.metaDataRef = (String) o.get(FIELD_METADATA_REF);
		this.version = (Long) o.get(FIELD_VERSION);

	}

	@JsonIgnore
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;

	}

}
