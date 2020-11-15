package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.PoolDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.pools.IPoolService;
import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectService;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectActionRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectItem;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.Column;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.LEFT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.RIGHT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;
import py.com.sodep.mobileforms.impl.services.ui.multiselect.BaseMultiselectService;

@Service("multiselect.pools")
@Transactional
public class PoolMultiselect extends BaseMultiselectService implements IMultiselectService {

	@Autowired
	private IPoolService poolService;
	
	@Autowired
	private I18nBundle i18nService;

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		Application app = request.getApplication();
		User user = request.getUser();
		String language = request.getLanguage();

		int pageNumber = request.getPageNumber();
		int pageLength = request.getPageLength();
		String filter = request.getFilter();
		Map<String, String> params = request.getParams();

		PagedData<List<PoolDTO>> pagedData = null;
		String auth = AuthorizationNames.Pool.READ;
		Pool poolToExlcude = null;
		if (params != null && params.get("excludePool") != null) {
			String poolParam = params.get("excludePool");
			Long poolId = Long.parseLong(poolParam);
			poolToExlcude = poolService.findById(poolId);
		}	
		
		pagedData = poolService.findByLabelExcludePool(user, app, poolToExlcude, auth, filter, pageNumber, pageLength,language);
		

		List<PoolDTO> data = pagedData.getData();

		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (PoolDTO dto : data) {
			MultiselectItem item = new MultiselectItem();
			item.setId(dto.getId());
			item.setLabel(dto.getLabel());
			if (dto.getDescription() != null) {
				item.setTooltip(dto.getLabel() + " - " + dto.getDescription());
			} else {
				item.setTooltip(dto.getLabel());
			}
			items.add(item);
		}

		MultiselectServiceResponse response = new MultiselectServiceResponse();
		response.setSuccess(true);
		response.setItems(items);
		return response;
	}

	@Override
	public MultiselectServiceResponse doAction(MultiselectActionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label=i18nService.getLabel(language, "web.generic.pools");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
