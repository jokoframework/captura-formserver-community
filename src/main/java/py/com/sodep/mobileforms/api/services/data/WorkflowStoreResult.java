package py.com.sodep.mobileforms.api.services.data;

import java.util.List;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;

public class WorkflowStoreResult {
	
	private final MFOperationResult mfOperaationResult;
	
	private final Long lastStoredRowId;
	
	private final Long oldStateId;
	
	private final Long newStateId;

	public WorkflowStoreResult(MFOperationResult mfOperationResult, Long lastStoredRowId, Long oldStateId, Long newStateId) {
		this.mfOperaationResult = mfOperationResult;
		this.lastStoredRowId = lastStoredRowId;
		this.oldStateId = oldStateId;
		this.newStateId = newStateId;
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

	/**
	 * @return the oldStateId
	 */
	public Long getOldStateId() {
		return oldStateId;
	}

	/**
	 * @return the newStateId
	 */
	public Long getNewStateId() {
		return newStateId;
	}
}
