package py.com.sodep.mobileforms.api.services.data;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransportMultiple;

public interface SynchronizationService {

	public MFDMLTransportMultiple downloadDataMultiple(Long lookupId, MFDataSetDefinition ddl, String txId,
			Long lastReceivedRow, int maxNumberOfData);

	/**
	 * This method compute the missing data for a metaDataRef starting on a
	 * given transaction. It is assumed that data of previous transaction are
	 * not needed. If txStart is the last row modified by txStart, then the
	 * method will return data from the next transaction until no more
	 * transaction are available. The method will return at most
	 * maxNumberOfData.
	 * 
	 * @param metaDataRef
	 * @param txStart
	 * @param rowStart
	 *            a valid row between the rows modified by the the transaction
	 *            txStart
	 * @param numberOfData
	 */
	public MFDMLTransport downloadData(MFDataSetDefinition ddl, String txId, Long lastReceivedRow, int maxNumberOfData);

}