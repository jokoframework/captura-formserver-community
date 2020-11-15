package py.com.sodep.mobileforms.api.services.reports;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.QueryDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.OrderBy;

public interface IReportQueryService {

	List<QueryDTO> listAllQuerys(Long formId, Long version);

	QueryDefinitionDTO getDefaultQuery(Long formId, Long version, String language);

	public QueryDefinitionDTO getQueryDefinition(User u, Long queryID);

	public QueryDefinitionDTO saveQuery(Long formId, QueryDefinitionDTO queryDefinition, String language);

	QueryDefinitionDTO deleteQuery(User user, Long queryId);
	
	public List<OrderBy> getSortingColumns(Long queryID);
}
