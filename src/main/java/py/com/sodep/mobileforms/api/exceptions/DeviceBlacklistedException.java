package py.com.sodep.mobileforms.api.exceptions;

public class DeviceBlacklistedException extends SodepServiceOriginatedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceBlacklistedException() {
		super();
	}

	public DeviceBlacklistedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceBlacklistedException(String message) {
		super(message);
	}

	public DeviceBlacklistedException(Throwable cause) {
		super(cause);
	}

}
