define([ "jquery", "acme", "jquery-ui", "constants", "acme-ui" ], function($,
		acme, $ui, constants, acme_ui) {

	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(["web.generic.error",
	                                          "web.generic.requiredField",
	                                          "web.generic.unexpectedException",'web.home.myaccount.password.error.confirm',
	                                          "web.validation.user.password"]);
	
	var i18n = acme.I18N_MANAGER.getMessage;
	
	var PASSWORD_MIN_LENGTH = 8;

	var loadSettings = function() {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/settings/loadSettings.ajax';
		return $.ajax(ajaxRequest);
	};

	var changePassword = function(oldPass, newPass) {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/cruds/users/changePassword.ajax";
		ajaxRequest.data = {'oldPass':oldPass,'newPass':newPass};
		return $.ajax(ajaxRequest);
	};
	
	var bindEvents = function() {
		$('#myAppsSettings').click(function() {
			var launcherId = constants.LAUNCHER_IDS.application_settings;
			var p = acme.LAUNCHER.launch(launcherId);
			$.when(p).then(function(module) {
				acme.LOG.debug('application settings loaded');
			});
		});

		$("#savePreferencesButton").click(function() {
			var language = $('#selectOfLanguage').val();
			var defaultApplicationId = $('#defaultApplicationSelect').val();
			var userSettings = {};
			userSettings.language = language;
			userSettings.defaultApplicationId = defaultApplicationId ? defaultApplicationId : null;
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/settings/change.ajax';
			ajaxRequest.contentType = 'application/json; charset=utf-8';
			ajaxRequest.data = JSON.stringify(userSettings);
			ajaxRequest.success = function(resp) {
				if (resp.success) {
					var obj = resp.obj;
					acme_ui.HTML_BUILDER.notifySuccess(resp.title, resp.message);
					if(obj.reload){
						setTimeout(function() { acme.UTIL.reloadWorld(); }, 2000);
					}

				} else {
					acme_ui.HTML_BUILDER.notifyError(resp.title, resp.message);
				}
			};
			$.ajax(ajaxRequest);

		});
		
		$('#saveUserData').click(function() {
			var name = $('#firstName').val();
			var lastName = $('#lastName').val();
			
			if (name.length <= 0) {
				acme_ui.HTML_BUILDER.notifyError(i18n('web.generic.requiredField'));
				$('#firstName').parent().parent().addClass('error');
			} else if (lastName.length <= 0) {
				acme_ui.HTML_BUILDER.notifyError(i18n('web.generic.requiredField'));
				$('#lastName').parent().parent().addClass('error');
				$('#firstName').parent().parent().removeClass('error');
			} else {
				$('#firstName').parent().parent().removeClass('error');
				$('#lastName').parent().parent().removeClass('error');
				
				var ajaxRequest = acme.AJAX_FACTORY.newInstance();
				
				var user = {};
				user.firstName = $('#firstName').val(); 
				user.lastName = $('#lastName').val();
				
				ajaxRequest.url += '/settings/changeUser.ajax';
				ajaxRequest.contentType = 'application/json; charset=utf-8';
				ajaxRequest.data = JSON.stringify(user);
				ajaxRequest.success = function(resp) {
					if (resp.success) {
						var obj = resp.obj;
						acme_ui.HTML_BUILDER.notifySuccess(resp.title, resp.message);
						if(obj.reload){
							setTimeout(function() { acme.UTIL.reloadWorld(); }, 2000);
						}

					} else {
						acme_ui.HTML_BUILDER.notifyError(resp.title, resp.message);
					}
				};
				$.ajax(ajaxRequest);
			}
			
		});
		
		$("#changePasswordBtn").click(function(){
			var oldPass=$("#old_pass").val();
			var newPass=$("#new_pass").val();
			var confirmPass=$("#confirm_pass").val();
			if(newPass.length <= 0){
				acme_ui.HTML_BUILDER.notifyError(i18n('web.generic.requiredField'));
				$("#new_pass").parent().parent().addClass("error");
				return;
			}
			
			// #2966
			if(newPass.length < PASSWORD_MIN_LENGTH){
				acme_ui.HTML_BUILDER.notifyError(i18n('web.validation.user.password'));
				$("#new_pass").parent().parent().addClass("error");
				return;
			}
			
			if(newPass===confirmPass){
				$(".inputPass").parent().parent().removeClass("error");
				var promise=changePassword(oldPass,newPass);
				$.when(promise).done(function(obj){
					if (obj.success) {
						acme_ui.HTML_BUILDER.notifySuccess(obj.title, obj.message);						
					} else {
						acme_ui.HTML_BUILDER.notifyError(obj.title, obj.message);
					}
				});
			}else{
				$("#confirm_pass").parent().parent().addClass("error");
				acme_ui.HTML_BUILDER.notifyError(i18n('web.generic.error'),i18n('web.home.myaccount.password.error.confirm'));
			}
		});

	};

	pkg.start = function() {
		var settingsLoaded = loadSettings();
		
		$.when(settingsLoaded).then(function(settings) {
			$("#selectOfLanguage").val(settings.language);
		});
		
		$.when(i18nPromise).then(bindEvents);
		
		var fullLoaded = $.Deferred();
		
		$.when(settingsLoaded, i18nPromise).then(function() {
			fullLoaded.resolve();
		});
		
		return fullLoaded.promise;
	};

	return pkg;
});
