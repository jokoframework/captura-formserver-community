require([ "jquery", "acme", "registration/registrationManager"], function($, acme, registration) {
	
	var registrationManager = registration.Manager({formId:'#user', url:'/registration/register.ajax'});
	
	var i18nPromise = acme.I18N_MANAGER.load([ "web.registration.title" ]);
	
	
	
	
	$(function() {
		$("#registerMenu").addClass("active");
		$('#submitUser').click(function() {
			registrationManager.submit();
			return false;
		});
		$("#refreshCaptchaButton").click(function(){
			registration.refreshCaptcha('captcha_img');
		});
		
		$('input[type=password]').bind('copy paste', function(e) {
			 e.preventDefault();
		});
		
		$.when(i18nPromise).then(function(){
			var title = acme.I18N_MANAGER.getMessage("web.registration.title");
			$("title").html(title);
		});
	});
	
});