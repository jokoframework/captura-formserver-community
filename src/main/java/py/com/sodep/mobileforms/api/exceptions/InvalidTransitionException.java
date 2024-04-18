package py.com.sodep.mobileforms.api.exceptions;

public class InvalidTransitionException extends SodepServiceOriginatedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTransitionException() {
		super();
	}

	public InvalidTransitionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTransitionException(String message) {
		super(message);
	}

	public InvalidTransitionException(Throwable cause) {
		super(cause);
	}
}
