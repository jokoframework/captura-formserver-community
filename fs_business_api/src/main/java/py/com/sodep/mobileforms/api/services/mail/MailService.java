package py.com.sodep.mobileforms.api.services.mail;

import py.com.sodep.mobileforms.api.entities.mail.MailQueue;

public interface MailService {

	// TODO Refactor. Unify methods for sending mail as a private helper method
	// using MimeMessage
	void sendPlainMail(String from, String to, String subject, String body);

	void sendHTMLMail(String from, String to, String subject, String body);

	/**
	 * Write a mail to the queue. A background worker will send this mail later.
	 * 
	 * @param mailFrom
	 * @param mailTo
	 * @param subject
	 * @param body
	 * @return
	 */
	public MailQueue queueMail(String mailFrom, String mailTo, String subject, String body);

}