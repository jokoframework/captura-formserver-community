package py.com.sodep.mobileforms.web.acme;

/**
 * If in need to pass html characteres in a String to a ftl page you can wrap
 * the String with this class.
 * 
 * Otherwise, a String will be escaped.
 * 
 * @author Miguel
 * 
 */
public class ACMEUnescapedString {

	private String string;

	public ACMEUnescapedString(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String toString() {
		return string;
	}

}
