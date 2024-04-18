package py.com.sodep.mobileforms.api.services.ui.multiselect;

import py.com.sodep.mobileforms.api.dtos.DTO;

public class MultiselectItem implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object id;

	private String[] labels;

	private String tooltip;
	
	private Boolean active;

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	/**
	 * The reason why we have multiple labels is that we might support multiple
	 * columns in the multiselect in a future.
	 * 
	 * @return
	 */
	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public String getLabel() {
		if (labels != null) {
			return labels[0];
		}
		return null;
	}

	public void setLabel(String label) {
		labels = new String[] { label };
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
