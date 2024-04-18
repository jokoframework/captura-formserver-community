package py.com.sodep.mobileforms.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import py.com.sodep.mobileforms.api.dtos.sys.AcmeMenuEntry;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;
import py.com.sodep.mobileforms.api.services.config.IAcmeModuleService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.session.SessionManager;

/**
 * This is a controller that can be used to query the menu, and the toolbox
 * associated to a given view.
 * 
 * @author danicricco
 * 
 */
@Controller
public class MenuController extends SodepController {

	@Autowired
	IAcmeModuleService acmeService;

	/**
	 * Load the options for the main menu. The menu that is on the top of the
	 * page
	 * 
	 * @param request
	 * @param pageId
	 * @return
	 */
	@RequestMapping(value = "/sys/menu.ajax")
	@ResponseBody
	public List<AcmeMenuEntry> loadMenu(HttpServletRequest request) {
		SessionManager sessionManager = new SessionManager(request);
		User user = sessionManager.getUser();
		I18nManager i18n = sessionManager.getI18nManager();
		Application app = sessionManager.getApplication();
		ArrayList<AcmeMenuEntry> menuList = new ArrayList<AcmeMenuEntry>();
		List<AcmeTreeMenu> rootModules = acmeService.getRootMenus(user);
		for (AcmeTreeMenu m : rootModules) {
			AcmeMenuEntry menu = acmeService.getSubTree(app, user, m.getId());
			if (menu != null) {
				translateLabels(menu, i18n);
				menuList.add(menu);
			}
		}
		return menuList;
	}

	/**
	 * Load the toolbox for a given view
	 * 
	 * @param request
	 * @param viewId
	 * @return
	 */
	@RequestMapping(value = "/sys/toolbox.ajax")
	@ResponseBody
	public AcmeMenuEntry listToolboxForView(HttpServletRequest request,
			@RequestParam(value = "viewId", required = false) Integer viewId) {
		SessionManager sessionManager = new SessionManager(request);
		I18nManager i18n = sessionManager.getI18nManager();
		Application app = sessionManager.getApplication();
		User user = sessionManager.getUser();
		if (viewId != null) {
			AcmeMenuEntry toolbox = acmeService.getToolbox(app, user, viewId);
			if (toolbox != null) {
				translateLabels(toolbox, i18n);
			}
			return toolbox;
		}
		return null;

	}

	/**
	 * This method will traverse the tree and translate the title and
	 * descriptions with the right i18n value
	 * 
	 * @param menu
	 * @param i18n
	 */
	private void translateLabels(AcmeMenuEntry menu, I18nManager i18n) {
		String i18nDescription = menu.getDescription();
		if (i18nDescription != null) {
			menu.setDescription(i18n.getMessage(i18nDescription));
		}
		String i18nTitle = menu.getTitle();
		if (i18nTitle != null) {
			menu.setTitle(i18n.getMessage(i18nTitle));
		}
		List<AcmeMenuEntry> childrens = menu.getChildrens();
		for (AcmeMenuEntry child : childrens) {
			translateLabels(child, i18n);
		}

	}

}
