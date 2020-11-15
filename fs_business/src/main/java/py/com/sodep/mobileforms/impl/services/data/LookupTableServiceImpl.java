package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.IllegalValueException;
import py.com.sodep.mf.exchange.MFDataHelper;
import py.com.sodep.mf.exchange.MFDataSetDefinition;
import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.data.DBLookupTable;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.LookuptableOperationException;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFDataSetDefinitionMongo;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;

@Service("data.LookupTableService")
@Transactional
public class LookupTableServiceImpl extends BaseService<DBLookupTable> implements ILookupTableService {

	private static final String lookupSortableColumns[] = new String[] { "name", "acceptsRESTDML", "identifier" };

	static {
		Arrays.sort(lookupSortableColumns);
	}

	@Autowired
	private IDataAccessService dataAccess;

	@Autowired
	private IFormService formService;

	protected LookupTableServiceImpl() {
		super(DBLookupTable.class);

	}

	private MFLoookupTableDefinition createLookupTable(Application app, User u, MFLoookupTableDefinition definition,
			boolean isEmbeded) throws LookupTableDefinitionException {
		LookupTableDTO info = definition.getInfo();
		if (info == null) {
			throw new IllegalArgumentException("MFLoookupTableDefinition doesn't have the required information");
		}

		if (info.isAcceptRESTDMLs()) {
			// check if there is another lookuptable with the same identifier
			// This is only relevant for remote lookup tables
			List<LookupTableDTO> lookups = listAvailableLookupTables(app, info.getIdentifier());
			if (lookups.size() > 0) {
				// there is already another lookup table with the same
				// identifier
				// on the same application
				throw new LookupTableDefinitionException(LookupTableDefinitionException.IDENTIFIER_ALREADY_EXISTS,
						"There is already a lookup table with the same identifier");
			}
		}

		int size = definition.getFields().size();
		if (size > MFLoookupTableDefinition.MAX_FIELDS) {
			throw new LookupTableDefinitionException(LookupTableDefinitionException.MAX_FIELDS,
					"The max number of columns of a lookuptable is " + MFLoookupTableDefinition.MAX_FIELDS);
		}

		MFDataSetDefinition storedDefinition = dataAccess.define(definition);

		DBLookupTable table = new DBLookupTable();

		table.setDataSetDefinition(storedDefinition.getMetaDataRef());
		table.setDatasetVersion(storedDefinition.getVersion());
		table.setApplication(app);

		table.setOwner(u);

		table.setIdentifier(info.getIdentifier());
		table.setName(info.getName());
		table.setAcceptsRESTDML(info.isAcceptRESTDMLs());
		if (isEmbeded) {
			table.setSource(OptionSource.EMBEDDED);
		} else {
			table.setSource(OptionSource.LOOKUP_TABLE);
		}
		DBLookupTable savedTable = save(u, table);

		return toLookupDef(savedTable);

	}

	@Override
	@Authorizable(value = AuthorizationNames.App.REST_API_LOOKUPTABLES_CREATE)
	public MFLoookupTableDefinition createLookupTable(Application app, User u, MFLoookupTableDefinition definition)
			throws LookupTableDefinitionException {
		return createLookupTable(app, u, definition, false);
	}

	@Authorizable(value = AuthorizationNames.App.REST_API_LOOKUPTABLES_CREATE)
	public MFLoookupTableDefinition createEmbededLookup(Application app, User u, MFLoookupTableDefinition definition) {
		try {
			LookupTableDTO info = new LookupTableDTO();
			info.setApplicationId(app.getId());
			info.setAcceptRESTDMLs(false);
			definition.setInfo(info);
			return createLookupTable(app, u, definition, true);
		} catch (LookupTableDefinitionException e) {
			// this type of exception should not happened within an embedded
			// lookup table. Therefore, I consider it an unexpected error
			throw new RuntimeException(e);
		}
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MFLoookupTableDefinition getLookupTableDefinition(Long lookupTableId) {
		DBLookupTable lookupTable = findById(lookupTableId);
		if (lookupTable != null) {
			return toLookupDef(lookupTable);
		} else {
			return null;
		}

	}

	private MFLoookupTableDefinition toLookupDef(DBLookupTable lookupTable) {
		MFDataSetDefinitionMongo defOnMongo = dataAccess.getDataSetDefinition(lookupTable.getDataSetDefinition(),
				lookupTable.getDatasetVersion());
		MFLoookupTableDefinition def = new MFLoookupTableDefinition(defOnMongo);
		LookupTableDTO dto = lookupTable.toDTO();
		def.setInfo(dto);
		return def;
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_CREATE)
	public DBLookupTable createNewVersion(Application app, User u, Long lookupTableId, MFDataSetDefinition ddl) {
		DBLookupTable lookupTable = findById(lookupTableId);
		if (lookupTable == null) {
			throw new IllegalArgumentException("Can't create a new lookup version. The lookup table " + lookupTableId
					+ " doesn't exists");
		}
		MFDataSetDefinition storedDDL = dataAccess.addDefinition(lookupTable.getDataSetDefinition(), ddl);
		DBLookupTable newTable = new DBLookupTable();
		newTable.setDefaultLanguage(lookupTable.getDefaultLanguage());
		newTable.setApplication(app);
		newTable.setDataSetDefinition(storedDDL.getMetaDataRef());
		newTable.setDatasetVersion(storedDDL.getVersion());
		DBLookupTable savedTable = save(u, newTable);
		return savedTable;
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_EDIT)
	public MFOperationResult insertData(Application app, Long lookupTableId, List<? extends MFIncomingDataI> rows,
			boolean fastFail, boolean failOnDuplicates) throws InterruptedException {
		DBLookupTable lookupTable = findById(lookupTableId);
		if (lookupTable == null) {
			throw new IllegalArgumentException("The lookup table " + lookupTableId + " doesn't exists");
		}
		StoreResult storedData = dataAccess.storeData(lookupTable.getDataSetDefinition(),
				lookupTable.getDatasetVersion(), rows, fastFail, failOnDuplicates);
		return storedData.getMfOperationResult();
	}

	public MFOperationResult insertData(Application app, Long lookupTableId, List<? extends MFIncomingDataI> rows,
			boolean fastFail) throws InterruptedException {
		return insertData(app, lookupTableId, rows, fastFail, true);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_READ)
	public List<MFManagedData> listAllData(Application app, Long lookupTableId) {
		if (lookupTableId == null) {
			throw new IllegalArgumentException("The lookup table " + lookupTableId + " doesn't exists");
		}
		DBLookupTable lookupTable = findById(lookupTableId);
		if (lookupTable == null) {
			throw new IllegalArgumentException("The lookup table " + lookupTableId + " doesn't exists");
		}
		List<MFManagedData> list = dataAccess.listAllData(lookupTable.getDataSetDefinition(),
				lookupTable.getDatasetVersion(), null);
		return list;
	}

	@Override
	public List<LookupTableDTO> listAvailableLookupTables(Application app) {
		PagedData<List<LookupTableDTO>> lookups = findAvailableLookupTables(app, null, null, false, null, null);
		return lookups.getData();
	}

	@Override
	public List<LookupTableDTO> listAvailableLookupTables(Application app, String identifier) {
		PagedData<List<LookupTableDTO>> lookups = findAvailableLookupTables(app, identifier, null, false, null, null);
		return lookups.getData();
	}

	@Override
	public PagedData<List<LookupTableDTO>> findAvailableLookupTables(Application app, String identifier,
			String orderBy, boolean ascending, Integer page, Integer pageSize) {
		String identifierWhere = "";
		if (identifier != null) {
			identifierWhere = " and upper(A.identifier)=:identifier ";
		}
		String baseQuery = " from " + DBLookupTable.class.getName()
				+ " A where A.deleted=false and A.source=:source and A.application=:app " + identifierWhere;
		String orderBySQL = "";
		if (orderBy != null) {
			if (Arrays.binarySearch(lookupSortableColumns, orderBy) < 0) {
				throw new IllegalArgumentException("Can't sort columns by '" + orderBy + "'");
			}
			orderBySQL = " order by A." + orderBy + " " + ((ascending) ? "asc" : "desc");
		}
		TypedQuery<DBLookupTable> query = em.createQuery("Select A  " + baseQuery + orderBySQL, DBLookupTable.class);

		query.setParameter("source", OptionSource.LOOKUP_TABLE);
		query.setParameter("app", app);
		if (identifier != null) {
			query.setParameter("identifier", identifier.toUpperCase());
		}
		if (page != null) {
			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);
		}
		List<DBLookupTable> lookups = query.getResultList();
		List<LookupTableDTO> dtoList = new ArrayList<LookupTableDTO>();
		for (DBLookupTable dbLookupTable : lookups) {
			dtoList.add(dbLookupTable.toDTO());
		}

		// Count the total number of lookups
		Query countQuery = em.createQuery("Select count(A) " + baseQuery);
		countQuery.setParameter("source", OptionSource.LOOKUP_TABLE);
		countQuery.setParameter("app", app);
		if (identifier != null) {
			countQuery.setParameter("identifier", identifier.toUpperCase());
		}
		Long totalCount = (Long) countQuery.getSingleResult();
		return new PagedData<List<LookupTableDTO>>(dtoList, totalCount, page, pageSize, lookups.size());
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_READ)
	public List<MFManagedData> listData(Application app, Long lookupTableId, ConditionalCriteria restriction) {
		DBLookupTable lookupTable = findById(lookupTableId);
		if (lookupTable == null) {
			throw new IllegalArgumentException("The lookup table " + lookupTableId + " doesn't exists");
		}
		return dataAccess.listData(lookupTable.getDataSetDefinition(), lookupTable.getDatasetVersion(), restriction,
				null);

	}

	private DBLookupTable checkLookupAccess(Application app, Long lookupId) {
		DBLookupTable lookupTable = findById(lookupId);
		if (lookupTable == null) {
			throw new IllegalArgumentException("The lookup table " + lookupId + " doesn't exists");
		}
		if (!lookupTable.getApplication().getId().equals(app.getId())) {
			throw new AuthorizationException("Can't access lookups of other applications");
		}
		return lookupTable;
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_READ)
	public List<MFManagedData> listData(Application app, Long lookupTableId, String column, String value) {

		DBLookupTable lookupTable = checkLookupAccess(app, lookupTableId);

		// Find out the data type of the column, in order to use appropriate
		// casting. Otherwise, the value might not be found on the table. For
		// example, searching an integer column with a string operand will
		// return an empty sets
		MFDataSetDefinitionMongo def = dataAccess.getDataSetDefinition(lookupTable.getDataSetDefinition(),
				lookupTable.getDatasetVersion());
		Map<String, MFField> fieldsMap = def.fieldsMappedByName();
		MFField fieldDef = fieldsMap.get(column);
		if (fieldDef == null) {
			throw new ApplicationException("The column '" + column + "' is not defined for the lookuptable "
					+ lookupTableId);
		}
		Object valueCasted = value;
		if (fieldDef.getType() == FIELD_TYPE.NUMBER) {
			try {
				valueCasted = MFDataHelper.unserialize(FIELD_TYPE.NUMBER, value);
			} catch (IllegalValueException e) {
				throw new ApplicationException("Value '" + value + "' can't be used to compare against the column "
						+ fieldDef.getColumnName() + " of the lookuptable #" + lookupTableId);
			}
		}
		// else. The other types will be compared by String.

		ConditionalCriteria restriction = new ConditionalCriteria(CONDITION_TYPE.AND);
		restriction.add(new Criteria(column, MFRestriction.OPERATOR.EQUALS, valueCasted));
		return listData(app, lookupTableId, restriction);
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_READ)
	public PagedData<List<MFManagedData>> listData(Application app, Long lookupTableId,
			ConditionalCriteria restriction, int pageNumber, int pageSize, String orderBy, boolean ascending) {

		DBLookupTable lookupTable = checkLookupAccess(app, lookupTableId);
		return dataAccess.listData(lookupTable.getDataSetDefinition(), lookupTable.getDatasetVersion(), restriction,
				new OrderBy(orderBy, ascending), pageNumber, pageSize);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public List<MFField> listFields(Long lutId, String language) {
		MFDataSetDefinition lookupTableDefinition = getLookupTableDefinition(lutId);
		List<MFField> fields = lookupTableDefinition.getFields();
		return fields;
	}

	@Override
	@Authorizable(value = AuthorizationNames.App.LOOKUP_EDIT)
	public DBLookupTable deleteLookupTable(Long lutId) throws LookuptableOperationException {
		List<Long> forms = formService.getFormsUsingTheLookupTable(lutId);
		if (forms != null && forms.size() > 0) {
			throw new LookuptableOperationException(LookuptableOperationException.LOOKUP_IN_USE, forms);
		}
		return logicalDelete(lutId);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MFBlob getFile(MFBlob blob) {
		return dataAccess.getFile(blob);
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MFFileStream getFileLazy(MFBlob blob) {
		return dataAccess.getFileLazy(blob);
	}

	@Override
	public MFOperationResult updateData(Application app, Long lookupTableId, MFIncomingDataI row,
			ConditionalCriteria selector) {
		DBLookupTable lookupTable = checkLookupAccess(app, lookupTableId);
		return dataAccess.update(lookupTable.getDataSetDefinition(), lookupTable.getDatasetVersion(), row, selector);

	}

	@Override
	public MFOperationResult updateOrInsertData(Application app, Long lookupTableId, MFIncomingDataI row,
			boolean fastFail) throws InterruptedException {
		MFLoookupTableDefinition lookup = getLookupTableDefinition(lookupTableId);
		List<MFField> fields = lookup.getFields();
		// Build a conditional criteria based on the primary key
		ConditionalCriteria selector = new ConditionalCriteria(CONDITION_TYPE.AND);
		for (MFField f : fields) {
			if (f.isPk()) {
				Object value = row.getData().get(f.getColumnName());
				selector.add(new Criteria(f.getColumnName(), OPERATOR.EQUALS, value));
			}
		}

		MFOperationResult result = updateData(app, lookupTableId, row, selector);
		if (result.hasSucceeded()) {
			if (result.getNumberOfAffectedRows() <= 0) {
				// The row didn't exist before so we need to insert it
				ArrayList<MFIncomingDataI> rows = new ArrayList<MFIncomingDataI>();
				rows.add(row);
				return insertData(app, lookupTableId, rows, fastFail, false);
			}
		}
		return result;

	}

	@Override
	public MFOperationResult deleteData(Application app, Long lookupTableId, ConditionalCriteria selector) {
		DBLookupTable lookupTable = checkLookupAccess(app, lookupTableId);
		return dataAccess.delete(lookupTable.getDataSetDefinition(), lookupTable.getDatasetVersion(), selector);
	}

}
