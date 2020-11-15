package py.com.sodep.mobileforms.impl.services.config;

import py.com.sodep.mobileforms.api.dtos.sys.AcmeMenuEntry;
import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher;
import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher.ACME_LAUNCH_TYPE;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;
import py.com.sodep.mobileforms.api.entities.sys.AcmeView;

/***
 * This is a helper that can transform an {@link AcmeTreeMenu} to the {@link AcmeMenuEntry}
 * @author danicricco
 *
 */
public class AcmeModuleHelper {

	public static AcmeMenuEntry transform(AcmeTreeMenu mod) {
		AcmeMenuEntry menu = new AcmeMenuEntry();
		menu.setDescription(mod.getI18nDescription());
		menu.setTitle(mod.getI18nTitle());
		menu.setMenuId(mod.getId());
		menu.setVisible(mod.getVisible());
		
		AcmeLauncher launcher = mod.getLauncher();
		if (launcher != null) {
			menu.setLauncherId(launcher.getId());

			if (launcher.getLaunchType() != null) {
				if (launcher.getLaunchType().equals(ACME_LAUNCH_TYPE.EXECUTE_JS)) {
					menu.setJsActionCode(launcher.getJsCode());
					menu.setActionJS(true);
					menu.setJsAMD(launcher.getJsAMD());
				} else if (launcher.getLaunchType().equals(ACME_LAUNCH_TYPE.OPEN_VIEW)) {
					AcmeView view = launcher.getAcmeView();
					if(view==null){
						throw new IllegalStateException("The acme_launcher is of type "+ACME_LAUNCH_TYPE.OPEN_VIEW+" but it doesn't have an associated acme_view");
					}
					menu.setJsAMD(view.getJsAMD());
					menu.setUrlView(view.getUrlView());
					menu.setViewId(view.getId());
					menu.setActionView(true);
					menu.setShowMenu((view.getShowMenu()!=null)?view.getShowMenu():true);
					menu.setShowToolbox((view.getShowToolbox()!=null)?view.getShowToolbox():true);
					menu.setShowNavigator((view.getShowNavigator()!=null)?view.getShowNavigator():true);
					menu.setTriggerNavigatorLink((view.getTriggerNavigatorLink()!=null)?view.getTriggerNavigatorLink():true);
					
				} else {
					// this should never happen
					throw new RuntimeException("Unknown ACME_ACTION_TYPE type \"" + launcher.getLaunchType() + "\"");
				}
			} else {
				throw new IllegalStateException("The acme_launcher #" + launcher.getId() + " doesn't have launch_type");
			}
		}

		return menu;
	}

	public static AcmeTreeMenu transform(AcmeMenuEntry entry) {
		AcmeTreeMenu mod = new AcmeTreeMenu();
		mod.setI18nDescription(entry.getDescription());
		mod.setI18nTitle(entry.getTitle());
		mod.setToolbox(entry.getToolbox());
		if (entry.getActionJS() != null && entry.getActionJS() && entry.getActionView() != null
				&& entry.getActionView()) {
			// a bit of defensive programming to test if an AcmeMenuEntry has
			// been declared of two types at the same time
			throw new IllegalStateException(
					"An "
							+ AcmeMenuEntry.class.getName()
							+ " can't have actionJS=true and actionView=true at the same time. It can have only one of them set to true or neither");
		}
		//TODO associate the treeMenu with the launcher
		return mod;
	}
}
