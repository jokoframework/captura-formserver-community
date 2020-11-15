define(["jquery", "acme", "jquery-ui", "acme-ui", "cruds/crud-utils","constants"], function($, acme, $ui, acme_ui, crudutils,constants) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.cruds.parameter.title',
	                                          'admin.cruds.parameter.cols.auths',
	                                         ]);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*960/1024;
		$("#parametersGrid").jqGrid('setGridWidth', desireWidth, true);				
	};
	
	var beforeShowSystemParametersForm = function(form, type){
		if(type === 'ADD'){
			$('#tr_authLevel').show();	
		} else {
			$('#tr_authLevel').hide();
		}
	};
	
	var parameterAfterSubmit = function(response, postdata) { 
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
			//if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.parameter.list")) {
				var parametersCrud = crudutils.Crud({entity:'systemParameters', 
					caption:I18N('admin.cruds.parameter.title'), 
					pathPrefix : "/admin/",
					gridId:'parametersGrid', 
					pagerId:'parametersPager',
					beforeShowForm: beforeShowSystemParametersForm,
					afterSubmit: parameterAfterSubmit,
					add : false,
					del : false
				});
				parametersCrud.showGrid();
				resizeTables();
			//}end if(acme...
			dfd.resolve();
		});
		return dfd.promise();
	};
	
	
	return pkg;
});