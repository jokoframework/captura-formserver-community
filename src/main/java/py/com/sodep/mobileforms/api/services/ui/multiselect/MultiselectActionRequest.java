package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.List;
import java.util.Map;

public class MultiselectActionRequest extends MultiselectReadRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String action;

	private Map<String, String> actionParams;

	private List<String> selectedRowIds;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, String> getActionParams() {
		return actionParams;
	}

	public void setActionParams(Map<String, String> actionParams) {
		this.actionParams = actionParams;
	}

	public List<String> getSelectedRowIds() {
		return selectedRowIds;
	}

	public void setSelectedRowIds(List<String> selectedRowIds) {
		this.selectedRowIds = selectedRowIds;
	}

}
