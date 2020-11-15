package py.com.sodep.mobileforms.api.services.auth;

import java.util.HashMap;

/**
 * <p>
 * This is a class that summarize the relations between the different MF
 * objects. For example a project is inside an application, a form inside a
 * project.
 * </p>
 * <p>
 * The goal of the class is to help during authorization evaluation. If a user
 * doesn't have access to a given object it might have access on the upper
 * container.
 * </p>
 * <p>
 * This is specially useful on the JS side where there is no access to the
 * database.
 * </p>
 * <p>
 * Note that this is possible because there is no way to change the container of
 * an object. For example, If a user wants to "import" a form on another project
 * he is actually creating a copy of it.
 * </p>
 * 
 * @author danicricco
 * 
 */
public class MFObjectRelations {

	private HashMap<Long, Long> appOfProject;
	private HashMap<Long, Long> projectOfForm;
	private HashMap<Long, Long> projectOfPool;

	public HashMap<Long, Long> getAppOfProject() {
		return appOfProject;
	}

	public void setAppOfProject(HashMap<Long, Long> appOfProject) {
		this.appOfProject = appOfProject;
	}

	public HashMap<Long, Long> getProjectOfForm() {
		return projectOfForm;
	}

	public void setProjectOfForm(HashMap<Long, Long> projectOfForm) {
		this.projectOfForm = projectOfForm;
	}

	public HashMap<Long, Long> getProjectOfPool() {
		return projectOfPool;
	}

	public void setProjectOfPool(HashMap<Long, Long> projectOfPool) {
		this.projectOfPool = projectOfPool;
	}

}
