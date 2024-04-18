package py.com.sodep.mobileforms.api.exceptions;


public class DuplicateEntityException extends InvalidEntityException {

	private static final long serialVersionUID = 1L;

	public DuplicateEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateEntityException(String message) {
		super(message);

	}

	public DuplicateEntityException(Throwable cause) {
		super(cause);

	}
}
