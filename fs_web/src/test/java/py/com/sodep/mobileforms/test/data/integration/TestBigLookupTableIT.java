package py.com.sodep.mobileforms.test.data.integration;

import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransportMultiple;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class TestBigLookupTableIT {

	private static final Logger logger = LoggerFactory.getLogger(TestBigLookupTableIT.class);

	private Long lookupTableId = 27l;

	@Autowired
	private SynchronizationService synchService;

	@Autowired
	private ILookupTableService lookupService;

	public TestBigLookupTableIT() {
		DOMConfigurator.configure(TestBigLookupTableIT.class.getResource("/log4j.xml"));
	}

	@Test
	@Ignore
	// This isn't actually a junit test because it depends on data already
	// stored (lookupTableId)
	// I (dancricco) wrote this test in order to measure how much a download of
	// data might take. The lookup is a big lookup uploaded with the CR
	public void testDownloadAll() {

		int numberOfData = 100;
		MFLoookupTableDefinition ddl = lookupService.getLookupTableDefinition(lookupTableId);
		Assert.assertNotNull(ddl);
		boolean isSynch = false;
		TXInfo lastTxInfo = null;
		int numberOfRows = 0;
		do {
			long lastRow = 0;
			String lastTx = null;
			if (lastTxInfo != null) {
				lastRow = lastTxInfo.getEndRow();
				lastTx = lastTxInfo.getTx();
			}
			MFDMLTransportMultiple multipleTransport = synchService.downloadDataMultiple(lookupTableId, ddl, lastTx,
					lastRow, numberOfData);
			List<MFDMLTransport> transactions = multipleTransport.getListOfTransports();

			for (MFDMLTransport t : transactions) {
				// iterate over all transactions and keep the last one
				lastTxInfo = t.getTxInfo();
				if (t.getData() != null) {
					numberOfRows += t.getData().size();
				}
			}
			logger.debug("Downloaded " + numberOfRows);

		} while (!isSynch && numberOfData < 20000);

	}
}
