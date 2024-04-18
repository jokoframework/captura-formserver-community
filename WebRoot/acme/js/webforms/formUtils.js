define(["jquery", "acme", "jquery-ui" ], function($, acme, jqueryui) {

	var that = {};

	var customAjaxCallback = null;

	var successCallback = null;

	var formId = null;

	var sendJSON = true;

	var defaultAjaxCallback = function(res) {
		if (customAjaxCallback != null) {
			customAjaxCallback(res);
		} else {
			$(formId).find("span.validation_msg").hide();
			
			if(!res.success) {
				var messages;
				if(messagesProperty){
					messages = res.content[messagesProperty];
				}else{
					messages = res.content;
				}
				for (var prop in messages) {
					if(messages.hasOwnProperty(prop)){
						$('#' + prop + '_validation_msg').show();
						$('#' + prop + '_validation_msg').html(messages[prop]);
					}
				}
			}
			
			if (successCallback != null) {
				successCallback(res);
			}
		}
	};

	that.submit = function(id, url, options) {
		formId = id;
		if (options) {
			customAjaxCallback = options.customAjaxCallback;
			successCallback = options.successCallback;
			sendJSON = options.sendJSON === true;
			messagesProperty = options.messagesProperty;
		}
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += url;
		if (sendJSON) {
			ajaxRequest.contentType = 'application/json; charset=utf-8';
			ajaxRequest.data = JSON.stringify($(formId).serializeObject());
		} else {
			ajaxRequest.data = $(formId).serializeObject();
		}
		ajaxRequest.success = defaultAjaxCallback;
		$.ajax(ajaxRequest);
	};

	return that;
});