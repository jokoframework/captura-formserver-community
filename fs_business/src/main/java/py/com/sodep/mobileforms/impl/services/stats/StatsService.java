package py.com.sodep.mobileforms.impl.services.stats;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.dtos.stats.DataUsage;
import py.com.sodep.mobileforms.api.dtos.stats.FailedDocument;
import py.com.sodep.mobileforms.api.dtos.stats.UsageStats;
import py.com.sodep.mobileforms.api.dtos.stats.UsageStats.DocumentCount;
import py.com.sodep.mobileforms.api.dtos.stats.UsageStats.LoginInfo;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.stats.IStatsService;

@Service
public class StatsService implements IStatsService {

	@Autowired
	private IApplicationService appService;

	@Autowired
	private IUserService userService;

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Override
	public UsageStats getAppUsageStats(Long appId) {
		Application app = appService.findById(appId);
		if (app == null) {
			return null;
		}
		UsageStats stats = new UsageStats();
		stats.setApplicationId(appId);
		stats.setApplicationName(app.getName());

		Long projectCount = getProjectCount(appId);
		stats.setProjectCount(projectCount);

		Long formCount = getFormCount(appId);
		stats.setFormCount(formCount);

		Long userCount = getUserCount(appId);
		stats.setUserCount(userCount);

		List<LoginInfo> loginInfo = getLastLogins(appId);
		stats.setLoginInfo(loginInfo);

		List<DocumentCount> documentCount = getDocumentCount(appId);
		stats.setDocumentCount(documentCount);

        stats.setApplicationActive(app.getActive());

		return stats;
	}

	@Override
	public List<DataUsage> getAllAppsDataUsage(Boolean search, String searchValue, Integer page, Integer rows, String orderBy, String order) {
		List<DataUsage> stats = new ArrayList<DataUsage>();
		boolean ascending = true;
		boolean orderByUploadedData = false;
		if (order != null) {
			ascending = order.equalsIgnoreCase("asc");
		}
		if (rows < 0) {
			rows = 300;		//FIXME CAP-285 MaxResults for now 
		}
		if (orderBy == null || orderBy.trim().isEmpty()) {
			orderBy = SodepEntity.ID;	// CAP-285 orderBy by id (by default)
		} else if (!orderBy.trim().isEmpty() && orderBy.equals("uploadedData")) {
			orderBy = SodepEntity.ID;
			orderByUploadedData = true;
		}
		PagedData<List<Application>> apps = null;
		if (!search || (search && searchValue.isEmpty())) {
			apps = appService.findAll(orderBy, ascending, page, rows);
		} else {
			apps = appService.findByProperty("name", "cn", searchValue, orderBy, ascending, page, rows);
		}
		for (Application app : apps.getData()) {
			DataUsage dataUsage = new DataUsage();
			stats.add(dataUsage);
			Query query = em.createNativeQuery(getByteCountQueryStr());
			query.setParameter("appId", app.getId());
			BigDecimal byteResult = (BigDecimal) query.getSingleResult();
			Long byteCount = (Long) byteResult.longValue();
			dataUsage.setAppId(app.getId());
			dataUsage.setAppName(app.getName());
			dataUsage.setActive(!app.getDeleted() && (app.getActive() != null && app.getActive()));
			dataUsage.setUploadedData(byteCount);
		}
		
		if (orderByUploadedData) {
			Collections.sort(stats);
			if (!ascending) {
				Collections.reverse(stats);
			}
		}
		
		return stats;
	}

	private List<DocumentCount> getDocumentCount(Long appId) {
		int[] days = new int[] { 1, 7, 30 };
		List<DocumentCount> ret = new ArrayList<DocumentCount>();
		for (int n : days) {
			Query documentCountQuery = em.createNativeQuery(getDocumentCountQueryStr(n));
			documentCountQuery.setParameter("appId", appId);
			BigInteger documentResult = (BigInteger) documentCountQuery.getSingleResult();
			Long documentCount = documentResult.longValue();

			Query byteCountQuery = em.createNativeQuery(getByteCountQueryStr(n));
			byteCountQuery.setParameter("appId", appId);
			BigDecimal byteResult = (BigDecimal) byteCountQuery.getSingleResult();
			Long byteCount = (Long) byteResult.longValue();

			DocumentCount count = new DocumentCount();
			count.setDays(n);
			count.setByteCount(byteCount);
			count.setDocumentCount(documentCount);
			ret.add(count);
		}
		return ret;
	}

	private String getDocumentCountQueryStr(int days) {
		final String query = "SELECT count(*) FROM mf_data.uploads "
				+ "WHERE saved_at > (now() - INTERVAL '%d DAY') AND application_id=:appId";
		return String.format(query, days);
	}

	private String getByteCountQueryStr(int days) {
		final String query = "SELECT COALESCE(SUM(size), 0) FROM mf_data.uploads "
				+ "WHERE saved_at > (now() - INTERVAL '%d DAY') AND application_id=:appId";
		return String.format(query, days);
	}

	private String getByteCountQueryStr() {
		return "SELECT COALESCE(SUM(size), 0) FROM mf_data.uploads " + "WHERE application_id=:appId";
	}

	@SuppressWarnings("rawtypes")
	private List<LoginInfo> getLastLogins(Long appId) {
		Query query = em.createNativeQuery("SELECT u.mail, l.login_at, l.login_type_value FROM log.logins l "
				+ "JOIN core.users u ON l.user_id = u.id " + "WHERE l.application_id = :appId AND u.mail IS NOT NULL "
				+ "ORDER BY login_at DESC");
		query.setMaxResults(10);
		query.setParameter("appId", appId);
		List list = query.getResultList();
		List<LoginInfo> logins = new ArrayList<LoginInfo>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (Object o : list) {
			Object[] row = (Object[]) o;
			LoginInfo log = new LoginInfo();
			log.setMail(row[0].toString());
			java.sql.Timestamp d = (java.sql.Timestamp) row[1];
			log.setTime(formatter.format(d));
			log.setLoginType(row[2].toString());
			logins.add(log);
		}
		return logins;
	}

	private Long getUserCount(Long appId) {
		Query query = em.createNativeQuery("SELECT count(*) FROM core.users u "
				+ " JOIN applications.application_users au ON u.id = au.user_id "
				+ " JOIN applications.applications a ON au.application_id = a.id "
				+ " WHERE au.status = 0 AND a.id=:appId AND u.mail IS NOT NULL");
		query.setParameter("appId", appId);
		BigInteger result = (BigInteger) query.getSingleResult();
		return result.longValue();
	}

	private Long getProjectCount(Long appId) {
		Query query = em.createNativeQuery("SELECT count(*) FROM " + " projects.projects p "
				+ " WHERE p.deleted=false AND p.application_id = :appId");
		query.setParameter("appId", appId);
		BigInteger result = (BigInteger) query.getSingleResult();
		return result.longValue();
	}

	private Long getFormCount(Long appId) {
		Query query = em.createNativeQuery("SELECT count(DISTINCT f.root_id) FROM forms.forms f JOIN "
				+ " projects.projects p ON f.project_id = p.id "
				+ " WHERE f.deleted=false AND p.deleted=false AND p.application_id = :appId");
		query.setParameter("appId", appId);
		BigInteger result = (BigInteger) query.getSingleResult();
		return result.longValue();
	}

	@Override
	public List<FailedDocument> getFailedDocuments(int lastNDays) {
		final String queryTemplate = "SELECT d.id, u.mail, a.name, d.error_desc, d.created_at FROM mf_data.uploads d "
				+ "JOIN applications.applications a ON d.application_id = a.id "
				+ "JOIN core.users u ON d.user_id = u.id " + "WHERE d.status='FAIL' AND d.created_at > (now() - INTERVAL '%d DAY') "
				+ "ORDER BY d.created_at DESC";
		Query query = em.createNativeQuery(String.format(queryTemplate, lastNDays));
		
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		List<FailedDocument> fails = new ArrayList<FailedDocument>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (Object o : list) {
			Object[] row = (Object[]) o;
			FailedDocument failed = new FailedDocument();
			fails.add(failed);
			failed.setId(row[0].toString());
            if (row[1] != null) {
                // mail can be null. See #CAP-113
                failed.setUserEmail(row[1].toString());
            }
			failed.setApplicationName(row[2].toString());
			if (row[3] != null) {
				failed.setErrorDescription(row[3].toString());
			}
			java.sql.Timestamp d = (java.sql.Timestamp) row[4];
			failed.setCreatedAt(formatter.format(d));
		}

		return fails;
	}

}
