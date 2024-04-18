package py.com.sodep.mobileforms.api.dtos;

import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;

public class ReportFilterOptionDTO {

	private String elementId;
	private String operator;
	private String value;
	private FIELD_TYPE type;

	public ReportFilterOptionDTO() {
		super();
	}

	public ReportFilterOptionDTO(String elementId, String operator, String value, FIELD_TYPE type) {
		this.elementId = elementId;
		this.operator = operator;
		this.value = value;
		this.type = type;
	}
	
	public ReportFilterOptionDTO(String elementId, String operator, String value) {
		this(elementId, operator, value, null);
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public FIELD_TYPE getType() {
		return this.type;
	}

	
}
