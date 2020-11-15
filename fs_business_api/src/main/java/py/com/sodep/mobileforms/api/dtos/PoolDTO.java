package py.com.sodep.mobileforms.api.dtos;


public class PoolDTO implements DTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String name;

	private String description;

	private boolean active;
	
	private Long applicationId;

	public PoolDTO() {

	}

	public PoolDTO(Long id, String name, String description, boolean active) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setLabel(String label) {
		this.name = label;
	}

	/**
	 * Label is a synonym of name
	 * @return
	 */
	public String getLabel() {
		return this.name;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	
}
