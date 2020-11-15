package py.com.sodep.mobileforms.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.json.JsonSerializer;

/**
 * 
 * An object of this class is used as a callback for the onAuthenticationFailure
 * method when the Spring's authentication did not succeed. There are two
 * possible flows:
 * <ul>
 * <li>jsonResponse==true: An object of type {@link JsonResponse} will be send
 * with the flag success set to false</li>
 * <li>jsonResponse==false: If forwardToDestination==true will make a forward to
 * the page defaultFailureUrl. Otherwise, a redirect to the defaultFailureUrl</li>
 * </ul>
 * 
 * 
 */
public class WebAuthFailureHandler implements AuthenticationFailureHandler {
	private static final Logger logger = Logger.getLogger(AuthenticationFailureHandler.class);

	private String defaultFailureUrl;
	private boolean forwardToDestination = false;
	private boolean allowSessionCreation = true;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	/**
	 * If this flag is set to true, then a json response will be send indicating
	 * that the user couldn't login.
	 */
	private boolean jsonResponse;

	public WebAuthFailureHandler() {
	}

	public WebAuthFailureHandler(String defaultFailureUrl) {
		setDefaultFailureUrl(defaultFailureUrl);
	}

	/**
	 * Performs the redirect or forward to the {@code defaultFailureUrl} if set,
	 * otherwise returns a 401 error code.
	 * <p>
	 * If redirecting or forwarding, {@code saveException} will be called to
	 * cache the exception for use in the target view.
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		if (jsonResponse) {
			JsonSerializer.writeOperationFail(response, "Login failure");
		} else {
			if (defaultFailureUrl == null) {
				logger.debug("No failure URL set, sending 401 Unauthorized error");

				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
						"Authentication Failed: " + exception.getMessage());
			} else {
				storeExceptionInSession(request, exception);

				if (forwardToDestination) {
					logger.debug("Forwarding to " + defaultFailureUrl);

					request.getRequestDispatcher(defaultFailureUrl).forward(request, response);
				} else {
					logger.debug("Redirecting to " + defaultFailureUrl);
					redirectStrategy.sendRedirect(request, response, defaultFailureUrl);
				}
			}

		}

	}

	public void setJsonResponse(boolean jsonResponse) {
		this.jsonResponse = jsonResponse;
	}

	/**
	 * Caches the {@code AuthenticationException} for use in view rendering.
	 * <p>
	 * If {@code forwardToDestination} is set to true, request scope will be
	 * used, otherwise it will attempt to store the exception in the session. If
	 * there is no session and {@code allowSessionCreation} is {@code true} a
	 * session will be created. Otherwise the exception will not be stored.
	 */
	protected final void storeExceptionInSession(HttpServletRequest request, AuthenticationException exception) {
		if (forwardToDestination) {
			request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
		} else {
			HttpSession session = request.getSession(false);

			if (session != null || allowSessionCreation) {
				request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
			}
		}
	}

	/**
	 * The URL which will be used as the failure destination.
	 * 
	 * @param defaultFailureUrl
	 *            the failure URL, for example "/loginFailed.jsp".
	 */
	public void setDefaultFailureUrl(String defaultFailureUrl) {
		Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'" + defaultFailureUrl
				+ "' is not a valid redirect URL");
		this.defaultFailureUrl = defaultFailureUrl;
	}

	protected boolean isUseForward() {
		return forwardToDestination;
	}

	/**
	 * If set to <tt>true</tt>, performs a forward to the failure destination
	 * URL instead of a redirect. Defaults to <tt>false</tt>.
	 */
	public void setUseForward(boolean forwardToDestination) {
		this.forwardToDestination = forwardToDestination;
	}

	/**
	 * Allows overriding of the behaviour when redirecting to a target URL.
	 */
	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	protected boolean isAllowSessionCreation() {
		return allowSessionCreation;
	}

	public void setAllowSessionCreation(boolean allowSessionCreation) {
		this.allowSessionCreation = allowSessionCreation;
	}
}