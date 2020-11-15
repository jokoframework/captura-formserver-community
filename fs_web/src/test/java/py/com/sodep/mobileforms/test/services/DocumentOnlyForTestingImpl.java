package py.com.sodep.mobileforms.test.services;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;

@Service("DocumentOnlyForTesting")
@Transactional
public class DocumentOnlyForTestingImpl implements IDocumentOnlyForTesting {
	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	public void cleanUploadDocuments() {

		Query q = em.createQuery("Delete from " + DocumentUpload.class.getName() + " A ");
		q.executeUpdate();

	}
}
