package py.com.sodep.mobileforms.impl.services.metadata.core;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mobileforms.api.dtos.AuthorizationDTO;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;

/**
 * Encapsulate some useful to methods to convert an {@link Authorization} to an
 * {@link AuthorizationDTO}
 * 
 * @author danicricco
 * 
 */
class AuthorizationHelper {

	static AuthorizationDTO translateAuthorizationToDTO(Authorization auth, I18nBundle i18n, String language) {
		AuthorizationDTO authDTO = new AuthorizationDTO();
		String authKey = auth.getName();
		authDTO.setId(authKey);
		authDTO.setVisible(auth.getVisible());
		String label = null;
		String description = null;
		if (language != null) {
			label = i18n.getLabel(language, authKey + ".label");
			description = i18n.getLabel(language, authKey + ".description");
		}
		if (label == null) {
			label = authKey + ".label";
		}
		if (description == null) {
			description = authKey + ".description";
		}
		authDTO.setName(label);
		authDTO.setDescription(description);

		List<Authorization> dependantAuthorizations = auth.getDependentAuthorizations();
		ArrayList<AuthorizationDTO> dependantAuthList = new ArrayList<AuthorizationDTO>();
		for (Authorization dependantAuth : dependantAuthorizations) {
			AuthorizationDTO authDTODependant = translateAuthorizationToDTO(dependantAuth, i18n, language);
			dependantAuthList.add(authDTODependant);
		}
		authDTO.setDependantAuthorization(dependantAuthList);
		return authDTO;
	}

	static AuthorizationDTO translateAuthorizationToDTO(Authorization auth) {
		return translateAuthorizationToDTO(auth, null, null);
	}
}
