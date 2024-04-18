package py.com.sodep.mobileforms.api.services.config;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.sys.AcmeMenuEntry;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;

public interface IAcmeModuleService {

	public List<AcmeLauncher> listAcmeModules();

	/**
	 * Obtain the sub modules based on a given module id
	 * 
	 * @param u
	 * @param id
	 * @return
	 */
	public AcmeMenuEntry getSubTree(Application app,User u, Integer id);

	public List<AcmeTreeMenu> getRootMenus(User u);

	public void assignPreOrderValues();

	public AcmeMenuEntry addTree(AcmeMenuEntry menu);

	public List<AcmeTreeMenu> getPlainSubTree(User u, Integer id);

	/**
	 * Obtain the toolbox root and its children of a given view
	 * 
	 * @param u
	 * @param viewId
	 * @return
	 */
	public AcmeMenuEntry getToolbox(Application app,User u, Integer viewId);

	/**
	 * There can't be two entries on the menu pointing to the same launcher, otherwise the
	 * navigator can't be built in a predictable manner. This method will check this condition and throw an {@link IllegalStateException} if the condition is not met
	 */
	public void checkTreeConsistency();

}
