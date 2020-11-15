require([ "jquery", "acme"], function($, acme) {
	
	var i18nPromise = acme.I18N_MANAGER.load([ "web.index.title" ]);
	
	$(function() {
		$.when(i18nPromise).then(function(){
			var title = acme.I18N_MANAGER.getMessage("web.index.title");
			$("title").html(title);
		});
	});
	
});