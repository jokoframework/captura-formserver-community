package py.com.sodep.mobileforms.impl.services.data;

import java.util.Date;
import java.util.Map;

import py.com.sodep.mf.exchange.MFManagedDataBasic;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.services.data.MFStorable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DataContainer extends MFManagedDataBasic implements MFStorable {

	public static enum TX_STATE {
		PENDING, CONFIRMED
	};

	private Long rowId;
	private Long version;

	private Long dataSetVersion;
	private String tx;
	private TX_STATE txState;

	/**
	 * A Unique auto-numeric id for the row register
	 */
	public Long getRowId() {
		return rowId;
	}

	public void setRowId(Long rowId) {
		this.rowId = rowId;
	}

	/**
	 * How many times this row has changed. After an insert it will always be
	 * zero, it will change after updates
	 */
	@JsonIgnore
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public void setUserData(Map<String, Object> userData) {
		this.userData = userData;
	}

	/**
	 * This is the id of the {@link DataSetMetadata}
	 * 
	 * @return
	 */
	public Long getDataSetVersion() {
		return dataSetVersion;
	}

	public void setDataSetVersion(Long dataSetVersion) {
		this.dataSetVersion = dataSetVersion;
	}

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		if (this.rowId != null) {
			obj.put(MFStorable._ID, rowId);
		}
		obj.put(FIELD_VERSION, version);
		obj.put(FIELD_DATASET_VERSION, dataSetVersion);
		obj.put(FIELD_TX, tx);
		obj.put(FIELD_TXSTATE, txState.ordinal());
		obj.put(FIELD_DATA, new BasicDBObject(userData));
		if (metaData != null) {
			obj.put(FIELD_META, metaData);
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void fromMongo(DBObject o) {
		rowId = (Long) o.get(MFStorable._ID);
		version = (Long) o.get(FIELD_VERSION);
		dataSetVersion = (Long) o.get(FIELD_DATASET_VERSION);
		tx = (String) o.get(FIELD_TX);
		Integer stateOrdinal = (Integer) o.get(FIELD_TXSTATE);
		if (stateOrdinal != null) {
			txState = TX_STATE.values()[stateOrdinal];
		}
		DBObject dataObj = (DBObject) o.get(FIELD_DATA);
		this.userData = dataObj.toMap();
		DBObject metaObj = (DBObject) o.get(FIELD_META);
		if (metaObj != null) {
			this.metaData = metaObj.toMap();
		}
	}

	@Override
	public Long getDDLVersion() {
		return getDataSetVersion();
	}

	@Override
	public Object getValue(String field) {
		return getUserData().get(field);
	}

	/**
	 * This method will automatically cast the value of data[field] to the
	 * provided class. The method will throw a {@link ClassCastException} if the
	 * downcasting is not possible
	 * 
	 * @param type
	 * @param field
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T safeCast(Class<T> type, String field) {
		Object value = getValue(field);
		if (value == null) {
			return null;
		}
		if (type.isInstance(value)) {
			return (T) value;
		}
		throw new ClassCastException("The Field " + field + " is of type " + value.getClass().getCanonicalName()
				+ ", can't cast to " + type.getCanonicalName());
	}

	@Override
	public String getString(String field) {
		return safeCast(String.class, field);
	}

	@Override
	public Long getLong(String field) {
		Object value = getValue(field);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		throw new ClassCastException("The Field " + field + " is of type " + value.getClass().getCanonicalName()
				+ ", can't cast to long");
	}

	@Override
	public Double getDouble(String field) {
		Object value = getValue(field);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		throw new ClassCastException("The Field " + field + " is of type " + value.getClass().getCanonicalName()
				+ ", can't cast to double");
	}

	@Override
	public Boolean getBoolean(String field) {
		return safeCast(Boolean.class, field);
	}

	@Override
	public Date getDate(String field) {
		Object value = getValue(field);
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return ((Date) value);
		}
		throw new ClassCastException("The Field " + field + " is of type " + value.getClass().getCanonicalName()
				+ ", can't cast to java.util.Date");
	}

	@JsonIgnore
	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}

	@JsonIgnore
	public TX_STATE getTxState() {
		return txState;
	}

	public void setTxState(TX_STATE txState) {
		this.txState = txState;
	}

}
