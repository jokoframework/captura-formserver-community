package py.com.sodep.mobileforms.api.entities.dynamicvalues;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mf.form.model.element.filter.MFFilter;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;

@Entity
@Table(schema = "forms", name = "elements_filters")
@SequenceGenerator(name = "seq_elements_filters", sequenceName = "forms.seq_elements_filters")
public class Filter implements Cloneable {

	private Long id;

	private ElementInstance elementInstance;

	private MFFilter.Type type;

	private String column;

	private MFFilter.Operator operator;

//	The right value cannot be identified by it's ElementInstance because of the cloning process!
//	private ElementInstance rightValue;

	private String rightValue;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_elements_filters")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "comparison_operator", nullable = false)
	public MFFilter.Operator getOperator() {
		return operator;
	}

	public void setOperator(MFFilter.Operator operator) {
		this.operator = operator;
	}

	@Column(name = "column_name", nullable = false)
	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	@Column(name = "filter_type", nullable = false)
	public MFFilter.Type getType() {
		return type;
	}

	public void setType(MFFilter.Type type) {
		this.type = type;
	}

	@ManyToOne
	@JoinColumn(name = "element_instance_id")
	public ElementInstance getElementInstance() {
		return elementInstance;
	}

	public void setElementInstance(ElementInstance elementInstance) {
		this.elementInstance = elementInstance;
	}

//	@ManyToOne
//	@JoinColumn(name = "right_value")
//	public ElementInstance getRightValue() {
//		return rightValue;
//	}
//
//	public void setRightValue(ElementInstance rightValue) {
//		this.rightValue = rightValue;
//	}

	@Override
	public Filter clone() throws CloneNotSupportedException {
		Filter clone = (Filter) super.clone();
		clone.id = null;
		return clone;
	}

	@Column(name="right_value")
	public String getRightValue() {
		return rightValue;
	}

	public void setRightValue(String rightValue) {
		this.rightValue = rightValue;
	}

}
