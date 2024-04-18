define(["jquery", "acme","home/reports/reports-jqgrid-manager" , "home/data/lookuptable-amd"],  function($, acme,reportsJqgrid,lookupMod) {

	var pkg = {};
	
	var jqGridLoadDfd = $.Deferred();
	
	pkg.loaded = jqGridLoadDfd.promise();
	
	var I18N = acme.I18N_MANAGER;
	
	var jsonReader =  { 
	    root: "data", 
	    repeatitems: true,
	    page: "pageNumber",
	    total: "totalPages",
	    records: "totalCount",
	    cell: "cell",
        id: "id"
	 };
	
	var colNames = null;
	
	//var cols = null;
	
	var sortname = null;
	var sortorder = null;
	
	var colModel = null;
	
	var caption = null;
	
	// Custom Formatters -------
	var imageFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var name = options.colModel.name;
		return '<a href="' + name + '" data="' + rowId + '" class="imageLink">show image</a>';
	};
	// -------------------------
	
	var afterInsertRow = function(rowid, rowdata, rowelem){
		
	};
	
	var showImagePopup = function(id, field) {
		$('#imageDialog').dialog('open');
		$('#image').hide('fast');
		$('#image').attr('src', acme.VARS.contextPath + "/lookuptable/data/images/image.jpeg?id=" + id + "&field=" + field);
		$('#loadingDiv').show('fast');
		return false;
	};
	
	var gridComplete = function(){
		$('a.imageLink').click(function(){
			var data = $(this).attr('data');
			var href = $(this).attr('href');
			showImagePopup(data, href);
			return false;
		});
	};
	
	
	
	var buildJqGridModel=function(lookupDef){
		var i=0;
		var colModel=[];
		var colNames=[];
		var fields=lookupDef.fields;
		
		var model={};
		
		if(fields){
			for(i=0;i<fields.length;i++){
				mod={};
				colNames[i]=fields[i].columnName;
				
				colModel[i]={'name':fields[i].columnName, sortable:true, editable:fields[i].editable || true};
				if(fields[i].type==='DATE'){
					colModel[i].formatter=reportsJqgrid.formatter.dateFormatter;
				}
			}
		}
		model.colModel=colModel;
		model.colNames=colNames;
		return model;
	};
	
	
	var beforeSubmit = function(postdata, formid) {
		var id = $('#lookupTableDataGrid').jqGrid('getGridParam', 'selrow');
		var index = $('#lookupTableDataGrid').jqGrid('getInd', id);
		postdata.gridId = index;
		return [true, ''];
	};
	
	var onclickDelSubmit = function(params) {
		var id = $('#lookupTableDataGrid').jqGrid('getGridParam', 'selrow');
		var index = $('#lookupTableDataGrid').jqGrid('getInd', id);
		
		return {gridId : index};
	};
	
	require(["order!jqgrid/grid.locale-" + I18N.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
	
		pkg.showGrid = function(lookupId) {
			$('#dataLookupTable_grid').jqGrid('GridUnload');
			//FIXME i18n loading
			//$('#dataLookupTable_grid').html('<tr><td><div style="text-align:center;width:100%;" ><br/>loading...</div></td></tr>');
			
			promise = lookupMod.loadLookupDefinition(lookupId);
			$.when(promise).then(function(lookupDef){
				var description=lookupDef.info.name||'Data of lookup #'+lookupId;
				var model=buildJqGridModel(lookupDef);
				var del = true, add = true, edit = true;
				del = add = edit = !(lookupDef.info.acceptRESTDMLs);
				$('#lookupTableDataGrid').jqGrid('GridUnload');
				$('#lookupTableDataGrid').jqGrid({
					datatype : 'json',
					jsonReader : jsonReader,
					sortable : true,
					colNames : model.colNames,
					colModel : model.colModel,
					mtype: 'GET',
					viewrecords: true,
					autowidth : true,
					width : '100%',
					postData: {'lookupTableId':lookupId},
					url : acme.VARS.contextPath +  '/api/lookupTable/data/read.ajax',
					editurl: acme.VARS.contextPath +  '/api/lookupTable/data/' + lookupId + '/edit.ajax',
					caption: description,
					pager: '#lookupTableData_pager',
					rowNum : 5,
					rowList : [5,10, 20, 30],
					prmNames : {id : "lookupTableDataGrid_row_id"},	// this is for edit - CAP-147
					afterInsertRow : afterInsertRow,
					gridComplete : gridComplete
				}).navGrid('#lookupTableData_pager',
						{del:del, add:add, edit:edit}, {beforeSubmit: beforeSubmit, closeAfterEdit:true}, {closeAfterAdd:true}, 
						{onclickSubmit: onclickDelSubmit});
				
				
			});
			return promise;
			
		};
		
		jqGridLoadDfd.resolve();
	});

	pkg.start=function(options){
		var lookupTableId=null;
		if(options && options.lookupTableId) {
			lookupTableId=options.lookupTableId;
		}
		return pkg.showGrid(lookupTableId);
	};
	return pkg;
	
});
