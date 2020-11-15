package py.com.sodep.mobileforms.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.IAuthenticationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;

/**
 * 
 * Reference:
 * http://stackoverflow.com/questions/3205469/custom-authentication-in-spring
 * 
 * @author Humber Aquino
 * 
 */
public class MainAuthProvider implements AuthenticationProvider {
	
	private static final Logger logger = Logger.getLogger(MainAuthProvider.class);

	@Autowired
	private IAuthenticationService authService;

	@Autowired
	private IUserService userService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
				"Only UsernamePasswordAuthenticationToken is supported");

		final UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
		String mail = userToken.getName();
		String password = (String) authentication.getCredentials();

		logger.debug("Authenticating user: " + mail);
		User user = userService.findByMail(mail);
		
		if(user == null){
			throw new BadCredentialsException("Bad credentials for '" + mail + "'");
		}
		
		boolean checkCredentials = authService.checkCredentials(user, password);
		if (!checkCredentials) {
			throw new BadCredentialsException("Bad credentials for '" + mail + "'");
		}
		
		List<Application> apps = userService.listActiveApplications(user);
		if ((apps == null || apps.isEmpty()) && !user.isRootUser()) {
			throw new BadCredentialsException("User '" + mail + "' has no active application");
		}

		String roles[] = null;
		if (user.isRootUser()) {
			roles = new String[] { "ROLE_SYSTEM", "ROLE_USER" };
		} else {
			roles = new String[] { "ROLE_USER" };
		}
		UsernamePasswordAuthenticationToken authenticatedUser = createSuccessfulAuthentication(mail, password, null,
				roles);
		return authenticatedUser;

	}

	protected UsernamePasswordAuthenticationToken createSuccessfulAuthentication(String mail, String password,
			Object details, String[] authoritiesList) {

		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (String authority : authoritiesList) {
			authorities.add(new SimpleGrantedAuthority(authority));
		}
		Collection<GrantedAuthority> unmodifiableCollection = Collections.unmodifiableCollection(authorities);
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(mail, password,
				unmodifiableCollection);
		result.setDetails(details);

		return result;
	}

	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

}