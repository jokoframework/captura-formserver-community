package py.com.sodep.mobileforms.api.services.data;

import java.util.List;

public class LookuptableOperationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1l;

	public static final int LOOKUP_IN_USE = 0;

	private final int errorCode;

	private final List<Long> formInUse;

	public LookuptableOperationException(int errorCode, List<Long> formInUse) {
		this.errorCode = errorCode;
		this.formInUse = formInUse;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public List<Long> getFormInUse() {
		return formInUse;
	}

}
