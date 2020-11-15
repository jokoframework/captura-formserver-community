package py.com.sodep.mobileforms.impl.services.metadata;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * The methods offered by a Service subclass of BaseService do not take into
 * account the app context. All searches, retrieval, etc. are done system-wide.
 * 
 * This class is meant to be extended by Services of entities that implement
 * IAppAwareEntity. It offers some of the methods of BaseService but taking as a
 * parameter the application in which to search, retrieve or persist.
 * 
 * @author Miguel
 * 
 * @param <T>
 */
@Transactional
public class AppAwareBaseService<T extends SodepEntity> extends BaseService<T> {

	protected AppAwareBaseService(Class<T> clazz) {
		super(clazz);
	}

	public PagedData<List<T>> findAll(Application app, String orderBy, boolean ascending, int pageNumber, int pageSize) {
		return find(app, null, null, null, orderBy, ascending, pageNumber, pageSize, false, null);
	}
	
	public PagedData<List<T>> findByProperty(Application app, final String propertyName, final String oper,
			Object value, final String orderBy, boolean ascending, int pageNumber, int pageSize) {
		return super.find(app, propertyName, oper, value, orderBy, ascending, pageNumber, pageSize, false, null);
	}

}
