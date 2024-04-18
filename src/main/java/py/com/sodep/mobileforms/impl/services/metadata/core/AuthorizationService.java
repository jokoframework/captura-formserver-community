package py.com.sodep.mobileforms.impl.services.metadata.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.AuthorizationDTO;
import py.com.sodep.mobileforms.api.dtos.AuthorizationGroupDTO;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.AuthorizationGroup;
import py.com.sodep.mobileforms.api.entities.core.Role;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.core.IAuthorizationService;
import py.com.sodep.mobileforms.api.services.metadata.core.IRoleService;

@Service("AuthorizationService")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
public class AuthorizationService implements IAuthorizationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

	@PersistenceContext(unitName = "mobileforms")
	protected EntityManager em;

	@Autowired
	private I18nBundle i18nBundle;

	@Autowired
	private IRoleService roleService;

	private ConcurrentHashMap<String, Authorization> authorizationTable = new ConcurrentHashMap<String, Authorization>();

	@Override
	public void reloadAuthorizations() {

		TypedQuery<Authorization> query = em.createQuery("From " + Authorization.class.getName() + " A ",
				Authorization.class);
		List<Authorization> dbAuthorizations = query.getResultList();
		// check if the authorization needs to be deleted
		Enumeration<String> auths = authorizationTable.keys();
		ArrayList<String> keysToRemove = new ArrayList<String>();
		while (auths.hasMoreElements()) {
			String authName = auths.nextElement();
			Authorization auth = authorizationTable.get(authName);
			if (!dbAuthorizations.contains(auth)) {
				keysToRemove.add(authName);
			}
		}
		// Remove all authorizations that are not longer on the DB
		for (String authName : keysToRemove) {
			authorizationTable.remove(authName);
		}
		// Load all authorizations
		for (Authorization auth : dbAuthorizations) {
			authorizationTable.put(auth.getName(), auth);
		}
		logger.info("Loaded authorization in mememory");
	}

	@Override
	public Authorization get(String authorization) {
		Authorization auth = authorizationTable.get(authorization);
		if (auth == null) {
			throw new IllegalArgumentException("Couldn't find authorization '" + authorization + "'");
		}
		return auth;
	}

	@Override
	public List<AuthorizationGroupDTO> getAuthorizationGroups(Long roleId, String language) {
		Role role = roleService.findById(roleId);
		return getAuthorizationGroups(role.getAuthLevel(), language);
	}

	private List<AuthorizationGroupDTO> getAuthorizationGroups(int level, String language) {
		List<AuthorizationGroupDTO> response = new ArrayList<AuthorizationGroupDTO>();
		List<AuthorizationGroup> authGroupList = em.createQuery("SELECT a FROM AuthorizationGroup a ORDER BY position",
				AuthorizationGroup.class).getResultList();
		for (AuthorizationGroup authGroup : authGroupList) {
			AuthorizationGroupDTO authGroupDTO = new AuthorizationGroupDTO();

			String i18nKey = authGroup.getName() + ".label";
			String authGroupName = i18nBundle.getLabel(language, i18nKey);
			if (authGroupName != null) {
				authGroupDTO.setName(authGroupName);
			} else {
				authGroupDTO.setName(i18nKey);
			}
			for (Authorization auth : authGroup.getAuthorizations()) {
				if (level == Authorization.LEVEL_FORM) {
					// pool level is greater than form but on the virtual tree
					// that we have defined is not. The tree that we defined is
					// App
					// | - Project
					// | --Form
					// |Pool
					if (auth.getLevel() == level && auth.getVisible()) {
						AuthorizationDTO authDTO = AuthorizationHelper.translateAuthorizationToDTO(auth, i18nBundle,
								language);
						authGroupDTO.addAuthorization(authDTO);
					}
				} else {
					if (auth.getLevel() >= level && auth.getVisible()) {
						AuthorizationDTO authDTO = AuthorizationHelper.translateAuthorizationToDTO(auth, i18nBundle,
								language);
						authGroupDTO.addAuthorization(authDTO);
					}
				}

			}
			if (authGroupDTO.getAuthorizations().size() > 0) {
				// only show authorization groups that contain at least one
				// valid authorization
				response.add(authGroupDTO);
			}
		}
		return response;
	}

}
