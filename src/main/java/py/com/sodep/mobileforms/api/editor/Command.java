package py.com.sodep.mobileforms.api.editor;

import java.util.List;
import java.util.Map;

/**
 * A command sent by the editor to modify a form definition
 * 
 * @author jmpr
 * 
 */
public class Command {

	public enum Type {
		ADD, DELETE, EDIT
	}

	private Type type;

	private MFRef ref;

	private List<Map<String, String>> attributes;

	/**
	 * Type of command to be executed
	 * 
	 * @return
	 */
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * The reference to the element (Form, Page, Item) against which the command
	 * is executed
	 * 
	 * @return
	 */
	public MFRef getRef() {
		return ref;
	}

	public void setRef(MFRef ref) {
		this.ref = ref;
	}

	/**
	 * Command attributes. As when executing a command in the command line,
	 * parameters may be passed. The meaning of those attributes or parameters
	 * depend on the command to be executed and the reference against which it
	 * is executed
	 * 
	 * @return
	 */
	public List<Map<String, String>> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Map<String, String>> attributes) {
		this.attributes = attributes;
	}

}
