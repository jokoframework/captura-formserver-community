package py.com.sodep.mobileforms.test.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.TXInfo.OPERATION;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.Transaction;
import py.com.sodep.mobileforms.impl.services.data.DataDefinitionReport;
import py.com.sodep.mobileforms.impl.services.data.DataSetDeleteReport;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public interface IDataAccessServiceMock extends IDataAccessService {

	public int getRowToFail();

	public void setRowToFail(int rowToFail);

	public boolean isFail();

	public void setFail(boolean fail);

	public void dropDatabase();

	public DataDefinitionReport getDataSetReport(String dataSetDefinition);

	public List<Transaction> getDoneTransactions(MFDataSetDefinitionMongo ddl);

	public DataSetDeleteReport deleteDataSet(String dataSetDefinition);

	public Transaction startTransaction(MFDataSetDefinitionMongo def, OPERATION t);

	public void _insertData(Long seq, DBCollection collection, DB db, String tx, MFDataSetDefinition dataSetDef,
			List<String> binaryFields, List<String> locationFields, Map<String, Object> data, Map<String, Object> meta)
			throws IOException;

}
