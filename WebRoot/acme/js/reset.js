require([ "jquery", "acme", "reset/passwordResetManager"], function($, acme, reset) {

	var resetManager = reset.Manager({formId:'#reset', url:'/password/reset.ajax'});
	
	var i18nPromise = acme.I18N_MANAGER.load(["web.password_reset.title" ]);

	
	$(function() {
		$('#okButton').click(function() {
			resetManager.submit();
			return false;
		});
		
		$.when(i18nPromise).then(function(){
			var title = acme.I18N_MANAGER.getMessage("web.password_reset.title");
			$("title").html(title);
		});
	});
	
});