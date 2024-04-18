package py.com.sodep.mobileforms.api.entities.application;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.core.Device;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;

/**
 * An application represents a container for projects and users for a particular
 * customer. Information between applications isn't shared in any way and this
 * should be strongly enforced by the authorization framework.
 * 
 * @author Humber
 * 
 */
@Entity
@Table(schema = "applications", name = "applications")
@SequenceGenerator(name = "seq_applications", sequenceName = "applications.seq_applications")
public class Application extends SodepEntity {

	private static final long serialVersionUID = 1L;

	private String name;

	private String defaultLanguage;

	private String defaultTimeZone;
	
	private User owner;

	private List<Project> projects;

	private List<User> appUsers;

	private List<Device> devices;

	private boolean initialSetupReady;

	private Boolean hasWorkflow;

	public Application(String name, String defaultLanguage, User owner, boolean active, boolean hasWorkflow) {
		super();
		this.name = name;
		this.defaultLanguage = defaultLanguage;
		this.owner = owner;
		this.setActive(active);
		this.hasWorkflow = hasWorkflow;
	}

	public Application() {
		super();
	}

	@ManyToMany
	@JoinTable(schema = "applications", name = "application_users", joinColumns = { @JoinColumn(name = "application_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "user_id", nullable = false) })
	public List<User> getAppUsers() {
		return appUsers;
	}

	public void setAppUsers(List<User> appUsers) {
		this.appUsers = appUsers;
	}

	@OneToMany
	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	@Override
	public Application clone() throws CloneNotSupportedException {
		Application clone = (Application) super.clone();
		clone.id = null;
		return clone;
	}

	/**
	 * An Application might have different labels in different languages. This
	 * should be set to the default language
	 * 
	 * @return
	 */
	@Column(nullable = false, columnDefinition = "character varying(255) NOT NULL DEFAULT 'en'")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_applications")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	@ManyToOne
	@JoinColumn(nullable = false, name = "owner_id")
	public User getOwner() {
		return this.owner;
	}

	@LogicalDelete
	@OneToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.LAZY, mappedBy = "application")
	public List<Project> getProjects() {
		return projects;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	@Column(name = "initial_setup_ready")
	public boolean isInitialSetupReady() {
		return initialSetupReady;
	}

	public void setInitialSetupReady(boolean initialSetupReady) {
		this.initialSetupReady = initialSetupReady;
	}

	@Column(name = "default_time_zone")
	public String getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}

	@Column(name = "has_workflow")
	public Boolean getHasWorkflow() {
		return this.hasWorkflow;
	}
	
	public void setHasWorkflow(boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
	}
	
	

}
