package py.com.sodep.mobileforms.api.services.metadata.forms.elements;

public class Option {

	private String label;

	private Object value;
	
	public Option(){
		
	}
	
	public Option(String label, Object value){
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
