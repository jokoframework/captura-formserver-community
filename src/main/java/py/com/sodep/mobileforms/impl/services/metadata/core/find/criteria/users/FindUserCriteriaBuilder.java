package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users;

import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.FindCriteriaBuilder;

public class FindUserCriteriaBuilder extends FindCriteriaBuilder<FindUserCriteriaBuilder> {

	private GroupCriteria groupCriteria;

	public FindUserCriteriaBuilder inGroup(Group g) {
		groupCriteria = new GroupCriteria(g, true);
		return this;
	}

	public FindUserCriteriaBuilder notInGroup(Group g) {
		groupCriteria = new GroupCriteria(g, false);
		return this;
	}

	public FindUserCriteria newInstance() {
		return new FindUserCriteria(groupCriteria, propertyCriteria, sortCriteria, pagingCriteria);
	}

}
