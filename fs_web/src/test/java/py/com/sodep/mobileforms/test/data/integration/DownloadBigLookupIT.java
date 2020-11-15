package py.com.sodep.mobileforms.test.data.integration;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.TXInfo;
import py.com.sodep.mf.exchange.objects.lookup.MFDMLTransport;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.SynchronizationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class DownloadBigLookupIT {

	{
		URL confURL = DataAccessCommitAndRollbackIT.class.getResource("/log4j.xml");
		if (confURL != null) {
			DOMConfigurator.configure(confURL);
		}

	}
	private static final Logger logger = Logger.getLogger(DownloadBigLookupIT.class);

	@Autowired
	private SynchronizationService synchService;

	@Autowired
	private ILookupTableService lookupService;

	@Test
	public void dummy() {

	}

	// @Test
	public void downloadData() {
		MFLoookupTableDefinition def = lookupService.getLookupTableDefinition(24l);
		int pageSize = 100;
		TXInfo lastInfo = null;
		String lastTx = null;
		Long lastReceivedRow = null;
		MFDMLTransport dmlTransport;
		long computedTime = System.currentTimeMillis();
		do {
			dmlTransport = synchService.downloadData(def, lastTx, lastReceivedRow, pageSize);
			lastInfo = dmlTransport.getTxInfo();
			lastTx = lastInfo.getTx();
			lastReceivedRow = lastInfo.getEndRow();
			logger.debug("Downloaded data of " + lastTx + " from: " + lastInfo.getStartRow() + " to: "
					+ lastInfo.getEndRow());
		} while (!dmlTransport.isSynch());

		computedTime = System.currentTimeMillis() - computedTime;

		logger.info("The complete download process took " + computedTime + " ms.");
	}
}
