package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.ui.multiselect.IMultiselectService;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectActionRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectItem;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.Column;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.LEFT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.RIGHT_IMAGE;

@Service("multiselect.applications")
@Transactional
public class ApplicationsMultiselect implements IMultiselectService {

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	private I18nBundle i18nService;

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {

		User user = request.getUser();
		if (!user.isRootUser()) {
			throw new AuthorizationException("Not a system administrator");
		}

		String language = request.getLanguage();

		int pageNumberRequest = request.getPageNumber();
		int pageLengthRequest = request.getPageLength();
		String filter = request.getFilter();
		Map<String, String> params = request.getParams();
		Boolean onlyActive = true;
		if (params != null && !params.isEmpty()) {
			onlyActive = Boolean.valueOf(params.get(Application.ACTIVE));
		}
		
		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		
		int pageNumber = pageNumberRequest;
		int pageLength = pageLengthRequest;
		if (onlyActive) {
			pageNumber = 1;
			pageLength = 999;
		}

		PagedData<List<Application>> pagedData = applicationService.findByLabel(filter, pageNumber, pageLength,
				language);

		List<Application> applications = pagedData.getData();
		
		int activeAppCount = 0;
		int itemsActiveToReturn = 0;
		int firstResult = (pageNumberRequest - 1) * pageLengthRequest;
		for (Application a : applications) {
			MultiselectItem item = new MultiselectItem();
			item.setId(a.getId());
			item.setLabel(a.getName());
			Boolean appActive = a.getActive();
			item.setActive(appActive);
			if (onlyActive && appActive) {
				if (activeAppCount == firstResult) {
					itemsActiveToReturn++;
					if (itemsActiveToReturn > pageLengthRequest) {
						break;
					}
					items.add(item);
				} else {
					activeAppCount++;
				}
			} else if (!onlyActive) {
				items.add(item);
			}
			
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
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label = i18nService.getLabel(language, "web.generic.applications");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
