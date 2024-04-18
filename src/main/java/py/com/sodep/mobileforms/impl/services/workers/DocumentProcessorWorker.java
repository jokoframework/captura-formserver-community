package py.com.sodep.mobileforms.impl.services.workers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLocationData;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.DocumentSaveRequest;
import py.com.sodep.mf.exchange.objects.data.DocumentSaveRequest.PageData;
import py.com.sodep.mf.exchange.objects.data.MFMultiplexedFileSerializer;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.upload.MFMultiplexedFile;
import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.MFPage;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.documents.upload.IUploadService;
import py.com.sodep.mobileforms.api.dtos.DocumentDTO;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.StateDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.server.ServerProperties;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.IFormDataService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.notifications.INotificationManager;
import py.com.sodep.mobileforms.api.services.notifications.NotificationReport;
import py.com.sodep.mobileforms.api.services.notifications.NotificationType;
import py.com.sodep.mobileforms.api.services.workers.IDocumentProcessorWorker;
import py.com.sodep.mobileforms.api.services.workflow.IStateService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.utils.TemporalUtils;

/**
 * This worker search for documents that have been uploaded and send them to the
 * MongoDB
 *
 * @author danicricco
 */
public class DocumentProcessorWorker extends BackgroundWorker implements
		IDocumentProcessorWorker {

	private static Logger logger = LoggerFactory
			.getLogger(DocumentProcessorWorker.class);

	private int readBufferSize = 256 * 1024;

	@Autowired
	private IUploadService uploadService;

	@Autowired
	private IFormModelService formModelService;

	@Autowired
	private ServerProperties serverProperties;

	@Autowired
	private IUserService userService;

	@Autowired
	private IFormDataService formDataService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IAuthorizationControlService authorizationControlService;

	@Autowired
	private INotificationManager notificationManager;

	@Autowired
	private IStateService stateService;

	@Autowired
	private IWorkflowDataService workflowDataService;

	public DocumentProcessorWorker(TaskExecutor executor) {
		super(executor);
	}

	public DocumentProcessorWorker() {
		super(null);
	}

	@Override
	protected void doWork() {
		logger.trace("Checking if there is a document to process");

		DocumentUpload doc = uploadService.obtainDocumentToProcess();
		DocumentSaveRequest saveRequestForMailInfo = null;
		if (doc == null) {
			logger.trace("Nothing to process");
			return;
		}
		logger.debug("Processing document #" + doc.getId());
		MFMultiplexedFile multiplexedFile = null;
		String fileName = serverProperties.getUploadFolder().getAbsolutePath()
				+ "/" + doc.getFileName();
		File file = new File(fileName);
		// TODO obtain the buffer size from the system parameters
		MFMultiplexedFileSerializer serializer = new MFMultiplexedFileSerializer(
				256 * 1024);
		try {
			multiplexedFile = serializer.parse(file);
			String saveRequestJSON = multiplexedFile.getFile("document");
			if (saveRequestJSON == null) {
				// A really, really WTF situations
				throw new RuntimeException("The multiplexed file #"
						+ doc.getId()
						+ " doesn't contain a file with the name 'document'");
			}
			ObjectMapper mapper = new ObjectMapper();
			DocumentSaveRequest saveRequest = mapper.readValue(saveRequestJSON,
					DocumentSaveRequest.class);
			// We use this in case there's an error, to pass information about
			// the form
			saveRequestForMailInfo = saveRequest;

			User user = userService.findById(doc.getUserId());
			if (user == null) {
				// Another WTF condition
				throw new RuntimeException("The user #" + doc.getUserId()
						+ " was not on the DB");
			}

			logger.trace("Document '" + fileName + "' successfully parsed");

			Long formId = saveRequest.getFormId();
			if (!authorizationControlService.hasFormLevelAccess(formId, user,
					AuthorizationNames.Form.MOBILE)) {
				String msg = "No authorization";
				logger.error(msg);
				uploadService.changeStatus(doc.getHandle(),
						UploadStatus.REJECTED, msg);
				return;
			}

			AuthorizationAspect.shouldCheckAuthorization(false);
			MFForm mfform = formModelService.getMFForm(saveRequest.getFormId(),
					saveRequest.getVersion(), null);
			if (doc.getBypassUniquessCheck()
					|| doesNotExist(user, doc.getDeviceId(),
							saveRequest.getFormId(), saveRequest.getVersion(),
							doc.getDocumentId())) {
				try {
					logger.trace("Sending the document #" + doc.getId()
							+ " to mongo");

					processSaveRequest(multiplexedFile, user,
							doc.getDeviceId(), doc.getDocumentId(), mfform,
							saveRequest);
					logger.debug("The document #" + doc.getId()
							+ " was successfully saved to mongo");
					uploadService.changeStatusToSaved(doc.getHandle());
					try {
						multiplexedFile.close();
					} catch (IOException e) {
						logger.error("Unable to gracefully close multiplesed file "
								+ doc.getFileName());
					}
					boolean deleted = file.delete();
					if (!deleted) {
						logger.warn("Unable to delete document file. "
								+ file.getAbsolutePath());
					} else {
						logger.trace("Removed temporal file " + fileName);
					}
				} catch (InvalidDocumentException e) {
					String msg = e.getMessage() + "\n"
							+ StringUtils.getStackTraceAsString(e);
					uploadService.changeStatus(doc.getHandle(),
							UploadStatus.REJECTED, msg);
					logger.error("The document #" + doc.getId()
							+ " was rejected. " + e.getMessage());
				}
			} else {
				// if the document already exist we should mark it as rejected
				String msg = "Rejected to avoid duplicate";
				uploadService.changeStatus(doc.getHandle(),
						UploadStatus.REJECTED, msg);
				logger.error("The document #" + doc.getId() + " was rejected. "
						+ msg);
			}
		} catch (InterruptedException e) {
			// Do nothing. We are shutting down.
			String msg = "Work interrupted. It should happen when the server is shutting down";
			logger.warn(msg);
			logger.debug(msg, e);
		} catch (JsonProcessingException e) {
			// If the JSON document has errors, we should mark it as REJECTED
			String msg = "Rejected JSON document: " + e.getMessage();
			logger.error(msg);
			logger.debug(msg, e);
			uploadService.changeStatus(doc.getHandle(), UploadStatus.REJECTED,
					msg);
		} catch (Exception e) {
			// Any exception that happen during the parsing is something not
			// expected. The document should be marked as FAIL
			logger.debug("Unable to read file of document " + doc.getId()
					+ ". FileName  = " + fileName, e);
			logger.error("Unable to read file of document " + doc.getId()
					+ ". FileName  = " + fileName);
			String msg = e.getMessage() + "\n"
					+ StringUtils.getStackTraceAsString(e);
			msg += "\n CAUSE: ";
			msg += StringUtils.getStackTraceAsString(e.getCause());
			if (e.getCause() != null
					&& e.getCause().getCause() instanceof MongoException) {
				// If DuplicateDocumentException is thrown we need its
				// MongoException cause
				msg += "\n";
				msg += e.getCause().getCause();
			}
			DocumentUpload documentUpload = uploadService.changeStatus(
					doc.getHandle(), UploadStatus.FAIL, msg);
			if (documentUpload == null) {
				logger.error("Could not found document with handle: "
						+ doc.getHandle());
			}
			logger.error("Unexpected Exception : " + msg);
			Form formWithError = formService.findById(saveRequestForMailInfo
					.getFormId());
			// cap-28 - Send email to support team, notifying the error.
			NotificationReport notification = NotificationReport
					.documentErrorReport(msg, doc);
			if (saveRequestForMailInfo != null) {
				notification.getData().put("formId",
						saveRequestForMailInfo.getFormId());
				notification.getData().put("formVersion",
						saveRequestForMailInfo.getVersion());
			}
			if (formWithError != null) {
				notification.getData().put("applicationName",
						formWithError.getProject().getApplication().getName());
			}
			notificationManager.notify(notification,
					NotificationType.FAILED_DOCUMENT);
		} finally {
			if (multiplexedFile != null) {
				try {
					multiplexedFile.close();
				} catch (IOException e) {
					logger.error("Unable to gracefully close multiplesed file "
							+ doc.getFileName());
				}
			}
		}

		// 1) parse the file
		// 2) OBtain the document
		// 3) OBtain the inputStreams for the files
		// 4) Send it to mongo
		// 5) Check that it has been stored
		// 6) Mark the handler as SAVED
		// 7) Delete the file

	}

	private boolean doesNotExist(User user, String deviceId, Long formId,
			Long version, String documentId) {

		ConditionalCriteria criteria = new ConditionalCriteria(
				CONDITION_TYPE.AND);
		criteria.add(getCriteria(MFIncomingDataI.META_FIELD_DEVICE_ID, deviceId));
		criteria.add(getCriteria(MFIncomingDataI.META_FIELD_DOCUMENT_ID,
				documentId));
		criteria.add(getCriteria(MFIncomingDataI.META_FIELD_USER_ID,
				user.getId()));
		AuthorizationAspect.shouldCheckAuthorization(false);
		List<MFManagedData> data = formDataService.getFormData(user, formId,
				version, criteria, null);
		return data.isEmpty();
	}

	private void checkRequiredField(DocumentSaveRequest saveRequest,
			MFForm mfform) throws InvalidDocumentException {
		List<PageData> pageData = saveRequest.getPageData();
		for (PageData p : pageData) {
			MFPage mfpage = mfform.getPageById(p.getPageId());
			if (mfpage != null) {
				List<MFElement> elements = mfpage.getElements();
				Map<String, Object> data = p.getData();
				for (MFElement e : elements) {
					String instanceId = e.getInstanceId();
					if (e.isRequired()) {
						if (!data.keySet().contains(instanceId)) {
							throw new InvalidDocumentException("Field "
									+ instanceId + " " + instanceId
									+ " is not present");
						} else {
							Object value = data.get(instanceId);
							if (value == null
									|| (value instanceof String && ((String) value)
											.isEmpty())) {
								throw new InvalidDocumentException("Field "
										+ instanceId + " " + instanceId
										+ " is null or empty");
							}
						}
					}
				}
			} else {
				throw new InvalidDocumentException("Invalid page id:"
						+ p.getPageId() + ", form: " + mfform.getId());
			}
		}
	}

	private static Criteria getCriteria(String field, Object value) {
		Criteria c = new Criteria();
		c.setField(field);
		c.setNamespace(MFStorable.FIELD_META);
		c.setOp(OPERATOR.EQUALS);
		c.setValue(value);
		return c;
	}

	private MFOperationResult processSaveRequest(MFMultiplexedFile multiplexedFile,
			User user, String deviceId, String documentId, MFForm mfform,
			DocumentSaveRequest saveRequest) throws InvalidDocumentException,
			InterruptedException {
		DocumentDTO dto = buildDocumentDTO(user, multiplexedFile, deviceId, documentId, mfform, saveRequest);
		
		AuthorizationAspect.shouldCheckAuthorization(false);
		if (!workflowDataService.shouldSaveInWorkflow(user, dto.getForm())) {
			AuthorizationAspect.shouldCheckAuthorization(false);
			return formDataService.saveData(user,
				dto.getData(), dto.getMeta(), mfform.getId(),
				mfform.getVersion());
		} else {
			// CAP-388
			DocumentDTO wfDto = newWorkflowDocumentDto(user, dto);
			AuthorizationAspect.shouldCheckAuthorization(false);
			return workflowDataService.initIn(user, wfDto).getMfOperationResult();
		}

	}

	private DocumentDTO newWorkflowDocumentDto(User user, DocumentDTO dto) {
		AuthorizationAspect.shouldCheckAuthorization(false);
		StateDTO state = stateService.getInitialFor(user, dto.getForm());
		return DocumentDTO.builder()
				.form(dto.getForm())
				.data(dto.getData())
				.metaData(dto.getMeta())
				.state(state)
				.stateAsMeta(state)
				.build();
	}

	private DocumentDTO buildDocumentDTO(User user, MFMultiplexedFile multiplexedFile, String deviceId, String documentId,
			MFForm mfform, DocumentSaveRequest saveRequest) throws InvalidDocumentException {
		checkRequiredField(saveRequest, mfform);
		Map<String, Object> parsedData = getAllData(saveRequest);
		Map<String, MFField> fieldsMap = fieldsMap(mfform);
		Map<String, Object> translatedDataEntry = translateDataTypes(
				multiplexedFile, parsedData, fieldsMap);
		Map<String, Object> metaParams = null;
		if (saveRequest.getLocation() != null) {
			metaParams = new HashMap<String, Object>();
			MFLocationData data = MFDataHelper.unserializeLocation(saveRequest
					.getLocation());
			metaParams.put(MFIncomingDataI.META_FIELD_LOCATION, data);
		}

		if (deviceId != null && !deviceId.trim().isEmpty()
				&& documentId != null && !documentId.trim().isEmpty()) {
			if (metaParams == null) {
				metaParams = new HashMap<String, Object>();
			}
			metaParams.put(MFIncomingDataI.META_FIELD_DEVICE_ID, deviceId);
			metaParams.put(MFIncomingDataI.META_FIELD_DOCUMENT_ID, documentId);
		}
		
		// CAP-152
		if (saveRequest.getSavedAt() != null) {
			if (metaParams == null) {
				metaParams = new HashMap<String, Object>();
			}
			Date savedDate = MFDataHelper.unserializeDate(saveRequest.getSavedAt(), TemporalUtils.DATE_TIME_FORMAT);
			metaParams.put(MFIncomingDataI.META_FIELD_SAVED_AT, savedDate);
		}
		
		// CAP-388 Add state info
		FormDTO formDto = buildFormDTO(mfform);
		
		
		return DocumentDTO.builder()
				.form(formDto)
				.data(translatedDataEntry)
				.metaData(metaParams)
				.build();
	}

	

	private FormDTO buildFormDTO(MFForm mfform) {
		FormDTO formDTO = new FormDTO();
		formDTO.setId(mfform.getId());
		formDTO.setVersion(mfform.getVersion());
		return formDTO;
	}


	/***
	 * Transform a map of <String,String> into a map of <String,Object>. The
	 * destination object is based on the {@link MFField#getType()}
	 *
	 * @param parsedData
	 * @param fieldNameToField
	 * @return
	 */
	private Map<String, Object> translateDataTypes(
			MFMultiplexedFile multiplexedFile, Map<String, Object> parsedData,
			Map<String, MFField> fieldNameToField) {
		
		HashMap<String, Object> m = new HashMap<String, Object>();
		Set<String> keys = fieldNameToField.keySet();
		for (String fieldKey : keys) {
			MFField fieldDef = fieldNameToField.get(fieldKey);
			String serializedValue = (String) parsedData.get(fieldKey);
			if (serializedValue == null) {
				if (parsedData.keySet().contains(fieldKey)) {
					throw new IllegalArgumentException(
							"The value for the field "
									+ fieldKey
									+ " is null. This might be a wrong assignment. If you really want to assign a null value, please remove the key from the map and null will be assumed");
				} else {
					continue;
				}
			}

			if (fieldDef.getType().equals(FIELD_TYPE.BLOB)) {
				String fileName = serializedValue;
				InputStream inputFile = multiplexedFile.openFile(fileName,
						readBufferSize);
				MFBlob blob = new MFBlob(fileName, inputFile);
				blob.setContentType(multiplexedFile.getContentType(fileName));
				m.put(fieldKey, blob);
			} else {
				Object value = MFDataHelper.unserialize(fieldDef.getType(),
						serializedValue);

				if (value == null) {
					throw new IllegalArgumentException(
							"Couldn't unserialize the value \""
									+ serializedValue + " \" to "
									+ fieldDef.getType());
				}
				m.put(fieldKey, value);
			}
		}

		return m;
	}

	private static Map<String, Object> getAllData(
			DocumentSaveRequest saveRequest) {
		Map<String, Object> data = new HashMap<String, Object>();
		List<PageData> pageData = saveRequest.getPageData();
		for (PageData p : pageData) {
			data.putAll(p.getData());
		}
		return data;
	}

	private Map<String, MFField> fieldsMap(MFForm mfform) {
		Map<String, MFField> map = new HashMap<String, MFField>();
		AuthorizationAspect.shouldCheckAuthorization(false);
		List<MFField> fields = formService.listFields(mfform);
		for (MFField f : fields) {
			map.put(f.getColumnName(), f);
		}
		return map;
	}
}
