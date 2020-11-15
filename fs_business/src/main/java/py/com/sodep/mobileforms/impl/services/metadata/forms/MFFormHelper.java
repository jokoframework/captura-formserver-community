package py.com.sodep.mobileforms.impl.services.metadata.forms;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;

//FIXME this class is used in the web layer directly. mf_business classes shouldn't be used in the 
// web layer. Move it to the api project.
/**
 * This class encapuslates some useful methods to work with MFElements
 * 
 * @author danicricco
 * 
 */
public class MFFormHelper {

	/**
	 * MField has information about the data type and the column name, while
	 * an MFElement contains information about the process item (how it will be
	 * rendered on the different devices). This method is very useful to
	 * determine the data type used to stored a given element.
	 * 
	 * @param e
	 * @return
	 */
	public static MFField mfElementToField(MFElement e) {
		if (!e.getProto().isOutputOnly()) {
			FIELD_TYPE type = FIELD_TYPE.STRING;
			switch (e.getProto().getType()) {
			case INPUT:
				MFInput mffinput = (MFInput) e.getProto();
				switch (mffinput.getSubtype()) {
				case TIME:
				case DATE:
				case DATETIME:
					type = FIELD_TYPE.DATE;
					break;
				case DECIMAL:
				case INTEGER:
					type = FIELD_TYPE.NUMBER;
					break;
				}
				break;
			case PHOTO:
				type = FIELD_TYPE.BLOB;
				break;
			case LOCATION:
				type = FIELD_TYPE.LOCATION;
				break;
			case CHECKBOX:
				type = FIELD_TYPE.BOOLEAN;
				break;
			case SIGNATURE:
				type = FIELD_TYPE.BLOB;
				break;
			}

			MFField mfField = new MFField(type, e.getInstanceId());
			return mfField;
		} else {
			throw new ApplicationException("Only input elements can be translated to MFField");
		}
	}

}
