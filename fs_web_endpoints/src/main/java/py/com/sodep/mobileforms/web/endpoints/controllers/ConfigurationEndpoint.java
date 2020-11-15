package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wordnik.swagger.annotations.Api;

@Controller
@Api(value = "configuration", description = "Endpoint for clients to get configuration parameters from the server and to know its status", position = 1)
public class ConfigurationEndpoint extends EndpointController {

	private static final long DEVICE_POLLING_TIME_IN_SECONDS = (30 * 60);

	@Autowired
	private ISystemParametersBundle systemParams;

	@RequestMapping(value = "/public/mobile/defaultSettings", method = RequestMethod.GET)
	public void settings(HttpServletRequest request, HttpServletResponse response) throws JsonGenerationException,
			JsonMappingException, IOException {

		Map<String, Object> map = new HashMap<String, Object>();
		Long pollingTime = systemParams.getLongValue(DBParameters.DEVICE_POLLING_TIME_IN_SECONDS);
		pollingTime = pollingTime == null ? DEVICE_POLLING_TIME_IN_SECONDS : pollingTime;
		map.put("pollingTime", Long.toString(pollingTime));
		String string = objectMapper.writeValueAsString(map);
		response.setContentType("application/json");
		response.getWriter().write(string);
		response.setStatus(HttpServletResponse.SC_OK);

	}

	@RequestMapping(value = "/public/ping", method = RequestMethod.GET)
	public void ping(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(HttpURLConnection.HTTP_OK);
		try {
			response.setContentType("text/plain");
			response.getWriter().print("ACK");
		} catch (IOException e) {
		}
	}

}
