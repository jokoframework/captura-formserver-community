package py.com.sodep.mobileforms.impl.services.ui.multiselect.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
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

@Service("multiselect.forms")
@Transactional
public class FormMultiselect extends BaseMultiselectService implements IMultiselectService {

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormService formService;

	@Autowired
	private I18nBundle i18nService;

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectServiceResponse listItems(MultiselectReadRequest request) {

		Application application = request.getApplication();
		User user = request.getUser();
		String language = request.getLanguage();

		int pageNumber = request.getPageNumber();
		int pageLength = request.getPageLength();
		String filter = request.getFilter();

		String auth = AuthorizationNames.Form.READ_WEB;
		Map<String, String> params = request.getParams();

		Project project = null;
		// false indicate that all projects should be included
		// true indicates that the project shouldn't be included
		boolean excludeProject = false;
		if (params != null) {
			if (params.get("project") != null) {
				// Shows the forms that belong to a given project
				Long projectId = Long.parseLong(params.get("project"));
				project = projectService.findById(projectId);
			} else if (params.get("excludeProject") != null) {
				excludeProject = true;
				Long projectId = Long.parseLong(params.get("excludeProject"));
				project = projectService.findById(projectId);
			}

		}
		PagedData<List<FormDTO>> pagedData = formService.findByLabel(application, project, excludeProject, user, auth,
				filter, pageNumber, pageLength, language);
		List<FormDTO> forms = pagedData.getData();

		List<MultiselectItem> items = new ArrayList<MultiselectItem>();
		for (FormDTO form : forms) {
			MultiselectItem item = new MultiselectItem();
			item.setId(form.getId());
			item.setLabel(form.getLabel());
			items.add(item);
		}

		MultiselectServiceResponse response = new MultiselectServiceResponse();
		response.setSuccess(true);
		response.setItems(items);
		return response;
	}

	@Override
	public MultiselectServiceResponse doAction(MultiselectActionRequest request) {

		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MultiselectModel loadModel(String language, Map<String, String> params) {
		MultiselectModel model = new MultiselectModel();
		String label = i18nService.getLabel(language, "web.generic.forms");
		model.setLabel(label);
		model.setLeftImage(LEFT_IMAGE.NONE);
		model.setRightImage(RIGHT_IMAGE.NONE);
		model.setShowColumnNames(false);
		model.addColumn(new Column("label", "label", "none"));
		return model;
	}

}
