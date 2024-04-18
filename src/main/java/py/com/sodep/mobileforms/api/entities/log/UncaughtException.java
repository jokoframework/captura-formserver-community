package py.com.sodep.mobileforms.api.entities.log;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.services.logging.IDBLogging;

/**
 * All uncaught exceptions are logged into the database.
 * 
 * @see {@link IDBLogging}
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "log", name = "uncaught_exceptions")
@SequenceGenerator(name = "seq_uncaught_exceptions", sequenceName = "log.seq_uncaught_exceptions")
public class UncaughtException implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int MAX_EXCEPTIONTYPE_LENGTH = 250;
	public static final int MAX_OFFENDINGCLASS_LENGTH = 250;
	public static final int MAX_STACKTRACE_LENGTH = 5000;
	public static final int MAX_URL_LENGTH = 250;
	public static final int MAX_USERAGENT_LENGTH = 250;

	private long id;

	private String exceptionType;

	private String offendingClass;

	private Long userId;

	private Timestamp inserTime;

	private String stackTrace;

	private String url;

	private String userAgent;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(generator = "seq_uncaught_exceptions")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "exception_type", length = MAX_EXCEPTIONTYPE_LENGTH)
	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Column(name = "offending_class", length = MAX_OFFENDINGCLASS_LENGTH)
	public String getOffendingClass() {
		return offendingClass;
	}

	public void setOffendingClass(String offendingClass) {
		this.offendingClass = offendingClass;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "insert_time", insertable = false, updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	public Timestamp getInserTime() {
		return inserTime;
	}

	public void setInserTime(Timestamp inserTime) {
		this.inserTime = inserTime;
	}

	@Column(name = "stack_trace", length = MAX_STACKTRACE_LENGTH)
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	@Column(name = "url", length = MAX_URL_LENGTH)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "user_agent", length = MAX_USERAGENT_LENGTH)
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

}
