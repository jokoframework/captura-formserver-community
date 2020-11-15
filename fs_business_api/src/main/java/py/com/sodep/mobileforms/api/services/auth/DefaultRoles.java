package py.com.sodep.mobileforms.api.services.auth;

/**
 * Contains the default role names assigned during application startup.
 * <p>
 * For more information about these roles, see class DefaultRolesLoader.
 * 
 * @author rodrigovillalba
 *
 */
public enum DefaultRoles {

	ROLE_APP_ADMIN("ROLE_APP_ADMIN"), 
	ROLE_APP_OWNER("ROLE_APP_OWNER");

	private final String text;
	
	private DefaultRoles(final String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
}
