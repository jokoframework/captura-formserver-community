package py.com.sodep.mobileforms.web.endpoints.exceptions;

import py.com.sodep.mf.exchange.objects.error.ErrorResponse;
import py.com.sodep.mf.exchange.objects.error.ErrorType;

/**
 * Exception thrown by an Endpoint.
 * 
 * An instance of this Exception contains information to construct an
 * {@link ErrorResponse}, which gives the client information about what went
 * wrong
 * 
 * @author jmpr
 * 
 */
public class EndpointOperationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String key;

	private String[] params;

	private Integer responseStatusCode;

	private boolean invalidateSession;

	private ErrorType errorType = null;

	private boolean logToDB = false;

	public EndpointOperationException() {

	}

	public EndpointOperationException(boolean logToDB) {
		this.logToDB = logToDB;
	}

	public EndpointOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * i18n Key of the message to send to the device
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Parameters for the message
	 * 
	 * @return
	 */
	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	/**
	 * HTTP status code of the response
	 * 
	 * @return
	 */
	public Integer getResponseStatusCode() {
		return responseStatusCode;
	}

	public void setResponseStatusCode(Integer responseStatusCode) {
		this.responseStatusCode = responseStatusCode;
	}

	/**
	 * Whether to suggest the device to invalidate session or not
	 * 
	 * @return
	 */
	public boolean isInvalidateSession() {
		return invalidateSession;
	}

	public void setInvalidateSession(boolean invalidateSession) {
		this.invalidateSession = invalidateSession;
	}

	/**
	 * An enumeration of known errors
	 * 
	 * @return
	 */
	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	/**
	 * Whether this Exception should be logged to the database
	 * 
	 * @return
	 */
	public boolean isLogToDB() {
		return logToDB;
	}

	public void setLogToDB(boolean logToDB) {
		this.logToDB = logToDB;
	}

}
