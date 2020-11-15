package py.com.sodep.mobileforms.api.services.data;

import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

import com.mongodb.DB;

/**
 * <p>
 * This class handle the data stored in the mongo, either lookup tables or
 * forms.
 * </p>
 * 
 * 
 * @author danicricco
 * 
 */
public interface IDataAccessService {

	/**
	 * This method will register the definition of a DataSet, this definition
	 * will later be used to validate if the incoming data is valid. If this
	 * method return it is guaranteed that the definition has been written to
	 * disk.
	 * */

	MFDataSetDefinitionMongo define(MFDataSetDefinition ddl);

	/**
	 * Creates a new version of an existing dataSet. If the dataSet doesn't
	 * exists the method will throw an {@link IllegalArgumentException}
	 * 
	 * @param metadataRef
	 * @param ddl
	 * @return
	 */
	MFDataSetDefinitionMongo addDefinition(String metadataRef, MFDataSetDefinition ddl);

	/**
	 * This method return the DDL of a given dataSet,version pair. If the DDL is
	 * not defined, then it will return null
	 * 
	 * @param id
	 * @param version
	 * @return
	 */
	MFDataSetDefinitionMongo getDataSetDefinition(String id, Long version);

	/**
	 * Store data for a given data set. The returning {@link MFOperationResult}
	 * will contain detail of the errors for each row unless the property
	 * fasFail is true.
	 * 
	 * @param id
	 * @param version
	 * @param rows
	 * @param fastFail
	 *            if its true will stop checking errors on the provided data
	 *            after finding the first row with errors
	 * @return A Complete list of errors, a partial list of errors (fastFail)
	 * @throws InterruptedException
	 */
	StoreResult storeData(String metadataRef, Long version, List<? extends MFIncomingDataI> rows,
			boolean fastFail, boolean failOnDuplicate) throws InterruptedException;

	MFOperationResult update(String metadataRef, Long version, MFIncomingDataI incomingData,
			ConditionalCriteria selector);

	/***
	 * 
	 * @param metadataRef
	 * @param version
	 * @param selector
	 * @return
	 */
	MFOperationResult delete(String metadataRef, Long version, ConditionalCriteria selector);

	/**
	 * This is just a wrapper that will call
	 * {@link #listData(String, Long, ConditionalCriteria)}
	 * 
	 * @param dataSetDef
	 * @param version
	 * @return
	 */
	List<MFManagedData> listAllData(String dataSetDef, Long version, OrderBy orderBy);

	/**
	 * List all the data of a given {@link DataSetMetadata}. This method should
	 * only be used with collection that are not very long, where all data can
	 * be loaded into memory
	 * 
	 * @param dataSetDef
	 *            the identifier of the {@link DataSetMetadata}
	 * @param ddlVersion
	 *            the version of the {@link DataSetMetadata}
	 * @param restriction
	 * @param orderBy
	 *            The name of the field that will be used for the sorting
	 * @param ascending
	 * @return
	 */
	List<MFManagedData> listData(String dataSetDef, Long ddlVersion, ConditionalCriteria restriction,
			OrderBy orderBy);

	/**
	 * List the data of {@link DataSetMetadata} in a controlled way (page by
	 * page).
	 * 
	 * @param dataSetDef
	 *            the identifier of the {@link DataSetMetadata}
	 * @param ddlVersion
	 *            the version of the {@link DataSetMetadata}
	 * @param restriction
	 * @param pageNumber
	 * @param pageSize
	 * @param orderBy
	 *            The name of the field that will be used for the sorting
	 * @param ascending
	 * @return
	 */
	PagedData<List<MFManagedData>> listData(String dataSetDef, Long ddlVersion,
			ConditionalCriteria restriction, OrderBy orderBy, int pageNumber, int pageSize);

	/**
	 * This method return an MFBlob loaded with the full content of the binary
	 * data. This method souldn't be used with large files, because the whole
	 * binary data will be loaded in memory. If the file size is unknown then it
	 * is preferred to use {@link #getFileLazy(MFBlob)}
	 * 
	 * @param fileId
	 * @return
	 */
	MFBlob getFile(MFBlob blob);

	/**
	 * Return an inputStream to a file that can be used to load it in lazy mode
	 * 
	 * @param blob
	 * @return
	 */
	MFFileStream getFileLazy(MFBlob blob);

	MFManagedData getRow(String dataSetDefinition, Long datasetVersion, Long rowId);

	// FIXME (danicricco) This method nees to be hidden
	DB getMongoConnection();

	List<MFManagedData> listDataOfTx(Transaction tx, long rowToStart, int maxNumberOfData, boolean asc);

	List<MFManagedData> listDataByRowId(String dataSetDef, Long ddlVersion, long initialRowId, int rows);

	MFOperationResult storeWorkflowHistoryData(
			Map<String, Object> workflowHistoryData);

	List<Map<String, Object>> listDataByStateId(Long stateId);

	List<Map<String, Object>> listDataByDocIdAndFormData(Long docId, Long formId, Long formVersion);

	
	MFOperationResult updateState(String metadataRef, Long version, StateDTO stateDto,
			ConditionalCriteria selector);
	
	MFOperationResult updateStateForMultiple(String metadataRef, Long version, StateDTO stateDto,
			ConditionalCriteria selector);


}