package py.com.sodep.mobileforms.impl.services.auth;

import py.com.sodep.mobileforms.api.services.auth.DeclareAuthorizationLevel;

/**
 * This is the configuration made with the annotation
 * {@link DeclareAuthorizationLevel} that the {@link AuthorizationLoader} keeps
 * in memory for further reference.
 * 
 * @author danicricco
 * 
 */
public class AuthorizationLevelConf {

	/**
	 * The reference level
	 */
	private Integer level;
	/**
	 * The prefix used by this level
	 */
	private String prefix;
	/**
	 * A field on the class {@link AuthorizationControlService} that will be
	 * used to store the referenced object
	 */
	private String column;

	public AuthorizationLevelConf(Integer level, String prefix, String column) {
		this.level = level;
		this.prefix = prefix;
		this.column = column;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

}
