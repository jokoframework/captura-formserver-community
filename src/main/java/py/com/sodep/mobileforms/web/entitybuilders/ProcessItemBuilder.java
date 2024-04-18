package py.com.sodep.mobileforms.web.entitybuilders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.javadocmd.simplelatlng.LatLng;

import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.dtos.ProcessItemDTO;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Headline;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Location;
import py.com.sodep.mobileforms.api.entities.forms.elements.Photo;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.services.forms.model.ElementPrototypeUtils;
import py.com.sodep.mobileforms.utils.NumberUtils;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.utils.TemporalUtils;

public class ProcessItemBuilder {

	private static final String VISIBLE_KEY = "admin.form.processitem.common.visible";

	public static final String VALIDATION_NOT_NULL = "admin.processitem.validation.notnull";

	public static final String VALIDATION_IS_REQUIRED = "admin.processitem.validation.required";

	private static final String REQUIRED_KEY = "admin.form.processitem.common.required";

	public static final String LABEL_KEY = "admin.form.processitem.common.label";

	public static final String TYPE_KEY = "admin.form.processitem.common.type";

	public static final String POOL_KEY = "admin.form.processitem.common.poolselect";

	private static final String READONLY_KEY = "admin.form.processitem.input.readonly";

	private static final String KEY_MINIMUM = "admin.form.processitem.input.minimum";

	private static final String KEY_MAXIMUM = "admin.form.processitem.input.maximum";

	private static final String KEY_MULTIPLE = "admin.form.processitem.select.textarea";

	private static final String KEY_VALUE = "admin.form.processitem.select.value";

	private static final String KEY_LABEL = "admin.form.processitem.select.label";

	private static final String KEY_IDENTIFIER = "admin.form.processitem.select.identifier";

	private static final String VALIDATION_NOT_AN_INTEGER = "admin.processitem.validation.notaninteger";

	private static final String VALIDATION_MAXIMUM_LESS_THEN_MINIMUM = "admin.processitem.validation.maxlessthanmin";

	private static final String VALIDATION_IS_NEGATIVE = "admin.processitem.validation.isnegative";

	private static final String KEY_SOURCE = "admin.form.processitem.select.source";

	private static final String ERROR_NOT_NULL = "admin.processitem.error.isnull";

	private static final String KEY_OPTIONS = "admin.form.processitem.select.options";

	private static final String VALIDATION_AT_LEAST_ONE_OPTION = "admin.processitem.validation.atleastoneoption";

	private static final String KEY_OPTION_LABEL = "admin.form.processitem.select.optionlabel";

	private static final String VALIDATION_ALREADY_DEFINED = "admin.processitem.validation.optionalreadydefined";

	private static final String KEY_DEFAULT_VALUE = "admin.form.processitem.input.defaultValue";

	private static final String VALIDATION_INVALID_DATE = "admin.processitem.validation.invalid.date";

	private static final String VALIDATION_INVALID_DATETIME = "admin.processitem.validation.invalid.datetime";

	private static final String VALIDATION_INVALID_TIME = "admin.processitem.validation.invalid.time";

	private static final String VALIDATION_EMPTY_DEFAULT_VALUE = "admin.processitem.validation.invalid.empty";

	private static final String VALIDATION_DEFAULT_VALUE_GREATER_THAN_MAXIMUM = "admin.processitem.validation.invalid.greaterThanMaximum";

	private static final String VALIDATION_DEFAULT_VALUE_LESS_THAN_MINIMUM = "admin.processitem.validation.invalid.lessThanMinimum";

	private static final String VALIDATION_DEFAULT_VALUE_INVALID_INTEGER = "admin.processitem.validation.invalid.integer";

	private static final String VALIDATION_DEFAULT_VALUE_INVALID_DECIMAL = "admin.processitem.validation.invalid.decimal";

	private static final int MAX_DEFAULT_VALUE_LENGTH = 255;

	private static final String VALIDATION_MAX_LENGTH_EXCEED = "admin.processitem.validation.maxExceed";

	private static final String VALIDATION_IS_ZERO = "admin.processitem.validation.isZero";

	private static final String KEY_DFL_LATITUDE = "admin.form.processitem.location.latitude";

	private static final String KEY_DFL_LONGITUDE = "admin.form.processitem.location.longitude";

	private static final String ERROR_NOT_A_REAL_NUMBER = "admin.form.processitem.validation.notRealNumber";

	private static final String ERROR_NOT_A_VALID_COORDINATE = "admin.form.processitem.validation.coordinate.invalid";

	private static final String VALIDATION_COORDINATE_REQUIRED = "admin.form.processitem.validation.coordinate.required";

	private String defaultLanguage;

	public ProcessItemBuilder(String defaultLanguage) {
		Assert.notNull(defaultLanguage,
				"The 'defaultLanguage' property should be set before calling this method. This is a bug.");
		this.defaultLanguage = defaultLanguage;
	}

	public ElementPrototype newElementPrototype(ProcessItemDTO dto) {

		try {
			String type = dto.getType();

			ElementPrototype newProcessItem = null;

			if (MFInput.Type.isValidType(type)) {
				newProcessItem = buildInput(dto, defaultLanguage);
			} else if ("Location".equalsIgnoreCase(type)) {
				newProcessItem = buildLocation(dto, defaultLanguage);
			} else if ("Photo".equalsIgnoreCase(type)) {
				newProcessItem = buildPhoto(dto, defaultLanguage);
			} else if ("Headline".equalsIgnoreCase(type)) {
				newProcessItem = buildHeadline(dto, defaultLanguage);
			} else if ("Select".equalsIgnoreCase(type)) {
				newProcessItem = buildSelect(dto, defaultLanguage);
			} else {
				throw new ApplicationException("Unsupported process item '" + type + "'");
			}

			// When creating the process item it should be active and visible by
			// default
			newProcessItem.setActive(true);
			newProcessItem.setVisible(true);

			return newProcessItem;
		} catch (ClassCastException ex) {
			throw new ApplicationException("Problems while building Process item", ex);
		}
	}

	private ElementPrototype buildHeadline(ProcessItemDTO dto, String defaultLanguage2) {
		Headline headline = new Headline();
		String label = dto.getLabel();
		headline.setLabel(defaultLanguage, label);
		headline.setRequired(false);
		headline.setVisible(true);
		return headline;
	}

	private ElementPrototype buildInput(ProcessItemDTO dto, String defaultLanguage) {
		String type = dto.getType();
		String label = dto.getLabel();
		Boolean required = dto.getRequired();
		Boolean visible = dto.getVisible();

		MFInput.Type inputType = MFInput.Type.valueOf(type.toUpperCase());
		Input input = new Input();

		input.setMinLength(NumberUtils.getInteger(dto.getMin()));
		input.setMaxLength(NumberUtils.getInteger(dto.getMax()));
		Boolean readonly = dto.getReadonly();
		if (readonly == null) {
			input.setReadOnly(false);
		} else {
			input.setReadOnly(readonly);
		}
		input.setLabel(defaultLanguage, label);
		input.setRequired(required);
		input.setVisible(visible);
		input.setDefaultValue(dto.getDefaultValue());
		input.setType(inputType);

		return input;
	}

	private ElementPrototype buildLocation(ProcessItemDTO dto, String defaultLanguage) {
		Location location = new Location();
		String label = dto.getLabel();
		Boolean required = dto.getRequired();
		Boolean visible = dto.getVisible();
		location.setLabel(defaultLanguage, label);
		location.setRequired(required);
		location.setVisible(visible);
		String defaultLatitudeStr = dto.getDefaultLatitude();
		String defaultLongitudeStr = dto.getDefaultLatitude();

		if (defaultLatitudeStr != null) {
			Double defaultLatitude = NumberUtils.getDouble(defaultLatitudeStr);
			location.setDefaultLatitude(defaultLatitude);
		}
		if (defaultLongitudeStr != null) {
			Double defaultLongitude = NumberUtils.getDouble(defaultLongitudeStr);
			location.setDefaultLongitude(defaultLongitude);
		}
		return location;
	}

	private ElementPrototype buildPhoto(ProcessItemDTO dto, String defaultLanguage) {
		Photo photo = new Photo();
		String label = dto.getLabel();
		Boolean required = dto.getRequired();
		Boolean visible = dto.getVisible();
		photo.setLabel(defaultLanguage, label);
		photo.setRequired(required);
		photo.setVisible(visible);
		return photo;
	}

	private ElementPrototype buildSelect(ProcessItemDTO dto, String defaultLanguage) {
		Select select = new Select();
		String label = dto.getLabel();
		Boolean required = dto.getRequired();
		Boolean visible = dto.getVisible();
		select.setLabel(defaultLanguage, label);
		select.setRequired(required);
		select.setVisible(visible);
		select.setMultiple(dto.getMultiple());
		if ("manual".equals(dto.getSource())) {
			select.setSource(OptionSource.LOOKUP_TABLE);
			select.setLookupTableId(dto.getLookupIdentifier());
			select.setLookupLabel(dto.getLookupLabel());
			select.setLookupValue(dto.getLookupValue());
		} else {
			// Embedded
			select.setSource(OptionSource.EMBEDDED);
			List<Map<String, String>> options = dto.getOptions();
			String defaultValue = getDefaultValueForRadio(options);
			select.setDefaultValue(defaultValue);
		}

		return select;
	}

	private static String getDefaultValueForRadio(List<Map<String, String>> options) {
		String defaultValue = null;
		for (Map<String, String> map : options) {
			String radioValueStr = map.get(Select.DROPDOWN_RADIO_FIELD);
			boolean radioValue = Boolean.valueOf(radioValueStr);
			if (radioValue) {
				defaultValue = map.get(Select.DROPDOWN_TEXT_FIELD);
				break;
			}
		}
		return defaultValue;
	}

	public Map<String, String> validate(ProcessItemDTO dto) {
		Map<String, String> errorMsgs = new LinkedHashMap<String, String>();
		// == Common fields
		// Field: Label
		validLabelExistence(dto, errorMsgs);
		// Field: Pool
		validPoolExistence(dto, errorMsgs);
		// Field: Required
		validRequiredExistence(dto, errorMsgs);
		// Field: Visible
		validVisibleExistence(dto, errorMsgs);
		// Field: Type
		if (!validTypeExistence(dto, errorMsgs)) {
			return errorMsgs;
		}

		validateMaxLengthDefaultValue(dto, errorMsgs);

		// Validate specific types
		String type = dto.getType();
		if (MFInput.Type.isTextualType(type)) {
			validateTextualType(dto, errorMsgs);
		} else if (MFInput.Type.isNumericType(type)) {
			validateNumericType(dto, errorMsgs);
		} else if (MFInput.Type.isTemporalType(type)) {
			validateTemporalType(dto, errorMsgs);
		} else if ("Location".equalsIgnoreCase(type)) {
			validateLocationType(dto, errorMsgs);
		} else if ("Photo".equalsIgnoreCase(type)) {
			validatePhotoType(dto, errorMsgs);
		} else if ("Select".equalsIgnoreCase(type)) {
			validateSelectType(dto, errorMsgs);
		} else if ("Headline".equalsIgnoreCase(type)) {
			validateHeadline(dto, errorMsgs);
		} else {
			throw new ApplicationException("Unsupported process item '" + type + "'");
		}

		return errorMsgs;
	}

	private void validateHeadline(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Nothing to do here

	}

	private boolean validateNumericType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String type = dto.getType();
		if ("integer".equals(type)) {
			return validateIntegerType(dto, errorMsgs);
		} else {
			return validateDecimalType(dto, errorMsgs);
		}
	}

	private boolean validateDecimalType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		boolean validMin = validateMinIntegerField(dto, errorMsgs);
		boolean validMax = validateMaxIntegerField(dto, errorMsgs);
		if (validMax && validMin) {
			validateMinMaxIntegerField(dto, errorMsgs);
			// Only check if is in range if specified
			if (!StringUtils.isEmpty(dto.getDefaultValue())) {
				validateDefaultValueWithinFloatRange(dto, errorMsgs);
			}
		}

		return true;
	}

	private boolean validateIntegerType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		boolean validMin = validateMinIntegerField(dto, errorMsgs);
		boolean validMax = validateMaxIntegerField(dto, errorMsgs);
		if (validMax && validMin) {
			validateMinMaxIntegerField(dto, errorMsgs);
			// Only check if is in range if specified
			if (!StringUtils.isEmpty(dto.getDefaultValue())) {
				validateDefaultValueWithinIntegerRange(dto, errorMsgs);
			}
		}

		return true;
	}

	private void validateSelectType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// multiple: not null
		validateMultipleField(dto, errorMsgs);

		boolean existSource = validateExistenceSourceField(dto, errorMsgs);
		if (!existSource) {
			return;
		}

		String source = dto.getSource();
		if ("manual".equalsIgnoreCase(source)) {
			validateManualSelectSource(dto, errorMsgs);
		} else {
			validateEmbbededSelectSource(dto, errorMsgs);
		}

	}

	private boolean validateEmbbededSelectSource(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// DYNAMIC
		// 1. Check at least one option exists
		List<Map<String, String>> options = dto.getOptions();
		if (options.size() == 0) {
			errorMsgs.put(KEY_OPTIONS, VALIDATION_AT_LEAST_ONE_OPTION);
			return false;
		} else {
			// Check that options are not empty or duplicated
			List<String> values = new ArrayList<String>();
			for (Map<String, String> option : options) {
				String currentText = option.get(Select.DROPDOWN_TEXT_FIELD);

				if (StringUtils.isEmpty(currentText)) {
					errorMsgs.put(KEY_OPTION_LABEL, VALIDATION_IS_REQUIRED);
					return false;
				} else {
					currentText = currentText.trim();
					if (values.contains(currentText)) {
						errorMsgs.put(KEY_OPTION_LABEL, VALIDATION_ALREADY_DEFINED);
						return false;
					}
					values.add(currentText);
				}
			}
		}
		return true;
	}

	private boolean validateManualSelectSource(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// MANUAL
		// lookupCollection: not null
		boolean valid = true;
		Long lookupIdentifier = dto.getLookupIdentifier();
		if (lookupIdentifier == null) {
			errorMsgs.put(KEY_IDENTIFIER, VALIDATION_NOT_NULL);
			valid = false;
		}
		// lookupLabel: not null
		String lookupLabel = dto.getLookupLabel();
		if (StringUtils.isEmpty(lookupLabel)) {
			errorMsgs.put(KEY_LABEL, VALIDATION_IS_REQUIRED);
			valid = false;
		}
		// lookupValue: not null
		String lookupValue = dto.getLookupValue();
		if (StringUtils.isEmpty(lookupValue)) {
			errorMsgs.put(KEY_VALUE, VALIDATION_IS_REQUIRED);
			valid = false;
		}
		return valid;

	}

	private boolean validateExistenceSourceField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String source = dto.getSource();
		if (StringUtils.isEmpty(source)) {
			errorMsgs.put(KEY_SOURCE, ERROR_NOT_NULL);
			return false;
		}
		return true;
	}

	private boolean validateMultipleField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Boolean multiple = dto.getMultiple();
		if (multiple == null) {
			errorMsgs.put(KEY_MULTIPLE, VALIDATION_NOT_NULL);
			return false;
		}
		return true;
	}

	private void validatePhotoType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Do nothing for now

	}

	private boolean validateLocationType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String latitudeStr = dto.getDefaultLatitude();
		String longitudeStr = dto.getDefaultLongitude();

		if (StringUtils.isEmpty(latitudeStr) && StringUtils.isEmpty(longitudeStr)) {
			return true;
		}

		boolean valid = true;
		if (longitudeStr == null) {
			errorMsgs.put(KEY_DFL_LONGITUDE, VALIDATION_EMPTY_DEFAULT_VALUE);
			valid = false;
		} else {
			Double longitude = NumberUtils.getDouble(longitudeStr);
			if (longitude == null) {
				errorMsgs.put(KEY_DFL_LONGITUDE, ERROR_NOT_A_REAL_NUMBER);
				valid = false;
			}
		}
		if (latitudeStr == null) {
			errorMsgs.put(KEY_DFL_LATITUDE, VALIDATION_EMPTY_DEFAULT_VALUE);
			valid = false;
		} else {
			Double latitude = NumberUtils.getDouble(latitudeStr);
			if (latitude == null) {
				errorMsgs.put(KEY_DFL_LATITUDE, ERROR_NOT_A_REAL_NUMBER);
				valid = false;
			}
		}
		if (!valid) {
			return false;
		}

		Double latitude = NumberUtils.getDouble(latitudeStr);
		Double longitude = NumberUtils.getDouble(longitudeStr);
		try {
			LatLng point = new LatLng(latitude, longitude);
			return point != null;
		} catch (IllegalArgumentException e) {
			errorMsgs.put(KEY_DFL_LATITUDE, ERROR_NOT_A_VALID_COORDINATE);
			return false;
		}

	}

	private void validateTemporalType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		validateReadonlyField(dto, errorMsgs);
		// Only check if is in value if specified
		if (!StringUtils.isEmpty(dto.getDefaultValue())) {
			validateTemporalDefaultValue(dto, errorMsgs);
		}
	}

	private void validateTemporalDefaultValue(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String defaultValue = dto.getDefaultValue();
		String type = dto.getType();
		if (defaultValue != null) {
			if ("date".equals(type)) {
				if (!validateDate(defaultValue)) {
					errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_INVALID_DATE);
				}
			} else if ("datetime".equals(type)) {
				if (!validateDatetime(defaultValue)) {
					errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_INVALID_DATETIME);
				}
			} else if ("time".equals(type)) {
				if (!validateTime(defaultValue)) {
					errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_INVALID_TIME);
				}
			}
		}
	}

	private void validateTextualType(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		validateReadonlyField(dto, errorMsgs);

		boolean validMin = validateMinTextualField(dto, errorMsgs);
		boolean validMax = validateMaxTextualField(dto, errorMsgs);

		if (validMax && validMin) {
			validateMinMaxIntegerField(dto, errorMsgs);
			if (!StringUtils.isEmpty(dto.getDefaultValue())) {
				// Check that default value length is within range
				validateDefaultValueLengthWithinIntegerRange(dto, errorMsgs);
			}
		}
	}

	private boolean validateMaxLengthDefaultValue(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String defaultValue = dto.getDefaultValue();
		if (!StringUtils.isEmpty(defaultValue)) {
			if (defaultValue.trim().length() > MAX_DEFAULT_VALUE_LENGTH) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_MAX_LENGTH_EXCEED);
				return false;
			}
		}
		return true;
	}

	private boolean validateDefaultValueLengthWithinIntegerRange(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Integer minimum = NumberUtils.getInteger(dto.getMin());
		Integer maximum = NumberUtils.getInteger(dto.getMax());
		String defaultValue = dto.getDefaultValue();
		if (StringUtils.isEmpty(defaultValue)) {
			if (minimum > 0) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_EMPTY_DEFAULT_VALUE);
				return false;
			} else {
				// Minimum is 0 and default value is empty. Is within range
				return true;
			}
		}
		Integer length = defaultValue.length();
		if (length != null) {
			if (maximum != null && length > maximum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_GREATER_THAN_MAXIMUM);
				return false;
			}
			if (minimum != null && length < minimum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_LESS_THAN_MINIMUM);
				return false;
			}
		}
		return true;
	}

	private boolean validateDefaultValueWithinIntegerRange(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Integer minimum = NumberUtils.getInteger(dto.getMin());
		Integer maximum = NumberUtils.getInteger(dto.getMax());
		String defaultValue = dto.getDefaultValue();
		if (StringUtils.isEmpty(defaultValue)) {
			if (minimum > 0) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_EMPTY_DEFAULT_VALUE);
				return false;
			} else {
				return true;
			}
		}
		Integer defaultValueInt = NumberUtils.getInteger(defaultValue);
		if (defaultValueInt != null) {
			if (defaultValueInt > maximum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_GREATER_THAN_MAXIMUM);
				return false;
			}
			if (defaultValueInt < minimum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_LESS_THAN_MINIMUM);
				return false;
			}
		} else {
			// Is invalid
			errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_INVALID_INTEGER);
			return false;
		}
		return true;
	}

	private boolean validateDefaultValueWithinFloatRange(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Integer minimum = NumberUtils.getInteger(dto.getMin());
		Integer maximum = NumberUtils.getInteger(dto.getMax());
		String defaultValue = dto.getDefaultValue();

		if (StringUtils.isEmpty(defaultValue)) {
			if (minimum > 0) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_EMPTY_DEFAULT_VALUE);
				return false;
			} else {
				return true;
			}
		}
		// Here default value is not empty but we don't know if is valid yet
		Float defaultValueFloat = NumberUtils.getFloat(defaultValue);
		if (defaultValueFloat != null) {
			if (defaultValueFloat > maximum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_GREATER_THAN_MAXIMUM);
				return false;
			}
			if (defaultValueFloat < minimum) {
				errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_LESS_THAN_MINIMUM);
				return false;
			}
		} else {
			// Is invalid!
			errorMsgs.put(KEY_DEFAULT_VALUE, VALIDATION_DEFAULT_VALUE_INVALID_DECIMAL);
			return false;
		}
		return true;
	}

	private boolean validateMinMaxIntegerField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Integer minimum = NumberUtils.getInteger(dto.getMin());
		Integer maximum = NumberUtils.getInteger(dto.getMax());
		if (minimum != null && maximum != null) {
			// Minimum and maximum are defined
			if (minimum > maximum) {
				errorMsgs.put(KEY_MAXIMUM, VALIDATION_MAXIMUM_LESS_THEN_MINIMUM);
				return false;
			}
		}
		return true;
	}

	private boolean validateMaxIntegerField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Maximum: number
		String maxStr = dto.getMax();
		Integer maximum = null;
		if (maxStr != null) {
			maximum = NumberUtils.getInteger(maxStr);
			if (maximum == null) {
				errorMsgs.put(KEY_MAXIMUM, VALIDATION_NOT_AN_INTEGER);
				return false;
			}
		}
		return true;
	}

	private boolean validateMinIntegerField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Minimum: number
		String minStr = dto.getMin();
		Integer minimum = null;
		if (minStr != null) {
			minimum = NumberUtils.getInteger(minStr);
			if (minimum == null) {
				errorMsgs.put(KEY_MINIMUM, VALIDATION_NOT_AN_INTEGER);
				return false;
			}
		}
		return true;
	}

	private boolean validateMinTextualField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Minimum: number
		String minStr = dto.getMin();
		Integer minimum = null;
		if (minStr != null) {
			minimum = NumberUtils.getInteger(minStr);
			if (minimum == null) {
				errorMsgs.put(KEY_MINIMUM, VALIDATION_NOT_AN_INTEGER);
				return false;
			} else {
				if (minimum < 0) {
					errorMsgs.put(KEY_MINIMUM, VALIDATION_IS_NEGATIVE);
					return false;
				}
			}
		}
		return true;
	}

	private boolean validateMaxTextualField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		// Maximum: number
		String maxStr = dto.getMax();
		Integer maximum = null;
		if (maxStr != null) {
			maximum = NumberUtils.getInteger(maxStr);
			if (maximum == null) {
				errorMsgs.put(KEY_MAXIMUM, VALIDATION_NOT_AN_INTEGER);
				return false;
			} else {
				if (maximum < 0) {
					errorMsgs.put(KEY_MAXIMUM, VALIDATION_IS_NEGATIVE);
					return false;
				} else if (maximum == 0) {
					// Makes no sense
					errorMsgs.put(KEY_MAXIMUM, VALIDATION_IS_ZERO);
					return false;
				} else {
					if (maximum > MAX_DEFAULT_VALUE_LENGTH) {
						errorMsgs.put(KEY_MAXIMUM, VALIDATION_MAX_LENGTH_EXCEED);
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean validateReadonlyField(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Boolean readonly = dto.getRequired();
		if (readonly == null) {
			errorMsgs.put(READONLY_KEY, VALIDATION_NOT_NULL);
			return false;
		}
		return true;
	}

	private boolean validTypeExistence(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String type = dto.getType();
		if (StringUtils.isEmpty(type)) {
			errorMsgs.put(TYPE_KEY, VALIDATION_IS_REQUIRED);
			// If there's no type then stop doing validations
			return false;
		}
		return true;
	}

	private boolean validVisibleExistence(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Boolean visible = dto.getRequired();
		if (visible == null) {
			errorMsgs.put(VISIBLE_KEY, VALIDATION_NOT_NULL);
			return false;
		}
		return true;
	}

	private boolean validRequiredExistence(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Boolean required = dto.getRequired();
		if (required == null) {
			errorMsgs.put(REQUIRED_KEY, VALIDATION_NOT_NULL);
			return false;
		}
		return true;
	}

	private void validLabelExistence(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		String label = dto.getLabel();
		if (StringUtils.isEmpty(label)) {
			errorMsgs.put(LABEL_KEY, VALIDATION_IS_REQUIRED);
		}
	}

	private void validPoolExistence(ProcessItemDTO dto, Map<String, String> errorMsgs) {
		Long poolId = dto.getPool();
		if (poolId == null) {
			errorMsgs.put(POOL_KEY, VALIDATION_IS_REQUIRED);
		}
	}

	private boolean validateTime(String defaultValue) {
		String format = "h:mm aa";
		return TemporalUtils.isLegalDateStringWithFormat(defaultValue, format);
	}

	private boolean validateDate(String defaultValue) {
		String format = "MM/dd/yyyy";
		return TemporalUtils.isLegalDateStringWithFormat(defaultValue, format);
	}

	private boolean validateDatetime(String defaultValue) {
		String format = "MM/dd/yyyy hh:mm aa";
		return TemporalUtils.isLegalDateStringWithFormat(defaultValue, format);
	}

	public ProcessItemDTO buildDTO(ElementPrototype ep, String label) {
		if (ep == null) {
			return null;
		}
		ProcessItemDTO dto = new ProcessItemDTO();
		dto.setId(ep.getId());
		dto.setActive(ep.getActive());
		dto.setRequired(ep.getRequired());
		dto.setLabel(label);
		dto.setVersion(ep.getVersion());
		dto.setRootId(ep.getRoot().getId());

		Pool pool = ep.getPool();
		if (pool != null) {
			dto.setPool(pool.getId());
		}
		if (ep instanceof Input) {
			Input input = (Input) ep;
			if (MFInput.Type.TEXT.equals(input.getType())) {
				if (input.getMinLength() != null) {
					dto.setMin(Integer.toString(input.getMinLength()));
				}
				if (input.getMaxLength() != null) {
					dto.setMax(Integer.toString(input.getMaxLength()));
				}
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.DATE.equals(input.getType())) {
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.DECIMAL.equals(input.getType())) {
				if (input.getMinLength() != null) {
					dto.setMin(Integer.toString(input.getMinLength()));
				}
				if (input.getMaxLength() != null) {
					dto.setMax(Integer.toString(input.getMaxLength()));
				}
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.PASSWORD.equals(input.getType())) {
				if (input.getMinLength() != null) {
					dto.setMin(Integer.toString(input.getMinLength()));
				}
				if (input.getMaxLength() != null) {
					dto.setMax(Integer.toString(input.getMaxLength()));
				}
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.TEXTAREA.equals(input.getType())) {
				if (input.getMinLength() != null) {
					dto.setMin(Integer.toString(input.getMinLength()));
				}
				if (input.getMaxLength() != null) {
					dto.setMax(Integer.toString(input.getMaxLength()));
				}
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.TIME.equals(input.getType())) {
				dto.setReadonly(input.getReadOnly());
			} else if (MFInput.Type.INTEGER.equals(input.getType())) {
				if (input.getMinLength() != null) {
					dto.setMin(Integer.toString(input.getMinLength()));
				}
				if (input.getMaxLength() != null) {
					dto.setMax(Integer.toString(input.getMaxLength()));
				}
				dto.setReadonly(input.getReadOnly());
			}
			dto.setDefaultValue(input.getDefaultValue());
		} else if (ep instanceof Location) {
			Location location = (Location) ep;

			Double defaultLatitudeStr = location.getDefaultLatitude();
			if (defaultLatitudeStr != null) {
				dto.setDefaultLatitude(Double.toString(defaultLatitudeStr));
			}
			Double defaultLongitudeStr = location.getDefaultLongitude();
			if (defaultLongitudeStr != null) {
				dto.setDefaultLongitude(Double.toString(defaultLongitudeStr));
			}
		} else if (ep instanceof Photo) {
			// Photo photo = (Photo) ep;
		} else if (ep instanceof Select) {
			Select select = (Select) ep;
			dto.setMultiple(select.getMultiple());
			OptionSource selectSource = select.getSource();
			if (selectSource != null && selectSource == OptionSource.EMBEDDED) {
				dto.setSource("dynamic");
				dto.setDefaultValue(select.getDefaultValue());
			} else {
				// Lookup table
				dto.setLookupIdentifier(select.getLookupTableId());
				dto.setLookupLabel(select.getLookupLabel());
				dto.setLookupValue(select.getLookupValue());
				dto.setSource("manual");
			}
		} else if (ep instanceof Headline) {
			// throw new RuntimeException("Not yet Implemented");
		} else {
			throw new ApplicationException("Process item '" + ep.getClass().getName() + "' not supported");
		}
		// FIXME I don't like this... jmpr 02/01/13
		dto.setType(ElementPrototypeUtils.getName(ep));
		return dto;
	}

	public static String[] getTypeList() {
		return ElementPrototype.allTypeList();
	}

	public void changeProcessItemBasicProperties(ElementPrototype ep, ProcessItemDTO dto, Pool pool) {
		ep.setRequired(dto.getRequired());
		ep.setPool(pool);

		if (ep instanceof Input) {
			Input input = (Input) ep;
			String type = dto.getType();
			MFInput.Type inputType = MFInput.Type.safeValueOf(type);

			// Setting all values according to type
			input.setType(inputType);
			if (MFInput.Type.TEXT.equals(input.getType())) {
				input.setMinLength(Integer.parseInt(dto.getMin()));
				input.setMaxLength(Integer.parseInt(dto.getMax()));
			} else if (MFInput.Type.DATE.equals(input.getType())) {
				// Nothing to do yet
			} else if (MFInput.Type.DECIMAL.equals(input.getType())) {
				input.setMinLength(Integer.parseInt(dto.getMin()));
				input.setMaxLength(Integer.parseInt(dto.getMax()));
			} else if (MFInput.Type.PASSWORD.equals(input.getType())) {
				input.setMinLength(Integer.parseInt(dto.getMin()));
				input.setMaxLength(Integer.parseInt(dto.getMax()));
			} else if (MFInput.Type.TEXTAREA.equals(input.getType())) {
				input.setMinLength(Integer.parseInt(dto.getMin()));
				input.setMaxLength(Integer.parseInt(dto.getMax()));
			} else if (MFInput.Type.TIME.equals(input.getType())) {
				// Nothing to do yet
			} else if (MFInput.Type.INTEGER.equals(input.getType())) {
				input.setMinLength(Integer.parseInt(dto.getMin()));
				input.setMaxLength(Integer.parseInt(dto.getMax()));
			}
			input.setDefaultValue(dto.getDefaultValue());
			input.setReadOnly(dto.getReadonly());

		} else if (ep instanceof Location) {
			Location location = (Location) ep;
			String defaultLatitudeStr = dto.getDefaultLatitude();
			String defaultLongitudeStr = dto.getDefaultLongitude();
			if (defaultLatitudeStr != null) {
				Double defaultLatitude = NumberUtils.getDouble(defaultLatitudeStr);
				location.setDefaultLatitude(defaultLatitude);
			}
			if (defaultLongitudeStr != null) {
				Double defaultLongitude = NumberUtils.getDouble(defaultLongitudeStr);
				location.setDefaultLongitude(defaultLongitude);
			}
			// Nothing to do yet
		} else if (ep instanceof Photo) {
			// Photo photo = (Photo) ep;
			// Nothing to do yet
		} else if (ep instanceof Select) {
			Select select = (Select) ep;
			select.setMultiple(dto.getMultiple());
			String source = dto.getSource();
			if ("manual".equals(source)) {
				// LUT
				select.setLookupTableId(dto.getLookupIdentifier());
				select.setLookupLabel(dto.getLookupLabel());
				select.setLookupValue(dto.getLookupValue());
				select.setSource(OptionSource.LOOKUP_TABLE);
			} else {
				// embbeded
				select.setSource(OptionSource.EMBEDDED);
				String defaultValueForRadio = getDefaultValueForRadio(dto.getOptions());
				select.setDefaultValue(defaultValueForRadio);
			}
		} else if (ep instanceof Headline) {
			throw new RuntimeException("Not yet implemented");
		} else {
			throw new ApplicationException("Process item '" + ep.getClass().getName() + "' not supported.");
		}

	}
}
