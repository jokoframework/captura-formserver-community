package py.com.sodep.mobileforms.api.exceptions;

import java.util.HashMap;
import java.util.Map;

public class SodepServiceOriginatedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<String, String> messages = new HashMap<String, String>();
	
	
	public SodepServiceOriginatedException() {
		super();
	}

	public SodepServiceOriginatedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SodepServiceOriginatedException(String message) {
		super(message);
	}

	public SodepServiceOriginatedException(Throwable cause) {
		super(cause);
	}

	/**
	 * The key should be the name of the offending property and the value is the
	 * i18n key of a message to be shown
	 * 
	 * @return A map of error messages
	 */
	public Map<String, String> getMessages() {
		return messages;
	}

	public void setMessages(Map<String, String> messages) {
		this.messages = messages;
	}

	public void addMessage(String key, String value) {
		messages.put(key, value);
	}

}
