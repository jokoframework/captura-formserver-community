package py.com.sodep.mobileforms.api.dtos;

public class LabeledElementsDTO implements DTO {
	private static final long serialVersionUID = 1L;
	private String label;
	private Object elements;

	public LabeledElementsDTO(String label, Object elements) {
		this(label);
		if (elements == null) {
			throw new IllegalArgumentException("'elements' can't be null");
		}
		this.elements = elements;
	}

	public LabeledElementsDTO(String label) {
		if (label == null) {
			throw new IllegalArgumentException("'label' can't be null");
		}
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public Object getElements() {
		return elements;
	}

}
