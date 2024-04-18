package py.com.sodep.mobileforms.api.entities.documents.upload;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mf.exchange.objects.upload.UploadStatus;

@Entity
@Table(schema = "mf_data", name = "uploads", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id",
		"device_id", "document_id" }) })
@SequenceGenerator(name = "seq_uploads", sequenceName = "mf_data.seq_uploads")
public class DocumentUpload {

	public static class HandleID {

		public final long id;
		public final String randomStr;

		HandleID(long id, String randomStr) {
			this.id = id;
			this.randomStr = randomStr;
		}

	}

	private Long id;

	private Long userId;

	private String deviceId;

	private String documentId;

	private long size;

	private UploadStatus status;

	private String randomStr;

	private Timestamp created;

	private Timestamp modified;

	public String errorDescription;

	private Timestamp completedAt;

	private Timestamp savedAt;

	private boolean bypassUniquessCheck;
	
	private Long applicationId;

	@PreUpdate
	public void preUpdate() {
		modified = new Timestamp(System.currentTimeMillis());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_uploads")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "device_id")
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Column(name = "document_id")
	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	@Column(name = "size")
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	public UploadStatus getStatus() {
		return status;
	}

	public void setStatus(UploadStatus status) {
		this.status = status;
	}

	@Transient
	public String getFileName() {
		return getHandle() + ".tmp";
	}

	@Column(name = "random_str")
	public String getRandomStr() {
		return randomStr;
	}

	public void setRandomStr(String randomStr) {
		this.randomStr = randomStr;
	}

	@Column(name = "created_at", insertable = false, updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Column(name = "modified_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getModified() {
		return modified;
	}

	public void setModified(Timestamp modified) {
		this.modified = modified;
	}

	@Column(name = "completed_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Timestamp completedAt) {
		this.completedAt = completedAt;
	}

	@Column(name = "saved_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getSavedAt() {
		return savedAt;
	}

	public void setSavedAt(Timestamp savedAt) {
		this.savedAt = savedAt;
	}

	@Transient
	public String getHandle() {
		return getId() + "_" + getRandomStr();
	}

	public static HandleID parseHandleStr(String str) {
		String[] handleTokens = str.split("_");
		Long id = Long.parseLong(handleTokens[0]);
		String randomStr = handleTokens[1];
		return new HandleID(id, randomStr);

	}

	@Column(name = "error_desc")
	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * When saving a document to MongoDB a check is made: doesNotExist(user,
	 * deviceId, formId, version, documentId) to avoid duplicates.
	 * 
	 * The deviceId and documentId are provided by the device. This is makes it
	 * very difficult for a web client (or any other dummy, no persistent,
	 * distributed, no device related client) to post a document.
	 * 
	 * If this is set to true, that check will be bypassed.
	 * 
	 * @return
	 */
	@Column(name = "bypass_uniqueness_check")
	public boolean getBypassUniquessCheck() {
		return bypassUniquessCheck;
	}

	public void setBypassUniquessCheck(boolean bypassUniquessCheck) {
		this.bypassUniquessCheck = bypassUniquessCheck;
	}

	@Column(name = "application_id")
	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	
	
}
