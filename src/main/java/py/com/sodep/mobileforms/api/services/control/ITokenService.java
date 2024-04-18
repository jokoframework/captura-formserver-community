package py.com.sodep.mobileforms.api.services.control;

import java.util.Date;

import py.com.sodep.mobileforms.api.entities.core.Token;
import py.com.sodep.mobileforms.api.entities.core.User;

public interface ITokenService {

	boolean isValid(User grantee, String token, int purpose);

	int purpose(User grantee, String token);

	Token createNewToken(User currentUser, User grantee, int purpose, Date expires);

	boolean useToken(User grantee, String token);

	/**
	 * Returns a Token or null
	 * 
	 * @param token
	 * @return
	 */
	Token getToken(String token);

	/**
	 * Deletes all the tokens granted to the user with the given purpose
	 * 
	 * If no token was deleted it returns false
	 * 
	 * @param grantee
	 * @param purpose
	 * @return
	 */
	boolean deleteTokens(User grantee, int purpose);

	boolean deactivateToken(Long id);

}
