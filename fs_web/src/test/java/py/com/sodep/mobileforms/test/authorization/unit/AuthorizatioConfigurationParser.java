package py.com.sodep.mobileforms.test.authorization.unit;

import java.io.FileOutputStream;
import java.io.IOException;

import py.com.sodep.mobileforms.api.entities.core.AuthorizationConfiguration;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthorizatioConfigurationParser {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		AuthorizationConfiguration conf = new AuthorizationConfiguration();
		conf.hideAuthorization("form.edit");
		conf.addDependency("project.edit", "form.edit");
		conf.addDependency("application.project.cancreate", "project.edit");

		ObjectMapper mapper = new ObjectMapper();
		FileOutputStream f = new FileOutputStream("authorizations_conf.json");
		mapper.writeValue(f, conf);
		f.close();
	}
}
