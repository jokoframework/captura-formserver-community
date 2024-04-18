package py.com.sodep.mobileforms.impl.services.data;

/**
 * There was an unexpected error during the insertion of data
 * 
 * @author danicricco
 * 
 */
public class DataInsertUnexpecteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataInsertUnexpecteException(String msg) {
		super(msg);
	}

	public DataInsertUnexpecteException(String msg, Throwable e) {
		super(msg, e);
	}
}
