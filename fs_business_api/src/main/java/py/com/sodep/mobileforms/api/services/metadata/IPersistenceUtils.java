package py.com.sodep.mobileforms.api.services.metadata;

import java.util.List;
import java.util.Map;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

public interface IPersistenceUtils {

	public <T extends SodepEntity, V extends SodepEntity> List<V> loadList(Class<T> entityClass, Object id,
			String listName, Class<V> listClass);

	public <T extends SodepEntity, K, V> Map<K, V> loadMap(Class<T> entityClass, Object id, String mapName,
			Class<K> keyClass, Class<V> valueClass);
}
