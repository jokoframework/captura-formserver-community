require([ "jquery", "login/loginManager","acme"], function($,login,acme) {
	
	var loginManager = login.Manager({formId:'#login', url:'/login'});
	
	
	$(function() {
		
		$('#submitLogin').click(function() {
			loginManager.submit();
			return false;
		});
		
		loginManager.init();
	});
	
});