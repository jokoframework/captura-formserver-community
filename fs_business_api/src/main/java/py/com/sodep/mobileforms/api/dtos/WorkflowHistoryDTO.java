package py.com.sodep.mobileforms.api.dtos;

public class WorkflowHistoryDTO {

	private final Long oldStateId;
	
	private final Long newStateId;
	
	private final Long changedBy;
	
	private final Long formId;
	
	private final Long docId;

	private final Long formVersion;
	
	private final String comment;
	
	public static Builder builder() {
		return new Builder();
	}
	
	public WorkflowHistoryDTO(Builder builder) {
		this.oldStateId = builder.oldStateId;
		this.newStateId = builder.newStateId;
		this.changedBy = builder.changedBy;
		this.formId = builder.formId;
		this.docId = builder.docId;
		this.formVersion = builder.formVersion;
		this.comment = builder.comment;
	}
	
	/**
	 * @return the oldStateId
	 */
	public Long getOldStateId() {
		return oldStateId;
	}

	/**
	 * @return the newStateId
	 */
	public Long getNewStateId() {
		return newStateId;
	}

	/**
	 * @return the changedBy
	 */
	public Long getChangedBy() {
		return changedBy;
	}

	/**
	 * @return the formId
	 */
	public Long getFormId() {
		return formId;
	}

	/**
	 * @return the docId
	 */
	public Long getDocId() {
		return docId;
	}

	public Long getFormVersion() {
		return formVersion;
	}
	
	public String getComment() {
		return comment;
	}

	public static class Builder {

		public Long formVersion;
		public Long docId;
		public Long formId;
		private Long oldStateId;
		private Long newStateId;
		private Long changedBy;
		private String comment;
		
		public Builder oldStateId(Long oldStateId) {
			this.oldStateId = oldStateId;
			return this;
		}
		
		public Builder newStateId(Long newStateId) {
			this.newStateId = newStateId;
			return this;
		}
		
		public Builder changedBy(Long changedBy) {
			this.changedBy = changedBy;
			return this;
		}
		
		public Builder formId(Long formId) {
			this.formId = formId;
			return this;
		}
		
		public Builder docId(Long docId) {
			this.docId = docId;
			return this;
		}
		
		public Builder formVersion(Long formVersion) {
			this.formVersion = formVersion;
			return this;
		}
		
		public Builder comment(String comment) {
			this.comment = comment;
			return this;
		}
		
		public WorkflowHistoryDTO build() {
			return new WorkflowHistoryDTO(this);
		}
		
	}

	
}
