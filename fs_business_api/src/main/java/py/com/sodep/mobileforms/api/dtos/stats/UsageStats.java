package py.com.sodep.mobileforms.api.dtos.stats;

import java.util.ArrayList;
import java.util.List;

public class UsageStats {

	public static class LoginInfo {

		private String mail;

		private String time;
		
		private String loginType;

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public String getLoginType() {
			return loginType;
		}

		public void setLoginType(String loginType) {
			this.loginType = loginType;
		}

	}

	public static class DocumentCount {

		private int days;

		private Long documentCount;

		private Long byteCount;

		public int getDays() {
			return days;
		}

		public void setDays(int days) {
			this.days = days;
		}

		public Long getDocumentCount() {
			return documentCount;
		}

		public void setDocumentCount(Long documentCount) {
			this.documentCount = documentCount;
		}

		public Long getByteCount() {
			return byteCount;
		}

		public void setByteCount(Long byteCount) {
			this.byteCount = byteCount;
		}

	}

	private Long applicationId;

	private String applicationName;

	private Long projectCount;

	private Long formCount;

	private Long userCount;

    private boolean applicationActive;

    private List<LoginInfo> loginInfo = new ArrayList<UsageStats.LoginInfo>();

	private List<DocumentCount> documentCount = new ArrayList<UsageStats.DocumentCount>();

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public List<LoginInfo> getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(List<LoginInfo> loginInfo) {
		this.loginInfo = loginInfo;
	}

	public List<DocumentCount> getDocumentCount() {
		return documentCount;
	}

	public void setDocumentCount(List<DocumentCount> documentCount) {
		this.documentCount = documentCount;
	}

	public Long getProjectCount() {
		return projectCount;
	}

	public void setProjectCount(Long projectCount) {
		this.projectCount = projectCount;
	}

	public Long getFormCount() {
		return formCount;
	}

	public void setFormCount(Long formCount) {
		this.formCount = formCount;
	}

	public Long getUserCount() {
		return userCount;
	}

	public void setUserCount(Long userCount) {
		this.userCount = userCount;
	}

    public boolean isApplicationActive() {
        return applicationActive;
    }

    public void setApplicationActive(boolean p_applicationActive) {
        applicationActive = p_applicationActive;
    }
}
