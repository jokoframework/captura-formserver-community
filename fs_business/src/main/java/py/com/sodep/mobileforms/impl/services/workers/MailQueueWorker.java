package py.com.sodep.mobileforms.impl.services.workers;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.mail.MailQueue;
import py.com.sodep.mobileforms.api.entities.sys.IParameter;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.mail.MailService;

/**
 * Processes and sends all the pending mails in the queue.
 * 
 */
@Transactional
public class MailQueueWorker  {

	private static final Logger logger = LoggerFactory.getLogger(MailQueueWorker.class);

	@Autowired
	private MailService mailService;

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Autowired
	private IParametersService parameterService;

	private int maxMails = 20;

	public MailQueueWorker() {
	
	}

	public void doWork() {
		logger.trace("Sending mails from mail queue");
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
