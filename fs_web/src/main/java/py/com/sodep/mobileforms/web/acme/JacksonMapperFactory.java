package py.com.sodep.mobileforms.web.acme;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonMapperFactory {

	public synchronized ObjectMapper createInstance() {
		ObjectMapper mapper = new ObjectMapper();
		
		SimpleModule testModule = new SimpleModule("JSONXSSSafeSerializer", new Version(1, 0, 0, null));
		testModule.addSerializer(new JSONXSSSafeSerializer());
		testModule.addDeserializer(String.class, new JSONXSSSafeDeserializer());
		mapper.registerModule(testModule);
		return mapper;
	}
}
