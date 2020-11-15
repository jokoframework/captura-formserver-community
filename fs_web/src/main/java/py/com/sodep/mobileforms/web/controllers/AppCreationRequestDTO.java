package py.com.sodep.mobileforms.web.controllers;

import py.com.sodep.mobileforms.api.dtos.UserDTO;

public class AppCreationRequestDTO {

	private String appName;

	private UserDTO userDTO;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public UserDTO getUserDTO() {
		return userDTO;
	}

	public void setUserDTO(UserDTO userDTO) {
		this.userDTO = userDTO;
	}

}
