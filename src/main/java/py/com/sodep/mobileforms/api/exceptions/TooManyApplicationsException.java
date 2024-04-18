package py.com.sodep.mobileforms.api.exceptions;

public class TooManyApplicationsException extends LicenseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long maxCount;

	public TooManyApplicationsException() {
		super();
	}

	public TooManyApplicationsException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyApplicationsException(String message) {
		super(message);
	}

	public TooManyApplicationsException(Throwable cause) {
		super(cause);
	}

	public long getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(long maxCount) {
		this.maxCount = maxCount;
	}

}
