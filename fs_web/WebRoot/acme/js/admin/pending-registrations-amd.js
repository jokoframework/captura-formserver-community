define(["jquery", "acme", "jquery-ui", "acme-ui", "order!jqgrid/grid.locale-en", "order!jqgrid/jqGrid", "cruds/crud-utils"], function($, acme, $ui, acme_ui, qjlocale ,jqGrid, crudutils) {
	
	var pkg = {};
	
	var I18N =  acme.I18N_MANAGER.getMessage;
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.root.pendingRegistration.grid.caption',
	                                          'web.root.pendingRegistration.grid.id',
	                                          'web.root.pendingRegistration.grid.mail',
	                                          'web.root.pendingRegistration.grid.name',
	                                          'web.root.pendingRegistration.grid.lastName',
	                                          'web.root.pendingRegistration.grid.time',
	                                          'web.root.pendingRegistration.grid.options',
	                                          'web.root.pendingRegistration.accept',
	                                          'web.generic.cancel']);
	
	var jsonReader =  { 
	    root: "data", 
	    repeatitems: false,
	    page: "pageNumber",
	    total: "totalPages",
	    records: "totalCount",
	    id: "0"
	};
	
	var optionsFormatter = function(cellvalue, options, rowObject){
		return '<a href="' + acme.VARS.contextPath + "/api/public/activate/" + rowObject.mail + "/" + rowObject.activationToken + '" target="_blank">'+ I18N('web.root.pendingRegistration.accept') + '</a>'
		+ ' | <a href="javascript:void(0)" data-id="' + rowObject.id + '" class="cancelRegistration">' + I18N('web.generic.cancel') + '</a>';;
	};
	
	var cancelPendingRegistration = function(id){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/admin/cancelPendingRegistration.ajax";
		ajaxRequest.data = "id=" + id;
		ajaxRequest.success = function(response) {
			if(response.success){
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
			}
			$('#pendingRegistrationGrid').trigger("reloadGrid");
		};
		$.ajax(ajaxRequest);
	};
	
	var gridComplete = function(){
		$(".cancelRegistration").click(function(){
			id = $(this).data("id");
			cancelPendingRegistration(id);
		});
	};
	
	pkg.start = function(){
		$.when(i18nPromise).then(function(){
			$('#pendingRegistrationGrid').jqGrid({
				url : acme.VARS.contextPath + '/admin/pendingRegistrations/list.ajax',
				datatype: 'json',
				jsonReader: jsonReader,
			    mtype: 'POST',
			    pager: '#pendingRegistrationPager',
			    viewrecords: true,
			    rownumbers: true,
		        autowidth : true,
		        rowNum : 5,
		        rowList : [5,10,15],
				width : '100%',
				height : '100%',
			    caption: I18N('web.root.pendingRegistration.grid.caption'),
			    viewrecords: true,
			    gridComplete : gridComplete,
				colNames:[I18N('web.root.pendingRegistration.grid.id'),
				          I18N('web.root.pendingRegistration.grid.mail'), 
				          I18N('web.root.pendingRegistration.grid.name'),
				          I18N('web.root.pendingRegistration.grid.lastName'), 
				          I18N('web.root.pendingRegistration.grid.time'),
				          I18N('web.root.pendingRegistration.grid.options')],
				colModel:[{name:'id', index:'id', sortable:false},
				          {name:'mail',index:'mail', sortable:false},
				          {name:'name',index:'name',sortable:false},
				          {name:'lastName',index:'lastName',sortable:false},
				          {name:'registrationTime', index:'registrationTime', formatter: crudutils.timestampFormatter, sortable:false},
				          {name:'options', index:'options', formatter: optionsFormatter, sortable:false}]
			}).navGrid('#pendingRegistrationPager', {edit:false,add:false,del:false,search:false});
		});
	};
	
	return pkg;
});