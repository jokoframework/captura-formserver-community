package py.com.sodep.mobileforms.web.json;

/**
 * 
 * 
 * @author danicricco
 * 
 */
public class LoginInfoJSON  {

	private final String pageToRedirect;

	public LoginInfoJSON(String pageToRedirect) {
		this.pageToRedirect = pageToRedirect;
	}

	public String getPageToRedirect() {
		return pageToRedirect;
	}

}
