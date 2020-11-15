package py.com.sodep.mobileforms.impl.services.metadata.core.find.criteria;

public class PropertyCriteria {

	PropertyCriteria(String name, String oper, Object value) {
		this.name = name;
		this.oper = oper;
		this.value = value;
	}

	private String name;

	private String oper;

	private Object value;

	public String getName() {
		return name;
	}

	public String getOper() {
		return oper;
	}

	public Object getValue() {
		return value;
	}

}
