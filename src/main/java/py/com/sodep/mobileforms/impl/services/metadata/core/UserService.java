package py.com.sodep.mobileforms.impl.services.metadata.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.objects.device.MFDevice;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.PendingRegistrationDTO;
import py.com.sodep.mobileforms.api.dtos.UserDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.application.ApplicationUserAssociation;
import py.com.sodep.mobileforms.api.entities.application.ApplicationUserAssociation.ASSOCIATION_STATUS;
import py.com.sodep.mobileforms.api.entities.application.ApplicationUserAssociation.ApplicationUserAssociationPK;
import py.com.sodep.mobileforms.api.entities.core.Group;
import py.com.sodep.mobileforms.api.entities.core.Token;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.exceptions.AuthorizationException;
import py.com.sodep.mobileforms.api.exceptions.InvalidEntityException;
import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.api.persistence.constants.DBParameters;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.Authorizable.CHECK_TYPE;
import py.com.sodep.mobileforms.api.services.config.ISystemParametersBundle;
import py.com.sodep.mobileforms.api.services.control.ITokenService;
import py.com.sodep.mobileforms.api.services.control.TokenPurpose;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.license.MFLicenseManager;
import py.com.sodep.mobileforms.api.services.mail.MailService;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;
import py.com.sodep.mobileforms.api.services.metadata.core.IGroupService;
import py.com.sodep.mobileforms.api.services.metadata.core.IUserService;
import py.com.sodep.mobileforms.impl.services.metadata.BaseService;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PagingCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.PropertyCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.SortCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users.FindUserCriteria;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users.FindUserCriteriaBuilder;
import py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria.users.GroupCriteria;
import py.com.sodep.mobileforms.license.MFApplicationLicense;
import py.com.sodep.mobileforms.utils.SecurityUtils;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("UserService")
@Transactional
class UserService extends BaseService<User> implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private I18nBundle i18nBundle;

    @Autowired
    private ISystemParametersBundle systemParams;

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private MFLicenseManager mfLicenseManager;

    @Autowired
    private MailService mailService;

    protected UserService() {
        super(User.class);
    }

    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public User findByMail(String mail) {
        List<User> users = listByPropertyEquals(User.MAIL, mail);
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    @Override
    public User addNewUser(User currentUser, Application app, User newUser) {
        return addNewUser(currentUser, app, newUser, ASSOCIATION_STATUS.MEMBER);
    }

    private static final Object newUserLock = new Object();

    // TODO #2765
    // Control User creation with license manager
    private User addNewUser(User currentUser, Application app, User newUser, ASSOCIATION_STATUS status) {
        User u = findByMail(newUser.getMail());

        if (u != null) { // a user with that mail exists
            InvalidEntityException ex = new InvalidEntityException("services.user.register.mail.duplicate");
            ex.addMessage(User.MAIL, "services.user.register.mail.duplicate");
            throw ex;
        }

        if (app != null) {
            checkLicense(app);
        }

        if (newUser.getLastName() == null || newUser.getFirstName() == null) {
            throw new IllegalArgumentException("Can't save a user with empty lastname or firstname");
        }

        if (app != null) {
            if (newUser.getLanguage() == null) {
                newUser.setLanguage(app.getDefaultLanguage());
            }

            if (!em.contains(app)) {
                app = em.find(Application.class, app.getId());
            }
            newUser = save(newUser);
            newUser.setApplication(app);
            associateUserToApp(app, newUser, status);
        } else {
            save(newUser);
        }

        logger.trace("New user added: user[id=", newUser.getId() + "]");

        return newUser;

    }

    private void checkLicense(Application app) {
        Long appId = app == null ? null : app.getId();
        if (mfLicenseManager.doesLicenseApply(appId)) {
            MFApplicationLicense applicationLicense = mfLicenseManager.getLicense(appId);
            Long maxUsers = applicationLicense.getMaxUsers();
            if (maxUsers != null) {
                synchronized (newUserLock) {
                    Long count = count(appId);
                    if (count >= maxUsers) {
                    	String language = app.getDefaultLanguage();
                        //FIXME i18n
                        //FIXME message should be internationalized
                        throw new LicenseException(i18nBundle.getLabel(language, "services.user.new.license.max_user.reached", 
                        		maxUsers.toString(), count.toString()));
                    }
                }
            }
        }
    }

    private String getMailFrom() {
        String mailFrom = systemParams.getStrValue(DBParameters.SYSTEM_MAIL_ADDRESS);
        if (mailFrom == null) {
            throw new RuntimeException("System Parameter [SYSTEM_MAILER_ADDR] id=[" + DBParameters.SYSTEM_MAIL_ADDRESS
                    + "] NOT SET");
        }
        return mailFrom;
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public User findByMail(Application app, String mail) {
        TypedQuery<User> q = em.createQuery(
                "SELECT DISTINCT(u) FROM User u LEFT JOIN u.applications apps WHERE u.mail=:mail AND apps=:app ",
                User.class);
        q.setParameter("mail", mail);
        q.setParameter("app", app);
        try {
            User u = q.getSingleResult();
            return u;
        } catch (NoResultException e) {
            return null;
        }

    }

    private String fieldNameWithPrefix(final String field) {
        String f = "u.id";
        if (field.matches("firstName|lastName|mail|language")) {
            f = "u." + field;
        }
        return f;
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public User findById(Application app, Long id) {
        String query = "SELECT DISTINCT(u) FROM User u LEFT JOIN u.applications apps WHERE u.deleted = false AND "
                + " u.id=:id AND apps=:app";
        TypedQuery<User> q = em.createQuery(query, User.class);
        q.setParameter("id", id);
        q.setParameter("app", app);
        try {
            User user = q.getSingleResult();
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Application> listApplications(User user, ASSOCIATION_STATUS status) {
        String query = "SELECT status.application FROM " + ApplicationUserAssociation.class.getSimpleName()
                + " status " + "WHERE status.user =:user ";
        if (status != null) {
            query += "AND status.status = :status";
        }
        TypedQuery<Application> q = em.createQuery(query, Application.class);
        q.setParameter("user", user);
        if (status != null) {
            q.setParameter("status", ASSOCIATION_STATUS.MEMBER);
        }
        return new ArrayList<Application>(q.getResultList());
    }

    @Override
    @Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
    public List<Application> listApplications(User user) {
        return listApplications(user, ASSOCIATION_STATUS.MEMBER);
    }

    @Override
    @Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
    public List<Application> listActiveApplications(User user) {
        List<Application> list = listApplications(user, ASSOCIATION_STATUS.MEMBER);
        Iterator<Application> iter = list.iterator();
        while (iter.hasNext()) {
            Application next = iter.next();
            if (next.getActive() == null || !next.getActive()) {
                iter.remove();
            }
        }
        return list;
    }

    @Override
    @Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
    public List<Application> listAllApplications(User user) {
        return listApplications(user, null);
    }

    @Override
    public boolean queuePasswordSetupMail(Application app, User currentUser, User user) {
        String language = user.getLanguage();
        String toFullName = user.getFirstName() + " " + user.getLastName();
        String senderFullName = null;
        if (currentUser != null) {
            senderFullName = currentUser.getFirstName() + " " + currentUser.getLastName() + " ("
                    + currentUser.getMail() + ")";
        }

        String mailFrom = getMailFrom();

        String contextPath = getContextPath();
        Token token = createNewToken(currentUser, user, TokenPurpose.PASSWORD_RESET, 30);
        String url = contextPath + "api/public/password/reset/" + user.getMail() + "/" + token.getToken();
        String body = null;
        if (senderFullName != null) {
            body = i18nBundle.getLabel(language, "services.mail.set_password.body.sender", toFullName, senderFullName,
                    url);
        } else {
            body = i18nBundle.getLabel(language, "services.mail.set_password.body.nosender", toFullName, url);
        }
        String subject = i18nBundle.getLabel(language, "services.mail.set_password.subject");

        mailService.queueMail(mailFrom, user.getMail(), subject, body);
        return true;
    }

    @Override
    public boolean queueCredentialsMail(Application app, User currentUser, User user) {
        String language = user.getLanguage();
        String fullName = user.getFirstName() + " " + user.getLastName();
        String mailFrom = getMailFrom();
        String newPassword = SecurityUtils.getRandomPassword();
        String body = i18nBundle.getLabel(language, "services.mail.credentials.body", fullName, user.getMail(),
                user.getPassword());
        String subject = i18nBundle.getLabel(language, "services.mail.credentials.subject");
        mailService.queueMail(mailFrom, user.getMail(), subject, body);
        return true;
    }

    @Override
    public boolean queueActivationMail(Application app, User currentUser, User user) {
        if (!em.contains(user)) {
            user = findById(user.getId());
        }
        String language = user.getLanguage();
        String fullName = user.getFirstName() + " " + user.getLastName();

        String mailFrom = getMailFrom();

        String contextPath = getContextPath();
        Token token = createActivationToken(currentUser, user);

        String url = contextPath + "api/public/activate/" + user.getMail() + "/" + token.getToken();
        String body = i18nBundle.getLabel(language, "services.mail.registration.body", fullName, url);
        String subject = i18nBundle.getLabel(language, "services.mail.registration.subject");

        mailService.queueMail(mailFrom, user.getMail(), subject, body);
        return true;
    }

    @Override
    public boolean queueActivationEmail(Application app, User currentUser, User user, String email) {
        if (!em.contains(user)) {
            user = findById(user.getId());
        }
        String language = user.getLanguage();
        String fullName = user.getFirstName() + " " + user.getLastName();

        String mailFrom = getMailFrom();

        String contextPath = getContextPath();
        Token token = createActivationToken(currentUser, user);

        String url = contextPath + "api/public/activate/" + user.getMail() + "/" + token.getToken();
        String body = i18nBundle.getLabel(language, "services.mail.registration.body", fullName, url); //TODO: cambiar esto
        String subject = i18nBundle.getLabel(language, "services.mail.registration.subject");

        mailService.queueMail(mailFrom, email, subject, body);
        return true;
    }

    @Override
    public boolean queueSendActivationEmail(String email, MFDevice device) {
        String language = "es";
        String mailFrom = getMailFrom();
        String contextPath = getContextPath();

        // Convertir el objeto a JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObject = "";
        try {
            jsonObject = objectMapper.writeValueAsString(device);
        } catch (JsonProcessingException e) {
            return false;
        }

        // Codificar el JSON a base64
        String encodedObject = Base64.getEncoder().encodeToString(jsonObject.getBytes(StandardCharsets.UTF_8));

        //String url = contextPath + "account/activation.mob?device=" + encodedObject;
        String url =  "http://localhost:8080/mf/account/activation.mob?device=" + encodedObject; //TODO: obtener del properties el dominio del server
        String body = i18nBundle.getLabel(language, "services.mail.activation.body", url);
        String subject = i18nBundle.getLabel(language, "services.mail.activation.subject");

        mailService.queueMail(mailFrom, email, subject, body);
        return true;
    }

    @Override
    public Token createActivationToken(User currentUser, User user) {
        return createNewToken(currentUser, user, TokenPurpose.ACTIVATION, 30);
    }

    private Token createNewToken(User currentUser, User user, int purpose, int days) {
        Calendar inst = Calendar.getInstance();
        inst.add(Calendar.DAY_OF_MONTH, days);
        Token token = tokenService.createNewToken(currentUser, user, purpose, inst.getTime());
        return token;
    }

    private String getContextPath() {
        String contextPath = systemParams.getStrValue(DBParameters.CONTEXT_PATH);
        if (contextPath == null) {
            throw new RuntimeException("System Parameter [CONTEXT_PATH] id=[" + DBParameters.CONTEXT_PATH + "] NOT SET");
        }
        return contextPath;
    }

    @Override
    public boolean changePassword(User user, String newPassword) {
        if (!em.contains(user)) {
            user = findById(user.getId());
        }
        // FIXME password should at least be encrypted
        user.setPassword(newPassword);
        if (user.getSalt() == null) {
            String salt = SecurityUtils.getRandomSalt();
            user.setSalt(salt);
        }
        try {
            user.setSecurePassword(SecurityUtils.hashPassword(newPassword, user.getSalt()));
        } catch (Exception e) {
            throw new RuntimeException("Can't generate new secure password for user:" + user.getMail(), e);
        }

        return true;
    }

    @Override
    public User changeFullName(User user, String firstName, String lastName) {
        if (!em.contains(user)) {
            user = findById(user.getId());
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }


    @Override
    public User changeLanguage(User user, String language) {
        if (!em.contains(user)) {
            user = findById(user.getId());
        }

        user.setLanguage(language);
        return user;
    }

    /*
     * #747 2) Adding a user to an application: a) If the users does't exists,
     * then there are no problems :D b) If the users exists on another
     * application, the system will propose to send to the existing user an
     * invitation. If the users had existed, but was already deleted, the system
     * should consider it as a new user (the old user should be kept for audit
     * purposes)
     */
    @Override
    public boolean addUserToApp(User currentUser, Application app, User userToAdd) {
        String mail = userToAdd.getMail();
        User u = findSingleByProperty(User.MAIL, mail);

        if (!em.contains(app)) {
            app = em.find(Application.class, app.getId());
        }

        if (u != null) {
            if (isMember(app.getId(), u)) {
                return false;
            } else {
                associateUserAsMember(app, u);
            }

            if (u.getApplication() == null) {
                u.setApplication(app);
            }
        } else {
            addNewUser(currentUser, app, userToAdd);
        }

        return true;
    }

    private void associateUserAsInvited(Application application, User user) {
        if (!isMember(application.getId(), user)) {
            associateUserToApp(application, user, ASSOCIATION_STATUS.INVITED);
        } else {
            throw new RuntimeException("User is already a member");
        }
    }

    private void associateUserAsMember(Application application, User user) {
        associateUserToApp(application, user, ASSOCIATION_STATUS.MEMBER);
    }

    private void associateUserToApp(Application application, User user, ASSOCIATION_STATUS status) {
        ApplicationUserAssociationPK pk = new ApplicationUserAssociationPK();
        pk.setApplication(application);
        pk.setUser(user);

        ApplicationUserAssociation inst = em.find(ApplicationUserAssociation.class, pk);
        if (inst == null) {
            inst = new ApplicationUserAssociation();
            inst.setApplication(application);
            inst.setUser(user);
            inst.setStatus(status);
            em.persist(inst);
        } else {
            inst.setStatus(status);
        }
    }

    /*
     * #747 1) Deleting a user: If the user exists in multiple application, the
     * user access to the current application should be forbidden. If a users
     * exists only in the current application, then the user should be marked as
     * "deleted" (logical delete)
     */
    @Override
    public boolean deleteUserAppAssociation(User currentUser, Application app, User userToRemove) {
        if (!em.contains(userToRemove)) {
            userToRemove = em.find(User.class, userToRemove.getId());
        }

        if (!em.contains(app)) {
            app = em.find(Application.class, app.getId());
        }

        Query query = em.createQuery("FROM  " + ApplicationUserAssociation.class.getSimpleName()
                + " association WHERE association.application=:app AND user=:user");
        query.setParameter("app", app);
        query.setParameter("user", userToRemove);

        try {
            ApplicationUserAssociation association = (ApplicationUserAssociation) query.getSingleResult();
            em.remove(association);
            em.flush();
            em.refresh(userToRemove);
            Application defaultApp = userToRemove.getApplication();
            List<Application> apps = userToRemove.getApplications();
            if (apps.size() == 0) {
                // the user isn't bound to any application, just delete it
                userToRemove.setDeleted(true);
            } else {
                if (app.equals(defaultApp)) {
                    // we just removed the default application
                    // set another one as the default
                    Application newDefaultApp = apps.get(0);
                    userToRemove.setApplication(newDefaultApp);

                }
            }
            // #1405
            // 1. Remove from all groups in the app
            // 2. Remove all roles within the app
            // easiest way around this, Native Query
            // http://stackoverflow.com/questions/3298175/how-to-delete-all-associations-in-a-hibernate-jointable-at-once

            // 1. remove application authorizations OK
            removeAppAuthorizations(app, userToRemove);
            // 2. remove project authorizations OK
            removeProjectAuthorizations(app, userToRemove);
            // 3. remove form authorizations OK
            removeFormAuthorizations(app, userToRemove);
            // 4. remove pool authorizations OK
            removePoolAuthorizations(app, userToRemove);
            // 5. remove from app groups OK
            removeFromGroups(app, userToRemove);

            return true;
        } catch (NoResultException e) {
            return false;
        }

    }

    private void removeFromGroups(Application app, User userToRemove) {
        String removeSQL = "DELETE FROM core.groups_users WHERE user_id = :id "
                + " AND group_id IN (SELECT g.id FROM core.groups g WHERE g.application_id=:appId)";
        executeRemoveUpdate(app, userToRemove, removeSQL);
    }

    private void removePoolAuthorizations(Application app, User userToRemove) {
        String removeSQL = "DELETE FROM core.authorizable_entities_authorizations WHERE authorizable_entity_id = :id "
                + " AND pool_id IN (SELECT p.id FROM pools.pools p WHERE p.application_id = :appId)";
        executeRemoveUpdate(app, userToRemove, removeSQL);
    }

    private void removeFormAuthorizations(Application app, User userToRemove) {
        String removeSQL = "DELETE FROM core.authorizable_entities_authorizations WHERE authorizable_entity_id = :id "
                + " AND form_id IN (SELECT f.id FROM forms.forms f JOIN projects.projects p ON f.project_id = p.id WHERE p.application_id = :appId)";
        executeRemoveUpdate(app, userToRemove, removeSQL);
    }

    private void removeProjectAuthorizations(Application app, User userToRemove) {
        String removeSQL = "DELETE FROM core.authorizable_entities_authorizations WHERE authorizable_entity_id = :id "
                + " AND project_id IN (SELECT p.id FROM projects.projects p WHERE p.application_id = :appId)";
        executeRemoveUpdate(app, userToRemove, removeSQL);
    }

    private void removeAppAuthorizations(Application app, User userToRemove) {
        String removeSQL = "DELETE FROM core.authorizable_entities_authorizations WHERE authorizable_entity_id = :id "
                + " AND application_id = :appId ";
        executeRemoveUpdate(app, userToRemove, removeSQL);
    }

    private void executeRemoveUpdate(Application app, User userToRemove, String removeSQL) {
        Query q = em.createNativeQuery(removeSQL);
        q.setParameter("id", userToRemove.getId());
        q.setParameter("appId", app.getId());
        q.executeUpdate();
    }

    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public UserDTO getUserDTO(Long userId) {
        User user = this.findById(userId);
        UserDTO dto = entityToDTO(user);
        return dto;
    }

    private UserDTO entityToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMail(user.getMail());
        dto.setPassword(user.getPassword());
        dto.setActive(user.getActive());
        return dto;
    }

    @Override
    public User addGroups(Long userId, List<Long> groupsId) {
        User user = this.findById(userId);
        for (Long groupId : groupsId) {
            Group group = this.groupService.findById(groupId);
            user.getGroups().add(group);
            group.getUsers().add(user);
        }
        return user;
    }

    @Override
    public User removeGroups(Long userId, List<Long> groupsId) {
        User user = this.findById(userId);
        for (Long groupId : groupsId) {
            Group group = this.groupService.findById(groupId);
            user.getGroups().remove(group);
            group.getUsers().remove(user);
        }
        return user;
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findAll(Application app, Group group, boolean inGroup, String orderBy,
                                         boolean ascending, int pageNumber, int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        builder.pageNumber(pageNumber).pageSize(pageSize);
        if (group != null && inGroup) {
            builder.inGroup(group);
        } else if (group != null) {
            builder.notInGroup(group);
        }

        if (ascending) {
            builder.asc(orderBy);
        } else {
            builder.desc(orderBy);
        }
        return find(app, builder.newInstance());
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findAll(Application app, String orderBy, boolean ascending, int pageNumber,
                                         int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        builder.pageNumber(pageNumber).pageSize(pageSize);
        if (ascending) {
            builder.asc(orderBy);
        } else {
            builder.desc(orderBy);
        }
        return find(app, builder.newInstance());
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findByMail(Application app, String value, int pageNumber, int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        if (value != null) {
            builder.property(User.MAIL, BaseService.OPER_CONTAINS, value);
        }
        builder.pageNumber(pageNumber).pageSize(pageSize);
        builder.asc(User.MAIL);
        return find(app, builder.newInstance());
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findByMail(Application app, Group group, boolean inGroup, String value,
                                            int pageNumber, int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        if (value != null) {
            builder.property(User.MAIL, BaseService.OPER_CONTAINS, value);
        }
        builder.pageNumber(pageNumber).pageSize(pageSize);
        if (group != null && inGroup) {
            builder.inGroup(group);
        } else if (group != null) {
            builder.notInGroup(group);
        }
        builder.asc(User.MAIL);
        return find(app, builder.newInstance());
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findByProperty(Application app, final String propertyName, final String oper,
                                                Object value, final String orderBy, boolean ascending, int pageNumber, int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        if (value instanceof String &&  oper.equals(OPER_CONTAINS)) {
        	value = ((String)value).toLowerCase();
        }
        builder.property(propertyName, oper, value).pageNumber(pageNumber).pageSize(pageSize);
        if (ascending) {
            builder.asc(orderBy);
        } else {
            builder.desc(orderBy);
        }
        return find(app, builder.newInstance());
    }

    @Override
    @Authorizable(AuthorizationNames.App.USER_LIST)
    public PagedData<List<User>> findByProperty(Application app, Group group, boolean inGroup,
                                                final String propertyName, final String oper, Object value, final String orderBy, boolean ascending,
                                                int pageNumber, int pageSize) {
        FindUserCriteriaBuilder builder = new FindUserCriteriaBuilder();
        builder.property(propertyName, oper, value).pageNumber(pageNumber).pageSize(pageSize);
        if (group != null && inGroup) {
            builder.inGroup(group);
        } else if (group != null) {
            builder.notInGroup(group);
        }
        if (ascending) {
            builder.asc(orderBy);
        } else {
            builder.desc(orderBy);
        }
        return find(app, builder.newInstance());
    }

    /**
     * This Method contains all the logic to find Users.
     * <p/>
     * It's called by the other exposed methods findXX
     *
     * @param app
     * @param criteria
     * @return
     */
    private PagedData<List<User>> find(Application app, FindUserCriteria criteria) {
        String propertyWhereClause = "";
        String propertyValue = null;

        if (criteria.getPropertyCriteria() != null) {
            PropertyCriteria property = criteria.getPropertyCriteria();
            propertyWhereClause = "AND "
                    + whereClause(fieldNameWithPrefix(property.getName()), property.getOper(), property.getValue());
            String oper = property.getOper();
            if (oper.equals(OPER_CONTAINS) || oper.equals(OPER_NOT_CONTAINS)) {
                propertyValue = "%" + property.getValue() + "%";
            } else if (oper.equals(OPER_EQUALS)) {
            	propertyValue = "" + property.getValue();
            }
        }

        String orderBy = "id";
        boolean ascending = true;
        SortCriteria c = criteria.getSortCriteria();
        if (c != null) {
            orderBy = c.getOrderBy();
            ascending = c.isAscending();
        }

        Group group = null;
        String groupWhereClause = "";
        String groupJoin = "";
        if (criteria.getGroupCriteria() != null) {
            GroupCriteria groupCriteria = criteria.getGroupCriteria();
            group = groupCriteria.getGroup();
            boolean inGroup = groupCriteria.isInGroup();
            if (inGroup) {
                groupJoin = "JOIN u.groups g";
                groupWhereClause = "AND g.deleted = false AND g = :group ";
            } else {
                groupWhereClause = "AND :group NOT IN elements(u.groups) ";
            }
        }

        PagingCriteria pagingCriteria = criteria.getPagingCriteria();
        int pageNumber = pagingCriteria.getPageNumber();
        int pageSize = pagingCriteria.getPageSize();

        String selectQueryStr = "SELECT DISTINCT(u) FROM User u JOIN u.applications apps " + groupJoin
                + " WHERE u.deleted = false AND apps=:app " + groupWhereClause + propertyWhereClause
                + " ORDER BY $1 $2 ";
        selectQueryStr = selectQueryStr.replace("$1", fieldNameWithPrefix(orderBy));
        selectQueryStr = selectQueryStr.replace("$2", ascending ? " ASC " : " DESC ");
        TypedQuery<User> query = em.createQuery(selectQueryStr, User.class);
        setFindQueryParameters(query, app, propertyValue, group, pageNumber, pageSize);
        query.setMaxResults(pageSize);
        query.setFirstResult((pageNumber - 1) * pageSize);
        List<User> data = query.getResultList();

        String countQueryStr = "SELECT COUNT(DISTINCT u) FROM User u JOIN u.applications apps " + groupJoin
                + " WHERE u.deleted = false AND apps=:app " + groupWhereClause + propertyWhereClause;
        Query countQuery = em.createQuery(countQueryStr);
        setFindQueryParameters(countQuery, app, propertyValue, group, pageNumber, pageSize);
        Long count = (Long) countQuery.getSingleResult();

        return new PagedData<List<User>>(data, count, pageNumber, pageSize, data.size());
    }

    private void setFindQueryParameters(Query query, Application app, String propertyValue, Group group,
                                        int pageNumber, int pageSize) {
        query.setParameter("app", app);
        if (propertyValue != null) {
            query.setParameter("value", propertyValue);
        }
        if (group != null) {
            query.setParameter("group", group);
        }
    }

    @Override
    public User updateUser(User currentUser, User updatedUser, Application app) {
        if (isMember(app.getId(), updatedUser)) {
            User user = findByMail(updatedUser.getMail());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setActive(updatedUser.getActive());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
                updateSecurePassword(updatedUser.getPassword(), user);
            }
            return user;
        } else {
            throw new AuthorizationException("The user is not a member of the Application");
        }

    }

    private void updateSecurePassword(String password, User p_user) {
        String salt = SecurityUtils.getRandomSalt();
        p_user.setSalt(salt);
        try {
            p_user.setSecurePassword(SecurityUtils.hashPassword(password, salt));
        } catch (Exception e) {
            throw new AuthorizationException("Can't store secure password for user: " + p_user.getMail());
        }
    }

    @Override
    public boolean acceptInvitation(String mail, String invitationToken) {
        Token token = tokenService.getToken(invitationToken);
        if (token != null && token.getActive()) {
            // if the token is valid, add the user to the application
            User user = token.getGrantee();

            if (!user.getMail().equals(mail)) {
                throw new AuthorizationException("Invalid token");
            }

            Application app = token.getApplication();
            Long appId = app.getId();
            if (isInvited(appId, user) && !isMember(appId, user)) {
                associateUserAsMember(app, user);
                tokenService.useToken(user, invitationToken);
                // delete other invitations
                tokenService.deleteTokens(user, TokenPurpose.APP_INVITATION);
                return true;
            } else {
                return false;
            }
        } else {
            throw new AuthorizationException("Invalid token");
        }
    }

    @Override
    public boolean inviteUserToApp(User currentUser, Application application, User userToInvite, Boolean addDirectly) {
        User u = findByMail(userToInvite.getMail());
        Application app = em.find(Application.class, application.getId());
        if (u == null) {
            userToInvite.setId(null);
            // It's a new user. It doesn't exist in the system.
            if (!addDirectly) {
	            userToInvite = addNewUser(currentUser, app, userToInvite, ASSOCIATION_STATUS.INVITED);
	            userToInvite.setActive(false);
	            queueInvitationMail(app, currentUser, userToInvite);
            } else {
            	userToInvite = addNewUser(currentUser, app, userToInvite, ASSOCIATION_STATUS.MEMBER);
            }
        } else {
            // The user already exists.
            if (!isMember(app.getId(), u)) {
                // if its not a member, invite the user or add directly
                checkLicense(app);
                if (!addDirectly) {
	                associateUserAsInvited(app, u);
	                queueInvitationMail(app, currentUser, u);
                } else {
                	associateUserAsMember(app, u);
                }    
            } else {
                // the user is already a member, no point in sending an
                // invitation
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean queueInvitationMail(Application app, User currentUser, User user) {
        if (!em.contains(user)) {
            user = findByMail(user.getMail());
        }

        if (!em.contains(app)) {
            app = em.find(Application.class, app.getId());
        }

        if (isInvited(app.getId(), user)) {
            String language = user.getLanguage();
            String fullName = user.getFirstName() + " " + user.getLastName();
            String inviterName = currentUser.getFirstName() + " " + currentUser.getLastName() + " ("
                    + currentUser.getMail() + ")";

            String mailFrom = getMailFrom();

            String contextPath = getContextPath();
            Token token = createNewToken(currentUser, user, TokenPurpose.APP_INVITATION, 30);
            token.setApplication(app);

            String url = contextPath + "api/public/invitation/accept/" + user.getMail() + "/" + token.getToken();
            String body = i18nBundle.getLabel(language, "services.mail.invitation.body", fullName, inviterName,
                    app.getName(), url);
            String subject = i18nBundle.getLabel(language, "services.mail.invitation.subject", app.getName());

            mailService.queueMail(mailFrom, user.getMail(), subject, body);
            return true;
        }
        return false;
    }

    // FIXME is it ok if we say that a system administrator is member of all
    // applications?
    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public boolean isMember(Long appId, User user) {
        return isAssociation(appId, user.getMail(), ASSOCIATION_STATUS.MEMBER);
    }

    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public boolean isInvited(Long appId, User user) {
        return isAssociation(appId, user.getMail(), ASSOCIATION_STATUS.INVITED);
    }

    private boolean isAssociation(Long appId, String mail, ASSOCIATION_STATUS status) {
        String query = "SELECT status FROM " + ApplicationUserAssociation.class.getSimpleName() + " status "
                + "WHERE status.user.mail =:mail AND status.application.id=:appId AND status.status = :status ";
        Query q = em.createQuery(query);
        q.setMaxResults(1);
        q.setParameter("appId", appId);
        q.setParameter("mail", mail);
        q.setParameter("status", status);
        int count = q.getResultList().size();
        return count == 1;
    }

    @Override
    public void activateUser(User user) {
        if (!em.contains(user)) {
            user = findByMail(user.getMail());
        }
        user.setActive(true);
    }

    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public User findById(Long userId) {
        return super.findById(userId);
    }

    @Override
    public User save(User user) {
        if (user.getSalt() == null) {
            String salt = SecurityUtils.getRandomSalt();
            try {
                user.setSecurePassword(SecurityUtils.hashPassword(user.getPassword(), salt));
            } catch (Exception e) {
                throw new RuntimeException("Can't generate new secure password for user:" + user.getMail(), e);
            }
            user.setSalt(salt);
        }
        return super.save(user);
    }

    private List<Group> listGroups(User user) {
        TypedQuery<Group> query = em.createQuery("SELECT g FROM " + User.class.getName() + " u JOIN u.groups g "
                + "WHERE g.deleted = false AND u = :user", Group.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    @Authorizable(checkType = CHECK_TYPE.NONE)
    public List<Long> listGroupsIds(User user) {
        List<Group> groups = listGroups(user);
        if (groups != null && !groups.isEmpty()) {
            List<Long> ids = new ArrayList<Long>();
            for (Group g : groups) {
                ids.add(g.getId());
            }
            return ids;
        }
        return Collections.emptyList();
    }

    @Override
    public PagedData<List<PendingRegistrationDTO>> listPendingRegistrations(int pageNumber, int pageSize) {
        Query countQuery = em
                .createQuery("SELECT COUNT(t) FROM User u, Token t WHERE "
                        + "u.active = false AND u.deleted = false AND t.grantee = u AND t.purpose = 1 AND t.active = true AND t.deleted = false"
                        + " AND length(u.mail) > 0");

        long count = (Long) countQuery.getSingleResult();
        List<PendingRegistrationDTO> data = null;
        if (count > 0) {
            data = new ArrayList<PendingRegistrationDTO>();

            TypedQuery<Token> dataQuery = em
                    .createQuery(
                            "SELECT t"
                                    + " FROM User u, Token t WHERE "
                                    + " u.active = false AND u.deleted = false AND t.grantee = u AND t.purpose = 1 AND t.active = true AND t.deleted = false "
                                    + " AND length(u.mail) > 0 ORDER BY t.created DESC", Token.class);
            dataQuery.setMaxResults(pageSize);
            dataQuery.setFirstResult((pageNumber - 1) * pageSize);
            List<Token> activationTokens = dataQuery.getResultList();

            for (Token t : activationTokens) {
                User u = t.getGrantee();
                PendingRegistrationDTO dto = new PendingRegistrationDTO(t.getId(), u.getMail(), u.getFirstName(),
                        u.getLastName(), t.getToken(), t.getCreated());
                data.add(dto);
            }
        } else {
            data = Collections.emptyList();
        }

        return new PagedData<List<PendingRegistrationDTO>>(data, count, pageNumber, pageSize, data.size());
    }

    @Override
    public Long numberOfSystemUsers() {
        Query q = em.createQuery("Select count(A) from " + User.class.getName() + " A where A.rootUser=true ");
        return (Long) q.getSingleResult();

    }

    @Override
    public List<UserDTO> listAllActiveUsers() {
        TypedQuery<User> q = em.createQuery("FROM " + User.class.getName()
                        + " u WHERE u.deleted = false AND u.active = true " + "ORDER BY u.firstName, u.lastName, u.mail",
                User.class);
        List<User> users = q.getResultList();
        List<UserDTO> dtos = new ArrayList<UserDTO>();
        for (User u : users) {
            UserDTO dto = entityToDTO(u);
            dto.setPassword(null);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<UserDTO> listAllActiveUsers(Application app) {
        TypedQuery<User> q = em.createQuery("SELECT u FROM User u JOIN u.applications a "
                        + " WHERE u.deleted = false AND u.active = true AND a.id = :appId",
                User.class);
        q.setParameter("appId", app.getId());
        List<User> users = q.getResultList();
        List<UserDTO> dtos = new ArrayList<UserDTO>();
        for (User u : users) {
            UserDTO dto = entityToDTO(u);
            dto.setPassword(null);
            dtos.add(dto);
        }
        return dtos;
    }

    private Long count(Long appId) {
        Query countQuery = em.createQuery("SELECT COUNT(u) FROM User u JOIN u.applications a WHERE "
                + "u.deleted = false AND a.id = :appId ");
        countQuery.setParameter("appId", appId);
        Long count = (Long) countQuery.getSingleResult();
        return count;
    }

    @Override
    public void changePassword(Long userId, String password) {
        User user = findById(userId);
        user.setPassword(password);
        updateSecurePassword(password, user);
    }

    @Override
    public User changeDefaultApplication(Long userId, Long appId) {
        User user = findById(userId);
        if (isMember(appId, user)) {
            Application application = em.find(Application.class, appId);
            user.setApplication(application);
            return user;
        } else {
            throw new AuthorizationException(String.format("User id: %d is not a member of application id: %d", userId,
                    appId));
        }

    }

	@Override
	public String getResetPasswordUrl(User currentUser, User user) {
		String contextPath = getContextPath();
        Token token = createNewToken(currentUser, user, TokenPurpose.PASSWORD_RESET, 30);
        String url = contextPath + "api/public/password/reset/" + user.getMail() + "/" + token.getToken();
        
        return url;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String getLastLogin(Long appId, String mail) {
		Query query = em.createNativeQuery("SELECT u.mail, l.login_at FROM log.logins l "
				+ "JOIN core.users u ON l.user_id = u.id " + "WHERE l.application_id = :appId AND u.mail IS NOT NULL AND u.mail= :mail "
				+ "ORDER BY login_at DESC");
		query.setMaxResults(1);
		query.setParameter("appId", appId);
		query.setParameter("mail", mail);
		List list = query.getResultList();
		String lastLogin = "";
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		for (Object o : list) {
			Object[] row = (Object[]) o;
			java.sql.Timestamp d = (java.sql.Timestamp) row[1];
			lastLogin = formatter.format(d);
		}
		return lastLogin;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map<Long, String> getLastLogins(Long appId,
			Long[] userIds, int numberOfUsers) {
		Query query = em.createNativeQuery("SELECT DISTINCT ON(l.user_id) l.user_id, l.login_at FROM log.logins l "
				+ "WHERE l.application_id = :appId AND l.user_id IN "
				+ ":userIds "
				+ "ORDER BY l.user_id, l.login_at DESC");
		query.setMaxResults(numberOfUsers);
		query.setParameter("appId", appId);
		query.setParameter("userIds", Arrays.asList(userIds));
		List list = query.getResultList();
		Map<Long, String> userLastLogins = new HashMap<Long, String>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		for (Object o : list) {
			Object[] row = (Object[]) o;
			long userId = ((BigInteger) row[0]).longValue();
			java.sql.Timestamp d = (java.sql.Timestamp) row[1];
			userLastLogins.put(new Long(userId), formatter.format(d)) ;
		}
		return userLastLogins;
	}

}
