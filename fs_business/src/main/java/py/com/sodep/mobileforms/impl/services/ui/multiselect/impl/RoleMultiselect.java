package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.AuthorizableEntity;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;
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

@Service("multiselect.roles")
@Transactional
public class RoleMultiselect extends BaseMultiselectService implements IMultiselectService {

	@Autowired
	protected I18nBundle i18nService;

	@Autowired
	protected IAuthorizationControlService controlService;
	
	@Autowired
	private IRoleService roleService;

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {
		Application app = request.getApplication();

		int pageNumber = request.getPageNumber();
		int pageLength = request.getPageLength();
		String filter = request.getFilter();

		Map<String, String> params = request.getParams();
		AuthorizableEntity authorizableEntity = getEntity(request, params);
		int level = getLevel(request, params);

		List<Role> roles = getData(app, authorizableEntity, level, filter, pageNumber, pageLength);
		List<MultiselectItem> items = listItems(roles);

		MultiselectServiceResponse response = new MultiselectServiceResponse();
		response.setSuccess(true);
		response.setItems(items);
		return response;
	}

	private int getLevel(MultiselectReadRequest request, Map<String, String> params) {
		int level = Authorization.LEVEL_APP;
		if (params != null) {
			String levelParam = params.get("level");
			if ("application".equals(levelParam)) {
				level = Authorization.LEVEL_APP;
			} else if ("project".equals(levelParam)) {
				level = Authorization.LEVEL_PROJECT;
			} else if ("form".equals(levelParam)) {
				level = Authorization.LEVEL_FORM;
			} else if ("pool".equals(levelParam)) {
				level = Authorization.LEVEL_POOL;
			}
		}
		return level;
	}

	private AuthorizableEntity getEntity(MultiselectReadRequest request, Map<String, String> params) {
		String entityId = null;
		if (params != null) {
			entityId = request.getParams().get("userId");
			if (entityId == null) {
				entityId = request.getParams().get("groupId");
			}
		}

		AuthorizableEntity authorizableEntity = null;
		if (entityId != null) {
			Long id = Long.parseLong(entityId);
			authorizableEntity = em.find(AuthorizableEntity.class, id);
		}
		return authorizableEntity;
	}

	private List<MultiselectItem> listItems(List<Role> roles) {
		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (Role role : roles) {
			MultiselectItem item = new MultiselectItem();
			item.setId(role.getId());
			item.setLabel(role.getName());
			items.add(item);
		}
		return items;
	}

	protected List<Role> getData(Application app, AuthorizableEntity authorizableEntity, int level, String filter,
			int pageNumber, int pageLength) {
		// FIXME we are not taking into account the size of the page
		// Loading data on the multiselect will be slower but it should be shown
		// correctly
		if (pageNumber == 1) {
			// All data goes in the first page
			if (authorizableEntity != null) {
				// If an entity is passed it should list all the roles the entity has at Application level
				// This happens only on the add/edit User/Group page where only application level roles are visible/assignables
				return controlService.listAssignedRoles(app, authorizableEntity, filter);
			} else {
				// If no entity is passed as a parameter to the RoleMultiselect, 
				// It should display all roles valid at the level
				// This happens on the add/edit Form/Project/Pool page
				return roleService.listValidRolesAtLevel(app, level, filter);
			}
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public MultiselectServiceResponse doAction(MultiselectActionRequest request) {

		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label = null;
		if (params != null && params.get("label") != null) {
			String key = params.get("label");
			label = i18nService.getLabel(language, key);
		} else if (params != null && (params.get("userId") != null || params.get("groupId") != null)) {
			label = i18nService.getLabel(language, "web.generic.assignedRoles");
		} else {
			label = i18nService.getLabel(language, "web.generic.roles");
		}
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}
}
