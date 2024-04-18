package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.groups;

import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.FindCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PagingCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PropertyCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.SortCriteria;

public class FindGroupCriteria extends FindCriteria {

	private UserCriteria userCriteria;

	FindGroupCriteria(UserCriteria userCriteria, PropertyCriteria propertyCriteria, SortCriteria sortCriteria,
			PagingCriteria pagingCriteria) {
		super(propertyCriteria, sortCriteria, pagingCriteria);
		this.userCriteria = userCriteria;
	}

	/**
	 * Filters the search of the groups by user's membership
	 * 
	 * @return
	 */
	public UserCriteria getUserCriteria() {
		return userCriteria;
	}

}
