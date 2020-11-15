package py.com.sodep.mobileforms.test.notifications.integration;

import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;
import py.com.sodep.mobileforms.api.services.config.IParametersService;
import py.com.sodep.mobileforms.api.services.mail.MailService;
import py.com.sodep.mobileforms.api.services.notifications.INotificationManager;
import py.com.sodep.mobileforms.api.services.notifications.NotificationReport;
import py.com.sodep.mobileforms.api.services.notifications.NotificationType;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test failed document notification (until queued mail)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class MailNotificationManagerIT {
	
	@SuppressWarnings("unused")
	@Autowired
	private MailService mailService;
	
	@SuppressWarnings("unused")
	@Autowired
	private IParametersService parametersService;
	
	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;
	
	@Autowired
	private INotificationManager notificationManager;
	
	@Autowired
	private MockObjectsContainer mockContainer;

	// passs = dst3st1ng
	private static final String TEST_MAIL = "ds.testing.acc@gmail.com";
	
	private static final String ERROR_MSG = "The multiplexed file #16 doesn't contain a file with the name 'document'\n "
			 + "java.util.concurrent.ThreadPoolExecutor$Worker-run(Line 615)\n " 
			 + "java.lang.Thread-run(Line 744)";
	
	/**
	 * Create a dummy document and mark it as failed
	 * to notify that 
	 */
	@Test
	public void testSendNotificationOnFailedDoc() {
		User u = new User();
		u.setMail(TEST_MAIL);
		DocumentUpload docUpload = getTestFailedDocumentUpload();
		NotificationReport report = NotificationReport.documentErrorReport(ERROR_MSG, docUpload);
		Boolean enqueued = notificationManager.notify(u, report, NotificationType.FAILED_DOCUMENT);
		Assert.assertEquals(Boolean.TRUE, enqueued);
		// TODO rvillalba. I disabled the worker that sends mails, 
		// to avoid any side effects in production environments.
		//ErrorMailWorkerOnlyForTest worker = new ErrorMailWorkerOnlyForTest(em, mailService, parametersService);
		//worker.setMaxMails(0); 
		//worker.doWork();
	}

	private DocumentUpload getTestFailedDocumentUpload() {
		Application testApp = mockContainer.getTestApplication();
		User appOwner = mockContainer.getTestApplicationOwner();
		DocumentUpload docUpload = new DocumentUpload();
		docUpload.setId(999L);
		docUpload.setDeviceId("0909");
		docUpload.setStatus(UploadStatus.FAIL);
		docUpload.setApplicationId(testApp.getId());
		docUpload.setUserId(appOwner.getId());
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		docUpload.setCreated(ts);
		docUpload.setModified(ts);
		return docUpload;
	}
}
