package py.com.sodep.mobileforms.api.documents.upload;

public interface IUploadManager {

	/**
	 * This method will change the status of the document to completed and try
	 * to process it ASAP (i.e. will wake up the thread
	 * IDocumentProcessorWorker)
	 * 
	 * @param handle
	 */
	// We added this method as a fix for #2947
	public void changeStatusToCompleted(String handle);
}
