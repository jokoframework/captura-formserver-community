package py.com.sodep.mobileforms.api.dtos;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.hibernate.validator.constraints.Length;

public class FormDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;  //it's root_id of Form Entity

	@Length(min = 2, message = "web.validation.form.label", groups = { Default.class })
	@NotNull
	private String label;

	private String description;

	private Long version;

	//true if actual version is published
	private boolean published;

	private boolean active;

	private Long projectId;

	private String projectName;

	// this field has mean only if published = false, and it holds the version is published if any or null
	private Long versionPublished; 
	
	//List of all version was ever published
	private List<Long> versionsWasPublished;
	
	private boolean workflow;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * The last version that was created of this form
	 * 
	 * @return
	 */
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	/**
	 * The version published or null if any
	 * 
	 * @return
	 */
	public Long getVersionPublished() {
		return versionPublished;
	}

	public void setVersionPublished(Long versionPublished) {
		this.versionPublished = versionPublished;
	}

	public List<Long> getPublishedVersions() {
		return versionsWasPublished;
	}

	public void setVersionsWasPublished(List<Long> versionsWasPublished) {
		this.versionsWasPublished = versionsWasPublished;
	}

	public boolean isWorkflow() {
		return workflow;
	}

	public void setWorkflow(boolean workflow) {
		this.workflow = workflow;
	}
	
}
