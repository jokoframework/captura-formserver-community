package py.com.sodep.mobileforms.api.server;

import java.io.File;

public interface ServerProperties {

	String imageFolder();

	String csvFolder();

	void setHome(String home);

	void setImageFolder(String folder);

	void setCSVFolder(String folder);

	File getUploadFolder();

	void setUploadFolder(String path);

}
