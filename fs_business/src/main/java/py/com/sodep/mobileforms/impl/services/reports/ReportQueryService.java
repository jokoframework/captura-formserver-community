package py.com.sodep.mobileforms.impl.services.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDTO;
import py.com.sodep.mobileforms.api.dtos.QueryDefinitionDTO;
import py.com.sodep.mobileforms.api.dtos.ReportFilterOptionDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.report.Query;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.exceptions.InvalidDatabaseStateException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.reports.IReportQueryService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("QueryService")
@Transactional
public class ReportQueryService implements IReportQueryService {

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Autowired
	private IFormService formService;

	@Autowired
	private IAuthorizationControlService authControlService;

	@Override
	@Authorizable(value = AuthorizationNames.Form.VIEW_REPORTS, formParam = 0)
	public List<QueryDTO> listAllQuerys(Long formId, Long version) {
		Form form = this.formService.getForm(formId, version);
		TypedQuery<Query> q = em.createQuery("FROM " + Query.class.getSimpleName()
				+ " q WHERE q.form.id = :formId ORDER BY name", Query.class);
		q.setParameter("formId", form.getId());
		List<Query> querys = q.getResultList();
		List<QueryDTO> response = new ArrayList<QueryDTO>();
		for (Query query : querys) {
			QueryDTO dto = new QueryDTO();
			dto.setId(query.getId());
			dto.setName(query.getName());
			dto.setFormId(formId);
			if (query.getDefaultQuery() != null) {
				dto.setDefaultQuery(query.getDefaultQuery());
			}
			response.add(dto);
		}
		return response;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.VIEW_REPORTS, formParam = 0)
	public QueryDefinitionDTO getDefaultQuery(Long formId, Long version, String language) {
		Form form = this.formService.getForm(formId, version);
		TypedQuery<Query> q = em.createQuery("FROM " + Query.class.getSimpleName()
				+ " q WHERE q.form = :form AND q.defaultQuery = true", Query.class);
		q.setParameter("form", form);
		Query query = null;
		try {
			query = q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			throw new InvalidDatabaseStateException(e);
		}
		return this.getQueryDefinition(query.getId());
	}

	private void validateQueryUniqueness(QueryDefinitionDTO queryDTO) {
		Form form = this.formService.getForm(queryDTO.getFormId(), queryDTO.getVersion());
		String queryStr = "SELECT count(*) FROM Query q WHERE q.name = :name AND q.form = :form ";
		if (queryDTO.getId() != null) {
			// do not check against itself if its an already stored query
			queryStr += " AND q.id != :id";
		}

		javax.persistence.Query q = em.createQuery(queryStr);
		q.setParameter("name", queryDTO.getName());
		q.setParameter("form", form);
		if (queryDTO.getId() != null) {
			q.setParameter("id", queryDTO.getId());
		}
		Long singleResult = (Long) q.getSingleResult();

		if (singleResult.longValue() > 0) {
			DuplicateEntityException ex = new DuplicateEntityException("web.home.querys.invalid.duplicated");
			ex.addMessage("form", "web.home.querys.invalid.duplicated");
			throw ex;
		}
	}

	/**
	 * Set the flag defaultQuery to false of all queries of the form
	 * 
	 * @param queryId
	 * @param form
	 */
	private void turnOffPreviousDefaultQuery(Form form) {
		javax.persistence.Query query = em.createQuery("Update  " + Query.class.getName()
				+ " A set A.defaultQuery=false where A.form=:form");
		query.setParameter("form", form);
		query.executeUpdate();

	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.CREATE_REPORTS, formParam = 0)
	public QueryDefinitionDTO saveQuery(Long formId, QueryDefinitionDTO queryDefinition, String language) {
		validateQueryUniqueness(queryDefinition);
		Query query = null;
		boolean isNew = queryDefinition.getId() == null;
		if (isNew) {
			query = new Query();
		} else {
			query = em.find(Query.class, queryDefinition.getId());
		}
		// associate the query with the correct form
		Form form = formService.getForm(queryDefinition.getFormId(), queryDefinition.getVersion());
		query.setForm(form);
		query.setName(queryDefinition.getName());
		try {
			ObjectMapper mapper = new ObjectMapper();

			// TODO check that the list of columns do not contain columns that
			// are not on the model
			if (queryDefinition.getSelectedTableColumns() != null) {
				String selectedTableColumns = mapper.writeValueAsString(queryDefinition.getSelectedTableColumns());
				query.setSelectedTableColumns(selectedTableColumns);
			} else {
				query.setSelectedTableColumns(null);
			}

			if (queryDefinition.getSelectedCSVColumns() != null) {
				String selectedCSVColumns = mapper.writeValueAsString(queryDefinition.getSelectedCSVColumns());
				query.setSelectedCSVColumns(selectedCSVColumns);
			} else {
				query.setSelectedCSVColumns(null);
			}

			if (queryDefinition.getSelectedSortingColumns() != null) {
				String selectedSortingColumns = mapper.writeValueAsString(queryDefinition.getSelectedSortingColumns());
				query.setSelectedSortingColumns(selectedSortingColumns);
			} else {
				query.setSelectedSortingColumns(null);
			}

			if (queryDefinition.getFilterOptions() != null) {
				String filterOptions = mapper.writeValueAsString(queryDefinition.getFilterOptions());
				query.setFilterOptions(filterOptions);
			}

			String locationsAsLinksStr = mapper.writeValueAsString(queryDefinition.getDownloadLocationsAsLinks());
			Boolean locationsAsLinks = Boolean.valueOf(locationsAsLinksStr);
			query.setDownloadLocationsAsLinks(locationsAsLinks);

            if(queryDefinition.getElementsFileNames() != null) {
                String elementsFileNamesValue = mapper.writeValueAsString(queryDefinition.getElementsFileNames());
                query.setElementsFileNames(elementsFileNamesValue);
            }


		} catch (JsonGenerationException e) {
			// these exception should never happen, so we are just wrapping it
			// in a runtimeexception
			throw new ApplicationException("Error storing a query", e);
		} catch (JsonMappingException e) {
			throw new ApplicationException("Error storing a query", e);
		} catch (IOException e) {
			throw new ApplicationException("Error storing a query", e);
		}
		
				
		if (queryDefinition.isDefaultQuery()) {
			// CAP-434 Revert changes from CAP-254
			turnOffPreviousDefaultQuery(form);
			// CAP-434 TODO Investigate why this works
			if (em.contains(query)) {
				em.refresh(query);
			}
			
			query.setDefaultQuery(true);
		} else {
			query.setDefaultQuery(false);
		}
		
		if (isNew) {
			em.persist(query);
		}

		queryDefinition.setId(query.getId());
		return queryDefinition;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// This method doesn't perform any authorization check because the
	// authorization is performed by the method itself
	public QueryDefinitionDTO getQueryDefinition(User u, Long queryID) {
		// This method perform its own validation because the form (on which the
		// authorization are granted) is not known before calling this method
		Query query = em.find(Query.class, queryID);
		if (!authControlService.hasFormLevelAccess(query.getForm().getId(), u, AuthorizationNames.Form.VIEW_REPORTS)) {
			throw new AuthorizationException("Can't execute method. Not enough authorization");
		}
		return getQueryDefinition(queryID);
	}

	private QueryDefinitionDTO getQueryDefinition(Long queryID) {

		Query query = em.find(Query.class, queryID);
		QueryDefinitionDTO def = new QueryDefinitionDTO();
		def.setId(query.getId());
		def.setName(query.getName());
		if (query.getDefaultQuery() != null) {
			def.setDefaultQuery(query.getDefaultQuery());
		}
		// We are using a fixed language because this method is not interested
		// on any of the label, so it doesn't matter which language we are
		// actually using
		FormDTO formDTO = formService.getFormDTO(query.getForm(), "en");
		def.setFormId(formDTO.getId());
		def.setVersion(formDTO.getVersion());
		ObjectMapper mapper = new ObjectMapper();
		try {

			String selectedTableColumns = query.getSelectedTableColumns();

			if (selectedTableColumns != null) {
				List<String> selectedColumns = mapper.readValue(selectedTableColumns,
						new TypeReference<List<String>>() {
						});
				def.setSelectedTableColumns(selectedColumns);
			}

			String selectedSortingJSON = query.getSelectedSortingColumns();

			if (selectedSortingJSON != null) {
				List<String> selectedSortingColumns = mapper.readValue(selectedSortingJSON,
						new TypeReference<List<String>>() {
						});
				def.setSelectedSortingColumns(selectedSortingColumns);
			}

			String filterOptionsJSON = query.getFilterOptions();
			if (filterOptionsJSON != null) {
				List<ReportFilterOptionDTO> filterOptions = mapper.readValue(filterOptionsJSON,
						new TypeReference<List<ReportFilterOptionDTO>>() {
						});
				def.setFilterOptions(filterOptions);
			}

			def.setDownloadLocationsAsLinks(query.getDownloadLocationsAsLinks());

            String elementsFileNamesValue = query.getElementsFileNames();
            if (elementsFileNamesValue != null) {
                Map<String, String> elementsFileNames = mapper.readValue(elementsFileNamesValue,
                        new TypeReference<Map<String, String>>() {
                        });
                def.setElementsFileNames(elementsFileNames);
            }

		} catch (JsonParseException e) {
			/*
			 * This exception should never happen, so we are just wrapping it in
			 * a runtime exception
			 */
			throw new ApplicationException("Error storing a query", e);
		} catch (JsonMappingException e) {
			throw new ApplicationException("Error storing a query", e);
		} catch (IOException e) {
			throw new ApplicationException("Error storing a query", e);
		}

		return def;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	// This method doesn't perform any authorization check because the
	// authorization is performed by the method itself
	public QueryDefinitionDTO deleteQuery(User user, Long queryID) {
		Query query = em.find(Query.class, queryID);
		if (query == null) {
			throw new IllegalArgumentException("ID #" + queryID + ", is no a valid queryID");
		}
		
		if (!authControlService.hasFormLevelAccess(query.getForm().getId(), user,
				AuthorizationNames.Form.DELETE_REPORTS)) {
			throw new AuthorizationException("Can't execute method. Not enough authorization");
		}

		QueryDefinitionDTO def = getQueryDefinition(queryID);
		em.remove(query);
		return def;
	}
	
	@Override
	public List<OrderBy> getSortingColumns(Long queryID) {
		Query query = em.find(Query.class, queryID);
		String sortingColumnsJSON = query.getSelectedSortingColumns();

		List<OrderBy> sortingColumns = new ArrayList<OrderBy>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			if (sortingColumnsJSON != null) {
				List<String> selectedSortingColumns = mapper.readValue(sortingColumnsJSON,
						new TypeReference<List<String>>() {
						});

				for (String column : selectedSortingColumns) {
					String field = column.substring(0, column.indexOf('_'));
					String order = column.substring(column.indexOf('_') + 1).toLowerCase();
					Boolean ascending;

					if (order.equals("asc")) {
						ascending = true;
					} else {
						ascending = false;
					}

					OrderBy orderBy = new OrderBy(field, ascending);
					sortingColumns.add(orderBy);
				}
			}
		} catch (JsonParseException e) {
			/*
			 * This exception should never happen, so we are just wrapping it in
			 * a runtime exception
			 */
			throw new ApplicationException("Error storing a query", e);
		} catch (JsonMappingException e) {
			throw new ApplicationException("Error storing a query", e);
		} catch (IOException e) {
			throw new ApplicationException("Error storing a query", e);
		}

		return sortingColumns;
	}

}