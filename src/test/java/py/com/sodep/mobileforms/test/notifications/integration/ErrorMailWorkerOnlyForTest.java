package py.com.sodep.mobileforms.test.notifications.integration;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mobileforms.api.entities.mail.MailQueue;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.mail.MailService;

/**
 * Should be used only for testing purpose.
 * 
 * @author rodrigo
 *
 */
public class ErrorMailWorkerOnlyForTest {

	private static final Logger logger = LoggerFactory.getLogger(ErrorMailWorkerOnlyForTest.class);

	private MailService mailService;

	protected EntityManager em;

	private IParametersService parameterService;

	private int maxMails = 20;

	public ErrorMailWorkerOnlyForTest() {
		
	}
	
	public ErrorMailWorkerOnlyForTest(EntityManager em, MailService mailService, IParametersService parameterService) {
		this.mailService = mailService;
		this.parameterService = parameterService;
		this.em = em;
	}

	public void doWork() {
		logger.debug("Sending mails from mail queue");
		Integer maxAttempts;
		IParameter param = parameterService.getParameter(DBParameters.MAX_ATTEMPTS_EMAIL_SEND);
		if (param != null && param.getActive() == true) {
			maxAttempts = Integer.parseInt(param.getValue());
		} else {
			maxAttempts = 1;
		}

		TypedQuery<MailQueue> q = em.createQuery("FROM " + MailQueue.class.getSimpleName()
				+ " mq WHERE mq.sent=false AND mq.attempts < :attempts ORDER BY mq.inserted", MailQueue.class);
		q.setParameter("attempts", maxAttempts);
		q.setMaxResults(maxMails);
		List<MailQueue> mails = q.getResultList();
		if (mails.isEmpty()) {
			logger.debug("There are no mails in queue to send.");
			return;
		}
		for (MailQueue m : mails) {
			try {
				if (m.getHtml()) {
					mailService.sendHTMLMail(m.getFrom(), m.getTo(), m.getSubject(), m.getBody());
				} else {
					mailService.sendPlainMail(m.getFrom(), m.getTo(), m.getSubject(), m.getBody());
				}
				m.setSent(true);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				m.setAttempts(m.getAttempts() + 1);
			}
		}
	}

	public int getMaxMails() {
		return maxMails;
	}

	public void setMaxMails(int maxMails) {
		this.maxMails = maxMails;
	}

}
