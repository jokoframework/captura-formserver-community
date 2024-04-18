package py.com.sodep.mobileforms.web.controllers;

import py.com.sodep.mobileforms.api.dtos.UserDTO;

public class RegistrationRequest {

	private UserDTO userDTO;
	private String captchaValue;
	public UserDTO getUserDTO() {
		return userDTO;
	}
	public void setUserDTO(UserDTO userDTO) {
		this.userDTO = userDTO;
	}
	public String getCaptchaValue() {
		return captchaValue;
	}
	public void setCaptchaValue(String captchaValue) {
		this.captchaValue = captchaValue;
	}
	
}
