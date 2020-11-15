package py.com.sodep.mobileforms.impl.services.mail;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.IParametersService;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MailSender extends JavaMailSenderImpl implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(MailSender.class);

	@Autowired
	private IParametersService parameterService;

	// #213 specifies that the configuration should be stored in the DB
	//TODO include more properties. It's not enough with only host and port
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO override default parameters with db parameters (if any)
		IParameter param = parameterService.getParameter(DBParameters.SMTP_CONFIG);
		if (param != null && param.getActive() == true) {
			logger.debug("Initialized MailSender with DB properties. Overrides default settings");
			ObjectMapper mapper = new ObjectMapper();
			String value = param.getValue();
			@SuppressWarnings("unchecked")
			Map<String, Object> dbParams = mapper.readValue(value, Map.class);
			Object host = dbParams.get("host");
			if (host != null) {
				this.setHost((String) host);
				logger.debug("Host = " + host);
			}
			Object port = dbParams.get("port");
			if (port != null) {
				if (port instanceof Integer) {
					this.setPort((Integer) port);
				} else if (port instanceof String) {
					this.setPort(Integer.parseInt((String) port));
				}
				logger.debug("Port = " + port);
			}
		} else {
			logger.debug("Initialized MailSender with default properties");
		}
	}
}
