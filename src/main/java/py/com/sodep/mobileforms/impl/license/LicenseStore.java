package py.com.sodep.mobileforms.impl.license;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.exceptions.LicenseException;

@Component
public class LicenseStore {

	private static final Logger logger = LoggerFactory.getLogger(LicenseStore.class);

	@Value("${MOBILEFORMS_HOME}")
	private String mobileFormsHomePath;

	public String getEncryptedFormServerLicense() {
		File f = new File(mobileFormsHomePath + "/license/license");
		logger.info("License file " + f.getAbsolutePath());
		if (f.exists()) {
			return IOUtils.readLine(f);
		} else {
			throw new LicenseException("No Form Server License Found");
		}
	}

	public List<String> getEncryptedApplicationLicense(Long applicationId) {
		File f = new File(mobileFormsHomePath + "/license/apps/license-" + applicationId);
		if (f.exists()) {
			String line = IOUtils.readLine(f);
			List<String> licenses = new ArrayList<String>();
			licenses.add(line);
			return licenses;
		}
		return Collections.emptyList();
	}

	public void saveApplicationLicense(Long applicationId, String encryptedLicense) {
		File dir = new File(mobileFormsHomePath + "/license/apps");
		dir.mkdirs();
		File file = new File(dir, "license-" + applicationId);
		file.delete();
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(os);
			writer.write(encryptedLicense);
			writer.flush();
		} catch (FileNotFoundException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}
}
