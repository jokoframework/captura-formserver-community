package py.com.sodep.mobileforms.web.acme;

import java.io.Serializable;

import org.apache.commons.lang3.StringEscapeUtils;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * A {@link TemplateScalarModel} that wraps a string into an XSS safe string, by escaping the html tags.
 * @author danicricco
 *
 */
public class XSSSafeString implements TemplateScalarModel,Serializable {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * @serial the value of this <tt>SimpleScalar</tt> if it wraps a
     * <tt>String</tt>.
     */
    private String value;
	
	public XSSSafeString(String obj) {
		this.value=obj;
	}

	@Override
	public String getAsString() throws TemplateModelException {
		return (value == null) ? "" :StringEscapeUtils.escapeHtml4(value);
	}

	@Override
	public String toString() {
		return value;
	}
	
	

}
