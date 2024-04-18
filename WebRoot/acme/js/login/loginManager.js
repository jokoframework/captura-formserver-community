define(["jquery", "acme", "webforms/formUtils"],  function($,acme, formUtils) {
	
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.login.emailEmpty',
	                                          'web.login.passwordEmpty']);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	pkg.Manager = function(params){
		
		var that = {};
		
		var formId = params.formId;
		
		var url = params.url;
		
		var insertErrorMsg = function(msg) {
			$('#messageSection').empty();
			$('#messageSection').text(msg);
			$('#messageBlock').show();
		};
	
		var success = function(res) {
			if (res.success) {
				// keep the progress and don't enable the inputs, we are being redirected
				var url = res.obj.pageToRedirect;
				window.location = url;
			} else {
				$("#progressBlock").hide();
				insertErrorMsg(res.message);
				enableInput();
			}
		};
		
		var required_fields_ok = function() {
			var username=$('#username').val();
			if(!(username && $.trim(username).length > 0)) {
				insertErrorMsg(I18N('web.login.emailEmpty'));
				return false;
			}
			
			var password=$('#password').val();
			if(!(password && $.trim(password).length > 0)) {
				insertErrorMsg(I18N('web.login.passwordEmpty'));
				return false;
			}
			
			return true;
		};
	
		that.init = function() {
			$('#registerLink').css("visibility", "visible");
			$('#signup_forms_panel').css("visibility", "visible");
			$('#submitLogin').css("visibility", "visible");
			$('#forgotPasswordLink').css("visibility", "visible");
			enableInput();
			$("input:text:visible:first").focus();
		};
		
		var enableInput = function(){
			$('#username').removeAttr("disabled");
			$('#password').removeAttr("disabled");
			$('#submitLogin').removeAttr("disabled");
		};
		
		var disableInput = function(){
			$('#username').attr("disabled", "disabled");
			$('#password').attr("disabled", "disabled");
			$('#submitLogin').attr("disabled", "disabled");
		};
		
		that.submit = function() {		
			if(!required_fields_ok()){
				return;
			}
			$('#messageBlock').hide();
			$("#progressBlock").show();
			formUtils.submit(formId, url, {
				customAjaxCallback : null,
				successCallback : success,
				sendJSON : false
			});
			disableInput();
		};
		
		return that;
	};
	
	return pkg;
});
