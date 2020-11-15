define(["jquery", "acme", "jquery-ui", "acme-ui", "cruds/crud-utils","constants"], function($, acme, $ui, acme_ui, crudutils,constants) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.cruds.role.title',
	                                          'admin.cruds.role.cols.auths',
	                                         ]);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*960/1024;
		$("#rolesGrid").jqGrid('setGridWidth', desireWidth, true);				
	};
	
	var beforeShowRolesForm = function(form, type){
		if(type === 'ADD'){
			$('#tr_authLevel').show();	
		} else {
			$('#tr_authLevel').hide();
		}
	};
	
	var rolesCustomFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'settings',
				'authLink',
				I18N('admin.cruds.role.cols.auths'),
				function(){
					return true;
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:50px;" >'+actions+'</div>';
		//return '';
	};
	
	var rolesGridComplete = function(){
		$('.authLink').click(
				function(){
					var roleid = $(this).attr('rowid');
					//launch the view to administer the access rights for a given role
					acme.LAUNCHER.launch(constants.LAUNCHER_IDS.role_permissions,roleid);
				}	
		);
	};
	
	var roleAfterSubmit = function(response, postdata) { 
		res=$.parseJSON(response.responseText);	
		if(res.success===true) {
			acme_ui.HTML_BUILDER.notifySuccess(res.title, res.message);
		} else {
			acme_ui.HTML_BUILDER.notifyError(res.title, res.message);
		}	
		
		return [res.success, res.message];
	};
	
	pkg.resized=function(){
		resizeTables();
	};
	
	pkg.start = function(){
		var dfd = $.Deferred();
		$.when(i18nPromise,crudutils.loaded).then(function(){
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.role.list")) {
				var rolesCrud = crudutils.Crud({entity:'roles', 
					caption:I18N('admin.cruds.role.title'), 
					gridId:'rolesGrid', 
					pagerId:'rolesPager',
					beforeShowForm: beforeShowRolesForm,
					customFormatter: rolesCustomFormatter,
					gridComplete: rolesGridComplete,
					afterSubmit: roleAfterSubmit
				});
				rolesCrud.showGrid();
				resizeTables();
			}
			dfd.resolve();
		});
		return dfd.promise();
	};
	
	
	return pkg;
});