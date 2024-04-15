package py.com.sodep.mobileforms.web.endpoints.controllers;

import static py.com.sodep.mobileforms.web.endpoints.controllers.MFTranslatorHelper.toMf;
import static py.com.sodep.mobileforms.web.endpoints.controllers.MFTranslatorHelper.toMfWithPublishedVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mf.exchange.objects.metadata.Application;
import py.com.sodep.mf.exchange.objects.metadata.Form;
import py.com.sodep.mf.exchange.objects.metadata.Project;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.api.services.metadata.projects.IProjectService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class has method that are going to be invoked by the device in order to
 * synchronize its data. In order to invoke these rest-like methods a client
 * must prefix the URIs presented here with the "/api" keyword. On the web.xml
 * we have made a mapping form the /api/* to the "Spring MVC Dispatcher Servlet"
 * 
 * @author danicricco
 * 
 */
@Controller
@Api(value = "metadata", description = "Endpoint for metadata (applications, projects, forms) synchronization", position = 1)
public class MetadataEndpoint extends EndpointController {

	private static final Logger logger = LoggerFactory.getLogger(MetadataEndpoint.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IAuthorizationControlService authService;

	@Autowired
	private IProjectService projectService;

	@Autowired
	private IFormService formService;

	@Autowired
	private IFormModelService formModelService;

	@Autowired
	private ILookupTableService lookupTableService;

	@Autowired
	private SynchronizationService syncService;

	@Autowired
	private ISystemParametersBundle systemParams;

	@ApiOperation(value = "Returns the list of the application of the logged in user", response = Application.class, responseContainer = "List")
	@RequestMapping(value = "/metadata/applications", method = RequestMethod.GET)
	public @ResponseBody
	List<Application> getApplicationForUser(HttpServletRequest request) {
		SessionManager sessionManager = new SessionManager(request);
		I18nManager i18nManager = sessionManager.getI18nManager();
		User user = sessionManager.getUser();
		List<py.com.sodep.mobileforms.api.entities.application.Application> applications = userService
				.listApplications(user);
		ArrayList<Application> mfList = new ArrayList<Application>();
		for (py.com.sodep.mobileforms.api.entities.application.Application entity : applications) {
			Application app = toMf(entity);
			app.setProjects(getProjects(entity, user, i18nManager));
			mfList.add(app);
		}

		return mfList;
	}

	// FIXME this method is returning all the forms from every application,where
	// the user belongs
	@ApiOperation(value = "Returns a list of all the forms (from all the applications) of the user", response = Form.class, responseContainer = "List")
	@RequestMapping(value = "/metadata/forms", method = RequestMethod.GET)
	public @ResponseBody
	List<Form> getForms(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18nManager = mgr.getI18nManager();
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		List<Form> forms = new ArrayList<Form>();
		List<py.com.sodep.mobileforms.api.entities.application.Application> applications = userService
				.listApplications(user);
		for (py.com.sodep.mobileforms.api.entities.application.Application app : applications) {

			List<FormDTO> formsDTOList = formService.listAllPublished(app, null, user, AuthorizationNames.Form.MOBILE,
					i18nManager.getSelectedLanguage());
			// bug fix for #1737
			List<Form> formList = toMfWithPublishedVersion(formsDTOList);
			// List<Form> formList = toMf(formsDTOList);
			for (Form f : formList) {
				List<Long> lookupTableIds = formService.getRequiredLookupTableIds(f.getId(), f.getVersion());
				f.setRequiredLookupTables(lookupTableIds);
			}
			forms.addAll(formList);
		}
		return forms;

	}

	@RequestMapping(value = "/metadata/formDefinition/{formId}/{formVersion}", method = RequestMethod.GET)
	public @ResponseBody
	Form getFormDefinition(HttpServletRequest request, @PathVariable("formId") Long formId,
			@PathVariable("formVersion") Long version) throws JsonGenerationException, JsonMappingException,
			IOException {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18nManager = mgr.getI18nManager();
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		
		if (!authControlService.hasFormLevelAccess(formId, user, AuthorizationNames.Form.MOBILE)) {
			throwNoAuthorization();
		}
		
		String language = i18nManager.getSelectedLanguage();
		return getFormDefinition(formId, version, language);
	}
	
	@RequestMapping(value = "/metadata/form/{formId}/{formVersion}", method = RequestMethod.GET)
	public @ResponseBody
	MFForm getForm(HttpServletRequest request, @PathVariable("formId") Long formId,
			@PathVariable("formVersion") Long version) throws JsonGenerationException, JsonMappingException,
			IOException {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18nManager = mgr.getI18nManager();
		SessionManager session = new SessionManager(request);
		User user = session.getUser();
		
		if (!authControlService.hasFormLevelAccess(formId, user, AuthorizationNames.Form.MOBILE)) {
			throwNoAuthorization();
		}
		
		String language = i18nManager.getSelectedLanguage();
		MFForm mfForm = formModelService.getMFForm(formId, version, language, false);
		
		return mfForm;
	}

	private Form getFormDefinition(Long formId, Long version, String language)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Long> lookupTableIds = formService.getRequiredLookupTableIds(formId, version);
		// FIXME Should Form be exposed here?
		py.com.sodep.mobileforms.api.entities.forms.Form formEntity = formService.getForm(formId, version);

		String label = formService.getLabel(formId, language);

		Form form = toMf(formEntity, label);
		form.setRequiredLookupTables(lookupTableIds);

		MFForm mfform = formModelService.getMFForm(formId, version, language, false);
		String definition = objectMapper.writeValueAsString(mfform);
		form.setDefinition(definition);

		return form;
	}

	private List<Project> getProjects(py.com.sodep.mobileforms.api.entities.application.Application app, User u,
			I18nManager i18nManager) {
		ArrayList<Project> mfList = new ArrayList<Project>();
		List<py.com.sodep.mobileforms.api.entities.projects.Project> projects = authService.listProjectsByAuth(app, u,
				AuthorizationNames.Project.READ_MOBILE);
		for (py.com.sodep.mobileforms.api.entities.projects.Project entity : projects) {
			ProjectDetails projectDetails = projectService.loadDetails(entity.getId(),
					i18nManager.getSelectedLanguage());
			Project project = toMf(entity, projectDetails);
			mfList.add(project);
		}

		return mfList;
	}
}
