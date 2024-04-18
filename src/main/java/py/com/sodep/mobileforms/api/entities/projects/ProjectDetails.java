package py.com.sodep.mobileforms.api.entities.projects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

/**
 * Any project information that must be internationalizable should be in this
 * class
 * 
 * @author Miguel
 * @see {@link Project}
 * 
 */
@Entity
@Table(schema = "projects", name = "projects_details", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"project_id", "language", "label" }) })
@SequenceGenerator(name = "seq_projects_details", sequenceName = "projects.seq_projects_details")
public class ProjectDetails extends SodepEntity {

	public static String LABEL = "label";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String description;

	private String label;

	private String language;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_projects_details")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable = false)
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(nullable = false)
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public ProjectDetails clone() throws CloneNotSupportedException {
		ProjectDetails clone = (ProjectDetails) super.clone();
		clone.id = null;
		return clone;
	}

}
