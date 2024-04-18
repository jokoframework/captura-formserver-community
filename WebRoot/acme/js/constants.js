define([ "jquery", "acme" ], function($, acme) {
	var pkg = {};
	
	// IMPORTANT: This object has database IDs that should be in sync with gui_definition.sql
	
	pkg.LAUNCHER_IDS = {
			editor : 4,
			roles_crud : 10,
			devices : 15,
			process_manager : 17,
			app_license : 22,
			my_account : 24,
			application_settings : 25,
			project_new : 26,
			role_permissions : 27,
			project_edit : 28,
			form_new : 29,
			form_edit : 30,
			myAccountMenuItem : 24,
			users_and_groups : 19,
			group_new : 33,
			group_edit : 34,
			user_new : 35,
			user_edit : 36,
			pool_new : 37,
			pool_edit : 38,
			process_item_new : 39,
			process_item_edit : 40,
			reports : 41,
			reportQuery : 42,
			lookupData:44,
			device_new : 1001,
			systemParameters: 1004
	};
	
	//keynote (design form)
	pkg.LANUCHER_ID_TO_GLYPH_ICON={
		12: 'file_import',//csv import
		26:'folder_open' ,//project
		29:'notes_2',// form
		37:'briefcase',//pool cargo
		39 : 'nameplate',//more_items
		33 : 'group',//group
		35 : 'user_add',//user
		1001: 'iphone',//device
		1002: 'notes_2',//form
		1003: 'nameplate',//process
		1004: 'adjust_alt'//systemParameters
	};
	
	pkg.start = function() {
		
	};
	
	return pkg;
});