package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.FindCriteriaBuilder;

public class FindGroupCriteriaBuilder extends FindCriteriaBuilder<FindGroupCriteriaBuilder> {

	private UserCriteria userCriteria;

	public FindGroupCriteriaBuilder isMember(User user) {
		userCriteria = new UserCriteria(user, true);
		return this;
	}

	public FindGroupCriteriaBuilder notAMember(User user) {
		userCriteria = new UserCriteria(user, false);
		return this;
	}

	@Override
	public FindGroupCriteria newInstance() {
		return new FindGroupCriteria(userCriteria, propertyCriteria, sortCriteria, pagingCriteria);
	}

}
