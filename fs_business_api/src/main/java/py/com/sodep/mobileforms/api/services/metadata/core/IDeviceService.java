package py.com.sodep.mobileforms.api.services.metadata.core;

import java.util.List;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Device;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IDeviceService {

	Device save(Device device);

	Device getOrCreateIfNotExists(MFDevice device);

	void associate(User user, MFDevice device);

	void disassociateDevice(Long userId, Long deviceId);

	List<MFDevice> getDevicesOfUser(Application app, User user);

	MFDevice findById(Long deviceId);

	boolean isDeviceAssociated(User user, Application app, String identifier);

	PagedData<List<MFDeviceInfo>> listBlacklistedDevices(Application app, String orderBy, boolean ascending,
			int pageNumber, int pageSize);

	boolean addToBlacklist(Application app, Long deviceId);

	boolean removeFromBlacklist(Application app, Long deviceId);

}
