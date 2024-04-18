package py.com.sodep.mobileforms.impl.documents.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import py.com.sodep.mobileforms.api.documents.upload.IUploadManager;
import py.com.sodep.mobileforms.api.documents.upload.IUploadService;
import py.com.sodep.mobileforms.api.services.workers.IDocumentProcessorWorker;

@Component("core.uploadManager")
public class UploadManager implements IUploadManager {

	@Autowired
	private IUploadService uploadService;

	@Autowired
	private IDocumentProcessorWorker docProcessesorWorker;

	public void changeStatusToCompleted(String handle) {
		uploadService.changeStatusToCompleted(handle);
		docProcessesorWorker.scheduleWork();
	}
}
