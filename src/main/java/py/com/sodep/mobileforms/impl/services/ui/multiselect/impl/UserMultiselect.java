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

@Service("multiselect.users")
@Transactional
public class UserMultiselect extends BaseMultiselectService implements IMultiselectService {
	
	@Autowired
	protected IUserService userService;

	@Autowired
	protected IGroupService groupService;
	
	@Autowired
	protected I18nBundle i18nService;
	
	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		Application app = request.getApplication();
		int pageNumber = request.getPageNumber();
		int pageLength = request.getPageLength();
		String filter = request.getFilter();
		Map<String, String> params = request.getParams();
		String groupId = null;
		if(params!= null) {
			groupId = request.getParams().get("groupId");
		}
		
		List<User> data = getData(app, pageNumber, pageLength, filter, groupId);	
		List<MultiselectItem> items = listItems(data);

		MultiselectServiceResponse response = new MultiselectServiceResponse();
		response.setSuccess(true);
		response.setItems(items);
		return response;
	}

	protected List<User> getData(Application app, int pageNumber, int pageLength, String filter, String groupId) {
		PagedData<List<User>> pagedData = null;
		if(groupId != null) {
			Group group = groupService.findById(app, Long.parseLong(groupId));
			pagedData = userService.findByMail(app, group, true, filter, pageNumber, pageLength);
		} else {
			pagedData = userService.findByMail(app, filter, pageNumber, pageLength);
		}
		return pagedData.getData();
	}

	private List<MultiselectItem> listItems(List<User> data) {
		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (User u : data) {
			MultiselectItem item = new MultiselectItem();
			item.setId(u.getId());
			item.setLabel(u.getMail());
			items.add(item);
		}
		return items;
	}

	@Override
	public MultiselectServiceResponse doAction(MultiselectActionRequest request) {
		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language,Map<String,String> params) {
		MultiselectModel model = new MultiselectModel();
		if(params!= null && params.get("groupId")!=null) {
			String label=i18nService.getLabel(language, "web.generic.usersInGroup");
			model.setLabel(label);
		} else {
			String label=i18nService.getLabel(language, "web.generic.users");
			model.setLabel(label);
		}
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}
}
