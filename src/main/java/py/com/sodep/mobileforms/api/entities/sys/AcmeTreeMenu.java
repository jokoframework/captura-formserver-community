package py.com.sodep.mobileforms.api.entities.sys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(schema = "sys", name = "acme_tree_menu")
@SequenceGenerator(name = "seq_acme_tree_menu", sequenceName = "sys.seq_acme_tree_menu")
public class AcmeTreeMenu {

	private Integer id;
	private String i18nTitle;
	private String i18nDescription;
	private Boolean toolbox;
	private Integer treeLft;
	private Integer treeRgt;
	private Integer position;
	private Integer root;
	private AcmeLauncher launcher;
	private Integer parent;
	private boolean visited;
	private Boolean visible;
	private Boolean active;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_acme_tree_menu")
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "i18n_title")
	public String getI18nTitle() {
		return i18nTitle;
	}

	public void setI18nTitle(String i18nTitle) {
		this.i18nTitle = i18nTitle;
	}

	@Column(name = "i18n_description")
	public String getI18nDescription() {
		return i18nDescription;
	}

	public void setI18nDescription(String i18nDescription) {
		this.i18nDescription = i18nDescription;
	}

	@Column(name = "toolbox")
	public Boolean getToolbox() {
		return toolbox;
	}

	public void setToolbox(Boolean toolbox) {
		this.toolbox = toolbox;
	}

	@Column(name = "tree_lft")
	public Integer getTreeLft() {
		return treeLft;
	}

	public void setTreeLft(Integer treeLft) {
		this.treeLft = treeLft;
	}

	@Column(name = "tree_rgt")
	public Integer getTreeRgt() {
		return treeRgt;
	}

	public void setTreeRgt(Integer treeRgt) {
		this.treeRgt = treeRgt;
	}

	@Column(name = "position")
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	@Column(name = "root")
	public Integer getRoot() {
		return root;
	}

	public void setRoot(Integer root) {
		this.root = root;
	}

	@ManyToOne
	@JoinColumn(name = "launcher_id")
	public AcmeLauncher getLauncher() {
		return launcher;
	}

	public void setLauncher(AcmeLauncher launcher) {
		this.launcher = launcher;
	}

	// TODO
	/**
	 * What is visited used for?
	 * 
	 * @return
	 */
	@Transient
	public Boolean isVisited() {
		return visited;
	}

	public void setVisited(Boolean visited) {
		this.visited = visited;
	}

	@Column(name = "parent_id")
	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	// TODO
	/**
	 * If an AcmeTreeMenu is visible, it means that it should be included in the
	 * computed tree but not shown on the menu.
	 * 
	 * The breadcrumbs are displayed based on the computed tree. So, for
	 * example, the WF editor may be active but not visible (visible = false),
	 * which means it shouldn't be shown on any menu
	 * 
	 * @return
	 */
	@Column(name = "visible")
	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	// TODO danicricco
	/**
	 * If an AcmeTreeMenu is not active (active = false) it is not included in
	 * the computed tree of the menu
	 * 
	 * @return
	 */
	@Column(name = "active")
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
