package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import py.com.sodep.mf.form.model.prototype.MFInput;

/**
 * A concrete implementation of an element that allows the input of text
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "elements_inputs")
public class Input extends ElementPrototype implements Serializable {

	private static final long serialVersionUID = 1L;

	private String defaultValue;

	private Integer maxLength;

	private Integer minLength;

	private Boolean readOnly = false;

	private MFInput.Type type = MFInput.Type.TEXT;

	@Column(name = "default_value")
	public String getDefaultValue() {
		return defaultValue;
	}

	@Column(name = "max_length")
	public Integer getMaxLength() {
		return this.maxLength;
	}

	@Column(name = "min_length")
	public Integer getMinLength() {
		return this.minLength;
	}

	@Column(name = "read_only", nullable = false)
	public Boolean getReadOnly() {
		return this.readOnly;
	}

	@Enumerated(EnumType.STRING)
	public MFInput.Type getType() {
		return type;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setType(MFInput.Type type) {
		this.type = type;
	}
}
