define(["jquery", "acme", "acme-ui",  "webforms/formUtils"],  function($, acme, acme_ui, formUtils) {
	
	var pkg = {};
	
	pkg.Manager = function(params){
	
		var that = {};
		
		var formId = params.formId;
		
		var url = params.url;
		var resetMessage=acme.messageManager('resetMessage');
		
		var success = function(res) {
			$("#progressBlock").hide();
			if(res.success){
				$("#mainDiv").hide();
				resetMessage.success(res.unescapedMessage);
				$('#okButton').hide();
			} else {
				resetMessage.error(res.unescapedMessage);
				
			}
		};
	
		that.submit = function() {
			$("#progressBlock").show();
			resetMessage.hide();
			formUtils.submit(formId, url, {
				customAjaxCallback : null,
				successCallback : success,
				sendJSON : false
			});
		};
		
		return that;
	};
	
	return pkg;
});