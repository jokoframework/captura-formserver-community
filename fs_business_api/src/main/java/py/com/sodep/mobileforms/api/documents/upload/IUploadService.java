package py.com.sodep.mobileforms.api.documents.upload;

import py.com.sodep.mf.exchange.objects.upload.UploadHandle;
import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;

public interface IUploadService {

	/**
	 * Returns a handle to the file upload.
	 * 
	 * A handle is created if the upload is a new one. The upload is uniquely
	 * identified by [user, deviceId, documentId]. An existing handle is
	 * returned if the upload is to continue or has already ended. The client is
	 * responsible of checking the status of the handle and to act according to
	 * it.
	 * 
	 * If a handle with the same [user, deviceId, documentId] already exists but
	 * the size isn't equal to the previously passed size, then the handle is
	 * returned but flagged as not acquired and with an "INVALID" Upload status.
	 * 
	 * @param app
	 * @param user
	 * @param deviceId
	 * @param documentId
	 * @param size
	 * @return
	 */
	UploadHandle requestHandle(Application app, User user, String deviceId, String documentId, long size);

	/**
	 * Returns an instance of DocumentUpload which gives all the necessary
	 * upload information
	 * 
	 * @param user
	 * @param handle
	 * @return
	 */
	DocumentUpload getDocumentUploadData(User user, String handle);

	DocumentUpload changeStatus(String handle, UploadStatus status);

	DocumentUpload changeStatus(String handle, UploadStatus status, String msg);

	DocumentUpload obtainDocumentToProcess();

	void changeStatusToCompleted(String handle);

	void changeStatusToSaved(String handle);

	UploadHandle handleForSimpleUpload(Application app, User user, String key, long size);
}
