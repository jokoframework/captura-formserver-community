package py.com.sodep.mobileforms.api.services.workflow;

import java.util.List;

import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;

public interface IStateDataAccess {

	StoreResult storeData(String metadataRef, Long version, List<? extends MFIncomingDataI> rows) throws InterruptedException;

	List<MFManagedData> listData(String testMetadataRef, Long version, Long stateId);

	List<MFManagedData> listData(String metadataRef, Long version, Long stateId, ConditionalCriteria filter,
			OrderBy orderBy);

}
