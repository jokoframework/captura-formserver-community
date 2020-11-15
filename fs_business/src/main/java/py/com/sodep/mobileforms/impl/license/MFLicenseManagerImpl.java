package py.com.sodep.mobileforms.impl.license;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.api.services.license.MFAppLicenseStatus;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.license.MFFormServerLicense;
import py.com.sodep.mobileforms.license.crypto.CryptoUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component("LicenseManager")
public class MFLicenseManagerImpl implements MFLicenseManager {

	private static final Logger logger = LoggerFactory.getLogger(MFLicenseManagerImpl.class);

	@Autowired
	private KeyStore keyStore;

	@Autowired
	private LicenseStore licenseStore;

	private volatile MFFormServerLicense serverLicense;

	private Object lock = new Object();

	private Map<Long, MFApplicationLicense> applicationLicenseCache = new ConcurrentHashMap<Long, MFApplicationLicense>();

	@Autowired
	private IUserService userService;

	@Override
	public MFFormServerLicense getFormServerLicense() {
		// double-check idiom. Effective Java p. 283
		MFFormServerLicense result = serverLicense;
		if (result == null) {
			synchronized (lock) {
				result = serverLicense;
				if (result == null) {
					String encrypted = licenseStore.getEncryptedFormServerLicense();
					String decrypted = decrypt(encrypted);
					serverLicense = result = parseServerLicense(decrypted);
				}
			}
		}
		return result;
	}

	private MFFormServerLicense parseServerLicense(String decrypted) {
		try {
			MFFormServerLicense license = Utils.getMapper().readValue(decrypted, MFFormServerLicense.class);
			logLicenseInfo(license);
			return license;
		} catch (Exception e) {
			throw new LicenseException("Error while reading license", e);
		}
	}

	@Override
	public boolean doesLicenseApply(Long appId) {
		return true;  
	}

	@Override
	public MFApplicationLicense getLicense(Long applicationId) {
		MFApplicationLicense mfApplicationLicense = applicationLicenseCache.get(applicationId);
		if (mfApplicationLicense == null) {
			synchronized (lock) {
				mfApplicationLicense = applicationLicenseCache.get(applicationId);
				if (mfApplicationLicense == null) {
					List<String> encryptedApplicationLicenses = licenseStore
							.getEncryptedApplicationLicense(applicationId);
					if (encryptedApplicationLicenses != null && !encryptedApplicationLicenses.isEmpty()) {
						String encrypted = encryptedApplicationLicenses.get(0);
						String decrypted = decrypt(encrypted);
						mfApplicationLicense = parseDecryptedOrGetDefault(decrypted);
						Long id = mfApplicationLicense.getApplicationId();
						if (id != null && !id.equals(applicationId)) {
							logger.warn("License application Id and request don't match " + applicationId
									+ ". Using default");
							mfApplicationLicense = getDefaultApplicationLicense();
						}
					} else {
						logger.info("No license for application " + applicationId + ". Using default");
						mfApplicationLicense = getDefaultApplicationLicense();
					}
				}
				applicationLicenseCache.put(applicationId, mfApplicationLicense);
			}
		}
		return mfApplicationLicense;
	}

	@Override
	public void setLicense(Long applicationId, String encryptedLicense) {
		synchronized (lock) {
			applicationLicenseCache.remove(applicationId);
			licenseStore.saveApplicationLicense(applicationId, encryptedLicense);
		}
	}

	@Override
	public MFApplicationLicense parseApplicationLicense(InputStream is) {
		String line = IOUtils.readLine(is);
		String decrypted = decrypt(line);
		ObjectMapper mapper = Utils.getMapper();
		try {
			return mapper.readValue(decrypted, MFApplicationLicense.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private MFApplicationLicense parseDecryptedOrGetDefault(String decrypted) {
		ObjectMapper mapper = Utils.getMapper();
		MFApplicationLicense applicationLicense;
		try {
			applicationLicense = mapper.readValue(decrypted, MFApplicationLicense.class);
			logLicenseInfo(applicationLicense);
		} catch (Exception e) {
			logger.debug("Error while reading license, using default", e);
			applicationLicense = getDefaultApplicationLicense();
		}
		return applicationLicense;
	}

	private MFApplicationLicense getDefaultApplicationLicense() {
		MFFormServerLicense formServerLicense = getFormServerLicense();
		return formServerLicense.getDefaultApplicationLicense();
	}

	private String decrypt(String encrypted) {
		PublicKey lsPublicKey = keyStore.getLicenseServerPublicKey();
		PublicKey fsPublicKey = keyStore.getServerKeyPair().getPublic();

		byte[] bytes = CryptoUtils.decrypt(CryptoUtils.decrypt(CryptoUtils.fromHexString(encrypted), lsPublicKey),
				fsPublicKey);

		return new String(bytes);
	}

	private void logLicenseInfo(MFApplicationLicense applicationLicense) {
		logger.info("----- Application license -----");
		logger.info("# application id : " + applicationLicense.getApplicationId());
		logger.info("# owner : " + applicationLicense.getOwner());
		logger.info("# Max users : " + applicationLicense.getMaxUsers());
		logger.info("# Max devices : " + applicationLicense.getMaxDevices());
		logger.info("# Creation date :" + applicationLicense.getCreationDate());
		logger.info("# Valid for : " + applicationLicense.getValidDays() + " days");
	}

	private void logLicenseInfo(MFFormServerLicense serverLicense) {
		logger.info("----- Form Server License -----");
		logger.info("# ID : " + serverLicense.getId());
		logger.info("# Granted on : " + serverLicense.getCreationDate());
		logger.info("# Valid for : " + serverLicense.getValidDays() + " days");
		logger.info("# Max Applications : " + serverLicense.getMaxApplications());
		logger.info("\tLicense properties");
		Map<String, String> properties = serverLicense.getProperties();
		if (properties != null) {
			Set<String> keys = properties.keySet();
			for (String key : keys) {
				logger.info("\t" + key + " :" + properties.get(key));
			}
		}

		MFApplicationLicense defaultApplicationLicense = serverLicense.getDefaultApplicationLicense();
		if (defaultApplicationLicense == null) {
			throw new LicenseException("Default Application License is null");
		}
		logger.info("----- Default Application license -----");
		logger.info("# Max users : " + defaultApplicationLicense.getMaxUsers());
		logger.info("# Max devices : " + defaultApplicationLicense.getMaxDevices());
		logger.info("# Valid for : " + defaultApplicationLicense.getValidDays() + " days");
	}

	@Override
	public void reloadLicenses() {
		synchronized (lock) {
			serverLicense = null;
			applicationLicenseCache.clear();
		}
	}

	@Override
	public MFApplicationLicense parseEncrypted(String encrypted) {
		try {
			ObjectMapper mapper = Utils.getMapper();
			return mapper.readValue(decrypt(encrypted), MFApplicationLicense.class);
		} catch (Exception e) {
			logger.debug("Error while reading license, using default", e);
		}
		return null;
	}

	@Override
	public MFAppLicenseStatus getLicenseStatus(Application app) {
		MFAppLicenseStatus status = new MFAppLicenseStatus();
		final MFApplicationLicense license = getLicense(app.getId());
		status.setMaxUsers(license.getMaxUsers());

		// query a one page user list in order to obtain the total number of
		// users without rewriting the query
		PagedData<List<User>> page = userService.findAll(app, "", true, 1, 1);
		status.setActiveUsers(page.getTotalCount());
		return status;
	}

	@Override
	public Date caculateExpirationDate(MFApplicationLicense license) {
		Date creationDate = license.getCreationDate();
		if (creationDate != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(creationDate);
			c.add(Calendar.DAY_OF_MONTH, license.getValidDays().intValue());
			Date time = c.getTime();
			return time;
		}
		return null;
	}
}
