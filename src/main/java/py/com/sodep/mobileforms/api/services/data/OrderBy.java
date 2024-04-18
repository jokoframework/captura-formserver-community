package py.com.sodep.mobileforms.api.services.data;

public class OrderBy {
	
	public static final String META_NAMESPACE = MFStorable.FIELD_META;
	public static final String DATA_NAMESPACE = MFStorable.FIELD_DATA;

	private String field;

	private String namespace = DATA_NAMESPACE;

	private boolean ascending;

	public OrderBy(String field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public OrderBy(String namespace, String field, boolean ascending) {
		this.namespace = namespace;
		this.field = field;
		this.ascending = ascending;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

}
