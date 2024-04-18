package py.com.sodep.mobileforms.api.entities.log;


public enum LoginType {
	
	WEB("WEB"), 
	DEVICE("DEVICE"),
	REST_CLIENT("REST_CLIENT"),
	CONNECTOR_REPOSITORY("CONNECTOR_REPOSITORY"), 
	UNKNOWN("UNKNOWN");
	
	private final String value;

	LoginType(String value) {
		this.value = value;
	}

	public static LoginType fromValue(String value) {
		if (value != null) {
			for (LoginType mode : values()) {
				if (mode.value.equals(value)) {
					return mode;
				}
			}
		}

		// you may return a default value
		return getDefault();
	}

	public static LoginType getDefault() {
		return UNKNOWN;
	}

	public String toValue() {
		return value;
	}
}
