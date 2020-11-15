package py.com.sodep.mobileforms.api.services.ui.multiselect;

import py.com.sodep.mobileforms.api.dtos.DTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MultiselectRequest implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private User user;

	private String language;

	private Application application;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonIgnore
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@JsonIgnore
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@JsonIgnore
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

}
