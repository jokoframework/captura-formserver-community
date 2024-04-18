package py.com.sodep.mobileforms.api.services.metadata.core;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.AuthorizationGroupDTO;
import py.com.sodep.mobileforms.api.entities.core.Authorization;
import py.com.sodep.mobileforms.api.services.auth.IAuthorizationControlService;

/**
 * This class provides access to the available {@link Authorization} . It
 * doesn't provides logic to check if a user has enough rights to execute an
 * operation, if you are interested on this check
 * {@link IAuthorizationControlService}
 * 
 * @author danicricco
 * 
 */
public interface IAuthorizationService {

	/**
	 * Load an authorization based on its primary key
	 * 
	 * @param authorization
	 * @return
	 */
	public Authorization get(String authorization);

	/**
	 * <p>
	 * A list of all authorization groups of a Role. Each authorization group will contain
	 * a list of the authorization within the group.
	 * </p>
	 * <p>
	 * The language is used to automatically translate the label and the
	 * description of the authorization and authorization's group.
	 * </p>
	 * <p>
	 * An authorization's label can be register with [authorization_key].label.
	 * For example, the authorization "application.allmighty" can register a
	 * i18n key of the form "application.allmight.label". The same concept
	 * applies for the description and the label of the authorization group.
	 * </p>
	 * 
	 * @param roleId 
	 * @param language
	 * @return
	 */
	List<AuthorizationGroupDTO> getAuthorizationGroups(Long roleId, String language);
	
	/**
	 * Force to read all the authorization from the DB.
	 */
	public void reloadAuthorizations();

}
