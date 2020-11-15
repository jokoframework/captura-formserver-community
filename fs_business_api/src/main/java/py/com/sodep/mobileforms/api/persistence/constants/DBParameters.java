package py.com.sodep.mobileforms.api.persistence.constants;

//FIXME 
public final class DBParameters {

	public enum PARAMETER_TYPE {
		STRING, INTEGER, BOOLEAN, LONG, LIST
	};

	public static final Long LANGUAGE = 1000L;

	// This param is expected to be stored in the DB as a JSON object
	// Something like "{"host":"theMailServer.com", "port":"108"}"
	public static final Long SMTP_CONFIG = 1001L;

	public static final Long CONTEXT_PATH = 1002L;

	public static final Long SYSTEM_MAIL_ADDRESS = 1003L;

	public static final Long SYS_REGISTRATION_DISABLED = 1004L;

	public static final Long SYS_DATAIMPORT_ROWSTOSHOW = 1005L;

	public static final Long SYS_SYNCHRONIZATION_ROWSPERITERATION = 1006L;

	public static final Long APP_MAIL_NEW_USER = 5000L;

	@Deprecated
	public static final Long SYS_JS_CACHE_CONTROL_HEADER = 1007L;

	public static final Long CREATE_TEST_APP = 1008L;

	public static final Long DEFAULT_ROLE_APP_OWNER = 1009L;
	public static final Long DEFAULT_ROLE_PROJECT_OWNER = 1010L;
	public static final Long DEFAULT_ROLE_FORM_OWNER = 1011L;
	public static final Long DEFAULT_ROLE_POOL_OWNER = 1012L;

	@Deprecated
	public static final Long DEFAULT_ROLE_APP_MEMBER = 1013L;

	public static final Long MAX_ATTEMPTS_EMAIL_SEND = 1014L;

	public static final Long LOGOUT_AFTER_ERROR = 1015L;

	
	public static final Long SEND_ACTIVATION_MAIL_AFTER_REGISTRATION = 1016L;


	public static final Long REST_LOOKUP_DATA_MAXROWS = 1017L;
	
	public static final Long DEVICE_POLLING_TIME_IN_SECONDS = 1018L;
	
	public static final Long REGISTRATION_NOTIFICATION_MAIL = 1019L;
	
	public static final Long ABOUT_TO_EXPIRE_NOTIFICATION = 1020L;
	
	/**
	 * URI to upload a document. Must be the complete URL
	 */
	public static final Long UPLOAD_LOCATION = 1021L;
	
	public static final Long SYS_NOTIFICATION_ERROR_DISABLED = 1022L;

	public static final Long SYS_NOTIFICATION_SUPPORT_MAIL_ADDRESS = 1023L;
	
	public static final Long SYS_DEPLOY_ID = 1024L;

}
