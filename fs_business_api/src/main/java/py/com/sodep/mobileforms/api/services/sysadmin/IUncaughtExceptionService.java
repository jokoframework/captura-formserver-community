package py.com.sodep.mobileforms.api.services.sysadmin;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IUncaughtExceptionService {
	PagedData<List<UncaughtException>> findAll(String orderBy, boolean ascending, Integer pageNumber, Integer pageSize);

	public void clean();
}
