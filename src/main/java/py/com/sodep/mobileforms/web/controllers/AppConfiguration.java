package py.com.sodep.mobileforms.web.controllers;

public class AppConfiguration {

	private Long appId;
	private Long defaulLauncher;

	public AppConfiguration() {

	}

	public AppConfiguration(Long appId, Long defaulLauncher) {

		this.appId = appId;
		this.defaulLauncher = defaulLauncher;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public Long getDefaulLauncher() {
		return defaulLauncher;
	}

	public void setDefaulLauncher(Long defaulLauncher) {
		this.defaulLauncher = defaulLauncher;
	}

}
