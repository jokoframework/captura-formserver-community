package py.com.sodep.mobileforms.api.dtos;

import java.util.List;

import py.com.sodep.mf.exchange.objects.device.MFDevice;

/**
 * Extends the {@link UserDTO} concept with the devices that the user has on a
 * given application
 * 
 * @author danicricco
 * 
 */
public class UserDTODevices extends UserDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<MFDevice> devices;

	private Long maxDevices;
	
	// cap-305
	private String lastLogin;

	public UserDTODevices() {

	}

	public UserDTODevices(UserDTO dto) {
		super(dto);

	}

	public List<MFDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<MFDevice> devices) {
		this.devices = devices;
	}

	public Long getMaxDevices() {
		return maxDevices;
	}

	public void setMaxDevices(Long maxDevices) {
		this.maxDevices = maxDevices;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

}
