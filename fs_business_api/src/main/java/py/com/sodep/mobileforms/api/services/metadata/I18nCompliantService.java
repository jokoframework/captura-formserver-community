package py.com.sodep.mobileforms.api.services.metadata;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;

public interface I18nCompliantService<T> {

	public List<T> findAll(Application app, String language);

	public PagedData<List<T>> findAll(Application app, String orderBy, boolean ascending, int pageNumber, int pageSize,
			String language);

	public PagedData<List<T>> findAll(User user, Application app, String auth, String orderBy, boolean ascending,
			int pageNumber, int pageSize, String language);

	public PagedData<List<T>> findByProperty(Application app, String propertyName, String oper, Object value,
			String orderBy, boolean ascending, int pageNumber, int pageSize, String language);

	public PagedData<List<T>> findByProperty(User user, Application app, String auth, String propertyName, String oper,
			Object value, String orderBy, boolean ascending, int pageNumber, int pageSize, String language);

}
