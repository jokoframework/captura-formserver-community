package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users;

import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.FindCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PagingCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PropertyCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.SortCriteria;

public class FindUserCriteria extends FindCriteria {

	private GroupCriteria groupCriteria;

	FindUserCriteria(GroupCriteria groupCriteria, PropertyCriteria propertyCriteria, SortCriteria sortCriteria,
			PagingCriteria pagingCriteria) {
		super(propertyCriteria, sortCriteria, pagingCriteria);
		this.groupCriteria = groupCriteria;
	}

	/**
	 * Filters the search according to whether we are searching for the user in
	 * a given group or if we are searching for the user outside the given group
	 * 
	 * May be null. In that case, it should matter the groups a user belongs to.
	 * 
	 * @return
	 */
	public GroupCriteria getGroupCriteria() {
		return groupCriteria;
	}

}
