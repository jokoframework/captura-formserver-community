package py.com.sodep.mobileforms.api.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String description;
	private String Level;
	private Boolean active;

	private List<AuthorizationDTO> grants = new ArrayList<AuthorizationDTO>();

	private Long applicationId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLevel() {
		return Level;
	}

	public void setLevel(String level) {
		Level = level;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	
	public List<AuthorizationDTO> getGrants() {
		return grants;
	}

	public void setGrants(List<AuthorizationDTO> grants) {
		this.grants = grants;
	}

	public void addGrant(AuthorizationDTO grant) {
		this.grants.add(grant);
	}

}
