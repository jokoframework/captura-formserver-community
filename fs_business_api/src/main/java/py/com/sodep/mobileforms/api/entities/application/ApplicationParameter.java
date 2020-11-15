package py.com.sodep.mobileforms.api.entities.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;

@Entity
@Table(schema = "applications", name = "parameters", uniqueConstraints = {
		@UniqueConstraint(columnNames = "application_id"), @UniqueConstraint(columnNames = "parameter_id") })
@SequenceGenerator(name = "seq_applications_parameters", sequenceName = "applications.seq_applications_parameters")
public class ApplicationParameter extends SodepEntity implements IParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Application application;

	private PARAMETER_TYPE type;

	private String description;

	private Long parameterId;

	private String label;

	private String value;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_applications_parameters")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	@ManyToOne
	@JoinColumn(nullable = false, name = "application_id")
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Override
	public String getLabel() {
		return label;
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

	@Override
	public String getDescription() {
		return description;
	}

	@Column(name = "parameter_id", nullable = false)
	public Long getParameterId() {
		return parameterId;
	}

	public void setParameterId(Long parameterId) {
		this.parameterId = parameterId;
	}

	public void setType(PARAMETER_TYPE type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLabel(String label) {
		this.label = label;
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
