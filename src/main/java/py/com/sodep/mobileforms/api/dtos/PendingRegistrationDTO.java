package py.com.sodep.mobileforms.api.dtos;

import java.sql.Timestamp;

public class PendingRegistrationDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String mail;

	private String name;

	private String lastName;

	private String activationToken;

	private Timestamp registrationTime;

	public PendingRegistrationDTO() {

	}

	public PendingRegistrationDTO(Long id, String mail, String name, String lastName, String token,
			Timestamp registrationTime) {
		super();
		this.id = id;
		this.mail = mail;
		this.name = name;
		this.lastName = lastName;
		this.activationToken = token;
		this.registrationTime = registrationTime;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getActivationToken() {
		return activationToken;
	}

	public void setActivationToken(String activationToken) {
		this.activationToken = activationToken;
	}

	public Timestamp getRegistrationTime() {
		return registrationTime;
	}

	public void setRegistrationTime(Timestamp registrationTime) {
		this.registrationTime = registrationTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
