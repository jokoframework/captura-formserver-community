package py.com.sodep.mobileforms.impl.documents.upload;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.upload.UploadHandle;
import py.com.sodep.mf.exchange.objects.upload.UploadStatus;
import py.com.sodep.mobileforms.api.documents.upload.IUploadService;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;
import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload.HandleID;
import py.com.sodep.mobileforms.api.server.ServerProperties;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;

@Service("UploadService")
@Transactional
public class UploadService implements IUploadService {

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Autowired
	private ServerProperties serverProperties;

	@Override
	public UploadHandle handleForSimpleUpload(Application app, User user, String key, long size) {
		DocumentUpload documentUpload = new DocumentUpload();
		documentUpload.setDeviceId(key);
		documentUpload.setDocumentId(UUID.randomUUID().toString());
		documentUpload.setSize(size);
		documentUpload.setCreated(new Timestamp(new Date().getTime()));
		documentUpload.setStatus(UploadStatus.PROGRESS);
		documentUpload.setUserId(user.getId());
		documentUpload.setRandomStr(RandomStringUtils.random(8, true, true));
		documentUpload.setBypassUniquessCheck(true);
		em.persist(documentUpload);
		return documentUploadToUploadHandle(documentUpload);
	}

	@Override
	public UploadHandle requestHandle(Application app, User user, String deviceId, String documentId, long size) {
		TypedQuery<DocumentUpload> query = em.createQuery("FROM " + DocumentUpload.class.getSimpleName()
				+ " WHERE userId=:userId AND deviceId=:deviceId AND documentId=:documentId", DocumentUpload.class);
		query.setParameter("userId", user.getId());
		query.setParameter("deviceId", deviceId);
		query.setParameter("documentId", documentId);
		DocumentUpload documentUpload = null;
		try {
			documentUpload = query.getSingleResult();
			if (documentUpload.getSize() != size) {
				documentUpload = new DocumentUpload();
				documentUpload.setDeviceId(deviceId);
				documentUpload.setDocumentId(documentId);
				documentUpload.setSize(size);
				documentUpload.setStatus(UploadStatus.INVALID);
				documentUpload.setUserId(user.getId());
				return documentUploadToUploadHandle(documentUpload, false);
			}
		} catch (NoResultException e) {
			documentUpload = new DocumentUpload();
			documentUpload.setApplicationId(app.getId());
			documentUpload.setDeviceId(deviceId);
			documentUpload.setDocumentId(documentId);
			documentUpload.setSize(size);
			documentUpload.setCreated(new Timestamp(new Date().getTime()));
			documentUpload.setStatus(UploadStatus.PROGRESS);
			documentUpload.setUserId(user.getId());
			// FIXME what happens in case of a collision? can it happen?
			documentUpload.setRandomStr(RandomStringUtils.random(8, true, true));
			em.persist(documentUpload);
		}

		return documentUploadToUploadHandle(documentUpload);
	}

	private UploadHandle documentUploadToUploadHandle(DocumentUpload documentUpload) {
		return documentUploadToUploadHandle(documentUpload, true);
	}

	private UploadHandle documentUploadToUploadHandle(DocumentUpload documentUpload, boolean acquired) {
		UploadHandle handle = new UploadHandle();
		handle.setHandle(documentUpload.getHandle());
		handle.setStatus(documentUpload.getStatus());
		File receivedFile = new File(serverProperties.getUploadFolder(), handle.getHandle() + ".tmp");
		if (receivedFile.exists()) {
			handle.setReceivedBytes(receivedFile.length());
		}
		handle.setAcquired(acquired);
		return handle;
	}

	@Override
	public DocumentUpload getDocumentUploadData(User user, String handle) {
		HandleID handleID = DocumentUpload.parseHandleStr(handle);

		TypedQuery<DocumentUpload> query = em.createQuery("FROM " + DocumentUpload.class.getSimpleName()
				+ " WHERE userId=:userId AND id=:id AND randomStr=:randomStr", DocumentUpload.class);
		query.setParameter("userId", user.getId());
		query.setParameter("id", handleID.id);
		query.setParameter("randomStr", handleID.randomStr);
		DocumentUpload result = null;

		try {
			result = query.getSingleResult();
		} catch (NoResultException e) {

		}

		return result;
	}

	private DocumentUpload getDocumentUpload(String handle) {
		HandleID handleID = DocumentUpload.parseHandleStr(handle);

		TypedQuery<DocumentUpload> query = em.createQuery("FROM " + DocumentUpload.class.getSimpleName()
				+ " WHERE id=:id AND randomStr=:randomStr", DocumentUpload.class);
		query.setParameter("id", handleID.id);
		query.setParameter("randomStr", handleID.randomStr);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
		}
		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public DocumentUpload changeStatus(String handle, UploadStatus status, String msg) {
		DocumentUpload documentUpload = getDocumentUpload(handle);
		// TODO control possible changes. E.g. From completed it cannot go back
		// to progress
		if(documentUpload != null) {
			documentUpload.setStatus(status);
			documentUpload.setErrorDescription(msg);
		}
		return documentUpload;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public DocumentUpload changeStatus(String handle, UploadStatus status) {
		return changeStatus(handle, status, null);
	}

	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public DocumentUpload obtainDocumentToProcess() {

		// Pick the oldest document already completed. Note that we prioritize
		// by id, so a document that started the whole upload process before has
		// a higher priority, even-though it might have just complete the upload
		// process
		Query q = em.createNativeQuery(
				"Select * from mf_data.uploads A where A.status=:status order by id desc limit 1 FOR UPDATE NOWAIT",
				DocumentUpload.class);
		q.setParameter("status", UploadStatus.COMPLETED.name());
		try {
			Object r = q.getSingleResult();

			if (r != null) {
				DocumentUpload doc = (DocumentUpload) r;
				doc.setStatus(UploadStatus.SAVING);
				q = em.createQuery("Update " + DocumentUpload.class.getName()
						+ " A set A.status=:status  where A.id=:docId");
				q.setParameter("status", UploadStatus.SAVING);
				q.setParameter("docId", doc.getId());
				int updatedDocs = q.executeUpdate();
				if (updatedDocs < 0) {
					// This is a WTF. We have obtained the document,locked it,
					// but we couldn't update it.
					throw new IllegalStateException("Unable to update document " + doc.getId()
							+ " that was the next in line to be processed");
				}
				return doc;
			}
		} catch (NoResultException e) {
			// do nothing, this is pretty normal. There is no document waiting
			// to be processed
		} catch (PersistenceException e) {

			if (e.getCause() instanceof GenericJDBCException) {
				GenericJDBCException jdbcError = (GenericJDBCException) e.getCause();
				if (jdbcError.getSQLState().equals("55P03")) {
					// this might happen if two threads try to pick up the same
					// register, since we are using nowait.
					// We will just return null indicating that there is no
					// pending documents. Since this is intended to be run on a
					// regular basis, the next thread will pick up the next
					// available document if any and it will just have a minor
					// delay
					return null;
				}
				// any other error should be consider an unexpected error
				throw e;

			}

		}
		return null;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public void changeStatusToCompleted(String handle) {

		DocumentUpload documentUpload = getDocumentUpload(handle);
		if (!documentUpload.getStatus().equals(UploadStatus.PROGRESS)) {
			throw new IllegalStateException("Can't move to completed a document that was on "
					+ documentUpload.getStatus() + " doc ID #" + documentUpload.getId());
		}
		documentUpload.setStatus(UploadStatus.COMPLETED);
		documentUpload.setCompletedAt(new Timestamp(new Date().getTime()));

	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public void changeStatusToSaved(String handle) {
		DocumentUpload documentUpload = getDocumentUpload(handle);
		if (!documentUpload.getStatus().equals(UploadStatus.SAVING)) {
			throw new IllegalStateException("Can't move to saved a document that was on " + documentUpload.getStatus()
					+ " doc ID #" + documentUpload.getId());
		}
		documentUpload.setStatus(UploadStatus.SAVED);
		documentUpload.setSavedAt(new Timestamp(new Date().getTime()));
	}
}
