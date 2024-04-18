package py.com.sodep.mobileforms.api.services.config;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.sys.IParameter;

/**
 * This class stores a copy of the system parameters in memory. The system
 * parameter are loaded once at startup and keep in a shared memory. If the
 * parameter is changed on the database it is required to call the method
 * {@link #reload()} to apply the changes.
 * 
 * @author danicricco
 * 
 */
public interface ISystemParametersBundle {

	void reload();

	/**
	 * This is the same as calling {@link #getParameter(Long)} and then casting
	 * it to Integer
	 * 
	 * @param id
	 * @return
	 */
	Integer getIntValue(Long id);

	/**
	 * This is the same as calling {@link #getParameter(Long)} and then
	 * obtaining the value
	 * 
	 * @param id
	 * @return
	 */
	String getStrValue(Long id);

	/**
	 * This is the same as calling {@link #getParameter(Long)} and then casting
	 * it to Long
	 * 
	 * @param id
	 * @return
	 */
	Long getLongValue(Long id);

	/**
	 * This is the same as calling {@link #getParameter(Long)} and then casting
	 * it to Boolean
	 * 
	 * @param id
	 * @return
	 */
	Boolean getBoolean(Long id);

	List<String> getListValues(Long id);

	IParameter getParameter(Long id);

}
