package py.com.sodep.mobileforms.web.controllers.lookuptable;

import java.util.List;

import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDefinitionException;
import py.com.sodep.mobileforms.api.services.data.LookuptableOperationException;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;
import py.com.sodep.mobileforms.web.i18n.I18nManager;
import py.com.sodep.mobileforms.web.json.JsonResponse;

public class LookupExceptionTranslator {

	public static String translate(LookupTableDefinitionException ex, I18nManager i18n, MFLoookupTableDefinition def) {
		switch (ex.getErrorCode()) {
		case LookupTableDefinitionException.IDENTIFIER_ALREADY_EXISTS:
			return i18n.getMessage("lookupTable.definition.identifier.duplicate", def.getInfo().getIdentifier());
		case LookupTableDefinitionException.MAX_FIELDS:
			return i18n.getMessage("lookupTable.definition.max_fields", Integer.toString(MFLoookupTableDefinition.MAX_FIELDS));
		default:
			return i18n.getMessage("web.generic.error");

		}
	}

	public static JsonResponse<String> translate(LookuptableOperationException ex, I18nManager i18n,
			IFormService formService) {
		JsonResponse<String> response = new JsonResponse<String>();
		response.setSuccess(false);

		switch (ex.getErrorCode()) {
		case LookuptableOperationException.LOOKUP_IN_USE:

			response.setTitle(i18n.getMessage("web.lookup.error.inuse.title"));
			List<Long> forms = ex.getFormInUse();

			if (forms != null&&forms.size()>0) {
				StringBuffer buff = new StringBuffer();
				buff.append("<ul>");
				for (Long fId : forms) {
					String formLabel = formService.getLabel(fId, i18n.getSelectedLanguage());
					buff.append("<li>"+formLabel + "</li>");
				}
				buff.append("</ul>");
				response.setUnescapedMessage(buff.toString());
			}
			break;
		default:

			response.setTitle(i18n.getMessage("web.generic.error"));
			break;
		}
		return response;
	}
}
