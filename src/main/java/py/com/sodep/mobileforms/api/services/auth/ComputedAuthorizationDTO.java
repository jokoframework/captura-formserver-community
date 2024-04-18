package py.com.sodep.mobileforms.api.services.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This is a copy of the computed authorization that a user had on a given time.
 * Modifications to this object won't have any effect on the user authorizations
 * 
 * @author danicricco
 * 
 */
public class ComputedAuthorizationDTO {

	private List<HashMap<Long, Set<String>>> accessRights;

	ComputedAuthorizationDTO() {

	}

	public List<HashMap<Long, Set<String>>> getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(List<HashMap<Long, Set<String>>> accessRights) {
		this.accessRights = accessRights;
	}

}
