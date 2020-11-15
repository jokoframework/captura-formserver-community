package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "forms", name = "elements_photos")
public class Photo extends ElementPrototype implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fileName;

	private boolean cameraOnly;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "camera_only")
	public boolean isCameraOnly() {
		return cameraOnly;
	}

	public void setCameraOnly(boolean cameraOnly) {
		this.cameraOnly = cameraOnly;
	}

}
