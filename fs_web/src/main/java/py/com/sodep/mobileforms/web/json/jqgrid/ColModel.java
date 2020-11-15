package py.com.sodep.mobileforms.web.json.jqgrid;

import java.util.HashMap;
import java.util.Map;

public class ColModel {

	public static final String CUSTOM_FORMATTER = "custom";

	private String name;

	private String index;

	private String width;

	private String align;

	private boolean editable = true;

	private boolean hidden = false;

	private boolean search = true;

	private boolean sortable = true;

	private boolean resizable = true;

	private String edittype = "text";

	// http://www.trirand.com/jqgridwiki/doku.php?id=wiki:predefined_formatter
	private String formatter;

	private Map<String, String> editoptions = new HashMap<String, String>();

	private Map<String, Object> editrules = new HashMap<String, Object>();

	private Map<String, String> formatoptions = new HashMap<String, String>();

	private Map<String, String> formoptions = new HashMap<String, String>();

	public String getName() {
		return name;
	}

	public ColModel name(String name) {
		this.name = name;
		return this;
	}

	public String getIndex() {
		return index;
	}

	public ColModel index(String index) {
		this.index = index;
		return this;
	}

	public String getWidth() {
		return width;
	}

	public ColModel width(String width) {
		this.width = width;
		return this;
	}

	public String getAlign() {
		return align;
	}

	public ColModel align(String align) {
		this.align = align;
		return this;
	}

	public Boolean getEditable() {
		return editable;
	}

	public ColModel editable(Boolean editable) {
		this.editable = editable;
		return this;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public ColModel hidden(Boolean hidden) {
		this.hidden = hidden;
		return this;
	}

	public String getEdittype() {
		return edittype;
	}

	public ColModel edittype(String edittype) {
		this.edittype = edittype;
		return this;
	}

	public Map<String, String> getEditoptions() {
		return editoptions;
	}

	public void addEditoption(String key, String value) {
		this.editoptions.put(key, value);
	}

	public Map<String, Object> getEditrules() {
		return editrules;
	}

	public ColModel required(boolean value) {
		editrules.put("required", value);
		return this;
	}

	public ColModel email(boolean value) {
		editrules.put("email", Boolean.toString(value));
		return this;
	}

	public Map<String, String> getFormatoptions() {
		return this.formatoptions;
	}

	public ColModel addFormatoption(String key, String value) {
		this.formatoptions.put(key, value);
		return this;
	}

	public Map<String, String> getFormoptions() {
		return this.formoptions;
	}

	public ColModel addFormoption(String key, String value) {
		this.formoptions.put(key, value);
		return this;
	}

	public ColModel number(boolean value) {
		editrules.put("number", Boolean.toString(value));
		return this;
	}

	public ColModel edithidden(boolean value) {
		editrules.put("edithidden", Boolean.toString(value));
		return this;
	}

	public ColModel integer(boolean value) {
		editrules.put("integer", Boolean.toString(value));
		return this;
	}

	public ColModel maxValue(Integer val) {
		editrules.put("minValue", val.toString());
		return this;
	}

	public ColModel minValue(Integer val) {
		editrules.put("maxValue", val.toString());
		return this;
	}

	public ColModel elmprefix(String prefix) {
		formoptions.put("elmprefix", prefix);
		return this;
	}

	public ColModel elmsuffix(String suffix) {
		formoptions.put("elmsuffix", suffix);
		return this;
	}

	public String getFormatter() {
		return formatter;
	}

	public ColModel formatter(String formatter) {
		this.formatter = formatter;
		return this;
	}

	public boolean isSearch() {
		return search;
	}

	public ColModel search(boolean search) {
		this.search = search;
		return this;
	}

	public boolean isSortable() {
		return sortable;
	}

	public ColModel sortable(boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	public boolean isResizable() {
		return resizable;
	}

	public ColModel resizable(boolean resizable) {
		this.resizable = resizable;
		return this;
	}

}
