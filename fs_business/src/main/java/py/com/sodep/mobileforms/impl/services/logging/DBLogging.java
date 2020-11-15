package py.com.sodep.mobileforms.impl.services.logging;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.log.Login;
import py.com.sodep.mobileforms.api.entities.log.LoginType;
import py.com.sodep.mobileforms.api.entities.log.UncaughtException;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.logging.IDBLogging;

@Service("DBLogging")
@Transactional(propagation = Propagation.REQUIRES_NEW)
class DBLogging implements IDBLogging {

	private Logger logger = LoggerFactory.getLogger(DBLogging.class);
	
	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public UncaughtException logException(UncaughtException exception) {
		em.persist(exception);
		logger.error("Logged an uncaught exception");
		return exception;
	}

	@Override
	public void logLogin(Long userId, Long applicationId) {
		logLogin(userId, applicationId, LoginType.WEB);
	}

	
	@Override
	public void logLogin(Long userId, Long applicationId, LoginType loginType) {
		Login login = new Login(applicationId, userId);
		// CAP-198
		login.setLoginType(loginType);
		em.persist(login);
	}
}
