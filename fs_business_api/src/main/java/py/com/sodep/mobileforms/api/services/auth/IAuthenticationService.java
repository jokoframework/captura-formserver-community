package py.com.sodep.mobileforms.api.services.auth;

import py.com.sodep.mobileforms.api.entities.core.User;

/**
 * An implementation of this interface should implement the methods to check if
 * the user's credentials are correct
 *
 * @author Miguel
 */
public interface IAuthenticationService {

    boolean checkCredentials(String mail, String password);

    boolean checkCredentials(String mail, String password, String deviceToken);

    public boolean checkCredentials(User user, String password);

    public boolean checkCredentialsAndDevice(User user, String deviceToken);

}
