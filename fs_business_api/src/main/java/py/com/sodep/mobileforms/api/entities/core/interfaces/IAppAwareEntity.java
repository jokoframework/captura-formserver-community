package py.com.sodep.mobileforms.api.entities.core.interfaces;

import py.com.sodep.mobileforms.api.entities.application.Application;

/**
 * Some entities "belong" to an application.
 * 
 * This is mostly a tagging interface. If an entity implements this interface it
 * means that it is contained in the app scope/context directly.
 * 
 * Some entities do not implement this interface because they are contained by
 * some other entity that might be App aware or contained by another App aware
 * entity.
 * 
 * @author Miguel
 * 
 */
public interface IAppAwareEntity {
	
	public static final String APPLICATION = "application";

	public Application getApplication();

}
