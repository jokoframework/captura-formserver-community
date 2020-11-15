define(["jquery", "acme", "jquery-ui", "acme-ui"], function($, acme, $ui, acme_ui) {
	
	var that = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(["web.generic.error",
	                                          "web.generic.unexpectedException"]);
	
	var I18N = acme.I18N_MANAGER.getMessage;

	
	that.start = function(){
		$("#uploadIframe").load(function() {
			var iframe = window.document.getElementById("uploadIframe");
			// http://stackoverflow.com/questions/1088544/javascript-get-element-from-within-an-iframe
			var document = iframe.contentDocument || iframe.contentWindow.document;
			var responseStr = $(document).find('body').html();
			if(responseStr){
				try {					
					var response = JSON.parse(responseStr);
					acme_ui.dialog(response.message, {title: response.title});
					if(response.success === true){
						var license = response.content.license;
						var validUntil = response.content.validUntil;
						// $("#applicationIdDiv").html(license.applicationId);
						$("span#maxUsers").html(license.maxUsers);
						$("span#maxDevices").html(license.maxDevices);
						$("span#license_owner").html(license.owner);
						$("span#validUntil").html(validUntil);
					}												
				} catch (err) {
					$.when(i18nPromise).then(function(){
						acme_ui.dialog(I18N("web.generic.unexpectedException"), {title : I18N("web.generic.error")});
					});
				}
			}
		});
		
		$("#savePreferences").click(function(){
			var data = {
				applicationName : $("#applicationName").val(),
				defaultLanguage : $("#defaultLanguage").val()
			};
			
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += "/application/settings/save.ajax";
			ajaxRequest.data = data;
			ajaxRequest.success = function(response){
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			};
			ajaxRequest.error = function(jqXHR, textStatus, errorThrown){
				acme_ui.HTML_BUILDER.notifyError(I18N("web.generic.error"), I18N("web.generic.unexpectedException"));
			};
			$.ajax(ajaxRequest);
		});
	};
	
	return that;
});