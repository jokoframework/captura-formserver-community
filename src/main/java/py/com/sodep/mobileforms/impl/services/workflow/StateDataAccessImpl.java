package py.com.sodep.mobileforms.impl.services.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import py.com.sodep.mf.exchange.MFIncomingDataI;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria.CONDITION_TYPE;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR;
import py.com.sodep.mobileforms.api.services.data.IDataAccessService;
import py.com.sodep.mobileforms.api.services.data.MFStorable;
import py.com.sodep.mobileforms.api.services.data.OrderBy;
import py.com.sodep.mobileforms.api.services.data.StoreResult;
import py.com.sodep.mobileforms.api.services.workflow.IStateDataAccess;
import py.com.sodep.mobileforms.api.services.workflow.MFIncominDataWorkflow;

@Service
public class StateDataAccessImpl implements IStateDataAccess {

	private IDataAccessService dataAccess;
	
	@Autowired
	public StateDataAccessImpl(IDataAccessService dataAccess) {
		super();
		this.dataAccess = dataAccess;
	}
	
	@Override
	public StoreResult storeData(String metadataRef, Long version, List<? extends MFIncomingDataI> rows) throws InterruptedException {
		Assert.notNull(metadataRef, "Parameter metadataRef is required");
		Assert.notNull(version, "Parameter version is required");
		ensureStateParameters(rows);
		
		// 1. Guardar los datos en mongo
		return dataAccess.storeData(metadataRef, version, rows, true, true);
	}


	@Override
	public List<MFManagedData> listData(String metadataRef, Long version, Long stateId) {
		ConditionalCriteria criteria = new ConditionalCriteria(CONDITION_TYPE.AND);
		Criteria c = new Criteria(MFIncominDataWorkflow.META_FIELD_STATE_ID, OPERATOR.EQUALS, stateId);
		c.setNamespace(MFStorable.FIELD_META);
		criteria.add(c);
		
		List<MFManagedData> list = dataAccess.listData(metadataRef, version, criteria, null);
		return list;
	}

	@Override
	public List<MFManagedData> listData(String metadataRef, Long version, Long stateId, ConditionalCriteria filter, OrderBy orderBy) {
		
		Assert.notNull(filter, "Filter is required");
		
		Criteria c = new Criteria(MFIncominDataWorkflow.META_FIELD_STATE_ID, OPERATOR.EQUALS, stateId);
		c.setNamespace(MFStorable.FIELD_META);
		filter.add(c);
		
		List<MFManagedData> list = dataAccess.listData(metadataRef, version, filter, orderBy);
		return list;
	}
	
	private void ensureStateParameters(List<? extends MFIncomingDataI> rows) {
		for (MFIncomingDataI row: rows) {
			Long stateId = (Long) row.getMeta().get(MFIncominDataWorkflow.META_FIELD_STATE_ID);
			Assert.notNull(stateId, "WORKFLOW Incoming data should have STATE ID");
		}
	}

}
