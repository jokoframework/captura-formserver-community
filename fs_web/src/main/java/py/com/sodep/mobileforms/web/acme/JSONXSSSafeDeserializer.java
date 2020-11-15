package py.com.sodep.mobileforms.web.acme;

import java.io.IOException;

import py.com.sodep.mobileforms.web.exceptions.XssInputContentException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class JSONXSSSafeDeserializer extends StdScalarDeserializer<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String CONTROL_REGEX = ".*\\<[^>]+>.*";

	private StringDeserializer stringDeserializer;

	protected JSONXSSSafeDeserializer() {
		super(String.class);
		stringDeserializer = new StringDeserializer();
	}

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String s = stringDeserializer.deserialize(jp, ctxt);
		return sanitize(s);
	}

	@Override
	public String deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
			throws IOException, JsonProcessingException {
		String s = stringDeserializer.deserializeWithType(jp, ctxt, typeDeserializer);
		return sanitize(s);
	}

	private String sanitize(String s) throws IOException, JsonProcessingException {
		if (s != null) {
			if (s.matches(CONTROL_REGEX)) {
				throw new XssInputContentException();
			}
		}
		return s;
	}
}
