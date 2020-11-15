package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria;

public class PagingCriteria {

	private int pageSize = -1;

	private int pageNumber = -1;

	PagingCriteria() {

	}

	PagingCriteria(int pageSize, int pageNumber) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

}
