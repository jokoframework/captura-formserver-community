package py.com.sodep.mobileforms.api.exceptions;

public class MailException extends SodepServiceOriginatedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MailException() {
		super();
	}

	public MailException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailException(String message) {
		super(message);
	}

	public MailException(Throwable cause) {
		super(cause);
	}

}
