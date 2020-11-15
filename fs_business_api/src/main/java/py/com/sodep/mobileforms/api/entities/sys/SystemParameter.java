package py.com.sodep.mobileforms.api.entities.sys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;

/**
 * A system parameter.
 * 
 * Used to persist parameters like the default language or other data relevant
 * to the execution of the application that shouldn't be hardcoded.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "sys", name = "parameters")
@SequenceGenerator(name = "seq_parameters", sequenceName = "sys.seq_parameters")
public class SystemParameter extends SodepEntity implements IParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String DESCRIPTION = "description";

	public static final String LABEL = "label";

	public static final String TYPE = "type";

	public static final String VALUE = "value";

	private String label;

	private String value;

	private PARAMETER_TYPE type;

	private String description;

	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_parameters")
	public Long getId() {
		return this.id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public PARAMETER_TYPE getType() {
		return type;
	}

	public void setType(PARAMETER_TYPE type) {
		this.type = type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	@Transient
	public Long getParameterId() {
		return id;
	}

	@Override
	@Transient
	public Boolean getValueAsBoolean() {
		if (value != null) {
			return new Boolean(value);
		}
		return null;
	}

}
