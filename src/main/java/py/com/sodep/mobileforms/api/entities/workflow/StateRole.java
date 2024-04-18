package py.com.sodep.mobileforms.api.entities.workflow;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema = "workflow", name = "states_roles")
public class StateRole {
	
	private StateRoleId id;
	
	private State state;
	
	public StateRole() {
	}

	public StateRole(StateRoleId id, State state) {
		this.id = id;
		this.state = state;
	}

	@EmbeddedId
	@AttributeOverrides( { @AttributeOverride(name = "stateId", column = @Column(name = "state_id", nullable = false)),
			@AttributeOverride(name = "roleId", column = @Column(name = "role_id", nullable = false)) })
	public StateRoleId getId() {
		return id;
	}

	public void setId(StateRoleId id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "state_id", nullable = false, insertable = false, updatable = false)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}
