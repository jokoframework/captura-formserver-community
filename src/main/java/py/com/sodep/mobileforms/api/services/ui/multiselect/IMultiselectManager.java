package py.com.sodep.mobileforms.api.services.ui.multiselect;

import java.util.Map;

public interface IMultiselectManager {
	
	public MultiselectModel loadModel(String id, String language,Map<String,String> params);

	public MultiselectServiceResponse listItems(String id, MultiselectReadRequest request);

	public MultiselectServiceResponse doAction(String id, MultiselectActionRequest request);

}
