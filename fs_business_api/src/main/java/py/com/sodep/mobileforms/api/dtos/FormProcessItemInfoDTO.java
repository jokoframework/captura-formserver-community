package py.com.sodep.mobileforms.api.dtos;

public class FormProcessItemInfoDTO implements DTO {

	private static final long serialVersionUID = 1L;

	public static final String ID = "id";

	private Long formId;
	private String formName;
	private Long projectId;
	private String projectName;
	private Long processItemVersion;
	
	public FormProcessItemInfoDTO(Long formId, String formName, Long projectId, String projectName,
			Long processItemVersion) {
		super();
		this.formId = formId;
		this.formName = formName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.processItemVersion = processItemVersion;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Long getProcessItemVersion() {
		return processItemVersion;
	}
	public void setProcessItemVersion(Long processItemVersion) {
		this.processItemVersion = processItemVersion;
	}
	
	
}
