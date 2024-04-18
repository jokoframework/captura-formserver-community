package py.com.sodep.mobileforms.api.dtos;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO implements DTO {

	/**
	 * Tagging Interface. Validations that are imposed when saving a User from
	 * the Admin Page
	 * 
	 * @author Miguel
	 * 
	 */
	// FIXME take out of this class, make a package for validation tagging
	// interfaces
	public static interface AdminValidationGroup {

	}
	
	public static final int MIN_PASSWORD_LENGTH = 8;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean active = false;

	private Long id;

	@Pattern(regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "web.validation.user.invalidmail", groups = {
			AdminValidationGroup.class, Default.class })
	private String mail;

	@Length(min = 2, message = "web.validation.user.firstName", groups = { AdminValidationGroup.class, Default.class })
	@NotNull
	private String firstName;

	@Length(min = 2, message = "web.validation.user.lastName", groups = { AdminValidationGroup.class, Default.class })
	@NotNull
	private String lastName;

	@Length(min = MIN_PASSWORD_LENGTH, message = "web.validation.user.password", groups = { Default.class })
	@NotNull
	private String password;

	private Long applicationId;

	private List<RoleDTO> roles;

	private Boolean member;
	
	private String username;

	public UserDTO() {

	}

	public UserDTO(UserDTO dto) {

		this.active = dto.active;
		this.id = dto.id;
		this.mail = dto.mail;
		this.firstName = dto.firstName;
		this.lastName = dto.lastName;
		this.password = dto.password;
		this.applicationId = dto.applicationId;
		this.roles = dto.roles;
		this.member = dto.member;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public List<RoleDTO> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleDTO> roles) {
		this.roles = roles;
	}

	public Boolean getMember() {
		return member;
	}

	public void setMember(Boolean member) {
		this.member = member;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
