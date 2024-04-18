package py.com.sodep.mobileforms.impl.services.config;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mobileforms.api.dtos.sys.AcmeMenuEntry;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;
import py.com.sodep.mobileforms.api.entities.sys.AcmeView;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.config.IAcmeModuleService;

@Service("sys.AcmeModule")
@Transactional
@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
// TODO need to check the authorizations of the GUI
public class AcmeModuleImpl implements IAcmeModuleService {

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Autowired
	private AcmeTreeService treeMenuService;

	@Override
	public List<AcmeLauncher> listAcmeModules() {
		TypedQuery<AcmeLauncher> query = em.createQuery("From " + AcmeLauncher.class.getCanonicalName() + "",
				AcmeLauncher.class);
		return query.getResultList();
	}

	@Override
	public AcmeMenuEntry getToolbox(Application app, User u, Integer viewId) {
		AcmeView view = em.find(AcmeView.class, viewId);
		if (view == null) {
			throw new IllegalStateException("The view #" + viewId + " doesn't exists");
		}
		if (view.getToolBooxRootId() != null) {
			return treeMenuService.getSubTree(app, u, view.getToolBooxRootId());
		}
		return null;
	}

	// METHODS THAT ARE GOING TO BE FORWARDED TO AcmeTreeService

	@Override
	public AcmeMenuEntry addTree(AcmeMenuEntry menu) {
		return treeMenuService.addTree(menu);
	}

	public AcmeMenuEntry getSubTree(Application app, User u, Integer id) {
		return treeMenuService.getSubTree(app, u, id);
	}

	public List<AcmeTreeMenu> getPlainSubTree(User u, Integer id) {
		return treeMenuService.getPlainSubTree(u, id);
	}

	public List<AcmeTreeMenu> getRootMenus(User u) {
		return treeMenuService.getRootMenus(u);
	}

	@Override
	public void assignPreOrderValues() {
		treeMenuService.assignPreOrderValues();
	}

	@Override
	public void checkTreeConsistency() {
		List<Integer> treeConsistency = treeMenuService.checkTreeConsistency();

		if (treeConsistency.size() > 0) {
			StringBuffer buff = new StringBuffer();
			buff.append("There are more than one menu entry pointing to the launchers: ");
			for (Integer launcherId : treeConsistency) {
				buff.append(launcherId + ",");
			}
			String msg = buff.toString();
			// remove the last ","
			throw new IllegalStateException(msg.substring(0, msg.length() - 1));

		}
	}
}
