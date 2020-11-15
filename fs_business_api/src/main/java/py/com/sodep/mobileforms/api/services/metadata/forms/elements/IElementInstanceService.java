package py.com.sodep.mobileforms.api.services.metadata.forms.elements;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;

public interface IElementInstanceService {

	// WHAT is "Header Proto"?
	// List<ElementInstance> listAllExcludingHeaderProto(Form form);

	/**
	 * Lists all elements that can be used to enter data
	 * 
	 * (Headlines, for example, are exluded)
	 * 
	 * @param form
	 * @return
	 */
	List<ElementInstance> listAllDataInputElements(Form form);

	ElementInstance findById(Long id);
}
