require([ "jquery", "acme", "registration/registrationManager"], function($, acme,registration) {
	
	
	
	
	$(function() {
		var messageManager=acme.messageManager("recoveryMessage");
		var mailInput = $('#mail').focus();
		$("#refreshCaptchaButton").click(function(){
			registration.refreshCaptcha('captcha_img');
		});
		
		$('#continueBtn').click(function(){
			
			messageManager.hide();
			$("#progressBlock").show();
			acme.deactivateButtons('continueBtn');
			
			var mail = mailInput.val();
			var captcha=$("#captcha").val();
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += "/account/recover.ajax";
			ajaxRequest.data = {'mail':mail,'captcha':captcha};
			ajaxRequest.success = function(response){
				if(response.success){
					messageManager.success(response.unescapedMessage);
					$("#recoveryForm").hide();
					$("#continueBtn").hide();
				} else {
					if(response.obj === "INACTIVE"){
						$('#resendActivation').show();
						messageManager.info(response.unescapedMessage);
					} else {
						messageManager.error(response.unescapedMessage);
						mailInput.attr('disabled', null);
					}

				}
				
			};
			$.ajax(ajaxRequest).always(function(){
				$("#progressBlock").hide();
				acme.activateButtons('continueBtn');
			});
		});
		
		$('#resendActivationLink').click(function(){
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/registration/resend.ajax';
			ajaxRequest.data = 'mail=' + $('#mail').val();
			ajaxRequest.success = function(response) {
				var resultMessageDiv = $('#recoveryMessage');
				$('#resendActivation').hide();
				if (response.success === true) {
					$("#recoveryForm").hide();
					$("#continueBtn").hide();
					messageManager.success(response.unescapedMessage);
				} else {
					messageManager.error(response.unescapedMessage);
				}
			};
			$.ajax(ajaxRequest);
		});
		
		$('#cancelResendActivationLink').click(function(){
			$('#resendActivation').hide();
			messageManager.hide();
		});
	});
});