package py.com.sodep.mobileforms.web.controllers;

public class UserSettingsJSON {

	private String language;

	private Long defaultApplicationId;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getDefaultApplicationId() {
		return defaultApplicationId;
	}

	public void setDefaultApplicationId(Long defaultApplicationId) {
		this.defaultApplicationId = defaultApplicationId;
	}

}
