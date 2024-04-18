package py.com.sodep.mobileforms.web.controllers.report;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;






// import org.hibernate.OptimisticLockException; //FIXME Wrong!, web should not depend on hibernate. Hibernate is our persistence library
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.reports.IReportQueryService;
import py.com.sodep.mobileforms.api.services.workflow.IWorkflowDataService;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class ReportQueryController extends SodepController {

	@Autowired
	private IReportQueryService queryService;
	
	@Autowired
	private IWorkflowDataService workflowService;

	@RequestMapping(value = "/reports/querys/listAllQueries.ajax")
	public @ResponseBody
	List<QueryDTO> listAllQueries(HttpServletRequest request,
			@RequestParam(value = "formId", required = true) Long formId,
			@RequestParam(value = "version", required = true) Long version) {
		return this.queryService.listAllQuerys(formId, version);
	}

	@RequestMapping(value = "/reports/querys/getQuery.ajax")
	public @ResponseBody
	QueryDefinitionDTO getQuery(HttpServletRequest request,
			@RequestParam(value = "queryId", required = true) Long queryID) {
		SessionManager mgr = new SessionManager(request);
		QueryDefinitionDTO queryDefinitionDTO = this.queryService.getQueryDefinition(mgr.getUser(), queryID);
		FormDTO formDTO = new FormDTO();
		formDTO.setId(queryDefinitionDTO.getFormId());
		formDTO.setVersion(queryDefinitionDTO.getVersion());
		if (!workflowService.workflowEnabled(mgr.getUser(), formDTO)) {
			queryDefinitionDTO.setFilterOptions(excludeStateFilterOption(queryDefinitionDTO.getFilterOptions()));
			queryDefinitionDTO.setSelectedTableColumns(excludeStateSelectedTableColumn(queryDefinitionDTO.getSelectedTableColumns()));
		}
		return queryDefinitionDTO;
	}

	private List<ReportFilterOptionDTO> excludeStateFilterOption(
			List<ReportFilterOptionDTO> filterOptions) {
		List<ReportFilterOptionDTO> newFilterOptions = new ArrayList<ReportFilterOptionDTO>();
		for (ReportFilterOptionDTO filterOption : filterOptions) {
			if (!filterOption.getElementId().equals("meta_stateId")) {
				newFilterOptions.add(filterOption);
			}
		}
		return newFilterOptions;
	}

	private List<String> excludeStateSelectedTableColumn(
			List<String> selectedTableColumns) {
		List<String> newSelectedTableColumns = new ArrayList<String>();
		for (String selectedTableColumn : selectedTableColumns) {
			if (!selectedTableColumn.equals("meta_stateId")) {
				newSelectedTableColumns.add(selectedTableColumn);
			}
		}
		return newSelectedTableColumns;
	}
	
	@RequestMapping(value = "/reports/querys/saveQuery.ajax")
	public @ResponseBody
	JsonResponse<QueryDTO> saveQuery(HttpServletRequest request, @RequestBody QueryDefinitionDTO def) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<QueryDTO> response = new JsonResponse<QueryDTO>();
		try {
			/*Text Report Columns (CSV, excel, pdf) now are the same as visible columns */
			def.setSelectedCSVColumns(def.getSelectedTableColumns());
			QueryDefinitionDTO savedQuery = this.queryService.saveQuery(def.getFormId(), def,
					i18n.getSelectedLanguage());

			response.setSuccess(true);
			response.setTitle(i18n.getMessage("web.home.querys.saved.title"));
			response.setMessage(i18n.getMessage("web.home.querys.saved.message", savedQuery.getName()));
			response.setObj(savedQuery);
		} catch (InvalidEntityException e) {
			sodepServiceExceptionToJsonResponse(request, i18n, response, e);
		} catch (RuntimeException e) {
			response.setSuccess(false);
			response.setTitle(i18n.getMessage("web.home.querys.notsaved.title"));
			response.setMessage(i18n.getMessage("web.home.querys.notsaved.message", def.getName()));
		}
		return response;
	}

	@RequestMapping(value = "/reports/querys/deleteQuery.ajax")
	public @ResponseBody
	JsonResponse<String> deleteQuery(HttpServletRequest request, @RequestParam(value = "queryId") Long queryId) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		QueryDefinitionDTO def = queryService.deleteQuery(mgr.getUser(), queryId);

		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(true);
		response.setTitle(i18n.getMessage("web.home.querys.deleted.title"));
		if (def != null) {
			// Since there is a small chance that the query was deleted by
			// another user we need to check if the query is not null
			response.setMessage(i18n.getMessage("web.home.querys.deleted.message", def.getName()));
		} else {
			response.setMessage(i18n.getMessage("web.home.querys.deleted.message", ""));
		}
		return response;
	}

}
