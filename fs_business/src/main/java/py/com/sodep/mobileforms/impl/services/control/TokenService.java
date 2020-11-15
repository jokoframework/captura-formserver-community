package py.com.sodep.mobileforms.impl.services.control;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.entities.core.Token;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.control.ITokenService;

@Service("TokenService")
@Transactional
public class TokenService implements ITokenService {

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;
	
	private static int DEFAULT_TOKEN_LENGTH = 32;

	@Override
	public boolean isValid(User grantee, String token, int purpose) {
		// FIXME sometimes deleted and active are confusing
		TypedQuery<Token> q = em.createQuery("FROM " + Token.class.getName()
				+ " WHERE deleted = false AND active = true "
				+ "AND grantee = :grantee AND token = :token AND purpose = :purpose ", Token.class);
		q.setParameter("grantee", grantee);
		q.setParameter("token", token);
		q.setParameter("purpose", purpose);
		try {
			Token t = q.getSingleResult();
			return t.getExpires() == null || t.getExpires().after(new Date());
		} catch (NoResultException e) {
			return false;
		}
	}

	@Override
	public boolean useToken(User grantee, String token) {
		Token t = getToken(grantee, token);
		if (t != null) {
			if (t.getExpires() == null || t.getExpires().after(new Date())) {
				t.setActive(false);
			}
			return true;
		}
		return false;
	}
	
	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public boolean deactivateToken(Long id) {
		Token t = em.find(Token.class, id);
		if (t != null) {
			// This "if" was the cause of #3263
			// if (t.getExpires() == null || t.getExpires().after(new Date())) {
			t.setActive(false);
			// }
			return true;
		}
		return false;
	}

	@Override
	public int purpose(User grantee, String token) {
		Token t = getToken(grantee, token);
		if (t != null) {
			if (t.getExpires() == null || t.getExpires().after(new Date())) {
				return t.getPurpose();
			}
		}
		return -1;
	}

	private Token getToken(User grantee, String token) {
		TypedQuery<Token> q = em.createQuery("FROM " + Token.class.getName()
				+ " WHERE deleted = false AND active = true " + "AND grantee = :grantee AND token = :token ",
				Token.class);
		q.setParameter("grantee", grantee);
		q.setParameter("token", token);
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Token createNewToken(User currentUser, User grantee, int purpose, Date expires) {
		Token token = new Token();
		token.setGranter(currentUser);
		token.setGrantee(grantee);
		token.setPurpose(purpose);
		token.setExpires(expires);
		String str = RandomStringUtils.randomAlphanumeric(DEFAULT_TOKEN_LENGTH);
		token.setToken(str);
		em.persist(token);
		return token;
	}
	
	@Override
	public boolean deleteTokens(User grantee, int purpose) {
		Query updateQuery = em.createQuery("UPDATE " + Token.class.getName()
				+ " t SET t.deleted = true WHERE t.grantee=:grantee AND t.purpose=:purpose");
		updateQuery.setParameter("grantee", grantee);
		updateQuery.setParameter("purpose", purpose);
		int updateCount = updateQuery.executeUpdate();
		return updateCount > 0;
	}

	@Override
	public Token getToken(String token) {
		TypedQuery<Token> q = em.createQuery("FROM " + Token.class.getName() + " WHERE deleted = false AND token = :token ", Token.class);
		q.setParameter("token", token);
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
