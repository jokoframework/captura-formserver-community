package py.com.sodep.mobileforms.api.exceptions;

import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;

/**
 * This Exception is thrown when a user doesn't have authorization over the
 * resource or is not allowed to do an action
 * 
 * @see {@link IAuthorizationControlService}
 * @author Miguel
 * 
 */
public class AuthorizationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthorizationException() {
		super();
	}

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationException(String authorization) {
		super("User does not have authorization '"+ authorization + "'");
	}

	public AuthorizationException(Throwable cause) {
		super(cause);
	}

}
