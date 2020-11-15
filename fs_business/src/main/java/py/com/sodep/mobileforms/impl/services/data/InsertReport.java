package py.com.sodep.mobileforms.impl.services.data;

import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.services.data.StoreResult;

/**
 * This class contain the start an end sequence used during a transactional
 * insert. It is important to highlight that between the start and end sequence
 * other transactions might be running in parallel.
 * 
 * @author danicricco
 * 
 */
public class InsertReport  {

	private final Long sequenceStart;
	
	private final Long sequenceEnd;

	public InsertReport(Long sequenceStart, Long sequenceEnd) {
		super();
		this.sequenceStart = sequenceStart;
		this.sequenceEnd = sequenceEnd;
	}

	public Long getSequenceStart() {
		return sequenceStart;
	}

	public Long getSequenceEnd() {
		return sequenceEnd;
	}
	
	public int geAffectedRows() {
		int numberOfAffecteRows = 0;
		if (this.getSequenceEnd() >= 0) {
			// Zero is the first row ever inserted into a dataset
			numberOfAffecteRows  = (int) (this.getSequenceEnd() - this.getSequenceStart() + 1);
		} else {
			numberOfAffecteRows = 0;
		}
		
		return numberOfAffecteRows;
	}

	

	public StoreResult toStoreResult() {
		MFOperationResult mfOperationResult = new MFOperationResult(this.geAffectedRows());
		StoreResult storeResult = new StoreResult(mfOperationResult, sequenceEnd);
		return storeResult;
	}
	

}
