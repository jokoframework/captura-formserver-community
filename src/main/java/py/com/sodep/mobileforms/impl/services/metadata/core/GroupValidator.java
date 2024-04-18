package py.com.sodep.mobileforms.impl.services.metadata.core;

import py.com.sodep.mobileforms.api.entities.core.Group;

public class GroupValidator {

	/**
	 * This method receives a field of the Group entity and checks if there is
	 * actually an existing field, the idea is to prevent SQL injection on
	 * queries that are using concatenation
	 * 
	 * @param field
	 * @return
	 */
	public static final String sanitizeOrderBy(String field) {
		if (field.equals(Group.DESCRIPTION) || field.equals(Group.NAME)) {
			return field;
		}
		return "id";
	}
}
