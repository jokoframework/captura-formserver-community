package py.com.sodep.mobileforms.impl.services.mail;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.mail.MailQueue;
import py.com.sodep.mobileforms.api.exceptions.MailException;
import py.com.sodep.mobileforms.api.services.mail.MailService;

@Service("mailService")
@Transactional(noRollbackFor = { MailException.class })
public class MailServiceImpl implements MailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	// TODO Refactor. Unify methods for sending mail as a private helper method
	// using MimeMessage
	@Override
	public void sendPlainMail(String from, String to, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

	@Override
	public void sendHTMLMail(String from, String to, String subject, String body) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, to);
			message.setSubject(subject);
			// http://javamail.kenai.com/nonav/javadocs/javax/mail/internet/MimeMessage.html#setText(java.lang.String,java.lang.String, java.lang.String)
			/*
			 * Convenience method that sets the given String as this part's content,
			 * with a primary MIME type of "text" and the specified MIME subtype.
			 * The given Unicode string will be charset-encoded using the specified
			 * charset. The charset is also used to set the "charset" parameter.
			 */
			message.setText(body, "utf-8", "html");
			mailSender.send(message);
		} catch (Exception e) {
			throw new MailException(e);
		} 
	}
	
	@Override
	 public MailQueue queueMail(String mailFrom, String mailTo, String subject, String body) {
		MailQueue mq = new MailQueue();
		mq.setFrom(mailFrom);
		mq.setTo(mailTo);
		mq.setBody(body);
		mq.setSubject(subject);
		mq.setHtml(true);
		em.persist(mq);
		return mq;
	}

}
