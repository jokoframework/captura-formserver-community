package py.com.sodep.mobileforms.api.entities.forms.page;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

/**
 * Defines the navigation between pages.
 * 
 * There could be default target or conditional targets.
 * 
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "flows")
@SequenceGenerator(name = "seq_flows", sequenceName = "forms.seq_flows")
public class Flow extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_flows")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	private String defaultTarget;

	private List<ConditionalTarget> targets = new ArrayList<ConditionalTarget>();

	@Column(name = "default_target")
	public String getDefaultTarget() {
		return defaultTarget;
	}

	public void setDefaultTarget(String defaultTarget) {
		this.defaultTarget = defaultTarget;
	}

	@OneToMany(cascade = { CascadeType.PERSIST })
	@JoinColumn(nullable = false, name = "flow_id")
	public List<ConditionalTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<ConditionalTarget> targets) {
		this.targets = targets;
	}

	@Override
	protected Flow clone() throws CloneNotSupportedException {
		Flow clone = (Flow) super.clone();
		clone.id = null;
		if (targets != null) {
			clone.targets = new ArrayList<ConditionalTarget>();
			for (ConditionalTarget f : targets) {
				ConditionalTarget flowTarget = f.clone();
				clone.targets.add(flowTarget);
			}
		}
		return clone;
	}

}
