package py.com.sodep.mobileforms.web.controllers.ui.multiselect;

import java.util.List;

import py.com.sodep.mobileforms.api.services.ui.multiselect.MultiselectItem;
import py.com.sodep.mobileforms.web.json.JsonResponse;

public class MultiselectControllerResponse extends JsonResponse<String> {

	private List<MultiselectItem> items;

	public List<MultiselectItem> getItems() {
		return items;
	}

	public void setItems(List<MultiselectItem> items) {
		this.items = items;
	}

}
