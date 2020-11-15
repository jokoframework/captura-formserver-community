package py.com.sodep.mobileforms.api.services.forms.model;

import py.com.sodep.mobileforms.api.entities.forms.elements.Barcode;
import py.com.sodep.mobileforms.api.entities.forms.elements.Checkbox;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Headline;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Location;
import py.com.sodep.mobileforms.api.entities.forms.elements.Photo;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.forms.elements.Signature;


@Deprecated
// El nombre de la clase podría fácilmente guardarse como parte del proto 
// 
// Esta clase tiene que dejar de existir. No tiene mucho sentido. Además 
// agrega un paso más a las modificaciones necesarias para agregar un 
// item al toolbox (ticket CAP-31)
public class ElementPrototypeUtils {

	public static String getName(ElementPrototype proto) {
		if (proto instanceof Select) {
			return "select";
		} else if (proto instanceof Photo) {
			return "photo";
		} else if (proto instanceof Headline) {
			return "headline";
		} else if (proto instanceof Location) {
			return "location";
		} else if (proto instanceof Input) {
			Input input = (Input) proto;
			return input.getType().toString();
		} else if (proto instanceof Checkbox) {
			return "checkbox";
		} else if(proto instanceof Barcode){
			return "barcode";
		} else if (proto instanceof Signature){
			return "signature";
		}
		throw new RuntimeException("Unknown name for proto " + proto.getClass().getSimpleName());
	}

}
