package py.com.sodep.mobileforms.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.server.ServerProperties;

//FIXME take into account trailing / or \\ in the paths
// What library to use for that?
@Component
public class ServerPropertiesImpl implements ServerProperties {

	private String home;

	private String imageFolder;

	private String csvFolder;

	private String uploadFolder;

	private File uploadFolderFile;

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#imageFolder()
	 */
	@Override
	public String imageFolder() {
		return home + imageFolder;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#csvFolder()
	 */
	@Override
	public String csvFolder() {
		return home + csvFolder;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#setHome(java.lang.String)
	 */
	@Override
	@Value("${server.home}")
	public void setHome(String home) {
		this.home = home;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#setImageFolder(java.lang.String)
	 */
	@Override
	@Value("${server.images.folder}")
	public void setImageFolder(String folder) {
		this.imageFolder = folder;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#setCSVFolder(java.lang.String)
	 */
	@Override
	@Value("${server.csv.folder}")
	public void setCSVFolder(String folder) {
		this.csvFolder = folder;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#getUploadFolder()
	 */
	@Override
	public File getUploadFolder() {
		if (uploadFolderFile == null) {
			if (home == null) {
				throw new RuntimeException("Home null is null");
			}
			if (uploadFolder == null) {
				throw new RuntimeException("Upload folder is null");
			}
			uploadFolderFile = new File(home + uploadFolder);
			if (!this.uploadFolderFile.exists()) {
				this.uploadFolderFile.mkdirs();
			}
		}

		return this.uploadFolderFile;
	}

	/* (non-Javadoc)
	 * @see py.com.sodep.mobileforms.impl.ServerProperties#setUploadFolder(java.lang.String)
	 */
	@Override
	@Value("${server.upload.folder}")
	public void setUploadFolder(String path) {
		this.uploadFolder = path;
	}

}
