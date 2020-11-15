package py.com.sodep.mobileforms.api.exceptions;

public class InvalidDatabaseStateException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidDatabaseStateException() {
		super();
	}

	public InvalidDatabaseStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDatabaseStateException(String message) {
		super(message);
	}

	public InvalidDatabaseStateException(Throwable cause) {
		super(cause);
	}
}
