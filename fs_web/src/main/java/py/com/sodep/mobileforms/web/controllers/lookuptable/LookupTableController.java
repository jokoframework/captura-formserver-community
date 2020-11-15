package py.com.sodep.mobileforms.web.controllers.lookuptable;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.LookuptableOperationException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.utils.StringUtils;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class LookupTableController extends SodepController {

	@Autowired
	private ILookupTableService lookupService;

	@Autowired
	private IFormService formService;

	@RequestMapping("/cruds/projects/lookupTable/paging/read.ajax")
	public @ResponseBody
	PagedData<List<LookupTableDTO>> listLookups(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(value = "sidx", required = false, defaultValue = SodepEntity.ID) String orderBy,
			@RequestParam(value = "sord", required = false, defaultValue = "asc") String order) {
		SessionManager sm = new SessionManager(request);
		Application app = sm.getApplication();

		PagedData<List<LookupTableDTO>> pagedData = lookupService.findAvailableLookupTables(app, null, orderBy,
				order.equals("asc"), pageNumber, pageSize);

		return pagedData;
	}

	@RequestMapping(value = "/lookuptable/delete.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<String> deleteLookupTable(HttpServletRequest request,
			@RequestParam(value = "lutId", required = false) String lutIdParam) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		// Check lutId parameter

		if (StringUtils.isEmpty(lutIdParam)) {
			String message = i18n.getMessage("web.data.lookup_table.validation.emptyLutParam");
			return JsonResponse.buildSimpleFailureResponse(message);
		}

		Long lutId = null;
		try {
			lutId = Long.parseLong(lutIdParam);
		} catch (NumberFormatException e) {
			String message = i18n.getMessage("web.data.lookup_table.validation.invalidLutId");
			return JsonResponse.buildSimpleFailureResponse(message);
		}
		try {
			lookupService.deleteLookupTable(lutId);
		} catch (IllegalArgumentException e) {
			String message = i18n.getMessage("web.data.lookup_table.delete.failure.illegalArgument");
			return JsonResponse.buildSimpleSuccessResponse(message);
		} catch (LookuptableOperationException e) {
			JsonResponse<String> response = LookupExceptionTranslator.translate(e, i18n, formService);
			return response;
		}
		String message = i18n.getMessage("web.data.lookup_table.delete.success");
		return JsonResponse.buildSimpleSuccessResponse(message);
	}

	

}
