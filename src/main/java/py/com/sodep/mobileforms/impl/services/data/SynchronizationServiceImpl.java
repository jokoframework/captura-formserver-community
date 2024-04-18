package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.MFManagedDataBasic;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransportMultiple;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.api.services.data.Transaction;

import com.mongodb.DB;

/**
 * This class is in charge of synchronizing the data of dataSet to different
 * destinations. A dataSet can be a lookup table or a form, and this service
 * doesn't make any difference. In the same way a destination can be a device or
 * connector repository
 * 
 * @author danicricco
 * 
 */
@Component
public class SynchronizationServiceImpl implements SynchronizationService {

	private static final Logger logger = LoggerFactory.getLogger(SynchronizationServiceImpl.class);

	@Autowired
	private TransactionManager txManager;

	@Autowired
	private IDataAccessService dataAccessService;

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.services.data.SynchronizationService#downloadDataMultiple(java.lang.Long, py.com.sodep.mf.exchange.MFDataSetDefinition, java.lang.String, java.lang.Long, int)
	 */
	@Override
	public MFDMLTransportMultiple downloadDataMultiple(Long lookupId, MFDataSetDefinition ddl, String txId,
			Long lastReceivedRow, int maxNumberOfData) {
		int countOfData = 0;
		boolean isSynch = false;
		ArrayList<MFDMLTransport> dmlTransportList = new ArrayList<MFDMLTransport>();

		TXInfo lastTxInfo = new TXInfo(lookupId);
		lastTxInfo.setEndRow(lastReceivedRow);
		lastTxInfo.setTx(txId);
		long elapsedTime = System.currentTimeMillis();
		while (countOfData < maxNumberOfData && !isSynch) {
			// Try to fetch as many transaction as possible

			MFDMLTransport dmlTransport = downloadData(ddl, lastTxInfo.getTx(), lastTxInfo.getEndRow(), maxNumberOfData);
			dmlTransport.getTxInfo().setLookupTable(lookupId);
			dmlTransportList.add(dmlTransport);
			isSynch = dmlTransport.isSynch();
			if (dmlTransport.getData() != null) {
				countOfData += dmlTransport.getData().size();
			}

			// Try to move to the next transaction
			// this is what the device does if it calls the method downloadData.
			// We are simlating multiple calls of the device to avoid roundtrips
			lastTxInfo = dmlTransport.getTxInfo();
		}
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		logger.debug("Computed missing transactions for lookup #" + lookupId + ". Elapsed time = " + elapsedTime
				+ " ms. ");
		return new MFDMLTransportMultiple(dmlTransportList);
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.services.data.SynchronizationService#downloadData(py.com.sodep.mf.exchange.MFDataSetDefinition, java.lang.String, java.lang.Long, int)
	 */
	@Override
	public MFDMLTransport downloadData(MFDataSetDefinition ddl, String txId, Long lastReceivedRow, int maxNumberOfData) {
		logger.trace("Calculating transactions missing after tx #"+txId+", row end= "+lastReceivedRow );
		DB db = dataAccessService.getMongoConnection();
		Transaction tx;
		// the method will obtain rows that were affected by a transaction. We
		// use rowToStart = 0 (by default) to force all rows of the transaction
		// Note that this doesn't mean that the row 0 will be returned from
		// dataAccessService.listDataOfTx

		long rowToStart = 0;
		boolean isSynch = false;
		if (txId != null) {
			tx = txManager.getTransaction(db, txId);
			if (lastReceivedRow < tx.getRowEnd()) {
				// there are pending data on the transactions tx
				// Although we are not sure that the next row of the transaction
				// will have +1, it is just a lower bound
				rowToStart = lastReceivedRow + 1;
			} else {
				// move to the next transaction
				long elapsedTime = System.currentTimeMillis();
				Transaction nextTx = txManager.getNextTransaction(db, txId);
				elapsedTime = System.currentTimeMillis() - elapsedTime;
				logger.trace("Next tx Elapsed time " + elapsedTime);
				if (nextTx == null) {
					// There are no more transactions for the lookup table.
					// Therefore we set the flag of synchronized
					isSynch = true;
				} else {
					// advance to the next transaction
					tx = nextTx;
				}
			}
		} else {
			long elapsedTime = System.currentTimeMillis();
			MFDataSetDefinitionMongo ddlOnMongo = dataAccessService.getDataSetDefinition(ddl.getMetaDataRef(),
					ddl.getVersion());
			tx = txManager.getFirstTransaction(db, ddlOnMongo);
			elapsedTime = System.currentTimeMillis() - elapsedTime;
			logger.trace("First tx Elapsed time " + elapsedTime);
		}

		MFDMLTransport dmlTransport = new MFDMLTransport();
		TXInfo txInfo = new TXInfo();
		txInfo.setTx(tx.getId());
		txInfo.setOperation(tx.getType());

		dmlTransport.setTxInfo(txInfo);

		if (isSynch) {
			dmlTransport.setSynch(true);
			// This is convenient for the mobile devices, so they can save the
			// last transaction info and use it to query the next time
			txInfo.setEndRow(tx.getRowEnd());
			txInfo.setStartRow(tx.getRowStart());
		} else {
			if (rowToStart == 0) {
				txInfo.setStartRow(tx.getRowStart());
			} else {
				// the start row is some intermediate point of the transaction

				txInfo.setStartRow(rowToStart);
			}

			long elapsedTime = System.currentTimeMillis();
			List<MFManagedData> data = dataAccessService.listDataOfTx(tx, rowToStart, maxNumberOfData, true);
			elapsedTime = System.currentTimeMillis() - elapsedTime;
			logger.trace("List data Elapsed time " + elapsedTime);
			logger.trace("Obtained data: " + data);
			if (data != null && data.size() > 0) {

				// Even-though we are asking for data of the transaction, we
				// need to check that the data is not null since it might
				// get deleted
				dmlTransport.setData(transform(data));
				MFManagedData lastRow = data.get(data.size() - 1);
				txInfo.setEndRow(lastRow.getRowId());
				elapsedTime = System.currentTimeMillis();

				MFManagedData lastExistentRow = getLastExistentRow(tx);
				elapsedTime = System.currentTimeMillis() - elapsedTime;
				logger.trace("Get last existent row time " + elapsedTime);
				if (lastExistentRow != null) {
					if (lastExistentRow.getRowId() <= lastRow.getRowId()) {
						// Most of the time the lastExistenceRow should be equal
						// to the lastRow in order to finish the transaction.
						// However, the user might have deleted data in the
						// meantime, hence the lastExistenceRow might be lower
						// than the lastRow (this also mean that this is the end
						// of this transaction)
						dmlTransport.setFinal(true);
						txInfo.setEndRow(tx.getRowEnd());
					}

				} else {
					// since we have data for the transaction it shouldn't be
					// possible not to have at least one row
					// However, we are in a parallel world and somebody might
					// have asked on the "apropriate time" to delete the rows
					// We are going to commit this data and the next transaction
					// might delete them
					dmlTransport.setFinal(true);
				}

			} else {
				// if there are no longer data associated with this
				// transaction
				// Then we will mark as commit and set the end row. This way
				// the next call to downloadData will move to the next
				// available trasnsaction (if any)
				txInfo.setStartRow(tx.getRowStart());
				txInfo.setEndRow(tx.getRowEnd());
				dmlTransport.setFinal(true);
				// Note that a delete transaction will never have associated
				// data
			}

		}
		return dmlTransport;
	}

	/**
	 * This method will return the last row of a transaction that has not been
	 * deleted.
	 * 
	 * @param tx
	 * @return
	 */
	private MFManagedData getLastExistentRow(Transaction tx) {
		// We list of all rows from the transaciton in inverse order, and we are
		// only asking one row. Threfore, this should return the last available
		// row
		if (tx.getRowStart() == null) {
			// This is just a condition to detect a bug
			throw new IllegalArgumentException("rowStart of the tx " + tx.getId() + " is null");
		}
		List<MFManagedData> data = dataAccessService.listDataOfTx(tx, tx.getRowStart(), 1, false);
		if (data != null && data.size() > 0) {
			return data.get(0);
		} else {
			return null;
		}
	}

	public static List<MFManagedDataBasic> transform(List<MFManagedData> l) {
		ArrayList<MFManagedDataBasic> list = new ArrayList<MFManagedDataBasic>();
		for (MFManagedData d : l) {
			list.add(new MFManagedDataBasic(d));
		}
		return list;
	}
}
