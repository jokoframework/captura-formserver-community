package py.com.sodep.mobileforms.api.entities.forms.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.I18nLabels;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.IInstanceId;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;

/**
 * A Page is a container of elements. An Element is where the user actually
 * inputs data (or sees data)
 * 
 * A Page also may have a Flow. This is, the possibility of navigation between
 * pages.
 * 
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "pages")
@SequenceGenerator(name = "seq_pages", sequenceName = "forms.seq_pages")
public class Page extends SodepEntity implements IInstanceId, I18nLabels {

	private static final long serialVersionUID = 1L;

	private Map<String, String> labels;

	private String onLoad;

	private String onUnload;

	private Integer position;

	private Boolean save = false;

	private List<ElementInstance> elements;

	private Flow flow;

	private Form form;

	private String instanceId;

	private String defaultLanguage;

	public Page() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_pages")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@Override
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "value", length = 1024)
	@MapKeyColumn(name = "language", length = 16)
	@CollectionTable(schema = "forms", name = "pages_labels", joinColumns = @JoinColumn(name = "page_id"))
	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	@Override
	public String getLabel(String language) {
		if (language == null || labels.get(language) != null) {
			return labels.get(language);
		} else {
			return labels.get(defaultLanguage);
		}

	}

	/**
	 * Action/Script to execute when this page is loaded
	 * 
	 * @return
	 */
	@Column(name = "on_load")
	public String getOnLoad() {
		return this.onLoad;
	}

	public void setOnLoad(String onLoad) {
		this.onLoad = onLoad;
	}

	/**
	 * Action/Script to execute when navigating out of this page
	 * 
	 * @return
	 */
	@Column(name = "on_unload")
	public String getOnUnload() {
		return this.onUnload;
	}

	public void setOnUnload(String onUnload) {
		this.onUnload = onUnload;
	}

	@Override
	@Column(name = "instance_id")
	public String getInstanceId() {
		if (instanceId != null) {
			return instanceId;
		}
		if (id != null) {
			return "page" + id;
		}
		return null;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * This will be used for generating the XML. The pages in a Form will be
	 * ordered by postion.
	 * 
	 * @return
	 */
	@Column(nullable = false)
	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	/**
	 * Should the save option be available on this Page?
	 * 
	 * @return show the save option
	 */
	@Column(nullable = false)
	public Boolean getSave() {
		return this.save;
	}

	public void setSave(Boolean save) {
		this.save = save;
	}

	@Override
	public void setLabel(String language, String text) {
		if (labels == null)
			labels = new HashMap<String, String>();
		labels.put(language, text);
	}

	@LogicalDelete
	@OneToMany(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "page_id")
	@OrderBy("position")
	public List<ElementInstance> getElements() {
		return this.elements;
	}

	// FIXME un ElementInstance puede estar en varios pages
	public void setElements(List<ElementInstance> elements) {
		this.elements = elements;
	}

	@OneToOne(optional = true, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "flow_id", unique = true, nullable = true)
	public Flow getFlow() {
		return this.flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	@Column(name = "default_language")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	@Override
	public Page clone() throws CloneNotSupportedException {
		Page clone = (Page) super.clone();
		clone.id = null;
		if (labels != null)
			clone.labels = new HashMap<String, String>(labels);

		if (elements != null) {
			clone.elements = new ArrayList<ElementInstance>();
			for (ElementInstance e : elements) {
				ElementInstance instance = e.clone();
				clone.elements.add(instance);
			}
		}
		if (flow != null) {
			clone.flow = flow.clone();
		}

		return clone;
	}
}