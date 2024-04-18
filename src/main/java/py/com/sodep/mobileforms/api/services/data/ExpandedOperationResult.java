package py.com.sodep.mobileforms.api.services.data;

import java.util.List;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;

public class ExpandedOperationResult {

	private MFOperationResult mfOperationResult;

	public static ExpandedOperationResult get(MFOperationResult mfOperationResult) {
		ExpandedOperationResult result = new ExpandedOperationResult();
		result.setMfOperationResult(mfOperationResult);
		return result;
	}

	
	private void setMfOperationResult(MFOperationResult mfOperationResult) {
		this.mfOperationResult = mfOperationResult;
	}

	public List<RowCheckError> getErrors() {
		return mfOperationResult.getErrors();
	}

	
	public boolean hasFailed() {
		RESULT result = this.mfOperationResult.getResult();
		if (result.equals(RESULT.FAIL)) {
			return true;
		}
		return false;
	}

	public boolean hasSucceeded() {
		RESULT result = this.mfOperationResult.getResult();
		if (result.equals(RESULT.SUCCESS)) {
			return true;
		}
		return false;
	}

	public boolean hasTimedOut() {
		RESULT result = this.mfOperationResult.getResult();
		
		if (result.equals(RESULT.TIMEOUT)) {
			return true;
		}
		return false;
	}

	public RESULT getResult() {
		return this.mfOperationResult.getResult();
	}

	
	public String getMsg() {
		return this.mfOperationResult.getMsg();
	}


	public MFOperationResult getMfOperationResult() {
		return this.mfOperationResult;
	}


	public Object getNumberOfAffectedRows() {
		return this.mfOperationResult.getNumberOfAffectedRows();
	}

	
}
