package py.com.sodep.mobileforms.api.services.data;

import py.com.sodep.mf.exchange.MFLocationData;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MFLocationMongo extends MFLocationData implements MFStorable {

	public static final String FIELD_LATITUDE = "latitude";
	public static final String FIELD_LONGITUDE = "longitude";
	public static final String FIELD_ALTITUDE = "altitude";
	public static final String FIELD_ACCURACY = "accuracy";

	public MFLocationMongo(MFLocationData field) {
		super(field.getLatitude(), field.getLongitude(), field.getAltitude(), field.getAccuracy());
	}

	public MFLocationMongo(DBObject o) {
		fromMongo(o);
	}

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		obj.put(FIELD_LATITUDE, getLatitude());
		obj.put(FIELD_LONGITUDE, getLongitude());
		obj.put(FIELD_ALTITUDE, getAltitude());
		obj.put(FIELD_ACCURACY, getAccuracy());
		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {
		setLatitude((Double) o.get(FIELD_LATITUDE));
		setLongitude((Double) o.get(FIELD_LONGITUDE));
		setAltitude((Double) o.get(FIELD_ALTITUDE));
		setAccuracy((Double) o.get(FIELD_ACCURACY));
	}

	@Override
	public String toString() {
		return "[" + getLatitude() + "," + getLongitude() + "]";
	}

}
