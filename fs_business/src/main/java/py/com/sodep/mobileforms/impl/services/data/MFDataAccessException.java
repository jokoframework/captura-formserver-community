package py.com.sodep.mobileforms.impl.services.data;

/**
 * This exception. is thrown if some data was expected but the data wasn't
 * there. For example trying to insert a DDL for an non exiting DataSetMetadata
 * 
 * @author danicricco
 * 
 */
public class MFDataAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MFDataAccessException(String msg) {
		super(msg);
	}

}
