package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria;

public class SortCriteria {

	private String orderBy;

	private boolean ascending;

	SortCriteria(String orderBy, boolean ascending) {
		super();
		this.orderBy = orderBy;
		this.ascending = ascending;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public boolean isAscending() {
		return ascending;
	}

}
