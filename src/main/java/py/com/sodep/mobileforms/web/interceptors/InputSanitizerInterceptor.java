package py.com.sodep.mobileforms.web.interceptors;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class InputSanitizerInterceptor extends HandlerInterceptorAdapter  {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			Enumeration<String> parameterNames = request.getParameterNames();
			boolean htmlContent = false;
			while(parameterNames.hasMoreElements()){
				String name = parameterNames.nextElement();
				String value = request.getParameter(name);
				if(value.matches(".*\\<[^>]+>.*")){ // we just look for tags
					htmlContent = true;
					break;
				}
			}
			if (!htmlContent) {
				return true;
			}
			response.sendError(499, "Invalid input content"); // FIXME i18n
		} catch (Exception e) {

		}
		return false;
	}

}
