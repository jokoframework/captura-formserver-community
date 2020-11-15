package py.com.sodep.mobileforms.api.services.data;

import com.mongodb.DBObject;

/**
 * This interface mark the classes that are stored in mongo.
 * 
 * @author danicricco
 * 
 */
public interface MFStorable {

	public static final String FIELD_VERSION = "version";
	public static final String FIELD_DATASET_VERSION = "dataSetVersion";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_META = "meta";
	public static final String FIELD_TX = "tx";
	public static final String FIELD_TXSTATE = "tx_state";
	public static final String _ID="_id";
	/**
	 * 
	 * @return The representation of this object in a Mongo DB
	 */
	public DBObject toMongo();

	/**
	 * Assign to the fields of this class the values in the DBOject
	 * 
	 * @param o
	 */
	public void fromMongo(DBObject o);

	// Implementation note:
	// I know that there is a better to way to create custom classes from and to
	// Mongo. However, in this short period I couldn't figure out the right way.
	// This might be suitable for future refactor
	// http://www.mongodb.org/display/DOCS/Java+-+Saving+Objects+Using+DBObject
}
