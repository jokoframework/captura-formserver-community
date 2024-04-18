define(["jquery", "acme", "jquery-ui","acme-ui", "order!jqgrid/grid.locale-en", "order!jqgrid/jqGrid"], 
	function($, acme, $ui, acme_ui, qjlocale ,jqGrid) {
	
	var pkg = {};
	
	pkg.start = function(){
		var bytesFormatter = function(cellvalue, options, rowObject) {
			if(cellvalue == 0) return '0 Bytes';
    	   var k = 1000;
    	   var dm = 3;
    	   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    	   var i = Math.floor(Math.log(cellvalue) / Math.log(k));
    	   return (cellvalue / Math.pow(k, i)).toPrecision(dm) + ' ' + sizes[i];
		};
		
		$('#stats').jqGrid({
			url : acme.VARS.contextPath + '/admin/dataUsage.ajax',
			datatype: 'json',		   
			width : '100%',
			height : '100%',
		    caption: 'Data usage',
		    viewrecords: true,
		    rownumbers: true,
            rowNum: -1, // if we don't set this parameter, it will only show 20 rows by default. see CAP-110
	        autowidth : true,
	        sortname: 'uploadedData',
	        sortorder: 'desc',
			colNames:['id', 'nombre',
			          'activa', 
			          'bytes subidos',
			          ],
			colModel:[{name:'appId', index:'id', sortable:true, search: false},
			          {name:'appName',index:'name', sortable:true},
			          {name:'active',index:'active',sortable:true, firstsortorder:'desc', search: false},
			          {name:'uploadedData',index:'uploadedData',sortable:true, formatter: bytesFormatter, search: false}]
		}).filterToolbar({searchOnEnter: false});
	};
	
	return pkg;
});
