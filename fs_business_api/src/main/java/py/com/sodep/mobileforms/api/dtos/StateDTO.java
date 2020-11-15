package py.com.sodep.mobileforms.api.dtos;

public class StateDTO {

	private final Long id;
	
	private final String comment;

	private String name;

	private Boolean initial;

	private String description;

	private Long formId;
	
	public StateDTO(Long stateId, String comment) {
		super();
		this.id = stateId;
		this.comment = comment;
		this.initial = Boolean.FALSE;
		this.name = null;
	}

	public StateDTO(Long stateId) {
		super();
		this.id = stateId;
		this.comment = null;
		this.initial = Boolean.FALSE;
		this.name = null;
	}

	public StateDTO() {
		super();
		this.id = null;
		this.comment = null;
		this.initial = Boolean.FALSE;
		this.name = null;
	}

	/**
	 * @return the stateId
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setInitial(Boolean initial) {
		this.initial = initial;
	}
	
	public Boolean getInitial() {
		return this.initial;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Long getFormId() {
		return this.formId;
	}
	
	public void setFormId(Long formId) {
		this.formId = formId;
	}

	
	
}
