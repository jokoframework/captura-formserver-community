
--erase GUI definition

update sys.acme_launchers set view_id=null;
delete from sys.acme_views;
delete from sys.acme_tree_menu;
delete from sys.acme_launchers;
--------------------------------------------------------
--INSERT VIEWS
--------------------------------------------------------
-- INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (3, 'web.home.data.input',  'home/data/data-input-amd', '/home/pages/data/data-input.mob', false, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (4, 'web.home.editor',  'editor/editor-amd', '/home/pages/editor.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (10, 'web.home.admin.roles',  'home/admin/role-crud-amd', '/home/pages/admin/role.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (12, 'web.home.data.import',  'home/data/data-import-amd', '/home/pages/data/data-import.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (13, 'web.home.data.lookup_table',  'home/data/lookuptable-amd', '/home/pages/data/lookuptable.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (14, 'web.system', null , '/system.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (15, 'web.home.admin.devices', 'home/v2/devices/devices-amd', '/home/pages/v2/devices/devices.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (16, 'web.home.acmeui', 'ui-examples/ui-examples' , '/testUI/loadUIComponentExamples.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (17, 'web.processes.home', 'home/v2/processes/processes-amd' , '/home/pages/v2/processes/processes.mob', true, false, false, true);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (19, 'web.home.usersAndGroups', 'home/v2/users-and-groups/users-and-groups-amd' , '/home/pages/v2/users-and-groups/users-and-groups.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (21, 'web.home.connectors',  'home/v2/misc/not-yet-implemented-amd', '/home/pages/v2/misc/not-yet-implemented.mob', true, false, false);
-- INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (22, 'web.home.license',  'home/license/license-amd', '/settings/license.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (24, 'web.home.myaccount',  'home/my-account/my-account', '/settings/my-account.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (25, 'web.home.application_settings', 'home/application/settings', '/application/settings.mob', true, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (26, 'web.home.admin.project.new',  'home/admin/new-project-crud-amd', '/home/pages/admin/new-project-crud.mob', true, true, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (27, 'web.home.admin.role.permission',  'home/admin/role-permissions-amd', '/home/pages/admin/role-permissions.mob', true, true, true, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (28, 'web.home.admin.project.edit',  'home/admin/new-project-crud-amd', '/home/pages/admin/edit-project-crud.mob', true, true, true, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (29, 'web.home.admin.form.new',  'home/admin/new-form-crud-amd', '/home/pages/admin/new-form-crud.mob', true, true, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (30, 'web.home.admin.form.edit',  'home/admin/new-form-crud-amd', '/home/pages/admin/edit-form-crud.mob', true, true, true, false);
-- INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (31, 'web.home.admin.brands', 'home/admin/brand-crud-amd' , '/home/pages/admin/brand.mob', false, false, false);
-- INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (32, 'web.home.admin.models', 'home/admin/model-crud-amd' , '/home/pages/admin/model.mob', false, false, false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (33, 'web.home.admin.group.new', 'home/v2/users-and-groups/new-group-amd' , '/home/pages/v2/users-and-groups/new-group.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (34, 'web.home.admin.group.edit', 'home/v2/users-and-groups/new-group-amd' , '/home/pages/v2/users-and-groups/edit-group.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (35, 'web.home.admin.user.new', 'home/v2/users-and-groups/new-user-amd' , '/home/pages/v2/users-and-groups/new-user.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (36, 'web.home.admin.user.edit', 'home/v2/users-and-groups/new-user-amd' , '/home/pages/v2/users-and-groups/edit-user.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (37, 'web.home.admin.pool.new', 'home/admin/new-pool-crud-amd' , '/home/pages/admin/new-pool-crud.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (38, 'web.home.admin.pool.edit', 'home/admin/new-pool-crud-amd' , '/home/pages/admin/edit-pool-crud.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (39, 'web.home.admin.processitem.new', 'home/process-item/process-item-amd' , '/home/pages/process-item/process-item.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (40, 'web.home.admin.processitem.edit', 'home/process-item/process-item-amd' , '/home/pages/process-item/process-item.mob', true, false, false,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (41, 'web.home.reports',  'home/reports/reports-amd', '/home/pages/reports/reports.mob', true, false, true,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (42, 'web.home.reports.query',  'home/reports/query-amd', '/home/pages/reports/query.mob', true, false, true,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator,trigger_navigator_link) VALUES (43, 'web.home.uncaughtException',  'home/sysadmin/uncaughtException-amd', '/sysadmin/uncaughtException.mob', true, false, true,false);
INSERT INTO sys.acme_views (id, name, js_amd, url_view, show_menu, show_toolbox, show_navigator) VALUES (44, 'web.home.data.lookup_table_data',  'home/data/lookuptable-data-amd', '/home/pages/data/lookuptable-data.mob', true, true, false);
INSERT INTO sys.acme_views (id,name,js_amd,url_view,toolbox_root,show_menu,show_toolbox,show_navigator,trigger_navigator_link) VALUES (46,'web.home.systemParameters','home/sysadmin/systemParameters-crud-amd','/sysadmin/systemParameters.mob',null,true,false,true,true);

--------------------------------------------------------
--INSERT LAUNCHERS
--------------------------------------------------------
--launchers of view start with 1
-- INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (3, 'web.home.data.input', 0, NULL, 3, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (4, 'web.home.editor', 0, NULL, 4, NULL,NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (10, 'web.home.admin.roles', 0, NULL, 10, NULL,'application.menu.roles');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (12, 'web.home.data.import', 0, NULL, 12, NULL,'application.menu.dataImport');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (13, 'web.home.data.lookup_table', 0, NULL, 13, NULL,'application.menu.lookupTables');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (14, 'web.system', 0, NULL, 14, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (15, 'web.home.admin.devices', 0, NULL, 15, NULL,'application.menu.devices');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (16, 'web.home.acmeui', 0, NULL, 16, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (17, 'web.processes.home', 0, NULL, 17,NULL, 'application.menu.processManager');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (19, 'web.home.usersAndGroups', 0, NULL, 19, NULL,'application.menu.usersAndGroups');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (21, 'web.home.connectors', 0, NULL, 21, NULL);
-- INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (22, 'web.home.license', 0, NULL, 22, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (24, 'web.home.myaccount', 0, NULL, 24, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (25, 'web.home.application_settings', 0, NULL, 25, NULL, 'application.menu.config'); 
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (26, 'web.home.admin.project.new', 0, NULL, 26, NULL,'application.toolbox.project.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (27, 'web.home.admin.role.permission', 0, NULL, 27, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (28, 'web.home.admin.project.edit', 0, NULL, 28, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (29, 'web.home.admin.form.new', 0, NULL, 29, NULL,'application.toolbox.form.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (30, 'web.home.admin.form.edit', 0, NULL, 30, NULL);
-- INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd, authorization_name) VALUES (31, 'web.home.admin.brands', 0, NULL, 31, NULL,'sys.menu');
-- INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd, authorization_name) VALUES (32, 'web.home.admin.models', 0, NULL, 32, NULL,'sys.menu');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (33, 'web.home.admin.group.new', 0, NULL, 33, NULL,'application.toolbox.group.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (34, 'web.home.admin.group.edit', 0, NULL, 34, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (35, 'web.home.admin.user.new', 0, NULL, 35, NULL,'application.toolbox.user.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (36, 'web.home.admin.user.edit', 0, NULL, 36, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (37, 'web.home.admin.pool.new', 0, NULL, 37, NULL,'application.toolbox.pool.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (38, 'web.home.admin.pool.edit', 0, NULL, 38, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (39, 'web.home.admin.processitem.new', 0, NULL, 39, NULL,'application.toolbox.processItem.new');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (40, 'web.home.admin.processitem.edit', 0, NULL, 40, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (41, 'web.home.reports', 0, NULL, 41, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd) VALUES (42, 'web.home.reports.query', 0, NULL, 42, NULL);
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (43, 'web.home.uncaughtException', 0, NULL, 43, NULL,'sys.menu');
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (44, 'web.home.data.lookup_table_data', 0, NULL, 44, NULL,'application.menu.lookupTables');
INSERT INTO sys.acme_launchers (id,name,launch_type,js_code,view_id,js_amd,authorization_name) VALUES (1004,'web.home.admin.systemParameters',0,null,46,null,null);


--launchers of type EXECUTE_JS start with 1000
-- INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (1001, 'web.home.admin.device.new', 1, 'createNewDevice', NULL, 'home/admin/device-crud-amd','application.toolbox.device.new');
--this launcher is for opening a new form inside projects page
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (1002, 'web.home.admin.form.new', 1, 'newForm', NULL, 'home/admin/new-project-crud-amd','application.toolbox.project.new');
--this launcher is for opening a new process item inside pool page
INSERT INTO sys.acme_launchers (id, name, launch_type, js_code, view_id, js_amd,authorization_name) VALUES (1003, 'web.home.admin.pool.new', 1, 'newProcessItem', NULL, 'home/admin/new-pool-crud-amd','application.toolbox.pool.new');

--------------------------------------------------------
--CREATE THE MENU
--------------------------------------------------------
--each root menu should start with a multiple of 100

-- MAIN
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (100,'web.processes.home','web.processes.home',false,NULL,1,17);
 --We need this entry to properly display the breadcrum (a.k.a navigator). Every page should be declared within the menu tree, even though it is not visible
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1011,'web.home.admin.project.manager','web.home.admin.project.manager',true, 100, 1,26,false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1012,'web.home.admin.project.manager','web.home.admin.project.manager',true, 100, 2,28,false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1013,'web.home.admin.form.manager','web.home.admin.form.manager',true, 100, 3, 29, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1014,'web.home.admin.form.manager', 'web.home.admin.form.manager', true, 100, 4, 30, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1015,'web.home.admin.pool.manager','web.home.admin.pool.manager',true, 100, 5, 37, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1016,'web.home.admin.pool.manager', 'web.home.admin.pool.manager', true, 100, 6, 38, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1017,'web.home.admin.processitem.manager', 'web.home.admin.processitem.manager', false, 100, 7, 39, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1018,'web.home.admin.processitem.manager', 'web.home.admin.processitem.manager', false, 100, 8, 40, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1019,'web.home.editor', 'web.home.editor', false, 100, 1, 4, false); 
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1020,'web.home.reports','web.home.reports',false, 100, 9, 41, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id, visible) VALUES (1021,'web.home.reports.query','web.home.reports.query',false, 1020, 1, 42, false);

 --Administration
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (400, 'web.home.administration', 'web.home.administration', false, NULL, 2, NULL);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (401, 'web.home.devices', 'web.home.devices', false, 400, 1, 15); 
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (402, 'web.home.usersAndGroups', 'web.home.usersAndGroups', false, 400, 2, 19);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (403, 'web.home.rolesAndPermissions', 'web.home.rolesAndPermissions', false, 400, 3, 10);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (404,'web.home.rolesDefinition', 'web.home.rolesDefinition', false, 403, 1, 27, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (405, 'web.home.application_settings', 'web.home.application_settings', false, 400, 4, 25);  
 

 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (4021,'web.usersAndGroups.toolbox.newUser','web.usersAndGroups.toolbox.newUser',true, 402, 1,35,false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (4022,'web.home.admin.user.edit','web.home.admin.user.edit',true, 402, 2,36,false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (4023,'web.usersAndGroups.toolbox.newGroup','web.usersAndGroups.toolbox.newGroup',true, 402, 3, 33, false);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (4024,'web.home.admin.group.edit', 'web.home.admin.group.edit', true, 402, 4, 34, false);

--System Administration
INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (300, 'web.home.systemAdministration', 'web.home.systemAdministration', false, NULL, 3, NULL);
--INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (3001, 'web.home.admin.brands', 'web.home.admin.brands', false, 300, 1, 31);
--INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (3002, 'web.home.admin.models', 'web.home.admin.models', false, 300, 2, 32);
INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (3003, 'web.home.uncaughtException', 'web.home.uncaughtException', false, 300, 1, 43);

--CONNECTORS
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (500, 'web.home.connectors', 'web.home.connectors', false, NULL, 4, NULL);
 --INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (5001, 'web.home.data.import', 'web.home.data.import', false, 500, 1, 12);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (5002, 'web.home.data.lookup_table', 'web.home.data.lookup_table', false, 500, 2, 13);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (5003, 'web.home.data.lookup_table_data', 'web.home.data.lookup_table', false, 5002, 2, 44);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (5004,'web.home.data.import','web.home.data.import',true, 5002, 1,12,false);
 
 --Application License
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (600, 'web.home.license', 'web.home.license', false, NULL, 5, 22);

 --LEGACY
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (700, 'web.home.legacy', 'web.home.legacy', false, NULL, 6, NULL);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (701, 'web.system', 'web.system', false, 700, 1, 14);
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (702, 'web.home.application_settings', 'web.home.application_settings', false, 700, 2, 25);
 
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (703, 'web.home.data.input', 'web.home.data.input', false, 700, 3, 3);

INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id,visible) VALUES (800,'web.home.myaccount','web.home.myaccount',false,NULL,1,24,false);   	

 --Toolboxes

 --Normal toolbox of any Process manager
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (10000,'web.generic.toolbox',null,true,null,1,null);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (10001,'web.processes.toolbox.newProject','web.processes.toolbox.newProject',true, 10000, 1,26);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (10002,'web.processes.toolbox.newForm','web.processes.toolbox.newForm',true, 10000, 2, 29);
 -- #3665 asked to hide pool & process items
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (10003,'web.processes.toolbox.newPool','web.processes.toolbox.newPool',true, 10000, 3, 37);
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (10004,'web.processes.toolbox.newProcessItem','web.processes.toolbox.newProcessItem',true, 10000, 4, 39);	

 --Toolbox used when editing a project because 'new form' option uses a different launcher
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (20000,'web.generic.toolbox',null,true,null,1,null);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (20001,'web.processes.toolbox.newProject','web.processes.toolbox.newProject',true, 20000, 1,26);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (20002,'web.processes.toolbox.newForm','web.processes.toolbox.newForm',true, 20000, 2, 1002);
 -- #3665 asked to hide pool & process items
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (20003,'web.processes.toolbox.newPool','web.processes.toolbox.newPool',true, 20000, 3, 37);
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (20004,'web.processes.toolbox.newProcessItem','web.processes.toolbox.newProcessItem',true, 20000, 4, 39);
 
 --Toolbox used when editing a pool because 'new process item' option uses a different launcher
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (80000,'web.generic.toolbox',null,true,null,1,null);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (80001,'web.processes.toolbox.newProject','web.processes.toolbox.newProject',true, 80000, 1,26);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (80002,'web.processes.toolbox.newForm','web.processes.toolbox.newForm',true, 80000, 2, 29);
 -- #3665 asked to hide pool & process items
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (80003,'web.processes.toolbox.newPool','web.processes.toolbox.newPool',true, 80000, 3, 37);
 -- INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (80004,'web.home.admin.processitem.new','web.home.admin.processitem.new',true, 80000, 4, 1003);
 
 --INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (50000,'web.generic.toolbox',null,true,null,1,null);
 --INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (50001,'web.home.admin.device.new','web.home.admin.device.new',true, 50000, 1, 1001);	

 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (60000,'web.generic.toolbox',null,true,null,1,null);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (60001,'web.usersAndGroups.toolbox.newUser','web.usersAndGroups.toolbox.newUser',true,60000,1,35);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (60002,'web.usersAndGroups.toolbox.newGroup','web.usersAndGroups.toolbox.newGroup',true,60000,2,33);
 
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (70000,'web.generic.toolbox',null,true,null,1,null);
 INSERT INTO sys.acme_tree_menu (id, i18n_title, i18n_description, toolbox, parent_id, "position",  launcher_id) VALUES (70001,'web.home.data.import','web.home.data.import',true,70000,1,12);
 

--------------------------------------------------------
--CONFIGURE VIEWS
--------------------------------------------------------
--by default all views will show the menu and no toolbox
update sys.acme_views set show_menu=true,show_toolbox=false,show_navigator=true;

-- Processes Manager
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=17;

--Project
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=26;
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=20000 where id=28;

--Forms
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=29;
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=30;

-- Pool
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=37;
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=80000 where id=38;

-- Process Item
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=39;
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=10000 where id=40;

-- Devices
-- update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=50000 where id=15;

-- Users and Groups
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=60000 where id=19;

--Form editor
update sys.acme_views set show_menu=true,show_toolbox=false,show_navigator=false where id=4;

--Lookup Table
update sys.acme_views set show_menu=true,show_toolbox=true,show_navigator=true, toolbox_root=70000 where id=13;

--------------------------------------------------------
--Disable some parts of the menu
--------------------------------------------------------
--Disable the "System Administration" Menu
--update sys.acme_tree_menu set visible=false where id=300;
--update sys.acme_tree_menu set visible=false where id>=3001 and id<=3002;

--Disable the "Legacy" Menu
update sys.acme_tree_menu set visible=false where id=700;
update sys.acme_tree_menu set visible=false where id>=700 and id<=703;

--Disable the devices sub menu from the administration page
--update sys.acme_tree_menu set visible=false where id=401;

--------------------------------------------------------
--UPDATE SEQUENCES
--------------------------------------------------------
--Set the sequences to the max value so new menu can be added from the GUI. Otherwise, we will have a potential duplicate key problems
select setval('sys.seq_acme_launchers',(select max(id) from sys.acme_launchers));
select setval('sys.seq_acme_tree_menu',(select max(id) from sys.acme_tree_menu));
select setval('sys.seq_acme_views',(select max(id) from sys.acme_views));

 
