package py.com.sodep.mobileforms.api.dtos.sys;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.entities.sys.AcmeLauncher;
import py.com.sodep.mobileforms.api.entities.sys.AcmeTreeMenu;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents an entry on the menu or the toolbox. There are two
 * different types of action that can happen after the user clicks on a given
 * entry: <li>
 * <ul>
 * Open view (actionView=true) will open the ACME view {@link #urlView} and load
 * the js module {@link #jsAMD}
 * </ul>
 * <ul>
 * Execute JS (actionJS=true) will execute a JS function on the context of the
 * user clicks ({@link #jsActionCode} )
 * </ul>
 * </li> This class merge the information from {@link AcmeTreeMenu} and
 * {@link AcmeLauncher} (if any)
 * 
 * @author danicricco
 * 
 */
public class AcmeMenuEntry {

	private Integer menuId;
	private String title;
	private String description;
	private Boolean actionView;
	private Boolean actionJS;
	private String jsAMD;
	private String urlView;
	private String jsActionCode;
	private Boolean toolbox;
	private Integer launcherId;
	private Integer viewId;
	private boolean showMenu;
	private boolean showToolbox;
	private boolean showNavigator;
	private boolean visible;
	private boolean triggerNavigatorLink;
	private String auth;
	
	private List<AcmeMenuEntry> childrens = new ArrayList<AcmeMenuEntry>();

	public AcmeMenuEntry() {

	}

	public AcmeMenuEntry(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.toolbox = false;
	}

	public AcmeMenuEntry(String title, String description, String jsAMD, String urlView) {
		this(title, description);
		this.jsAMD = jsAMD;
		this.urlView = urlView;
		this.actionView = true;
	}

	public AcmeMenuEntry(String title, String description, String jsActionCode) {
		this(title, description);
		this.jsActionCode = jsActionCode;
		this.actionJS = true;
	}

	public Integer getLauncherId() {
		return launcherId;
	}

	public void setLauncherId(Integer launcherId) {
		this.launcherId = launcherId;
	}

	public Integer getMenuId() {
		return menuId;
	}

	public void setMenuId(Integer menuId) {
		this.menuId = menuId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getActionView() {
		return actionView;
	}

	public void setActionView(Boolean actionView) {
		this.actionView = actionView;
	}

	public Boolean getActionJS() {
		return actionJS;
	}

	public void setActionJS(Boolean actionJS) {
		this.actionJS = actionJS;
	}

	public String getJsAMD() {
		return jsAMD;
	}

	public void setJsAMD(String jsAMD) {
		this.jsAMD = jsAMD;
	}

	public String getUrlView() {
		return urlView;
	}

	public void setUrlView(String urlView) {
		this.urlView = urlView;
	}

	public String getJsActionCode() {
		return jsActionCode;
	}

	public void setJsActionCode(String jsActionCode) {
		this.jsActionCode = jsActionCode;
	}

	public void addChild(AcmeMenuEntry child) {
		childrens.add(child);
	}

	public List<AcmeMenuEntry> getChildrens() {
		return childrens;
	}

	public Boolean getToolbox() {
		return toolbox;
	}

	public void setToolbox(Boolean toolbox) {
		this.toolbox = toolbox;
	}

	public Integer getViewId() {
		return viewId;
	}

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public boolean isShowMenu() {
		return showMenu;
	}

	public void setShowMenu(boolean showMenu) {
		this.showMenu = showMenu;
	}

	public boolean isShowToolbox() {
		return showToolbox;
	}

	public void setShowToolbox(boolean showToolbox) {
		this.showToolbox = showToolbox;
	}

	public boolean isShowNavigator() {
		return showNavigator;
	}

	public void setShowNavigator(boolean showNavigator) {
		this.showNavigator = showNavigator;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isTriggerNavigatorLink() {
		return triggerNavigatorLink;
	}

	public void setTriggerNavigatorLink(boolean triggerNavigatorValue) {
		this.triggerNavigatorLink = triggerNavigatorValue;
	}

	@JsonIgnore
	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}
	
	

}
