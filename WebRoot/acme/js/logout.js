require([ "jquery"], function($) {	

	$(function() {
		// Redirect t ohome page
		// Ref: http://stackoverflow.com/questions/503093/how-can-i-make-a-redirect-page-in-jquery-javascript
		var pathname = window.location.pathname;
		// Doesn't work on firefox, only in chrome and safari
		// var origin = window.location.origin;
		
		var origin = window.location.protocol + "//" + window.location.host;		
		var pathnameParts = pathname.split("/");
		var appName = "";
		if (pathnameParts.length > 1) {
			appName = pathnameParts[1];
			window.location.href = origin + "/" + appName;
		} else {
			window.location.href = origin;
		}
	});
	
});