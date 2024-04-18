package py.com.sodep.mobileforms.web.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mobileforms.api.dtos.ApplicationDTO;
import py.com.sodep.mobileforms.api.dtos.ApplicationSettingsDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.license.MFAppLicenseStatus;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.utils.TemporalUtils;
import py.com.sodep.mobileforms.web.constants.Attributes;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class ApplicationSettingsController {

	@Autowired
	private IApplicationService appService;

	@Autowired
	private MFLicenseManager licenseManager;

	private ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "/application/settings.mob")
	public ModelAndView settings(HttpServletRequest request, HttpServletResponse response) {
		SessionManager mgr = new SessionManager(request);
		Application app = mgr.getApplication();
		ModelAndView mav = new ModelAndView("/home/pages/application/settings.ftl");
		final MFApplicationLicense license = licenseManager.getLicense(app.getId());
		String validUntil = getValidUntil(app, mgr.getI18nManager(), license);
		mav.addObject("validUntil", validUntil);
		mav.addObject("license", license);
		mav.addObject("maxAppnameLength", Attributes.MAX_APPNAME_LENGTH);
		mav.addObject("applicationName", app.getName());
		mav.addObject("defaultLanguage", app.getDefaultLanguage());
		mav.addObject("languages", appService.getSupportedLanguages());
		User owner = app.getOwner();
		String mail = owner.getMail() != null ? owner.getMail() : owner.getFormerMail();
		String fullName = owner.getFirstName() + " " + owner.getLastName();
		mav.addObject("contactFullName", fullName);
		mav.addObject("contactEmail", mail);
		mav.addObject("applicationId", app.getId());
		return mav;
	}

	/**
	 * Obtain the license status of the application where the user is currently
	 * connected
	 * 
	 * @return
	 */
	@RequestMapping(value = "/application/license/status.ajax")
	public @ResponseBody
	MFAppLicenseStatus getAppLicenseStatus(HttpServletRequest request) {
		SessionManager mgr = new SessionManager(request);
		Application app = mgr.getApplication();
		return licenseManager.getLicenseStatus(app);
	}

	@RequestMapping("/application/settings/license/upload.mob")
	public ModelAndView uploadFile(HttpServletRequest request,
			@RequestParam(value = "licenseFile") MultipartFile uploadedFile) throws IOException {
		ModelAndView mav = new ModelAndView("/home/pages/application/json-response.ftl");
		JsonResponse<Object> response = new JsonResponse<Object>();
		SessionManager mgr = new SessionManager(request);
		I18nManager i18nManager = mgr.getI18nManager();
		Application application = mgr.getApplication();
		InputStream inputStream = null;
		try {
			inputStream = uploadedFile.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String encrypted = reader.readLine();
			MFApplicationLicense parsedApplicationLicense = licenseManager.parseEncrypted(encrypted);
			if (parsedApplicationLicense.getApplicationId().equals(application.getId())) {
				licenseManager.setLicense(application.getId(), encrypted);
				response.setTitle(i18nManager.getMessage("web.generic.ok"));
				response.setMessage(i18nManager.getMessage("web.settings.applicationLicense.licenseUpload"));
				response.setSuccess(true);
				response.addContent("license", parsedApplicationLicense);
				response.addContent("validUntil",
						getValidUntil(mgr.getApplication(), mgr.getI18nManager(), parsedApplicationLicense));
			} else {
				response.setTitle(i18nManager.getMessage("web.generic.error"));
				response.setMessage(i18nManager.getMessage("web.settings.applicationLicense.invalid"));
				response.setSuccess(false);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("An error ocurred while reading the license");
			response.setTitle("Error");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		String responseStr = mapper.writeValueAsString(response);
		mav.addObject("response", responseStr);
		return mav;
	}

	@RequestMapping(value = "/application/settings/save.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<ApplicationDTO> settings(HttpServletRequest request,
			@RequestParam(value = "applicationName", required = false) String applicationName,
			@RequestParam(value = "defaultLanguage", required = false) String defaultLanguage) {
		SessionManager mgr = new SessionManager(request);
		Application app = mgr.getApplication();
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<ApplicationDTO> response = new JsonResponse<ApplicationDTO>();
		boolean success = appService.initialSetup(app, applicationName, defaultLanguage);
		if (success) {
			response.setTitle(i18n.getMessage("web.settings.preferences.saved.title"));
			response.setMessage(i18n.getMessage("web.settings.preferences.saved.message"));
			response.setSuccess(true);
		}

		// changed the detached entity in the http session
		Application savedApp = appService.findById(app.getId());
		mgr.setApplication(savedApp);

		ApplicationDTO dto = toDTO(savedApp);
		response.setObj(dto);
		return response;
	}
	

	@RequestMapping(value = "/application/admin/settings/save.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<ApplicationDTO> adminSettingsSave(HttpServletRequest request,
			@RequestParam(value = "appId", required = true) Long appId,
			@RequestParam(value = "hasWorkflow", required = false, defaultValue="false") Boolean hasWorkflow) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();

		JsonResponse<ApplicationDTO> response = new JsonResponse<ApplicationDTO>();
		ApplicationSettingsDTO dto = new ApplicationSettingsDTO(hasWorkflow.booleanValue());
		ApplicationDTO savedDto = appService.saveSettings(appId, dto);
		if (savedDto != null) {
			response.setTitle(i18n.getMessage("web.settings.preferences.saved.title"));
			response.setMessage(i18n.getMessage("web.settings.preferences.saved.message"));
			response.setSuccess(true);
		}

		response.setObj(savedDto);
		return response;
	}

	@RequestMapping(value = "/application/admin/settings/get.ajax", method = RequestMethod.GET)
	public @ResponseBody
	JsonResponse<ApplicationDTO> adminSettingsGet(HttpServletRequest request,
			@RequestParam(value = "appId", required = true) Long appId) {
		JsonResponse<ApplicationDTO> response = new JsonResponse<ApplicationDTO>();
		Application app = appService.findById(appId);
		if (app != null) {
			response.setSuccess(true);
		}
		response.setObj(toDTO(app));
		return response;
	}
	
	private ApplicationDTO toDTO(Application savedApp) {
		ApplicationDTO dto = new ApplicationDTO();
		dto.setId(savedApp.getId());
		dto.setName(savedApp.getName());
		dto.setDefaultLanguage(savedApp.getDefaultLanguage());
		dto.setHasWorkflow(savedApp.getHasWorkflow());
		return dto;
	}
	
	
	/**
	 * Activate o deactivate an application
	 * 
	 * @return
	 */
	@RequestMapping(value = "/application/activate.mob", method = RequestMethod.GET)
	public void flipFlopActication(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "appId") Long appId) {
		Application app = appService.findById(appId);
		app.setActive(!app.getActive());
		appService.save(app);
	}

	@RequestMapping(value = "/application/settings/activate.ajax", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse<ApplicationDTO> activation(HttpServletRequest request,
			@RequestParam(value = "applicationIdentifier", required = false) Long applicationIdentifier) {
		SessionManager mgr = new SessionManager(request);
		I18nManager i18n = mgr.getI18nManager();
		Application app = appService.findById(applicationIdentifier);
		app.setActive(!app.getActive());
		app = appService.save(app);

		JsonResponse<ApplicationDTO> response = new JsonResponse<ApplicationDTO>();
		boolean success = app != null;
		if (success) {
			response.setTitle(i18n.getMessage("web.application.state.title"));
			response.setMessage(i18n.getMessage("web.application.state.message"));
			response.setSuccess(true);
		} else
		{
			response.setMessage(i18n.getMessage("web.application.state.error.message"));
			response.setSuccess(false);
		}

		ApplicationDTO dto = toDTO(app);
		response.setObj(dto);
		return response;
	}
	
	private static String getValidUntil(Application app,
			I18nManager i18nManager, MFApplicationLicense license) {
		Date creationDate = license.getCreationDate() == null ? app.getCreated() : license.getCreationDate();
		if (creationDate != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(creationDate);
			c.add(Calendar.DAY_OF_MONTH, license.getValidDays().intValue());
			Date time = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(TemporalUtils.DATE_FORMAT);
			return sdf.format(time);
		} else {
			return i18nManager.getMessage("web.settings.applicationLicense.validUntil.unlimited");
		}
	}
	
}
