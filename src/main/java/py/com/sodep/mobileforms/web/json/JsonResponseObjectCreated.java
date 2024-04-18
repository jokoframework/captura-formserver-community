package py.com.sodep.mobileforms.web.json;

import py.com.sodep.mobileforms.api.services.auth.ComputedAuthorizationDTO;

/**
 * <p>
 * After a user has created an object the system will automatically grant to him
 * some access, therefore we need to apply authorization on the browser side.
 * Otherwise, the user will perceive that he doesn't have authorization over its
 * newly created object.
 * </p>
 * 
 * @author danicricco
 * 
 */
public class JsonResponseObjectCreated<T> extends JsonResponse<T> {

	private ComputedAuthorizationDTO computedAuthorizations;

	public ComputedAuthorizationDTO getComputedAuthorizations() {
		return computedAuthorizations;
	}

	public void setComputedAuthorizations(ComputedAuthorizationDTO authorization) {
		this.computedAuthorizations = authorization;
	}

}
