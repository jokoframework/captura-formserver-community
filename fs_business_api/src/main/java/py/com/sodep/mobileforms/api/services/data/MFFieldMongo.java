package py.com.sodep.mobileforms.api.services.data;

import py.com.sodep.mf.exchange.MFField;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MFFieldMongo extends MFField implements MFStorable {

	public MFFieldMongo(MFField field) {
		super(field.getType(), field.getColumnName());
		setPk(field.isPk());
		setUnique(field.isUnique());
	}

	public MFFieldMongo(DBObject o) {
		fromMongo(o);
	}

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("type", getType().toString());
		obj.put("columnName", getColumnName());
		obj.put("pkMember", isPk());
		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {
		String fieldTypeStr = (String) o.get("type");
		setType(FIELD_TYPE.valueOf(fieldTypeStr));
		setColumnName((String) o.get("columnName"));
		Object pkMember = o.get("pkMember");
		if (pkMember != null) {
			setPk((Boolean) pkMember);
		} else {
			setPk(false);
		}

	}
}
