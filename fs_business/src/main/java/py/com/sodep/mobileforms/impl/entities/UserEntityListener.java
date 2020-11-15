package py.com.sodep.mobileforms.impl.entities;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PreRemove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mobileforms.api.entities.core.User;


public class UserEntityListener {
	
	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;
	
	private static Logger logger = LoggerFactory.getLogger(UserEntityListener.class);
	
	@PreRemove
	public void preRemove(User user){
		logger.debug("remove " + user.getId());
		logger.debug(em.toString());
	}

}
