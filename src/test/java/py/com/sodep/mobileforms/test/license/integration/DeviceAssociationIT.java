package py.com.sodep.mobileforms.test.license.integration;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mf.exchange.objects.device.MFDeviceInfo;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.core.IDeviceService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test device association and disassociate to the user
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DeviceAssociationIT {

	@Autowired
	private IUserService userService;
	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private MockObjectsContainer mockContainer;

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	// @Test
	public void insertDevices() {
		Application app = mockContainer.getTestApplication();

		User user = userService.findById(2l);
		// User user = mockContainer.getDummyUser(app);

		MFDevice dtoDevice = new MFDevice();
		MFDeviceInfo info = new MFDeviceInfo();
		dtoDevice.setDeviceInfo(info);
		dtoDevice.setApplicationId(app.getId());
		info.setBrand("apple");
		info.setIdentifier("2999");

		info.setModel("iphone5s");
		info.setOs("IOS");
		info.setVersionNumber("988v1");

		info.setManufacturer("setManufacturer");
		info.setProduct("my product");
		info.setRelease("release");

		deviceService.associate(user, dtoDevice);
	}

	/**
	 * Associate and disassociate device to/from user
	 */
	@Test
	public void associateDeviceTest() {
		Application app = mockContainer.getTestApplication();
		User user = mockContainer.getDummyUser(app);

		MFDevice dtoDevice = new MFDevice();
		MFDeviceInfo info = new MFDeviceInfo();
		dtoDevice.setDeviceInfo(info);
		dtoDevice.setApplicationId(app.getId());
		info.setBrand("apple");
		info.setIdentifier("983dkkkk");

		info.setModel("iphone5s");
		info.setOs("iOS");
		info.setVersionNumber("988v1");

		info.setManufacturer("setManufacturer");
		info.setProduct("my product");
		info.setRelease("release");

		deviceService.associate(user, dtoDevice);
		// We need this flush only during testing, because the method
		// deviceService.getDevicesOfUser was implemented using a native access
		// to the DB. This approach should not represent any problem in
		// production were a commit would be done to the database after
		// "deviceService.getDevicesOfUser"
		em.flush();
		List<MFDevice> devices = deviceService.getDevicesOfUser(app, user);
		Assert.assertNotNull(devices);
		Assert.assertEquals(1, devices.size());

		// The only difference between the stored device and the original device
		// should be the id
		MFDevice storedDevice = devices.get(0);
		dtoDevice.getDeviceInfo().setId(storedDevice.getDeviceInfo().getId());
		Assert.assertEquals(dtoDevice, storedDevice);

		// Storing twice the same device shouldn't have any effect
		deviceService.associate(user, dtoDevice);
		em.flush();
		devices = deviceService.getDevicesOfUser(app, user);
		Assert.assertEquals(1, devices.size());

		// Deassociate the device and the number of devices should go back to
		// zero
		deviceService.disassociateDevice(user.getId(), storedDevice.getDeviceInfo().getId());
		devices = deviceService.getDevicesOfUser(app, user);
		Assert.assertEquals(0, devices.size());

	}
}
