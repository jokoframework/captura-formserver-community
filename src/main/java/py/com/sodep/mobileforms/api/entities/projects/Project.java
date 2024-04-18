package py.com.sodep.mobileforms.api.entities.projects;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import py.com.sodep.mobileforms.api.entities.LogicalDelete;
import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.core.interfaces.IAppAwareEntity;
import py.com.sodep.mobileforms.api.entities.forms.Form;

/**
 * A Project is a container of forms.
 * 
 * A Project might have different labels in different languages that's why it
 * has a List of {@link ProjectDetails}
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "projects", name = "projects")
@SequenceGenerator(name = "seq_projects", sequenceName = "projects.seq_projects")
public class Project extends SodepEntity implements IAppAwareEntity {

	private static final long serialVersionUID = 2L;

	private String defaultLanguage;
	private User owner;
	private List<ProjectDetails> details;
	private List<Form> forms;
	private Application application;
	private Integer lockVersion;

	@Version
	@Column(name = "lock_version")
	public Integer getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Integer lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Project() {
	}

	public void addProjectDetails(ProjectDetails d) {
		if (details == null) {
			details = new ArrayList<ProjectDetails>();
		}
		details.add(d);
	}

	@ManyToOne
	@JoinColumn(name = "application_id", nullable = false)
	public Application getApplication() {
		return application;
	}

	
	// TODO now that we have Application, which has a defaultLanguage
	// is this field really necessary?
	/**
	 * A Project might have different labels in different languages. This should
	 * be set to the default language
	 * 
	 * @return
	 */
	@Column(nullable = false, columnDefinition = "character varying(255) NOT NULL DEFAULT 'en'")
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * A Project has a list of labels (name, description, etc.) by language
	 * 
	 * @return
	 */
	@OneToMany(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "project_id", nullable = false)
	public List<ProjectDetails> getDetails() {
		return details;
	}

	@LogicalDelete
	@OneToMany(mappedBy = "project")
	public List<Form> getForms() {
		return forms;
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_projects")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@ManyToOne
	@JoinColumn(nullable = false, name = "owner_id")
	public User getOwner() {
		return this.owner;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public void setDetails(List<ProjectDetails> details) {
		this.details = details;
	}

	public void setForms(List<Form> forms) {
		this.forms = forms;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

}