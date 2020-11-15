package py.com.sodep.mobileforms.api.entities.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to read the authorization configuration from a JSON file.
 * There are two options to configure:
 * <ul>
 * <li>hiddenAuthorizations: Include here a list of elements that should NOT be
 * visible on the role configuration page</li>
 * <li>dependentAuthorization: Granting a given authorization will grant also
 * grant all the authorizations listed here</li>
 * </ul>
 * 
 * @author danicricco
 * 
 */
public class AuthorizationConfiguration {

	/**
	 * Authorization that should not be displayed to the user
	 */
	private List<String> hiddenAuthorizations;

	private Map<String, Set<String>> dependentAuthorization;

	public AuthorizationConfiguration() {
		hiddenAuthorizations = new ArrayList<String>();
		dependentAuthorization = new HashMap<String, Set<String>>();
	}

	public List<String> getHiddenAuthorizations() {
		return hiddenAuthorizations;
	}

	public void setHiddenAuthorizations(List<String> hiddenAuthorizations) {
		this.hiddenAuthorizations = hiddenAuthorizations;
	}

	public Map<String, Set<String>> getDependentAuthorization() {
		return dependentAuthorization;
	}

	public void setDependentAuthorization(Map<String, Set<String>> dependentAuthorization) {
		this.dependentAuthorization = dependentAuthorization;
	}

	public void hideAuthorization(String auth) {
		hiddenAuthorizations.add(auth);
	}

	public void addDependency(String authBased, String authGranted) {
		Set<String> grantedAuth = dependentAuthorization.get(authBased);
		if (grantedAuth == null) {
			grantedAuth = new TreeSet<String>();
			dependentAuthorization.put(authBased, grantedAuth);
		}
		grantedAuth.add(authGranted);
	}
}
