package py.com.sodep.mobileforms.api.services.notifications;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import py.com.sodep.mobileforms.api.entities.documents.upload.DocumentUpload;


/**
 * 
 * 
 * @author rodrigo
 *
 */
public class NotificationReport {

	/**
	 * Could be property,value pair information of any object to send with the notification.
	 */
	private Map<String, Object> data = new LinkedHashMap<String, Object>();

	private String message;
	
	/**
	 * If this is an error notification report.
	 */
	private String stackTraceString;
	
	private NotificationReport(String stackTraceString, Map<String, Object> data) {
		this.stackTraceString = stackTraceString;
		this.data = data;
	}
	
	/**
	 * Constructs a new NotificationReport with error and document info.
	 * 
	 * @param stackTraceString
	 * @param docUpload
	 * @return
	 */
	public static NotificationReport documentErrorReport(String stackTraceString, DocumentUpload docUpload) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		if (docUpload != null) {
			map.put("id", docUpload.getId().toString());
			if (docUpload.getApplicationId() != null) {
				map.put("applicationId", docUpload.getApplicationId().toString());
			}
			map.put("userId", docUpload.getUserId().toString());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			java.sql.Timestamp d = (java.sql.Timestamp) docUpload.getCreated();
			map.put("createdAt", formatter.format(d));
		}
		return new NotificationReport(stackTraceString, map);
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getStackTraceString() {
		return this.stackTraceString;
	}
	
}
