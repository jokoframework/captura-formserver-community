package py.com.sodep.mobileforms.impl.services.metadata.core;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Device;
import py.com.sodep.mobileforms.api.entities.core.Device.OS;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.DeviceBlacklistedException;
import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.applications.IApplicationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;
import py.com.sodep.mobileforms.license.MFApplicationLicense;

@Service("DeviceService")
@Transactional
public class DeviceService extends BaseService<Device> implements IDeviceService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IApplicationService applicationService;

	@Autowired
	private MFLicenseManager mfLicenseManager;

	public DeviceService() {
		super(Device.class);
	}

	private void associate(User user, Device device) {
		List<Device> devices = user.getDevices();
		if (devices == null) {
			devices = new ArrayList<Device>();
			user.setDevices(devices);
		}
		if (!devices.contains(device)) {
			devices.add(device);
			List<User> users = device.getUsers();
			if (users == null) {
				users = new ArrayList<User>();
				device.setUsers(users);
			}
			users.add(user);
		}
	}

	@Override
	public boolean isDeviceAssociated(User user, Application app, String identifier) {
		String queryStr = "SELECT d FROM " + Device.class.getSimpleName() + " d JOIN d.users u"
				+ " WHERE d.deleted = false AND d.active = true AND d.identifier = :identifier "
				+ " AND d.application = :application " + " AND u = :user";

		TypedQuery<Device> query = em.createQuery(queryStr, Device.class);
		query.setParameter("application", app);
		query.setParameter("identifier", identifier);
		query.setParameter("user", user);
		List<Device> resultList = query.getResultList();

		return !resultList.isEmpty();
	}

	private Device search(String model, String brand, OS os, String identifier, Application application) {
		String queryStr = "FROM " + Device.class.getSimpleName()
				+ " WHERE os = :os AND brand = :brand AND identifier = :identifier "
				+ "AND model = :model AND application = :application";

		TypedQuery<Device> query = em.createQuery(queryStr, Device.class);
		query.setParameter("os", os);
		query.setParameter("brand", brand);
		query.setParameter("model", model);
		query.setParameter("identifier", identifier);
		query.setParameter("application", application);

		Device device = null;
		try {
			device = query.getSingleResult();
		} catch (NoResultException e) {

		}
		return device;
	}

	private Long countDevices(User user, Application application) {
		Query query = em.createQuery("SELECT COUNT(*) FROM Device d WHERE :user MEMBER OF d.users "
				+ "AND d.application = :application");
		query.setParameter("user", user);
		query.setParameter("application", application);
		return (Long) query.getSingleResult();
	}

	@Override
	public Device getOrCreateIfNotExists(MFDevice device) {
		MFDeviceInfo deviceInfo = device.getDeviceInfo();

		String model = deviceInfo.getModel();
		String brand = deviceInfo.getBrand();
		OS os = OS.getOS(device.getDeviceInfo().getOs());
		String identifier = deviceInfo.getIdentifier();

		String versionNumber = deviceInfo.getVersionNumber();
		String phoneNumber = deviceInfo.getPhoneNumber();

		Application application = applicationService.findById(device.getApplicationId());
		Device d = search(model, brand, os, identifier, application);
		if (d == null) {
			d = DeviceHelper.toEntity(device);
			d.setApplication(application);
			d = save(d);
		} else {
			d.setPhoneNumber(phoneNumber);
			d.setVersionNumber(versionNumber);
		}

		return d;
	}

	@Override
	public void disassociateDevice(Long userId, Long deviceId) {
		Query q = em.createNativeQuery("Delete from core.users_devices where user_id=:userId and device_id=:deviceId");
		q.setParameter("userId", userId);
		q.setParameter("deviceId", deviceId);
		q.executeUpdate();
	}
	
	private void disassociateAll(Long deviceId){
		Query q = em.createNativeQuery("Delete from core.users_devices where device_id=:deviceId");
		q.setParameter("deviceId", deviceId);
		q.executeUpdate();
	}

	@Override
	public void associate(User user, MFDevice mfDevice) {
		Device device = getOrCreateIfNotExists(mfDevice);
		if (device.getBlacklisted() != null && device.getBlacklisted()) {
			throw new DeviceBlacklistedException();
		}
		user = userService.findById(user.getId());
		Application application = device.getApplication();
		Long applicationId = application.getId();

		if (mfLicenseManager.doesLicenseApply(applicationId)) {
			MFApplicationLicense applicationLicense = mfLicenseManager.getLicense(applicationId);
			Long maxDevicesPerUser = applicationLicense.getMaxDevices();
			Long deviceCount = countDevices(user, application);
			List<Device> devices = user.getDevices();
			boolean contains = false;
			if (devices != null) {
				contains = devices.contains(device);
			}
			if (!contains && deviceCount < maxDevicesPerUser) {
				associate(user, device);
			} else if (!contains) {
				logger.info("Too many devices for user : " + user.getMail() + " in application #" + application.getId());
				throw new LicenseException("Too many devices");
			}
		}

	}

	public List<MFDevice> getDevicesOfUser(Application app, User user) {
		Query q = em.createNativeQuery(
				"select d.* from core.users_devices ud join core.devices d on d.id=ud.device_id "
						+ "where d.application_id=:appId and ud.user_id=:userId", Device.class);
		q.setParameter("appId", app.getId());
		q.setParameter("userId", user.getId());
		List<Device> data = q.getResultList();
		ArrayList<MFDevice> dtoList = new ArrayList<MFDevice>();
		for (Device device : data) {
			dtoList.add(DeviceHelper.fromEntity(device));
		}
		return dtoList;
	}

	@Override
	public MFDevice findById(Long deviceId) {
		Device device = em.find(Device.class, deviceId);
		MFDevice dto = DeviceHelper.fromEntity(device);
		return dto;
	}

	@Override
	public PagedData<List<MFDeviceInfo>> listBlacklistedDevices(Application app, String orderBy, boolean ascending,
			int pageNumber, int pageSize) {
		PagedData<List<Device>> devices = find(app, "blacklisted", BaseService.OPER_EQUALS, true, orderBy, ascending,
				pageNumber, pageSize, false, null);
		return toPagedDataOfMFDeviceInfo(devices);
	}
	
	private Device getDeviceInAppOrThrow(Application app, Long deviceId) {
		Device device = em.find(Device.class, deviceId);
		if(!device.getApplication().getId().equals(app.getId())){
			throw new AuthorizationException();
		}
		return device;
	}

	@Override
	public boolean addToBlacklist(Application app, Long deviceId) {
		//TODO when blacklisted, all associations must be removed
		Device device = getDeviceInAppOrThrow(app, deviceId);
		if (device != null) {
			device.setBlacklisted(true);
			disassociateAll(deviceId);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean removeFromBlacklist(Application app, Long deviceId) {
		Device device = getDeviceInAppOrThrow(app, deviceId);
		if (device != null) {
			device.setBlacklisted(false);
			return true;
		} else {
			return false;
		}
	}

	private PagedData<List<MFDeviceInfo>> toPagedDataOfMFDeviceInfo(PagedData<List<Device>> from) {
		PagedData<List<MFDeviceInfo>> pagedData = new PagedData<List<MFDeviceInfo>>();
		List<MFDeviceInfo> mfdevicesInfo = new ArrayList<MFDeviceInfo>();
		List<Device> devices = from.getData();
		for (Device d : devices) {
			MFDevice mfDevice = DeviceHelper.fromEntity(d);
			mfdevicesInfo.add(mfDevice.getDeviceInfo());
		}
		pagedData.setPageSize(from.getPageSize());
		pagedData.setAvailable(from.getAvailable());
		pagedData.setPageNumber(from.getPageNumber());
		pagedData.setTotalCount(from.getTotalCount());
		pagedData.setData(mfdevicesInfo);
		return pagedData;
	}

}
