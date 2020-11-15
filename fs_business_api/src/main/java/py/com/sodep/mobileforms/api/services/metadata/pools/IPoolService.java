package py.com.sodep.mobileforms.api.services.metadata.pools;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IPoolService {

	List<PoolDTO> listPools(Application app, User user, String language, String auth);

	PagedData<List<PoolDTO>> findAll(User user, Application app, String auth, int page, int pageSize, String language);

	PagedData<List<PoolDTO>> findAllExcludePool(User user, Application app, Pool poolToExclude, String auth, int page,
			int pageSize, String language);

	PagedData<List<PoolDTO>> findByLabel(User user, Application app, String auth, String label, int page, int pageSize,
			String language);

	PagedData<List<PoolDTO>> findByLabelExcludePool(User user, Application app, Pool poolToExclude, String auth,
			String label, int page, int pageSize, String language);

	PoolDTO getPool(Long id);

	Pool findById(Long poolId);
	
	String getOwnerDefaultRole();
	
	Pool edit(Long poolId, User user, PoolDTO dto);

	Pool createNew(Application app, User owner, PoolDTO dto);
	
	Pool logicalDelete(Pool pool);
}
