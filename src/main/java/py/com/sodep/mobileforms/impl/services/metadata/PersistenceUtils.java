package py.com.sodep.mobileforms.impl.services.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Service;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.services.metadata.IPersistenceUtils;

@Service("PersistenceUtils")
public class PersistenceUtils implements IPersistenceUtils {

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	public <T extends SodepEntity, V extends SodepEntity> List<V> loadList(Class<T> entityClass, Object id, String listName, Class<V> listClass) {
		TypedQuery<V> q = em.createQuery("SELECT list FROM " + entityClass.getSimpleName() + " e JOIN e." + listName
				+ " list WHERE e.id=:id", listClass);
		q.setParameter("id", id);
		return new ArrayList<V>(q.getResultList());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends SodepEntity, K, V> Map<K, V> loadMap(Class<T> entityClass, Object id, String mapName, Class<K> keyClass,
			Class<V> valueClass) {
		Query q = em.createQuery("SELECT indices(map), elements(map) FROM " + entityClass.getSimpleName() + " e JOIN e." + mapName
				+ " map WHERE e.id=:id");
		q.setParameter("id", id);
		List res = q.getResultList();
		Map<K, V> map = new LinkedHashMap<K, V>();
		for(Object obj : res){
			Object[] array =(Object[])obj;
			K key = (K)array[0];
			V value = (V)array[1];
			map.put(key, value);
		}
		return map;
	}

}
