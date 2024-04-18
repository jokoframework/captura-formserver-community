package py.com.sodep.mobileforms.api.services.metadata.forms;

import java.io.Serializable;

public class Criterion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum Condition {
		NONE, // FIXME why is this necessary?
		EQUALS, // DONE
		CONTAINS, // TODO
		GREATER_THAN, // $gt
		LESS_THAN, // $lt
		BETWEEN, // TODO
		DISTINCT, // $ne
		NOT_EXIST // TODO
	}

	private String propertyName;

	private boolean metadata;

	private Object value;

	private Object value2;

	private Condition condition;

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue2() {
		return value2;
	}

	public void setValue2(Object value2) {
		this.value2 = value2;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}

}
