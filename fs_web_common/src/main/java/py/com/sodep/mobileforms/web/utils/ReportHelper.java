package py.com.sodep.mobileforms.web.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLocationData;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR_MODIF;
import py.com.sodep.mf.form.model.MFBaseModel;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.dtos.DocumentWorkflowInfoDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.Option;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.i18n.I18nManager;

public class ReportHelper {

    // FIXME What is the optimal buffer size?
    public static final int BUFFER = 32 * 1024;

	public static class Options {

		private Options() {

		}

		public static Options newInstance() {
			return new Options();
		}

		public Options locationsAsLinks(boolean value) {
			this.locationsAsLinks = value;
			return this;
		}

		public Options dateFormatter(SimpleDateFormat formatter) {
			this.dateFormatter = formatter;
			return this;
		}

		public Options timeFormatter(SimpleDateFormat formatter) {
			this.timeFormatter = formatter;
			return this;
		}

		public Options dateTimeFormatter(SimpleDateFormat formatter) {
			this.dateTimeFormatter = formatter;
			return this;
		}

		public Options numberFormatter(NumberFormat formatter) {
			this.numberFormatter = formatter;
			return this;
		}

		public Options onlyDropdownValue(boolean value) {
			this.onlyDropdownValue = value;
			return this;
		}

		private boolean onlyDropdownValue;

		private boolean locationsAsLinks;

		private SimpleDateFormat dateFormatter;

		private SimpleDateFormat timeFormatter;

		private NumberFormat numberFormatter;

		private SimpleDateFormat dateTimeFormatter;

		boolean customDateFormatter() {
			return dateFormatter != null;
		}

		boolean customTimeFormatter() {
			return timeFormatter != null;
		}

		boolean customNumberFormatter() {
			return numberFormatter != null;
		}

		boolean customDateTimeFormatter() {
			return dateTimeFormatter != null;
		}
	}

	/**
	 * Performs a conversion of the data type stored on the mongo to objects
	 * that are displayable by the custom formatter of the
	 * reports-jqgrid-manager.js or objects that are printable on a CSV file
	 * 
	 * @param data
	 * @param fields
	 * @param i18n
	 * @param elementsMapByInstanceId
	 * @param isTextOnlyReport
	 * @param locationsAsLinks
	 * @return
	 */
	public static Map<String, Object> toMap(MFManagedData data, List<String> fields, I18nManager i18n,
			Map<String, MFElement> elementsMapByInstanceId, IElementPrototypeService prototypeService, Options options) {
		Map<String, Object> formattedData = new HashMap<String, Object>();
		Map<String, Object> userData = data.getUserData();
		Map<String, ?> metadata = data.getMetaData();

		for (String field : fields) {
			if (field.startsWith("meta_")) {
				Object formatted = formatMetadataField(field, metadata, options, i18n, null);
				formattedData.put(field, formatted);
			} else if (field.equals(MFStorable._ID)) { // FIXME CAP-438 temporal changes
				Object formatted = (Long) data.getRowId();
				formattedData.put(field, formatted);
			} else {
				Object formatted = formatDataField(field, userData, options, elementsMapByInstanceId, prototypeService,
						i18n);
				formattedData.put(field, formatted);
			}
		}
		return formattedData;
	}

	private static Object formatDataField(String field, Map<String, Object> userData, Options options,
			Map<String, MFElement> elementsMapByInstanceId, IElementPrototypeService prototypeService, I18nManager i18n) {
		Object formattedObj = null;
		MFElement element = elementsMapByInstanceId.get(field);

		if (!element.getProto().isOutputOnly()) {
			final Object data = userData.get(field);
			switch (element.getProto().getType()) {
			case INPUT:
				MFInput mffinput = (MFInput) element.getProto();
				switch (mffinput.getSubtype()) {
				case DATE:
					if (data != null && options.customDateFormatter()) {
						formattedObj = options.dateFormatter.format(data);
					} else {
						formattedObj = MFDataHelper.serialize((Date) data);
					}
					break;
				case TIME:
					if (data != null && options.customTimeFormatter()) {
						formattedObj = options.timeFormatter.format(data);
					} else {
						formattedObj = MFDataHelper.serialize((Date) data);
					}
					break;
				case DATETIME:
					if (data != null && options.customDateFormatter()) {
						formattedObj = options.dateFormatter.format(data);
					} else {
						formattedObj = MFDataHelper.serialize((Date) data);
					}
					break;
				case DECIMAL:
				case INTEGER:
					if (data != null && options.customNumberFormatter()) {
						formattedObj = options.numberFormatter.format(data);
					} else {
						formattedObj = data; // the number as is
					}
					break;
				case PASSWORD:
					formattedObj = "***"; // is this used at all?
					break;
				default:
					formattedObj = data;
					break;
				}
				break;
			case SELECT:
				if (!options.onlyDropdownValue) {
					// The data send to the web view
					// needs to be translated to a label; otherwise the
					// user might not recognize the selection. (keep in
					// mind that the value is hidden to the user)
					// Since we are not checking if the value of a
					// select has multiple options associated with the
					// same value, then this query might return more then one
					// result.
					// Even-though, this is not desired it is possible
					// (see #1922)
					if (data != null) {
						List<Option> listOptions = prototypeService.listSelectOptions(element.getProto().getId(),
								i18n.getSelectedLanguage(), (String) data);
						if (listOptions.size() < 1) {
							// FIXME what does this mean?
							// It might be the case that the column
							// is of a type that is currently not searchable on
							// a lookuptable (e.g. date)
							// In this case we will just add the value
							// an display an empty label
							listOptions.add(new Option(null, data));
						}
						formattedObj = listOptions;
					}
				} else {
					// Just send the value selected. Don't care about the label
					formattedObj = data;
				}
				break;
			case LOCATION:
				if (options.locationsAsLinks) {
					formattedObj = formatLocation((MFLocationData) data);
				} else {
					formattedObj = data;
				}
				break;
			default:
				formattedObj = data;
			}
		}
		return formattedObj;
	}

	private static Object formatMetadataField(final String field, Map<String, ?> metadata, Options options,
			I18nManager i18n, IStateService stateService) {
		Object value = null;
		final String metaId = field.substring("meta_".length());
		if (metaId.equals(MFIncomingDataI.META_FIELD_RECEIVED_AT) || metaId.equals(MFIncomingDataI.META_FIELD_SAVED_AT)) {
			Object dateObj = metadata.get(metaId);
			if (dateObj != null && dateObj instanceof Date) {
				Date metaDate = (Date) metadata.get(metaId);
				if (options.customDateTimeFormatter()) {
					value = options.dateTimeFormatter.format(metaDate);
				} else {
					value = MFDataHelper.serialize(metaDate);
				}
			} else {
				value = "";
			}
		} else if (metaId.equals(MFIncomingDataI.META_FIELD_MAIL)) {
			Object mailObj = metadata.get(MFIncomingDataI.META_FIELD_MAIL);

			if (mailObj != null) {
				value = (String) mailObj;
			} else {
				// FIXME is this even possible?
				value = i18n.getMessage("web.data.show.not_available");
			}
		} else if (metaId.equals(MFIncomingDataI.META_FIELD_LOCATION)) {
			MFLocationData location = (MFLocationData) metadata.get(MFIncomingDataI.META_FIELD_LOCATION);
			if (location != null) {
				if (options.locationsAsLinks) {
					return formatLocation(location);
				} else {
					value = location;
				}
			} else {
				value = "";
			}
		} else if (metaId.equals(MFIncominDataWorkflow.META_FIELD_STATE_ID)) {
			Long stateId = (Long) metadata.get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
			if (stateId != null) {
				String comment = "";
				if (metadata.containsKey(MFIncominDataWorkflow.META_FIELD_COMMENT)) {
					comment = (String) metadata.get(MFIncominDataWorkflow.META_FIELD_COMMENT);
				}
				State state = stateService.findById(stateId);
				String stateName = state.getName();
				DocumentWorkflowInfoDTO docWFInfoDTO = new DocumentWorkflowInfoDTO(stateId, stateName, comment);
				value = docWFInfoDTO;
			} else {
				value = "";
			}
		}
		return value;
	}

	private static String formatLocation(MFLocationData location) {
		if (location == null) {
			return null;
		}

		StringBuffer url = new StringBuffer(Attributes.ATTRIBUTE_BASE_LOCATION_URL);
		url.append(location.getLatitude()).append(",");
		url.append(location.getLongitude());
		return url.toString();
	}

	public static Map<String, Object> toMap(MFManagedData data, List<String> fields, I18nManager i18n,
			Map<String, MFElement> elementsMapByInstanceId, IElementPrototypeService prototypeService) {
		return toMap(data, fields, i18n, elementsMapByInstanceId, prototypeService, Options.newInstance());
	}

    public static List<Map<String, Object>> toList(List<MFManagedData> data, List<String> fields, I18nManager i18n,
                                            Map<String, MFElement> elementsMapByInstanceId, IElementPrototypeService prototypeService,
                                            Options options) {
        List<Map<String, Object>> entriesList = new ArrayList<Map<String, Object>>();
        for (MFManagedData entry : data) {
            Map<String, Object> entries = new HashMap<String, Object>();
            entries.put("id", entry.getRowId());
            entries.putAll(ReportHelper.toMap(entry, fields, i18n, elementsMapByInstanceId, prototypeService, options));
            entriesList.add(entries);
        }
        return entriesList;
    }


    public static class ZipOptions {

        String tempFileName;

        File tempFile;

        Map<String, String> elementsFileNames;

        public static ZipOptions newInstance() {
            return new ZipOptions();
        }

        private ZipOptions() {
        }

        public ZipOptions reportFile(String tempFileName, File tempFile) {
            this.tempFileName = tempFileName + ".xls";
            this.tempFile = tempFile;
            return this;
        }

        public ZipOptions elementsFileNames(Map<String, String> elementsFileNames) {
            this.elementsFileNames = elementsFileNames;
            return this;
        }

    }

    /**
     *
     * @param outputStream
     * @param entriesList list with document entries, whose values are already formatted
     * @param dataList
     * @param elementsMapByInstanceId
     * @param zipOptions
     * @param formDataService
     * @throws IOException
     */
    public static void generateZip(OutputStream outputStream, List<Map<String, Object>> entriesList,
                            List<MFManagedData> dataList, Map<String, MFElement> elementsMapByInstanceId,
                            ZipOptions zipOptions, IFormDataService formDataService) throws IOException {

        ZipOutputStream zipOs = new ZipOutputStream(new BufferedOutputStream(outputStream, BUFFER));
        List<String> addedNames = new ArrayList<String>();
        // Add the report temp file to the zip
        addZipEntry(zipOs, zipOptions);
        NameParsingOptions options = NameParsingOptions.newInstance();
        options.totalBlobs(countBlobElements(dataList, elementsMapByInstanceId));
        String ext = "jpeg";
        long repeated = 1;
        for (MFManagedData data : dataList) {
            Map<String, Object> userData = data.getUserData();
            Set<String> keySet = elementsMapByInstanceId.keySet();
            for (String fieldName : keySet) {
                Object dataObj = userData.get(fieldName);
                if (dataObj instanceof MFBlob) {
                    MFBlob blob = (MFBlob) dataObj;
                    MFElement element = elementsMapByInstanceId.get(fieldName);
                    MFBaseModel.Type type = element.getProto().getType();
                    if (type == MFBaseModel.Type.PHOTO) {
                        MFFileStream mfFileStream = formDataService.getFileLazy(blob);
                        try {
                            InputStream is = mfFileStream.getInputStream();
                            options.unparsedName(zipOptions.elementsFileNames.get(fieldName));
                            options.incrementBlobCount();
                            options.label(element.getProto().getLabel());
                            String fileName = getParsedFileName(data.getRowId(), entriesList, elementsMapByInstanceId, options);
                            if (addedNames.contains(fileName)) {
                                fileName = fileName + "(" + repeated + ")." + ext;
                                repeated++;
                            } else {
                                addedNames.add(fileName);
                                fileName = fileName + "." + ext;
                            }
                            addZipEntry(zipOs, fileName, is);
                            is.close();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        zipOs.flush();
        zipOs.close();
    }

    static class NameParsingOptions {

        private String unparsedName;
        private long blobCount = 0;
        private long totalBlobs;
        private String label;

        private NameParsingOptions(){

        }
        static NameParsingOptions newInstance() {
            return new NameParsingOptions();
        }

        NameParsingOptions unparsedName(String unparsedName) {
            this.unparsedName = unparsedName;
            return this;
        }

        NameParsingOptions totalBlobs(long totalBlobs) {
            this.totalBlobs = totalBlobs;
            return this;
        }

        NameParsingOptions label(String label) {
            this.label = label;
            return this;
        }

        NameParsingOptions incrementBlobCount() {
            this.blobCount++;
            return this;
        }

    }

    private static String getParsedFileName(Long rowId, List<Map<String, Object>> entriesList, Map<String, MFElement> elementsMapByInstanceId, NameParsingOptions options) {
        String fileName = options.unparsedName;
        if (fileName == null) {
            // the user has not configured any file name
            // but we still want a formatted name
            return formatFileName(options.label, options);
        }

        if (!fileName.contains("[")) {
            // does not have any [elementId] pattern to do a value replacement
            return fileName;
        }

        for (Map<String, Object> entry : entriesList) {
            Long entryId = (Long) entry.get("id");
            if (entryId.longValue() == rowId.longValue()) {
                Set<String> elementsSet = entry.keySet();
                for (String elementId : elementsSet) {
                    if (!elementId.equals("id") && !elementId.startsWith("meta_")) {
                        MFElement element = elementsMapByInstanceId.get(elementId);
                        String label = element.getProto().getLabel();

                        // Elements that have null value at this point
                        // will be displayed with its labels.
                        // If the user has chosen those
                        // fields in the query configuration.
                        Object value = entry.get(elementId);
                        if (value == null) {
                            String validLabel = cleanString(label);
                            fileName = fileName.replaceAll("\\[\\s*" + elementId + "\\s*\\]", validLabel);
                        } else {
                            String valueAsStr = parseValue(value, element);
                            fileName = fileName.replaceAll("\\[\\s*" + elementId + "\\s*\\]", valueAsStr);
                        }

                    }
                }
            }
        }
        return fileName;
    }

    /**
     * Takes the value and cast it to string, if it is not already cast.
     * Also sanitizes the value and converts it to a valid file name string.
     *
     * @param value
     * @param element
     * @return
     */
    private static String parseValue(Object value, MFElement element) {
        String parsedValue = null;
        switch(element.getProto().getType()){
            case INPUT: {
                MFInput mffinput = (MFInput) element.getProto();
                switch(mffinput.getSubtype()) {
                    case DATE: case DATETIME:
                        // This is because the forward-slash
                        // is not a valid file name character
                        String dateStr = (String) value;
                        parsedValue = dateStr.replaceAll("/", "-");
                        break;
                    case TEXT: case TEXTAREA:
                        // replaces potenial invalid characters
                        parsedValue = cleanString((String) value);
                        break;
                    default:
                        parsedValue = (String) value;
                        break;
                }
                break;
            }
            case CHECKBOX:
                if (value instanceof Boolean) {
                    parsedValue = MFDataHelper.serialize((Boolean)value);
                }
                break;
            case SELECT: case BARCODE:
                parsedValue = cleanString((String) value);
                break;
            case PHOTO: case SIGNATURE: case LOCATION:
                // instead of some value we will display
                // the label for this element
                String validLabel = cleanString(element.getProto().getLabel());
                parsedValue = validLabel;
                break;
            default:
                parsedValue = (String) value;
                break;
        }

        // types not checked above are all
        // valid string formatted values
        // that can be included as text
        // inside a file name.
        return parsedValue;
    }

    /**
     * Replaces al illegal file character with an underscore.
     * Illegal character are all but:
     * - a-z and A-Z
     * - 0-9
     * - hyphen '-', backslash '\', dot '.' and underscore
     *
     * @param value
     * @return
     */
    private static String cleanString(String value) {
        //http://stackoverflow.com/questions/5574688/library-to-clean-up-and-simplify-filenames
        return value.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }


    private static String formatFileName(String fileName, NameParsingOptions options) {
        int padding = String.valueOf(options.totalBlobs).length() + 1;
        String formatStr = "%0" + padding + "d";
        String fileNum = String.format(formatStr, options.blobCount);
        return fileNum + "_" + fileName;
    }

    private static long countBlobElements(List<MFManagedData> dataList, Map<String, MFElement> elementMapByInstanceId) {
        long count = 0;
        for (MFManagedData data: dataList) {
            Map<String, Object> userData = data.getUserData();
            Set<String> keySet = elementMapByInstanceId.keySet();
            for (String fieldName : keySet) {
                Object dataObj = userData.get(fieldName);
                if (dataObj instanceof MFBlob) {
                    MFElement element = elementMapByInstanceId.get(fieldName);
                    MFBaseModel.Type type = element.getProto().getType();
                    if (type == MFBaseModel.Type.PHOTO) {
                       count++;
                    }
                }
            }
        }
        return count;
    }

    private static void addZipEntry(ZipOutputStream zipOs, String fileName, InputStream is) throws IOException {
        zipOs.putNextEntry(new ZipEntry(fileName));
        byte[] buffer = new byte[BUFFER];
        int read;
        while ((read = is.read(buffer)) != -1) {
            zipOs.write(buffer, 0, read);
        }
    }

    private static void addZipEntry(ZipOutputStream zipOutputStream, ZipOptions zipOptions) throws IOException {
        InputStream is = new FileInputStream(zipOptions.tempFile);
        addZipEntry(zipOutputStream, zipOptions.tempFileName, is);
        is.close();
    }


	public static List<ReportFilterOptionDTO> parseFilterOptions(String filterOptionsJSON) {
		List<ReportFilterOptionDTO> filterOptions = null;
		if (filterOptionsJSON != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				filterOptions = mapper.readValue(filterOptionsJSON, new TypeReference<List<ReportFilterOptionDTO>>() {
				});
			} catch (JsonParseException e) {
				throw new ApplicationException("Unable to parse report data", e);
			} catch (JsonMappingException e) {
				throw new ApplicationException("Unable to parse report data", e);
			} catch (IOException e) {
				throw new ApplicationException("Unable to parse report data", e);
			}
		}
		return filterOptions;
	}
	
	/**
	 * Returns a list with the names of all the fields of a form. A
	 * form has elements and elements have an "internal name" which is not
	 * visible by the user, which is used to reference it.
	 * 
	 * This method returns a list of those internal names. Not the visible
	 * labels. Do not confuse them.
	 * 
	 * The terminology may not be very clear because of legacy reasons but it's
	 * not rocket science. If you don't like it, don't complain, fix it. Stop
	 * being a whiner.
	 * 
	 * @param formId
	 * @param version
	 * @param language
	 * @param pformModelService 
	 * @return
	 */
	public static List<String> getAllFields(Long formId, Long version, String language, IFormModelService pformModelService) {
		ArrayList<String> fields = new ArrayList<String>();
		MFForm mfform = pformModelService.getMFForm(formId, version, language, false);
		// show all columns of the form
		List<MFElement> allElements = mfform.listAllElements();
		for (MFElement mfElement : allElements) {
			fields.add(mfElement.getInstanceId());
		}
		return fields;
	}
	
	/**
	 * This method translated filter options to a conditional criteria. Every
	 * option is concatenated with an AND operator
	 * 
	 * @param elementsMapByInstanceId
	 * @param filterOptions
	 * @return
	 */
	public static ConditionalCriteria toConditionalCriteria(Map<String, MFElement> elementsMapByInstanceId,
			List<ReportFilterOptionDTO> filterOptions) {
		ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
		for (ReportFilterOptionDTO filterOption : filterOptions) {
			if (filterOption.getValue() != null && !filterOption.getValue().trim().equals("")) {
				Criteria c = new Criteria();
				FIELD_TYPE fieldType = null;
				if (filterOption.getElementId().startsWith("meta_")) {
					// Set the namespace and the field
					String columnName = filterOption.getElementId().substring("meta_".length());
					c.setField(columnName);
					c.setNamespace(MFStorable.FIELD_META);
					// FIXME this helper is in mf_bussiness not in the api
					// project
					fieldType = filterOption.getType();
					boolean isStateIdColumn = columnName.equals(MFIncominDataWorkflow.META_FIELD_STATE_ID);
					if (fieldType == null && !isStateIdColumn) {
						fieldType = MFDataHelper.whatTypeIsThisMetaData(columnName);
					} else if (isStateIdColumn) {
						fieldType = FIELD_TYPE.NUMBER;
					}
					// adequate operator

				} else {
					c.setField(filterOption.getElementId());
					c.setNamespace(MFStorable.FIELD_DATA);

					MFElement mfElement = elementsMapByInstanceId.get(filterOption.getElementId());
					MFField field = mfElementToField(mfElement);
					fieldType = field.getType();

				}

				// set the adequate operator
				applyOperator(c, filterOption.getOperator());
				try {
					Object value = MFDataHelper.unserialize(fieldType, filterOption.getValue());
					c.setValue(value);
					criteria.add(c);
				} catch (IllegalArgumentException e) {
					// if the users has enter some illegal data then it won't be
					// included on the query
					// The JS interface should not let the user enter something
					// illegal. However, there are no controls for the select.
					// For example
				}
			}

		}
		return criteria;
	}
	
	private static void applyOperator(Criteria c, String op) {
		if (op.equals("LIKE")) {
			// The like operators is the only one that can't be translated
			// directly
			c.setOp(OPERATOR.REGEX);
			c.addModificator(OPERATOR_MODIF.ANYWHERE);
			c.addModificator(OPERATOR_MODIF.CASE_INSENSITIVE);
		} else {
			OPERATOR operator = OPERATOR.valueOf(op);
			c.setOp(operator);

		}
	}
	
	/**
	 * MField has information about the data type and the column name, while
	 * an MFElement contains information about the process item (how it will be
	 * rendered on the different devices). This method is very useful to
	 * determine the data type used to stored a given element.
	 * 
	 * @param e
	 * @return
	 */
	public static MFField mfElementToField(MFElement e) {
		if (!e.getProto().isOutputOnly()) {
			FIELD_TYPE type = FIELD_TYPE.STRING;
			switch (e.getProto().getType()) {
			case INPUT:
				MFInput mffinput = (MFInput) e.getProto();
				switch (mffinput.getSubtype()) {
				case TIME:
				case DATE:
				case DATETIME:
					type = FIELD_TYPE.DATE;
					break;
				case DECIMAL:
				case INTEGER:
					type = FIELD_TYPE.NUMBER;
					break;
				}
				break;
			case PHOTO:
				type = FIELD_TYPE.BLOB;
				break;
			case LOCATION:
				type = FIELD_TYPE.LOCATION;
				break;
			case CHECKBOX:
				type = FIELD_TYPE.BOOLEAN;
				break;
			case SIGNATURE:
				type = FIELD_TYPE.BLOB;
				break;
			}

			MFField mfField = new MFField(type, e.getInstanceId());
			return mfField;
		} else {
			throw new ApplicationException("Only input elements can be translated to MFField");
		}
	}




	public static OrderBy getOrderBy(String field, String order) {
		String fieldReadyForQuery = field;
		if (field != null && order != null) {
			String namespace = null;
			if (field.startsWith("meta_")) {
				namespace = OrderBy.META_NAMESPACE;
				fieldReadyForQuery = field.substring("meta_".length());
			} else {
				namespace = OrderBy.DATA_NAMESPACE;
			}
			return new OrderBy(namespace, fieldReadyForQuery, "asc".equalsIgnoreCase(order));
		}
		return null;
	}

	public static ConditionalCriteria getSubRestrictions(
			User user, Map<String, MFElement> elementsMapByInstanceId,
			List<ReportFilterOptionDTO> filterOptions) {
		// Los criterios que hay que armar son
		// 1. meta_mail EQUAL my_user
		ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
		Criteria c = new Criteria();
		c.setField(MFIncomingDataI.META_FIELD_MAIL);
		c.setOp(OPERATOR.EQUALS);
		c.setValue(user.getMail());
		c.setNamespace(MFStorable.FIELD_META);
		criteria.add(c);
		return criteria;
	}

	public static Map<String, Object> toMap(
			MFManagedData data, List<String> fields, I18nManager i18n,
			Map<String, MFElement> elementsMapByInstanceId,
			IElementPrototypeService prototypeService,
			IStateService stateService) {
		return toMap(data, fields, i18n, elementsMapByInstanceId, prototypeService, Options.newInstance(), stateService);
	}

	public static Map<String, Object> toMap(MFManagedData data,
			List<String> fields, I18nManager i18n,
			Map<String, MFElement> elementsMapByInstanceId,
			IElementPrototypeService prototypeService, Options options,
			IStateService stateService) {
		Map<String, Object> formattedData = new HashMap<String, Object>();
		Map<String, Object> userData = data.getUserData();
		Map<String, ?> metadata = data.getMetaData();

		for (String field : fields) {
			if (field.startsWith("meta_")) {
				Object formatted = formatMetadataField(field, metadata, options, i18n, stateService);
				if (!field.equals("meta_stateId")) { 
					formattedData.put(field, formatted);
				} else {
					formattedData.put("meta_state", formatted);
				}	
			} else if (field.equals(MFStorable._ID)) { // FIXME CAP-438 temporal changes
				Object formatted = (Long) data.getRowId();
				formattedData.put(field, formatted);
			} else {
				Object formatted = formatDataField(field, userData, options, elementsMapByInstanceId, prototypeService,
						i18n);
				formattedData.put(field, formatted);
			}
		}
		return formattedData;
	}


}
