package py.com.sodep.mobileforms.api.services.data;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;

public class StoreResult {

	private MFOperationResult mfOperaationResult;
	
	private Long lastStoredRowId;

	public StoreResult(MFOperationResult mfOperationResult, Long lastStoredRowId) {
		this.mfOperaationResult = mfOperationResult;
		this.lastStoredRowId = lastStoredRowId;
	}
	
	public StoreResult(ArrayList<RowCheckError> errors) {
		this.mfOperaationResult = new MFOperationResult(errors);
		this.lastStoredRowId = 0L;
	}

	public StoreResult() {
		this.mfOperaationResult = new MFOperationResult();
		this.lastStoredRowId = 0L;
	}

	public int getNumberOfAffectedRows() {
		return mfOperaationResult.getNumberOfAffectedRows();
	}

	public List<RowCheckError> getErrors() {
		return mfOperaationResult.getErrors();
	}

	public boolean hasFailed() {
		return mfOperaationResult.hasFailed();
		
	}

	public boolean hasSucceeded() {
		return mfOperaationResult.hasSucceeded();
		
	}

	public boolean hasTimedOut() {
		return mfOperaationResult.hasTimedOut();
	}

	public RESULT getResult() {
		return mfOperaationResult.getResult();
	}
	
	public String getMsg() {
		return mfOperaationResult.getMsg();
	}

	public Long getLastStoredRowId() {
		return this.lastStoredRowId;
	}

	public MFOperationResult getMfOperationResult() {
		return this.mfOperaationResult;
	}
}
