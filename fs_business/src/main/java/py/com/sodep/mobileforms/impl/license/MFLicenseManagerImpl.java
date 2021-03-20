package py.com.sodep.mobileforms.impl.license;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.DateUtils;
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
import py.com.sodep.mobileforms.license.Constants;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.license.MFFormServerLicense;
import py.com.sodep.mobileforms.license.crypto.CryptoUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component("LicenseManager")
public class MFLicenseManagerImpl implements MFLicenseManager {

	private static final Logger logger = LoggerFactory.getLogger(MFLicenseManagerImpl.class);
	private static final Long MAX_USERS_OPENSOURCE = 50l;
	private static final Long MAX_VALID_DAYS = 3650L;
	private static final Long MAX_DEVICES = 10l;
	private static final Long MAX_APPLICATIONS = 10l;
	private static final String DEFAULT_OWNER = "devnull@sodep.com.py";
	private static final Date YESTERDAY = DateUtils.addDays(new Date(), -1);

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
		MFFormServerLicense result = new MFFormServerLicense() {
			@Override
			public Long getId() {
				return 1l;
			}

			@Override
			public Long getServerId() {
				return 1l;
			}

			@Override
			public Long getMaxApplications() {
				return MAX_APPLICATIONS;
			}

			@Override
			public Map<String, String> getProperties() {
				return new HashMap<>();
			}

			@Override
			public MFApplicationLicense getDefaultApplicationLicense() {
				return new MFApplicationLicense() {
					@Override
					public Long getApplicationId() {
						return 1l;
					}

					@Override
					public Long getMaxDevices() {
						return MAX_DEVICES;
					}

					@Override
					public Long getMaxUsers() {
						return MAX_USERS_OPENSOURCE;
					}

					@Override
					public String getOwner() {
						return DEFAULT_OWNER;
					}

					@Override
					public Date getCreationDate() {
						return YESTERDAY;
					}

					@Override
					public Long getValidDays() {
						return MAX_VALID_DAYS;
					}
				};
			}

			@Override
			public Date getCreationDate() {
				return DateUtils.addDays(new Date(), -1);
			}

			@Override
			public Long getValidDays() {
				return MAX_VALID_DAYS;
			}
		};
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
		MFApplicationLicense mfApplicationLicense = new MFApplicationLicense() {
			@Override
			public Long getApplicationId() {
				return 1l;
			}

			@Override
			public Long getMaxDevices() {
				return MAX_DEVICES;
			}

			@Override
			public Long getMaxUsers() {
				return MAX_USERS_OPENSOURCE;
			}

			@Override
			public String getOwner() {
				return DEFAULT_OWNER;
			}

			@Override
			public Date getCreationDate() {
				return YESTERDAY;
			}

			@Override
			public Long getValidDays() {
				return MAX_VALID_DAYS;
			}
		};
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
		status.setMaxUsers(MAX_USERS_OPENSOURCE);

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
