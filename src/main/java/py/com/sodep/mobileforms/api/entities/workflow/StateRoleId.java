package py.com.sodep.mobileforms.api.entities.workflow;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class StateRoleId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long stateId;
	
	private Long roleId;
	
	public StateRoleId() {
	}

	/** full constructor */
	public StateRoleId(Long stateId, Long roleId) {
		this.stateId = stateId;
		this.roleId = roleId;
	}

	@Column(name = "state_id", nullable = false)
	public Long getStateId() {
		return stateId;
	}

	public void setStateId(Long stateId) {
		this.stateId = stateId;
	}

	@Column(name = "role_id", nullable = false)
	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
}
