package py.com.sodep.mobileforms.api.entities.forms;

import java.sql.Timestamp;
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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.I18nLabels;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.IInstanceId;
import py.com.sodep.mobileforms.api.entities.forms.page.Page;
import py.com.sodep.mobileforms.api.entities.projects.Project;

/**
 * This entity representes a Form. A form is a collection of Pages. A page is a
 * collection of elements and navigation instructions.
 * 
 * A Form entity has a mapping to an XML tag. A form has a style that defines
 * how it looks. A form and its components should be internationalizable.
 * 
 * A Form can have different versions that are distinguish by the version field.
 * The different versions of the forms share common ancestor on the "root"
 * field.
 * 
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "forms")
@SequenceGenerator(name = "seq_forms", sequenceName = "forms.seq_forms")
public class Form extends SodepEntity implements IInstanceId, I18nLabels {

	private static final long serialVersionUID = 2L;

	private Project project;

	private Map<String, String> labels;

	private List<Page> pages;

	private String defaultLanguage;

	private List<String> languages;

	// Name of the collection where data is saved
	private String dataSetDefinition;

	private Long datasetVersion;

	private Boolean published = false;

	// if it's version was ever published to be shown in the reports
	private Boolean wasPublished = false;

	private Timestamp publishedDate;

	private Form root;

	private Long version = 1l;

	private Integer lockVersion;

	private Boolean acceptData = true;

	private Boolean provideLocation = false;

	// This is a fk to the register of the form that was published on this form
	// hierarchy.
	// if it's not published it's draft and still it's not visible to mobile
	// users in any version.
	private Long formPublished;

	@Version
	@Column(name = "lock_version")
	public Integer getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Integer lockVersion) {
		this.lockVersion = lockVersion;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_forms")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@Override
	@Transient
	public String getInstanceId() {
		if (id == null) {
			return null;
		}
		return "form" + id;
	}

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "value", length = 1024)
	@MapKeyColumn(name = "language", length = 16)
	@CollectionTable(schema = "forms", name = "forms_labels", joinColumns = @JoinColumn(name = "form_id"))
	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	@Override
	/**
	 * This method assumes that the form was loaded with eager labels
	 * @return If the label in the desire language is not null will return it. Otherwise, will return the label in the default language
	 * 
	 */
	public String getLabel(String language) {
		if (language == null || root.labels.get(language) == null) {
			return root.labels.get(getDefaultLanguage());
		} else {
			return root.labels.get(language);
		}

	}

	@LogicalDelete
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "form")
	@OrderBy("position")
	public List<Page> getPages() {
		return this.pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	@Column(nullable = false)
	public Boolean getPublished() {
		return published;
	}

	public void setPublished(Boolean published) {
		this.published = published;
	}

	@Column(name = "was_published")
	public Boolean getWasPublished() {
		return wasPublished;
	}

	public void setWasPublished(Boolean wasPublished) {
		this.wasPublished = wasPublished;
	}

	@Column(name = "published_date")
	public Timestamp getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Timestamp publishedDate) {
		this.publishedDate = publishedDate;
	}

	/**
	 * If set to false, any incoming data for this form should be discarded
	 * 
	 * @return accept new data or not
	 */
	@Column(nullable = false)
	public Boolean getAcceptData() {
		return acceptData;
	}

	public void setAcceptData(Boolean acceptData) {
		this.acceptData = acceptData;
	}

	@Column(nullable = false, length = 16, name = "default_language")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	@Override
	public void setLabel(String language, String text) {
		if (labels == null)
			labels = new HashMap<String, String>();
		labels.put(language, text);
	}

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(schema = "forms", name = "forms_languages", joinColumns = @JoinColumn(name = "form_id"))
	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	// @LogicalDelete
	@ManyToOne
	@JoinColumn(name = "root_id")
	public Form getRoot() {
		return root;
	}

	public void setRoot(Form root) {
		this.root = root;
	}

	@Column(nullable = false, columnDefinition = "bigint DEFAULT 1")
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * The name of the collection where data of this Form is persisted
	 * 
	 * @return Form's collection name
	 */
	@Column(name = "dataset_definition")
	public String getDataSetDefinition() {
		return dataSetDefinition;
	}

	public void setDataSetDefinition(String definition) {
		this.dataSetDefinition = definition;
	}

	@Column(name = "dataset_version")
	public Long getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(Long datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	public long incrementVersion() {
		return ++version;
	}

	@Column(name = "provide_location")
	public Boolean getProvideLocation() {
		return provideLocation;
	}

	public void setProvideLocation(Boolean provideLocation) {
		this.provideLocation = provideLocation;
	}

	/**
	 * This is a fk to the register of the form that was published on this form
	 * hierarchy
	 * 
	 * @return
	 */
	@Column(name = "published_version")
	public Long getFormPublished() {
		return formPublished;
	}

	public void setFormPublished(Long formPublished) {
		this.formPublished = formPublished;
	}
	
	public void addPage(Page page){
		this.pages.add(page);
	}

	@Override
	public Form clone() throws CloneNotSupportedException {
		Form clone = (Form) super.clone();
		clone.id = null;
		clone.published = false;
		clone.wasPublished = false;
		clone.publishedDate = null;
		clone.lockVersion = 0;
		if (labels != null) {
			clone.labels = new HashMap<String, String>(labels);
		}
		if (languages != null) {
			clone.languages = new ArrayList<String>(languages);
		}
		if (pages != null) {
			clone.pages = new ArrayList<Page>();
			for (Page p : pages) {
				Page page = p.clone();
				page.setForm(clone);
				clone.pages.add(page);
			}
		}
		return clone;
	}
}