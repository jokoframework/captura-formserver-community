define(["jquery", "acme", "jquery-ui","acme-ui", "order!jqgrid/grid.locale-en", "order!jqgrid/jqGrid"], 
	function($, acme, $ui, acme_ui, qjlocale ,jqGrid) {
	
var pkg = {};
	
	pkg.start = function(){
		$('#failedDocuments').jqGrid({
			url : acme.VARS.contextPath + '/admin/failedDocuments.ajax',
			datatype: 'json',		   
			width : '100%',
			height : '100%',
		    caption: 'Failed Documents',
		    viewrecords: true,
		    rownumbers: true,
	        autowidth : true,
            rowNum: -1, // if we don't set this parameter, it will only show 20 rows by default. see CAP-110
            colNames:['upload id', 'aplicación', 'fecha',
			          'usuario'/*, 
			          'error',*/
			          ],
			colModel:[{name:'id', index:'id', sortable:false},
			          {name:'applicationName',index:'applicationName', sortable:false},
			          {name:'createdAt',index:'createdAt', sortable:false},
			          {name:'userEmail',index:'userEmail',sortable:false}/*,
			          {name:'errorDescription',index:'errorDescription',sortable:false}*/]
		});
		
		pkg.showNotificationsStatus();
		
		$('#disableNotifications').click(function() {
			var that = this;
			var disable = !$(this).is(":checked");
			$(this).attr('disabled', 'disabled');
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/admin/disableNotifications.ajax';
			ajaxRequest.data = {"disable": disable};
			ajaxRequest.complete = function() {
				$(that).removeAttr('disabled');
			};
			ajaxRequest.success = function(res) {
				if (res.success) {
					$(that).next('span').text(res.message);
				}
			};
			$.ajax(ajaxRequest);
		});
	};
	
	pkg.showNotificationsStatus = function() {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/admin/getNotificationsStatus.ajax';
		ajaxRequest.success = function(res) {
			if (res.success) {
				var disabled = res.obj === "true";
				$('#disableNotifications')
				.prop('checked', !disabled)
				.next('span').text(res.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	
	return pkg;
	
});