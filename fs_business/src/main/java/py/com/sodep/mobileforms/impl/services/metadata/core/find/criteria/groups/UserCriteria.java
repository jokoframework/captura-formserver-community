package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups;

import py.com.sodep.mobileforms.api.entities.core.User;

public class UserCriteria {

	private User user;

	private boolean member;

	UserCriteria(User user, boolean member) {
		this.user = user;
		this.member = member;
	}

	public User getUser() {
		return user;
	}

	public boolean isMember() {
		return member;
	}

}
