package py.com.sodep.mobileforms.api.dtos;

import java.util.List;
import java.util.Map;

public class ProcessItemDTO implements DTO {

	private static final long serialVersionUID = 1L;

	public static final String TYPE = "type";

	public static final String POOL = "pool";
	public static String LABEL = "label";
	public static String REQUIRED = "required";
	public static String VISIBLE = "visible";
	public static String CREATED = "created";

	private Long id;

	private Long rootId;

	private String label;

	private Boolean required;

	private Boolean visible;

	private Boolean active;

	private String type;

	private Long pool;

	private String source;

	private List<Map<String, String>> options;

	private String defaultValue;

	private String defaultLongitude;

	private String defaultLatitude;

	private Long version;

	// Input
	private String min;

	private String max;

	private Boolean readonly;

	private String lookupCollection;

	private String lookupLabel;

	private String lookupValue;

	private Boolean multiple;

	private Long lookupIdentifier;

	public Boolean getActive() {
		return active;
	}

	public String getDefaultLatitude() {
		return defaultLatitude;
	}

	public String getDefaultLongitude() {
		return defaultLongitude;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getLookupCollection() {
		return lookupCollection;
	}

	public Long getLookupIdentifier() {
		return lookupIdentifier;
	}

	// Select

	public String getLookupLabel() {
		return lookupLabel;
	}

	public String getLookupValue() {
		return lookupValue;
	}

	public String getMax() {
		return max;
	}

	public String getMin() {
		return min;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public List<Map<String, String>> getOptions() {
		return options;
	}

	public Long getPool() {
		return pool;
	}

	public Long getRootId() {
		return rootId;
	}

	public Boolean getReadonly() {
		return readonly;
	}

	public Boolean getRequired() {
		return required;
	}

	public String getSource() {
		return source;
	}

	public String getType() {
		return type;
	}

	public Long getVersion() {
		return version;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setDefaultLatitude(String defaultLatitude) {
		this.defaultLatitude = defaultLatitude;
	}

	public void setDefaultLongitude(String defaultLongitude) {
		this.defaultLongitude = defaultLongitude;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLookupCollection(String lookupCollection) {
		this.lookupCollection = lookupCollection;
	}

	public void setLookupIdentifier(Long lookupIdentifier) {
		this.lookupIdentifier = lookupIdentifier;
	}

	public void setLookupLabel(String lookupLabel) {
		this.lookupLabel = lookupLabel;
	}

	public void setLookupValue(String lookupValue) {
		this.lookupValue = lookupValue;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public void setOptions(List<Map<String, String>> options) {
		this.options = options;
	}

	public void setPool(Long pool) {
		this.pool = pool;
	}

	public void setRootId(Long processItemRootId) {
		this.rootId = processItemRootId;
	}

	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

}
