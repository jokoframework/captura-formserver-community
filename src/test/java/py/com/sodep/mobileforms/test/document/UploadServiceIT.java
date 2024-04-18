package py.com.sodep.mobileforms.test.document;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import py.com.sodep.mf.exchange.objects.upload.UploadHandle;
import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mobileforms.api.documents.upload.IUploadService;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;
import py.com.sodep.mobileforms.test.services.IDocumentOnlyForTesting;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
// This is a test that can't use transactions and rollbacks because obtaining a
// document to be processed locks the row in the transaction. Moreover, part of
// the test itself consist of checking if the row status changed as expected.
public class UploadServiceIT {

	private static Logger logger = LoggerFactory.getLogger(UploadServiceIT.class);

	@Autowired
	private IUploadService uploadService;

	@Autowired
	private IDocumentOnlyForTesting docForTestingService;

	@Autowired
	private MockObjectsContainer mockContainer;
	
	/**
	 * Check an state flow for upload document
	 */
	@Test
	public void testObtainDocument() {

		logger.info("Calling document upload");

		Application testApp = mockContainer.getTestApplication();
		User appOwner = mockContainer.getTestApplicationOwner();

		UploadHandle firstHandle = uploadService.requestHandle(testApp, appOwner, "", "1", 1010l);
		UploadHandle secondHandle = uploadService.requestHandle(testApp, appOwner, "", "2", 1010l);

		Assert.assertNotNull(firstHandle);
		Assert.assertEquals(UploadStatus.PROGRESS, firstHandle.getStatus());
		Assert.assertNotNull(secondHandle);
		if (firstHandle.getHandle().equals(secondHandle.getHandle())) {
			Assert.fail("Upload Handle of different documents should never be the same");
		}
		DocumentUpload doc = uploadService.obtainDocumentToProcess();
		// There shouldn't be any document ready for saving. The document is
		// still in progress
		Assert.assertNull(doc);

		// Changing the status to completed indicates that the whole file is
		// ready
		// to be processed
		uploadService.changeStatus(firstHandle.getHandle(), UploadStatus.COMPLETED);
		doc = uploadService.obtainDocumentToProcess();
		Assert.assertNotNull(doc);
		// When the document is returned it will be in "SAVING" status to
		// indicate that it has been picked up by a thread
		Assert.assertEquals(UploadStatus.SAVING, doc.getStatus());
		Assert.assertEquals(firstHandle.getHandle(), doc.getHandle());

		doc = uploadService.obtainDocumentToProcess();
		// Since we have already obtained the last COMPLETED document, there
		// shouldn't be any other in the queue
		Assert.assertNull(doc);

	}

	@After
	public void cleanBD() {
		docForTestingService.cleanUploadDocuments();
	}
}
