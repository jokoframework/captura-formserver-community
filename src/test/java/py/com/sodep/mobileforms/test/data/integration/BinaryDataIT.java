package py.com.sodep.mobileforms.test.data.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFField.FIELD_TYPE;
import py.com.sodep.mf.exchange.MFIncomingDataBasic;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.data.ColumnCheckError;
import py.com.sodep.mf.exchange.objects.data.MFOperationResult;
import py.com.sodep.mf.exchange.objects.data.RowCheckError;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.data.MFBlob;
import py.com.sodep.mobileforms.api.services.data.MFFileStream;
import py.com.sodep.mobileforms.impl.authorization.AuthorizationAspect;
import py.com.sodep.mobileforms.test.services.MockObjectsContainer;

/**
 * This class test correct image data types insertion on lookuptable and
 * getting after that
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-business-applicationContext.xml" })
@TransactionConfiguration(transactionManager = "myTxManager", defaultRollback = true)
@Transactional
public class BinaryDataIT {

	private static final Logger logger = Logger.getLogger(BinaryDataIT.class);
	@Autowired
	private ILookupTableService service;

	@Autowired
	private MockObjectsContainer mockContainer;

	public static MFLoookupTableDefinition getDataSetDef() {
		MFLoookupTableDefinition def = new MFLoookupTableDefinition();
		def.addField(new MFField(FIELD_TYPE.NUMBER, "ID"));
		def.addField(new MFField(FIELD_TYPE.STRING, "name"));
		def.addField(new MFField(FIELD_TYPE.BLOB, "foto"));

		LookupTableDTO info = new LookupTableDTO();
		info.setIdentifier("anyIdentifier");
		info.setName("Any name");
		info.setAcceptRESTDMLs(false);
		def.setInfo(info);
		return def;
	}

	private static byte[] readTestFile(String fileName) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		byte buff[] = new byte[1024];
		InputStream stream = BinaryDataIT.class.getResourceAsStream(fileName);
		int readBytes = 0;
		while ((readBytes = stream.read(buff)) > 0) {
			bOut.write(buff, 0, readBytes);
		}
		return bOut.toByteArray();
	}

	private MFIncomingDataBasic getBeatle(Integer id, String name, String image, boolean useInputStream)
			throws IOException {
		HashMap<String, Object> beatle = new HashMap<String, Object>();
		beatle.put("ID", id);
		beatle.put("name", name);

		MFBlob beatlePicture;
		if (useInputStream) {
			beatlePicture = new MFBlob(image, BinaryDataIT.class.getResourceAsStream("/images/" + image));
		} else {
			beatlePicture = new MFBlob(image, readTestFile("/images/" + image));
		}

		beatle.put("foto", beatlePicture);

		MFIncomingDataBasic row = new MFIncomingDataBasic(id, beatle);
		return row;
	}

	private Map<Long, MFIncomingDataBasic> getData(boolean useInputStream) throws IOException {
		Map<Long, MFIncomingDataBasic> rows = new HashMap<Long, MFIncomingDataBasic>();
		rows.put(1l, getBeatle(1, "Paul Mc. Cartney", "test-picture-paul.jpg", useInputStream));
		rows.put(2l, getBeatle(2, "John Lennon", "test-picture-john.jpg", useInputStream));
		return rows;
	}

	// @Test
	public void testInsertPictureByteArray() throws InterruptedException, IOException, LookupTableDefinitionException {
		testInsertPicture(false);
	}

	/**
	 * Insert pictures using inputstream
	 * saved in lookuptable as its dataset
	 */
	@Test
	public void testInsertPictureInputStream() throws InterruptedException, IOException, LookupTableDefinitionException {
		testInsertPicture(true);
	}

	public void testInsertPicture(boolean useInputStream) throws InterruptedException, IOException,
			LookupTableDefinitionException {
		User u = new User();

		MFLoookupTableDefinition lookupDef = getDataSetDef();
		Application defaultApp = mockContainer.getTestApplication();
		User user = mockContainer.getTestApplicationOwner();
		AuthorizationAspect.setUserInRequest(user);

		MFLoookupTableDefinition storedLookupDef = service.createLookupTable(defaultApp, u, lookupDef);

		Map<Long, MFIncomingDataBasic> originalTable = getData(useInputStream);
		Collection<MFIncomingDataBasic> list = originalTable.values();

		MFOperationResult storedData = service.insertData(defaultApp, storedLookupDef.getInfo().getPk(),
				new ArrayList<MFIncomingDataBasic>(list), false);

		// no matter if the test is using inputStream or directly a bytearray,
		// the final comparation should be done against the byte array
		originalTable = getData(false);
		if (storedData.hasSucceeded()) {

			List<MFManagedData> listData = service.listAllData(defaultApp, storedLookupDef.getInfo().getPk());
			for (MFManagedData row : listData) {
				Long id = row.getLong("ID");
				MFIncomingDataBasic originalRow = originalTable.get(id);
				Map<String, ?> originalUserData = originalRow.getData();
				Map<String, ?> savedUserData = row.getUserData();

				// check that the blob was saved
				MFBlob expectedBlob = (MFBlob) originalUserData.get("foto");
				MFBlob savedBlob = (MFBlob) savedUserData.get("foto");
				Assert.assertEquals(expectedBlob.getFileName(), savedBlob.getFileName());
				Assert.assertEquals(expectedBlob.getContentType(), savedBlob.getContentType());

				savedBlob = service.getFile(savedBlob);
				Assert.assertArrayEquals(expectedBlob.getData(), savedBlob.getData());

				// check that the blob is accessible in "lazy" mode

				MFFileStream stream = service.getFileLazy(savedBlob);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				stream.write(byteOut);
				Assert.assertArrayEquals(expectedBlob.getData(), byteOut.toByteArray());

			}
		} else {

			List<RowCheckError> errros = storedData.getErrors();
			for (RowCheckError rowCheckError : errros) {
				Object rowHandle = rowCheckError.getHandle();
				logger.info("Row = " + rowHandle);
				logger.info("-------------------------");
				List<ColumnCheckError> columnErrros = rowCheckError.getColumnErrors();
				for (ColumnCheckError columnCheckError : columnErrros) {
					logger.info("Column:" + columnCheckError.getOffendingField() + " , "
							+ columnCheckError.getErrorType());

				}
			}
			Assert.fail("Couldn't insert data for a lookup table with binary content");
		}
	}

}
