package py.com.sodep.mobileforms.impl.services.metadata.applications;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.ApplicationDTO;
import py.com.sodep.mobileforms.api.dtos.ApplicationSettingsDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;
import py.com.sodep.mobileforms.api.exceptions.DuplicateEntityException;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.exceptions.TooManyApplicationsException;
import py.com.sodep.mobileforms.api.persistence.constants.ContantSystemParams;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.license.KeyStore;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;
import py.com.sodep.mobileforms.license.Constants;
import py.com.sodep.mobileforms.license.MFFormServerLicense;
import py.com.sodep.mobileforms.license.crypto.CryptoUtils;
import py.com.sodep.mobileforms.license.info.FormServerInfo;
import py.com.sodep.mobileforms.license.json.notification.MFFormServerNotification;
import py.com.sodep.mobileforms.license.json.notification.MFFormServerNotificationResponse;

@Service("ApplicationService")
@Transactional
public class ApplicationService extends BaseService<Application> implements IApplicationService {

	private static Logger logger = LoggerFactory.getLogger(ApplicationService.class);

	@Autowired
	private ISystemParametersBundle parameterBoundle;
	
	@Autowired
	private IAuthorizationControlService authorizationControlService;

	@Autowired
	private IUserService userService;

	@Autowired
	private MFLicenseManager mfLicenseManager;
	
	@Autowired
	private KeyStore keyProvider;

	@Autowired
	private RestOperations rest;

	@Value("${checkLicense:true}")
	private boolean checkLicense;
	
	
	public void setCheckLicense(boolean checkLicense) {
		this.checkLicense = checkLicense;
	}
	
	protected ApplicationService() {
		super(Application.class);
	}
	
	@PostConstruct
	public void init() {
		if (checkLicense) {
			checkLicense();
		
			try {
				notifyStartup();
			} catch (Exception e) {
				throw new RuntimeException("An error ocurred while notifying the License Server about this Form Server", e);
			}
		}
	}

	private void notifyStartup() throws JsonGenerationException, JsonMappingException, IOException {
		MFFormServerNotification notification = getNotification();
		String encryptedHex = encryptNotification(notification);
		String url = Constants.LS_NOTIFICATION_URL + "?id=" + notification.getServerId();
		logger.info("Notifying startup : " + url);
		MFFormServerNotificationResponse result = rest.postForObject(url, encryptedHex, MFFormServerNotificationResponse.class);
		if (result.getSuccess() != null && !result.getSuccess()) {
			logger.debug(result.getMessage());
			throw new ApplicationException("Gandalf didn't allow you to pass");
		} else {
			logger.info("Great!, Gandalf let you pass");
		}
	}

	private String encryptNotification(MFFormServerNotification notification) throws IOException,
			JsonGenerationException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		byte[] bytes = mapper.writeValueAsBytes(notification);
		PrivateKey privateKey = keyProvider.getServerKeyPair().getPrivate();
		byte[] encryptedBytes = CryptoUtils.encrypt(bytes, privateKey);
		String encryptedHex = CryptoUtils.toHexString(encryptedBytes);
		return encryptedHex;
	}

	private MFFormServerNotification getNotification() {
		final MFFormServerLicense serverLicense = mfLicenseManager.getFormServerLicense();
		
		MFFormServerNotification notification = new MFFormServerNotification();
		notification.setServerLicenseId(serverLicense.getId());
		notification.setHddSerial(FormServerInfo.getHddSerial());
		notification.setMacAddress(FormServerInfo.getMacAddress());
		notification.setServerId(serverLicense.getServerId());
		notification.setNotificationType(MFFormServerNotification.NOTIFICATION_STARTUP);
		return notification;
	}

	@Override
	public boolean appExists(String name) {
		List<Application> existingApps = listByPropertyEquals("name", name);
		return existingApps != null && existingApps.size() > 0;
	}

	private String getOwnerDefaultRole() {
		String roleName = parameterBoundle.getStrValue(DBParameters.DEFAULT_ROLE_APP_OWNER);
		if (roleName == null) {
			throw new ApplicationException(
					"The default role for application owner is not on the DB. Please check system parameter "
							+ DBParameters.DEFAULT_ROLE_APP_OWNER);
		}
		return roleName;
	}

	@Override
	public Application initAppWithOwner(String name, User owner, String defaultLanguage) {
		return initAppWithOwner(name, owner, true, defaultLanguage);
	}

	private static final Object newApplicationLock = new Object();

	@Override
	public Application initAppWithOwner(String name, User owner, boolean addOwnerAsMember, String defaultLanguage) {
		if (!em.contains(owner)) {
			owner = em.find(User.class, owner.getId());
		}

		if (appExists(name)) {
			throw new DuplicateEntityException("An application with name '" + name + "' already exists");
		}

		Application application = new Application(name, defaultLanguage, owner, true, false);

		try {
			application = save(application);
			checkLicense();
			if (addOwnerAsMember) {
				authorizationControlService.assignRoleToEntity(application, owner.getId(), getOwnerDefaultRole(),
						Authorization.LEVEL_APP, application.getId());
				userService.addUserToApp(owner, application, owner);
			}
			return application;
		} catch (InvalidEntityException e) {
			String msg = "Invalid entity: " + application;
			logger.error(msg);
			if (logger.isDebugEnabled()) {
				logger.debug(msg, e);
			}
			return null;
		}
	}

	private void checkLicense() {
		MFFormServerLicense serverLicense = mfLicenseManager.getFormServerLicense();
		Long maxApplications = null;
		if (serverLicense != null && (maxApplications = serverLicense.getMaxApplications()) != null) {
			synchronized (newApplicationLock) {
				Long count = count();
				if (count > maxApplications) {
					TooManyApplicationsException ex = new TooManyApplicationsException("License grants up to "
							+ maxApplications + " applications. Current count is " + count);
					ex.setMaxCount(maxApplications);
					throw ex; // FIXME i18n?
				}
			}
		}
	}

	@Override
	public Long count() {
		Query query = em.createQuery("SELECT COUNT(*) FROM " + Application.class.getSimpleName()
				+ " a WHERE a.deleted = false");
		Long count = (Long) query.getSingleResult();
		return count;
	}

	@Override
	public List<Application> findByName(String name) {
		return listByPropertyEquals("name", name);
	}

	@Override
	public Application findById(Long applicationId) {
		return super.findById(applicationId);
	}

	@Override
	public List<String> getSupportedLanguages() {
		//Currently all application support all supported languages
		//In the future we might decide to decrease the list of supported languages for a given application
		return ContantSystemParams.supportedLanguages;
	}

	@Override
	@Authorizable(AuthorizationNames.App.APP_CONFIG)
	public boolean initialSetup(Application app, String label, String language) {
		// There are no problems having multiple apps with the same name.
		if (label != null && !label.equals(app.getName())) {
			app.setName(label);
		}

		if (language != null && isSupportedLanguage(language)) {
			app.setDefaultLanguage(language);
		} else if (language != null) {
			throw new RuntimeException("Unsupported language " + language);
		}

		app.setInitialSetupReady(true);
		save(app);
		return true;

	}

	private boolean isSupportedLanguage(String language) {
		return language != null && getSupportedLanguages().contains(language);
	}

	@Override
	public PagedData<List<Application>> findByLabel(String label, int pageNumber, int pageLength, String language) {
		if (label != null) {
			return super.findByProperty("name", BaseService.OPER_CONTAINS, label, "name", true, pageNumber, pageLength);
		} else {
			return super.findAll("name", true, pageNumber, pageLength);
		}
	}

	@Override
	public boolean isActive(Long applicationId) {
		Application app = em.find(Application.class, applicationId);
		if (app != null) {
			Boolean active = app.getActive();
			return active != null && active;
		}
		return false;
	}

	@Override
	public PagedData<List<Application>> findByProperty(String propertyName,
			String oper, String searchValue, String orderBy, boolean ascending,
			Integer page, Integer rows) {
		return super.findByProperty(propertyName, oper, searchValue, orderBy, ascending, page, rows);
	}

	@Override
	public Long countActive() {
		Query query = em.createQuery("SELECT COUNT(*) FROM " + Application.class.getSimpleName()
				+ " a WHERE a.deleted = false AND a.active = true");
		Long count = (Long) query.getSingleResult();
		return count;
	}

	@Override
	public ApplicationDTO saveSettings(Long appId, ApplicationSettingsDTO settings) {
		Application application = this.findById(appId);
		application.setHasWorkflow(settings.getHasWorkflow());
		Application savedApp = this.save(application);
		return toDTO(savedApp);
	}

	private ApplicationDTO toDTO(Application savedApp) {
		ApplicationDTO dto = new ApplicationDTO();
		dto.setId(savedApp.getId());
		dto.setName(savedApp.getName());
		dto.setDefaultLanguage(savedApp.getDefaultLanguage());
		return dto;
	}

}
