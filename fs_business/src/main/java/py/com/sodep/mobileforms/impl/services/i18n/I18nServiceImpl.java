package py.com.sodep.mobileforms.impl.services.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.i18n.I18nLanguage;

/**
 * Gets the language list and labels from the database
 * 
 * @author Miguel
 * 
 */
@Service("I18nService")
@Transactional
class I18nServiceImpl {

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	private static Logger logger = LoggerFactory.getLogger(I18nServiceImpl.class);

	public Map<String, String> getLabels(String language) {
		Query q = em.createQuery("SELECT l FROM " + I18nLanguage.class.getName()
				+ " l  WHERE l.isoLanguage=:isoLanguage ");
		q.setParameter("isoLanguage", language);
		try {
			I18nLanguage l = (I18nLanguage) q.getSingleResult();
			return new HashMap<String, String>(l.getLabels());
		} catch (NoResultException e) {
			logger.error("No language for: " + language);
		}
		return Collections.emptyMap();
	}

	public Map<String, String> getLabels(String language, List<String> keys) {
		if (language == null) {
			throw new IllegalArgumentException("language can't be null");
		}
		Map<String, String> map = new HashMap<String, String>();
		if (keys != null && !keys.isEmpty()) {
			Query q = em
					.createNativeQuery("SELECT l.key AS key, l.value AS value "
							+ "FROM i18n.labels l JOIN i18n.languages i ON l.language_id=i.id WHERE key IN (:keys) AND i.iso_language = :language");
			q.setParameter("keys", keys);
			q.setParameter("language", language);
			@SuppressWarnings("unchecked")
			List<Object[]> data = q.getResultList();

			for (Object row[] : data) {
				map.put((String) row[0], (String) row[1]);
			}
		}
		return map;
	}

	public Map<String, String> listLanguages() {
		Map<String, String> languagesMap = new LinkedHashMap<String, String>();
		Query q = em.createQuery("SELECT isoLanguage, name FROM " + I18nLanguage.class.getName() + " ORDER BY name");
		@SuppressWarnings("rawtypes")
		List list = q.getResultList();
		for (Object o : list) {
			// @SuppressWarnings("unchecked")
			Object[] c = (Object[]) o;
			languagesMap.put(c[0].toString(), c[1].toString());
		}

		return languagesMap;
	}

}
