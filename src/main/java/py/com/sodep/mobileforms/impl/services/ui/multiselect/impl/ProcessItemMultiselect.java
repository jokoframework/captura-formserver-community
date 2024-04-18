package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.elements.IElementPrototypeService;
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

@Service("multiselect.processItems")
@Transactional
public class ProcessItemMultiselect extends BaseMultiselectService implements IMultiselectService {

	@Autowired
	private IElementPrototypeService elementPrototypeService;

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
		String auth = AuthorizationNames.Pool.READ;

		PagedData<List<ElementPrototype>> pagedData = null;
		if (params != null && params.get("pool") != null) {
			String poolParam = params.get("pool");
			Long poolId = Long.parseLong(poolParam);
			Pool pool = poolService.findById(poolId);

			if (filter != null && !filter.trim().isEmpty()) {
				pagedData = elementPrototypeService.findByLabel(user, pool, auth, filter, pageNumber, pageLength,language);
			} else {
				pagedData = elementPrototypeService.findAll(language,user, pool, auth,pageNumber, pageLength);
			}
		} else if (params != null && params.get("excludePool") != null) {
			String poolParam = params.get("excludePool");
			Long poolId = Long.parseLong(poolParam);
			Pool pool = poolService.findById(poolId);

			if (filter != null && !filter.trim().isEmpty()) {
				pagedData = elementPrototypeService.findByLabelNotInPool(user,app, pool, auth, filter, pageNumber, pageLength,language);
			} else {
				pagedData = elementPrototypeService.findAllNotInPool(language,user,app, pool, auth,pageNumber, pageLength);
			}
		} else {
			if (filter != null && !filter.trim().isEmpty()) {
				pagedData = elementPrototypeService.findByLabel(user, app, auth, filter, pageNumber, pageLength,language);
			} else {
				pagedData = elementPrototypeService.findAll(language,user, app, auth, pageNumber, pageLength);
			}
		}

		List<ElementPrototype> data = pagedData.getData();
		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (ElementPrototype p : data) {
			MultiselectItem item = new MultiselectItem();
			item.setId(p.getRoot().getId());
			item.setLabel(p.getLabel(language));
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
	public MultiselectModel loadModel(String language,Map<String,String> params) {
		MultiselectModel model = new MultiselectModel();
		String label=i18nService.getLabel(language, "web.generic.processItems");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
