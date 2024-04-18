package py.com.sodep.mobileforms.api.services.data;

import java.io.OutputStream;
import java.util.List;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.data.DBLookupTable;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface ILookupTableService {

	public List<LookupTableDTO> listAvailableLookupTables(Application app);

	public List<LookupTableDTO> listAvailableLookupTables(Application app, String identifier);

	public PagedData<List<LookupTableDTO>> findAvailableLookupTables(Application app, String identifier,
			String orderBy, boolean ascending, Integer page, Integer pageSize);

	/**
	 * Creates a new lookup table, stores it definition in mongo and associate
	 * the mongo objects with the DBLookupTable (dataSetDefinition,version)
	 * 
	 * @param u
	 * @param table
	 * @param definition
	 * @return
	 */
	public MFLoookupTableDefinition createLookupTable(Application app, User u, MFLoookupTableDefinition definition)
			throws LookupTableDefinitionException;

	/**
	 * Embeded lookuptables are lookuptables that are only meaninful within the
	 * Form editor
	 * 
	 * @param app
	 * @param u
	 * @param definition
	 * @return
	 */
	public MFLoookupTableDefinition createEmbededLookup(Application app, User u, MFLoookupTableDefinition definition);

	public MFOperationResult deleteData(Application app, Long lookupTableId, ConditionalCriteria selector);

	/**
	 * This method will insert a number of registry in a lookup Table. The
	 * returned {@link MFOperationResult} will contain the summary of the
	 * operation. everything went fine then the
	 * {@link MFOperationResult#hasSucceeded()} should return true and the
	 * method {@link MFOperationResult#getNumberOfAffectedRows()} will return
	 * the number of inserted rows
	 * 
	 * @param lookupTableId
	 * @param rows
	 * @param fastFail
	 * @return
	 * @throws InterruptedException
	 */
	public MFOperationResult insertData(Application app, Long lookupTableId, List<? extends MFIncomingDataI> rows,
			boolean fastFail, boolean failOnDuplicates) throws InterruptedException;

	/**
	 * this is a wraper over
	 * {@link #insertData(Application, Long, List, boolean,boolean)} with the
	 * parameter failOnDuplicates set to true
	 * 
	 * @param app
	 * @param lookupTableId
	 * @param rows
	 * @param fastFail
	 * @return
	 * @throws InterruptedException
	 */
	public MFOperationResult insertData(Application app, Long lookupTableId, List<? extends MFIncomingDataI> rows,
			boolean fastFail) throws InterruptedException;

	public MFOperationResult updateData(Application app, Long lookupTableId, MFIncomingDataI row,
			ConditionalCriteria selector);

	public MFOperationResult updateOrInsertData(Application app, Long lookupTableId, MFIncomingDataI row,
			boolean fastFail) throws InterruptedException;

	/**
	 * This method will return the lookup definition (DDL) for a given
	 * lookupTableId. If the lookupTableId doesn't exists the method will thrown
	 * an {@link IllegalArgumentException}. If the lookupTable exists in the
	 * postgres but the corespnding data is not on the mongo DB, then it will
	 * thrown an {@link InconsistentLookupDeclaration}
	 * 
	 * @param lookupTableId
	 * @return
	 */
	public MFLoookupTableDefinition getLookupTableDefinition(Long lookupTableId);

	/**
	 * This method will create a new lookup table that will share the dataSet of
	 * its predecessor but might have a different way to see them. Currently
	 * there are no restrictions on the new MFDataSetDefinition, but this might
	 * change in the future (For example, It doesn't make sense to change the
	 * data type of a column from "date" to "integer")
	 * 
	 * @param u
	 * @param lookupTableId
	 * @param definition
	 * @return
	 */
	public DBLookupTable createNewVersion(Application app, User u, Long lookupTableId, MFDataSetDefinition definition);

	/**
	 * Return the data loaded to this current lookup table in the given version
	 * 
	 * @param lookupTableId
	 * @return
	 */
	public List<MFManagedData> listAllData(Application app, Long lookupTableId);

	public List<MFManagedData> listData(Application app, Long lookupTableId, ConditionalCriteria criteria);

	/**
	 * This is a wrapper over the method
	 * {@link #listData(Application, Long, ConditionalCriteria)} that returns
	 * all entries where the column matched the value (using string equals)
	 * 
	 * @param app
	 * @param lookupTableId
	 * @param column
	 * @param value
	 * @return
	 */
	public List<MFManagedData> listData(Application app, Long lookupTableId, String column, String value);

	/**
	 * List data of a lookup table using pagination.
	 * 
	 * @param lookupTableId
	 * @param restriction
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public PagedData<List<MFManagedData>> listData(Application app, Long lookupTableId,
			ConditionalCriteria restriction, int pageNumber, int pageSize, String orderBy, boolean ascending);

	public List<MFField> listFields(Long lutId, String language);

	public DBLookupTable deleteLookupTable(Long lutId) throws LookuptableOperationException;

	/**
	 * Based on a given blob it will load the binary data completely on memory.
	 * This method shouldn't be used if its known the file might be big (see
	 * {@link #getFileLazy(MFBlob)})
	 * 
	 * @param blob
	 * @return
	 */
	public MFBlob getFile(MFBlob blob);

	/**
	 * Instead of loading the file completely on memory, this method will return
	 * a handy object that can be used to write the file directly to an
	 * {@link OutputStream}.
	 * 
	 * @param blob
	 * @return
	 */
	public MFFileStream getFileLazy(MFBlob blob);

}
