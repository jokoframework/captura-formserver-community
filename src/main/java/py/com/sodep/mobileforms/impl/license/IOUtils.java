package py.com.sodep.mobileforms.impl.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class IOUtils {

	public static String readLine(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			return readLine(fis);
		} catch (FileNotFoundException e) {
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static String readLine(InputStream is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			return reader.readLine();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

}
