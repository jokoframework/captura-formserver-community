package py.com.sodep.mobileforms.api.services.forms.model;

import java.util.List;

import py.com.sodep.mobileforms.api.editor.Command;
import py.com.sodep.mobileforms.api.entities.core.User;
import py.com.sodep.mobileforms.api.entities.projects.Project;

public interface ICommandService {

	ExecResponse executeSave(User user, Long formId, List<Command> commands);

	ExecResponse executeSave(User user, Long formId, Command[] commands);

	ExecResponse executeSaveAs(User user, Project project, Long formId, String label, Command[] commands);

	ExecResponse executeSaveAs(User user, Project project, Long formId, String label, List<Command> commands);

}
