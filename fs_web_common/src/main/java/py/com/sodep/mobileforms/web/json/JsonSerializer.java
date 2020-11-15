package py.com.sodep.mobileforms.web.json;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import py.com.sodep.mobileforms.api.exceptions.ApplicationException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer {

	private static final String dateFormat = "yyyy.MM.dd.HH.mm.ss";

	/**
	 * Return a string in the form yyyy.MM.dd.HH.mm.ss that can be send to JS
	 * inside JSON January is 1 based, therefore the JS API should convert it
	 */
	public static String toJSDate(Date d) {
		DateFormat dateFormmater = new SimpleDateFormat(dateFormat);
		return dateFormmater.format(d);
	}

	/**
	 * Based on a string yyyy.MM.dd.HH.mm.ss, returns a Date Object January is 1
	 * based
	 */
	public static Date fromJSDate(String s) throws ParseException {
		DateFormat dateFormmater = new SimpleDateFormat(dateFormat);
		return dateFormmater.parse(s);
	}

	public static void writeJSON(HttpServletResponse response, Object json) {

		/*
		 * ObjectMapper mapper = new ObjectMapper(); try {
		 * response.setContentType("application/json"); //the default encoding
		 * of jackson is UTF-8 mapper.writeValue(response.getOutputStream(),
		 * json); } catch (JsonGenerationException e) { throw new
		 * ApplicationException("Unable to generate response", e); } catch
		 * (JsonMappingException e) { throw new
		 * ApplicationException("Unable to generate response", e); } catch
		 * (IOException e) { throw new
		 * ApplicationException("Fail writing response", e); }
		 */

		ObjectMapper mapper = new ObjectMapper();
		try {
			response.setContentType("application/json");
			// the default encoding of jackson is UTF-8
			String jsonstr = mapper.writeValueAsString(json);
			response.getOutputStream().write(jsonstr.getBytes("UTF-8"));

		} catch (JsonGenerationException e) {
			throw new ApplicationException("Unable to generate response", e);
		} catch (JsonMappingException e) {
			throw new ApplicationException("Unable to generate response", e);
		} catch (IOException e) {
			throw new ApplicationException("Fail writing response", e);
		}

	}

	/**
	 * This is a handy method to write a JsonResponse with success=true, and the
	 * message set to msg This method shouldn't be used from Controllers. The
	 * preferred method to return a json from a Controller is to mark the return
	 * with ResponseBody
	 * 
	 * @param response
	 * @param msg
	 */
	public static void writeOperationSuccess(HttpServletResponse response, String msg) {
		JsonResponse<String> json = new JsonResponse<String>();
		json.setMessage(msg);
		json.setSuccess(true);
		// I don't think this is a good idea.
		// Jackson does something similar but it exposes the class hierarchy.
		// Our inner working
		// response.setHeader("x-sodep-mf-class", json.getClass().getCanonicalName());
		writeJSON(response, json);
	}

	/**
	 * This is a handy method to write a JsonResponse with success=false, and
	 * the message set to msg This method shouldn't be used from Controllers.
	 * The preferred method to return a json from a Controller is to mark the
	 * return with ResponseBody
	 * 
	 * @param response
	 * @param msg
	 */
	public static void writeOperationFail(HttpServletResponse response, String msg) {
		JsonResponse<String> json = new JsonResponse<String>();
		json.setMessage(msg);
		json.setSuccess(false);
		// response.setHeader("x-sodep-mf-class", json.getClass().getCanonicalName());
		writeJSON(response, json);
	}

}