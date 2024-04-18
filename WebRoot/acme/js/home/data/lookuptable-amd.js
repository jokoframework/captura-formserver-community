define([ "jquery", "acme", "jquery-ui", "acme-ui", "constants" ], function($, acme, $ui,
		acme_ui, constants) {
	
	var pkg = {};
	var i18nPromise = acme.I18N_MANAGER.load([ 'web.generic.action','web.home.data.lookup_table','web.lookup.cols.id',
	                                           'web.lookup.cols.label','web.lookup.cols.isRemote','web.lookup.cols.remoteIdentifier',
	                                           'lookupTable.delete.confirmation.title','lookupTable.delete.confirmation.message',
	                                           'web.lookup.actions.delete.tooltip','web.lookup.actions.view.tooltip',
	                                           'web.lookup.model.remote.true', 'web.lookup.model.remote.false'
	                                           ]);

	var lookupGrid;
	
	var i18n = acme.I18N_MANAGER.getMessage;
	
	var jsonReader =  { 
		    root: "data", 
		    repeatitems: false,
		    page: "pageNumber",
		    total: "totalPages",
		    records: "totalCount",
		    id: "0"
	};
	
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*710/1024;
		$("#lookupTableGrid").jqGrid('setGridWidth', desireWidth, true);
		
	};
	
	var loadLookupDefinition = function(lookupId){
      	var dfd = $.Deferred();
		var promise = dfd.promise();
		var ajax = acme.AJAX_FACTORY.newInstance();
		ajax.url += '/api/lookupTable/definition/' + lookupId;
		ajax.type='GET';
		return $.ajax(ajax);
	
	};
	
	var listLookupTables=function(){
		var ajax = acme.AJAX_FACTORY.newInstance();
		ajax.url +=  '/api/lookupTable/definition/listAll';
		ajax.type='GET';
		return $.ajax(ajax);
	};
	
	var listLookupTableData=function(lookupId){
		var ajax = acme.AJAX_FACTORY.newInstance();
		ajax.url +=  '/api/lookupTable/data/' + lookupId;
		ajax.type='GET';
		return $.ajax(ajax);
	};
	
	//TODO review 24/07/2013
	var listLookupTableColumnData = function(lookupId, columns){
		var dfd = $.Deferred();
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		var requestURL =  '/api/lookupTable/columns/data/' + lookupId + '?';
		for(var i = 0; i < columns.length; i++){
			requestURL += "columns=" + columns[i] + "&";
		}
		ajaxRequest.url += requestURL;
		ajaxRequest.type = 'GET';
		ajaxRequest.success = function(response){
			if(response.success){
				dfd.resolve({success: true, values : response.obj});	
			} else {
				dfd.resolve({success: false, values : null});
			}
		};
		ajaxRequest.error = function(){
			dfd.resolve({success: false});
		};
		$.ajax(ajaxRequest);
		return dfd.promise();
	};
	
	var doDelete=function(lookupId){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/lookuptable/delete.ajax';
		ajaxRequest.data={lutId:lookupId};
		ajaxRequest.success = function(obj){
			if(obj.success) {
				acme_ui.HTML_BUILDER.notifySuccess(obj.message,'');
				$("#lookupTableGrid").trigger("reloadGrid");
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.unescapedMessage);
			}
		};
		$.ajax(ajaxRequest);
	};
	var lookupActionsFormatter = function(cellvalue, options, rowObject) {
		var lookupId=options.rowId;
		var actions=acme_ui.iconForTable(
				lookupId,
				'edit',
				'viewLookupLink',
				i18n('web.lookup.actions.view.tooltip'),
				function(){
					//return true;
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.rest.lookupTables.read");
				}
				
		);
		actions+=acme_ui.iconForTable(
				lookupId,
				'remove',
				'deleteLookupLink',
				i18n('web.lookup.actions.delete.tooltip'),
				function(){
					//return true;
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.rest.lookupTables.modify");
				}
				
		);
		
		return '<div class="actionButtonOnTable" style="width:95px;" >'+actions+'</div>';
	};
	
	var lookupRemoteFormatter = function(cellvalue, options, rowObject) {
		var retVal;
		
		if (rowObject.acceptRESTDMLs) {
			retVal = i18n("web.lookup.model.remote.true");
		} else {
			retVal = i18n("web.lookup.model.remote.false");
		}
		
		return retVal;
	};

	
	var initTableDelegates=function(){
		$("#lookupTableGrid").on("click", ".deleteLookupLink", function(event){
			var lookupId = $(this).attr('rowid');
			acme_ui.confirmation(
				i18n('lookupTable.delete.confirmation.title'),i18n('lookupTable.delete.confirmation.message'),function(){
					doDelete(lookupId);
				}	
			);

			
		});
		$("#lookupTableGrid").on("click", ".viewLookupLink", function(event){
			var lookupId = $(this).attr('rowid');
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.lookupData, {'lookupTableId':lookupId});
		});
	};
	var initTable=function(){
		lookupGrid=$("#lookupTableGrid").jqGrid({
			url : acme.VARS.contextPath + '/cruds/projects/lookupTable/paging/read.ajax',
			datatype: 'json',
			jsonReader: jsonReader,
		    mtype: 'POST',
		    viewrecords: true,
		    rownumbers: true,
	        autowidth : true,
		    rowNum : 5,
	        rowList : [5,10,15],
	        pager:'#lookupTableGrid_pager',
	        width : '100%',
			height : '100%',	      
			sortname:'name',
			caption:i18n('web.home.data.lookup_table'),
			colNames:[i18n('web.lookup.cols.id'),
			          i18n('web.lookup.cols.label'),
			          i18n('web.lookup.cols.isRemote'),
			          i18n('web.lookup.cols.remoteIdentifier'),
			          i18n('web.generic.action')
			          ],
			colModel:[{name:'pk',index:'id', width:0,hidden:true,sortable:false},
			          {name:'name',index:'name', width:150},
			          {name:'acceptRESTDMLs',index:'acceptsRESTDML', width:50, formatter:lookupRemoteFormatter},
			          {name:'identifier',index:'identifier', width:100},
			          {name:'action', index:'action', width:150,fixed:true, align:"center", formatter:lookupActionsFormatter,sortable:false}
			]
		}).navGrid('#lookupTableGrid_pager',{edit:false,add:false,del:false,search:false});
		
		initTableDelegates();
		resizeTables();
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

	pkg.loadLookupDefinition=loadLookupDefinition;
	pkg.listLookupTables=listLookupTables;
	pkg.listLookupTableData=listLookupTableData;
	pkg.listLookupTableColumnData=listLookupTableColumnData;
	return pkg;
});