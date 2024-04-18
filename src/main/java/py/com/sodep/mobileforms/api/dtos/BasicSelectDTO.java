package py.com.sodep.mobileforms.api.dtos;

import java.util.HashMap;
import java.util.Map;

public class BasicSelectDTO implements DTO {

	private static final long serialVersionUID = 1L;

	private Map<String, LabeledElementsDTO> elements = new HashMap<String, LabeledElementsDTO>();

	public void addMapElement(String key, String label) {
		LabeledElementsDTO value = new LabeledElementsDTO(label);
		elements.put(key, value);
	}

	public void addMapElement(String key, String label, HashMap<String, Object> selectTypes) {
		LabeledElementsDTO value = new LabeledElementsDTO(label, selectTypes);
		elements.put(key, value);
	}

	public void addMapElement(String key, String label, String[] stringList) {
		LabeledElementsDTO value = new LabeledElementsDTO(label, stringList);
		elements.put(key, value);
	}

	public Map<String, LabeledElementsDTO> getElements() {
		return elements;
	}

}
