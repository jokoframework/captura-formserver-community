package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.MFManagedDataBasic;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult.RESULT;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableModificationRequest;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableModificationRequest.OPERATION_TYPE;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransportMultiple;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@Api(value = "lookuptables", description = "Operations related to Lookup Tables", position = 3)
public class LookupTableEndpoint extends EndpointController {

	// This value will be overwritten by the system parameter
	// SYS_SYNCHRONIZATION_ROWSPERITERATION
	private static final int DEFAULT_SYS_SYNCHRONIZATION_ROWSPERITERATION = 100;

	private static final Logger logger = LoggerFactory.getLogger(LookupTableEndpoint.class);

	@Autowired
	private ILookupTableService lookupService;

	@Autowired
	private ISystemParametersBundle systemParams;
	
	@Autowired
	private SynchronizationService syncService;

	@ApiOperation(value = "Returns the definition of the Lookup Table", response = MFLoookupTableDefinition.class, position = 1, notes = "This operation returns the definition of the Lookup Table if it exists")
	@RequestMapping(value = "/lookupTable/definition/{lookupTableId}", method = RequestMethod.GET)
	public void getLookupTableById(
			HttpServletRequest request,
			HttpServletResponse response,
			@ApiParam(name = "lookupTableId", value = "ID of the Lookup Table definition to be returned", required = true) @PathVariable("lookupTableId") String lookupTableIdStr)
			throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();

		Long lookupTableId = convertToLongOrSendError(response, "lookupTableId", lookupTableIdStr);
		try {
			if (lookupTableId != null) {
				MFLoookupTableDefinition lookup = lookupService.getLookupTableDefinition(lookupTableId);

				if (lookup != null) {
					boolean hasAuthorization = authControlService.hasAppLevelAccess(
							lookup.getInfo().getApplicationId(), user,
							AuthorizationNames.App.REST_API_LOOKUPTABLES_READ);
					if (hasAuthorization) {
						sendObject(response, lookup);
					} else {
						sendMsg(response, "Doesn't have enough authorizations. Check authorization '"
								+ AuthorizationNames.App.REST_API_LOOKUPTABLES_READ + "'",
								HttpServletResponse.SC_UNAUTHORIZED);
					}
				} else {
					sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
				}
			}

		} catch (AuthorizationException e) {
			sendMsg(response, "Doesn't have enough authorizations. Check authorization '"
					+ AuthorizationNames.App.REST_API_LOOKUPTABLES_READ + "'", HttpServletResponse.SC_FORBIDDEN);

		}

	}

	@ApiOperation(value = "Lookup Table definitions in the application", response = MFLoookupTableDefinition.class, position = 2, notes = "This operation returns the Lookup Table "
			+ "definitions in the application with the given identifier or all Lookup Tables if no identifier is provided")
	@RequestMapping(value = "/lookupTable/definition/listAll", method = RequestMethod.GET)
	public void listAll(
			HttpServletRequest request,
			HttpServletResponse response,
			@ApiParam(name = "appId", value = "ID of the application", required = true) @RequestParam(value = "appId", required = false) String appIdStr,
			@ApiParam(name = "identifier", value = "The identifier of the lookup table", required = false) @RequestParam(value = "identifier", required = false) String identifier)
			throws IOException {

		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();

		Long appId = null;
		if (appIdStr != null) {
			appId = convertToLongOrSendError(response, "appId", appIdStr);
		} else {
			appId = mgr.getApplication().getId();
		}

		try {

			if (appId != null) {
				Application app = checkAppAccessOrSendError(response, user, appId,
						AuthorizationNames.App.REST_API_LOOKUPTABLES_LIST);

				if (app != null) {
					// reaching this point means that we have a valid
					// application
					// and
					// the user has the required authorization
					List<LookupTableDTO> lookupTables = lookupService.listAvailableLookupTables(app, identifier);
					sendObject(response, lookupTables);
				}

			}
		} catch (AuthorizationException e) {
			sendMsg(response, "Doesn't have enough authorizations. Check authorization '"
					+ AuthorizationNames.App.REST_API_LOOKUPTABLES_LIST + "'", HttpServletResponse.SC_FORBIDDEN);

		}

	}

	@ApiOperation(value = "Operation to create a new Lookup Table Definition", response = MFLoookupTableDefinition.class, position = 3, notes = "This operation creates a new definition of a Lookup Table")
	@RequestMapping(value = "/lookupTable/definition", method = RequestMethod.POST)
	@ApiImplicitParam(value = "An object of type MFLoookupTableDefinition that will be redefined", paramType = "body", required = true, dataType = "MFLoookupTableDefinition")
	public void createLookupTable(HttpServletRequest request, HttpServletResponse response) throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();

		MFLoookupTableDefinition lookupDef = parseOrSendError(response, MFLoookupTableDefinition.class,
				request.getInputStream());

		try {
			if (lookupDef != null && lookupDef.getInfo() != null) {
				LookupTableDTO info = lookupDef.getInfo();
				if (info != null) {
					if (info.getApplicationId() != null) {
						Application app = checkAppAccessOrSendError(response, user, info.getApplicationId(),
								AuthorizationNames.App.REST_API_LOOKUPTABLES_CREATE);
						if (app != null) {
							MFLoookupTableDefinition def = lookupService.createLookupTable(app, user, lookupDef);
							sendObject(response, def, HttpServletResponse.SC_CREATED);
						}
					} else {

						sendError(response, HttpServletResponse.SC_BAD_REQUEST,
								"The application field can't be null. Check field info.applicationId");
					}

				} else {
					sendMsg(response, "MFLoookupTableDefinition doesn't have info field",
							HttpServletResponse.SC_BAD_REQUEST);
				}
			}
		} catch (AuthorizationException e) {
			sendMsg(response, "Doesn't have enough authorizations. Check authorization '"
					+ AuthorizationNames.App.REST_API_LOOKUPTABLES_CREATE + "'", HttpServletResponse.SC_FORBIDDEN);
		} catch (LookupTableDefinitionException e) {
			sendError(response, HttpServletResponse.SC_CONFLICT, e.getErrorCode(), e.getMessage());
		}
	}

	@ApiOperation(value = "DML operation over the lookup table. Add data to the lookup table", response = MFOperationResult.class, position = 4)
	@RequestMapping(value = "/lookupTable/data/{lookupTableId}", method = RequestMethod.POST)
	@ApiImplicitParam(value = "A List of json objects. Every json should have as keys the name of the fields of the lookup table", required = true, name = "data", paramType = "body", dataType = "List<Map>")
	public void insertData(
			HttpServletRequest request,
			HttpServletResponse response,
			@ApiParam(name = "lookupTableId", value = "ID of the Lookup Table were data is going to be inserted", required = true) @PathVariable("lookupTableId") String lookupTableIdStr)
			throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		Long lookupTableId = convertToLongOrSendError(response, "lookupTableId", lookupTableIdStr);
		if (lookupTableId != null) {
			MFLoookupTableDefinition def = lookupService.getLookupTableDefinition(lookupTableId);
			if (def == null) {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Lookup table Not found");
				return;
			}
			Application app = appService.findById(def.getInfo().getApplicationId());
			// the app can't be null since it is obtained from a stored lookup
			// table
			boolean hasAuthorization = authControlService.has(app, user,
					AuthorizationNames.App.REST_API_LOOKUPTABLES_INSERT);
			if (!hasAuthorization) {
				sendMsg(response, "You don't have access to insert data on the lookup table",
						HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			try {

				List<HashMap<String, String>> rows = objectMapper.readValue(request.getInputStream(),
						new TypeReference<List<HashMap<String, String>>>() {
						});
				ArrayList<MFIncomingDataBasic> rowsToInsert = new ArrayList<MFIncomingDataBasic>();
				for (int i = 0; i < rows.size(); i++) {
					HashMap<String, String> r = rows.get(i);
					Map<String, Object> row = MFDataHelper.unserializeValues(def, r);
					rowsToInsert.add(new MFIncomingDataBasic(i, row));
				}
				MFOperationResult insertSummary = lookupService.insertData(app, lookupTableId, rowsToInsert, true,
						false);
				if (insertSummary.hasSucceeded()) {
					sendObject(response, insertSummary, HttpServletResponse.SC_CREATED);
				} else {
					sendObject(response, insertSummary, HttpServletResponse.SC_CONFLICT);
				}

			} catch (JsonParseException e) {
				sendMsg(response, "Unable to parse incoming request", HttpServletResponse.SC_BAD_REQUEST);
			} catch (JsonMappingException e) {
				sendMsg(response, "Unable to parse incoming request", HttpServletResponse.SC_BAD_REQUEST);
			} catch (InterruptedException e) {
				// do nothing, probably we are shutting down
			}
		}
	}

	// FIXME this method shouldn't be here
	/**
	 * This is a wrapper over
	 * {@link #listData(HttpServletRequest, HttpServletResponse, String, String, String, String, String)}
	 * that support the parameters sent by
	 * 
	 * @param request
	 * @param page
	 * @param rows
	 * @param orderBy
	 * @param order
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	@RequestMapping(value = "/lookupTable/data/read.ajax", method = RequestMethod.GET)
	@ApiIgnore
	public void listData(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "lookupTableId", required = true) Long loookupTableId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) throws IOException {

		Boolean sorting = new Boolean(order.equals("asc"));
		listData(request, response, loookupTableId.toString(), page.toString(), rows.toString(), orderBy,
				sorting.toString());
	}

	@ApiOperation(value = "Get data from the Lookup Table", response = PagedData.class, position = 5)
	@RequestMapping(value = "/lookupTable/data/{lookupTableId}", method = RequestMethod.GET)
	public void listData(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("lookupTableId") String lookupTableIdStr,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "1") String pageNumberStr,
			@RequestParam(value = "pageSize", required = false, defaultValue = "500") String limitStr,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "asc", required = false) String ascStr) throws IOException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		Long lookupTableId = convertToLongOrSendError(response, "lookupTableId", lookupTableIdStr);
		Integer pageNumber = convertToIntOrSendError(response, "pageNumber", pageNumberStr);
		Integer limit = convertToIntOrSendError(response, "limit", limitStr);

		boolean asc = true;
		if (ascStr != null) {
			try {
				asc = Boolean.parseBoolean(ascStr);
			} catch (IllegalArgumentException e) {
				sendError(response, HttpServletResponse.SC_BAD_REQUEST, "asc parmeter must be either true or false");
				return;
			}
		}
		if (pageNumber == null || limit == null) {
			// this can only happens if the user submitted some garbage on this
			// fields
			return;
		}
		// This is a constraint to avoid clients retrieving too many data
		Integer maxRows = systemParams.getIntValue(DBParameters.REST_LOOKUP_DATA_MAXROWS);
		if (limit > maxRows) {
			limit = maxRows;
		}
		if (lookupTableId != null) {
			MFLoookupTableDefinition def = lookupService.getLookupTableDefinition(lookupTableId);
			if (def == null) {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Lookup table Not found");
				return;
			}
			Application app = appService.findById(def.getInfo().getApplicationId());
			boolean hasAuthorization = authControlService.has(app, user,
					AuthorizationNames.App.REST_API_LOOKUPTABLES_READ);
			if (!hasAuthorization) {
				sendMsg(response, "You don't have access to read lookup tables", HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			Map<String, MFField> fieldsMap = def.fieldsMappedByName();
			// FIXME I (Miguel) temporarily commented because 
			// the data in the lookup tables was not visible
			// after commenting this code it worked.
			// Need to check this constraint. Why is it needed? Why is it failing?

			// At this point we are sure that is a request that should be
			// processed
			ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);

			Map<String, String[]> parameters = request.getParameterMap();
			Set<String> keys = parameters.keySet();

			for (String param : keys) {
				if (!param.equals("offset") && !param.equals("limit")) {
					String[] value = parameters.get(param);
					MFField field = fieldsMap.get(param);
					if (field != null) {
						try {
							Object val = py.com.sodep.mf.exchange.MFDataHelper.unserialize(field.getType(), value[0]);
							criteria.add(new Criteria(param, OPERATOR.EQUALS, val));
						} catch (IllegalArgumentException e) {
							sendMsg(response, "Wrong parameter value for attribute " + field.getColumnName()
									+ ". Expected data is " + field.getType(), HttpServletResponse.SC_BAD_REQUEST);
							return;
						}
					}

				}
			}

			PagedData<List<MFManagedData>> serviceData = lookupService.listData(app, lookupTableId, criteria,
					pageNumber, limit, orderBy, asc);

			// We need this transformation in order to just send the user
			// data. Otherwise, we will be sending lot of internal
			// information
			List<MFManagedData> rows = serviceData.getData();
			BeanToPropertyValueTransformer dataTransformer = new BeanToPropertyValueTransformer("userData");
			
			//CAP-147 - For edit
			Map<Integer, Long> mapPosToId = new HashMap<Integer, Long>();  
			Integer rowPos = 0;
			for (MFManagedData row : rows) {
				rowPos++;
				Long rowId = row.getRowId();
				mapPosToId.put(rowPos, rowId);
			}
			mgr.setPositionRowsMap(mapPosToId);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>(CollectionUtils.collect(rows,
					dataTransformer));
			ArrayList<Map<String, String>> dataConvertedList = new ArrayList<Map<String, String>>();
			for (Map<String, Object> originalRow : dataList) {
				Map<String, String> convertedRow = MFDataHelper.serializeValues(def, originalRow);
				dataConvertedList.add(convertedRow);
			}
			py.com.sodep.mf.exchange.PagedData<List<Map<String, String>>> restPageData = new py.com.sodep.mf.exchange.PagedData<List<Map<String, String>>>(
					dataConvertedList, serviceData.getTotalCount(), serviceData.getPageNumber(),
					serviceData.getPageSize(), serviceData.getAvailable());

			sendObject(response, restPageData);

		}

	}

	@ApiOperation(value = "Modify the data in the Lookup Table (update or delete data)", response = MFOperationResult.class, position = 6, notes = "Update operation must only affect one row, if they affect more rows the update will be rejected")
	@RequestMapping(value = "/lookupTable/data/{lookupTableId}", method = RequestMethod.PUT)
	@ApiImplicitParam(value = "The json that specifies how to perform the update or delete", required = true, paramType = "body", dataType = "LookupTableModificationRequest")
	public void modifyLookupTable(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("lookupTableId") String lookupTableIdStr) throws IOException, InterruptedException {
		SessionManager mgr = new SessionManager(request);
		User user = mgr.getUser();
		Long lookupTableId = convertToLongOrSendError(response, "lookupTableId", lookupTableIdStr);
		if (lookupTableId != null) {
			MFLoookupTableDefinition def = lookupService.getLookupTableDefinition(lookupTableId);
			if (def == null) {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Lookup table Not found");
				return;
			}
			Application app = appService.findById(def.getInfo().getApplicationId());
			boolean hasAuthorization = authControlService.has(app, user,
					AuthorizationNames.App.REST_API_LOOKUPTABLES_MODIFY);
			if (!hasAuthorization) {
				sendMsg(response, "You don't have access to modify the lookup tables", HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			try {
				LookupTableModificationRequest modificationRequest = objectMapper.readValue(request.getInputStream(),
						LookupTableModificationRequest.class);

				MFOperationResult result;
				if (modificationRequest.getOperationType().equals(OPERATION_TYPE.UPDATE)) {
					logger.trace("Processing update for lookupTable #" + lookupTableId);
					Map<String, Object> row = MFDataHelper.unserializeValues(def, modificationRequest.getNewData());
					MFIncomingDataBasic newData = new MFIncomingDataBasic(0, row);
					result = lookupService.updateOrInsertData(app, lookupTableId, newData, true);
				} else if (modificationRequest.getOperationType().equals(OPERATION_TYPE.DELETE)) {
					logger.trace("Processing delete for lookupTable #" + lookupTableId);
					result = lookupService.deleteData(app, lookupTableId, modificationRequest.getSelector());
				} else {
					sendMsg(response, "Unable to parse incoming request", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
					return;
				}
				if (result.getResult().equals(RESULT.SUCCESS)) {
					sendObject(response, result);
				} else {
					sendObject(response, result, HttpServletResponse.SC_CONFLICT);
				}

			} catch (JsonParseException e) {
				logger.debug("Unable to parse incoming request", e);
				sendMsg(response, "Unable to parse incoming request", HttpServletResponse.SC_BAD_REQUEST);
			} catch (JsonMappingException e) {
				logger.debug("Unable to parse incoming request", e);
				sendMsg(response, "Unable to parse incoming request", HttpServletResponse.SC_BAD_REQUEST);
			}

		}
	}

	@ApiOperation(value = "Get the data in the given columns", response = MFOperationResult.class, position = 7)
	@RequestMapping(value = "/lookupTable/columns/data/{lookupTableId}", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse<List<Object[]>> columnData(HttpServletRequest request,
			@PathVariable("lookupTableId") Long lookupTableId, @RequestParam("columns") String columns[]) {
		SessionManager sessionManager = new SessionManager(request);
		Application app = sessionManager.getApplication();

		JsonResponse<List<Object[]>> response = new JsonResponse<List<Object[]>>();
		List<Object[]> list = new ArrayList<Object[]>();
		response.setObj(list);

		try {
			List<MFManagedData> rows = lookupService.listAllData(app, lookupTableId);
			for (MFManagedData row : rows) {
				Map<String, Object> userData = row.getUserData();
				Object[] data = new Object[columns.length];
				int i = 0;
				for (String column : columns) {
					Object object = userData.get(column);
					data[i] = object;
					i++;
				}
				list.add(data);
			}
			response.setSuccess(true);
		} catch (IllegalArgumentException e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return response;

	}

	@ApiOperation(value = "Get data from the Lookup Table" ,response = MFDMLTransportMultiple.class, position = 8)
	@RequestMapping(value = "/lookupTable/lookupTableDataFast", method = RequestMethod.POST)
	public void getDataFast(HttpServletRequest request, HttpServletResponse response, @RequestBody TXInfo txInfo) {
		Integer maxNumberOfData = systemParams.getIntValue(DBParameters.SYS_SYNCHRONIZATION_ROWSPERITERATION);
		if (maxNumberOfData == null) {
			maxNumberOfData = DEFAULT_SYS_SYNCHRONIZATION_ROWSPERITERATION;
		}
		MFLoookupTableDefinition ddl = lookupService.getLookupTableDefinition(txInfo.getLookupTable());
		MFDMLTransportMultiple multiple = syncService.downloadDataMultiple(txInfo.getLookupTable(), ddl,
				txInfo.getTx(), txInfo.getEndRow(), maxNumberOfData);

		if (logger.isDebugEnabled()) {
			long numberOfTransactions = 0;
			if (multiple.getListOfTransports() != null) {
				numberOfTransactions = multiple.getListOfTransports().size();
			}
			logger.debug("Downloading multiple transactions for lookup #" + txInfo.getLookupTable()
					+ ". Number of transactions = " + numberOfTransactions);
		}
		// FIX for #2732
		// Is this the right place?
		List<MFDMLTransport> dmlTransports = multiple.getListOfTransports();
		for (MFDMLTransport dmlTransport : dmlTransports) {
			serializeValues(dmlTransport);
		}
		// ---- //
		writeObject(response, multiple);
	}



	// FIX for #2732
	private void serializeValues(MFDMLTransport dmlTransport) {
		List<MFManagedDataBasic> list = dmlTransport.getData();
		if (list != null) {
			for (MFManagedDataBasic d : list) {
				Map<String, Object> userData = d.getUserData();
				Set<String> keySet = userData.keySet();
				for (String key : keySet) {
					Object val = userData.get(key);
					String serialized = MFDataHelper.serialize(val);
					userData.put(key, serialized);
				}
			}
		}
	}
	
	// FIXME this method shouldn't be here (probably)
	@RequestMapping(value = "/lookupTable/data/{lookupTableId}/edit.ajax", method = RequestMethod.POST)
	public void editData(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("lookupTableId") Long lookupTableId) throws IOException {
		SessionManager mgr = new SessionManager(request);
		MFLoookupTableDefinition def = lookupService.getLookupTableDefinition(lookupTableId);
		Application app = appService.findById(def.getInfo().getApplicationId());
		Map<String, MFField> fieldsMap = def.fieldsMappedByName();
		ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);

		Map<String, String[]> parameters = request.getParameterMap();
		Set<String> keys = parameters.keySet();
		Map<String, Object> row = new HashMap<String, Object>();
		String oper = "";
		for (String param : keys) {
			if (!param.equals("oper") && !param.equals("gridId")) {
				String[] value = parameters.get(param);
				MFField field = fieldsMap.get(param);
				if (field != null) {
					try {
						Object val = py.com.sodep.mf.exchange.MFDataHelper.unserialize(field.getType(), value[0]);
						row.put(param, val);
					} catch (IllegalArgumentException e) {
						sendMsg(response, "Wrong parameter value for attribute " + field.getColumnName()
								+ ". Expected data is " + field.getType(), HttpServletResponse.SC_BAD_REQUEST);
						return;
					}
				}

			} else if (param.equals("oper")) {
				oper = parameters.get(param)[0];
			} else if (param.equals("gridId")) {
				Map<Integer, Long> mapPosToId = mgr.getPositionRowsMap();
				Integer rowPos = Integer.valueOf(parameters.get(param)[0]);
				Long rowId = mapPosToId.get(rowPos);
				criteria.add(new Criteria(MFStorable._ID, OPERATOR.EQUALS, rowId));
			}
		}
		MFIncomingDataBasic newData = new MFIncomingDataBasic(0, row);
		
		// Call the appropriate method (del, edit or add)
		MFOperationResult operationResult; 
		if (oper.equals("edit")) {
			operationResult = lookupService.updateData(app, lookupTableId, newData, criteria);
		} else if (oper.equals("add")) {
			List<MFIncomingDataBasic> newDataList = new ArrayList<MFIncomingDataBasic>();
			newDataList.add(newData);
			try {
				operationResult = lookupService.insertData(app, lookupTableId, newDataList, true);
			} catch (InterruptedException e) {
				// do nothing, probably we are shutting down
			}
		} else if (oper.equals("del")) {
			operationResult = lookupService.deleteData(app, lookupTableId, criteria);
		}
	}
	
}
