package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.dtos.SystemParameterDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.SystemParameter;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters.PARAMETER_TYPE;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.utils.BeanUtils;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.jqgrid.ColModel;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class SystemParameterCrudController extends CrudController {

	@Autowired
	private IParametersService parameterService;

	@Override
	@RequestMapping("/admin/systemParameters/columninfo.ajax")
	protected @ResponseBody
	JsonResponse<?> columnInfo(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		Map<String, Object> content = new HashMap<String, Object>();
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(true);
		response.setContent(content);

		String[] cols = { SystemParameter.ID, SystemParameter.DESCRIPTION, SystemParameter.LABEL, SystemParameter.TYPE,
				SystemParameter.VALUE };
		String[] colNames = { i18n.getMessage("admin.cruds.parameter.cols.id"),
				i18n.getMessage("admin.cruds.parameter.cols.description"),
				i18n.getMessage("admin.cruds.parameter.cols.label"),
				i18n.getMessage("admin.cruds.parameter.cols.type"), i18n.getMessage("admin.cruds.parameter.cols.value") };

		content.put("cols", cols);
		content.put("colNames", colNames);

		List<ColModel> colModel = new ArrayList<ColModel>();
		colModel.add(new ColModel().name(cols[0]).index(cols[0]).width("50").editable(false));
		colModel.add(new ColModel().name(cols[1]).index(cols[1]).editable(true).required(true).width("150"));
		colModel.add(new ColModel().name(cols[2]).index(cols[2]).editable(true).width("180"));

		ColModel levelCol = new ColModel().name(cols[3]).index(cols[3]).edittype("select").width("100");
		String levels = allLevels(i18n);
		levelCol.addEditoption("value", levels);
		colModel.add(levelCol);

		
		colModel.add(new ColModel().name(cols[4]).index(cols[4]).width("70").align("center").formatter("custom"));

		content.put("colModel", colModel);

		content.put("sortorder", "asc");
		content.put("sortname", SystemParameter.ID);
		content.put("addCaption", i18n.getMessage("admin.cruds.parameter.form.addCaption"));
		content.put("editCaption", i18n.getMessage("admin.cruds.parameter.form.editCaption"));
		return response;
	}

	private String allLevels(I18nManager i18n) {
		StringBuilder sb = new StringBuilder();

		sb.append(PARAMETER_TYPE.STRING);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.parameter.type.string"));
		sb.append(';');

		sb.append(PARAMETER_TYPE.BOOLEAN);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.parameter.type.boolean"));
		sb.append(';');

		sb.append(PARAMETER_TYPE.LONG);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.parameter.type.long"));
		sb.append(';');

		sb.append(PARAMETER_TYPE.LIST);
		sb.append(':');
		sb.append(i18n.getMessage("admin.cruds.parameter.type.list"));

		return sb.toString();
	}

	@Override
	@RequestMapping("/admin/systemParameters/paging/read.ajax")
	public @ResponseBody
	PagedData<List<Map<String, Object>>> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1")
			Integer page, @RequestParam(value = "rows", required = false, defaultValue = "10")
			Integer rows, @RequestParam(value = "sidx", defaultValue=SystemParameter.DESCRIPTION, required = false)
			String orderBy, @RequestParam(value = "sord", required = false)
			String order, @RequestParam(value = "_search", required = false, defaultValue = "")
			String _search, @RequestParam(value = "filters", required = false)
			String filters, @RequestParam(value = "searchOper", required = false)
			String searchOper, @RequestParam(value = "searchField", required = false)
			String searchField, @RequestParam(value = "searchString", required = false)
			String searchString) {

		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		boolean ascending = true;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}

		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = SystemParameter.DESCRIPTION;
		}

		boolean search = _search.equals("true");
		PagedData<List<SystemParameter>> serviceData = null;
		if (!search) {
			serviceData = parameterService.findAll(orderBy, ascending, page, rows);
		} else {
			if (searchField.equals("id")) {
				Long val = Long.parseLong(searchString);
				serviceData = parameterService.findByProperty(searchField, searchOper, val, orderBy, ascending, page,
						rows);
			} else {
				serviceData = parameterService.findByProperty(searchField, searchOper, searchString, orderBy,
						ascending, page, rows);
			}
		}
		PagedData<List<Map<String, Object>>> controllerData = new PagedData<List<Map<String, Object>>>();

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

		for (SystemParameter r : serviceData.getData()) {
			Map<String, Object> entry = new HashMap<String, Object>();
			entry.put(SystemParameter.ID, r.getId());
			entry.put(SystemParameter.DESCRIPTION, r.getDescription());
			entry.put(SystemParameter.LABEL, r.getLabel());
			entry.put(SystemParameter.TYPE, r.getType());
			entry.put(SystemParameter.VALUE, r.getValue());
			data.add(entry);
		}
		BeanUtils.mapBean(serviceData, controllerData);
		controllerData.setData(data);

		return controllerData;
	}

	@Override
	@RequestMapping("/admin/systemParameters/paging/edit.ajax")
	public @ResponseBody
	JsonResponse<String> edit(HttpServletRequest request, @RequestParam(value = "oper")
	String oper, @RequestParam(value = "id", required = false)
	String id) {
		Map<String, String> params = getParametersMap(request);
		Boolean active = Boolean.parseBoolean(params.get(SystemParameter.ACTIVE));
		return edit(request, oper, id, params.get(SystemParameter.DESCRIPTION), params.get(SystemParameter.LABEL),
				params.get(SystemParameter.TYPE), params.get(SystemParameter.VALUE), active);
	}

	private JsonResponse<String> edit(HttpServletRequest request, String oper, String id, String description,
			String label, String type, String value, Boolean active) {

		User currentUser = getUser(request);
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = mgr.getApplication();

		JsonResponse<String> response = new JsonResponse<String>();
		SystemParameterDTO dto = getSystemParameterDTO(id, description, label, type, value, active);

		try {
			if (!oper.equals("del")) {
				// TODO Validation...
				if (oper.equals("add")) {
					//we make sure is an insert
//					dto.setId(null);
//					parameterService.createSystemParameter(currentUser, dto);
					throw new RuntimeException("NOT SUPPORTED. SYSTEM PARAMETERS CANNOT BE ADDED");
				} else if (oper.equals("edit")) {
					parameterService.editSystemParameter(currentUser, dto);
				}
				response.setSuccess(true);
				response.setTitle(i18n.getMessage("admin.cruds.parameter.saved.title"));
				response.setMessage(i18n.getMessage("admin.cruds.parameter.saved.message", dto.getDescription()));
			} else {
				throw new RuntimeException("NOT SUPPORTED. SYSTEM PARAMETERS CANNOT BE DELETED");
//				SystemParameter parameter = parameterService.findById(dto.getId());
//				parameterService.logicalDelete(parameter);
//				response.setTitle(i18n.getMessage("admin.cruds.parameter.deleted.title"));
//				response.setMessage(i18n.getMessage("admin.cruds.parameter.deleted.message", parameter.getDescription()));
//				response.setSuccess(true);
			}
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		}

		return response;
	}

	private SystemParameterDTO getSystemParameterDTO(String id, String description, String label, String type,
			String value, Boolean active) {
		SystemParameterDTO dto = new SystemParameterDTO();
		if (id != null && !id.trim().isEmpty() && !id.equals("_empty")) {
			dto.setId(Long.parseLong(id));
		}
		dto.setDescription(description);
		dto.setLabel(label);
		if(type != null)
			dto.setType(PARAMETER_TYPE.valueOf(type));
		dto.setValue(value);
		dto.setActive(active);
		return dto;
	}

	@RequestMapping(value = "/systems/parameters/get.ajax", method = RequestMethod.POST)
	public @ResponseBody
	SystemParameterDTO getSystemParameter(HttpServletRequest request, @RequestParam(value = "parameterId")
	Long parameterId) {
		return parameterService.getSystemParameter(parameterId);
	}

}
