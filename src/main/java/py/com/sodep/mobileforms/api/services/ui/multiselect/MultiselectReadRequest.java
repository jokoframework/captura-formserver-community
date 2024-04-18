package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.Map;

/**
 * This is mapped to a request made by a Sodep multiselect widget.
 * 
 * An instance of this class is passed by the Web layer to the Manager and the
 * manager after looking up for the service responsible of handling the request,
 * passes the MultiselectReadRequest object to it.
 * 
 * An instance of this class, then, should contain all the necessary information
 * so that the service may return a correct result.
 * 
 * @author Miguel
 * 
 */
public class MultiselectReadRequest extends MultiselectRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String filter;

	private Integer pageNumber;

	private Integer pageLength;

	private Map<String, String> params;

	private MultiselectOrder order;

	/**
	 * The page number that is being requested
	 * 
	 * @return
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * The jQuery ui widget has an input to filter the items. This value is
	 * passed along here. The implementation of a IMultiselectService should
	 * take this value into consideration to filter accordingly.
	 * 
	 * @return
	 */
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * 
	 * @return
	 */
	public MultiselectOrder getOrder() {
		return order;
	}

	public void setOrder(MultiselectOrder order) {
		this.order = order;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * The multiselect was meant to support some kinf of pagination. When data
	 * is requested. In the request it must be specified the page number and the
	 * Page Length or the number of items in the page
	 * 
	 * @return
	 */
	public Integer getPageLength() {
		return pageLength;
	}

	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}

}
