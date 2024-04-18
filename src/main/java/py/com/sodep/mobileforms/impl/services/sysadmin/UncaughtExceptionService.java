package py.com.sodep.mobileforms.impl.services.sysadmin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.sysadmin.IUncaughtExceptionService;

@Service("UncaughtExceptionService")
@Transactional
public class UncaughtExceptionService implements IUncaughtExceptionService {

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Override
	public PagedData<List<UncaughtException>> findAll(String orderBy, boolean ascending, Integer pageNumber,
			Integer pageSize) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UncaughtException> selectCriteria = builder.createQuery(UncaughtException.class);

		Root<UncaughtException> root = selectCriteria.from(UncaughtException.class);

		if (ascending) {
			selectCriteria.orderBy(builder.asc(root.get(orderBy)));
		} else {
			selectCriteria.orderBy(builder.desc(root.get(orderBy)));
		}

		TypedQuery<UncaughtException> selectQuery = em.createQuery(selectCriteria);

		selectQuery.setMaxResults(pageSize);
		selectQuery.setFirstResult((pageNumber - 1) * pageSize);
		List<UncaughtException> data = selectQuery.getResultList();

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		countCriteria.select(builder.count(countCriteria.from(UncaughtException.class)));

		TypedQuery<Long> countQuery = em.createQuery(countCriteria);

		Long count = (Long) countQuery.getSingleResult();

		PagedData<List<UncaughtException>> pdUnEx = new PagedData<List<UncaughtException>>(data, count, pageNumber,
				pageSize, data.size());

		return pdUnEx;
	}

	@Override
	public void clean() {
		Query query = em.createQuery("Delete from " + UncaughtException.class.getName() + " A ");
		query.executeUpdate();

	}

}
