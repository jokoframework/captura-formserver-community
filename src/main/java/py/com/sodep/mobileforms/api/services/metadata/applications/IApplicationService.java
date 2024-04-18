package py.com.sodep.mobileforms.api.services.metadata.applications;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.ApplicationDTO;
import py.com.sodep.mobileforms.api.dtos.ApplicationSettingsDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface IApplicationService {

	/**
	 * A wrapper of {@link #initAppWithOwner(String, User, boolean, String)}
	 * that use addOwnerAsMember=true
	 * 
	 * @param name
	 * @param owner
	 * @param defaultLanguage
	 * @return
	 */
	public Application initAppWithOwner(String name, User owner, String defaultLanguage);

	/**
	 * Initializes an application with the given name, owner and defaultLanguage.
	 * IMPORTANT: This method is not Thread safe!. It should be called from
	 * within a synchronized block.
	 * 
	 * @param name
	 * @param owner
	 * @param defaultLanguage
	 * @param addOwnerAsMember
	 *            if set to true the owner will be added as a member of the
	 *            application
	 * @return
	 */
	public Application initAppWithOwner(String name, User owner, boolean addOwnerAsMember, String defaultLanguage);

	/**
	 * Checks of an application with the given name exits
	 * 
	 * @param name
	 * @return
	 */
	boolean appExists(String name);

	List<Application> findAll();
	
	PagedData<List<Application>> findAll(String orderBy, boolean ascending, int pageNumber, int pageSize);

	List<Application> findByName(String name);

	Application findById(Long applicationId);

	Application save(Application app);

	boolean initialSetup(Application app, String label, String language);

	PagedData<List<Application>> findByLabel(String label, int pageNumber, int pageLength, String language);

	List<String> getSupportedLanguages();

	Long count();
	
	Long countActive();
	
	boolean isActive(Long applicationId);

	PagedData<List<Application>> findByProperty(String propertyName,
			String oper, String searchValue, String orderBy,
			boolean ascending, Integer page, Integer rows);


	public ApplicationDTO saveSettings(Long appId, ApplicationSettingsDTO dto);

}
