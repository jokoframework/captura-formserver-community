package py.com.sodep.mobileforms.api.dtos.widgets;

import py.com.sodep.mobileforms.api.dtos.DTO;

public class GroupSelectOption implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String group;
	
	private String label;
	

	public GroupSelectOption(String id, String group, String label) {
		super();
		this.id = id;
		this.group = group;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupSelectOption other = (GroupSelectOption) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
