package py.com.sodep.mobileforms.web.controllers.cruds;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.web.controllers.SodepController;
import py.com.sodep.mobileforms.web.json.JsonResponse;

public abstract class CrudController extends SodepController {

	protected static final String instance = "";
	
	@Autowired
	protected I18nBundle i18nBundle;

	// The annotations are not necessary in this class
	// I leave them just as documentation purpose
	// The subclasses must implement these methods and annotate them
	// correctly

	protected abstract @ResponseBody 
	PagedData<?> read(HttpServletRequest request,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") Integer rows,
			@RequestParam(value = "sidx", required = false) String orderBy,
			@RequestParam(value = "sord", required = false) String order,
			@RequestParam(value = "_search", required = false) String _search,
			@RequestParam(value = "filters", required = false) String filters,
			@RequestParam(value = "searchOper", required = false) String searchOper,
			@RequestParam(value = "searchField", required = false) String searchField,
			@RequestParam(value = "searchString", required = false) String searchString);

	protected abstract @ResponseBody 
	JsonResponse<String> edit(HttpServletRequest request, @RequestParam(value = "oper") String oper,
			@RequestParam(value = "id", required = false) String id);

	//TODO consider refactoring the name of this method to gridInfo and the Request Mappings of the Controllers 
	protected abstract @ResponseBody  
	JsonResponse<?> columnInfo(HttpServletRequest request);

	protected String languageOptions() {
		// FIXME This should be internationalized too
		// this is implemented with a LinkedHashMap, so the order is guaranteed
		Map<String, String> languageMap = i18nBundle.getLanguages();
		Set<String> keySet = languageMap.keySet();
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			sb.append(key);
			sb.append(':');
			sb.append(languageMap.get(key));
			if (iter.hasNext()) {
				sb.append(';');
			} else {
				break;
			}
		}

		return sb.toString();
	}
	
}
