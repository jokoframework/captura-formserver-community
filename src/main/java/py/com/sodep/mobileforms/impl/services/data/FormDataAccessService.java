package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLocationData;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.DocumentSaveRequest;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.services.data.FormMatchQuery;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;

@Service("data.FormDataAccessService")
public class FormDataAccessService implements IFormDataService {

	private static Logger logger = LoggerFactory
			.getLogger(FormDataAccessService.class);

	
	@Autowired
	private IFormService formService;

	@Autowired
	private IDataAccessService dataAccessService;

	@Override
	// OK, so it's right that these 2 are the authorizations needed to input
	// data
	// but currently this method is only called from InputDataController.
	// A lot of work has to be done before calling saveData, so it seems right
	// that
	// the authorization control should be done in the controller to avoid
	// unnecesary work
	// @Authorizable(authorizations = { AuthorizationNames.Form.MOBILE,
	// AuthorizationNames.Form.INPUT_DATA_WEB }, formParam = 2)
	public MFOperationResult saveData(User user, List<Map<String, Object>> data, Map<String, Object> metaParams,
			Long formId, Long version) throws InterruptedException {
		return saveData(user, data, metaParams, formId, version, true);
	}

	@Override
	public MFOperationResult saveData(User user, List<Map<String, Object>> data, Map<String, Object> metaParams,
			Long formId, Long version, boolean failFast) throws InterruptedException {
		Form form = formService.getForm(formId, version);
		List<MFIncomingDataI> rows = new ArrayList<MFIncomingDataI>(data.size());
		int i = 0;
		for (Map<String, Object> d : data) {
			// for compatibility with the current mobile app
			// we need to remove any extra field
			Map<String, Object> cleanData = cleanData(d, formId, version);
			Map<String, Object> meta = metaMap(user, formId, metaParams);
			MFIncomingDataBasic r = new MFIncomingDataBasic(i++, cleanData, meta);
			rows.add(r);
		}

		StoreResult mfResult = dataAccessService.storeData(form.getDataSetDefinition(), form.getDatasetVersion(),
				rows, failFast,true);
		return mfResult.getMfOperationResult();
	}

	@Override
	public MFOperationResult saveData(User user, Map<String, Object> data, Map<String, Object> metaParams, Long formId,
			Long version) throws InterruptedException {
		logger.trace("Getting form: id="+ formId + ", version=" + version);
		Form form = formService.getForm(formId, version);
		List<MFIncomingDataI> rows = new ArrayList<MFIncomingDataI>(1);
		// for compatibility with the current mobile app
		// we need to remove any extra field
		Map<String, Object> cleanData = cleanData(data, formId, version);
		Map<String, Object> meta = metaMap(user, formId, metaParams);
		MFIncomingDataBasic r = new MFIncomingDataBasic(0, cleanData, meta);
		rows.add(r);

		StoreResult mfResult = dataAccessService.storeData(form.getDataSetDefinition(), form.getDatasetVersion(),
				rows, true,true);
		return mfResult.getMfOperationResult();
	}

	private Map<String, Object> metaMap(User user, long formId, Map<String, Object> metaParams) {
		Map<String, Object> meta = new HashMap<String, Object>();
		meta.put(MFIncomingDataI.META_FIELD_RECEIVED_AT, new Date());
		meta.put(MFIncomingDataI.META_FIELD_USER_ID, user.getId());
		meta.put(MFIncomingDataI.META_FIELD_MAIL, user.getMail());
		meta.put(MFIncomingDataI.META_FIELD_FORM_ID, formId);
		if (metaParams != null) {
			Object location = metaParams.get(MFIncomingDataI.META_FIELD_LOCATION);
			if (location != null) {
				meta.put(MFIncomingDataI.META_FIELD_LOCATION, location);
			}
			
			Object deviceId = metaParams.get(MFIncomingDataI.META_FIELD_DEVICE_ID);
			if (deviceId != null) {
				meta.put(MFIncomingDataI.META_FIELD_DEVICE_ID, deviceId);
			}
			
			Object documentId = metaParams.get(MFIncomingDataI.META_FIELD_DOCUMENT_ID);
			if(documentId != null){
				meta.put(MFIncomingDataI.META_FIELD_DOCUMENT_ID, documentId);
			}
			
			// CAP-152
			Object savedAt = metaParams.get(MFIncomingDataI.META_FIELD_SAVED_AT);
			if (savedAt != null) {
				meta.put(MFIncomingDataI.META_FIELD_SAVED_AT, savedAt);
			}
			
			
		}
		return meta;
	}

	private MFDataSetDefinition getFormFieldsDefinition(Long formId, Long version) {
		Form form = formService.getForm(formId, version);
		MFDataSetDefinition def = dataAccessService.getDataSetDefinition(form.getDataSetDefinition(),
				form.getDatasetVersion());
		return def;
	}

	/**
	 * This method will clean up the extra fields in the map d, and return a map
	 * that only contains the fields defined in form
	 * 
	 * @param d
	 * @param form
	 * @return
	 */
	private Map<String, Object> cleanData(Map<String, Object> d, Long formId, Long version) {
		Map<String, Object> cleanData = new HashMap<String, Object>();
		MFDataSetDefinition def = getFormFieldsDefinition(formId, version);
		List<MFField> fields = def.getFields();
		for (MFField f : fields) {
			String columnName = f.getColumnName();
			if (d.get(columnName) != null) {
				cleanData.put(columnName, d.get(columnName));
			}
		}
		return cleanData;
	}

	@Override
	public PagedData<List<MFManagedData>> getFormData(User user, Long formId, Long version, int pageNumber, int pageSize) {
		return getFormData(user, formId, version, null, null, pageNumber, pageSize);
	}

	@Override
	public PagedData<List<MFManagedData>> getFormData(User user, Long formId, Long version,
			ConditionalCriteria restriction, OrderBy orderBy, int pageNumber, int pageSize) {
		Form form = formService.getForm(formId, version);
		if (form == null) {
			throw new IllegalArgumentException("The form [id = " + formId + "; version = " + version
					+ "] doesn't exists");
		}

		PagedData<List<MFManagedData>> pagedData = dataAccessService.listData(form.getDataSetDefinition(),
				form.getDatasetVersion(), restriction, orderBy, pageNumber, pageSize);
		return pagedData;

	}
	
	@Override
	public List<MFManagedData> getFormDataByRowId(User user, Long formId, Long version, long rowId, int maxRows) {
		Form form = formService.getForm(formId, version);
		if (form == null) {
			throw new IllegalArgumentException("The form [id = " + formId + "; version = " + version
					+ "] doesn't exists");
		}

		return dataAccessService.listDataByRowId(form.getDataSetDefinition(), form.getDatasetVersion(), rowId, maxRows);
	}

	@Override
	public List<MFManagedData> getFormData(User user, Long formId, Long version, ConditionalCriteria restriction,
			OrderBy orderBy) {
		Form form = formService.getForm(formId, version);
		if (form == null) {
			throw new IllegalArgumentException("The form [id = " + formId + "; version = " + version
					+ "] doesn't exists");
		}
		return dataAccessService.listData(form.getDataSetDefinition(), form.getDatasetVersion(), restriction, orderBy);
	}
	
	

	@Override
	public MFBlob getFile(MFBlob blob) {
		return dataAccessService.getFile(blob);
	}

	@Override
	public MFFileStream getFileLazy(MFBlob blob) {
		return dataAccessService.getFileLazy(blob);
	}

	@Override
	public MFManagedData getRow(User user, Long formId, Long version, Long rowId) {
		Form form = formService.getForm(formId, version);
		if (form == null) {
			throw new IllegalArgumentException("The form [id = " + formId + "; version = " + version
					+ "] doesn't exists");
		}

		return dataAccessService.getRow(form.getDataSetDefinition(), form.getDatasetVersion(), rowId);
	}
	
	@Override
	public MFOperationResult copyData(User user, Long formId,
			Long version, Long originRowId, DocumentSaveRequest newData) throws InterruptedException {
		
		logger.trace("Getting form: id="+ formId + ", version=" + version);
		Form form = formService.getForm(formId, version);
		
		List<MFManagedData> storedList = this.getFormDataByRowId(user, formId, version, originRowId, 1);
		List<MFIncomingDataI> rows = new ArrayList<MFIncomingDataI>(1);
		
		for (MFManagedData stored : storedList) {
			
			MFIncomingDataBasic r = getIncomingUpdated(user, formId, version, newData, stored);
			rows.add(r);
		}
		
		
		StoreResult mfResult = dataAccessService.storeData(form.getDataSetDefinition(), form.getDatasetVersion(),
				rows, true,true);
		return mfResult.getMfOperationResult();
	}

	private MFIncomingDataBasic getIncomingUpdated(User user, Long formId, Long version,
			DocumentSaveRequest newData,
			MFManagedData stored) {
		Map<String, Object> userData = stored.getUserData();
		Map<String, Object> metaData = stored.getMetaData();
		// for compatibility with the current mobile app
		// we need to remove any extra field
		Map<String, Object> cleanData = cleanData(userData, formId, version);
		Map<String, Object> meta = metaMap(user, formId, metaData);
		updateNewData(cleanData, meta, newData);
		MFIncomingDataBasic r = new MFIncomingDataBasic(0, cleanData, meta);
		return r;
	}

	@Override
	public PagedData<List<MFManagedData>> getDataMatchingForms(User user, FormMatchQuery matchQuery) {
		
		Form form = formService.getForm(matchQuery.getFormId(), matchQuery.getVersion());
		if (form == null) {
			throw new IllegalArgumentException("The form [id = " + matchQuery.getFormId() + "; version = " + matchQuery.getVersion()
					+ "] doesn't exists");
		}

		Form matchForm = formService.getForm(matchQuery.getMatchQueryFormId(), matchQuery.getMatchQueryFormVersion());
		
		
		List<MFManagedData> listData = dataAccessService.listData(matchForm.getDataSetDefinition(), matchForm.getDatasetVersion(), matchQuery.getMatchQueryRestrictions(), null);
		List<Object> subFieldList = getSubFieldList(listData, matchQuery.getMatchQueryElementId());
		
		expandRestrictions(matchQuery.getRestrictions(), matchQuery.getElementId(), subFieldList);
		
		return dataAccessService.listData(form.getDataSetDefinition(), form.getDatasetVersion(), matchQuery.getRestrictions(), matchQuery.getOrderBy(), matchQuery.getPage(), matchQuery.getRows());
	}

	private void expandRestrictions(
			ConditionalCriteria restrictions, String elementId, List<Object> subFieldList) {
		if (subFieldList == null || subFieldList.isEmpty()) {
			return;
		}
		Criteria c = new Criteria();
		c.setField(elementId);
		c.setValue(subFieldList);
		c.setOp(OPERATOR.NOT_IN);
		restrictions.add(c);
	}

	private List<Object> getSubFieldList(List<MFManagedData> listData, String elementId) {
		List<Object> fieldValues = new ArrayList<Object>();
		for (MFManagedData data : listData) {
			Map<String, Object> userData = data.getUserData();
			Object nroForm = userData.get(elementId);
			fieldValues.add(nroForm);
		}
		return fieldValues;
	}
	
	private void updateNewData(Map<String, Object> cleanData,
			Map<String, Object> meta, DocumentSaveRequest newData) {
		
		// CAP-152
		String savedAt = newData.getSavedAt();
		if (savedAt != null) {
			Date unserializeDate = MFDataHelper.unserializeDate(savedAt);
			meta.put(MFIncomingDataI.META_FIELD_SAVED_AT, unserializeDate);
		}
		
		if (newData.getLocation() != null) {
			MFLocationData data = MFDataHelper.unserializeLocation(newData.getLocation());
			meta.put(MFIncomingDataI.META_FIELD_LOCATION, data);
		}
		
		
		// Instanciamos los elementos BLOB
		// para que se pueda guardar luego
		Set<String> keySet = cleanData.keySet();
		for (String key: keySet) {
			Object value = cleanData.get(key);
			if (value instanceof MFBlob) {
				MFBlob blob = (MFBlob) value;
				// Si se hace con dataAccessService.getFile
				// la operación tarda demasiado porque hace
				// una copia de los bytes del binario antes
				// de devolver el MFBlob
				MFFileStream fileLazy = dataAccessService.getFileLazy(blob);
				blob.setStream(fileLazy.getInputStream());
			}
		}
		
	}

	
}
