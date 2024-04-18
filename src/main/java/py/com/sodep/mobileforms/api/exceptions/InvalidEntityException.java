package py.com.sodep.mobileforms.api.exceptions;


/**
 * This Exception is thrown by the Service layer, when trying to persist an
 * Entity, if the entity doesn't comply with some business rule or invariant.
 * e.g. When registering a user, before trying to persist the entity a check
 * could be made to look for a user with the same e-mail address. If the user
 * already exists, throwing this Exception is suitable.
 * 
 * This Exception has a Map<String, String> of messages. The idea is to use the
 * name of the offending property as the key and the i18n key to the message as
 * the value (Not the message it self, i18n!)
 * 
 * @author Miguel
 * 
 */
public class InvalidEntityException extends SodepServiceOriginatedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidEntityException() {
		super();
	}

	public InvalidEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidEntityException(String message) {
		super(message);
	}

	public InvalidEntityException(Throwable cause) {
		super(cause);
	}

}
