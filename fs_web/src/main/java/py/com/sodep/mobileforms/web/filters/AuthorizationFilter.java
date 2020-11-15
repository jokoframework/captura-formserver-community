package py.com.sodep.mobileforms.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect; //FIXME Wrong!, web should not depend on py.com.sodep.mobileforms.impl.*
import py.com.sodep.mobileforms.web.session.SessionManager;

/**
 * This filter takes the user from the session and stores it in a ThreadLocal
 * variable, so it is accessible in the {@link AuthorizationAspect}
 * 
 * @author danicricco
 * 
 */
public class AuthorizationFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		SessionManager sessionManager = new SessionManager(httpRequest);
		if (sessionManager.isOpen()) {
			User user = sessionManager.getUser();
			AuthorizationAspect.setUserInRequest(user);
		} else {
			AuthorizationAspect.setUserInRequest(null);
		}
		chain.doFilter(httpRequest, response);
		// make sure that this thread doesn't remind the last user
		AuthorizationAspect.setUserInRequest(null);
	}

}
