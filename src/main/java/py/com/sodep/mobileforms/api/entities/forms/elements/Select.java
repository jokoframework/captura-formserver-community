package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;

/**
 * The concrete implementation of an Element that shows a list of values. How it
 * looks depends on the style and if it allows to select multiple values.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "elements_selects")
public class Select extends ElementPrototype implements Serializable {

	public static final String DROPDOWN_RADIO_FIELD = "radio";
	public static final String DROPDOWN_TEXT_FIELD = "text";

	private static final long serialVersionUID = 1L;

	private Boolean multiple = false;

	private OptionSource source;

	private Long lookupIdentifier;

	private String lookupValue;

	private String lookupLabel;

	private String defaultValue;

	public Select() {
	}

	@Override
	public Select clone() throws CloneNotSupportedException {
		Select clone = (Select) super.clone();
		clone.id = null;
		return clone;
	}

	//FIXME what if we want the values of the select to be an integer
	// what other possible types are valid?
	@Column(name = "default_value")
	public String getDefaultValue() {
		return defaultValue;
	}

	@Column(name = "lookup_identifier")
	public Long getLookupTableId() {
		return lookupIdentifier;
	}

	@Column(name = "lookup_label")
	public String getLookupLabel() {
		return lookupLabel;
	}

	@Column(name = "lookup_value")
	public String getLookupValue() {
		return lookupValue;
	}

	@Column(nullable = false)
	public Boolean getMultiple() {
		return this.multiple;
	}

	@Column(name = "option_source")
	public OptionSource getSource() {
		return source;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setLookupTableId(Long lookupIdentifier) {
		this.lookupIdentifier = lookupIdentifier;
	}

	public void setLookupLabel(String lookupLabel) {
		this.lookupLabel = lookupLabel;
	}

	public void setLookupValue(String lookupValue) {
		this.lookupValue = lookupValue;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public void setSource(OptionSource source) {
		this.source = source;
	}
}