package py.com.sodep.mobileforms.api.services.data;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.DocumentSaveRequest;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IFormDataService {

	MFManagedData getRow(User user, Long formId, Long version, Long rowId);

	PagedData<List<MFManagedData>> getFormData(User user, Long formId, Long version, int pageNumber, int pageSize);

	PagedData<List<MFManagedData>> getFormData(User user, Long formId, Long version, ConditionalCriteria restriction,
			OrderBy orderBy, int pageNumber, int pageSize);

	List<MFManagedData> getFormData(User user, Long formId, Long version, ConditionalCriteria restriction,
			OrderBy orderBy);

	/**
	 * Based on a given blob it will load the binary data completely on memory.
	 * This method shouldn't be used if its known the file might be big (see
	 * {@link #getFileLazy(MFBlob)})
	 * 
	 * @param blob
	 * @return
	 */
	MFBlob getFile(MFBlob blob);

	/**
	 * Instead of loading the file completely on memory, this method will return
	 * a handy object that can be used to write the file directly to an
	 * {@link OutputStream}.
	 * 
	 * @param blob
	 * @return
	 */
	MFFileStream getFileLazy(MFBlob blob);

	MFOperationResult saveData(User user, List<Map<String, Object>> data, Map<String, Object> metaParams, Long formId,
			Long version) throws InterruptedException;

	MFOperationResult saveData(User user, List<Map<String, Object>> data, Map<String, Object> metaParams, Long formId,
			Long version, boolean failFast) throws InterruptedException;

	MFOperationResult saveData(User user, Map<String, Object> data, Map<String, Object> metaParams, Long formId,
			Long version) throws InterruptedException;

	/**
	 * Returns up to {@code maxRows} starting from the document with its "rowId" ("_id" in the document)
	 * equal to the {@code rowId} parameter
	 * 
	 * @param user
	 * @param formId
	 * @param version
	 * @param rowId
	 * @param maxRows
	 * @return
	 */
	List<MFManagedData> getFormDataByRowId(User user, Long formId, Long version, long rowId, int maxRows);

	MFOperationResult copyData(User user, Long formId, Long version,
			Long originRowId, DocumentSaveRequest saveRequest) throws InterruptedException;

	PagedData<List<MFManagedData>> getDataMatchingForms(User user,
			FormMatchQuery matchQuery);

}
