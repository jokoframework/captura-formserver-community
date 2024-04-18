package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.Column;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.LEFT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.RIGHT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;

@Service("multiselect.groupsNotContainingUser")
@Transactional
public class GroupNotContainingUserMultiselect extends GroupMultiselect {

	//FIXME We must override the method to avoid an Error in the AuthorizationAspect
	// ... [py.com.sodep.mobileforms.authorization.AuthorizationAspect] ERROR - Unable to obtain method declaration to inspect it
	// java.lang.NoSuchMethodException: py.com.sodep.mobileforms.impl.services.ui.multiselect.impl.GroupNotContainingUserMultiselect.listItems(py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest)
	// Are there other possible solutions? - jmpr
	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		return super.listItems(request);
	}
	
	@Override
	protected List<Group> getData(Application app, int pageNumber, int pageLength, String filter, User user) {
		PagedData<List<Group>> pagedData = groupService.findByMemberAndName(app, user, false, filter, pageNumber,
				pageLength);
		return pagedData.getData();
	}
	
	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label = i18nService.getLabel(language, "web.generic.availableGroups");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
