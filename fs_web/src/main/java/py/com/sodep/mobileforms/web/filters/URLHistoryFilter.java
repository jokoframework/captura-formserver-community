package py.com.sodep.mobileforms.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter will search for a specific string on an URL and remove whatever
 * is after the token. Since our application is full ajax we have implemented a
 * custom back and forward that is rewriting the browser's URL. If the user
 * refresh the page after the URL rewriting he will be redirected to a 404 not
 * found page. (see #2917)
 * 
 * @author danicricco
 * 
 */
public class URLHistoryFilter implements Filter {
	
	private static Logger logger = LoggerFactory.getLogger(URLHistoryFilter.class);

	private String key = "mfh_=";
	private String defaultURL = "";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		key = filterConfig.getInitParameter("key");
		String defaultSuffix = filterConfig.getInitParameter("defaultURL");
		if (defaultSuffix != null && defaultSuffix.length() > 0) {
			if (defaultSuffix.startsWith("/")) {
				defaultURL = defaultSuffix;
			} else {
				defaultURL = "/" + defaultSuffix;
			}
		}

//		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig
//				.getServletContext());
//		systemParametersBundle = wac.getBean(ISystemParametersBundle.class);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String uri = httpRequest.getRequestURI();
		int index = uri.indexOf(key);

		if (index > 0) {
		// This should only be used by the services where there's no notion of the 
		// http request
		// This caused the bug #3680
		//	String contextPath = systemParametersBundle.getStrValue(DBParameters.CONTEXT_PATH);
			String redirectURI = getRedirectURI(httpRequest); 
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect(redirectURI);
		} else {
			chain.doFilter(httpRequest, response);
		}

	}

	private String getRedirectURI(HttpServletRequest httpRequest) {
		logger.info("Sending redirect");
		String url = httpRequest.getRequestURL().toString();
		logger.info("Requested URL " + url);
		String contextPath = httpRequest.getContextPath();
		int indexContextPath = url.indexOf(contextPath + "/");
		String redirectURI = url.substring(0, indexContextPath + contextPath.length()) + defaultURL;
		return redirectURI;
	}

	@Override
	public void destroy() {

	}

}
