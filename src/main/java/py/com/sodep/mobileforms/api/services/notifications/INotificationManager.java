package py.com.sodep.mobileforms.api.services.notifications;

import py.com.sodep.mobileforms.api.entities.core.User;

/**
 * General purpose interface with methods to notify events that happened in the
 * system. Example; an exception when uploading a document.
 * <p>
 * This interface should not depend on the underlying technology used to send
 * the notifications, i.e mails.
 * 
 * @author rodrigo
 * 
 */
public interface INotificationManager {

	Boolean notify(NotificationReport report, NotificationType type);

	Boolean notify(User user, NotificationReport report, NotificationType type);

	void disableNotifications(User currentUser, Boolean disable);

	Boolean getNotificationsStatus();
	

}
