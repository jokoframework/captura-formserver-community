package py.com.sodep.mobileforms.api.services.license;

public class MFAppLicenseStatus {
	private Long maxUsers;
	private Long activeUsers;

	public Long getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(Long maxUsers) {
		this.maxUsers = maxUsers;
	}

	public Long getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(Long activeUsers) {
		this.activeUsers = activeUsers;
	}
}
