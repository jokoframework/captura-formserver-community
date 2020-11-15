package py.com.sodep.mobileforms.web.controllers.lookuptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.jqgrid.ColModel;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class LookupTableDataController extends SodepController {
	
	private static Logger logger = LoggerFactory.getLogger(LookupTableDataController.class);

	@Autowired
	private ILookupTableService lookupTableService;

	
	@RequestMapping(value = "/lookuptable/data/read.ajax")
	public @ResponseBody
	PagedData<List<Map<String, Object>>> data(HttpServletRequest request,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "lutId") Long lutId,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false) String _search,
			@RequestParam(value = "filters", required = false) String filters,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString) {

		SessionManager manager = new SessionManager(request);

		I18nManager i18n = manager.getI18nManager();
		if (language == null) {
			language = i18n.getSelectedLanguage();
		}

		ConditionalCriteria restriction = null;
		PagedData<List<MFManagedData>> serviceData = lookupTableService.listData(manager.getApplication(),lutId, restriction, page, rows,
				orderBy, order.equals("asc") ? true : false);

		PagedData<List<Map<String, Object>>> controllerData = new PagedData<List<Map<String, Object>>>(null,
				serviceData.getTotalCount(), serviceData.getPageNumber(), serviceData.getPageSize(),
				serviceData.getAvailable());

		List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
		controllerData.setData(entries);
		// TODO Get fields
		List<MFField> fieldInfo = lookupTableService.listFields(lutId, language);
		// formFieldService.listFields(formId, language);
		int size = fieldInfo.size();
		for (MFManagedData row : serviceData.getData()) {
			Map<String, ?> data = row.getUserData();
			Map<String, Object> entry = new HashMap<String, Object>();

			Object[] cell = new Object[size];
			for (int i = 0; i < size; i++) {
				MFField fi = fieldInfo.get(i);
				cell[i] = data.get(fi.getColumnName());
			}
			entry.put("cell", cell);
			entries.add(entry);
		}

		return controllerData;

	}

	@RequestMapping("/lookuptable/data/columninfo.ajax")
	protected @ResponseBody
	JsonResponse<?> columnInfo(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		String language = i18n.getSelectedLanguage();
		// User user = mgr.getUser();

		String lutIdParam = request.getParameter("lutId");

		if (StringUtils.isEmpty(lutIdParam)) {
			String msg = "web.data.lookup_table.validation.emptyLutParam";
			return createFailureResponse(i18n, msg);
		}
		Long lutId = null;
		try {
			lutId = Long.parseLong(lutIdParam);
		} catch (NumberFormatException e) {
			String msg = "web.data.lookup_table.validation.invalidLutId";
			return createFailureResponse(i18n, msg);
		}

		String caption = i18n.getMessage("web.generic.lookup_table");
		Map<String, Object> content = new HashMap<String, Object>();
		JsonResponse<Object> response = new JsonResponse<Object>();

		List<MFField> fieldInfo = lookupTableService.listFields(lutId, language);

		if (fieldInfo.size() != 0) {
			int size = fieldInfo.size();
			String[] cols = new String[size];
			String[] colNames = new String[size];
			ColModel[] colModel = new ColModel[size];
			for (int i = 0; i < fieldInfo.size(); i++) {
				MFField fi = fieldInfo.get(i);
				cols[i] = fi.getColumnName();
				colNames[i] = fi.getColumnName();
				colModel[i] = new ColModel().name(cols[i]).index(cols[i]);

			}
			content.put("cols", cols);
			content.put("colNames", colNames);
			content.put("colModel", colModel);
			content.put("caption", caption);
			content.put("sortorder", "asc");
			content.put("sortname", cols[0]);
			response.setSuccess(true);
			response.setContent(content);
		} else {
			response.setSuccess(false);
			response.setMessage(i18n.getMessage("web.data.lookup_table.empty.message", caption));
		}
		return response;
	}

	private JsonResponse<?> createFailureResponse(I18nManager i18n, String msg) {
		JsonResponse<Object> response = new JsonResponse<Object>();
		response.setSuccess(false);
		response.setMessage(i18n.getMessage(msg));
		return response;
	}
}
