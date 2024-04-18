package py.com.sodep.mobileforms.api.cruds.project;

import java.util.List;

public class GroupRolesRequest {
	private Integer groupId;
	private List<Integer> rolesId;
	
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public List<Integer> getRolesId() {
		return rolesId;
	}
	public void setRolesId(List<Integer> rolesId) {
		this.rolesId = rolesId;
	}
	
	

}
