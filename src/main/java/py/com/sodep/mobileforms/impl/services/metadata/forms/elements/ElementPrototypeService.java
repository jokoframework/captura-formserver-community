package py.com.sodep.mobileforms.impl.services.metadata.forms.elements;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.dtos.FormProcessItemInfoDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype.InstantiabilityType;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.ElementPrototypeInUseException;
import py.com.sodep.mobileforms.api.exceptions.InvalidDatabaseStateException;
import py.com.sodep.mobileforms.api.exceptions.PersistenceException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.Option;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("ElementPrototypeService")
@Transactional
public class ElementPrototypeService extends BaseService<ElementPrototype> implements IElementPrototypeService {

	private static Logger logger = LoggerFactory.getLogger(ElementPrototypeService.class);

	@Autowired
	private ILookupTableService lookupService;

	@Autowired
	private IAuthorizationControlService controlService;

	@Autowired
	private IPoolService poolService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private ILookupTableService lookupTableService;

	protected ElementPrototypeService() {
		super(ElementPrototype.class);
	}

	/**
	 * Get the label on the desired language or null if there is no translation
	 * 
	 * @param elementId
	 * @param language
	 * @return
	 */
	private String getLabelOnTargetLanguage(Long elementId, String language) {

		Query q = em.createNativeQuery("SELECT labels.value FROM forms.elements_labels labels "
				+ " WHERE labels.element_id=:elementId AND labels.language=:language");
		q.setParameter("elementId", elementId);
		q.setParameter("language", language);
		try {
			return (String) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public String getLabel(Long elementId, String language) {
		String desiredTranslation = getLabelOnTargetLanguage(elementId, language);
		if (desiredTranslation == null) {
			ElementPrototype element = em.find(ElementPrototype.class, elementId);
			String defaultLabel = getLabelOnTargetLanguage(elementId, element.getDefaultLanguage());
			if (defaultLabel == null) {
				throw new ApplicationException("There is no default translation for element " + elementId);
			}
			return defaultLabel;
		}
		return desiredTranslation;
	}

	@Override
	public String getLabelLastestVersion(Long rootId, String language) {
		ElementPrototype ep = findLastVersion(rootId);
		return getLabel(ep.getId(), language);
	}

	public List<Input> systemInputFields() {
		TypedQuery<Input> q = em.createQuery(
				"FROM " + Input.class.getSimpleName() + " e WHERE e.deleted = false AND e.application IS NULL "
						+ " AND e.instantiability = :instantiability", Input.class);
		q.setParameter("instantiability", InstantiabilityType.TEMPLATE);
		List<Input> elements = q.getResultList();
		return elements;
	}

	@Override
	public List<ElementPrototype> systemTemplatePrototypes() {
		TypedQuery<ElementPrototype> q = em.createQuery(
				"FROM " + ElementPrototype.class.getSimpleName()
						+ " e WHERE e.deleted = false AND e.application IS NULL "
						+ " AND e.instantiability = :instantiability", ElementPrototype.class);
		q.setParameter("instantiability", InstantiabilityType.TEMPLATE);
		List<ElementPrototype> elements = q.getResultList();
		return new ArrayList<ElementPrototype>(elements);
	}

	@Override
	public List<ElementPrototype> applicationTemplatePrototypes(Application app) {
		TypedQuery<ElementPrototype> q = em.createQuery("FROM " + ElementPrototype.class.getSimpleName()
				+ " e WHERE e.deleted = false AND e.pool IS NULL " + " AND e.application.id = :appId "
				+ " AND e.instantiability = :instantiability", ElementPrototype.class);
		q.setParameter("instantiability", InstantiabilityType.TEMPLATE);
		q.setParameter("appId", app.getId());
		List<ElementPrototype> elements = q.getResultList();
		return new ArrayList<ElementPrototype>(elements);
	}

	// FIXME why do we have to pass the label? Why not take it from the
	// ElementPrototype?
	public ElementPrototype create(ElementPrototype ep, Pool pool, String defaultLanguage, String label) {
		ep.setVersion(1L);
		ep.setRoot(ep);
		ep.setPool(pool);
		ElementPrototype savedElement = save(ep, defaultLanguage, label);
		if (pool != null) {
			if (!em.contains(pool)) {
				pool = em.find(Pool.class, pool.getId());
			}
			List<ElementPrototype> prototypes = pool.getPrototypes();
			if (prototypes == null) {
				prototypes = new ArrayList<ElementPrototype>();
				pool.setPrototypes(prototypes);
			}
			prototypes.add(savedElement);
		}

		return savedElement;
	}

	@Override
	public ElementPrototype update(ElementPrototype ep, Long rootId, Pool pool, String label) {
		ElementPrototype epLastVersion = findLastVersion(rootId);
		ep.setRoot(epLastVersion.getRoot());
		ep.setVersion(epLastVersion.getVersion() + 1);
		ep.setPool(pool);
		// an update can't change the default language of the process item so we
		// are just using the same
		ElementPrototype savedElement = save(ep, epLastVersion.getDefaultLanguage(), label);
		return savedElement;
	}

	private ElementPrototype save(ElementPrototype entity, String defaultLanguage, String label) {
		entity.setDefaultLanguage(defaultLanguage);
		entity = em.merge(entity);
		entity.setLabel(defaultLanguage, label);
		return super.save(entity);
	}

	@Override
	public Select createSelect(Application app, Pool pool, Select entity, List<Map<String, String>> options, User user,
			String defaultLanguage, String label) {
		// Save the process item
		Select savedEntity = (Select) create(entity, pool, defaultLanguage, label);

		if (OptionSource.EMBEDDED.equals(savedEntity.getSource())) {
			// OptionSource: Embbeded.
			// 1. Create the lookup table with its labels

			// 2. Define the columns
			MFLoookupTableDefinition def = new MFLoookupTableDefinition();

			def.addField(new MFField(FIELD_TYPE.STRING, Select.DROPDOWN_TEXT_FIELD));
			// 3. Create/save the lookup table

			MFLoookupTableDefinition storedLookupTable = lookupTableService.createEmbededLookup(app, user, def);
			List<? extends MFIncomingDataI> optionsTable = toTable(options);
			// 4. Add the values to the lookup table
			MFOperationResult insertData = null;
			try {
				insertData = lookupService.insertData(app, storedLookupTable.getInfo().getPk(), optionsTable, true,
						true);
			} catch (InterruptedException e) {
				String msg = "Insert data into table ID: " + storedLookupTable.getInfo().getPk()
						+ " interrupted. Aborting save.";
				logger.error(msg);
				throw new PersistenceException(msg);
			}

			List<RowCheckError> insertErrors = insertData.getErrors();
			if (insertErrors != null && insertErrors.size() > 0) {
				for (RowCheckError rowCheckError : insertErrors) {
					List<ColumnCheckError> columnErrors = rowCheckError.getColumnErrors();
					String rowError = "Error in row: ";
					for (ColumnCheckError columnCheckError : columnErrors) {
						rowError += "Field " + columnCheckError.getOffendingField() + " with error "
								+ columnCheckError.getErrorType() + " ";
					}
					// TODO: i18n
					throw new PersistenceException(rowError);
				}

			}
			// 5. Associate to the "Select" process item
			savedEntity.setLookupTableId(storedLookupTable.getInfo().getPk());
			savedEntity.setLookupLabel(Select.DROPDOWN_TEXT_FIELD);
			savedEntity.setLookupValue(Select.DROPDOWN_TEXT_FIELD);
		}
		return savedEntity;
	}

	@Override
	public Select updateSelect(Application app, Pool pool, Select entity, Long rootId,
			List<Map<String, String>> options, User user, String label) {

		// Save the process item
		Select savedEntity = (Select) update(entity, rootId, pool, label);

		if (OptionSource.EMBEDDED.equals(savedEntity.getSource())) {
			// OptionSource: Embbeded.
			// 1. Create the lookup table with its labels

			// 2. Define the columns
			MFLoookupTableDefinition def = new MFLoookupTableDefinition();
			// def.addField(new MFField(FIELD_TYPE.NUMBER, "id"));
			def.addField(new MFField(FIELD_TYPE.STRING, Select.DROPDOWN_TEXT_FIELD));
			// 3. Create/save the lookup table
			MFLoookupTableDefinition storedLookupTable = lookupService.createEmbededLookup(pool.getApplication(), user,
					def);
			List<? extends MFIncomingDataI> optionsTable = toTable(options);
			// 4. Add the values to the lookup table
			MFOperationResult insertData = null;
			try {
				insertData = lookupService.insertData(pool.getApplication(), storedLookupTable.getInfo().getPk(),
						optionsTable, true, true);
			} catch (InterruptedException e) {
				String msg = "Insert data into table ID: " + storedLookupTable.getInfo().getPk()
						+ " interrupted. Aborting save.";
				logger.error(msg);
				throw new PersistenceException(msg);
			}

			List<RowCheckError> insertErrors = insertData.getErrors();
			if (insertErrors != null && insertErrors.size() > 0) {
				for (RowCheckError rowCheckError : insertErrors) {
					List<ColumnCheckError> columnErrors = rowCheckError.getColumnErrors();
					String rowError = "Error in row: ";
					for (ColumnCheckError columnCheckError : columnErrors) {
						rowError += "Field " + columnCheckError.getOffendingField() + " with error "
								+ columnCheckError.getErrorType() + " ";
					}
					// TODO: i18n
					throw new PersistenceException(rowError);
				}

			}

			// 5. Associate to the "Select" process item
			savedEntity.setLookupTableId(storedLookupTable.getInfo().getPk());
			savedEntity.setLookupLabel(Select.DROPDOWN_TEXT_FIELD);
			savedEntity.setLookupValue(Select.DROPDOWN_TEXT_FIELD);
		}
		return savedEntity;
	}

	private List<? extends MFIncomingDataI> toTable(List<Map<String, String>> options) {
		List<MFIncomingDataBasic> rows = new ArrayList<MFIncomingDataBasic>();

		int i = 0;
		for (Map<String, String> option : options) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(Select.DROPDOWN_TEXT_FIELD, option.get(Select.DROPDOWN_TEXT_FIELD));
			MFIncomingDataBasic data = new MFIncomingDataBasic(i, map);
			rows.add(data);
			i++;
		}
		return rows;
	}

	@Override
	public List<Option> listSelectOptions(Long selectId, String language) {

		Select select = em.find(Select.class, selectId);

		List<MFManagedData> optionList = lookupService.listAllData(select.getApplication(), select.getLookupTableId());
		List<Option> options = mangedDataToOptionList(select, optionList);
		return options;
	}

	private List<Option> mangedDataToOptionList(Select select, List<MFManagedData> optionList) {
		List<Option> options = new ArrayList<Option>();
		for (MFManagedData option : optionList) {
			Object labelObject = option.getValue(select.getLookupLabel());
			String label = null;
			if (labelObject instanceof String) {
				label = (String) labelObject;
			} else if (labelObject instanceof Number) {
				label = labelObject.toString();
			} else if (labelObject instanceof Date) {
				// FIXME quick fix for #2772
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				label = sdf.format(labelObject);
			}

			Object value = option.getValue(select.getLookupValue());

			// FIXME #119 Lookup Table column could have different types
			options.add(new Option(label, MFDataHelper.serialize(value)));

		}
		return options;
	}

	@Override
	public List<Option> listSelectOptions(Long selectId, String language, String value) {
		Select select = em.find(Select.class, selectId);
		Long looupTableId = select.getLookupTableId();
		if (looupTableId != null) {
			List<MFManagedData> optionList = lookupService.listData(select.getApplication(), looupTableId,
					select.getLookupValue(), value);
			List<Option> options = mangedDataToOptionList(select, optionList);
			return options;
		} else {
			// a select with an empty lookuptable is actually an error but at
			// the time of this writing it was possible.
			// See the report bug #2057. The user managed to enter an embeded
			// select with no lookup data which in turn generated a select
			// without lookup table
			return new ArrayList<Option>();
		}

	}

	@Override
	public MFField getDefinitionOfValueColumn(Long selectId) {
		Select select = em.find(Select.class, selectId);
		MFLoookupTableDefinition def = lookupTableService.getLookupTableDefinition(select.getLookupTableId());
		Map<String, MFField> fieldsMap = def.fieldsMappedByName();
		MFField mfField = fieldsMap.get(select.getLookupValue());
		return mfField;
	}

	@Override
	public List<Map<String, String>> getSelectOptions(Long rootid) {
		ElementPrototype element = findLastVersion(rootid);
		if (element == null) {
			return null;
		}
		if (!(element instanceof Select)) {
			throw new ApplicationException("Trying to get select options from a Process item of class: "
					+ element.getClass());
		}
		Select select = (Select) element;
		Long lookupIdentifier = select.getLookupTableId();

		List<Map<String, String>> options = new ArrayList<Map<String, String>>();

		List<MFManagedData> listAllData = lookupService.listAllData(select.getApplication(), lookupIdentifier);
		for (MFManagedData mfManagedData : listAllData) {
			Map<String, ?> userData = mfManagedData.getUserData();
			String name = (String) userData.get(Select.DROPDOWN_TEXT_FIELD);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Select.DROPDOWN_TEXT_FIELD, name);
			String defaultValue = select.getDefaultValue();
			if (defaultValue != null && defaultValue.equals(name)) {
				map.put(Select.DROPDOWN_RADIO_FIELD, Boolean.TRUE.toString());
			} else {
				map.put(Select.DROPDOWN_RADIO_FIELD, Boolean.FALSE.toString());
			}
			options.add(map);
		}
		return options;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public List<ElementPrototype> findAll(User user, Pool pool, String auth) {
		if (!controlService.has(pool, user, auth)) {
			return new ArrayList<ElementPrototype>(Collections.<ElementPrototype> emptyList());
		}

		String queryStr = "SELECT e FROM ElementPrototypeLastVersionView ev, ElementPrototype e "
				+ " WHERE e.deleted=false AND e.application = :application AND e.root=ev.root AND e.version=ev.version "
				+ " AND e.pool = :pool AND e.instantiability = 0 ";

		TypedQuery<ElementPrototype> q = em.createQuery(queryStr, ElementPrototype.class);
		q.setParameter("application", pool.getApplication());
		q.setParameter("pool", pool);

		List<ElementPrototype> data = new ArrayList<ElementPrototype>(q.getResultList());
		return data;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findAll(String language, User user, Pool pool, String auth, int page,
			int pageSize) {

		return findAll(language, user, pool, auth, page, pageSize, null, true);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findAll(String language, User user, Pool pool, String auth, int page,
			int pageSize, String orderBy, boolean asc) {
		if (!controlService.has(pool, user, auth)) {
			return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
					pageSize, 0);
		}

		List<Pool> authPools = new ArrayList<Pool>();
		authPools.add(pool);

		return executeFindAll(language, pool.getApplication(), page, pageSize, authPools, orderBy, asc);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findAllNotInPool(String language, User user, Application application,
			Pool poolToExclude, String auth, int page, int pageSize) {

		List<Pool> authPools = controlService.listPoolsByAuth(application, user, auth);
		if (authPools != null) {
			authPools.remove(poolToExclude);
			if (authPools.isEmpty()) {
				return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
						pageSize, 0);
			}
		}

		return executeFindAll(language, application, page, pageSize, authPools);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findAll(String language, User user, Application application, String auth,
			int page, int pageSize) {

		List<Pool> authPools = controlService.listPoolsByAuth(application, user, auth);

		if (authPools == null || authPools.isEmpty()) {
			return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
					pageSize, 0);
		}

		return executeFindAll(language, application, page, pageSize, authPools);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findByLabel(User user, Application application, String auth, String value,
			int page, int pageSize, String language) {
		List<Pool> authPools = controlService.listPoolsByAuth(application, user, auth);
		if (authPools == null || authPools.isEmpty()) {
			return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
					pageSize, 0);
		}

		List<Long> ids = new ArrayList<Long>();
		for (Pool pool : authPools) {
			ids.add(pool.getId());
		}

		return executeFindByLabel(application, value, page, pageSize, ids, language);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findByLabel(User user, Pool pool, String auth, String value, int page,
			int pageSize, String language) {
		if (!controlService.has(pool, user, auth)) {
			return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
					pageSize, 0);
		}

		List<Long> ids = new ArrayList<Long>();
		ids.add(pool.getId());

		return executeFindByLabel(pool.getApplication(), value, page, pageSize, ids, language);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public PagedData<List<ElementPrototype>> findByLabelNotInPool(User user, Application application,
			Pool poolToExclude, String auth, String value, int page, int pageSize, String language) {
		List<Pool> authPools = controlService.listPoolsByAuth(application, user, auth);

		List<Long> ids = new ArrayList<Long>();
		for (Pool pool : authPools) {
			if (!pool.equals(poolToExclude)) {
				ids.add(pool.getId());
			}
		}
		if (ids == null || ids.isEmpty()) {
			return new PagedData<List<ElementPrototype>>(Collections.<ElementPrototype> emptyList(), 0L, page,
					pageSize, 0);
		}
		return executeFindByLabel(application, value, page, pageSize, ids, language);
	}

	private PagedData<List<ElementPrototype>> executeFindAll(String language, Application application, int page,
			int pageSize, List<Pool> authPools) {
		return executeFindAll(language, application, page, pageSize, authPools, null, true);
	}

	private PagedData<List<ElementPrototype>> executeFindAll(String language, Application application, int page,
			int pageSize, List<Pool> authPools, String orderBy, boolean asc) {

		String orderStr = " order by lower(COALESCE(l.value,l2.value)) ";
		if (orderBy != null) {
			if (orderBy.equals("label")) {
				orderStr = " order by lower(COALESCE(l.value,l2.value)) ";
			}

			orderStr += (asc) ? "asc" : "desc";
		}
		String baseQueryStr = " from forms.element_prototypes e "
				+ "join forms.element_prototypes_last_version_view lv on e.root_id=lv.root_id and e.version=lv.version "
				+ "left outer join forms.elements_labels l on  (e.id=l.element_id and l.language=:desireLanguage) "
				+ "left outer join forms.elements_labels l2 on  (e.id=l2.element_id and l2.language=e.default_language) "
				+ "where e.deleted=false and e.application_id=:application_id and e.instantiability=0 "
				+ "and e.pool_id in (:pools) ";


		String completeQuery = "SELECT e.id " + baseQueryStr + orderStr;

		Query q = em.createNativeQuery(completeQuery);
		q.setParameter("application_id", application.getId());
		q.setParameter("pools", authPools);
		q.setParameter("desireLanguage", language);

		q.setMaxResults(pageSize);
		q.setFirstResult((page - 1) * pageSize);

		ArrayList<ElementPrototype> data = new ArrayList<ElementPrototype>();
		List<BigInteger> ids = (List<BigInteger>) q.getResultList();
		for (BigInteger id : ids) {
			Long elementId = id.longValue();
			ElementPrototype element = em.find(ElementPrototype.class, elementId);
			data.add(element);
		}

		String queryCount = "SELECT COUNT(1) " + baseQueryStr;
		Query countQuery = em.createNativeQuery(queryCount);
		countQuery.setParameter("application_id", application.getId());
		countQuery.setParameter("pools", authPools);
		countQuery.setParameter("desireLanguage", language);
		BigInteger count = (BigInteger) countQuery.getSingleResult();

		PagedData<List<ElementPrototype>> pagedData = new PagedData<List<ElementPrototype>>(data, count.longValue(),
				page, pageSize, data.size());
		return pagedData;
	}

	private PagedData<List<ElementPrototype>> executeFindByLabel(Application application, String value, int page,
			int pageSize, List<Long> ids, String language) {
		String like = "%" + value.trim().toLowerCase() + "%";

		String baseQueryStr = " FROM forms.element_prototypes_last_version_view ev, forms.element_prototypes e "
				+ " LEFT OUTER JOIN forms.elements_labels l ON e.id = l.element_id and l.language = :language"
				+ " JOIN forms.elements_labels l2 ON e.id = l2.element_id and l2.language=e.default_language "
				+ " WHERE e.deleted=false  AND lower(COALESCE(l.value,l2.value)) LIKE lower(:like) "
				+ " AND e.application_id=:appId AND e.root_id=ev.root_id AND e.version=ev.version AND e.instantiability = 0 "
				+ " AND e.pool_id IN (:poolsId) ";

		Query q = em.createNativeQuery(" Select e.id " + baseQueryStr + " ORDER BY l.value");
		q.setParameter("appId", application.getId());
		q.setParameter("like", like);
		q.setParameter("language", language);
		q.setParameter("poolsId", ids);
		q.setMaxResults(pageSize);
		q.setFirstResult((page - 1) * pageSize);

		@SuppressWarnings("unchecked")
		List<BigInteger> result = q.getResultList();

		List<ElementPrototype> data = new ArrayList<ElementPrototype>();
		for (BigInteger id : result) {
			ElementPrototype ep = findById(id.longValue());
			data.add(ep);
		}

		Query countQuery = em.createNativeQuery("Select COUNT(e) " + baseQueryStr);
		countQuery.setParameter("appId", application.getId());
		countQuery.setParameter("like", like);
		countQuery.setParameter("language", language);
		countQuery.setParameter("poolsId", ids);

		BigInteger count = (BigInteger) countQuery.getSingleResult();

		PagedData<List<ElementPrototype>> pagedData = new PagedData<List<ElementPrototype>>(data, count.longValue(),
				page, pageSize, data.size());

		return pagedData;
	}

	private ElementPrototype find(Long rootId, Long version) {
		String queryStr = "SELECT p FROM ElementPrototype p WHERE p.root.id = :rootId AND p.version = :version";
		Query query = em.createQuery(queryStr);
		query.setParameter("rootId", rootId);
		query.setParameter("version", version);

		ElementPrototype singleResult = null;
		try {
			singleResult = (ElementPrototype) query.getSingleResult();
			return singleResult;
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			logger.error(e.getMessage(), e);
			throw new InvalidDatabaseStateException("The are more than one process item with rootId=" + rootId
					+ " and version=" + version, e);
		}
	}

	@Override
	public ElementPrototype findLastVersion(Long rootId) {
		try {
			String queryStr = "select version from forms.element_prototypes_last_version_view where root_id = :rootId";
			Query query = em.createNativeQuery(queryStr);
			query.setParameter("rootId", rootId);
			Object singleResult = query.getSingleResult();
			BigInteger version = (BigInteger) singleResult;
			return find(rootId, version.longValue());
		} catch (NoResultException e) {
			logger.warn(e.getMessage());
			return null;
		} catch (NonUniqueResultException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}

	@Override
	public PagedData<List<FormProcessItemInfoDTO>> formsUsingProcessItem(Long rootId, String orderBy,
			boolean ascending, Integer page, Integer pageSize, String language) {
		String baseQuery = " FROM forms.forms_last_version_view last_form "
				+ "JOIN forms.forms form ON (form.root_id = last_form.root_id AND form.version = last_form.version) "
				+ "JOIN projects.projects project ON (form.project_id = project.id) "
				+ "JOIN forms.pages page ON (page.form_id = form.id) "
				+ "JOIN forms.element_instances einstance ON (page.id = einstance.page_id) "
				+ "JOIN forms.element_prototypes proto ON (einstance.prototype_id = proto.id) "
				+ "WHERE form.deleted = false AND page.deleted = false AND einstance.deleted = false"
				+ " AND proto.root_id = :rootId ";

		Query q = em.createNativeQuery("SELECT form.id, proto.version" + baseQuery);

		q.setMaxResults(pageSize);
		q.setFirstResult((page - 1) * pageSize);
		q.setParameter("rootId", rootId);

		List<FormProcessItemInfoDTO> data = new ArrayList<FormProcessItemInfoDTO>();
		@SuppressWarnings("unchecked")
		ArrayList<Object[]> rows = (ArrayList<Object[]>) q.getResultList();
		for (Object[] row : rows) {
			BigInteger formId = (BigInteger) row[0];
			BigInteger epVersion = (BigInteger) row[1];

			Form form = em.find(Form.class, formId.longValue());
			Project project = form.getProject();
			ProjectDetails detail = projectService.loadDetails(project.getId(), language);
			String projectName = null;
			if (detail != null) {
				projectName = detail.getLabel();
			}
			String formName = form.getLabel(language);
			FormProcessItemInfoDTO dto = new FormProcessItemInfoDTO(form.getRoot().getId(), formName,
					project.getId(), projectName, epVersion.longValue());
			data.add(dto);
		}

		Query countQuery = em.createNativeQuery("SELECT COUNT(form.id)" + baseQuery);

		countQuery.setMaxResults(pageSize);
		countQuery.setFirstResult((page - 1) * pageSize);
		countQuery.setParameter("rootId", rootId);

		BigInteger count = (BigInteger) countQuery.getSingleResult();

		PagedData<List<FormProcessItemInfoDTO>> pagedData = new PagedData<List<FormProcessItemInfoDTO>>(data,
				count.longValue(), page, pageSize, data.size());

		return pagedData;
	}

	@Override
	public List<Form> formsUsingProcessItem(Long rootId) {

		String baseQuery = "SELECT form.* FROM forms.forms_last_version_view last_form "
				+ " JOIN forms.forms form ON (form.root_id = last_form.root_id AND form.version = last_form.version) "
				+ " JOIN forms.pages page ON (page.form_id = form.id) "
				+ " JOIN forms.element_instances einstance ON (page.id = einstance.page_id) "
				+ " JOIN forms.element_prototypes proto ON (einstance.prototype_id = proto.id) "
				+ " WHERE form.deleted = false AND page.deleted = false AND einstance.deleted = false"
				+ " AND proto.root_id = :rootId ";

		Query q = em.createNativeQuery(baseQuery, Form.class);
		q.setParameter("rootId", rootId);
		@SuppressWarnings("unchecked")
		List<Form> forms = (List<Form>) q.getResultList();
		return forms;
	}

	@Override
	public ElementPrototype importElementPrototypeAndSave(Application app, Long elementRootId, Long destPoolId,
			User user, String language) {
		ElementPrototype ep = findLastVersion(elementRootId);
		Pool pool = poolService.findById(destPoolId);
		try {
			ElementPrototype clonedElement = ep.clone();
			clonedElement.setPool(pool);
			if (clonedElement instanceof Select) {
				Select select = (Select) ep;
				if (OptionSource.EMBEDDED.equals(select.getSource())) {
					List<MFManagedData> optionListMF = lookupTableService.listAllData(select.getApplication(),
							select.getLookupTableId());
					List<Map<String, String>> options = new ArrayList<Map<String, String>>();
					for (MFManagedData optionMF : optionListMF) {
						Map<String, String> option = new HashMap<String, String>();
						String label = (String) optionMF.getValue(select.getLookupLabel());
						String value = (String) optionMF.getValue(select.getLookupValue());
						option.put(label, value);
						options.add(option);
					}
					createSelect(app, pool, (Select) clonedElement, options, user, language,
							clonedElement.getLabel(language));
				} else {
					create(clonedElement, pool, language, clonedElement.getLabel(language));
				}
			} else {
				create(clonedElement, pool, language, clonedElement.getLabel(language));
			}
			return clonedElement;
		} catch (CloneNotSupportedException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}

	public ElementPrototype logicalDelete(ElementPrototype prototype) {
		if (canDelete(prototype)) {
			return super.logicalDelete(prototype);
		}
		return prototype;
	}

	private boolean canDelete(ElementPrototype prototype) {
		if (!em.contains(prototype)) {
			prototype = em.find(prototype.getClass(), prototype.getId());
		}

		List<ElementInstance> elements = prototype.getElements();
		List<Form> forms = new ArrayList<Form>();
		Query query = em.createNativeQuery("SELECT f.* FROM forms.forms f " + "JOIN forms.pages p ON f.id = p.form_id "
				+ "JOIN forms.element_instances e ON p.id = e.page_id "
				+ " WHERE f.deleted = false AND p.deleted = false " + "AND e.deleted = false AND e.id = :elementId ",
				Form.class);
		for (ElementInstance element : elements) {
			if (!element.getDeleted()) {
				query.setParameter("elementId", element.getId());
				Form f = (Form) query.getSingleResult();
				if (!forms.contains(f)) {
					forms.add(f);
				}
			}
		}

		if (!forms.isEmpty()) {
			ElementPrototypeInUseException exception = new ElementPrototypeInUseException();
			exception.setForms(forms);
			throw exception;
		}

		return true;
	}

	@Override
	public String getDefaultLanguageOfProcessItem(Long rootId) {
		ElementPrototype element = em.find(ElementPrototype.class, rootId);
		return element.getDefaultLanguage();
	}
}
