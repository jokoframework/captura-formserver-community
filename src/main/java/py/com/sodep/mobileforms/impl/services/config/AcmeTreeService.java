package py.com.sodep.mobileforms.impl.services.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.sys.AcmeMenuEntry;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;

/**
 * This is a service that handles the tree function associated with the menu.
 * 
 * @see AcmeModuleImpl
 * @author danicricco
 * 
 */
@Service("sys.AcmeTreeService")
@Transactional
class AcmeTreeService {

	private static Logger logger = LoggerFactory.getLogger(AcmeTreeService.class);
	
	@Autowired
	IAuthorizationControlService authorizationControlService;

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	public AcmeMenuEntry addTree(AcmeMenuEntry menu) {
		AcmeTreeMenu mod = AcmeModuleHelper.transform(menu);
		em.persist(mod);
		storeChildrens(mod.getId(), menu.getChildrens());
		assignPreOrderValue(mod);
		return AcmeModuleHelper.transform(mod);
	}

	private void storeChildrens(Integer parent, List<AcmeMenuEntry> entries) {
		for (AcmeMenuEntry entry : entries) {
			AcmeTreeMenu mod = AcmeModuleHelper.transform(entry);
			mod.setParent(parent);
			em.persist(mod);
			storeChildrens(mod.getId(), entry.getChildrens());
		}
	}

	//TODO review
	public AcmeMenuEntry getSubTree(Application app,User u, Integer id) {
		AcmeTreeMenu root = em.find(AcmeTreeMenu.class, id);
		if (root == null) {
			throw new IllegalArgumentException("The module #" + id + " doesn't exists");
		}
		// it's OK to cut here
		AcmeLauncher rootLauncher = root.getLauncher();
		Authorization rootLauncherAuth = rootLauncher != null ? rootLauncher.getAuthorization() : null;
		if (root.getLauncher() != null && rootLauncherAuth != null) {
			if(!authorizationControlService.has(app, u, rootLauncherAuth.getName())) {
				return null;
			}
		}
		List<AcmeTreeMenu> plainTree = getPlainSubTree(u, id);
		Stack<AcmeTreeMenu> stack = new Stack<AcmeTreeMenu>();
		
		HashMap<Integer, AcmeMenuEntry> modToMenu = new HashMap<Integer, AcmeMenuEntry>();
		AcmeMenuEntry rootEntry = AcmeModuleHelper.transform(root);
		modToMenu.put(root.getId(), rootEntry);
		AcmeTreeMenu parent = root;
		
		for (AcmeTreeMenu descendant : plainTree) {
// IT'S NOT OK TO CUT HERE, JUST LIKE THIS - jmpr			
//			if (descendant.getLauncher() != null && descendant.getLauncher().getAuthorization() != null) {
//				if (!authorizationControlService.has(app, u, descendant.getLauncher().getAuthorization().getName())) {
//					continue;
//				}
//			}
			AcmeMenuEntry descendantEntry = AcmeModuleHelper.transform(descendant);
			modToMenu.put(descendant.getId(), descendantEntry);
			if (descendant.getTreeLft() < parent.getTreeLft() || descendant.getTreeRgt() > parent.getTreeRgt()) {
				// we are going down the tree unless the descendant is not
				// actually a descendant of the parent
				// Then we need to look on the stack the parent of the node
				while ((stack.peek()).getTreeRgt() < descendant.getTreeRgt()) {
					stack.pop();
				}
				parent = stack.pop();
			}
			Authorization auth = descendant.getLauncher() == null ? null : descendant.getLauncher().getAuthorization();
			if (auth != null) {
				// to later prune the tree
				descendantEntry.setAuth(auth.getName());
			}
			AcmeMenuEntry parentEntry = modToMenu.get(parent.getId());
			parentEntry.addChild(descendantEntry);
			stack.push(parent);
			parent = descendant;

		}
		// I don't like this but haven't found a nice way to modify the above
		// algorithm, so I prefer to leave it this way, until a I come up with
		// an elegant / more efficient way. I do want to respect correctness!
		pruneTree(app, u, rootEntry);
		return rootEntry;
	}
	
	private void pruneTree(Application app, User u, AcmeMenuEntry rootEntry) {
		Iterator<AcmeMenuEntry> iter = rootEntry.getChildrens().iterator();
		while (iter.hasNext()) {
			AcmeMenuEntry child = iter.next();
			String auth = child.getAuth();
			if (auth == null ||authorizationControlService.has(app, u, auth) ) {
				pruneTree(app, u, child);
			} else {
				iter.remove();
			}
		}
	}

	public List<AcmeTreeMenu> getPlainSubTree(User u, Integer id) {
		AcmeTreeMenu mod = em.find(AcmeTreeMenu.class, id);
		if (mod == null) {
			throw new IllegalArgumentException("The module #" + id + " doesn't exists");
		}
		if (mod.getTreeLft() == null || mod.getTreeRgt() == null) {
			throw new IllegalStateException("The module #" + id
					+ " doesn't contain the tree values (tree_lft, tree_right)");
		}
		TypedQuery<AcmeTreeMenu> query = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
				+ " A where A.treeLft>:lft and A.treeRgt<:rgt and A.root=:root and A.active=true order by A.treeLft ", AcmeTreeMenu.class);
		query.setParameter("lft", mod.getTreeLft());
		query.setParameter("rgt", mod.getTreeRgt());
		// the table AcmeModule has several tree, therefore it is very important
		// to obtain only the nodes on the same tree
		query.setParameter("root", mod.getRoot());
		return query.getResultList();
	}

	public List<AcmeTreeMenu> getRootMenus(User u) {
		TypedQuery<AcmeTreeMenu> query = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
				+ " A where A.treeLft=1 and A.toolbox=false order by A.position", AcmeTreeMenu.class);
		return query.getResultList();
	}

	/**
	 * This method will traverse the tree in preorder and assign lft and rgt
	 * value to each node.
	 * 
	 * @param m
	 */
	private void assignPreOrderValue(AcmeTreeMenu m) {
		logger.debug("Traversing the tree #" + m.getId() + "(" + m.getI18nTitle() + ") in pre-order ");
		Stack<AcmeTreeMenu> nodes = new Stack<AcmeTreeMenu>();
		int order = 1;
		int root = m.getId();
		m.setVisited(false);
		nodes.push(m);
		while (!nodes.isEmpty()) {
			AcmeTreeMenu node = nodes.pop();

			TypedQuery<AcmeTreeMenu> queryChilds = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
					+ " A where A.parent=:parentId order by A.position desc", AcmeTreeMenu.class);
			queryChilds.setParameter("parentId", node.getId());
			List<AcmeTreeMenu> childrens = queryChilds.getResultList();

			if (node.isVisited()) {
				// ascending
				node.setTreeRgt(order);
				order++;
			} else {
				node.setRoot(root);
				// descending
				node.setTreeLft(order);
				order++;
				// push the node in order to assign its right side when we
				// are ascending through the tree
				node.setVisited(true);
				nodes.push(node);
				for (AcmeTreeMenu child : childrens) {
					nodes.push(child);
				}
			}

		}
	}

	/**
	 * This method searches for the nodes that points to the launcherId and
	 * returns the menu path reach it. The method assumes that there is only one
	 * menu (i.e. toolbox=false) pointing to the given launcher, there aren't
	 * two paths for the same launcher. Note that "menu" is used in this context
	 * as an entry on AcmeTreeMenu with toolbox=false. There can be several
	 * AcmeTreeMenu toolbox=true pointing to the same launcher.
	 * 
	 * @param launcherId
	 * @return
	 */
	@Deprecated
	// I (danicricco) have designed and implemented this method as a support for
	// the "navigator. Later I realized that I could implement the navigator in
	// JS by using the information already available at the client. I haven't
	// tested this method, but I consider that it might be useful in the future. Therefore, I designed to leave this method as "deprecated" until someone check if this is working and finds a good way to use it
	private List<AcmeMenuEntry> getPathToLauncherInMenu(Integer launcherId) {
		Query q = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
				+ " A where A.toolbox=false and A.launcher.id=:launcherId");
		q.setParameter("launcherId", launcherId);
		List<AcmeTreeMenu> menus = q.getResultList();
		if (menus.size() != 1) {
			AcmeTreeMenu childMenu = menus.get(0);
			q = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
					+ " A where A.treeLft < :lft and A.treeRgt >: rgt ");
			List<AcmeTreeMenu> path = q.getResultList();
			ArrayList<AcmeMenuEntry> pathMenu = new ArrayList<AcmeMenuEntry>();
			for (AcmeTreeMenu node : path) {
				AcmeMenuEntry menu = AcmeModuleHelper.transform(node);
				pathMenu.add(menu);
			}
			return pathMenu;
		} else {
			// returning a null value can't actually happen, because the method
			// will always throw an exeption
			reportConfigurationError(launcherId, menus);
			return null;
		}

	}

	/***
	 * If there is a configuration error on the menu this method will throw an
	 * exception with a helpful message to fix the problem.
	 * 
	 * @param launcherId
	 * @param menus
	 */
	@Deprecated
	//see the deprecation comment that I (danicricco) wrote for the method getPathToLauncherInMenu
	private void reportConfigurationError(Integer launcherId, List<AcmeTreeMenu> menus) {
		if (menus.size() > 1) {
			// make a report of the wrong menus to include it on the exception
			// message, so the developer can easily fix the problem
			StringBuffer buff = new StringBuffer();
			int maxReportOfWrongIds = 10;
			for (int i = 0; i < maxReportOfWrongIds && i < menus.size(); i++) {
				AcmeTreeMenu menu = menus.get(i);
				buff.append(menu.getId() + ",");
			}
			String some = "";
			if (maxReportOfWrongIds < menus.size()) {
				// if there are more than maxReportOfWrongIds menus add a "hint"
				// in the exception message
				some = "Some of the";
			}
			throw new IllegalStateException("There are several menus pointing to the launcher #" + launcherId + "."
					+ some + " Wrong Menu IDs={" + buff.toString() + "}");
		} else {
			throw new IllegalStateException("There are no menu pointing to the luancher #" + launcherId);
		}
	}

	public void assignPreOrderValues() {
		logger.info("Traversing the menu in pre-order to assign lft,rgt values");
		TypedQuery<AcmeTreeMenu> queryRoots = em.createQuery("From " + AcmeTreeMenu.class.getCanonicalName()
				+ " A where A.parent is null order by A.position desc", AcmeTreeMenu.class);

		List<AcmeTreeMenu> list = queryRoots.getResultList();

		for (AcmeTreeMenu m : list) {
			assignPreOrderValue(m);
		}
		logger.info("Successfully assigned lft,rgt values to " + list.size() + " different menu trees.");
	}
	/**
	 * This method will check that that are not two active menus pointing to the same launcher. The method will return the list of launcher Ids that have more than one entry on the menu
	 * @return
	 */
	public List<Integer> checkTreeConsistency(){
		Query q = em.createNativeQuery("select launcher_id from sys.acme_tree_menu "+ 
		"where toolbox=false " +
		"and launcher_id is not null " +
		"and active=true "+
		"group by launcher_id "+
		"having count(1)>1 ");
		
		List data = q.getResultList();
		return data;
		
	}
}
