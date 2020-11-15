package py.com.sodep.mobileforms.web.controllers.cruds;

public class UserStatus {

	public static final String EXISING = "existing";
	public static final String NON_EXISTING = "non_existing";
	public static final String MEMBER = "member";
	private String status;

	public UserStatus(String status) {
		this.status = status;
	}

	public UserStatus() {

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
