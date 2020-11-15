package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users;

import py.com.sodep.mobileforms.api.entities.core.Group;

public class GroupCriteria {

	private Group group;

	private boolean inGroup;

	GroupCriteria(Group group, boolean inGroup) {
		this.group = group;
		this.inGroup = inGroup;
	}

	public Group getGroup() {
		return group;
	}

	public boolean isInGroup() {
		return inGroup;
	}
}
