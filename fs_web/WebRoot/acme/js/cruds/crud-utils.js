define(["jquery", "acme", "jquery-ui", "acme-ui"], function($, acme, $ui, acme_ui) {
	
	var pkg = {};
	
	var jqGridLoadDfd = $.Deferred();
	
	pkg.loaded = jqGridLoadDfd.promise();
	
	var I18N =  acme.I18N_MANAGER;
	
	pkg.HELPERS = {};
	
	pkg.HELPERS.genericAfterComplete = function(response, postdata) {
		res=$.parseJSON(response.responseText);
		var alert = null;
		if(res.success===true) {
			alert = acme_ui.HTML_BUILDER.alertSuccess;
		} else {
			alert = acme_ui.HTML_BUILDER.alertError;
		}
		//FIXME hardcoded div id
		alert("message", res.title, res.message);
	};
	
	var timestampFormatter = function(cellValue, options) {
        if(cellValue) {
            return $.fmatter.util.DateFormat(
                '', 
                new Date(+cellValue), 
                'UniversalSortableDateTime', 
                $.extend({}, $.jgrid.formatter.date, options)
            );
        } else {
            return '';
        }
    };
    
    pkg.timestampFormatter = timestampFormatter;
	
	require(["order!jqgrid/grid.locale-" + I18N.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
		
		pkg.Crud = function(params){
			
			var that = {};
			
			var entity = null;
			
			var caption = null;
			
			var gridId = null;
			
			var pagerId = null;
			
			var rowNum = 10;
			
			var width = '100%';
			
			var height = '100%';
			
			var autowidth = true;
			
			var rownumbers = true;
			
			var customBeforeShowForm = null;
			
			var customAfterSubmit = null;
			
			var customBeforeSubmit = null;
			
			var customAfterComplete = null;
			
			var sortname = 'id';
			
			var sortorder = 'asc';
			
			var customFormatter = null;
			// formatter shoud be = function(cellvalue, options, rowObject ) {...}
			
			var gridComplete = null;
			
			var leGrid = null;
			
			var colNames = null;
			
			var colModel = null;
			
			var rowList = [10, 20, 30,50,100];
			
			var add = true;
			
			var edit = true;
			
			var del = true;
			
			var search = false;
			
			var refresh = true;

			var selectedRowId = null;
			
			var postData = {};
			
			var editCaption = '';
			
			var addCaption = '';
			
			var filterToolbar=false;
			
			var pathPrefix = '/cruds/';
			
			var jsonReader =  { 
				    root: "data", 
				    repeatitems: false,
				    page: "pageNumber",
				    total: "totalPages",
				    records: "totalCount",
				    id: "0"
				};
				
			/** Initialization **/
			if(params){
				entity = params.entity;
				gridId = params.gridId;
				pagerId = params.pagerId;
				
				if(params.caption){
					caption=params.caption;
				}
				
				if(params.add === false){
					add = false;
				}
				
				if(params.edit === false){
					edit = false;
				}
				
				if(params.del === false){
					del = false;
				}
				
				if(params.refresh === false){
					refresh = false;
				}
				
				if(params.rowNum){
					rowNum = params.rowNum;
				}
				
				if(params.rowList){
					rowList = params.rowList;
				}
				
				if(params.width){
					width = params.width;
				}
				
				if(params.height){
					height = params.height;
				}
				
				
				if(params.beforeShowForm){
					customBeforeShowForm = params.beforeShowForm;
				}
				
				if(params.afterSubmit){
					customAfterSubmit = params.afterSubmit; 
				}
				
				if(params.beforeSubmit){
					customBeforeSubmit = params.beforeSubmit; 
				}							
				
				if(params.autowidth === false){
					autowidth = false;
				}
				
				if(params.rownumbers === false){
					rownumbers = false;
				}
				
				if(params.sortname){
				    sortname = params.sortname;
				}
				
				if(params.sortorder){
				    sortorder = "asc";
				}
				
				if(params.customFormatter){
					customFormatter = params.customFormatter;
				}
				
				if(params.gridComplete){
					gridComplete = params.gridComplete;
				}
				
				if(params.afterComplete){
					customAfterComplete = params.afterComplete;
				}
				
				if(params.postData) {
					postData = params.postData; 
				}
				
				if(params.colNames) {
					colNames = params.colNames; 
				}
				
				if(params.colModel) {
					colModel = params.colModel; 
				}
				
				if(params.filterToolbar) {
					filterToolbar=params.filterToolbar; 
				}
				
				if(params.pathPrefix){
					pathPrefix = params.pathPrefix
				}
				
				if(params.search === true){
					search = true;
				}
				
			}
			/********************/
			
			var checkParams = function(){
				if(!gridId){
					acme.LOG.error('Grid id has not been defined');
				}
				if(!entity){
					acme.LOG.error('Fatal, no entity defined');
				}
				if(!pagerId){
					acme.LOG.error('No pager id defined');
				}
				if(customBeforeShowForm && !$.isFunction(customBeforeShowForm)){
					acme.LOG.error('Fatal, beforeShowForm is not a function');
				}
				if(customAfterSubmit && !$.isFunction(customAfterSubmit)){
					acme.LOG.error('Fatal, afterSubmit is not a function');
				}
				if(customBeforeSubmit && !$.isFunction(customBeforeSubmit)){
					acme.LOG.error('Fatal, beforeSubmit is not a function');
				}
				if(customFormatter && !$.isFunction(customFormatter)){
					acme.LOG.error('Fatal, customFormatter is not a function');
				}
				if(gridComplete && !$.isFunction(gridComplete)){
					acme.LOG.error('Fatal, gridComplete is not a function');
				}
			};
			
			var loadColumnInfo = function(){
		      	var dfd = $.Deferred();
				var promise = dfd.promise();
				var ajax = acme.AJAX_FACTORY.newInstance();
				ajax.url += pathPrefix + entity + '/columninfo.ajax';
				ajax.success = function(obj){
					if(obj.success){
						colNames = obj.content.colNames;
						colModel = obj.content.colModel;
						if(obj.content.sortname){
							sortname = obj.content.sortname;
						}
						if(obj.content.sortorder){
							sortorder = obj.content.sortorder;
						}
						
						// Actually, if !colModel something is seriously wrong!
						// if(colModel){
						for(var i = 0; i < colModel.length; i++){
							if(colModel[i].formatter === "custom"){
								colModel[i].formatter = customFormatter;
							} else if(colModel[i].formatter === "timestamp"){
								colModel[i].formatter = timestampFormatter;
							}
						}
						// }
						
						if(obj.content.addCaption){
							addCaption = obj.content.addCaption;
						}
						
						if(obj.content.editCaption){
							editCaption = obj.content.editCaption;
						}
						
					}else{
						acme.LOG.error('Fatal Error!');
					}
					dfd.resolve();
				};
				$.ajax(ajax);
				return promise;
			};
			
			var removeErrorMessages = function(){
				for(var i = 0; i<colModel.length; i++){
					$('#div_error_' + colModel[i].index).remove();
				}
			};
			
			var afterShowForm = function(form){
				//REF #307 Creating a new project opens the popup window with improper size
				$('#editmod'+ gridId).width(form.get(0).scrollWidth + 20);
			};
			
			var beforeShowFormFactory = function(type){
				return function(form) {
					removeErrorMessages();
					if($.isFunction(customBeforeShowForm)){
						customBeforeShowForm(form, type);
					}
				};
			};
			
			that.showGrid = function(){
				checkParams();
				var columnInfoPromise;
				if(params.colModel && params.colNames) {
					columnInfoPromise = {success:true};
				} else {
					columnInfoPromise = loadColumnInfo();
				}
				var dfd = $.Deferred();
				var showGridPromise = dfd.promise();
				$.when(columnInfoPromise).then(function(){
					
					leGrid = $('#'+ gridId).jqGrid({
						url : acme.VARS.contextPath + pathPrefix + entity + '/paging/read.ajax',
						editurl: acme.VARS.contextPath + pathPrefix + entity +'/paging/edit.ajax',
						datatype: 'json',
						jsonReader: jsonReader,
					    mtype: 'POST',
					    colNames: colNames,
					    colModel : colModel,
					    pager: '#' + pagerId,
				        rowNum: rowNum,
				        rownumbers: rownumbers,
				        rowList: rowList,
						autowidth : autowidth,
						height:  height,
						width : width,
					    caption: caption,
					    sortorder: sortorder,
					    sortname: sortname,
					    viewrecords: true,
					    gridComplete: function(){ 
					    	selectedRowId = that.selectedRowId = null;
					    	if(gridComplete && $.isFunction(gridComplete)){
					    		gridComplete();
					    	}
					    },
					    postData: postData,
					    onSelectRow: function(id){
					        selectedRowId = that.selectedRowId = id;
					    }
					}).navGrid('#' + pagerId,
						{
							del : del, 
							add : add, 
							edit : edit, 
							search : search, 
							refresh : refresh
						}, 
						{
							reloadAfterSubmit : true, 
							closeAfterEdit : true, 
							beforeShowForm : beforeShowFormFactory('EDIT'), 
							afterShowForm: afterShowForm, 
							afterSubmit : customAfterSubmit, 
							beforeSubmit : customBeforeSubmit, 
							afterComplete : customAfterComplete, 
							editCaption : editCaption
						}, //  default settings for edit
						{
							reloadAfterSubmit : true, 
							closeAfterAdd : true, 
							beforeShowForm : beforeShowFormFactory('ADD'), 
							afterShowForm: afterShowForm, 
							afterSubmit : customAfterSubmit, 
							beforeSubmit : customBeforeSubmit, 
							afterComplete : customAfterComplete, 
							addCaption : addCaption
						}, //  default settings for add
						{
							afterComplete : customAfterComplete
						},  // delete instead that del:false we need this
						{
							sopt: ['eq','ne', 'cn', 'nc'], 
							closeAfterSearch: true
						}, // search options
						{} /* view parameters*/);
					that.leGrid = leGrid;
					if(filterToolbar) {
						leGrid.jqGrid('filterToolbar',{});
					}
					dfd.resolve();
				});
					
				return showGridPromise;
			};
			
			that.refresh = function(){
				if(leGrid != null){
					leGrid.trigger("reloadGrid");
				}
			};
			
			that.postData = function(obj){
				leGrid.setGridParam({postData:obj});
			};
			
			return that;
		};
		
		jqGridLoadDfd.resolve();
	});
	
	return pkg;
});
