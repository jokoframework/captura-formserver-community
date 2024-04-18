package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria;

public abstract class FindCriteria {

	protected PropertyCriteria propertyCriteria;

	protected SortCriteria sortCriteria;

	protected PagingCriteria pagingCriteria;

	protected FindCriteria(PropertyCriteria propertyCriteria, SortCriteria sortCriteria, PagingCriteria pagingCriteria) {
		this.propertyCriteria = propertyCriteria;
		this.sortCriteria = sortCriteria;
		this.pagingCriteria = pagingCriteria;
	}

	/**
	 * Filters the search by a given property.
	 * 
	 * May be null. In that case, no filter over properties should be applied in
	 * finding the user.
	 * 
	 * @return
	 */
	public PropertyCriteria getPropertyCriteria() {
		return propertyCriteria;
	}

	/**
	 * How to order the searh.
	 * 
	 * May be null. In that case, the service should have a default sort order.
	 * 
	 * @return
	 */
	public SortCriteria getSortCriteria() {
		return sortCriteria;
	}

	/**
	 * Returns paging information.
	 * 
	 * If null, no pagins should be made. i.e. return all values
	 * 
	 * @return
	 */
	public PagingCriteria getPagingCriteria() {
		return pagingCriteria;
	}
}
