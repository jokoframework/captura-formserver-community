--This is a script that migrates the data from menu.sub_menu and menu.menu_item to the table sys.acme_modules
--The script keeps the ids of the menu in order to reproduce the same structure


--STEP 1) Migrate the table sub_menu (this are the roots)
insert into sys.acme_modules (id,i18n_title,i18n_description,"position",toolbox) 
select id,i18n_title,i18n_title,"position",false from menu.sub_menu;

--STEP 2) Migrate the table menu_item (this are second level elements on the tree)
insert into sys.acme_modules (id,i18n_title,i18n_description,"position",toolbox,js_amd,url_view,parent_id,action_type) 
select id,i18n_title,i18n_title,"position",false,main_module,url,sub_menu_id,0 from menu.menu_item;