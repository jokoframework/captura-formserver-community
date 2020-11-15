package py.com.sodep.mobileforms.web.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

public class ApplicationLicenseFilter implements Filter {

	private MFLicenseManager licenseManager;

	private IAuthorizationControlService authorizationControlService;
	
	private ISystemParametersBundle systemParametersBundle;
	
	private static final int DEFAULT_ABOUT_TO_EXPIRE_NOTIFICATION = 15;

	private static final Logger logger = LoggerFactory.getLogger(ApplicationLicenseFilter.class);

	class ResponseWrapper extends HttpServletResponseWrapper {

		private ByteArrayOutputStream outputStrem = new ByteArrayOutputStream();

		private PrintWriter writer = new PrintWriter(outputStrem, true);

		private ServletOutputStream sos = new ServletOutputStream() {

			@Override
			public void write(int b) throws IOException {
				outputStrem.write(b);
			}
		};

		@Override
		public PrintWriter getWriter() throws IOException {
			return writer;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return sos;
		}

		public ResponseWrapper(HttpServletResponse response) {
			super(response);

		}

		public byte[] getWrittenBytes() {
			writer.flush();
			return outputStrem.toByteArray();
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig
				.getServletContext());
		licenseManager = wac.getBean(MFLicenseManager.class);
		authorizationControlService = wac.getBean(IAuthorizationControlService.class);
		systemParametersBundle = wac.getBean(ISystemParametersBundle.class);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		SessionManager sessionManager = new SessionManager(httpRequest);
		if (sessionManager.isOpen()) {
			User user = sessionManager.getUser();
			Application application = sessionManager.getApplication();
			I18nManager i18n = sessionManager.getI18nManager();
			// if the user has the authorization to see the applications settings
			// then he/she can do something about the license expiring
			if (user != null && application != null && i18n != null
					&& authorizationControlService.hasAppLevelAccess(application.getId(), user,
							AuthorizationNames.App.MENU_CONFIG)) {
				MFApplicationLicense applicationLicense = licenseManager.getLicense(application.getId());
				Date expirationDate = licenseManager.caculateExpirationDate(applicationLicense);
				Date now = new Date();
				
				if (expirationDate != null && now.after(expirationDate)) {
					ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);
					// pass on the request to the other filters and/or servlets
					chain.doFilter(request, responseWrapper);
					// the response is not written yet to the outputStream of the
					// HttpServletResponse
					String htmlResponse = getHTMLResponse(responseWrapper);
					
					String modifiedResponse = addExpirationMessage(i18n, htmlResponse, "label-important", "web.home.application.license.expired");
					httpResponse.getWriter().print(modifiedResponse);
					return;
				} else if (expirationDate != null && getWarningDate().after(expirationDate)) {
					ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);
					// pass on the request to the other filters and/or servlets
					chain.doFilter(request, responseWrapper);
					// the response is not written yet to the outputStream of the
					// HttpServletResponse
					String originalResponse = getHTMLResponse(responseWrapper);
					String modifiedResponse = addExpirationMessage(i18n, originalResponse, "label-warning", "web.home.application.license.aboutToExpire");
					httpResponse.getWriter().print(modifiedResponse);
					return;
				}
			} else {
				if (user == null || application == null || i18n == null) {
					logger.warn("user = " + user + ", application =" + application + ", i18n = " + i18n);
				}
			}
		}
		chain.doFilter(request, response);
	}

	private Date getWarningDate() {
		Date now = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		Integer value = systemParametersBundle.getIntValue(DBParameters.ABOUT_TO_EXPIRE_NOTIFICATION);
		value = value == null ? DEFAULT_ABOUT_TO_EXPIRE_NOTIFICATION : value;
		c.add(Calendar.DAY_OF_MONTH, value);
		Date time = c.getTime();
		return time;
	}

	private String addExpirationMessage(I18nManager i18n, String originalHTML, String labelClass, String message) {
		StringBuilder sb = new StringBuilder();
		int closingBody = originalHTML.indexOf("</body>");
		if (closingBody != -1) {
			sb.append(originalHTML.substring(0, closingBody));
			String remaining = originalHTML.substring(closingBody);
			
			sb.append("<div id=\"licenseWarning\" style=\"display:none\" class=\"row-fluid\">" 
					+ "<div class=\"span12\" style=\"text-align:center\">"
					+ "<span class=\"label " + labelClass + "\">" + i18n.getMessage(message) + "</span>" 
					+ "</div>"  
					+ "</div>" 
					+ "<script>\n" 
					+ "\t$('#mf_main_div').prepend($('#licenseWarning'));\n" 
					+ "\t$('#licenseWarning').show()\n"
					+ "</script>\n");
			sb.append(remaining);
		}
		return sb.toString();
	}

	private String getHTMLResponse(ResponseWrapper responseWrapper) throws UnsupportedEncodingException {
		byte[] writtenBytes = responseWrapper.getWrittenBytes();
		String text = new String(writtenBytes, responseWrapper.getCharacterEncoding());
		return text;
	}

	@Override
	public void destroy() {

	}
}
