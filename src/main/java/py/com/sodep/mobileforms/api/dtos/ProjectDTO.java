package py.com.sodep.mobileforms.api.dtos;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import py.com.sodep.mobileforms.api.constants.AuthorizationNames.Project;

/**
 * This class is simplified version of the entity {@link Project} that has the
 * label and description set on a language. In addition, it hides some fields of
 * Project that are risky to expose outside the system
 * 
 * @author danicricco
 * 
 */
public class ProjectDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String ID = "id";

	public static final String LABEL = "label";

	public static final String DESCRIPTION = "description";

	public static final String CREATED = "created";

	public static final String OWNER_MAIL = "ownerMail";

	public static final String LANGUAGE = "language";

	private Long id;

	private Boolean active = true;

	@NotNull
	private String language;

	@NotNull
	private Long ownerId;

	private String ownerMail;

	@NotNull
	private String description;

	@NotNull
	private String label;

	private Timestamp created;

	private List<FormDTO> forms;

	private Long applicationId;

	public ProjectDTO(Long id, Boolean active, String language, Long ownerId, String ownerMail, String description,
			String label, Timestamp created, Long applicationId) {
		super();
		this.id = id;
		this.active = active;
		this.language = language;
		this.ownerId = ownerId;
		this.ownerMail = ownerMail;
		this.description = description;
		this.label = label;
		this.created = created;
		this.applicationId = applicationId;
	}

	public ProjectDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
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

	@Transient
	public String getCreatedDateStr() {
		if (created != null) {
			//FIXME why are we using this "magic" date format
			return new SimpleDateFormat("MM/dd/yyyy").format(created);
		}
		return "";
	}

	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getOwnerMail() {
		return ownerMail;
	}

	public void setOwnerMail(String ownerMail) {
		this.ownerMail = ownerMail;
	}

	@Transient
	public List<FormDTO> getForms() {
		return forms;
	}

	public void setForms(List<FormDTO> forms) {
		this.forms = forms;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

}
