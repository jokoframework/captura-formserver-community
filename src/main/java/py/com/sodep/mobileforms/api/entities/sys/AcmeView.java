package py.com.sodep.mobileforms.api.entities.sys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema = "sys", name = "acme_views")
@SequenceGenerator(name = "seq_acme_views", sequenceName = "sys.seq_acme_views")
public class AcmeView {
	private Integer id;
	private String jsAMD;
	private String urlView;
	private Integer toolBooxRootId;
	private Boolean showMenu;
	private Boolean showToolbox;
	private Boolean showNavigator;
	private Boolean triggerNavigatorLink;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_acme_views")
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "js_amd")
	public String getJsAMD() {
		return jsAMD;
	}

	public void setJsAMD(String jsAMD) {
		this.jsAMD = jsAMD;
	}

	@Column(name = "url_view")
	public String getUrlView() {
		return urlView;
	}

	public void setUrlView(String urlView) {
		this.urlView = urlView;
	}

	@Column(name = "toolbox_root")
	public Integer getToolBooxRootId() {
		return toolBooxRootId;
	}

	public void setToolBooxRootId(Integer toolBooxRootId) {
		this.toolBooxRootId = toolBooxRootId;
	}

	@Column(name = "show_menu")
	public Boolean getShowMenu() {
		return showMenu;
	}

	public void setShowMenu(Boolean showMenu) {
		this.showMenu = showMenu;
	}

	@Column(name = "show_toolbox")
	public Boolean getShowToolbox() {
		return showToolbox;
	}

	public void setShowToolbox(Boolean showToolbox) {
		this.showToolbox = showToolbox;
	}

	@Column(name = "show_navigator")
	public Boolean getShowNavigator() {
		return showNavigator;
	}

	public void setShowNavigator(Boolean showNavigator) {
		this.showNavigator = showNavigator;
	}

	@Column(name = "trigger_navigator_link")
	public Boolean getTriggerNavigatorLink() {
		return triggerNavigatorLink;
	}

	public void setTriggerNavigatorLink(Boolean triggerNavigatorLink) {
		this.triggerNavigatorLink = triggerNavigatorLink;
	}

}
