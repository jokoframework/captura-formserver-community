package py.com.sodep.mobileforms.web.controllers.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.export.excel.ExcelGenerator;
import py.com.sodep.export.excel.PdfGenerator;
import py.com.sodep.export.exception.ConvertException;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFPhoto;
import py.com.sodep.mf.form.model.prototype.MFPrototype;
import py.com.sodep.mf.form.model.prototype.MFSignature;
import py.com.sodep.mobileforms.api.dtos.DocumentWorkflowInfoDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.workflow.State;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.reports.IReportQueryService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;
import py.com.sodep.mobileforms.web.utils.ReportHelper;
import py.com.sodep.mobileforms.web.utils.ReportHelper.Options;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author vrumich
 *
 */
@Controller
public class ReportController extends SodepController {
	
	private static final Logger logger = Logger.getLogger(ReportController.class);

	@Autowired
	private IFormDataService formDataService;

	@Autowired
	private IElementPrototypeService prototypeService;

	@Autowired
	private IReportQueryService queryService;

	@Autowired 
	IFormModelService formModelService;

	@Autowired
	private ILookupTableService lookupTableService;
	
	@Autowired
	private IStateService stateService;
	
	@Autowired
	private IFormService formService;
	
	@Autowired
	private IWorkflowDataService workflowService;

	/**
	 * Get a Definition or Model of the Form
	 * 
	 * @param request
	 * @param formId
	 * @param version
	 * @return
	 */
	@RequestMapping("/reports/formModel.ajax")
	public @ResponseBody
	MFForm getFormDefinition(HttpServletRequest request, @RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		String language = i18n.getSelectedLanguage();
		MFForm mfform = formModelService.getMFForm(formId, version, language, false);
		return mfform;
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

	
	
	

	@RequestMapping(value = "/reports/read.ajax")
	public @ResponseBody
	PagedData<List<Map<String, Object>>> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderByField,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "queryId", required = false) Long queryId,
			@RequestParam(value = "filterOptions", required = false) String filterOptionsJSON) {

		List<ReportFilterOptionDTO> filterOptions = ReportHelper.parseFilterOptions(filterOptionsJSON);

		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();

		List<String> fields;
		List<OrderBy> sortingColumns = null;
		boolean workflowEnabled = isWorkflowEnabled(formId, version, user);

		if (queryId == null) {
			fields = ReportHelper.getAllFields(formId, version, language, formModelService);
			/*
			 * The two meta columns were added after: #2281 Reports. As a user I
			 * would like to see "received at" and "user" column by default
			 * http://gohan.sodep.com.py/redmine/issues/2281
			 */
			List<String> metaColumns = Arrays.asList("meta_mail", "meta_receivedAt", "meta_savedAt");
			fields.addAll(metaColumns);
			// cap-376
			if (workflowEnabled) {
				fields.add("meta_stateId");
			}
		} else {
			QueryDefinitionDTO queryDef = queryService.getQueryDefinition(user, queryId);
			fields = queryDef.getSelectedTableColumns();
			sortingColumns = queryService.getSortingColumns(queryId);
		}

		MFForm mfForm = formModelService.getMFForm(formId, version, language);
		Map<String, MFElement> elementsMapByInstanceId = mfForm.elementsMappedByName();
		
		//cap-376 
		if (workflowEnabled) {
			transformStateFilterValue(formId, filterOptions);
		}
		ConditionalCriteria restrictions = ReportHelper.toConditionalCriteria(elementsMapByInstanceId, filterOptions);

		OrderBy orderBy = ReportHelper.getOrderBy(orderByField, order);

		// If the query has sortingColumns defined we use those
		if (sortingColumns != null && sortingColumns.size() > 0) {
			// orderByField is empty the first time the grid is rendered
			if (StringUtils.isEmpty(orderByField)) {
				orderBy = sortingColumns.get(0);
			}
		}

		PagedData<List<MFManagedData>> serviceData = formDataService.getFormData(user, formId, version, restrictions,
				orderBy, page, rows);

		List<Map<String, Object>> entriesList = new ArrayList<Map<String, Object>>();

		PagedData<List<Map<String, Object>>> controllerData = new PagedData<List<Map<String, Object>>>(entriesList,
				serviceData.getTotalCount(), serviceData.getPageNumber(), serviceData.getPageSize(),
				serviceData.getAvailable());

		for (MFManagedData data : serviceData.getData()) {
			Map<String, Object> entries = new HashMap<String, Object>();
			entries.put("id", data.getRowId());
			entries.putAll(ReportHelper.toMap(data, fields, i18n, elementsMapByInstanceId, prototypeService, stateService));
			entriesList.add(entries);
		}

		return controllerData;
	}

	private boolean isWorkflowEnabled(Long formId, Long version, User user) {
		FormDTO formDTO = buildFormDTO(formId, version);
		boolean workflowEnabled = workflowService.workflowEnabled(user, formDTO);
		return workflowEnabled;
	}

	private FormDTO buildFormDTO(Long formId, Long version) {
		FormDTO formDTO = new FormDTO();
		formDTO.setId(formId);
		formDTO.setVersion(version);
		return formDTO;
	}

	
	/**
	 * This method transform state filter value (if exist) from its name to id value
	 * 
	 * @param formId
	 * @param filterOptions
	 */
	private void transformStateFilterValue(
			Long formId, List<ReportFilterOptionDTO> filterOptions) {
		for (ReportFilterOptionDTO filterOption : filterOptions) {
			if (filterOption.getElementId().startsWith("meta_") &&
					filterOption.getElementId().substring("meta_".length()).equals(MFIncominDataWorkflow.META_FIELD_STATE_ID)) {
				if (filterOption.getValue() != null && !filterOption.getValue().trim().equals("") && filterOption.getOperator().equals("EQUALS")) {
					State state = stateService.findByName(formId, filterOption.getValue());
					if (state != null) {
						filterOption.setValue(state.getId().toString());
					}
				}
			}
		}
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


	@RequestMapping(value = "/reports/images/image.jpeg", method = RequestMethod.GET)
	public void getImage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId") Long formId, @RequestParam(value = "version") Long version,
			@RequestParam(value = "field") String fieldName, @RequestParam(value = "rowId") Long rowId) {
		sendImageToBrowser(request, response, formId, version, fieldName, rowId, false);
	}

	@RequestMapping(value = "/reports/images/image.jpeg", method = RequestMethod.POST)
	public void downloadImage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId") Long formId, @RequestParam(value = "version") Long version,
			@RequestParam(value = "field") String fieldName, @RequestParam(value = "rowId") Long rowId) {
		sendImageToBrowser(request, response, formId, version, fieldName, rowId, true);
	}
	
	private void sendImageToBrowser(HttpServletRequest request,
			HttpServletResponse response, Long formId, Long version,
			String fieldName, Long rowId, boolean downloadable) {
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		MFManagedData row = formDataService.getRow(user, formId, version, rowId);
		Map<String, ?> userData = row.getUserData();
		Object dataObj = userData.get(fieldName);

		if (dataObj instanceof MFBlob) {
			MFBlob blob = (MFBlob) dataObj;
			
			long computedTime = System.currentTimeMillis();
			MFFileStream mfFileStream = formDataService.getFileLazy(blob);
			computedTime = System.currentTimeMillis() - computedTime;

			logger.info("The image file getting process took " + computedTime + " ms.");
			try {
				InputStream is = mfFileStream.getInputStream();
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(blob.getContentType());
				if (downloadable) {
					response.setHeader("Content-Disposition", "attachment;filename=image.jpeg");
				} else {
					response.setDateHeader("Expires", System.currentTimeMillis() + 300000L);  // 5 min
				}
				OutputStream os = response.getOutputStream();
				byte[] buffer = new byte[32 * 1024];
				int read;
				
				computedTime = System.currentTimeMillis();
				while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);
				}
				computedTime = System.currentTimeMillis() - computedTime;

				logger.info("Write image file bytes to response OutputStream took " + computedTime + " ms.");
				is.close();
				os.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	

	/**
	 * This method map a meta column to its i18n key.
	 * 
	 * @param columnName
	 * @return
	 */
	private String metaDataColumnToI18nValue(String columnName) {
		// Changes performed on this method should also be made on query-amd.js,
		// since there is a duplicate list
		if (columnName.equals("meta_mail")) {
			return "web.home.reports.column.meta.mail";
		} else if (columnName.equals("meta_receivedAt")) {
			return "web.home.reports.column.meta.receivedAt";
		} else if (columnName.equals("meta_location")) {
			return "web.home.reports.column.meta.location";
		} else if (columnName.equals("meta_savedAt")) {
			return "web.home.reports.column.meta.savedAt";
		} else if (columnName.equals("meta_stateId")) {
			return "web.home.reports.column.meta.state";
		}
		return "";
	}

	@RequestMapping(value = "/reports/downloadxls.ajax", method = RequestMethod.POST)
	public void downloadXLS(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "queryId", required = false) Long queryId,
			@RequestParam(value = "filterOptions", required = false) String filterOptionsJSON,
			@RequestParam(value = "timezoneOffset", required = false) Integer timezoneOffset) {
		download(request, response, formId, version, queryId, filterOptionsJSON, timezoneOffset, Attributes.ATTRIBUTE_XLS);
	}

	@RequestMapping(value = "/reports/downloadcsv.ajax", method = RequestMethod.POST)
	public void downloadCSV(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId", required = false) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "queryId", required = false) Long queryId,
			@RequestParam(value = "filterOptions", required = false) String filterOptionsJSON,
			@RequestParam(value = "timezoneOffset", required = false) Integer timezoneOffset) {
		download(request, response, formId, version, queryId, filterOptionsJSON, timezoneOffset, Attributes.ATTRIBUTE_CSV);
	}

	@RequestMapping(value = "/reports/downloadpdf.ajax", method = RequestMethod.POST)
	public void downloadPDF(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "queryId", required = false) Long queryId,
			@RequestParam(value = "filterOptions", required = false) String filterOptionsJSON,
			@RequestParam(value = "timezoneOffset", required = false) Integer timezoneOffset) {
		download(request, response, formId, version, queryId, filterOptionsJSON, timezoneOffset, Attributes.ATTRIBUTE_PDF);
	}

    @RequestMapping(value = "/reports/downloadxlswithphotos.ajax", method = RequestMethod.POST)
    public void downloadXLSWithPhotos(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(value = "formId", required = true) Long formId,
                            @RequestParam(value = "version", required = true) Long version,
                            @RequestParam(value = "queryId", required = false) Long queryId,
                            @RequestParam(value = "filterOptions", required = false) String filterOptionsJSON,
                            @RequestParam(value = "timezoneOffset", required = false) Integer timezoneOffset) {
        download(request, response, formId, version, queryId, filterOptionsJSON, timezoneOffset, Attributes.ATTRIBUTE_XLS_WITH_PHOTOS);
    }

    @RequestMapping(value = "/reports/downloadRowPdf.ajax", method = RequestMethod.POST)
    public void downloadRowPDF(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(value = "formId", required = true) Long formId,
                            @RequestParam(value = "version", required = true) Long version,
                            @RequestParam(value = "queryId", required = false) Long queryId,
                            @RequestParam(value = "timezoneOffset", required = false) Integer timezoneOffset,
                            @RequestParam(value = "rowId", required = true) Long rowId) {

        SessionManager manager = new SessionManager(request);
        User user = manager.getUser();
        I18nManager i18n = manager.getI18nManager();
        String language = i18n.getSelectedLanguage();
        // Obtain the columns that the user has selected to download on the CSV
        // (if any)
        List<String> fields;
        Boolean locationsAsLinks = false;
        String reportTitle = null;

        if (queryId == null) {
            fields = getAllFields(formId, version, language, formModelService);
            // Bug #3992 En el reporte por default no aparecen "usuario" y "recibido al"
            fields.add("meta_mail");
            fields.add("meta_receivedAt");
        } else {
            QueryDefinitionDTO queryDef = queryService.getQueryDefinition(user, queryId);
            fields = queryDef.getSelectedTableColumns();
            locationsAsLinks = queryDef.getDownloadLocationsAsLinks();
            reportTitle = queryDef.getName();
        }

        MFForm mfForm = formModelService.getMFForm(formId, version, language);
        String fileName = null;
        Map<String, MFElement> elementsMapByInstanceId = mfForm.elementsMappedByName();

        /**
         * This method must be called because the headers of the documents may
         * not be the same as the columns visible in the web report.
         */
        List<ExportedDocumentHeader> headers = getHeadersForExportedDocuments(i18n, fields, elementsMapByInstanceId);
        Options options = downloadOptions(timezoneOffset, locationsAsLinks);

        MFManagedData row = formDataService.getRow(user, formId, version, rowId);

        if (reportTitle == null) {
            reportTitle = mfForm.getLabel();
        }
        fileName = mfForm.getLabel() + "_v" + mfForm.getVersion() + ".pdf";
        fileName = StringUtils.replace(fileName, " ", "_");
        response.setContentType("application/binary");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        sendRowPDFToBrowser(response, i18n, reportTitle, headers, elementsMapByInstanceId, row, options);
    }

	private static class ExportedDocumentHeader {

		String name;

		String label;

		boolean include;

		static List<String> toListOfLabels(List<ExportedDocumentHeader> headers) {
			List<String> labels = new ArrayList<>();
			for (ExportedDocumentHeader info : headers) {
				if (info.include) {
					labels.add(info.label);
				}
			}
			return labels;
		}

		static List<String> toListOfNames(List<ExportedDocumentHeader> headers) {
			List<String> names = new ArrayList<>();
			for (ExportedDocumentHeader info : headers) {
				if (info.include) {
					names.add(info.name);
				}
			}
			return names;
		}

	}

	private void download(HttpServletRequest request, HttpServletResponse response, Long formId, Long version,
			Long queryId, String filterOptionsJSON, Integer timezoneOffset, String type) {
		String unescapedJSON;
		try {
			unescapedJSON = URLDecoder.decode(filterOptionsJSON, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ApplicationException("Unable to download CSV", e);
		}
		List<ReportFilterOptionDTO> filterOptions = parseFilterOptions(unescapedJSON);
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		I18nManager i18n = manager.getI18nManager();
		Application application = manager.getApplication();
		String language = i18n.getSelectedLanguage();
		// Obtain the columns that the user has selected to download on the CSV
		// (if any)
		List<String> fields;
		Boolean locationsAsLinks = false;
		String reportTitle = null;
        Map<String, String> elementsFileNames = new HashMap<String, String>();

        if (queryId == null) {
			fields = getAllFields(formId, version, language, formModelService);
			// Bug #3992 En el reporte por default no aparecen "usuario" y "recibido al"
			fields.add("meta_mail");
			fields.add("meta_receivedAt");
			fields.add("meta_savedAt");
		} else {
			QueryDefinitionDTO queryDef = queryService.getQueryDefinition(user, queryId);
			fields = queryDef.getSelectedTableColumns();
			locationsAsLinks = queryDef.getDownloadLocationsAsLinks();
			reportTitle = queryDef.getName();
            elementsFileNames = queryDef.getElementsFileNames();
		}
        
        // FIXME CAP-438
        // Temporal 'hard-coded' changes only for Tigo Money client requirements
        // '114' is TIGO MONEY app id on production
        if (application.getId().equals(114) || application.getName().equalsIgnoreCase("TIGO MONEY")) {
        	fields.add(0, MFStorable._ID);
        }

		MFForm mfForm = formModelService.getMFForm(formId, version, language);
		String fileName = null;
		Map<String, MFElement> elementsMapByInstanceId = mfForm.elementsMappedByName();
		ConditionalCriteria restrictions = ReportHelper.toConditionalCriteria(elementsMapByInstanceId, filterOptions);
		List<MFManagedData> data = formDataService.getFormData(user, formId, version, restrictions, null);


		/**
		 * This method must be called because the headers of the documents may
		 * not be the same as the columns visible in the web report.
		 */
		List<ExportedDocumentHeader> headers = getHeadersForExportedDocuments(i18n, fields, elementsMapByInstanceId);
		Options options = downloadOptions(timezoneOffset, locationsAsLinks);

        if (Attributes.ATTRIBUTE_CSV.equals(type)) {
			response.setContentType("text/csv");
			response.setHeader("Content-Description:", "File Transfer");
			fileName = mfForm.getLabel() + "_v" + mfForm.getVersion() + ".csv";
			fileName = StringUtils.replace(fileName, " ", "_");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			sendCsvToBrowser(response, i18n, headers, elementsMapByInstanceId, data, options);
		} else if (Attributes.ATTRIBUTE_XLS.equals(type)) {
			fileName = mfForm.getLabel() + "_v" + mfForm.getVersion() + ".xls";
			fileName = StringUtils.replace(fileName, " ", "_");
			response.setContentType("application/binary");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			sendXlsToBrowser(response, i18n, headers, elementsMapByInstanceId, data, options);
		} else if (Attributes.ATTRIBUTE_PDF.equals(type)) {
			if (reportTitle == null) {
				reportTitle = mfForm.getLabel();
			}
			fileName = mfForm.getLabel() + "_v" + mfForm.getVersion() + ".pdf";
			fileName = StringUtils.replace(fileName, " ", "_");
			response.setContentType("application/binary");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			sendPDFToBrowser(response, i18n, reportTitle, headers, elementsMapByInstanceId, data, options);
		} else if (Attributes.ATTRIBUTE_XLS_WITH_PHOTOS.equals(type)) {
            fileName = mfForm.getLabel() + "_v" + mfForm.getVersion();
            fileName = StringUtils.replace(fileName, " ", "_");
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".zip");
            sendZipToBrowser(response, i18n, headers, elementsMapByInstanceId, data, options, fileName, elementsFileNames);
        }

	}

	private Options downloadOptions(Integer timezoneOffset, Boolean locationsAsLinks) {
		Options options = Options.newInstance();
		options.onlyDropdownValue(true).locationsAsLinks(locationsAsLinks);
		
		TimeZone timezone = parseGmtTimezone(timezoneOffset);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat(ReportConstants.SHORT_DATE_FORMAT);
		dateFormatter.setTimeZone(timezone);
		options.dateFormatter(dateFormatter);
		
		SimpleDateFormat timeFormatter = new SimpleDateFormat(ReportConstants.TIME_FORMAT);
		timeFormatter.setTimeZone(timezone);
		options.timeFormatter(timeFormatter);
		
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(ReportConstants.DATE_TIME_FORMAT);
		dateTimeFormatter.setTimeZone(timezone);
		options.dateTimeFormatter(dateTimeFormatter);
		
		DecimalFormat numberFormatter = new DecimalFormat(ReportConstants.NUMBER_FORMAT);
		options.numberFormatter(numberFormatter);
		
		return options;
	}

	private TimeZone parseGmtTimezone(Integer timezoneOffset) {
		String gmt = "GMT";
		if (timezoneOffset != null) {
			int hours = timezoneOffset / 60;
			if (hours > 0) {
				gmt = gmt + "-" + hours;
			} else if (hours < 0) {
				gmt = gmt  + "+" + (-1 * hours);
			}
			return TimeZone.getTimeZone(gmt);
		} else{
			return TimeZone.getTimeZone(gmt);
		}
	
	}

    private void sendCsvToBrowser(HttpServletResponse response, I18nManager i18n, List<ExportedDocumentHeader> headers,
                                  Map<String, MFElement> elementsMapByInstanceId, List<MFManagedData> data, Options options) {
        try {
            CSVFormat csvFileFormat = CSVFormat.EXCEL;
            PrintWriter out = response.getWriter();
            CSVPrinter csvPrinter = new CSVPrinter(out, csvFileFormat);
            // Print the header of the CSV
            csvPrinter.printRecord(ExportedDocumentHeader.toListOfLabels(headers));
            // Print the data of the CSV
            List<String> names = ExportedDocumentHeader.toListOfNames(headers);

            for (MFManagedData row : data) {
                Map<String, Object> entry = ReportHelper.toMap(row, names, i18n, elementsMapByInstanceId,
                        prototypeService, options, stateService);
                writeCsvRow(csvPrinter, headers, entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCsvRow(CSVPrinter printer, List<ExportedDocumentHeader> headers, Map<String, Object> data)
			throws IOException {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < headers.size(); i++) {
			ExportedDocumentHeader headerInfo = headers.get(i);
			if (headerInfo.include) {
				String field = headerInfo.name;
				Object obj = null;
				if (field.equals("meta_" + MFIncominDataWorkflow.META_FIELD_STATE_ID)) {
					obj = data.get("meta_state");
					if (!(obj instanceof String) ) {
						obj = ((DocumentWorkflowInfoDTO) obj).getName();
					}
					
				} else { 
					obj = data.get(field);
				}

				if (obj != null) {
					list.add(obj.toString());
				} else {
					list.add("");
				}
			}
		}
		printer.printRecord(list);
	}

	private void sendPDFToBrowser(HttpServletResponse response, I18nManager i18n, String title,
			List<ExportedDocumentHeader> headers, Map<String, MFElement> elementMapByInstanceId,
			List<MFManagedData> data, Options options) {
		try {
			PdfGenerator pdfGenerator = new PdfGenerator(title);
			File tmpFileName = File.createTempFile("mf-download-", ".tmp");
			Table<Integer, Integer, Object> tableData = toTableData(i18n, headers, elementMapByInstanceId, data,
					options);
			pdfGenerator.generateReportFile(tableData, ExportedDocumentHeader.toListOfLabels(headers),
					tmpFileName.getAbsolutePath());
			sendFileToBrowser(response, tmpFileName);
		} catch (ConvertException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendXlsToBrowser(HttpServletResponse response, I18nManager i18n, List<ExportedDocumentHeader> headers,
			Map<String, MFElement> elementMapByInstanceId, List<MFManagedData> list, Options options) {
		try {
			File tmpFileName = File.createTempFile("mf-download-", ".tmp");
			ExcelGenerator excelGenerator = new ExcelGenerator();
			Table<Integer, Integer, Object> tableData = toTableData(i18n, headers, elementMapByInstanceId, list,
					options);
			excelGenerator.generateExcel(tableData, ExportedDocumentHeader.toListOfLabels(headers),
					tmpFileName.getAbsolutePath());
			sendFileToBrowser(response, tmpFileName);
		} catch (ConvertException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    private void sendZipToBrowser(HttpServletResponse response, I18nManager i18n, List<ExportedDocumentHeader> headers,
                                  Map<String, MFElement> elementsMapByInstanceId, List<MFManagedData> dataList, Options options,
                                  String reportFileName, Map<String, String> elementsFileNames) {
        try {
            File tmpFileName = File.createTempFile("mf-download-", ".tmp");

            // Generate the report file to be compressed with the images ---------- //
           ExcelGenerator excelGenerator = new ExcelGenerator();
           Table<Integer, Integer, Object> tableData = toTableData(i18n, headers, elementsMapByInstanceId, dataList,
                        options);
           excelGenerator.generateExcel(tableData, ExportedDocumentHeader.toListOfLabels(headers),
                        tmpFileName.getAbsolutePath());


            // Zip generation parameters
            ReportHelper.ZipOptions zipOptions = ReportHelper.ZipOptions.newInstance();
            zipOptions.reportFile(reportFileName, tmpFileName);
            zipOptions.elementsFileNames(elementsFileNames);
            List<String> names = new ArrayList<String>();
            names.addAll(elementsMapByInstanceId.keySet());
            List<Map<String, Object>> entriesList = ReportHelper.toList(dataList, names, i18n, elementsMapByInstanceId, prototypeService, options);


            response.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = response.getOutputStream();

            ReportHelper.generateZip(os, entriesList, dataList, elementsMapByInstanceId, zipOptions, formDataService);
            os.flush();
            tmpFileName.delete();
        } catch (ConvertException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO This should generate a PDF with a 'form view' of the row.
    // For now we have a table view.
    private void sendRowPDFToBrowser(HttpServletResponse response, I18nManager i18n, String title,
                                  List<ExportedDocumentHeader> headers, Map<String, MFElement> elementMapByInstanceId,
                                  MFManagedData row, Options options) {
        try {
            PdfGenerator pdfGenerator = new PdfGenerator(title);
            File tmpFileName = File.createTempFile("mf-download-", ".tmp");
            List<MFManagedData> data = new ArrayList<MFManagedData>();
            data.add(row);
            Table<Integer, Integer, Object> tableData = toTableData(i18n, headers, elementMapByInstanceId, data,
                    options);
            pdfGenerator.generateReportFile(tableData, ExportedDocumentHeader.toListOfLabels(headers),
                    tmpFileName.getAbsolutePath());
            sendFileToBrowser(response, tmpFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ConvertException e) {
            throw new RuntimeException(e);
        }
    }


    private void sendFileToBrowser(HttpServletResponse response, File tmpFileName) throws FileNotFoundException,
			IOException {
		FileInputStream fis = new FileInputStream(tmpFileName);
		ServletOutputStream os = response.getOutputStream();
		byte[] bufferData = new byte[1024];
		int read = 0;
		while ((read = fis.read(bufferData)) != -1) {
			os.write(bufferData, 0, read);
		}
		os.flush();
		os.close();
		fis.close();
		tmpFileName.delete();
	}

	/**
	 * An instance of com.google.common.collect.Table<Integer, Integer, Object>
	 * is used to generate Xls an Pdf documents
	 * 
	 * @param i18n
	 * @param headers
	 * @param elementsMapByInstanceId
	 * @param data
	 * @param locationsAsLinks
	 * @return
	 */
	private Table<Integer, Integer, Object> toTableData(I18nManager i18n, List<ExportedDocumentHeader> headers,
			Map<String, MFElement> elementsMapByInstanceId, List<MFManagedData> data, Options options) {
		int rowCount = 0;
		Table<Integer, Integer, Object> table = HashBasedTable.create();
		List<String> names = ExportedDocumentHeader.toListOfNames(headers);
		
		for (MFManagedData row : data) {
			Map<String, Object> entry = ReportHelper.toMap(row, names, i18n, elementsMapByInstanceId, prototypeService, options, stateService);
			int col = 0;
			for (int i = 0; i < headers.size(); i++) {
				ExportedDocumentHeader headerInfo = headers.get(i);
				if (headerInfo.include) {
					String headerInfoName = headerInfo.name;
					Object obj = null;
					if (headerInfoName.equals("meta_" + MFIncominDataWorkflow.META_FIELD_STATE_ID)) {
						obj = entry.get("meta_state");
						if (!(obj instanceof String)) {
							obj = ((DocumentWorkflowInfoDTO) obj).getName();
						}
					} else {
						obj = entry.get(headerInfoName);
					}
					
					
					if (obj != null) {
						table.put(rowCount, col, obj);
					} else {
						table.put(rowCount, col, "");
					}
					col++;
				}
			}
			rowCount += 1;
		}
		return table;
	}

	/**
	 * Given the columns of the query, this method returns a list with more
	 * information about which ones should be used in the headers of documents
	 * to be exported
	 * 
	 * Each elements gives the column name, the label, and whether it should be
	 * visible or not.
	 * 
	 * This method should be used to get the headers for CSV, XLS or PDF
	 * documents.
	 * 
	 * @param i18n
	 * @param allColumns
	 * @param elementMappedByInstanceId
	 * @return
	 */
	private List<ExportedDocumentHeader> getHeadersForExportedDocuments(I18nManager i18n, List<String> allColumns,
			Map<String, MFElement> elementMappedByInstanceId) {
		List<ExportedDocumentHeader> headers = new ArrayList<ExportedDocumentHeader>();
		for (int i = 0; i < allColumns.size(); i++) {
			ExportedDocumentHeader header = new ExportedDocumentHeader();
			headers.add(header);
			String name = header.name = allColumns.get(i);
			if (name.startsWith("meta_")) {
				header.label = i18n.getMessage(metaDataColumnToI18nValue(name));
				header.include = true;
			} else if (name.equals(MFStorable._ID)) {	// FIXME CAP-438 temporal changes
				header.label = "ID";
				header.include = true;
			} else {
				MFElement mfElement = elementMappedByInstanceId.get(name);
				MFPrototype proto = mfElement.getProto();
				header.label = proto.getLabel();
				if (proto instanceof MFPhoto || proto instanceof MFSignature) {
					// do not include images (it just
					// don't make sense)
					header.include = false;
				} else {
					// e.g. the items of type headline shouldn't be included
					header.include = !proto.isOutputOnly();
				}
			}
		}
		return headers;
	}

	

}
