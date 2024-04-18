package py.com.sodep.mobileforms.api.services.metadata.projects;

import java.util.List;

import py.com.sodep.mobileforms.api.dtos.ProjectDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

/**
 * To create, update, retrieve and search Projects
 * 
 * @author Miguel
 * 
 */
public interface IProjectService {

	/**
	 * Loads project's details (name, description, etc) in the given language.
	 * If there are no details on the given language will return the details on
	 * the default language.
	 * 
	 * @param projectId
	 * @param language
	 * @return
	 */
	ProjectDetails loadDetails(Long projectId, String language);

	/**
	 * a list of project where the user has access rights
	 * 
	 * @param app
	 * @param user
	 * @param language
	 * @param auth
	 * @return
	 */
	List<ProjectDTO> listProjects(Application app, User user, String language, String auth);

	/**
	 * Find projects using the label on the given language. If the
	 * projectToExclude is a valid project, then it won't be included on the
	 * list of projects. The projects returned will be those were the parameter
	 * user has the access rights provided by auth
	 * 
	 * @param user
	 * @param app
	 * @param projectToExclude
	 * @param auth
	 * @param value
	 * @param pageNumber
	 * @param pageSize
	 * @param language
	 * @return
	 */
	PagedData<List<ProjectDTO>> findProjectsByLabelAndExcludeProject(final User user, final Application app,
			Project projectToExclude, final String auth, String label, int pageNumber, int pageSize,
			final String language);

	/**
	 * Loads the required data of a project on a given langugae
	 * 
	 * @param projectId
	 * @param language
	 * @return
	 */
	ProjectDTO getProject(Long projectId, String language);

	/**
	 * Edit the existing project represented by projectId with the data supplied
	 * on dto
	 * 
	 * @param projectId
	 * @param owner
	 * @param dto
	 * @return
	 */
	Project edit(Long projectId, User owner, ProjectDTO dto);

	/**
	 * Cretes a new project with the data suuplied on dto
	 * 
	 * @param app
	 * @param owner
	 * @param dto
	 * @return
	 */
	Project createNew(Application app, User owner, ProjectDTO dto);

	Project findById(Long projectId);

	/**
	 * Mark a project as "deleted"
	 * 
	 * @param currentUser
	 * @param project
	 */
	Project logicalDelete(Project project);

	/**
	 * 
	 * @return the default role that should be assigned to somebody that has
	 *         created a project
	 */
	String getOwnerDefaultRole();

}
