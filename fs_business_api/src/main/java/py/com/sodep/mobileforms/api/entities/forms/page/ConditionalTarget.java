package py.com.sodep.mobileforms.api.entities.forms.page;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mf.form.model.flow.MFConditionalTarget;
import py.com.sodep.mobileforms.api.entities.SodepEntity;

/**
 * Gives the capability of conditional navigation.
 * 
 * "conditional navigation" = given some element's value, if it meets certain
 * criteria, go to X page else go to Y.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "flows_targets")
@SequenceGenerator(name = "seq_flows_targets", sequenceName = "forms.seq_flows_targets")
public class ConditionalTarget extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String target;

	private MFConditionalTarget.Action preAction;

	private String elementId;

	private MFConditionalTarget.Operator operator;

	private String value;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_flows_targets")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Enumerated(EnumType.STRING)
	public MFConditionalTarget.Action getPreAction() {
		return preAction;
	}

	public void setPreAction(MFConditionalTarget.Action preAction) {
		this.preAction = preAction;
	}

	@Column(nullable = false, name = "element_id")
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Column(nullable = false, name = "operator")
	@Enumerated(EnumType.STRING)
	public MFConditionalTarget.Operator getOperator() {
		return operator;
	}

	public void setOperator(MFConditionalTarget.Operator operator) {
		this.operator = operator;
	}

	@Column(nullable = false, name = "element_value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected ConditionalTarget clone() throws CloneNotSupportedException {
		ConditionalTarget clone = (ConditionalTarget) super.clone();
		clone.id = null;
		return clone;
	}

}
