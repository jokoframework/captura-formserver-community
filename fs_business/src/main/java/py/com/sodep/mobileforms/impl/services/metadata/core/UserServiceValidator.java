package py.com.sodep.mobileforms.impl.services.metadata.core;

import py.com.sodep.mobileforms.api.entities.core.User;

public class UserServiceValidator {

	/**
	 * This method receives a field of the User entity and checks if there is
	 * actually an existing field, the idea is to prevent SQL injection on
	 * queries that are using concatenation
	 * 
	 * @param field
	 * @return
	 */
	public static final String sanitizeOrderBy(String field) {
		if (field.equals(User.FIRSTNAME) || field.equals(User.LASTNAME) || field.equals(User.MAIL)) {
			return field;
		}
		return "id";
	}
}
