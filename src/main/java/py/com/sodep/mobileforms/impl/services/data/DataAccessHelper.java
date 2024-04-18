package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLocationData;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;

/**
 * This is a set of useful method for the {@link DataAccessService}. It main
 * purpose is to have this method tested independently by a Junit class
 * 
 * @author danicricco
 * 
 */
public class DataAccessHelper {

	/**
	 * Check that the map contains value that match the expected data type of
	 * the field
	 * 
	 * @param fields
	 *            The declared list of fields
	 * @param row
	 *            the current registry to check. The keys of the map are the
	 *            fields.columnNames
	 * @param errors
	 *            an array that might already contains error reports
	 * @return an array containing errors or an empty array if there wasn't any
	 *         error
	 */
	public static List<ColumnCheckError> checkFieldsDataTypes(List<MFField> fields, Map<String, ?> row,
			List<ColumnCheckError> errors) {
		for (MFField ltField : fields) {
			Object value = row.get(ltField.getColumnName());
			if (value != null) {

				if (ltField.getType().equals(FIELD_TYPE.NUMBER)) {
					if (!(value instanceof Number)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					}
				} else if (ltField.getType().equals(FIELD_TYPE.STRING)) {
					if (!(value instanceof String)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					}
				} else if (ltField.getType().equals(FIELD_TYPE.DATE)) {
					if (!(value instanceof Date)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					}
				} else if (ltField.getType().equals(FIELD_TYPE.BOOLEAN)) {
					if (!(value instanceof Boolean)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					}
				} else if (ltField.getType().equals(FIELD_TYPE.BLOB)) {
					if (!(value instanceof MFBlob)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					} else {
						MFBlob blob = (MFBlob) value;
						if (blob.getData() != null && blob.getStream() != null) {
							// either the data[] or the inputStream must be not
							// null in order to save a blob
							errors.add(new ColumnCheckError(ColumnCheckError.TYPE.WRONG_BLOB, ltField.getColumnName()));
						}
					}
				} else if (ltField.getType().equals(FIELD_TYPE.LOCATION)) {

					if (!(value instanceof MFLocationData)) {
						errors.add(new ColumnCheckError(ColumnCheckError.TYPE.DATA_TYPE, ltField.getColumnName(), value));
					} else {
						MFLocationData location = (MFLocationData) value;
						if (location.getLatitude() == null || location.getLongitude() == null) {
							errors.add(new ColumnCheckError(ColumnCheckError.TYPE.WRONG_LOCATION, ltField
									.getColumnName()));
						}
					}
				}
			}

		}
		return errors;
	}

	/**
	 * Check that the map doesn't contain an undeclared field.
	 * 
	 * @param fields
	 *            The declared list of fields
	 * @param row
	 *            the current registry to check. The keys of the map are the
	 *            fields.columnNames
	 * @param errors
	 *            an array that might already contains error reports
	 * @return an array containing errors or an empty array if there wasn't any
	 *         error
	 */
	public static List<ColumnCheckError> checkFieldDeclaration(List<MFField> fields, Map<String, ?> row,
			List<ColumnCheckError> errors) {
		Set<String> fieldsOnMap = row.keySet();
		BeanToPropertyValueTransformer transformer = new BeanToPropertyValueTransformer("columnName");
		// transform the Collection of fields to a list of String that
		// contains the valid fields
		@SuppressWarnings("unchecked")
		List<String> validFieldsNames = new ArrayList<String>(CollectionUtils.collect(fields, transformer));
		// Note that the order of the validFieldsNames and fields is the same
		Collections.sort(validFieldsNames);
		for (String fieldInMap : fieldsOnMap) {

			if (Collections.binarySearch(validFieldsNames, fieldInMap) < 0) {
				errors.add(new ColumnCheckError(ColumnCheckError.TYPE.MISSING_FIELD, fieldInMap, row.get(fieldInMap)));
			}
		}
		return errors;
	}

	/***
	 * This method will check that the row match the declaration of the fields.
	 * Basically, this is a wrapper method that will call
	 * {@link #checkFieldsDataTypes(List, Map, List)} and
	 * {@link #checkFieldDeclaration(List, Map, List)}
	 * 
	 * @param fields
	 *            The declared list of fields
	 * @param row
	 *            the current registry to check. The keys of the map are the
	 *            fields.columnNames
	 * @return an array containing errors or an empty array if there wasn't any
	 *         error
	 */
	public static List<ColumnCheckError> checkFieldsConsistency(List<MFField> fields, Map<String, ?> row) {
		assert (row != null);
		List<ColumnCheckError> errors = new ArrayList<ColumnCheckError>();
		// 1) CHECK DATA TYPES OF THE FIELDS
		errors = checkFieldsDataTypes(fields, row, errors);

		// 2) CHECK THAT THE MAP DOESN'T CONTAIN AN UNDECLARED FIELD
		errors = checkFieldDeclaration(fields, row, errors);
		// create the transformer that will obtain the columnName of each
		// field

		return errors;
	}
	
	public static List<MFIncomingDataI> newIncoming(User user, DocumentDTO documentDto, List<MFField> fields) {
		List<Map<String, Object>> data = new ArrayList<>();
		if (!documentDto.getDataList().isEmpty()) {
			data.addAll(documentDto.getDataList());
		} else {
			data.add(documentDto.getData());
		}
		Long formId = documentDto.getForm().getId();
		Map<String, Object> metaParams = documentDto.getMeta();
		
		List<MFIncomingDataI> rows = new ArrayList<MFIncomingDataI>(data.size());
		int i = 0;
		for (Map<String, Object> d : data) {
			// for compatibility with the current mobile app
			// we need to remove any extra field
			Map<String, Object> cleanData = cleanData(d, fields);
			Map<String, Object> meta = metaMap(user, formId, metaParams);
			MFIncomingDataBasic r = new MFIncomingDataBasic(i++, cleanData, meta);
			rows.add(r);
		}
		return rows;
	}
	


	/**
	 * This method will clean up the extra fields in the map d, and return a map
	 * that only contains the fields defined in form
	 * @param fields 
	 * 
	 * @param d
	 * @param form
	 * @return
	 */
	private static Map<String, Object> cleanData(Map<String, Object> data, List<MFField> fields) {
		Map<String, Object> cleanData = new HashMap<String, Object>();
		for (MFField f : fields) {
			String columnName = f.getColumnName();
			if (data.get(columnName) != null) {
				cleanData.put(columnName, data.get(columnName));
			}
		}
		return cleanData;
	}
	
	private static Map<String, Object> metaMap(User user, long formId, Map<String, Object> metaParams) {
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
			
			// Workflow
			Object stateId = metaParams.get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
			if (stateId != null) {
				meta.put(MFIncominDataWorkflow.META_FIELD_STATE_ID, stateId);
			}
			
			Object comment = metaParams.get(MFIncominDataWorkflow.META_FIELD_COMMENT);
			if (comment != null) {
				meta.put(MFIncominDataWorkflow.META_FIELD_COMMENT, comment);
			}
		}
		return meta;
	}






}
