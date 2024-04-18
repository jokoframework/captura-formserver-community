define(["jquery", "acme", "jquery-ui", "acme-ui"], function($, acme, $ui, acme_ui) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.views.uncaughtException.cols.id', 'admin.views.uncaughtException.cols.exceptionType', 'admin.views.uncaughtException.cols.offendingClass', 'admin.views.uncaughtException.cols.userId', 'admin.views.uncaughtException.cols.inserTime', 'admin.views.uncaughtException.cols.stackTrace', 'admin.views.uncaughtException.cols.url', 'admin.views.uncaughtException.cols.userAgent', 'admin.views.uncaughtException.msg.noStackTrace']);
	
	var i18n = acme.I18N_MANAGER.getMessage;
	
	var cleanUp=function(){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/admin/uncaughtException/clean.ajax';
		return $.ajax(ajaxRequest);
	};
	
	pkg.start = function(){
		
		$("#btn_cleanup").click(
				function(){
					acme_ui.confirmation("Clean exceptions","Are you sure you want to cleanup the unexpected exceptions log ?",function(){
						var promise=cleanUp();
						$.when(promise).then(function(obj){
							if(obj.success) {
								acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
								$("#uncaughtExceptionGrid").trigger("reloadGrid");
							} else {
								acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
							}
						});
						
					});					
				}
				
		);
		
		$.when(i18nPromise).then(function(responseRoles){
			require(["order!jqgrid/grid.locale-" + acme.I18N_MANAGER.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
				var jsonReader =  { 
					    root: "data", 
					    repeatitems: false,
					    page: "pageNumber",
					    total: "totalPages",
					    records: "totalCount",
					    id: "0"
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
			    
			    
			    var imageFormatter = function(cellvalue, options, rowObject){
					var rowId = options.rowId;
					var name = options.colModel.name;
					if(cellvalue){
						return '<a href="' + name + '" data="' + cellvalue + '" class="imageLink picture">'+i18n('admin.views.uncaughtException.cols.stackTrace')+'</a>';
					} else {															
						return i18n('admin.views.uncaughtException.msg.noStackTrace');
					}
				};
				
				var showImagePopup = function(data, field) {
					$("#uncaughtExceptionPopup").text(data);
					$("#uncaughtExceptionPopup").dialog({
						title: i18n('admin.views.uncaughtException.cols.stackTrace'),
						width: 700,
						height: 500,
						modal:true,
						buttons:{"Ok": function() { $(this).dialog("close"); }}
					});
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
				
			    //i18n('web.data.upload.serverError')
				$("#uncaughtExceptionGrid").jqGrid({ 
					url: acme.VARS.contextPath + '/admin/uncaughtException/paging/read.ajax', 
					datatype: "json",
					jsonReader: jsonReader,
					autowidth: true,
					width:"100%",
					height:"100%",
					colNames:[i18n('admin.views.uncaughtException.cols.id'),i18n('admin.views.uncaughtException.cols.exceptionType'),i18n('admin.views.uncaughtException.cols.offendingClass'),i18n('admin.views.uncaughtException.cols.userId'),i18n('admin.views.uncaughtException.cols.inserTime'),i18n('admin.views.uncaughtException.cols.stackTrace'),i18n('admin.views.uncaughtException.cols.url'),i18n('admin.views.uncaughtException.cols.userAgent')], 
					colModel:[ 
					           {name:'id',index:'id', width:55},
					           {name:'exceptionType',index:'exceptionType', width:200},
					           {name:'offendingClass',index:'offendingClass', width:200}, 
					           {name:'userId',index:'userId', width:80}, 
					           {name:'inserTime',index:'inserTime', width:150, formatter:timestampFormatter}, 
					           {name:'stackTrace',index:'stackTrace', width:100, formatter:imageFormatter, sortable:false}, 
					           {name:'url',index:'url', width:200}, 
					           {name:'userAgent',index:'userAgent', width:200} ], 
					rowNum:5, 
					rowList:[5,10,20,30], 
					pager: '#uncaughtExceptionPager', 
					sortname: 'id', 
					viewrecords: true, 
					sortorder: "asc", 
					caption:"Uncaught Exceptions",
					gridComplete : gridComplete
				}); 
				
				
			});
		});
	};
	
	return pkg;
});
