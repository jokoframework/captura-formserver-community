package py.com.sodep.mobileforms.web.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;

@Controller
public class I18nController extends SodepController {

	private static Logger logger = LoggerFactory.getLogger(I18nController.class);

	@Autowired
	private I18nBundle i18nBundle;

	@RequestMapping("/i18n/clear")
	public void clear(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("i18n cache clear request");
		i18nBundle.clearCache();
		response.setStatus(200);
		response.setContentType("text/plain");
		try {
			OutputStream os = response.getOutputStream();
			PrintStream pstr = new PrintStream(os);
			pstr.print("OK - cache cleared");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@RequestMapping("/i18n/keys.ajax")
	@ResponseBody
	public I18nSet loadKeys(@RequestBody I18nRequest i18nRequest, HttpServletRequest request) {

		SessionManager manager = new SessionManager(request);
		I18nManager i18n = manager.getI18nManager();

		String language = null;
		if (i18nRequest.getLanguage() != null) {
			language = i18nRequest.getLanguage();
		} else {
			i18n.getSelectedLanguage();
		}

		Map<String, String> keys = i18nBundle.getLabels(language, i18nRequest.getKeys());
		I18nSet i18nSet = new I18nSet();
		i18nSet.setLanguage(language);
		i18nSet.setValues(keys);

		return i18nSet;

	}

	public static class I18nSet {
		
		private String language;

		private Map<String, String> values;

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		/**
		 * I18N keys shouldn't be escaped when sent to the web layer. Why?
		 * Because we (developers, MF Team) are directly responsible for those
		 * strings. They shouldn't be a threat of XSS, html injection or any
		 * other.
		 * 
		 * @return
		 */
		@JsonSerialize(contentUsing = StringSerializer.class)
		public Map<String, String> getValues() {
			return values;
		}

		public void setValues(Map<String, String> values) {
			this.values = values;
		}

	};

	public static class I18nRequest {
	
		private String language;
		
		private ArrayList<String> keys;

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public ArrayList<String> getKeys() {
			return keys;
		}

		public void setKeys(ArrayList<String> keys) {
			this.keys = keys;
		}
	};

}
