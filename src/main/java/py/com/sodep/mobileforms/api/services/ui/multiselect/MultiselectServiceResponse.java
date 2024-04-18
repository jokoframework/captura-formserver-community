package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.List;

public class MultiselectServiceResponse {

	private boolean success;

	private String title;

	private String message;

	private List<MultiselectItem> items;

	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<MultiselectItem> getItems() {
		return items;
	}

	public void setItems(List<MultiselectItem> items) {
		this.items = items;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
