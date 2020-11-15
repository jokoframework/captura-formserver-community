package py.com.sodep.mobileforms.api.entities.forms.elements;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "forms", name = "elements_signatures")
public class Signature extends ElementPrototype {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
