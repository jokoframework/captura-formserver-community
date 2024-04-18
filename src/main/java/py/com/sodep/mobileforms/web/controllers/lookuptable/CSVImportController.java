package py.com.sodep.mobileforms.web.controllers.lookuptable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.dtos.CVSImportOptionsDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.server.ServerProperties;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.utils.NumberUtils;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class CSVImportController extends SodepController {
	// This value is overloaded by the parameter
	// DBParameters.SYS_DATAIMPORT_ROWSTOSHOW. It will only be used if the
	// parameter has not been specified
	private static final int DEFAULT_ROWSTO_SHOW = 10;
	private Logger logger = LoggerFactory.getLogger(CSVImportController.class);

	@Autowired
	private ServerProperties serverProperties;

	@Autowired
	private ILookupTableService lookupService;

	private static final String NUMBER_REGEX = "^[0-9]+$";

	private static final String BOOLEAN_REGEX = "^true|false$";

	@Autowired
	private ISystemParametersBundle systemParams;

	private static final String UTF8 = "UTF-8";

	// private static final String DATE_REGEX =
	// "/^(0?[1-9]|[12][0-9]|3[01])[\\/\\-](0?[1-9]|1[012])[\\/\\-]\\d{4}$/";

	// 13-07-2015 rvillalba. Upgraded this from 1.500 to 10.000.
	// See CAP-174, CAP-172
	private static final int CSV_MAX_LINES = 10000; //FIXME Where to get this
													// parameter from? What
													// should be its value?
													// #2181
	
	private class ImportResult {
		boolean success;
		List<String> errors;
		// DBLookupTable table;
	}

	@RequestMapping("/data/import/file-upload.mob")
	public ModelAndView uploadFile(HttpServletRequest request,
			@RequestParam(value = "csv", required = false) MultipartFile uploadedFile) {

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		ModelAndView mav = new ModelAndView("/home/pages/data/data-import-response.ftl");

		Integer previewLinesCount = systemParams.getIntValue(DBParameters.SYS_DATAIMPORT_ROWSTOSHOW);
		if (previewLinesCount == null) {
			previewLinesCount = DEFAULT_ROWSTO_SHOW;
		}
		JsonResponse<Object> response = new JsonResponse<Object>();
		Map<String, Object> content = new HashMap<String, Object>();

		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			// Output file
			File outputFile = File.createTempFile("mff", ".csv", new File(serverProperties.csvFolder()));
			FileOutputStream fos = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, UTF8);
			writer = new BufferedWriter(osw);
			// Input file
			reader = new BufferedReader(new InputStreamReader(uploadedFile.getInputStream(), UTF8));

			List<String> previewLines = new ArrayList<String>();
			String line = null;
			int lineCount = 1;
			while ((line = reader.readLine()) != null) {
				writer.write(line + "\n");
				// Add lines to preview response
				if (lineCount <= previewLinesCount) {
					previewLines.add(line);
				}
				lineCount++;
			}

			// is lineCount a good measure for #2181 ?
			if (lineCount > CSV_MAX_LINES) {
				response.setSuccess(false);
				response.setTitle(i18n.getMessage("web.data.input.file.csvTooBig.title"));
				response.setMessage(i18n.getMessage("web.data.input.file.csvTooBig.message",
						Integer.toString(CSV_MAX_LINES)));
			} else {
				response.setSuccess(true);
				content.put("filename", outputFile.getName());
				content.put("previewLines", previewLines);
				response.setContent(content);
			}
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.error(msg);
			logger.debug(msg, e);
			response.setSuccess(false);
			response.setMessage(i18n.getMessage("web.data.input.file.upload.exception"));
		} finally {

			if (reader != null) {
				closeReader(reader, "Uploaded file for CSV import");
			}
			if (writer != null) {
				closeWriter(writer, "Saved CSV file to import later");
			}
		}

		// Pack response
		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseStr = mapper.writeValueAsString(response);
			mav.addObject("response", responseStr);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return mav;

	}

	private void closeReader(Reader reader, String name) {
		try {
			reader.close();
		} catch (IOException e) {
			logger.error("Failed to close file: '" + name + "'");
		}
	}

	private void closeWriter(Writer writer, String name) {
		try {
			writer.close();
		} catch (IOException e) {
			logger.error("Failed to close file: '" + name + "'");
		}
	}

	@RequestMapping("/data/import/csv.mob")
	@ResponseBody
	public JsonResponse<String> doImport(HttpServletRequest request, @RequestBody CVSImportOptionsDTO dto) {

		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<String> response = new JsonResponse<String>();

		String lookupTable = dto.getLookupTableName();
		ImportResult result = null;
		if (StringUtils.isEmpty(lookupTable)) {
			response.setTitle(i18n.getMessage("web.generic.error"));
			response.setMessage(i18n.getMessage("web.data.import.error.emptyTableName"));
		} else {
			File file = new File(serverProperties.csvFolder() + File.separator + dto.getFilename());
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				result = doImport(mgr, user, dto, i18n, is);
				response.setSuccess(result.success);
				if (!result.success) {
					response.setTitle(i18n.getMessage("web.generic.error"));
					StringBuilder sb = new StringBuilder();
					for (String msg : result.errors) {
						sb.append(msg);
					}
					response.setMessage(sb.toString());
				} else {
					response.setTitle(i18n.getMessage("web.data.import.success.title"));
					response.setMessage(i18n.getMessage("web.data.import.success.message", lookupTable));
				}

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						logger.error("Failed to close file: '" + dto.getFilename() + "'");
					}
				}
				if (result != null && result.success) {
					// #1238 only delete the temporal file after success
					boolean deleted = file.delete();
					if (!deleted) {
						logger.error("File couldn't be deteled: " + file.getAbsolutePath());
					}
				}
			}
		}

		return response;

	}

	/**
	 * This method checks if the column names have the expected format. It is
	 * not allowed to add columns with "." (this is a mongo restriction)
	 * 
	 * @param columns
	 * @return
	 */
	private List<String> checkColumnNames(I18nManager i18n, String[] columns, boolean fastFail) {
		ArrayList<String> errors = new ArrayList<String>();
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == null || columns[i].trim().isEmpty()) {
				errors.add(i18n.getMessage("web.data.import.error.emptyColumnName", i + ""));
			} else if (columns[i].contains(".")) {
				errors.add(i18n.getMessage("web.data.import.error.columnNameError", i + ""));
			}
			if (errors.size() > 0 && fastFail) {
				return errors;
			}

		}
		return errors;
	}

	private ImportResult doImport(SessionManager mgr, User user, CVSImportOptionsDTO dto, I18nManager i18n,
			InputStream is) throws IOException {
		// CSV Regex
		String regex = generateSeparatorRegexStr(dto);

		// Reader for CSV file
		InputStreamReader isr = new InputStreamReader(is, UTF8);
		BufferedReader buffReader = new BufferedReader(isr);

		// Parse the first line
		String line1 = buffReader.readLine();

		ImportResult result = new ImportResult();
		if (StringUtils.isEmpty(line1)) {
			result.success = false;
			result.errors = new ArrayList<String>();
			// TODO i18n error message
			result.errors.add("Empty CSV file or not provided");
		} else {
			int char0 = (int) line1.charAt(0);
			// http://stackoverflow.com/questions/2223882/whats-different-between-utf-8-and-utf-8-without-bom
			if (char0 == 0xFEFF) { // UTF-8 With BOM
				// REMOVE BOM
				char[] charArray = line1.toCharArray();
				char[] copyOfRange = Arrays.copyOfRange(charArray, 1, charArray.length);
				line1 = new String(copyOfRange);
			}
			// Read all lines
			List<String[]> lines = readLines(buffReader, regex, dto);

			String[] columns = null;
			if (dto.getUseFirstRowAsHeader()) {
				columns = line1.split(regex);
				List<String> errors = checkColumnNames(i18n, columns, true);
				if (errors.size() > 0) {
					result.success = false;
					result.errors = errors;
					return result;
				}
				trimData(columns, dto);
			} else {
				int cols = line1.split(regex).length;
				columns = new String[cols];
				for (int i = 0; i < columns.length; i++) {
					// FIXME. Now the column name is generated but this should
					// be
					// assignable from the Web UI.
					columns[i] = "col" + (i + 1);
				}
				lines.add(0, line1.split(regex));
			}

			FIELD_TYPE[] types = inferTypes(columns, lines);

			// TODO Validate is the number of columns in rows is the same as the
			// amount of columns

			MFLoookupTableDefinition def = new MFLoookupTableDefinition();
			for (int i = 0; i < columns.length; i++) {
				def.addField(new MFField(types[i], columns[i]));
			}
			LookupTableDTO lookupInfo = new LookupTableDTO();
			lookupInfo.setAcceptRESTDMLs(false);
			lookupInfo.setApplicationId(mgr.getApplication().getId());
			lookupInfo.setName(dto.getLookupTableName());
			def.setInfo(lookupInfo);
			MFLoookupTableDefinition defSaved;
			try {
				defSaved = lookupService.createLookupTable(mgr.getApplication(), user, def);
				List<MFIncomingDataI> incomingData = toIncomingDataList(columns, types, lines);

				MFOperationResult insertData = null;
				try {
					insertData = lookupService.insertData(mgr.getApplication(), defSaved.getInfo().getPk(),
							incomingData, true);
				} catch (InterruptedException e) {
					logger.trace("Insert data into table ID: " + defSaved.getInfo().getPk()
							+ " interrupted. Aborting save.");
				}

				List<RowCheckError> insertErrors = insertData.getErrors();

				result.success = insertErrors == null || insertErrors.isEmpty();
				if (!result.success) {
					List<String> errors = listErrorMessages(insertErrors);
					result.errors = errors;
				}
			} catch (LookupTableDefinitionException e1) {
				result.success = false;
				result.errors = new ArrayList<String>();
				String msg = LookupExceptionTranslator.translate(e1, i18n, def);
				result.errors.add(msg);

			}

		}

		return result;
	}

	private String generateSeparatorRegexStr(CVSImportOptionsDTO dto) {
		List<String> delimitersList = new ArrayList<String>();

		if (dto.getUseColon()) {
			delimitersList.add(":");
		}
		if (dto.getUseComma()) {
			delimitersList.add(",");
		}
		if (dto.getUseSemicolon()) {
			delimitersList.add(";");
		}
		if (dto.getUseSpace()) {
			delimitersList.add("\\s");
		}
		if (dto.getUseTab()) {
			delimitersList.add("\t");
		}

		int delimiters = delimitersList.size();
		if (delimiters == 0) {
			return "";
		} else if (delimiters == 1) {
			return delimitersList.get(0);
		} else {
			String result = delimitersList.get(0);
			for (int i = 1; i < delimiters; i++) {
				result += "|" + delimitersList.get(i);
			}
			return result;
		}
	}

	private FIELD_TYPE[] inferTypes(String[] columns, List<String[]> lines) {
		FIELD_TYPE[] types = new FIELD_TYPE[columns.length];
		Arrays.fill(types, FIELD_TYPE.STRING);

		for (int i = 0; i < columns.length; i++) {
			boolean couldBeNumber = true, couldBeBoolean = true;
			for (int j = 0; j < lines.size(); j++) {
				String[] values = lines.get(j);
				if (i < values.length) { // it could be the case that a row
											// doesn't fill all the columns
					String value = values[i];

					if (couldBeBoolean) {
						couldBeBoolean = couldBeBoolean && value.matches(BOOLEAN_REGEX);
					}

					if (couldBeNumber) {
						couldBeNumber = couldBeNumber && value.matches(NUMBER_REGEX);
					}

					if (!couldBeBoolean && !couldBeNumber) {
						break;
					}
				} else {
					break;
				}
			}
			if (couldBeNumber) {
				types[i] = FIELD_TYPE.NUMBER;
			} else if (couldBeBoolean) {
				types[i] = FIELD_TYPE.BOOLEAN;
			}
		}

		return types;
	}

	private List<String> listErrorMessages(List<RowCheckError> insertErrors) {
		List<String> errors = new ArrayList<String>();
		for (RowCheckError rowCheckError : insertErrors) {
			List<ColumnCheckError> columnErrors = rowCheckError.getColumnErrors();
			String rowError = "Error in row: ";
			for (ColumnCheckError columnCheckError : columnErrors) {
				rowError += "Field " + columnCheckError.getOffendingField() + " with error "
						+ columnCheckError.getErrorType() + " ";
			}
			errors.add(rowError);
		}
		return errors;
	}

	private List<MFIncomingDataI> toIncomingDataList(String[] columns, FIELD_TYPE[] types, List<String[]> lines) {
		List<MFIncomingDataI> incomingDataList = new ArrayList<MFIncomingDataI>();
		int n = 0;
		for (String[] values : lines) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < columns.length; i++) {
				if (i < values.length) {
					String strValue = values[i];
					FIELD_TYPE type = types[i];
					Object value = null;
					switch (type) {
					case NUMBER:
						value = NumberUtils.getLong(strValue);
						break;
					case BOOLEAN:
						value = NumberUtils.getBoolean(strValue);
						break;
					default:
						value = strValue;
					}

					map.put(columns[i], value);
				} else {
					break;
				}
			}
			MFIncomingDataBasic data = new MFIncomingDataBasic(n, map);
			incomingDataList.add(data);
			n++;
		}
		return incomingDataList;
	}

	private List<String[]> readLines(BufferedReader buffReader, String regex, CVSImportOptionsDTO dto)
			throws IOException {
		String l;
		List<String[]> lines = new ArrayList<String[]>();
		while ((l = buffReader.readLine()) != null) {
			l = l.trim();
			if (!l.isEmpty()) {
				String[] vaules = l.split(regex);
				trimData(vaules, dto);
				lines.add(vaules);
			}

		}
		return lines;
	}

	private void trimData(String[] data, CVSImportOptionsDTO dto) {
		for (int i = 0; i < data.length; i++) {
			String textQualifier = dto.getTextQualifier();
			data[i] = data[i].trim(); // ref #1856
			if ("all".equals(textQualifier)) {
				data[i] = StringUtils.trimWithCharList(data[i], new char[] { '\"', '\'' });
			} else if ("quote".equals(textQualifier)) {
				data[i] = StringUtils.trimWithCharList(data[i], new char[] { '\'' });
			} else if ("doublequote".equals(textQualifier)) {
				data[i] = StringUtils.trimWithCharList(data[i], new char[] { '\"' });
			}
		}
	}
}
