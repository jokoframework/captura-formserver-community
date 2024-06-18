require([ "jquery", "acme", "registration/registrationManager"], function($, acme,registration) {
	
	
	
	
	$(function() {
		var messageManager=acme.messageManager("activationMessage");
		$("#refreshCaptchaButton").click(function(){
			registration.refreshCaptcha('captcha_img');
		});
		
		$('#acceptBtn').click(function(){
			
			messageManager.hide();
			$("#progressBlock").show();
			acme.deactivateButtons('acceptBtn');

			var captcha=$("#captcha").val();
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += "/account/activation.ajax";
			ajaxRequest.data = {'captcha':captcha};
			ajaxRequest.success = function(response){
				if(response.success){
					messageManager.success(response.unescapedMessage);
					$("#activationForm").hide();
					$("#acceptBtn").hide();
				} else {
					messageManager.error(response.unescapedMessage);
				}
				
			};
			$.ajax(ajaxRequest).always(function(){
				$("#progressBlock").hide();
				acme.activateButtons('acceptBtn');
			});
		});
	});
});