package py.com.sodep.mobileforms.api.exceptions;

import java.util.List;
import java.util.Map;

import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;

public class ElementPrototypeInUseException extends RuntimeException {

	private Map<Form, List<ElementPrototype>> mapFormPrototypes;

	private List<Form> forms;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ElementPrototypeInUseException() {
		super();
	}

	public ElementPrototypeInUseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElementPrototypeInUseException(String message) {
		super(message);
	}

	public ElementPrototypeInUseException(Throwable cause) {
		super(cause);
	}

	public Map<Form, List<ElementPrototype>> getMapFormPrototypes() {
		return mapFormPrototypes;
	}

	public void setMapFormPrototypes(Map<Form, List<ElementPrototype>> mapFormPrototypes) {
		this.mapFormPrototypes = mapFormPrototypes;
	}

	public List<Form> getForms() {
		return forms;
	}

	public void setForms(List<Form> forms) {
		this.forms = forms;
	}

}
