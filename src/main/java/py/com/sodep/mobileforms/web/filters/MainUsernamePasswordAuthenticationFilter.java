package py.com.sodep.mobileforms.web.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import py.com.sodep.mobileforms.web.json.JsonSerializer;

/**
 * This filter sends a response to an AJAX request if the session has expired.
 * e.g. A user's session has expired and by clicking on a button, he/she sends a
 * new request.
 * 
 * Without this filter he/she will get a parse error. This filter understands
 * that the request is an AJAX request and sends a JSON response.
 * 
 */
public class MainUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final Logger logger = Logger.getLogger(MainUsernamePasswordAuthenticationFilter.class);

	private String apiUriPrefix;

	public String getApiUriPrefix() {
		return apiUriPrefix;
	}

	public void setApiUriPrefix(String apiUriPrefix) {
		this.apiUriPrefix = apiUriPrefix;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		// Is user not authenticated
		if (authentication == null) {
			String contextPath = request.getContextPath();
			String requestURI = request.getRequestURI();
			
			// FIXME
			// This is necessary to allow login through the web api
			String path = contextPath + apiUriPrefix;
			if (requestURI.startsWith(path + "/authentication/login")) {
				super.doFilter(req, res, chain);
				return;
			}
			
			// FIXME
			// If the URI prefix starts with "apiUriPrefix" and is not
			// authenticated, return with error.
			// This is used to give HTTP errors on web api requests that are
			// not authenticated
			if (requestURI.startsWith(path)) {
				logger.debug("Unauthenticated request: " + requestURI);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				// Bug fix #3555. Don't delete
				response.setHeader("WWW-Authenticate", "Basic realm=\"none\"");
				//1) If Basic is set, when making a request with swagger ui a pop will be shown
				//2) But none is not parsed correctly by android
				// http://stackoverflow.com/questions/11810447/httpurlconnection-worked-fine-in-android-2-x-but-not-in-4-1-no-authentication-c
				// response.setHeader("WWW-Authenticate", "none");
				// -------------
				JsonSerializer.writeOperationFail(response, "Unauthenticated request");
				logger.debug("Sending status code: " + response.getStatus());
				return;
			}

			// Is this an Ajax request
			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {

				// Only return the error if the request is not going to
				// 'login', otherwise the user won't be able to
				// login
				if (!"/login".equals(request.getServletPath())) {
					logger.debug("Ajax request received without an authenticated user. Responding JSON with error code "
							+ HttpServletResponse.SC_UNAUTHORIZED);

					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					JsonSerializer
							.writeOperationFail(response, "Session invalid! You must redirect to the login page.");
					return;
				}
			}
		}
		// User is authenticated or in the process of authentication, so just
		// follow
		super.doFilter(req, res, chain);
	}
}