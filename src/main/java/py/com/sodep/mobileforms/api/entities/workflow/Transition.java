package py.com.sodep.mobileforms.api.entities.workflow;

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

import py.com.sodep.mobileforms.api.entities.SodepEntity;

@Entity
@Table(schema = "workflow", name = "transitions")
@SequenceGenerator(name = "seq_transitions", sequenceName = "workflow.seq_transitions")
public class Transition extends SodepEntity {

	private static final long serialVersionUID = 1L;
	
	private String description;
	
	private State originState;
	
	private State targetState;
	
	private Long formId;
	
	private List<TransitionRole> transitionRoles;
	
	public Transition() {
	}

	public Transition(Long formId, String description, State originState,
			State targetState) {
		this.formId = formId;
		this.description = description;
		this.originState = originState;
		this.targetState = targetState;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_transitions")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne
	@JoinColumn(name = "origin_state")
	public State getOriginState() {
		return originState;
	}

	public void setOriginState(State originState) {
		this.originState = originState;
	}

	@ManyToOne
	@JoinColumn(name = "target_state", nullable = false)
	public State getTargetState() {
		return targetState;
	}

	public void setTargetState(State targetState) {
		this.targetState = targetState;
	}

	@Column(name = "form_id", nullable = false)
	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "transition")
	public List<TransitionRole> getTransitionRoles() {
		return transitionRoles;
	}

	public void setTransitionRoles(List<TransitionRole> transitionRoles) {
		this.transitionRoles = transitionRoles;
	}

}
