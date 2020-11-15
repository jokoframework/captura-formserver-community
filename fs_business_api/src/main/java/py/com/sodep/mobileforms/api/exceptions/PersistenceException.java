package py.com.sodep.mobileforms.api.exceptions;

/**
 * This type of Exception is thrown by the Service layer when an error occurs
 * while trying to persist, update or delete an Entity.Is a general error. When
 * possible a more specific Exception should be thrown, for example
 * {@link InvalidEntityException}
 * 
 * It should be used to wrap JPA Exceptions. Thus, avoiding the dependency of
 * the UI/WEB layer on JPA API.
 * 
 * @author Miguel
 * 
 */
public class PersistenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PersistenceException() {
		super();
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}

}
