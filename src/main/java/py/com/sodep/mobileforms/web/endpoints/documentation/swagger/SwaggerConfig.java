package py.com.sodep.mobileforms.web.endpoints.documentation.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	public static final String SWAGGER_GROUP = "mobile-api";

	@Value("${webapi.location}")
	private String docsLocation;


	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName(SWAGGER_GROUP)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage("py.com.sodep.mobileforms.web.endpoints"))
				.paths(PathSelectors.regex("/metadata/.*|/lookupTable/.*|/document.*|/public/mobile/.*|/public/ping|/authentication/.*|/workflow/.*"))
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Captura API")
				.description("API for Clients. Mobile applications and more!")
				.version("1.0")
				.contact(new Contact("Sodep S.A.", "https://www.sodep.com.py/captura", "captura@sodep.com.py"))
				.license("Sodep License")
				.licenseUrl("https://www.sodep.com.py/captura/license")
				.build();
	}
}