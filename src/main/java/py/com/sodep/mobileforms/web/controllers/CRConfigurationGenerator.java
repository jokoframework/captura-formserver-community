package py.com.sodep.mobileforms.web.controllers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.web.editor.ToolbarConfig;
import py.com.sodep.mobileforms.web.json.JsonResponse;
import py.com.sodep.mobileforms.web.session.SessionManager;

@Controller
public class CRConfigurationGenerator {
	private static Logger logger = LoggerFactory.getLogger(CRConfigurationGenerator.class);

	@Autowired
	private ISystemParametersBundle systemParams;

	public CRConfigurationGenerator() {

	}


	// TODO this is a duplicate of a private the method
	// UserService#getContextPath
	private String getContextPath() {
		String contextPath = systemParams.getStrValue(DBParameters.CONTEXT_PATH);
		if (contextPath == null) {
			throw new RuntimeException("System Parameter [CONTEXT_PATH] id=[" + DBParameters.CONTEXT_PATH + "] NOT SET");
		}
		return contextPath;
	}

	@RequestMapping(value = "/cr/configuration.txt")
	void configuration(HttpServletRequest request, HttpServletResponse response) throws IOException {
		SessionManager sm = new SessionManager(request);
		Long appId = sm.getApplication().getId();
		response.setContentType("text/plain");
		response.setHeader("Content-Description:", "File Transfer");
		String fileName="mf_cr.properties";
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		
		
		PrintWriter writer = response.getWriter();
		writer.write("mf_cr.mode XML\n");
		writer.write("mf_cr.log4jFile conf/log4j.xml\n");

		writer.write("#The authentication information required to reach the Form Server\n");
		writer.write("mf_cr.rest.user " + sm.getUser().getMail() + "\n");
		writer.write("mf_cr.rest.pass [PUT YOUR PASSWORD HERE]\n");
		writer.write("mf_cr.rest.baseURL " + getContextPath() + "\n");
		writer.write("mf_cr.rest.applicationId " + appId + "\n");
		writer.write("#If this variable is true and the connection can't be established on startup then\n");
		writer.write("#the Connector Repository Server will refuse to startup\n");
		writer.write("mf_cr.authenticateOnStartup true\n");
		writer.write("#credentials to the local DB\n");
		writer.write("#The user and password should be changed manually by acceding the h2 browser\n");
		writer.write("mf_cr.db.user sa\n");
		writer.write("mf_cr.db.pass\n");
		writer.write("mf_cr.db.filePath work/lookups\n");
		writer.write("\n");
		writer.write("\n");
		writer.write("mf_cr.xml.filePath conf/custom_des.xml\n");

		writer.close();

	}

}
