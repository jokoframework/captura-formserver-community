package py.com.sodep.mobileforms.impl.services.metadata.forms;

import java.io.Serializable;
import java.util.Map;

public class Row implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<String, Map<String, ?>> data;

	public Map<String, Map<String, ?>> getData() {
		return data;
	}

	public void setData(Map<String, Map<String, ?>> data) {
		this.data = data;
	}

}
