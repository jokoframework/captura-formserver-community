package py.com.sodep.mobileforms.api.dtos.widgets;

import py.com.sodep.mobileforms.api.dtos.DTO;

public class SimpleTableDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5782072347499792536L;
	
	private String name;
	private String value;
	
	public SimpleTableDTO(String name, Object value) {
		setName(name);
		if(value != null) {
			setValue(value.toString());
		}
		else {
			setValue("<null value>");
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
