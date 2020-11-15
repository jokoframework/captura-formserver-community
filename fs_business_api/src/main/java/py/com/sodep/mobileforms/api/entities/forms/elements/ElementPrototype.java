package py.com.sodep.mobileforms.api.entities.forms.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.forms.common.interfaces.I18nLabels;
import py.com.sodep.mobileforms.api.entities.pools.Pool;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;

/**
 * An Element is where the user actually inputs data or sees data.
 * 
 * An Element is a member of a Page. An element has a mapping to an XML
 * document, it has an XML id, UI style and should be internationalizable.
 * 
 * Examples of concrete elements are text inputs, drop down menus, etc.
 * 
 * An Element generally is represented graphically by a widget (or set of
 * widgets)
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "element_prototypes")
@SequenceGenerator(name = "seq_element_prototypes", sequenceName = "forms.seq_element_prototypes")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ElementPrototype extends SodepEntity implements I18nLabels {

	public enum InstantiabilityType {
		NORMAL, EMBEDDED, TEMPLATE
	}

	private static final long serialVersionUID = 1L;

	private Map<String, String> labels;

	private Boolean required = false;

	private Boolean visible = true;

	private Application application;

	private Integer lockVersion;

	private Pool pool;

	private Long version;

	private ElementPrototype root;

	private InstantiabilityType instantiability = InstantiabilityType.NORMAL;

	private List<ElementInstance> elements;

	private String defaultLanguage;

	// To keep the application consistent with the pool's application
	@Override
	public void prePersist() {
		super.prePersist();
		// This elementPrototype Application should be the same as its pool
		if (getPool() != null) {
			setApplication(getPool().getApplication());
			if (instantiability == InstantiabilityType.EMBEDDED || instantiability == InstantiabilityType.TEMPLATE) {
				throw new RuntimeException("The process item cannot belong to a pool");
			}
		}
	}

	@Override
	public void preUpdate() {
		super.preUpdate();
		if (getPool() != null) {
			setApplication(getPool().getApplication());
			if (instantiability == InstantiabilityType.EMBEDDED || instantiability == InstantiabilityType.TEMPLATE) {
				throw new RuntimeException("The process item cannot belong to a pool");
			}
		}
	}

	@Column(name = "version", nullable = false)
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@ManyToOne
	@JoinColumn(name = "root_id")
	public ElementPrototype getRoot() {
		return root;
	}

	public void setRoot(ElementPrototype root) {
		this.root = root;
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_element_prototypes")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "value", length = 1024)
	@MapKeyColumn(name = "language", length = 16)
	@CollectionTable(schema = "forms", name = "elements_labels", joinColumns = @JoinColumn(name = "element_id"))
	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	@Override
	public String getLabel(String language) {
		String label = labels.get(language);
		if (label == null) {
			return labels.get(defaultLanguage);
		}
		if(label==null){
			//This is just a bit of defensive programming
			//if we reach this point we have already try the label on the desired language
			//Then we tried with the default language and we still have no label
			//There was an error on the CommandModelService that was causing element_prototypes to be saved in a different language than the form. 
			throw new ApplicationException("This is a corrupted form. Report this to the administrator with #3131");
		}
		return label;
	}

	public Boolean getRequired() {
		return this.required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	@Column(nullable = false)
	public Boolean getVisible() {
		return this.visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	@Override
	public void setLabel(String language, String text) {
		if (labels == null) {
			labels = new HashMap<String, String>();
		}
		labels.put(language, text);
	}

	/**
	 * If the ElementPrototype has no pool but has an application it's
	 * associated with the Application as a "PoolLess" ElementPrototype which is
	 * used like a template for creating embedded ElementPrototypes.
	 * 
	 * If the ElementPrototype has no pool and no application it is associated
	 * System wide as a template to create embedded ElementPrototypes.
	 * 
	 * @return
	 */
	@ManyToOne
	@JoinColumn(name = "pool_id")
	public Pool getPool() {
		return pool;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}

	public static String[] allTypeList() {
		List<String> types = new ArrayList<String>();
		String[] allTypes = MFInput.Type.allTypes();
		for (String type : allTypes) {
			types.add(type);
		}

		types.add("location");
		types.add("photo");
		types.add("select");

		// - START Ignoring process items
		// FIXME: This is disabled as requested in ticket: #840
		// FIXME: This is disabled as requested in ticket: #864
		String[] hiddenProcessItems = { /* "location", "photo", */"datetime" };
		for (String name : hiddenProcessItems) {
			types.remove(name);
		}
		// - END

		return types.toArray(new String[0]);
	}

	/**
	 * If the application is null is a PoolLess element prototype that is used a
	 * template system wide
	 * 
	 * @return
	 */
	@ManyToOne
	@JoinColumn(name = "application_id")
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Version
	@Column(name = "lock_version")
	public Integer getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Integer lockVersion) {
		this.lockVersion = lockVersion;
	}

	/**
	 * An embedded prototype doesn't belong to a pool. It's part of the implicit
	 * pool of the Form to which it belongs.
	 * 
	 * @return
	 */
	@Transient
	public Boolean isEmbedded() {
		return instantiability == InstantiabilityType.EMBEDDED;
	}

	/**
	 * A template prototype is not instantiable. It doesn't belong to a pool.
	 * 
	 * It could be a system template (available on all applications) or an
	 * application template.
	 * 
	 * @return
	 */
	@Transient
	public Boolean isTemplate() {
		return instantiability == InstantiabilityType.TEMPLATE;
	}

	@LogicalDelete
	@OneToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.LAZY, mappedBy = "prototype")
	public List<ElementInstance> getElements() {
		return elements;
	}

	public void setElements(List<ElementInstance> elements) {
		this.elements = elements;
	}

	/**
	 * An ElementPrototype has levels of instantiability.
	 * 
	 * An element prototype can be used as a template. In that case, it's
	 * instantiability is said to be TEMPLATE and it should never be referenced
	 * by an ElementInstance directly. It should be used to create other
	 * ElementPrototypes with EMBEDDED instantiability . The new
	 * ElementPrototype should only be referenced by one and only one
	 * ElementInstance.
	 * 
	 * 
	 * @return
	 */
	@Enumerated(EnumType.ORDINAL)
	public InstantiabilityType getInstantiability() {
		return instantiability;
	}

	public void setInstantiability(InstantiabilityType instantiability) {
		this.instantiability = instantiability;
	}

	@Column(name = "default_language")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	@Override
	public ElementPrototype clone() throws CloneNotSupportedException {
		ElementPrototype clone = (ElementPrototype) super.clone();
		clone.id = null;
		clone.lockVersion = 0;
		if (labels != null) {
			clone.labels = new HashMap<String, String>(labels);
		}
		clone.elements = null;
		return clone;
	}
}
