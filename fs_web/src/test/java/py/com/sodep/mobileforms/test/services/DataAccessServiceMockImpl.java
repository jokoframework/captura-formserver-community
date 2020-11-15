package py.com.sodep.mobileforms.test.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.impl.services.data.DataAccessService;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionReport;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionService;
import py.com.sodep.mobileforms.impl.services.data.DataSetDeleteReport;
import py.com.sodep.mobileforms.impl.services.data.TransactionManager;

import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * This is a specialization of a {@link DataAccessService} that allows to
 * specify if an insert should be rollback or commit. The class is only useful
 * for testing purposes
 * 
 * @author danicricco
 * 
 */
public class DataAccessServiceMockImpl extends DataAccessService implements IDataAccessServiceMock {

	// If n rows are inserted an exception will be thrown when trying to insert
	// the register rowToFail
	private int rowToFail;
	// if this is true an excetion will be thrown when trying to insert the
	// register #rowToFail
	private boolean fail;

	public DataAccessServiceMockImpl(TransactionManager transactionManager) {
		super(transactionManager);

	}

	@Override
	protected boolean checkError(int n, Map<String, Object> data, Map<String, ?> meta) {
		if (fail && n == rowToFail) {
			throw new RuntimeException("Test Exception during the insert process");
		}
		return true;
	}

	public int getRowToFail() {
		return rowToFail;
	}

	public void setRowToFail(int rowToFail) {
		this.rowToFail = rowToFail;
	}

	public boolean isFail() {
		return fail;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}

	public void dropDatabase() {
		getMongoConnection().dropDatabase();
	}

	public DataDefinitionReport getDataSetReport(String dataSetDefinition) {
		DB db = getMongoConnection();
		return DataDefinitionService.getDataSetReport(db, dataSetDefinition);
	}

	public List<Transaction> getDoneTransactions(MFDataSetDefinitionMongo ddl) {
		DB db = getMongoConnection();
		return transactionManager.getAllDoneTransactions(db, ddl);
	}

	public DataSetDeleteReport deleteDataSet(String dataSetDefinition) {
		DB db = getMongoConnection();
		return DataDefinitionService.deleteDataSetMetaData(db, dataSetDefinition);
	}

	@Override
	public Transaction startTransaction(MFDataSetDefinitionMongo def, OPERATION t) {
		Transaction tx = transactionManager.startTransaction(getMongoConnection(), def, t);
		return tx;
	}

	public void _insertData(Long seq, DBCollection collection, DB db, String tx, MFDataSetDefinition dataSetDef,
			List<String> binaryFields, List<String> locationFields, Map<String, Object> data, Map<String, Object> meta)
			throws IOException {
		insertData(seq, collection, db, tx, dataSetDef, binaryFields, locationFields, data, meta);
	}
}
