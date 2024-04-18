package py.com.sodep.mobileforms.api.entities.workflow;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema = "workflow", name = "transitions_roles")
public class TransitionRole {
	
	private TransitionRoleId id;
	
	private Transition transition;
	
	public TransitionRole() {
	}

	public TransitionRole(TransitionRoleId id, Transition transition) {
		this.id = id;
		this.transition = transition;
	}
	
	@EmbeddedId
	@AttributeOverrides( { @AttributeOverride(name = "transitionId", column = @Column(name = "transition_id", nullable = false)),
		@AttributeOverride(name = "roleId", column = @Column(name = "role_id", nullable = false)) })
	public TransitionRoleId getId() {
		return id;
	}

	public void setId(TransitionRoleId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transition_id", nullable = false, insertable = false, updatable = false)
	public Transition getTransition() {
		return transition;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}

}
