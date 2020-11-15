package py.com.sodep.mobileforms.api.constants;

import py.com.sodep.mobileforms.api.services.auth.AuthorizationGroup;
import py.com.sodep.mobileforms.api.services.auth.DeclareAuthorizationLevel;

public class AuthorizationNames {

	@DeclareAuthorizationLevel(level = 0, prefix = "sys", column = "")
	@AuthorizationGroup("authorizationGroup.system")
	public static class System {

		public static final String SYS_ALLMIGHTY = "sys.allmighty";

		public static final String MENU_SYSADMIN = "sys.menu";

	}

	@DeclareAuthorizationLevel(level = 1, prefix = "app", column = "applicationId")
	@AuthorizationGroup("authorizationGroup.application")
	public static class App {

		/**
		 * Authorization that allows the user to change the app settings
		 */
		public static final String APP_CONFIG = "application.config";

		/**
		 * Authorization that allows a user to create a project
		 */
		@AuthorizationGroup("authorizationGroup.project")
		public static final String PROJECT_CANCREATE = "application.project.cancreate";

		/**
		 * Authorization that allows a user to create a pool
		 */
		@AuthorizationGroup("authorizationGroup.pool")
		public static final String POOL_CANCREATE = "application.pool.cancreate";

		/**
		 * Authorizations that allow a user to search other existing users.
		 * 
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String USER_LIST = "application.user.list";

		/**
		 * Authorizations that allow a user to delete other users
		 * 
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String USER_DELETE = "application.user.delete";

		/**
		 * Authorizations that allow a user to create another user
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String USER_CREATE = "application.user.cancreate";

		/**
		 * Authorizations that allow a user to edit another user
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String USER_EDIT = "application.user.edit";

		/**
		 * Authorizations that allow a user to search existing groups.
		 * 
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String GROUP_LIST = "application.group.list";

		/**
		 * Authorizations that allow a user to create groups
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String GROUP_CREATE = "application.group.cancreate";

		/**
		 * Authorization that allows a user to edit groups
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String GROUP_EDIT = "application.group.edit";

		/**
		 * Authorization that allows a user to delete groups
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String GROUP_DELETE = "application.group.delete";
		/**
		 * Authorizations that allow a user to search existing roles.
		 * 
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String ROLES_LIST = "application.role.list";

		/**
		 * Authorizations that allow a user to edit roles
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String ROLES_EDIT = "application.role.edit";

		/**
		 * Authorizations that allow a user to create roles
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String ROLES_CREATE = "application.role.cancreate";

		/**
		 * Authorizations that allow a user to delete roles
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String ROLES_DELETE = "application.role.delete";

		/**
		 * An authorization that will grant all possible authorization over
		 * roles
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String ROLE_ADMIN = "application.role.administration";
		
		/**
		 * Authorization that allow a user to read and change the application's
		 * license
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String APPLICATION_LICENSE = "application.license";

		/**
		 * Authorization that allow a user to disassociate devices from other users on 
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String APPLICATION_DIASSOCIATE_DEVICE = "application.diassociateDevice";
		
		/**
		 * Allows to see the project list in the Process Manager
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String PROJECT_LIST = "application.project.list";

		/**
		 * Allows to see the form list in the Process Manager
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String FORM_LIST = "application.form.list";

		/**
		 * Allows to see the pool list in the Process Manager
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String POOL_LIST = "application.pool.list";

		/**
		 * Allows to see the process items list in the Process Manager
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String PROCESS_ITEM_LIST = "application.processItem.list";

		/**
		 * Allows to create a lookup table inside an application
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String LOOKUP_CREATE = "application.lookupTable.create";

		/**
		 * Allows to edit the lookuptable and its data, and also remove
		 * lookuptables
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String LOOKUP_EDIT = "application.lookupTable.edit";

		/**
		 * Allows to read the content of a lookuptable
		 */
		@AuthorizationGroup("authorizationGroup.hidden")
		public static final String LOOKUP_READ = "application.lookupTable.read";

		/**
		 * Allows to create/edit/delete lookup tables
		 */
		@AuthorizationGroup("authorizationGroup.connector")
		public static final String LOOKUP_ADMINISTRATION = "application.lookupTable.administration";

		/**
		 * Allows to create a project from the toolbox
		 */
		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_PROJECT = "application.toolbox.project.new";

		/**
		 * Allows to create a form from the toolbox
		 */
		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_FORM = "application.toolbox.form.new";

		/**
		 * Allows to create a pool from the toolbox
		 */
		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_POOL = "application.toolbox.pool.new";

		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_PROCESS_ITEM = "application.toolbox.processItem.new";

		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_USER = "application.toolbox.user.new";

		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_GROUP = "application.toolbox.group.new";

		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_DEVICE = "application.toolbox.device.new";

		@AuthorizationGroup("authorizationGroup.toolbox")
		public static final String TOOLBOX_CREATE_ROLE = "application.toolbox.role.new";

		
		// ********************************************************************************
		// MENU AUTHORIZATIONS STARTS HERE
		// ********************************************************************************

		@AuthorizationGroup("authorizationGroup.menu")
		public static final String MENU_PROCESSMANAGER = "application.menu.processManager";

		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_DATAIMPORT = "application.menu.dataImport";

		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_ROLES = "application.menu.roles";

		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_USERS_AND_GROUPS = "application.menu.usersAndGroups";

		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_DEVICES = "application.menu.devices";

		/**
		 * This authorization gives access to application settings
		 */
		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_CONFIG = "application.menu.config";

		@AuthorizationGroup(value = "authorizationGroup.menu")
		public static final String MENU_LOOKUP_TABLES = "application.menu.lookupTables";

		@AuthorizationGroup(value = "authorizationGroup.REST")
		public static final String REST_API_LOOKUPTABLES_CREATE = "application.rest.lookupTables.create";

		@AuthorizationGroup(value = "authorizationGroup.REST")
		public static final String REST_API_LOOKUPTABLES_LIST = "application.rest.lookupTables.list";

		@AuthorizationGroup(value = "authorizationGroup.REST")
		public static final String REST_API_LOOKUPTABLES_INSERT = "application.rest.lookupTables.insert";

		@AuthorizationGroup(value = "authorizationGroup.REST")
		public static final String REST_API_LOOKUPTABLES_MODIFY = "application.rest.lookupTables.modify";

		@AuthorizationGroup(value = "authorizationGroup.REST")
		public static final String REST_API_LOOKUPTABLES_READ = "application.rest.lookupTables.read";
		
		/**
		 * An authorization that will grant all possible authorization over
		 * workflow administration features
		 */
		@AuthorizationGroup("authorizationGroup.userAccess")
		public static final String WORKFLOW_ADMIN = "application.workflow.administration";
		
	}

	@DeclareAuthorizationLevel(level = 2, prefix = "project", column = "projectId")
	@AuthorizationGroup("authorizationGroup.project")
	public static class Project {

		/**
		 * The user can change the label and description of the project, add
		 * forms and grant authorization to users and groups.
		 */
		public static final String EDIT = "project.edit";

		public static final String DELETE = "project.delete";

		/**
		 * A web user has authorization to see a project in the web application.
		 * Otherwise, the project isn't visible
		 */
		public static final String READ_WEB = "project.read.web";

		/**
		 * A user has authorization to see the project from a mobile device
		 * */
		public static final String READ_MOBILE = "project.read.mobile";

		/**
		 * The user can create forms inside a Project
		 */

		@AuthorizationGroup("authorizationGroup.form")
		public static final String CREATE_FORM = "project.create.form";

	}

	@DeclareAuthorizationLevel(level = 3, prefix = "form", column = "formId")
	@AuthorizationGroup("authorizationGroup.form")
	public static class Form {

		/**
		 * Edit the form on the web
		 */
		public static final String EDIT = "form.edit";

		/**
		 * A mobile user that has authorization to see a form, therefore post
		 * data on it. Otherwise, the form is invisible
		 */
		public static final String MOBILE = "form.mobile";

		/**
		 * A web user that has authorization to see a form. Otherwise, the form
		 * isn't visible
		 */
		public static final String READ_WEB = "form.read.web";

		/**
		 * Open the form manager.
		 */
		public static final String OPEN_MANAGER = "form.open";
		/**
		 * A web user that has authorization to post data on a Form
		 */
		public static final String INPUT_DATA_WEB = "form.inputData.web";

		/**
		 * A web user that has authorization to design a form
		 */
		public static final String CHANGE_DESIGN = "form.design";

		/**
		 * A web user that has authorization to publish a form
		 */
		public static final String PUBLISH = "form.publish";

		public static final String VIEW_REPORTS = "form.viewReport";

		public static final String CREATE_REPORTS = "form.createReport";
		
		public static final String DELETE_REPORTS = "form.deleteReport";

		public static final String DELETE = "form.delete";
		
		public static final String READ_WORKFLOW = "form.workflow.read";
		
		public static final String TRANSITION_WORKFLOW = "form.workflow.transition";

	}

	@DeclareAuthorizationLevel(level = 4, prefix = "pool", column = "poolId")
	@AuthorizationGroup("authorizationGroup.pool")
	public static class Pool {

		public static final String READ = "pool.read";

		public static final String EDIT = "pool.edit";

		public static final String DELETE = "pool.delete";

	}

	/** Names of features that an application can have. */
	@AuthorizationGroup("features")
	public static class Feature {

		public static final String WORKFLOW = "feature.workflow";

	}
}
