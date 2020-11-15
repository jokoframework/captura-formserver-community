package py.com.sodep.mobileforms.web.endpoints.documentation.swagger;

import com.mangofactory.swagger.core.SwaggerPathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;

public class ApiPathProvider implements SwaggerPathProvider {
	
	private SwaggerPathProvider defaultSwaggerPathProvider;
	
	@Autowired
	private ServletContext servletContext;

	private String docsLocation;

	public ApiPathProvider(String docsLocation) {
		this.docsLocation = docsLocation;
	}

	@Override
	public String getApiResourcePrefix() {
		return defaultSwaggerPathProvider.getApiResourcePrefix();
	}

	public String getAppBasePath() {
		return UriComponentsBuilder.fromHttpUrl(docsLocation).path(servletContext.getContextPath()).pathSegment("api").build().toString();
	}

	@Override
	public String getSwaggerDocumentationBasePath() {
		// FIXME hardcoded path to API
		return UriComponentsBuilder.fromHttpUrl(getAppBasePath()).pathSegment("api-docs/").build().toString();
	}

	@Override
	public String getRequestMappingEndpoint(String requestMappingPattern) {
		return defaultSwaggerPathProvider.getRequestMappingEndpoint(requestMappingPattern);
	}

	public void setDefaultSwaggerPathProvider(SwaggerPathProvider defaultSwaggerPathProvider) {
		this.defaultSwaggerPathProvider = defaultSwaggerPathProvider;
	}
}
