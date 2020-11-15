package py.com.sodep.mobileforms.web.endpoints.json;

public class WorkflowActionRequest {
	
	private Long formVersion = 0L;
	
	private Long formId = 0L;

	private String workflowComment;
	
	public WorkflowActionRequest(){
		// empty constructor
	}
	
	public Long getFormVersion() {
		return formVersion;
	}
	public Long getFormId() {
		return formId;
	}
	
	/**
	 * @param formVersion the formVersion to set
	 */
	public void setFormVersion(Long formVersion) {
		this.formVersion = formVersion;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public String getWorkflowComment() {
		return this.workflowComment;
	}
	
	public void setWorkflowComment(String worklfowComment) {
		this.workflowComment = worklfowComment;
	}
	
}
