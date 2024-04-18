package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.Map;

/**
 * This interface must be implemented by a Spring Service that will serve to
 * fetch the data necessary to show up a select option on the web.
 * 
 * The Spring Service's name must be set and associated to the multiselect's
 * identifier
 * 
 * @author Miguel
 * 
 */
public interface IMultiselectService {

	/**
	 * An implementation must read the parameters of the request and respond
	 * with a MultiselectServiceResponse.
	 * 
	 * The responpose must contain the result of executing the read request
	 * (success or failure). If successful, the implementation should also
	 * include the list of items (if any) that meet the request parameters.
	 * 
	 * An implementation is chosen by the mapping set between the
	 * implementation's Bean name and the multiselect Id. So a developer must
	 * register its implementation with its given Bean Name and the
	 * multiselect's id.
	 * 
	 * The implementation is responsible of paging as requested!
	 * 
	 * @param request
	 * @return
	 */
	public MultiselectServiceResponse listItems(MultiselectReadRequest request);

	/**
	 * This method should handle request actions other than just "list" the
	 * items. E.g. save the selection.
	 * 
	 * An implementation must read the parameters of the request and apply the
	 * corresponding business logic to execute the action.
	 * 
	 * The possible actions depend totally on the specific implementation.
	 * 
	 * @param request
	 * @return
	 */
	public MultiselectServiceResponse doAction(MultiselectActionRequest request);

	/**
	 * This method should return the multiselect's model. A model defines the
	 * multiselect's labels, how it should look like, etc. Ergo, "Model".
	 * 
	 * @param language
	 * @return
	 */
	public MultiselectModel loadModel(String language,Map<String,String> params);

}
