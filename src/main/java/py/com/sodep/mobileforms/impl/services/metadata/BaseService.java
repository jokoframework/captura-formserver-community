package py.com.sodep.mobileforms.impl.services.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.utils.BeanUtils;

@Transactional
public abstract class BaseService<T extends SodepEntity> {

	protected static final String OPER_EQUALS = "eq";
	protected static final String OPER_NOT_EQUALS = "ne";
	protected static final String OPER_CONTAINS = "cn";
	protected static final String OPER_NOT_CONTAINS = "nc";
	protected static final String OPER_GREATER_THAN = "gt";
	protected static final String OPER_LESS_THAN = "lt";

	private static final Logger logger = LoggerFactory.getLogger(BaseService.class);

	private static final Class<?> sodepEntityClass = SodepEntity.class;

	private static final Class<?> listClass = List.class;

	// private final BeanInfo beanInfo;

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	protected String entityName;

	protected Class<T> clazz;

	protected BaseService(Class<T> clazz) {
		this.clazz = clazz;
		this.entityName = clazz.getSimpleName();
		// try {
		// beanInfo = Introspector.getBeanInfo(clazz);
		// } catch (IntrospectionException e) {
		// throw new RuntimeException(e);
		// }
	}

	public T findById(Application app, Object id) {
		String query = "SELECT model FROM "
				+ entityName
				+ " model WHERE model.id = :id AND model.deleted = false AND (model.application = :app OR model.application IS NULL)";
		TypedQuery<T> q = em.createQuery(query, clazz);
		q.setParameter("id", id);
		q.setParameter("app", app);
		try {
			T res = q.getSingleResult();
			return res;
		} catch (NoResultException e) {
			return null;
		}
	}

	public final T findById(Object id) {
		try {
			T instance = em.find(clazz, id);
			return instance;
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	public T save(User user, T entity) {
		return this.save(entity);
	}

	public T save(T entity) {
		Long id = entity.getId();
		boolean isNew = id == null || id == 0;

		if (isNew) {
			em.persist(entity);
			return entity;
		} else {
			T savedEntity = em.find(clazz, id);
			if (savedEntity != null) {
				BeanUtils.mapBean(entity, savedEntity);
			} else {
				InvalidEntityException e = new InvalidEntityException();
				throw e;
			}
			return savedEntity;
		}
	}

	public boolean delete(User user, T entity) {
		return this.delete(entity);
	}

	public T logicalDelete(T entity) {
		return logicalDelete(entity.getId());
	}

	public T logicalDelete(Long id) {
		T e = em.find(clazz, id);
		logicalDeleteRecursion(e);
		return e;
	}

	private void logicalDeleteRecursion(SodepEntity e) {
		logger.debug("Logical delete: " + e.getClass().getSimpleName() + " " + e.getId());
		e.setDeleted(true);
		Method[] methods = e.getClass().getMethods();
		for (Method method : methods) {
			try {
				if (method.getName().startsWith("get")) {
					LogicalDelete annotation = method.getAnnotation(LogicalDelete.class);
					if (annotation != null) {
						Class<?> propertyClass = method.getReturnType();
						if (sodepEntityClass.isAssignableFrom(propertyClass)) {
							Object object = method.invoke(e, new Object[0]);
							SodepEntity sodepEntity = (SodepEntity) object;
							// to avoid recursion over the same object e.g. form.getRoot(); 
							if (sodepEntity.getClass() != e.getClass()
									|| (sodepEntity.getClass() == e.getClass() && !e.getId().equals(sodepEntity.getId()))) {
								logger.debug(method.getName());
								logicalDeleteRecursion(sodepEntity);
							}
						} else if (listClass.isAssignableFrom(propertyClass)) {
							List<?> list = (List<?>) method.invoke(e, new Object[0]);
							if (list != null && !list.isEmpty()) {
								Object o = list.get(0);
								if (o instanceof SodepEntity) {
									logger.debug(method.getName());
									for (Object sodepEntity : list) {
										// avoid recursion over same object?
										logicalDeleteRecursion((SodepEntity) sodepEntity);
									}
								} else {
									throw new RuntimeException("field " + method.getName()
											+ " annotated with logical delete isn't a valid type");
								}
							}

						} else {
							throw new RuntimeException("field " + method.getName()
									+ " annotated with logical delete isn't a valid type");
						}
					}
				}
			} catch (IllegalArgumentException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			} catch (SecurityException e1) {
			}
		}
	}

	public boolean delete(T entity) {
		return delete(clazz, entity.getId());
	}

	protected boolean delete(Class<?> c, Long id) {
		Object o = em.find(c, id);
		if (o != null) {
			em.remove(o);
			return true;
		}
		return false;
	}

	// FIND AND LIST METHODS
	// There are two possible ways to access data, with a findXXX method or with
	// listXXX. The former receives parameters to implement the pagination
	// while the later returns all available data.
	// There are several findXX and listXXX method, but there are all simplified
	// versions of the method find and list respectively

	public List<T> findAll() {
		final String queryString = "SELECT model FROM " + entityName + " model WHERE model.deleted=false";
		TypedQuery<T> query = em.createQuery(queryString, clazz);
		return new ArrayList<T>(query.getResultList());
	}

	/**
	 * This is a key method that can query a {@link SodepEntity} .
	 * 
	 * @param app
	 *            If this is set to null it will return objects from all
	 *            applications (won't use application_id field). Otherwise only
	 *            objects that belong to a given application will be returned.
	 *            Please note that this field only can be used with objects that
	 *            has the field "application_id"
	 * @param propertyName
	 *            If this is set to different than null it must be a field of
	 *            the class otherwise an exception will be thrown. If this field
	 *            is null, then it means that no constraint is desired
	 * @param oper
	 *            It can only be used if propertyName is different than null
	 * @param value
	 * @param orderBy
	 * @param ascending
	 * @param pageNumber
	 *            The Initial pageNumber should be 1
	 * @param pageSize
	 * @param includeSystemObjects
	 *            If the parameter app is not null, then this field can be used
	 *            to include the objects that do not belong to any application
	 *            (application==true). If the parameter app is null then this
	 *            doesn't have any effect
	 * @param allowedIds
	 *            only include objects that have the id on this set. If it is
	 *            set to null it won't be used
	 * @return
	 */
	public PagedData<List<T>> find(Application app, String propertyName, String oper, Object value, String orderBy,
			boolean ascending, int pageNumber, int pageSize, boolean includeSystemObjects, List<Long> allowedIds) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> selectCriteria = builder.createQuery(clazz);

		Root<T> root = selectCriteria.from(clazz);

		// Creates the where clause that will be used on the querys
		Predicate where = computeWhereClause(app, propertyName, oper, includeSystemObjects, allowedIds, builder,
				selectCriteria, root);
		selectCriteria.where(where);
		if (ascending) {
			selectCriteria.orderBy(builder.asc(root.get(orderBy)));
		} else {
			selectCriteria.orderBy(builder.desc(root.get(orderBy)));
		}

		// Obtain the data from the DB
		TypedQuery<T> selectQuery = em.createQuery(selectCriteria);
		if (propertyName != null) {
			if (value instanceof String && (oper.equals(OPER_CONTAINS) || oper.equals(OPER_NOT_CONTAINS))) {
				selectQuery.setParameter(propertyName, ((String) value).toLowerCase());
			} else {
				selectQuery.setParameter(propertyName, value);
			}
		}

		selectQuery.setMaxResults(pageSize);
		selectQuery.setFirstResult((pageNumber - 1) * pageSize);
		List<T> data = selectQuery.getResultList();

		// computes the total number of rows
		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		countCriteria.select(builder.count(countCriteria.from(clazz)));
		countCriteria.where(where);

		TypedQuery<Long> countQuery = em.createQuery(countCriteria);
		if (propertyName != null) {
			countQuery.setParameter(propertyName, value);
		}
		Long count = (Long) countQuery.getSingleResult();

		PagedData<List<T>> ret = new PagedData<List<T>>(data, count, pageNumber, pageSize, data.size());
		return ret;
	}

	public List<T> list(Application app, String propertyName, String oper, Object value, String orderBy,
			boolean ascending, boolean includeSystemObjects, List<Long> allowedIds) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> selectCriteria = builder.createQuery(clazz);

		Root<T> root = selectCriteria.from(clazz);

		// Creates the where clause that will be used on the queries
		Predicate where = computeWhereClause(app, propertyName, oper, includeSystemObjects, allowedIds, builder,
				selectCriteria, root);
		selectCriteria.where(where);
		if (ascending) {
			selectCriteria.orderBy(builder.asc(root.get(orderBy)));
		} else {
			selectCriteria.orderBy(builder.desc(root.get(orderBy)));
		}

		// Obtain the data from the DB
		TypedQuery<T> selectQuery = em.createQuery(selectCriteria);
		if (propertyName != null) {
			selectQuery.setParameter(propertyName, value);
		}

		return selectQuery.getResultList();

	}

	/**
	 * Creates a where clause with the options expected by
	 * {@link #find(Application, String, String, Object, String, boolean, int, int, boolean, List)}
	 * and
	 * {@link #list(Application, String, String, Object, String, boolean, boolean, List)}
	 * 
	 * @param app
	 * @param propertyName
	 * @param oper
	 * @param includeSystemObjects
	 * @param allowedIds
	 * @param builder
	 * @param selectCriteria
	 * @param root
	 * @return
	 */
	private Predicate computeWhereClause(Application app, String propertyName, String oper,
			boolean includeSystemObjects, List<Long> allowedIds, CriteriaBuilder builder,
			CriteriaQuery<T> selectCriteria, Root<T> root) {
		Predicate predicateApps = null;
		if (app != null) {
			if (clazz.isInstance(IAppAwareEntity.class)) {
				throw new IllegalArgumentException("Can't constraint a search over " + clazz.getName()
						+ " to application because it doesn't implement " + IAppAwareEntity.class.getName());
			}
			if (includeSystemObjects) {
				// if include system object is true we should also include the
				// objects that don't belong to any specific app (app==null)
				predicateApps = builder.or(builder.isNull(root.get(IAppAwareEntity.APPLICATION)),
						builder.equal(root.get(IAppAwareEntity.APPLICATION), app));
			} else {
				predicateApps = builder.equal(root.get(IAppAwareEntity.APPLICATION), app);
			}
		}
		Predicate where;
		if (predicateApps != null) {
			where = builder.and(predicateApps, builder.isFalse(root.<Boolean> get(SodepEntity.DELETED)));
		} else {
			where = builder.and(builder.isFalse(root.<Boolean> get(SodepEntity.DELETED)));
		}

		if (propertyName != null) {
			where = builder.and(where, whereClause(root, builder, propertyName, oper));
		}
		if (allowedIds != null && !allowedIds.isEmpty()) {
			where = builder.and(where, root.get("id").in(allowedIds));
		}
		selectCriteria.where(where);
		return where;
	}

	/**
	 * Returns all objects sorted by a given field
	 * 
	 * @param orderBy
	 * @param ascending
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public PagedData<List<T>> findAll(String orderBy, boolean ascending, int pageNumber, int pageSize) {
		return this.find(null, null, null, null, orderBy, ascending, pageNumber, pageSize, true, null);
	}

	/**
	 * This is a customization of
	 * {@link #find(Application, String, String, Object, String, boolean, int, int, boolean, List)}
	 * with application==null and no constraint over the allowedIds
	 */

	public PagedData<List<T>> findByProperty(String propertyName, String oper, Object value, String orderBy,
			boolean ascending, int pageNumber, int pageSize) {
		return find(null, propertyName, oper, value, orderBy, ascending, pageNumber, pageSize, true, null);
	}

	public List<T> listAll(String orderBy, boolean asc) {
		return list(null, null, null, null, orderBy, asc, true, null);
	}

	/**
	 * List the objects whose field "propertyName" is EQUALS to value
	 */
	public List<T> listByPropertyEquals(String propertyName, Object value) {
		return list(null, propertyName, OPER_EQUALS, value, propertyName, true, true, null);
	}

	public List<T> listByPropertyEquals(String propertyName, Object value, String orderBy, boolean asc) {
		return list(null, propertyName, OPER_EQUALS, value, orderBy, asc, true, null);

	}

	public T findSingleByProperty(String propertyName, Object value) {
		List<T> objs = list(null, propertyName, OPER_EQUALS, value, propertyName, true, true, null);
		if (objs.size() <= 0) {
			return null;
		}

		if (objs.size() > 1) {
			throw new IllegalStateException("Multiple results but expected only one. Entity = " + clazz + " ,  Field ="
					+ propertyName + " , value= " + value);
		}
		return objs.get(0);
	}

	protected final Predicate whereClause(Root<T> root, CriteriaBuilder builder, final String propertyName,
			final String oper) {
		Predicate where = null;
		final String paramName = propertyName;
		if (oper.equals(OPER_EQUALS)) {
			ParameterExpression<Object> p = builder.parameter(Object.class, paramName);
			where = builder.equal(root.get(propertyName), p);
		} else if (oper.equals(OPER_NOT_EQUALS)) {
			ParameterExpression<Object> p = builder.parameter(Object.class, paramName);
			where = builder.equal(root.get(propertyName), p);
		} else if (oper.equals(OPER_LESS_THAN)) {
			ParameterExpression<Number> p = builder.parameter(Number.class, paramName);
			where = builder.lt(root.<Number> get(propertyName), p);
		} else if (oper.equals(OPER_GREATER_THAN)) {
			ParameterExpression<Number> p = builder.parameter(Number.class, paramName);
			where = builder.gt(root.<Number> get(propertyName), p);
		} else if (oper.equals(OPER_CONTAINS)) {
			ParameterExpression<String> p = builder.parameter(String.class, paramName);
			where = builder.like(builder.lower(root.<String> get(propertyName)),
					builder.concat("%", builder.concat(p, "%")));
		} else if (oper.equals(OPER_NOT_CONTAINS)) {
			ParameterExpression<String> p = builder.parameter(String.class, paramName);
			where = builder.like(builder.lower(root.<String> get(propertyName)),
					builder.concat("%", builder.concat(p, "%")));
		}

		return where;
	}

	/**
	 * Risk of SQL Injection if not used properly
	 * 
	 * @param propertyName
	 * @param oper
	 * @param value
	 * @return
	 */
	protected static String whereClause(String propertyName, String oper, Object value) {
		String whereClause = "";
		if (oper.equals(OPER_EQUALS)) {
			whereClause = "$1 = :value";
		} else if (oper.equals(OPER_NOT_EQUALS)) {
			whereClause = "$1 != :value";
		} else if (oper.equals(OPER_LESS_THAN)) {
			if (!(value instanceof Number)) {
				throw new RuntimeException("Invalid Value for operator less than");
			}
			whereClause = "$1 < :value";
		} else if (oper.equals(OPER_GREATER_THAN)) {
			if (!(value instanceof Number)) {
				throw new RuntimeException("Invalid Value for operator greater than");
			}
			whereClause = "$1 > :value";
		} else if (oper.equals(OPER_CONTAINS)) {
			if (!(value instanceof String)) {
				throw new RuntimeException("Invalid Value for operator contains");
			}
			whereClause = "lower($1) LIKE :value";
		} else if (oper.equals(OPER_NOT_CONTAINS)) {
			if (!(value instanceof String)) {
				throw new RuntimeException("Invalid Value for operator contains");
			}
			whereClause = "NOT (lower($1) LIKE :value)";
		}

		return whereClause.replace("$1", propertyName);
	}	
	
}
