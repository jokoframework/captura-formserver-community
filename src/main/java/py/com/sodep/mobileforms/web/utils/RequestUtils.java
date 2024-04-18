package py.com.sodep.mobileforms.web.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestUtils {
	
	public static String INDENT_UNIT = "\t";
	
	
	public static String DEVICE = "DEVICE";
	
	public static String WEB = "WEB";
	
	public static String CONNECTOR_REPOSITORY = "CONNECTOR_REPOSITORY";
	
	public static String REST_CLIENT = "REST_CLIENT";
	
	public static String UNKNOWN = "UNKNOWN";
	
	
	// Private helper methods
	
	private static String debugStringSession(HttpSession session, int indent)
	{
		String indentString = RequestUtils.repeat(INDENT_UNIT, indent);
		if (session == null)
			return indentString + "{ }";
		StringBuilder sb = new StringBuilder();
		sb.append(indentString).append("{\n");
		sb.append(indentString).append(INDENT_UNIT).append("'id': '").append(session.getId()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'last_accessed_time': ").append(session.getLastAccessedTime()).append(", \n");
		sb.append(indentString).append(INDENT_UNIT).append("'max_inactive_interval': ").append(session.getMaxInactiveInterval()).append(", \n");
		sb.append(indentString).append(INDENT_UNIT).append("'is_new': '").append(session.isNew()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'attributes': {\n");
		Enumeration<String> attributeNames = session.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attributeName = (String) attributeNames.nextElement();
			Object o = session.getAttribute(attributeName);
			sb.
				append(indentString).
				append(INDENT_UNIT).
				append("'").append(attributeName).append("': ").
				append("'").append(o.toString()).append("',\n");
		}
		sb.append(indentString).append(INDENT_UNIT).append("}\n");
		sb.append(indentString).append("}\n");
		return sb.toString();
	}
	
	private static String debugStringParameter(String indentString, String parameterName, String[] parameterValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.
			append(indentString).
			append(INDENT_UNIT).
			append("'").append(parameterName).append("': ");
		if (parameterValues == null || parameterValues.length == 0) {
			sb.append("None");
		} else {
			if (parameterValues.length > 1) sb.append("[");
			sb.append(RequestUtils.join(parameterValues, ","));
			if (parameterValues.length > 1) sb.append("]");
		}
		return sb.toString();
	}
	
	private static String debugStringHeader(String indentString, String headerName, List<String> headerValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.
			append(indentString).
			append(INDENT_UNIT).
			append("'").append(headerName).append("': ");
		if (headerValues == null || headerValues.size() == 0) {
			sb.append("None");
		} else {
			if (headerValues.size() > 1) sb.append("[");
			sb.append(RequestUtils.join(headerValues, ","));
			if (headerValues.size() > 1) sb.append("]");
		}
		return sb.toString();
	}
	
	private static String debugStringParameters(HttpServletRequest request, int indent)
	{
		String indentString = RequestUtils.repeat(INDENT_UNIT, indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indentString).append("{\n");
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = (String) parameterNames.nextElement();
			String[] parameterValues = request.getParameterValues(parameterName);
			//List<String> headerValuesList = new ArrayList<String>(); 
			sb.
				append(RequestUtils.debugStringParameter(indentString, parameterName, parameterValues)).
				append(",\n");
		}
		sb.append(indentString).append("}\n");
		return sb.toString();
	}
	
	private static String debugStringCookie(Cookie cookie, String indentString)
	{
		if (cookie == null) return "";
		StringBuilder sb = new StringBuilder();
		sb.append(indentString).append("{ \n");
		sb.append(indentString).append(INDENT_UNIT).append("'name': '").append(cookie.getName()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'value': '").append(cookie.getValue()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'domain': '").append(cookie.getDomain()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'path': '").append(cookie.getPath()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'max_age': ").append(cookie.getMaxAge()).append(", \n");
		sb.append(indentString).append(INDENT_UNIT).append("'version': ").append(cookie.getVersion()).append(", \n");
		sb.append(indentString).append(INDENT_UNIT).append("'comment': '").append(cookie.getComment()).append("', \n");
		sb.append(indentString).append(INDENT_UNIT).append("'secure': '").append(cookie.getSecure()).append("',\n");
		sb.append(indentString).append("}");
		return sb.toString();
	}
	
	private static String debugStringCookies(HttpServletRequest request, int indent)
	{
		if (request.getCookies() == null)
			return "";
		String indentString = RequestUtils.repeat(INDENT_UNIT, indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indentString).append("[\n");
		int cookieCount = 0;
		for (Cookie cookie : request.getCookies()) {
			sb.append(RequestUtils.debugStringCookie(cookie, indentString + INDENT_UNIT)).append(",\n");
			cookieCount++;
		}
		if (cookieCount > 0) {
			sb.delete(sb.length() - ",\n".length(), sb.length());
		}
		sb.append("\n").append(indentString).append("]\n");
		return sb.toString();
	}
	
	private static String debugStringHeaders(HttpServletRequest request, int indent)
	{
		String indentString = RequestUtils.repeat(INDENT_UNIT, indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indentString).append("{\n");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			Enumeration<String> headerValues = request.getHeaders(headerName);
			List<String> headerValuesList = new ArrayList<String>(); 
			while (headerValues.hasMoreElements()) {
				String headerValue = (String) headerValues.nextElement();
				headerValuesList.add(headerValue);
			}
			sb.
				append(RequestUtils.debugStringHeader(indentString, headerName, headerValuesList)).
				append(",\n");
		}
		sb.append(indentString).append("}\n");
		return sb.toString();
	}
	
	private static boolean isDevice(String agent) {
		if (agent != null) {
			String a = agent.toLowerCase();
			if (a.indexOf("android") >= 0 || a.indexOf("iphone") >= 0) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isRestClient(String agent) {
		if (agent != null) {
			String a = agent.toLowerCase();
			// TODO there could be more strings in the user-agent.
			// For now, we use these because we ALWAYS implement
			// the REST communication using jersey
			if (a.indexOf("java") >= 0 || a.indexOf("jersey") >= 0) {
				return true;
			}
		}
		return false;
	}

	
	// API
	
	// HELPER methods
	
	// Added a few helper methods to use. 
	// alternatively, you could use Apache's Common Lang library
	
	// Alternative: org.apache.commons.lang.StringUtils.repeat
	// Note: I guess performance wise, this is probably way worse than Apache's repeat
	public static String repeat(String what, int times)
	{
		if (times <= 0)
			return "";
		StringBuilder sb = new StringBuilder();
		int i;
		for (i=0; i<times; i++)
			sb.append(what);
		return sb.toString();
	}
	
	// Alternative: org.apache.commons.lang.StringUtils.join
	// Note: do keep in mind that RequestUtils.join will add single-quotes to values
	public static String join(List<String> values, String conjuction)
	{
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			sb.append("'").append(value).append("'").append(conjuction);
		}
		sb.delete(sb.length() - conjuction.length(), sb.length());
		return sb.toString();
	}
	
	public static String join(String[] values, String conjuction)
	{
		return RequestUtils.join(Arrays.asList(values), conjuction);
	}
	
	
	/**
	 * Debug request's headers
	 * @param request Request parameter.
	 * @return A string with debug information on Request's header
	 */
	public static String debugStringHeaders(HttpServletRequest request)
	{
		return RequestUtils.debugStringHeaders(request, 0);
	}
	
	/**
	 * Debug request's parameters
	 * @param request Request parameter.
	 * @return A string with debug information on Request's header
	 */
	public static String debugStringParameters(HttpServletRequest request)
	{
		return RequestUtils.debugStringParameters(request, 0);
	}
	
	/**
	 * Debug request's cookies
	 * @param request Request parameter
	 * @return A string with debug information on Request's cookies
	 */
	public static String debugStringCookies(HttpServletRequest request)
	{
		return RequestUtils.debugStringCookies(request, 0);
	}

	/**
	 * 
	 * @param session
	 * @return
	 */
	public static String debugStringSession(HttpSession session)
	{
		return RequestUtils.debugStringSession(session, 0);
	}
	
	/**
	 * Debug complete request
	 * @param request Request parameter.
	 * @param printSession Enable session information printing
	 * @return A string with debug information on Request's header
	 */
	public static String debugString(HttpServletRequest request, boolean printSession)
	{
		StringBuilder sb = new StringBuilder();
		
		// GENERAL INFO
		sb.append(debugStringGeneralInfo(request));
		
		
		//sb.append("PATH INFO: ").append(request.getPathInfo()).append("\n");
		//sb.append("PATH TRANSLATED: ").append(request.getPathTranslated()).append("\n");
		
		// COOKIES
		sb.append("COOKIES:\n");
		sb.append("-------\n");
		sb.append(RequestUtils.debugStringCookies(request, 1));
		
		// PARAMETERS
		sb.append("PARAMETERS:\n");
		sb.append("----\n");
		sb.append(RequestUtils.debugStringParameters(request, 1));
		
		// HEADERS
		sb.append("HEADERS:\n");
		sb.append("-------\n");
		sb.append(RequestUtils.debugStringHeaders(request, 1));
		
		// SESSION
		if (printSession) {
			sb.append("SESSION:\n");
			sb.append("-------\n");
			HttpSession session = request.getSession(false);
			if (session != null) {
				sb.append(RequestUtils.debugStringSession(session, 1));
			} else {
				sb.append("NO SESSION AVAILABLE\n");
			}
		}
		
		return sb.toString();
	}

	public static String debugStringGeneralInfo(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append("PROTOCOL: ").append(request.getProtocol()).append("\n");
		sb.append("METHOD: ").append(request.getMethod()).append("\n");
		sb.append("QUERY STRING: ").append(request.getQueryString()).append("\n");
		sb.append("REQUEST URI: ").append(request.getRequestURI()).append("\n");
		return sb.toString();
	}


	/**
	 * Call debugString with 'false' value on session information
	 * @param request Request parameter.
	 * @return A string with debug information on Request's header but no information on session
	 */
	public static String debugString(HttpServletRequest request)
	{
		return RequestUtils.debugString(request, false);
	}
	
	
	public static String parseLoginType(HttpServletRequest request)
	{
		String agent = request.getHeader("user-agent");
		String device = request.getParameter("device");
		
		return parseLoginType(agent, device);
	}

	private static String parseLoginType(String agent, String device) {
		if (device != null) {
			device = device.trim().toLowerCase();
			if (device.toLowerCase().equals("false") && isRestClient(agent)) {
				return CONNECTOR_REPOSITORY;
			} else if (device.equals("true")) {
				return DEVICE;
			}
		}
		
		if (agent !=null) {
			if (isDevice(agent)) {
				return DEVICE;
			} else if (isRestClient(agent)) {
				return REST_CLIENT;
			} else {
				// we have user agent and 
				// none of the above clients were
				// detected, so we asume is a login
				// from a browser
				return WEB;
			}
		}
		
		// No user-agent info, no parameters info
		return UNKNOWN;
	}
	
	
	public static void main(String[] args) 
	{
		List<String> strs = new ArrayList<String>();
		strs.add("Hello");
		//strs.add("Constant");
		//strs.add("Concept");
		System.out.println(RequestUtils.join(strs, ",\n"));
		
		String agent = "Dalvik/1.6.0 (Linux; U; Android 4.4.4; XT1033 Build/KXB21.14-L1.40)";
		String device = null;
		
		System.out.println(RequestUtils.parseLoginType(agent, device));
		
		agent = "Java/1.7.0_45";
		device = "false";
		System.out.println(RequestUtils.parseLoginType(agent, device));
		
		agent = "Jersey/2.4 (HttpUrlConnection 1.7.0_45)";
		device = null;
		System.out.println(RequestUtils.parseLoginType(agent, device));
		
	}

}
