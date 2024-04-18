package py.com.sodep.mobileforms.impl.services.metadata.core;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.entities.core.Device;
import py.com.sodep.mobileforms.api.entities.core.Device.OS;

public class DeviceHelper {

	public static Device toEntity(MFDevice dto) {
		MFDeviceInfo deviceInfo = dto.getDeviceInfo();
		OS os = OS.getOS(deviceInfo.getOs());
		String identifier = deviceInfo.getIdentifier();
		String brand = deviceInfo.getBrand();
		String model = deviceInfo.getModel();
		String versionNumber = deviceInfo.getVersionNumber();

		Device d = new Device();
		d.setOs(os);
		d.setIdentifier(identifier);
		d.setBrand(brand);
		d.setModel(model);
		d.setVersionNumber(versionNumber);
		d.setManufacturer(deviceInfo.getManufacturer());
		d.setProduct(deviceInfo.getProduct());
		d.setRelease(deviceInfo.getRelease());
		return d;
	}

	public static MFDevice fromEntity(Device entity) {
		MFDevice dto = new MFDevice();
		dto.setApplicationId(entity.getApplication().getId());
		MFDeviceInfo info = new MFDeviceInfo();
		info.setBrand(entity.getBrand());
		info.setIdentifier(entity.getIdentifier());
		info.setModel(entity.getModel());
		info.setOs(entity.getOs().toString());
		info.setVersionNumber(entity.getVersionNumber());
		info.setId(entity.getId());
		info.setManufacturer(entity.getManufacturer());
		info.setProduct(entity.getProduct());
		info.setRelease(entity.getRelease());
		dto.setDeviceInfo(info);
		return dto;
	}
}
