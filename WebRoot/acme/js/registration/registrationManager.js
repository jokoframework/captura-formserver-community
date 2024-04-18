define(["jquery", "acme"],  function($, acme) {
	
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load([ "web.registration.error.requiredFieldEmpty",
	                                           "web.registration.error.emptyField",
	                                           "web.registration.passwordsDontMatch",
	                                           "web.generic.error"]);
	
	var I18N = acme.I18N_MANAGER.getMessage;

	pkg.Manager = function(params){
	
		var that = {};
		
		var formId = params.formId;
		
		var url = params.url;
		
		var messageManger=acme.messageManager('registrationMessage');
		
		var success = function(res) {
			// hide all unnecessary messages that might be left due to some previous error
			messageManger.hide();
			hideErrorOnFields();
			var regResult = res.content.registrationResult;
			if(res.success){
				// hide the form that was just sent
				$('#mainDiv').hide();
				// registration successful, show Message
				
				
				if(regResult === 'INACTIVE'){		
					messageManger.info(res.unescapedMessage);
					$('a#resendLink').click(function(){
						var ajaxRequest = acme.AJAX_FACTORY.newInstance();
						ajaxRequest.url += '/registration/resend.ajax';
						ajaxRequest.data = 'mail=' + res.content.mail;
						ajaxRequest.success = function(res2) {
							if (res2.success === true) {
								messageManger.success(res2.unescapedMessage);
								$("#registrationForm").hide();
								$("#submitUser").hide();
								
							} else {
								messageManger.error(res2.unescapedMessage);								
							}
						};
						$.ajax(ajaxRequest);
					});
				} else{
					messageManger.success(res.unescapedMessage);
					$("#registrationForm").hide();
					$("#submitUser").hide();
				}
				
			} else {
				if(regResult === 'ACTIVE'){
					messageManger.info(res.unescapedMessage);										
				} else {
					var messages=res.content.messages;
					for (var prop in messages) {
						if(messages.hasOwnProperty(prop)){
							$('#' + prop + '_validation_msg').show();
							$('#' + prop + '_validation_msg').html(messages[prop]);
						}
					}
					messageManger.error(res.unescapedMessage);					
				}
			}
			
		};
		
		var hideErrorOnFields=function(){
			$("span.validation_msg").hide().parent().parent().removeClass("error");
		};
		var showErrorOnFields = function (){
			$('span.validation_msg').each(function(i, obj){
				var span = $(obj);
				if(span.is(':visible')){
					span.parent().parent().addClass("error");
				}
			});
		};
		
		var isFieldEmpty = function(field){
			var val = $('#' + field).val(); 
			if(!/\S/.test(val)){
				$.when(i18nPromise).then(function(){
					$('#' + field + '_validation_msg').html(I18N('web.registration.error.emptyField')).show();
				});
				return true;
			}
			return false;
		};  
	
		that.submit = function() {
			hideErrorOnFields();
			var password = $('#password').val();
			var password2 = $('#password2').val();
			
			$(formId).find("span.validation_msg").hide();
			var fields = ['mail', 'firstName', 'lastName', 'password', 'password2'];
			var emptyField = false;
			for(var i = 0; i < fields.length; i++){
				var field = fields[i];
				emptyField = isFieldEmpty(field) || emptyField;
			}

			if(emptyField){
				$.when(i18nPromise).then(function(){
					messageManger.error(I18N('web.registration.error.requiredFieldEmpty'));
				});
				showErrorOnFields();
				return false;
			}
			
			if(password === password2){
				var ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += url;
				ajaxRequest.success=success;
				var registrationRequest={};
				registrationRequest.captchaValue=$("#captcha").val();
				registrationRequest.userDTO={};
				registrationRequest.userDTO.mail=$("#mail").val();
				registrationRequest.userDTO.firstName=$("#firstName").val();
				registrationRequest.userDTO.lastName=$("#lastName").val();
				registrationRequest.userDTO.password=password;
				
				ajaxRequest.contentType = 'application/json; charset=utf-8';
				ajaxRequest.data=JSON.stringify(registrationRequest);
				$.ajax(ajaxRequest);
			} else {
				$.when(i18nPromise).then(function(){
					$(formId).find("span.validation_msg").hide();
					var title = I18N('web.generic.error');
					var errorMsg = I18N('web.registration.passwordsDontMatch');
					$('#password_validation_msg').html(errorMsg);
					$('#password2_validation_msg').html(errorMsg);
					messageManger.error(errorMsg);
					$('#password_validation_msg').show();
					$('#password2_validation_msg').show();
				});
				errorCssClass();
				return false;
			}
			
		};
		
		return that;
	};
	
	pkg.refreshCaptcha=function(target){
		var d =new Date();
		$("#"+target).attr("src",acme.VARS.contextPath+"/api/public/captcha?refresh="+d.getTime());
	};
	return pkg;
});
