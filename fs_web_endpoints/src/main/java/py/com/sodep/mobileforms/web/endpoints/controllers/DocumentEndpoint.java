package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.DocumentSaveRequest;
import py.com.sodep.mf.exchange.objects.data.MFMultiplexedFileSerializer;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;
import py.com.sodep.mf.exchange.objects.upload.MFMultiplexedFile;
import py.com.sodep.mf.exchange.objects.upload.UploadContentResult;
import py.com.sodep.mf.exchange.objects.upload.UploadContentResult.Result;
import py.com.sodep.mf.exchange.objects.upload.UploadHandle;
import py.com.sodep.mf.exchange.objects.upload.UploadProgress;
import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.documents.upload.IUploadManager;
import py.com.sodep.mobileforms.api.documents.upload.IUploadService;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.server.ServerProperties;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.data.FormMatchQuery;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.reports.IReportQueryService;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;
import py.com.sodep.mobileforms.web.utils.ReportHelper;


@Controller
@Api(value = "documents", description = "Operations to upload documents", position = 2)
public class DocumentEndpoint extends EndpointController {

	public static Logger logger = LoggerFactory.getLogger(DocumentEndpoint.class);

	@Autowired
	private IFormDataService formDataService;

	@Autowired
	private IFormModelService formModelService;

	@Autowired
	private ServerProperties serverProperties;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private MFLicenseManager licenseManager;

	@Autowired
	private IUploadService uploadService;

	@Autowired
	private IUploadManager uploadManager;

	@Autowired
	private IReportQueryService queryService;

	@Autowired
	private IElementPrototypeService prototypeService;
	
	@Autowired
	private IParametersService parameterService;
	
	@Autowired
	private IStateService stateService;

	private MFMultiplexedFileSerializer mfMultiplexedFileSerializer = new MFMultiplexedFileSerializer();

	@RequestMapping(value = "/document/blob", method = RequestMethod.GET)
	public void downloadBlob(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId") Long formId, @RequestParam(value = "version") Long version,
			@RequestParam(value = "field") String fieldName, @RequestParam(value = "rowId") Long rowId) {
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		MFManagedData row = formDataService.getRow(user, formId, version, rowId);
		Map<String, ?> userData = row.getUserData();
		Object dataObj = userData.get(fieldName);

		if (dataObj instanceof MFBlob) {
			MFBlob blob = (MFBlob) dataObj;
			MFFileStream mfFileStream = formDataService.getFileLazy(blob);
			try {
				InputStream is = mfFileStream.getInputStream();
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(blob.getContentType());
				OutputStream os = response.getOutputStream();
				byte[] buffer = new byte[32 * 1024];
				int read;
				while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);
				}
				is.close();
				os.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@RequestMapping(value = "/document/list", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, Object>> listDocuments(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "rowId", required = false, defaultValue = "0") Long rowId,
			@RequestParam(value = "rows", required = false, defaultValue = "100") Integer rows) {
		
		if (rows > 100 || rows <= 0) {
			rows = 100;
		}
		
		if (rowId < 0) {
			rowId = 0L;
		}

		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();

		List<String> columnList = getAllColumns(formId, version, language);
		MFForm mfForm = formModelService.getMFForm(formId, version, language);
		Map<String, MFElement> elementsMappedByInstanceId = mfForm.elementsMappedByName();
		
		List<MFManagedData> serviceData = formDataService.getFormDataByRowId(user, formId, version, rowId, rows);

		List<Map<String, Object>> entriesList = new ArrayList<Map<String, Object>>();

		for (MFManagedData row : serviceData) {
			Map<String, Object> entry = new LinkedHashMap<>();
			// is this right?
			entry.put("id", row.getRowId());
			Map<String, Object> metaData = row.getMetaData();
			entry.put("user", metaData.get(MFIncomingDataI.META_FIELD_MAIL));
			entry.put("time", MFDataHelper.serialize((Date) metaData.get(MFIncomingDataI.META_FIELD_RECEIVED_AT)));
			Map<String, Object> userData = row.getUserData();
			Map<String, Object> data = processDataToSend(columnList, userData, i18n, elementsMappedByInstanceId,
					false);
			entry.put("data", data);
			entriesList.add(entry);
		}

		return entriesList;
	}

	public static class DocumentUploaded {

		private String handle;

		public String getHandle() {
			return handle;
		}

		public void setHandle(String handle) {
			this.handle = handle;
		}

	}

	@RequestMapping(value = "/document", method = RequestMethod.POST)
	public @ResponseBody
	DocumentUploaded simpleUpload(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "key") String key, @RequestBody DocumentSaveRequest saveRequest) throws IOException {
		SessionManager manager = new SessionManager(request);
		Application app = manager.getApplication();
		User user = manager.getUser();
		// This is because the application could have been
		// deactivated. manager.getApplication() doesn't return
		// a fresh copy from the db
		checkIfApplicationIsActive(user, app.getId());
		// check if device is associated
		checkApplicationLicense(app);

		// DocumentSaveRequest saveRequest = parseDocumentSaveRequest(request);
		File file = saveToMultiplexedFile(saveRequest);

		UploadHandle requestHandle = uploadService.handleForSimpleUpload(app, user, key, file.length());
		String handle = requestHandle.getHandle();
		final DocumentUpload upload = uploadService.getDocumentUploadData(user, handle);
		File uploadedFile = getUplodedFile(upload);
		file.renameTo(uploadedFile);
		uploadManager.changeStatusToCompleted(handle);
		DocumentUploaded uploaded = new DocumentUploaded();
		uploaded.setHandle(handle);
		return uploaded;
	}

	private File saveToMultiplexedFile(DocumentSaveRequest saveRequest) throws IOException {
		OutputStream os = null;
		try {
			MFMultiplexedFile multiplexedFile = new MFMultiplexedFile();
			String json = objectMapper.writeValueAsString(saveRequest);
			multiplexedFile.addFile("document", "application/json", json);
			File tmp = File.createTempFile("cp_", ".tmp", serverProperties.getUploadFolder());
			os = new FileOutputStream(tmp);
			mfMultiplexedFileSerializer.write(multiplexedFile, os);
			return tmp;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {

				}
			}
		}
	}

	
	private Map<String, Object> processDataToSend(final List<String> columnList, final Map<String, Object> userData,
			I18nManager i18n, final Map<String, MFElement> elementsMappedByInstanceId, boolean labelAsKey) {
		Map<String, Object> data = new HashMap<String, Object>();

		for (String column : columnList) {
			MFElement element = elementsMappedByInstanceId.get(column);
			Object value = null;
			assert (element != null);
			if (!element.getProto().isOutputOnly()) {
				value = userData.get(column);
				switch (element.getProto().getType()) {
				case INPUT:
					value = processInput(element, value);
					break;
				case PHOTO:
					Map<String, String> photoMap = new HashMap<>();
					if (value != null) {
						MFBlob blob = (MFBlob) value;
						photoMap.put("contentType", blob.getContentType());
						value = photoMap;
					}
					break;
				case SELECT:
					break;
				case LOCATION:
					break;
				default:

				}
			}
			String key = labelAsKey ? element.getProto().getLabel() : column;
			data.put(key, value);
		}
		return data;
	}

	private Object processInput(MFElement element, final Object value) {
		MFInput mffinput = (MFInput) element.getProto();
		switch (mffinput.getSubtype()) {
		case DATE:
			return MFDataHelper.serialize((Date) value);
		case TIME:
			return MFDataHelper.serialize((Date) value);
		case DATETIME:
			return MFDataHelper.serialize((Date) value);
		case DECIMAL:
		case INTEGER:
			return MFDataHelper.serialize((Number) value);
		case PASSWORD:
			return "*";
		default:
			return value;
		}
	}

	// FIXME copied from ReportController
	private List<String> getAllColumns(Long formId, Long version, String language) {
		ArrayList<String> columnsToShow = new ArrayList<String>();
		MFForm mfform = formModelService.getMFForm(formId, version, language, false);
		// show all columns of the form
		List<MFElement> allElements = mfform.listAllElements();
		for (MFElement mfElement : allElements) {
			columnsToShow.add(mfElement.getInstanceId());
		}
		return columnsToShow;
	}

	
	@RequestMapping(value = "/document/upload/handle", method = RequestMethod.POST)
	public @ResponseBody
	UploadHandle requestUploadHandle(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("documentId") String documentId, @RequestParam("deviceId") String deviceId,
			@RequestParam("size") long size, @RequestParam(value="formId", required = false) Long formId) {
		SessionManager manager = new SessionManager(request);

		Application app = manager.getApplication();
		User user = manager.getUser();

		// This is because the application could have been
		// deactivated. manager.getApplication() doesn't return
		// a fresh copy from the db
		checkIfApplicationIsActive(user, app.getId());

		// check if device is associated
		logger.debug("REQUEST_UPLOAD : user = #" + user.getId() + " " + user.getMail() + "; documentId = " + documentId
				+ "; deviceId = " + deviceId + "; app = #" + app.getId());
		boolean associated = deviceService.isDeviceAssociated(user, app, deviceId);
		if (!associated) {
			logger.warn("Device not associated:  user = #" + user.getId() + "; deviceId = " + deviceId);
			throwDeviceNotAssociated();
		}
		
		checkApplicationLicense(app);
		if (formId != null
				&& !authorizationControlService.hasFormLevelAccess(formId,
						user, AuthorizationNames.Form.MOBILE)) {
			throwNoAuthorization();
		}
		
		UploadHandle requestHandle = uploadService.requestHandle(app, user, deviceId, documentId, size);
		// set Upload location (upload URI)
		IParameter parameter = parameterService.getParameter(DBParameters.UPLOAD_LOCATION);
		if (parameter != null && (parameter.getActive() != null && parameter.getActive())) {
			final String value = parameter.getValue();
			if (value != null && value.trim().length() > 0) {
				final String location = value.replace("{handle}", requestHandle.getHandle());
				requestHandle.setLocation(location);
				logger.debug("Upload URI " + value);
			}
		}
		
		return requestHandle; 
	}

	// checks the license date
	private void checkApplicationLicense(Application app) {
		// FIXME the expiration date should be precalculated to avoid this
		// unnecessary computation every time
		MFApplicationLicense license = licenseManager.getLicense(app.getId());
		Date creationDate = license.getCreationDate();
		Long validDays = license.getValidDays();
		if (creationDate != null && validDays != null) {
			Calendar expirationCal = Calendar.getInstance();
			expirationCal.setTime(creationDate);
			expirationCal.add(Calendar.DAY_OF_YEAR, validDays.intValue());
			Calendar now = Calendar.getInstance();
			boolean before = expirationCal.before(now);
			if (before) {
				throwLicenseExpired();
			}
		}
	}

	private File getUplodedFile(DocumentUpload documentUpload) {
		File uploadFolder = serverProperties.getUploadFolder();
		if (uploadFolder == null) {
			throw new RuntimeException("upload folder is null");
		}
		File f = new File(uploadFolder, documentUpload.getFileName());
		return f;
	}

	@RequestMapping(value = "/document/upload/status", method = RequestMethod.GET)
	public @ResponseBody
	UploadProgress uploadStatus(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("handle") String handle) {

		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		DocumentUpload documentUploadData = uploadService.getDocumentUploadData(user, handle);
		UploadProgress progress = new UploadProgress();
		if (documentUploadData != null) {
			progress.setStatus(documentUploadData.getStatus());
			File file = getUplodedFile(documentUploadData);
			progress.setReceivedBytes(file.length());
			progress.setSize(documentUploadData.getSize());
		} else {
			progress.setStatus(UploadStatus.INVALID);
		}
		logger.debug("Upload Status : [ handle: " + handle + ", status: " + progress.getStatus() + ", received bytes: "
				+ progress.getReceivedBytes() + " ]");
		return progress;
	}

	private class ContentRange {

		private long firstByte;

		private long lastByte;

		private long totalLength;

	}

	@RequestMapping(value = "/document/upload/file", method = RequestMethod.PUT)
	public @ResponseBody
	UploadContentResult uploadFile(final HttpServletRequest request, final HttpServletResponse response,
			final @RequestParam("handle") String handle) {
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		logger.debug("Upload file for " + handle);
		final DocumentUpload upload = uploadService.getDocumentUploadData(user, handle);
		
		UploadContentResult result = null;

		if (upload != null) {
			if (upload.getStatus() == UploadStatus.PROGRESS) {
				result = processUploadInProgress(request, handle, upload);
			} else {
				result = new UploadContentResult();
				result.setResult(Result.UPLOAD_NOT_IN_PROGRESS);
				result.setStatus(upload.getStatus());
				result.setReceivedBytes(-1);
			}
		} else {
			result = new UploadContentResult();
			result.setResult(Result.INVALID_NON_EXISTENT);
			result.setStatus(UploadStatus.INVALID);
			result.setReceivedBytes(-1);
		}
		return result;
	}
	
	@RequestMapping(value = "/document/copy/{formId}/{version}/{fromRowId}", method = RequestMethod.POST)
	public @ResponseBody void copy(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "fromRowId") Long fromRowId, 
			@PathVariable(value = "formId") Long formId,
			@PathVariable(value = "version") Long version,
			@RequestBody DocumentSaveRequest saveRequest) throws IOException, InterruptedException {
		
		SessionManager manager = new SessionManager(request);
		Application app = manager.getApplication();
		User user = manager.getUser();
		// This is because the application could have been
		// deactivated. manager.getApplication() doesn't return
		// a fresh copy from the db
		checkIfApplicationIsActive(user, app.getId());
		// check if device is associated
		checkApplicationLicense(app);
		
		MFOperationResult result = formDataService.copyData(user, formId, version, fromRowId, saveRequest);
		if (result.getResult().equals(RESULT.SUCCESS)) {
			sendObject(response, result);
		} else {
			sendObject(response, result, HttpServletResponse.SC_CONFLICT);
		}
		
	}
	
	@RequestMapping(value = "/document/markreceived/{formId}/{version}", method = RequestMethod.POST)
	public @ResponseBody void markReceived(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "formId") Long formId,
			@PathVariable(value = "version") Long version,
			@RequestBody DocumentSaveRequest saveRequest) throws IOException, InterruptedException {
		
		SessionManager manager = new SessionManager(request);
		Application app = manager.getApplication();
		User user = manager.getUser();
		// This is because the application could have been
		// deactivated. manager.getApplication() doesn't return
		// a fresh copy from the db
		checkIfApplicationIsActive(user, app.getId());
		// check if device is associated
		checkApplicationLicense(app);

		
	}

	private UploadContentResult processUploadInProgress(final HttpServletRequest request, final String handle,
			final DocumentUpload upload) {
		final ContentRange contentRange = extractContentRange(request);
		final long contentLength = contentRange.lastByte - contentRange.firstByte + 1;
		File uploadedFile = getUplodedFile(upload);
		final long received = uploadedFile.length();

		UploadContentResult result = null;
		if (received == contentRange.firstByte && contentRange.totalLength == upload.getSize()) {
			result = processUpload(request, handle, upload, uploadedFile, contentLength, contentRange);
		} else {
			result = new UploadContentResult();
			result.setResult(Result.INVALID_RANGE);
			result.setStatus(upload.getStatus());
			result.setReceivedBytes(received);
		}
		return result;
	}

	private UploadContentResult processUpload(final HttpServletRequest request, final String handle,
			final DocumentUpload upload, File file, final long contentLength, final ContentRange contentRange) {

		UploadContentResult result = new UploadContentResult();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
			logger.debug("Writing to file " + file.getAbsolutePath());
			long totalLength = writeToStream(fos, request.getInputStream(), contentRange.firstByte);

			if (totalLength != contentLength) {
				result.setResult(Result.INVALID_LENGTH);
				result.setStatus(upload.getStatus());
				result.setReceivedBytes(totalLength);
			} else {
				result.setResult(Result.ACCEPTED);
				if (file.length() == upload.getSize()) {
					uploadManager.changeStatusToCompleted(handle);
				} else {
					result.setStatus(UploadStatus.PROGRESS);
				}

				result.setReceivedBytes(totalLength);
			}
		} catch (IOException e) {
			logger.error("Unable to open file for handle '" + handle + "'", e);
			throw new RuntimeException(e);
		} finally {
			close(fos);
		}
		return result;
	}

	private static final int BUFFER_SIZE = 8 * 1024;

	/**
	 * Writes up to the last byte from the InputStream to the FileOutputStream.
	 * 
	 * The file is written starting at the given position.
	 * 
	 * The return value is the number of bytes written to the file.
	 * 
	 * @param fos
	 * @param in
	 * @param position
	 * @return
	 * @throws IOException
	 */
	private long writeToStream(FileOutputStream fos, final InputStream in, long position) throws IOException {
		FileChannel channel = fos.getChannel();
		channel.position(position);

		byte[] buffer = new byte[BUFFER_SIZE];
		
		int length = 0;
		long totalLength = 0;
		while ((length = in.read(buffer)) != -1) {
			totalLength += length;
			ByteBuffer src = ByteBuffer.wrap(buffer, 0, length);
			channel.write(src);
			channel.force(true);
			logger.trace("Wrote " + length + " bytes to file");
		}
		return totalLength;
	}

	private ContentRange extractContentRange(HttpServletRequest request) {
		String contentRangeStr = request.getHeader("Content-Range");
		ContentRange contentRange = parseContentRange(contentRangeStr);
		return contentRange;
	}

	private ContentRange parseContentRange(final String contentRangeStr) {
		final String regex = "^bytes (\\d+)-(\\d+)/(\\d+)$";
		if (!contentRangeStr.matches(regex)) {
			return null;
		}

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(contentRangeStr);

		ContentRange contentRange = new ContentRange();
		if (matcher.find()) {
			String firstByte = matcher.group(1);
			String lastByte = matcher.group(2);
			String contentLength = matcher.group(3);

			contentRange.firstByte = Long.parseLong(firstByte);
			contentRange.lastByte = Long.parseLong(lastByte);
			contentRange.totalLength = Long.parseLong(contentLength);
		}
		return contentRange;
	}

	/***
	 * Transform a map of <String,String> into a map of <String,Object>. The
	 * destination object is based on the {@link MFField#getType()}
	 * 
	 * @param parsedData
	 * @param fieldNameToField
	 * @return
	 */
	public Map<String, Object> translateDataTypes(Map<String, Object> parsedData, Map<String, MFField> fieldNameToField) {
		
		HashMap<String, Object> m = new HashMap<String, Object>();
		Set<String> keys = fieldNameToField.keySet();
		for (String fieldKey : keys) {
			MFField fieldDef = fieldNameToField.get(fieldKey);
			Object originalObject = parsedData.get(fieldKey);
			if (originalObject == null) {
				if (parsedData.keySet().contains(fieldKey)) {
					throw new IllegalArgumentException(
							"The value for the field "
									+ fieldKey
									+ " is null. This might be a wrong assignment. If you really want to assign a null value, please remove the key from the map and null will be assumed");
				} else {
					continue;
				}
			}

			if (originalObject instanceof String) {
				String originalValue = (String) originalObject;
				Object value = MFDataHelper.unserialize(fieldDef.getType(), originalValue);

				if (value == null) {
					throw new IllegalArgumentException("Couldn't translate the value \"" + originalValue + " \" to "
							+ fieldDef.getType());
				}
				m.put(fieldKey, value);
			} else {
				m.put(fieldKey, originalObject);
			}
		}

		return m;
	}
	
	@RequestMapping(value = "/document/received/read", method = RequestMethod.POST)
	public @ResponseBody
	PagedData<List<Map<String, Object>>> receivedRead(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderByField,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version,
			@RequestParam(value = "queryId", required = false) Long queryId,
			@RequestParam(value = "filterOptions", required = false) String filterOptionsJSON,
			@RequestParam(value = "trackingFormId", required = true) Long trackingFormId,
			@RequestParam(value = "trackingVersion", required = true) Long trackingVersion,
			@RequestParam(value = "receivedElementId", required = true) String receivedElementId,
			@RequestParam(value = "trackingElementId", required = true) String trackingElementId) {
		
		List<ReportFilterOptionDTO> filterOptions = ReportHelper.parseFilterOptions(filterOptionsJSON);
		
		
		SessionManager manager = new SessionManager(request);
		User user = manager.getUser();
		I18nManager i18n = manager.getI18nManager();
		String language = i18n.getSelectedLanguage();

		List<String> fields;
		List<OrderBy> sortingColumns = null;

		if (queryId == null) {
			fields = ReportHelper.getAllFields(formId, version, language, formModelService);
			/*
			 * The two meta columns were added after: #2281 Reports. As a user I
			 * would like to see "received at" and "user" column by default
			 * http://gohan.sodep.com.py/redmine/issues/2281
			 */
			List<String> metaColumns = Arrays.asList("meta_mail", "meta_receivedAt", "meta_savedAt");
			fields.addAll(metaColumns);
		} else {
			QueryDefinitionDTO queryDef = queryService.getQueryDefinition(user, queryId);
			fields = queryDef.getSelectedTableColumns();
			sortingColumns = queryService.getSortingColumns(queryId);
		}
		// Add meta_stateId for getting later workflow info
		fields.add("meta_stateId");
		
		
		MFForm mfForm = formModelService.getMFForm(formId, version, language);
		Map<String, MFElement> elementsMapByInstanceId = mfForm.elementsMappedByName();
		ConditionalCriteria restrictions = ReportHelper.toConditionalCriteria(elementsMapByInstanceId, filterOptions);
		ConditionalCriteria trackingRestrictions = ReportHelper.getSubRestrictions(user, elementsMapByInstanceId, filterOptions);
        
		
		OrderBy orderBy = ReportHelper.getOrderBy(orderByField, order);

		// If the query has sortingColumns defined we use those
		if (sortingColumns != null && sortingColumns.size() > 0) {
			// orderByField is empty the first time the grid is rendered
			if (StringUtils.isEmpty(orderByField)) {
				orderBy = sortingColumns.get(0);
			}
		}

		FormMatchQuery matchQuery = FormMatchQuery.builder()
		.matchQueryFormId(trackingFormId)
		.matchQueryFormVersion(trackingVersion)
		.matchQueryElementId(trackingElementId)
		.matchQueryRestrictions(trackingRestrictions)
		.formId(formId).version(version)
		.elementId(receivedElementId)
		.restrictions(restrictions)
		.orderBy(orderBy).page(page).rows(rows).build();
		
		PagedData<List<MFManagedData>> serviceData = formDataService.getDataMatchingForms(user, matchQuery);

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

}
