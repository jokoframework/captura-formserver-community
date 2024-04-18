package py.com.sodep.mobileforms.api.dtos;

public class ApplicationDTO implements DTO {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String name;

	private String defaultLanguage;
	
	private boolean hasWorkflow;

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

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
	
	public boolean getHasWorkflow() {
		return this.hasWorkflow;
	}
	
	public void setHasWorkflow(boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
	}

}
