package py.com.sodep.mobileforms.api.entities.forms.elements;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "forms", name = "elements_barcodes")
public class Barcode extends ElementPrototype {

	//TODO qué propiedades tiene un barcode? Cómo pasa los datos?
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
