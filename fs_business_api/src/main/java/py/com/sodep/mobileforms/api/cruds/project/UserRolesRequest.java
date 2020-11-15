package py.com.sodep.mobileforms.api.cruds.project;

import java.util.List;

public class UserRolesRequest {
	private Integer userId;
	private List<Integer> rolesId;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public List<Integer> getRolesId() {
		return rolesId;
	}
	public void setRolesId(List<Integer> rolesId) {
		this.rolesId = rolesId;
	}
	
	
	
}
