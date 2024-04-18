package py.com.sodep.mobileforms.api.entities.workflow;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TransitionRoleId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long transitionId;
	
	private Long roleId;
	
	public TransitionRoleId () {
	}

	public TransitionRoleId(Long transitionId, Long roleId) {
		this.transitionId = transitionId;
		this.roleId = roleId;
	}



	@Column(name = "transition_id", nullable = false)
	public Long getTransitionId() {
		return transitionId;
	}

	public void setTransitionId(Long transitionId) {
		this.transitionId = transitionId;
	}
	
	@Column(name = "role_id", nullable = false)
	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
	
	
}
