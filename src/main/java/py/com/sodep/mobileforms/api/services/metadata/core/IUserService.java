package py.com.sodep.mobileforms.api.services.metadata.core;

import java.util.List;
import java.util.Map;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mobileforms.api.dtos.PendingRegistrationDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Token;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * To save, update, retrieve and search Users
 * 
 * @author Miguel
 * 
 */
public interface IUserService {

	/**
	 * The user is identified by its mail.
	 * 
	 * @param mail
	 * @return
	 */

	User findByMail(String mail);

	/**
	 * Given the mail search in the application scope.
	 * 
	 * The user could be invited or a member of the application.
	 * 
	 * @param app
	 * @param mail
	 * @return
	 */
	User findByMail(Application app, String mail);

	/**
	 * Adds a new user.
	 * 
	 * If the user already exists throws an InvalidEntityException.
	 * 
	 * The user who is adding is passed to check permissions. It should be
	 * allowed to add a new user with currentUser being null. That would mean
	 * that the user is being add from a registration page.
	 * 
	 * If app is not null, it becomes the user's default application. The user
	 * becomes a member of the application. Any unset default value of the user
	 * should be taken from the application. (e.g. default language).
	 * 
	 * @param currentUser
	 *            maybe null if the user is being add from a registration page
	 * @param defaultApp
	 *            maybe null if the user doesn't have an app yet
	 * @param newUser
	 *            the user to add
	 * @return
	 */
	User addNewUser(User currentUser, Application defaultApp, User newUser);

	/**
	 * A list of the Applications which the user is member of.
	 * 
	 * It DOES NOT include the applications to which the user has been invited
	 * but hasn't yet accepted the invitation.
	 * 
	 * @param user
	 * @return
	 */
	List<Application> listApplications(User user);

	/**
	 * List all application where a user belongs no matter the member status
	 * 
	 * @param user
	 * @return
	 */
	List<Application> listAllApplications(User user);

	/*
	 * A list of Groups which the user is member of.
	 * 
	 * @param user
	 * 
	 * @return
	 */
	// List<Group> listGroups(User user);

	/**
	 * Returns a list of with the IDs of the groups the user is member of.
	 * 
	 * It returns an empty list if the user is not a member of any group.
	 * 
	 * @param user
	 * @return
	 */
	List<Long> listGroupsIds(User user);

	/**
	 * Returns true if the user is member of the application.
	 * 
	 * An invited user (i.e. a user who hasn't accepted the invitation) is not
	 * part of the application.
	 * 
	 * @param currentUser
	 * @param app
	 * @param userToInvite
	 * @return
	 */
	boolean isMember(Long appId, User user);

	/**
	 * Returns true if the user has been invited to the application.
	 * 
	 * @param appId
	 * @param user
	 * @return
	 */
	boolean isInvited(Long appId, User user);

	boolean changePassword(User user, String newPassword);
	
	public User changeFullName(User user, String firstName, String lastName);

	public User changeLanguage(User user, String language);

	/**
	 * Given the id search for the user in the application scope.
	 * 
	 * The user could be invited or a member of the application.
	 * 
	 * @param app
	 * @param id
	 * @return The user or null if the user doesn't exist or doesn't belong to
	 *         the app
	 */
	User findById(Application app, Long id);

	/**
	 * Returns true if userToDelete had an association to the application and
	 * was successfully deleted.
	 * 
	 * The association maybe several. e.g. membership, invited to the app,
	 * banned, etc.
	 * 
	 * @param currentUser
	 * @param app
	 * @param userToDelete
	 * @return
	 */
	boolean deleteUserAppAssociation(User currentUser, Application app, User userToDelete);

	/**
	 * Returns true if the user to add was successfully added to the
	 * application.
	 * 
	 * If the user was already part of the app, it returns false.
	 * 
	 * @param currentUser
	 * @param app
	 * @param userToAdd
	 * @return
	 */
	boolean addUserToApp(User currentUser, Application app, User userToAdd);

	/**
	 * Returns true if the invitation token is valid and the User wasn't already
	 * in the Application.
	 * 
	 * Returns false if the token is invalid or the user is already a member of
	 * the application.
	 * 
	 * @param mail
	 * @param token
	 * @return
	 */
	boolean acceptInvitation(String mail, String invitationToken);

	/**
	 * Returns true if an invitation was sent. If the user already is already a
	 * member of the application no invitation should be send.
	 * 
	 * @param currentUser
	 * @param app
	 * @param userToInvite
	 * @param add 
	 * @return
	 */
	boolean inviteUserToApp(User currentUser, Application app, User userToInvite, Boolean addDirectly);

	UserDTO getUserDTO(Long userId);

	User addGroups(Long userId, List<Long> groupsId);

	User removeGroups(Long userId, List<Long> groupsId);

	/**
	 * Updates the user with data from updatedUser.
	 * 
	 * If the user is not a member of the application it throws an
	 * AuthorizationException.
	 * 
	 * It's not possible to change a user's mail with this method. That's a
	 * violation to security and integrity of the system.
	 * 
	 * If the user's password is null or empty string it doesn't update it.
	 * 
	 * @param currentUser
	 * @param newuser
	 * @param app
	 * @return
	 */
	User updateUser(User currentUser, User updatedUser, Application app);

	/**
	 * Send a mail with instructions to setup a new password to the user on
	 * behalf of the currentUser in the given application.
	 * 
	 * @param application
	 * @param currentUser
	 * @param user
	 * @return
	 */
	boolean queuePasswordSetupMail(Application application, User currentUser, User user);

	/**
	 * Send a mail to the user with information to activate the account on
	 * behalf of the currentUser in the given application.
	 * 
	 * @param application
	 * @param currentUser
	 * @param user
	 * @return
	 */
	boolean queueActivationMail(Application application, User currentUser, User user);


	boolean queueActivationEmail(Application application, User currentUser, User user, String email);

	boolean queueSendActivationEmail(String email, MFDevice device);

	/**
	 * Send a mail to the user with his/hers credentials on behalf of the
	 * currentUser in the given application.
	 * 
	 * @param app
	 * @param currentUser
	 * @param user
	 * @return
	 */
    //afeltes - 2015-02-10, marked as deprecated as nobody uses
    @Deprecated
	boolean queueCredentialsMail(Application app, User currentUser, User user);

	/**
	 * Send a mail to the user with an invitation to join the application.
	 * 
	 * @param app
	 * @param currentUser
	 * @param user
	 * @return
	 */
	boolean queueInvitationMail(Application app, User currentUser, User user);

	void activateUser(User u);

	User findById(Long userId);

	User save(User user);

	// TODO document!
	PagedData<List<User>> findAll(Application app, Group group, boolean inGroup, String orderBy, boolean ascending,
			int pageNumber, int pageSize);

	// TODO document!
	PagedData<List<User>> findAll(Application app, String orderBy, boolean ascending, int pageNumber, int pageSize);

	// TODO document!
	PagedData<List<User>> findByMail(Application app, String mail, int pageNumber, int pageSize);

	// TODO document!
	PagedData<List<User>> findByMail(Application app, Group group, boolean inGroup, String value, int pageNumber,
			int pageSize);

	// TODO document!
	PagedData<List<User>> findByProperty(Application app, String propertyName, String oper, Object value,
			String orderBy, boolean ascending, int pageNumber, int pageSize);

	// TODO document!
	PagedData<List<User>> findByProperty(Application app, Group group, boolean inGroup, String propertyName,
			String oper, Object value, String orderBy, boolean ascending, int pageNumber, int pageSize);

	/**
	 * Returns a list of all active users ordered by first name, last name,
	 * mail.
	 * 
	 * The password is set to null for security reasons
	 * 
	 * @return
	 */
	List<UserDTO> listAllActiveUsers();

	/**
	 * @return the number of system users
	 * */
	Long numberOfSystemUsers();

	/**
	 * An orderer (by date) list of registration requests
	 * 
	 * @param pageNumber
	 *            Starts at 1
	 * @param pageSize
	 * @return
	 */
	PagedData<List<PendingRegistrationDTO>> listPendingRegistrations(int pageNumber, int pageSize);

	/**
	 * Creates a valid activation token. An activation token is needed to
	 * activate the user through the web
	 * 
	 * @param currentUser
	 * @param user
	 * @return
	 */
	Token createActivationToken(User currentUser, User user);

	void changePassword(Long userId, String password);

	/**
	 * Lists the active applications in which the user is a member
	 * 
	 * @param user
	 * @return
	 */
	List<Application> listActiveApplications(User user);

	User changeDefaultApplication(Long userId, Long appId);

	List<UserDTO> listAllActiveUsers(Application app);

	String getResetPasswordUrl(User currentUser, User user);

	String getLastLogin(Long applicationId, String mail);

	Map<Long, String> getLastLogins(Long applicationId, Long[] userIds, int numberOfUsers);

}
