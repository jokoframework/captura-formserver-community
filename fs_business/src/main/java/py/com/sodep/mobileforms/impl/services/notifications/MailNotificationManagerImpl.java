package py.com.sodep.mobileforms.impl.services.notifications;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.dtos.SystemParameterDTO;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.mail.MailQueue;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.mail.MailService;
import py.com.sodep.mobileforms.api.services.notifications.INotificationManager;
import py.com.sodep.mobileforms.api.services.notifications.NotificationReport;
import py.com.sodep.mobileforms.api.services.notifications.NotificationType;

/**
 * This implementation sends notifications via email.
 * 
 * @author rodrigo
 *
 */
@Component("NotificationManager")
public class MailNotificationManagerImpl implements INotificationManager {

	private static Logger logger = LoggerFactory.getLogger(MailNotificationManagerImpl.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private ISystemParametersBundle systemParams;

	@Autowired
	private I18nBundle i18nBundle;
	
	@Autowired
	private IParametersService parametersService;
	
	@Override
	public Boolean notify(User user, NotificationReport report, NotificationType type) {
		MailQueue m = null;
		switch(type) {
			case FAILED_DOCUMENT: {
				if (user == null) {
					m = alertFailedDocument(report);
				} else {
					m = alertFailedDocument(user, report);
				}
				break;
			}
			default: {
				break;
			}
		}
		return m != null;
	}
	
	@Override
	public Boolean notify(NotificationReport report, NotificationType type) {
		return notify(null, report, type);
	}

	@Override
	public void disableNotifications(User currentUser, Boolean disable) {
		IParameter parameter = parametersService.getParameter(DBParameters.SYS_NOTIFICATION_ERROR_DISABLED);
		if (parameter != null) {
			try {
				SystemParameterDTO dto = new SystemParameterDTO();
				BeanUtils.copyProperties(dto, parameter);
				dto.setValue(disable.toString());
				parametersService.editSystemParameter(currentUser, dto); 
			} catch (Exception e) {
				logger.error("Could not update System Parameter [SYSTEM_MAILER_ADDR] id=[{}]", DBParameters.SYS_NOTIFICATION_ERROR_DISABLED);
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public Boolean getNotificationsStatus() {
		Boolean disabled = true;
		IParameter parameter = parametersService.getParameter(DBParameters.SYS_NOTIFICATION_ERROR_DISABLED);
		if (parameter != null) {
			disabled = Boolean.parseBoolean(parameter.getValue());
		} else {
			logger.debug("System Parameter [SYS_NOTIFICATION_ERROR_DISABLED] id=[{}] was not set", DBParameters.SYS_NOTIFICATION_ERROR_DISABLED);
		}
		return disabled;
	}
	
	private MailQueue alertFailedDocument(User user, NotificationReport report) {
		if (user == null || StringUtils.isBlank(user.getMail())) {
			return null;
		}
		String to = user.getMail();
		// 1. prepare mail queue
		MailQueue mail = getFailedDocumentMail(report);
		
		// 2. enqueue mail
		MailQueue sent = mailService.queueMail(mail.getFrom(), to, mail.getSubject(), mail.getBody());
		
		return sent;
	}
	
	private MailQueue alertFailedDocument(NotificationReport report) {
		Boolean disabled = getNotificationsStatus();
		if (disabled) {
			logger.warn("An ERROR was detected but the support team will NOT be notified. Error notifications are disabled.");
			return null;
		}
		String to = getSupportAddress();
		// 1. prepare mail queue
		MailQueue mail = getFailedDocumentMail(report);
		
		// 2. enqueue mail
		return mailService.queueMail(mail.getFrom(), to, mail.getSubject(), mail.getBody());
		
	}
	
	private MailQueue getFailedDocumentMail(NotificationReport report) {
		String language = getDefaultLanguage();
		MailQueue mail = new MailQueue();
		mail.setFrom(getMailFrom());
		
		// We avoid with this errors with '$' characters
		// inside the stack trace string.
		String errorMsg = Matcher.quoteReplacement(report.getStackTraceString());
		errorMsg = errorMsg.replaceAll("\\r?\\n", "<br />");
		String docMsg = getDocDataAsString(report.getData(), "<br />");
		String body = i18nBundle.getLabel(language, "services.mail.failed_document.body", docMsg, errorMsg);
		if (body == null) {
			body = getDefaultMessage(MailNotificationConstants.DEFAULT_NOTIFICATION_MAIL_BODY, docMsg, errorMsg);
		}
		mail.setBody(body);
		
		String shortDocMsg = getDocDataAsString(report.getData(), ", ", 2);
		String subject = i18nBundle.getLabel(language, "services.mail.failed_document.subject", shortDocMsg);
		if (subject == null) {
			subject = getDefaultMessage(MailNotificationConstants.DEFAULT_NOTIFICATION_MAIL_SUBJECT, shortDocMsg);
		}
		mail.setSubject(subject);
		
		return mail;
	}

	private String getDefaultMessage(String message, String... params) {
		for (int i = 0; i < params.length; i++) {
			message = message.replaceAll("\\{\\s*" + i + "\\s*\\}", params[i]);
		}
		return message;
	}
	
	private String getDocDataAsString(Map<String, Object> data, String separator) {
		return getDocDataAsString(data, separator, Integer.MAX_VALUE);
	}
	
	private String getDocDataAsString(Map<String, Object> data, String separator, int numOfProperties) {
		StringBuffer sb = new StringBuffer();
		Set<String> keySet = data.keySet();
		int count = 0;
		for(String key : keySet) {
			Object value = data.get(key);
			if (count < numOfProperties && value instanceof String) {
				if (count > 0) {
					sb.append(separator);
				}
				sb.append(key).append(": ").append((String) value);
				count++;
			}
		}
		
		return sb.toString();
	}

	private String getDefaultLanguage() {
		String lang = systemParams.getStrValue(DBParameters.LANGUAGE);
		if (lang == null) {
			throw new RuntimeException("System Parameter [LANGUAGE] id=[" + DBParameters.LANGUAGE
					+ "] NOT SET");
		}
		return lang;
	}


	// TODO Refactor. Copy-pasted from UserService
	private String getMailFrom() {
		String mailFrom = systemParams.getStrValue(DBParameters.SYSTEM_MAIL_ADDRESS);
		if (mailFrom == null) {
			throw new RuntimeException("System Parameter [SYSTEM_MAILER_ADDR] id=[" + DBParameters.SYSTEM_MAIL_ADDRESS
					+ "] NOT SET");
		}
		return mailFrom;
	}
	
	private String getSupportAddress() {
		String mail = systemParams.getStrValue(DBParameters.SYS_NOTIFICATION_SUPPORT_MAIL_ADDRESS);
		if (mail == null) {
			mail = MailNotificationConstants.DEFAULT_SUPPORT_MAIL_ADDRESS;
		}
		
		return mail;
	}

	
}
