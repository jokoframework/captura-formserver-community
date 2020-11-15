package py.com.sodep.mobileforms.impl.services.auth;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.IAuthenticationService;
import py.com.sodep.mobileforms.utils.SecurityUtils;

@Service("AuthenticationService")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
class AuthenticationService implements IAuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    
	@PersistenceContext(unitName = "mobileforms")
    protected EntityManager em;

    @Override
    public boolean checkCredentials(String mail, String password) {
        Query q = em.createQuery("SELECT COUNT(u) FROM " + User.class.getName()
                + " u WHERE u.deleted = false AND u.mail=:mail AND u.password=:password AND u.active=true");
        q.setParameter("password", password);
        Long count = (Long) q.getSingleResult();
        return count > 0;

    }

    @Override
    public boolean checkCredentials(String mail, String password, String deviceToken) {
        Query q = em
                .createQuery("SELECT COUNT(u) FROM "
                        + User.class.getName()
                        + " u JOIN u.devices d WHERE u.deleted = false AND u.mail=:mail AND u.password=:password AND u.active=true AND d.deleted!=true AND d.active=true AND d.token=:token ");
        q.setParameter("mail", mail);
        q.setParameter("password", password);
        q.setParameter("token", deviceToken);
        Long count = (Long) q.getSingleResult();
        return count > 0;
    }

    @Override
    public boolean checkCredentials(User user, String password) {
        boolean loginOk = false;
        if (user.getSalt() == null) {
            user = generateSecurePassword(user);
        } else {
            //Once secure password is set, we delete the plain password
            user = clearPlainTextPassword(user);
        }
        //Here we got the user with secure password
        try {
            String challengePassword = SecurityUtils.hashPassword(password, user.getSalt());
            loginOk = user.getSecurePassword().equals(challengePassword);
        } catch (Exception e) {
            throw new RuntimeException("Can't generate new challenge password for user:" + user.getMail(), e);
        }
        return loginOk;
    }

    private User clearPlainTextPassword(User pUser) {
        if(User.PASSWORD_SECURED.equals(pUser.getPassword())) {
            User userFromBD = em.find(User.class, pUser.getId());
            userFromBD.setPassword(User.PASSWORD_SECURED);
            em.persist(userFromBD);
            return userFromBD;
        } else {
            return pUser;
        }
    }

    private User generateSecurePassword(User user) {
        User userFromBD = em.find(User.class, user.getId());
        String salt = SecurityUtils.getRandomSalt();
        try {
            userFromBD.setSecurePassword(SecurityUtils.hashPassword(user.getPassword(), salt));
        } catch (Exception e) {
            throw new RuntimeException("Can't generate new secure password for user:" + user.getMail(), e);
        }
        userFromBD.setSalt(salt);
        em.persist(userFromBD);
        return userFromBD;
    }

    @Override
    public boolean checkCredentialsAndDevice(User user, String deviceToken) {
        boolean loginOk = false;
        try {
            String securePassword = SecurityUtils.hashPassword(user.getPassword(), user.getSalt());
            Query q = em
                    .createQuery("SELECT COUNT(u) FROM "
                            + User.class.getName()
                            + " u JOIN u.devices d WHERE u.deleted = false AND u.mail=:mail AND u.securePassword=:securePassword AND u.active=true AND d.deleted!=true AND d.active=true AND d.token=:token ");
            q.setParameter("mail", user.getMail());
            q.setParameter("securePassword", securePassword);
            q.setParameter("token", deviceToken);
            Long count = (Long) q.getSingleResult();
            loginOk = count > 0;
        } catch (Exception e) {
        	LOG.error(e.getMessage(), e);
        }
        return loginOk;
    }


    public boolean isRootUser(String mail) {
        boolean isRoot = false;
        Query q = em.createQuery("SELECT COUNT(u) FROM " + User.class.getName()
                + " u WHERE u.mail=:mail AND u.rootUser = true ");
        q.setParameter("mail", mail);
        Long count = (Long) q.getSingleResult();
        isRoot = count > 0;
        return isRoot;
    }
}
