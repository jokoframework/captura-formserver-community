package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.Column;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.LEFT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectModel.RIGHT_IMAGE;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectReadRequest;
import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectServiceResponse;

@Service("multiselect.rolesNotContainingEntity")
@Transactional
public class RoleNotContainingEntityMultiselect extends RoleMultiselect {

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		return super.listItems(request);
	}

	@Override
	protected List<Role> getData(Application app, AuthorizableEntity authorizableEntity, int level, String filter,
			int pageNumber, int pageLength) {
		// FIXME we are not taking into account the size of the page
		// Loading data on the multiselect will be slower but it should be shown
		// correctly
		if (pageNumber == 1) {
			// All data goes in the first page
			// Assignable roles are the ones that the entity could be assigned
			// and doesn't already have
			return controlService.listAssignableRoles(app, authorizableEntity, filter);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label = i18nService.getLabel(language, "web.generic.availableRoles");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}
}
