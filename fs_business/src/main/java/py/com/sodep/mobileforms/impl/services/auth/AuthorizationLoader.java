package py.com.sodep.mobileforms.impl.services.auth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.AuthorizationConfiguration;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.auth.AuthorizationGroup;
import py.com.sodep.mobileforms.api.services.auth.DeclareAuthorizationLevel;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.utils.StringUtils;

/**
 * This class will store on the Database the authorization declared on
 * {@link AuthorizationNames}
 * 
 * @author danicricco
 * 
 */
@Service("AuthorizationLoader")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
public class AuthorizationLoader {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationLoader.class);

	public final String authorizationClass = "py.com.sodep.mobileforms.api.constants.AuthorizationNames";

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	private Object lockConfigurationMap = new Object();
	private final HashMap<Integer, AuthorizationLevelConf> configuration = new HashMap<Integer, AuthorizationLevelConf>();

	public AuthorizationLevelConf getConfLevel(Integer level) {
		synchronized (lockConfigurationMap) {
			AuthorizationLevelConf conf = configuration.get(level);
			if (conf == null) {
				throw new IllegalArgumentException("The level #" + level
						+ " was not configured. Please check the class " + authorizationClass);
			}
			return conf;
		}
	}

	private void addLevelConfiguration(Integer level, String prefix, String columnId) {
		synchronized (lockConfigurationMap) {
			AuthorizationLevelConf conf = new AuthorizationLevelConf(level, prefix, columnId);
			configuration.put(level, conf);
		}
	}

	private void clearLevelConfiguration() {
		synchronized (lockConfigurationMap) {
			configuration.clear();
		}
	}

	/**
	 * This methods will iterate over an {@link AuthorizationConfiguration} and
	 * translate the state of this object to the DB. After calling this method
	 * the method {@link IAuthorizationService#reloadAuthorizations()} should be
	 * called, in order to keep the latest authorization definition on me
	 * 
	 * @param configuration
	 */
	public void insertAuthorizationConfiguration(AuthorizationConfiguration configuration) {
		logger.debug("Configuring authorizations dependencies and visibility");
		// All authorizations should be visible by default
		Query q = em.createQuery("Update " + Authorization.class.getName() + " A set A.visible=true");
		q.executeUpdate();
		em.flush();

		// mark the authorizations that shouldn't be visible
		List<String> hiddenAuthorizations = configuration.getHiddenAuthorizations();
		for (String auth : hiddenAuthorizations) {
			Authorization authorization = em.find(Authorization.class, auth);
			authorization.setVisible(false);
		}

		// Delete all dependencies of the authorizations, since we are going add
		// them agains
		q = em.createNativeQuery("delete from core.authorization_dependencies ");
		q.executeUpdate();
		em.flush();
		Map<String, Set<String>> dependenciesMap = configuration.getDependentAuthorization();
		Set<String> authWithDependencies = dependenciesMap.keySet();
		for (String authName : authWithDependencies) {
			Authorization auth = em.find(Authorization.class, authName);
			if (auth == null) {
				throw new IllegalArgumentException("Authorization '" + authName
						+ "' was declared as a dependant authorization but it is not a valid authorization ");
			}
			Set<String> dependencies = dependenciesMap.get(authName);
			ArrayList<Authorization> dependentAuthList = new ArrayList<Authorization>();
			for (String authDependendentName : dependencies) {
				Authorization dependentAuth = em.find(Authorization.class, authDependendentName);
				dependentAuthList.add(dependentAuth);
			}
			auth.setDependentAuthorizations(dependentAuthList);
		}

		// delete from the roles the authorization that are hidden. This
		// authorization can never be granted directly to any role

		logger.debug("DONE. Configuration of authorizations dependencies and visibility");

	}

	/**
	 * This method will iterate over the inner classes of the class
	 * {@link #authorizationClass} that are marked with
	 * {@link DeclareAuthorizationLevel}. Each of the static fields of this
	 * class will be considered an authorization of the level. After calling
	 * this method the method
	 * {@link IAuthorizationService#reloadAuthorizations()} should be called, in
	 * order to keep the latest authorization definition on memory
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Authorizable
	@SuppressWarnings("rawtypes")
	public void insertAndCheckAuthorizations() throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException {
		Class<?> authorizationNameClass = Class.forName(authorizationClass);
		// these maps are going to store the levels and prefix alread declared,
		// so we can avoid duplication and inform with a proper error message if
		// any.
		HashMap<Integer, Class> usedLevelMap = new HashMap<Integer, Class>();
		HashMap<String, Class> usedPrefixMap = new HashMap<String, Class>();
		HashMap<String, Class> usedColumnMap = new HashMap<String, Class>();
		// loop over the inner classes and only consider those that were marked
		// with the annotation DeclareAuthorizationLevel
		clearLevelConfiguration();
		Class<?>[] innerClasses = authorizationNameClass.getDeclaredClasses();

		ArrayList<String> authorizations = new ArrayList<String>();
		for (int i = 0; i < innerClasses.length; i++) {
			DeclareAuthorizationLevel annotation = innerClasses[i].getAnnotation(DeclareAuthorizationLevel.class);
			if (annotation != null) {
				// First check that there are no duplicate
				// DeclareAuthorizationLevel annotations

				int level = annotation.level();
				String prefix = annotation.prefix();
				String column = annotation.column();
				Class usedLevel = usedLevelMap.get(level);
				if (usedLevel != null) {
					throw new IllegalStateException(
							"There are two inner classes declared with the  Authorization level  " + level + ", "
									+ usedLevel.getName() + " and " + innerClasses[i].getName());
				} else {
					usedLevelMap.put(level, innerClasses[i]);
				}
				Class usedPrefix = usedPrefixMap.get(prefix);
				if (usedPrefix != null) {
					throw new IllegalStateException(
							"There are two inner classes declared with the same Authorization prefix  " + prefix + ", "
									+ usedPrefix.getName() + " and " + innerClasses[i].getName());
				} else {
					usedPrefixMap.put(prefix, innerClasses[i]);
				}
				Class usedColumn = usedColumnMap.get(column);
				if (usedColumn != null) {
					throw new IllegalStateException(
							"There are two inner classes declared with the same Authorization column  " + column + ", "
									+ usedColumn.getName() + " and " + innerClasses[i].getName());
				} else {
					usedColumnMap.put(column, innerClasses[i]);
				}
				String classProposedGroup = null;
				Integer classProposedPosition = 0;
				AuthorizationGroup annotationGroup = innerClasses[i].getAnnotation(AuthorizationGroup.class);
				if (annotationGroup != null) {
					classProposedGroup = annotationGroup.value();
					classProposedPosition = annotationGroup.position();
				}

				addLevelConfiguration(level, prefix, column);
				// iterate over the declared fields and insert them as
				// authorizations
				Field[] fields = innerClasses[i].getDeclaredFields();
				for (int j = 0; j < fields.length; j++) {
					Object value = fields[j].get(innerClasses[i]);
					annotationGroup = fields[j].getAnnotation(AuthorizationGroup.class);
					String authGroup;
					Integer groupPosition = 0;
					if (annotationGroup != null) {
						// the field has overwritten the authorization group
						authGroup = annotationGroup.value();
						groupPosition = annotationGroup.position();
					} else {
						authGroup = classProposedGroup;
						groupPosition = classProposedPosition;
					}
					String authName = value.toString();
					if (!authName.startsWith(prefix)) {
						throw new IllegalStateException("The value of the authorization " + innerClasses[i].getName()
								+ "." + fields[j].getName() + " should start with " + prefix);
					}
					insertAuthorizationIfItDoesntExists(authName, level, authGroup, groupPosition);
					authorizations.add(authName);
				}
			}
		}
		// Remove the authorizations that are not longer useful
		removeAuthorizationsComplement(authorizations);
	}

	/**
	 * remove all authorizations that are not contained within the authorization
	 * list
	 * 
	 * @param authorizations
	 */
	private void removeAuthorizationsComplement(ArrayList<String> authorizations) {
		String validAuthorizations = StringUtils.toStringWithSeparator(authorizations, "'", ",");
		// TypedQuery<String> q = em.createQuery("Select A.name from " +
		// Authorization.class.getName()
		// + " A where A.name not in ( " + validAuthorizations + ") ",
		// String.class);
		// List<String> authorizationsToDelete = q.getResultList();

		// delete the association to roles that are no longer useful
		Query q = em
				.createNativeQuery("delete from core.role_grants_authorization r where r.authorization_name in ( Select name from core.authorizations where name not in ("
						+ validAuthorizations + ") )");
		q.executeUpdate();
		q = em.createNativeQuery("delete from core.authorizations where name not in (" + validAuthorizations + ")");
		q.executeUpdate();
	}

	private void insertAuthorizationIfItDoesntExists(String name, int level, String group, Integer groupPosition) {
		Authorization authorization = em.find(Authorization.class, name);
		if (authorization == null) {
			authorization = new Authorization();
			authorization.setName(name);
			authorization.setLevel(level);

			em.persist(authorization);
			logger.info("inserted authorization " + name + " on level " + level);
		}
		if (group != null) {
			py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup authGroup = getOrCreateAuthGroup(group);
			authGroup.setPosition(groupPosition);
			authorization.setAuthorizationGroup(authGroup);
		}
	}

	private py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup getOrCreateAuthGroup(String name) {
		py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup authGroup = em.find(
				py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup.class, name);
		if (authGroup == null) {
			authGroup = new py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup();
			authGroup.setName(name);
			em.persist(authGroup);
		}
		return authGroup;
	}

}
