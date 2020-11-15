package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
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

@Service("multiselect.groups")
@Transactional
public class GroupMultiselect extends BaseMultiselectService implements IMultiselectService {
	
	@Autowired
	protected IGroupService groupService;

	@Autowired
	protected IUserService userService;
	
	@Autowired
	protected I18nBundle i18nService;
	
	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		Application app = request.getApplication();
		
		int pageNumber = request.getPageNumber();
		int pageLength = request.getPageLength();
		String filter = request.getFilter();
		User user = getFilterUser(request);
		
		List<Group> data = getData(app, pageNumber, pageLength, filter, user);
		List<MultiselectItem> items = listItems(data);

		MultiselectServiceResponse response = new MultiselectServiceResponse();
		response.setSuccess(true);
		response.setItems(items);
		return response;
	}

	protected List<Group> getData(Application app, int pageNumber, int pageLength, String filter, User user) {
		PagedData<List<Group>> pagedData = groupService.findByMemberAndName(app, user, true, filter, pageNumber, pageLength);
		return pagedData.getData();
	}

	private List<MultiselectItem> listItems(List<Group> data) {
		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (Group g : data) {
			MultiselectItem item = new MultiselectItem();
			item.setId(g.getId());
			item.setLabel(g.getName());
			items.add(item);
		}
		return items;
	}

	private User getFilterUser(MultiselectReadRequest request) {
		Map<String, String> params = request.getParams();
		String userId = null;
		if (params != null) {
			userId = request.getParams().get("userId");
		}

		User user = null;
		if (userId != null) {
			user = userService.findById(Long.parseLong(userId));
		}
		return user;
	}

	@Override
	public MultiselectServiceResponse doAction(MultiselectActionRequest request) {
		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		if (params != null && params.get("userId") != null) {
			String label = i18nService.getLabel(language, "web.generic.groupsContainingUser");
			model.setLabel(label);
		} else if (params != null) {
		
		}else{
			String label = i18nService.getLabel(language, "web.generic.groups");
			model.setLabel(label);
		}
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
