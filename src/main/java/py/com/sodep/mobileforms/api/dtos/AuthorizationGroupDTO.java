package py.com.sodep.mobileforms.api.dtos;

import java.util.ArrayList;
import java.util.List;


public class AuthorizationGroupDTO implements DTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private Integer columnNumbers;
	
	private List<AuthorizationDTO> authorizations=new ArrayList<AuthorizationDTO>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getColumnNumbers() {
		return columnNumbers;
	}

	public void setColumnNumbers(Integer columnNumbers) {
		this.columnNumbers = columnNumbers;
	}

	public List<AuthorizationDTO> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<AuthorizationDTO> authorizations) {
		this.authorizations = authorizations;
	}
	
	public void addAuthorization(AuthorizationDTO authorization) {
		this.authorizations.add(authorization);
	}

}
