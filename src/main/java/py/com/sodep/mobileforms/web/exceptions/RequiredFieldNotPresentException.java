package py.com.sodep.mobileforms.web.exceptions;

public class RequiredFieldNotPresentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequiredFieldNotPresentException() {
		super();
	}

	public RequiredFieldNotPresentException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequiredFieldNotPresentException(String message) {
		super(message);
	}

	public RequiredFieldNotPresentException(Throwable cause) {
		super(cause);
	}

}
