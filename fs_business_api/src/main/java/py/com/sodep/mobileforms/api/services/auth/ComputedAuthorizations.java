package py.com.sodep.mobileforms.api.services.auth;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is a thread safe class that serve as a cache for the user access rights.
 * 
 * @author danicricco
 * 
 */
public class ComputedAuthorizations {

	private final CopyOnWriteArrayList<ConcurrentHashMap<Long, Set<String>>> accessRights;
	private final int numberOfLevels;

	public ComputedAuthorizationDTO toDTO() {

		ArrayList<HashMap<Long, Set<String>>> clonedAuth = new ArrayList<HashMap<Long, Set<String>>>();
		for (int i = 0; i < accessRights.size(); i++) {
			ConcurrentHashMap<Long, Set<String>> authOnLevel = accessRights.get(i);
			// clone the map of each level
			HashMap<Long, Set<String>> clonedMap = new HashMap<Long, Set<String>>();
			clonedMap.putAll(authOnLevel);
			clonedAuth.add(i, clonedMap);
		}
		ComputedAuthorizationDTO dto = new ComputedAuthorizationDTO();
		dto.setAccessRights(clonedAuth);
		return dto;
	}

	public ComputedAuthorizations(int numberOfLevels) {
		this.accessRights = new CopyOnWriteArrayList<ConcurrentHashMap<Long, Set<String>>>();
		this.numberOfLevels = numberOfLevels;
		for (int i = 0; i < this.numberOfLevels; i++) {
			// initialize a map for each available level
			this.accessRights.add(new ConcurrentHashMap<Long, Set<String>>());
		}
	}

	public void addAuthorization(int level, Long objectId, String authorization) {
		ConcurrentHashMap<Long, Set<String>> levelMap = getLevelMap(level);
		Set<String> objectAccessRights = new CopyOnWriteArraySet<String>();
		Set<String> objectAccessQuantic = levelMap.putIfAbsent(objectId, objectAccessRights);
		if(objectAccessQuantic != null) {
			objectAccessRights = objectAccessQuantic;
		}
		objectAccessRights.add(authorization);
	}

	/**
	 * This method checks if the user has the required authorization at a given
	 * level.
	 * 
	 * @param level
	 * @param objectId
	 * @param auth
	 * @return
	 */
	public boolean hasAccess(int level, Long objectId, String auth) {
		ConcurrentHashMap<Long, Set<String>> levelMap = getLevelMap(level);
		Set<String> objectAccessRights = levelMap.get(objectId);
		if (objectAccessRights != null) {
			return objectAccessRights.contains(auth);
		}
		return false;
	}

	private ConcurrentHashMap<Long, Set<String>> getLevelMap(int level) {
		ConcurrentHashMap<Long, Set<String>> levelMap = this.accessRights.get(level);
		if (levelMap == null) {
			throw new IllegalArgumentException("The access right level #" + level + " was not correctly initialized ");
		}
		return levelMap;
	}

	/**
	 * Build a list of all objects granted with the permission auth in the given
	 * level
	 * 
	 * @param level
	 * @param auth
	 * @return
	 */
	public Set<Long> getGrantedObjects(int level, String auth) {
		TreeSet<Long> grantedObjs = new TreeSet<Long>();
		ConcurrentHashMap<Long, Set<String>> levelMap = getLevelMap(level);
		Enumeration<Long> objList = levelMap.keys();
		while (objList.hasMoreElements()) {
			Long obj = objList.nextElement();
			Set<String> grants = levelMap.get(obj);
			if (grants.contains(auth)) {
				grantedObjs.add(obj);
			}
		}
		return grantedObjs;
	}
}
