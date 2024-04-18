package py.com.sodep.mobileforms.api.entities.sys;

import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;

public interface IParameter {

	public Long getParameterId();

	public String getLabel();

	public String getValue();

	public Boolean getValueAsBoolean();

	public PARAMETER_TYPE getType();

	public String getDescription();

	public Boolean getActive();

}
