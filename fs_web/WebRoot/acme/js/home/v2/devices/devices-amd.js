define([ "jquery", "acme", "jquery-ui", "acme-ui", "constants" ], function($, acme, $ui,
		acme_ui, constants)   {
	
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load([ 'web.generic.ok', 
	                                           'web.generic.action',
	                                           'web.devices.blacklist.caption',
	                                           'web.home.admin.devices.field.identifier',
	                                           'web.home.admin.devices.field.brand',
	                                           'web.home.admin.devices.field.model',
	                                           'web.devices.removeFromBlackList']);

	var blackListGrid;
	
	var i18n = acme.I18N_MANAGER.getMessage;
	
	var jsonReader =  { 
		    root: "data", 
		    repeatitems: false,
		    page: "pageNumber",
		    total: "totalPages",
		    records: "totalCount",
		    id: "0"
	};
	
	var blackListGridActionsFormatter = function(cellvalue, options, obj) {
		var id = obj.id;
		var link = '<a class="removeBlackList" href="javascript:void(0)" data-id="' +id+'">' +i18n('web.devices.removeFromBlackList')  + '</a>';
		return link;
	};
	
	var blackListGridComplete  = function () {
		$('.removeBlackList').click(function(){
			var id = $(this).data('id');
			removeFromBlacklist(id);
		});
	};
	
	var removeFromBlacklist = function(deviceId){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/devices/removeFromBlacklist.ajax";
		ajaxRequest.data = {'deviceId':deviceId, remove: true};
		ajaxRequest.success = function(response) {
			if(response.success){
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
			}
			if(blackListGrid != null){
				blackListGrid.trigger("reloadGrid");
			}
			
		};
		$.ajax(ajaxRequest);
	};
	
	var initTable=function(){
		blackListGrid=$("#blackListGrid").jqGrid({
			url : acme.VARS.contextPath + '/devices/blacklist.ajax',
			datatype: 'json',
			jsonReader: jsonReader,
		    mtype: 'POST',
		    gridComplete : blackListGridComplete,
		    viewrecords: true,
		    rownumbers: true,
	        autowidth : true,
		    rowNum : 5,
	        rowList : [5,10,15],
	        pager:'#blackListGrid_pager',
	        width : '100%',
			height : '100%',	      
			sortname:'identifier',
			caption: i18n('web.devices.blacklist.caption'),
			colNames:['id',
			          i18n('web.home.admin.devices.field.identifier'),
			          i18n('web.home.admin.devices.field.brand'),
			          i18n('web.home.admin.devices.field.model'),
			          i18n('web.generic.action')
			          ],
			colModel:[{name:'pk',index:'id',width:0,hidden:true,sortable:false},
			          {name:'identifier',index:'identifier', width:100},
			          {name:'brand',index:'brand'},
			          {name:'model',index:'model'},
			          {name:'action', index:'action', width:200,fixed : true, align : "center", formatter : blackListGridActionsFormatter, sortable : false}
			]
		}).navGrid('#blackListGrid_pager',{edit:false,add:false,del:false,search:false});
		
	};
	
	pkg.start = function() {
		var dfd = $.Deferred();
		$.when(i18nPromise).then(function(){
			require(["order!jqgrid/grid.locale-" + acme.I18N_MANAGER.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
				initTable();
				dfd.resolve();
			});
		});
		return dfd.promise();
	};
		
	return pkg;
});
