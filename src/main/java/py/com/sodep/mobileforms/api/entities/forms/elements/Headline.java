package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "forms", name = "elements_headlines")
public class Headline extends ElementPrototype implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	private String value;
//
//	
//	@Column(name = "value")
//	public String getValue() {
//		return value;
//	}
//
//	public void setValue(String value) {
//		this.value = value;
//	}

}
