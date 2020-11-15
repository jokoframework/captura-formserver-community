package py.com.sodep.mobileforms.api.services.scripting;

import java.util.List;
import java.util.Map;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.scripting.SodepScript;

/**
 * An implementation of this service should allow saving, retrieving and
 * executing Groovy Scripts
 * 
 * @author Miguel
 * 
 */
public interface IScriptingService {

	String executeScript(User user, String script);

	String executeScript(User user, String script, Map<String, ? extends Object> params);

	SodepScript saveScript(User user, String script, String name);

	List<SodepScript> listScripts(User user);

	SodepScript getScript(User user, Long id);

	SodepScript getScript(User user, String name);

}
