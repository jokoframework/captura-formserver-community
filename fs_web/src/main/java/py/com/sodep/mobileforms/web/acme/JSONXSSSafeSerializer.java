package py.com.sodep.mobileforms.web.acme;

import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JSONXSSSafeSerializer extends StdSerializer<String> {

	protected JSONXSSSafeSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		String escapedValue = StringEscapeUtils.escapeHtml4(value);
		jgen.writeString(escapedValue);
	}
}
