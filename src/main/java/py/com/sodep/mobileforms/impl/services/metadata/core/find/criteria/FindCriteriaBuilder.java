package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria;


@SuppressWarnings("unchecked")
public abstract class FindCriteriaBuilder<T> {

	protected PropertyCriteria propertyCriteria;

	protected SortCriteria sortCriteria;
	
	protected PagingCriteria pagingCriteria = new PagingCriteria();

	protected FindCriteriaBuilder() {
	}

	public T pageSize(int pageSize) {
		pagingCriteria.setPageSize(pageSize);
		return (T) this;
	}

	public T pageNumber(int pageNumber) {
		pagingCriteria.setPageNumber(pageNumber);
		return (T) this;
	}

	public T asc(String orderBy) {
		sortCriteria = new SortCriteria(orderBy, true);
		return (T) this;
	}

	public T desc(String orderBy) {
		sortCriteria = new SortCriteria(orderBy, false);
		return (T) this;
	}

	public T property(String name, String oper, Object value) {
		propertyCriteria = new PropertyCriteria(name, oper, value);
		return (T) this;
	}

	public abstract FindCriteria newInstance();

}
