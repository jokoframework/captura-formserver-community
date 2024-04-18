package py.com.sodep.mobileforms.api.dtos;

public class QueryDTO implements DTO {

	private static final long serialVersionUID = 2L;

	private Long id;

	private Long formId;
	private Long version;

	private String name;

	private boolean defaultQuery;

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

	public boolean isDefaultQuery() {
		return defaultQuery;
	}

	public void setDefaultQuery(boolean defaultQuery) {
		this.defaultQuery = defaultQuery;
	}

	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}
