package py.com.sodep.mobileforms.api.services.data;

import java.util.Date;

import org.bson.types.ObjectId;

import py.com.sodep.mf.exchange.TXInfo.OPERATION;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This class has two purpose: 1) Perform a two phase commit so (only insert are
 * supported) 2) Track all types of DML performed over a given dataSet. This is
 * very important information to perform incremental updates of lookuptables
 * 
 * @author danicricco
 * 
 */
public class Transaction implements MFStorable {

	public static final String FIELD_HOST_IDENTIFIER = "host";
	public static final String FIELD_DATASET_DEFINITION = "ddlID";
	public static final String FIELD_START_TIME = "startTime";
	public static final String FIELD_OPERATION_TYPE = "operationType";
	public static final String FIELD_ROW_START = "rowStart";
	public static final String FIELD_ROW_END = "rowEnd";
	public static final String FIELD_END_TIME = "endTime";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_ORDER = "order";
	public static final String FIELD_DATASET_ID = "metaDataRef";

	public static enum TX_GLOBAL_STATE {
		PENDING, COMMITING, DONE, ROLLING_BACK
	};

	private OPERATION type;

	/**
	 * A unique id for the transaction
	 */
	private String id;

	// The identifier of the host that run the transaction
	private String hostIdentifier;
	/**
	 * This is an autonumeric value to order the transactions in the way they
	 * have happened. It is guaranteed to be unique for a given dataSet
	 */
	private Long order;
	private Date startTime;
	private Date endTime;
	/**
	 * The dataSet that the insert is affecting
	 */
	private String dataSetDefinitionId;
	private String metaDataSetId;
	private Long rowStart;
	private Long rowEnd;
	private TX_GLOBAL_STATE state;

	public Transaction(String id, String hostIdentifier) {
		this.id = id;
		this.hostIdentifier = hostIdentifier;
	}

	public Transaction() {

	}

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		if (this.id != null) {
			obj.put(MFStorable._ID, ObjectId.massageToObjectId(this.id));
		}

		obj.put(FIELD_STATE, state.ordinal());
		obj.put(FIELD_HOST_IDENTIFIER, hostIdentifier);
		obj.put(FIELD_DATASET_DEFINITION, dataSetDefinitionId);
		obj.put(FIELD_START_TIME, startTime);
		obj.put(FIELD_END_TIME, endTime);
		obj.put(FIELD_OPERATION_TYPE, type.ordinal());
		obj.put(FIELD_ORDER, order);
		obj.put(FIELD_DATASET_ID, metaDataSetId);
		if (rowStart != null) {
			obj.put(FIELD_ROW_START, rowStart);
		}
		if (rowEnd != null) {
			obj.put(FIELD_ROW_END, rowEnd);
		}

		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {
		this.id = (String) o.get(MFStorable._ID).toString();
		this.state = TX_GLOBAL_STATE.values()[(Integer) o.get(FIELD_STATE)];
		this.hostIdentifier = (String) o.get(FIELD_HOST_IDENTIFIER);
		this.dataSetDefinitionId = (String) o.get(FIELD_DATASET_DEFINITION);
		this.startTime = (Date) o.get(FIELD_START_TIME);
		this.endTime = (Date) o.get(FIELD_END_TIME);
		this.type = OPERATION.values()[(Integer) o.get(FIELD_OPERATION_TYPE)];
		this.order = (Long) o.get(FIELD_ORDER);
		this.metaDataSetId = (String) o.get(FIELD_DATASET_ID);
		this.rowStart = (Long) o.get(FIELD_ROW_START);
		this.rowEnd = (Long) o.get(FIELD_ROW_END);

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHostIdentifier() {
		return hostIdentifier;
	}

	public void setHostIdentifier(String hostIdentifier) {
		this.hostIdentifier = hostIdentifier;
	}

	public String getDataSetDefinitionId() {
		return dataSetDefinitionId;
	}

	public void setDataSetDefinitionId(String dataSet) {
		this.dataSetDefinitionId = dataSet;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public OPERATION getType() {
		return type;
	}

	public void setType(OPERATION type) {
		this.type = type;
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

	public TX_GLOBAL_STATE getState() {
		return state;
	}

	public void setState(TX_GLOBAL_STATE state) {
		this.state = state;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSetDefinitionId == null) ? 0 : dataSetDefinitionId.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((hostIdentifier == null) ? 0 : hostIdentifier.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((rowEnd == null) ? 0 : rowEnd.hashCode());
		result = prime * result + ((rowStart == null) ? 0 : rowStart.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Transaction other = (Transaction) obj;
		if (dataSetDefinitionId == null) {
			if (other.dataSetDefinitionId != null) {
				return false;
			}
		} else if (!dataSetDefinitionId.equals(other.dataSetDefinitionId)) {
			return false;
		}
		if (endTime == null) {
			if (other.endTime != null) {
				return false;
			}
		} else if (!endTime.equals(other.endTime)) {
			return false;
		}
		if (hostIdentifier == null) {
			if (other.hostIdentifier != null) {
				return false;
			}
		} else if (!hostIdentifier.equals(other.hostIdentifier)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (rowEnd == null) {
			if (other.rowEnd != null) {
				return false;
			}
		} else if (!rowEnd.equals(other.rowEnd)) {
			return false;
		}
		if (rowStart == null) {
			if (other.rowStart != null) {
				return false;
			}
		} else if (!rowStart.equals(other.rowStart)) {
			return false;
		}
		if (startTime == null) {
			if (other.startTime != null) {
				return false;
			}
		} else if (!startTime.equals(other.startTime)) {
			return false;
		}
		if (state != other.state) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public String getMetaDataSetId() {
		return metaDataSetId;
	}

	public void setMetaDataSetId(String metaDataSetId) {
		this.metaDataSetId = metaDataSetId;
	}

}
