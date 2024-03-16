package py.com.sodep.mobileforms.impl.services.data;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.Transaction;

public class SynchronizationStatus implements MFStorable {

	public static final String FIELD_USERNAME = "username";
	public static final String FIELD_DEVICE = "device";

	private String id;
	private String username;
	private String device;
	private String tx;
	private Long rowStart;
	private Long rowEnd;

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		if (this.id != null) {
			obj.put(MFStorable._ID, new ObjectId(id));
		}
		obj.put(FIELD_USERNAME, username);
		obj.put(FIELD_DEVICE, device);
		obj.put(DataContainer.FIELD_TX, tx);
		obj.put(Transaction.FIELD_ROW_START, rowStart);
		obj.put(Transaction.FIELD_ROW_END, rowEnd);

		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {
		this.id = o.get(MFStorable._ID).toString();
		this.username = (String) o.get(FIELD_USERNAME);
		this.device = (String) o.get(FIELD_DEVICE);
		this.tx = (String) o.get(DataContainer.FIELD_TX);
		this.rowStart = (Long) o.get(Transaction.FIELD_ROW_START);
		this.rowEnd = (Long) o.get(Transaction.FIELD_ROW_END);
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}

	public Long getRowStart() {
		return rowStart;
	}

	public void setRowStart(Long rowStart) {
		this.rowStart = rowStart;
	}

	public Long getRowEnd() {
		return rowEnd;
	}

	public void setRowEnd(Long rowEnd) {
		this.rowEnd = rowEnd;
	}

}
