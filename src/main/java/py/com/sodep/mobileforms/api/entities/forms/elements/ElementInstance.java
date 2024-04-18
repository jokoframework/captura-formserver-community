package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.dynamicvalues.Filter;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.IInstanceId;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype.InstantiabilityType;

@Entity
@Table(schema = "forms", name = "element_instances")
@SequenceGenerator(name = "seq_element_instances", sequenceName = "forms.seq_element_instances")
public class ElementInstance extends SodepEntity implements IInstanceId {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ElementPrototype prototype;

	private Integer position;

	private Boolean visible;

	private Boolean required;

	private String fieldName;

	private Long defaultValueLookup;

	private String defaultValueColumn;

	private String instanceId;

	private List<Filter> filters;
	
	public ElementInstance(){
		
	}

	@Override
	public void prePersist() {
		super.prePersist();
		if (prototype.getInstantiability() == InstantiabilityType.TEMPLATE) {
			throw new RuntimeException("A template prototype cannot be a template");
		}
	}

	@Override
	public void preUpdate() {
		super.preUpdate();
		if (prototype.getInstantiability() == InstantiabilityType.TEMPLATE) {
			throw new RuntimeException("A template prototype cannot be a template");
		}
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_element_instances")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "prototype_id")
	public ElementPrototype getPrototype() {
		return prototype;
	}

	public void setPrototype(ElementPrototype prototype) {
		this.prototype = prototype;
	}

	@Column(nullable = false)
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	@Column(name = "field_name")
	public String getFieldName() {
		if (fieldName == null) {
			return getInstanceId();
		}
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public ElementInstance clone() throws CloneNotSupportedException {
		ElementInstance clone = (ElementInstance) super.clone();
		clone.id = null;
		if (prototype.getInstantiability().equals(ElementPrototype.InstantiabilityType.EMBEDDED)) {
			clone.prototype = prototype.clone();
		}
		if (filters != null && !filters.isEmpty()) {
			clone.filters = new ArrayList<Filter>();
			for (Filter f : filters) {
				Filter fClone = f.clone();
				fClone.setElementInstance(clone);
				clone.filters.add(fClone);
			}
		} else {
			clone.filters = null;
		}
		return clone;
	}

	@Transient
	public Boolean isEmbedded() {
		return prototype.isEmbedded();
	}

	// Long ago I created this field but for some
	// reason I wasn't told of, it was made transient. It utterly
	// defeated the original purpose.
	//
	// jmpr 22/12/2012
	@Override
	@Column(name = "instance_id")
	public String getInstanceId() {
		if (instanceId != null) {
			return instanceId;
		}
		if (id != null) {
			return "element" + id;
		}
		return null;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@OneToMany(mappedBy = "elementInstance", cascade = { CascadeType.PERSIST })
	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	@Column(name="default_value_lookup")
	public Long getDefaultValueLookupTableId() {
		return defaultValueLookup;
	}

	public void setDefaultValueLookupTableId(Long defaultValueLookup) {
		this.defaultValueLookup = defaultValueLookup;
	}

	@Column(name="default_value_column")
	public String getDefaultValueColumn() {
		return defaultValueColumn;
	}

	public void setDefaultValueColumn(String defaultValueColumn) {
		this.defaultValueColumn = defaultValueColumn;
	}

}
