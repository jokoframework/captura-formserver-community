package py.com.sodep.mobileforms.test.data.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mobileforms.impl.services.data.DataAccessHelper;

/**
 * This class test the public methods on {@link DataAccessHelper}
 * 
 * @author danicricco
 * 
 */
public class DataAccessHelperTest {

	/**
	 * A row that match the field declaration should produce an empty list of
	 * errors
	 */
	@Test
	public void checkWorkingRow() {
		// check that a valid object pass the check
		ArrayList<MFField> fields = getFieldsDeclaration();

		@SuppressWarnings("rawtypes")
		HashMap<String, Comparable> row = getInstanceWithAllFields();

		List<ColumnCheckError> errors = DataAccessHelper.checkFieldsConsistency(fields, row);
		Assert.assertNotNull(errors);
		Assert.assertEquals(new Integer(0), new Integer(errors.size()));

	}

	/**
	 * A missing field shouldn't be a problem, the system should consider it as
	 * null.
	 */
	@Test
	public void checkMissingField() {
		// check that a valid object pass the check
		ArrayList<MFField> fields = getFieldsDeclaration();

		@SuppressWarnings("rawtypes")
		HashMap<String, Comparable> row = getInstanceWithAllFields();
		// Remove one of the fields
		row.remove("name");

		List<ColumnCheckError> errors = DataAccessHelper.checkFieldsConsistency(fields, row);
		Assert.assertNotNull(errors);
		Assert.assertEquals(new Integer(0), new Integer(errors.size()));
	}

	
	/**
	 * An Integer value is assigned to field declared as String to check if the system report the Data Type error
	 */
	@Test
	public void checkWrongDataType() {
		// check that a valid object pass the check
		ArrayList<MFField> fields = getFieldsDeclaration();

		@SuppressWarnings("rawtypes")
		HashMap<String, Comparable> row = getInstanceWithAllFields();
		Integer wrongValue = 4;
		// replace the name with an integer
		row.put("name", wrongValue);

		List<ColumnCheckError> errors = DataAccessHelper.checkFieldsConsistency(fields, row);
		Assert.assertNotNull(errors);
		Assert.assertEquals(new Integer(1), new Integer(errors.size()));
		ColumnCheckError dataTypeError = errors.get(0);
		Assert.assertEquals("name", dataTypeError.getOffendingField());
		Assert.assertEquals(wrongValue, dataTypeError.getValue());
		Assert.assertEquals(ColumnCheckError.TYPE.DATA_TYPE, dataTypeError.getErrorType());
	}

	/**
	 * A wrong fieldName is used to check if the system report that the field
	 * was no declared
	 */
	@Test
	public void checkWrongFieldName() {
		// check that a valid object pass the check
		ArrayList<MFField> fields = getFieldsDeclaration();

		@SuppressWarnings("rawtypes")
		HashMap<String, Comparable> row = getInstanceWithAllFields();
		// add a non declared field
		String offfendingFieldName = "fieldXXX";
		String offendingFieldValue = "ddd";
		row.put(offfendingFieldName, offendingFieldValue);

		List<ColumnCheckError> errors = DataAccessHelper.checkFieldsConsistency(fields, row);
		Assert.assertNotNull(errors);
		Assert.assertEquals(new Integer(1), new Integer(errors.size()));
		ColumnCheckError worngFieldError = errors.get(0);
		Assert.assertEquals(offfendingFieldName, worngFieldError.getOffendingField());
		Assert.assertEquals(offendingFieldValue, worngFieldError.getValue());
		Assert.assertEquals(ColumnCheckError.TYPE.MISSING_FIELD, worngFieldError.getErrorType());
	}

	/**
	 * 
	 * @return an array of {@link MFField} with 3 items
	 */
	private ArrayList<MFField> getFieldsDeclaration() {
		ArrayList<MFField> fields = new ArrayList<MFField>();
		fields.add(new MFField(FIELD_TYPE.NUMBER, "id"));
		fields.add(new MFField(FIELD_TYPE.STRING, "name"));
		fields.add(new MFField(FIELD_TYPE.BOOLEAN, "active"));
		return fields;
	}

	@SuppressWarnings({ "rawtypes" })
	/**
	 * @return a document that has all fields established (3 fields)
	 * 
	 */
	private HashMap<String, Comparable> getInstanceWithAllFields() {
		HashMap<String, Comparable> map = new HashMap<String, Comparable>();
		map.put("id", new Integer(3));
		map.put("name", "Paraguay");
		map.put("active", true);
		return map;
	}
}
