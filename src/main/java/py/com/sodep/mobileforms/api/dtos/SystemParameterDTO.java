package py.com.sodep.mobileforms.api.dtos;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;

public class SystemParameterDTO extends SodepEntity implements IParameter {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8172902674996197730L;

	private String label;

	private String value;

	private PARAMETER_TYPE type;

	private String description;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PARAMETER_TYPE getType() {
		return type;
	}

	public void setType(PARAMETER_TYPE type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Long getParameterId() {
		// TODO Auto-generated method stub
		return getId();
	}

	@Override
	public Boolean getValueAsBoolean() {
		// TODO Auto-generated method stub
		return Boolean.parseBoolean(getValue());
	}

	@Override
	public Long getId() {
		return this.id;
	}


}
