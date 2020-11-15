package py.com.sodep.mobileforms.api.services.logging;

import py.com.sodep.mobileforms.api.entities.log.LoginType;
import py.com.sodep.mobileforms.api.entities.log.UncaughtException;

/**
 * An implementation of this interface should allow logging to the database
 * 
 * (more methods should be defined soon)
 * 
 * @author Miguel
 * 
 */
public interface IDBLogging {

	UncaughtException logException(UncaughtException exception);
	
	/**
	 * 
	 * @param userId
	 * @param applicationId
	 */
	void logLogin(Long userId, Long applicationId);

	void logLogin(Long userId, Long applicationId, LoginType loginType);

}
