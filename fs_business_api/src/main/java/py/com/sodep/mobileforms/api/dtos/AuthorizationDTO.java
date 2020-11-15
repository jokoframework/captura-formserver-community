package py.com.sodep.mobileforms.api.dtos;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private String description;

	private List<AuthorizationDTO> dependantAuthorization = new ArrayList<AuthorizationDTO>();

	private boolean visible;

	public AuthorizationDTO() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public List<AuthorizationDTO> getDependantAuthorization() {
		return dependantAuthorization;
	}

	public void setDependantAuthorization(List<AuthorizationDTO> dependantAuthorization) {
		this.dependantAuthorization = dependantAuthorization;
	}

}
