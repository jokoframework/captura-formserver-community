package py.com.sodep.mobileforms.web.acme;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class ACMEObjectWrapper extends DefaultObjectWrapper {

	@Override
	public TemplateModel wrap(Object obj) throws TemplateModelException {
		if (obj == null) {
			return super.wrap(obj);
		} else {
			if (obj instanceof String) {
				return new XSSSafeString((String) obj);
			} else if (obj instanceof ACMEUnescapedString) {
				ACMEUnescapedString inst = (ACMEUnescapedString) obj;
				return super.wrap(inst.getString());
			} else {
				return super.wrap(obj);
			}
		}

	}

}
