define(["jquery", "acme", "jquery-ui", "acme-ui", "constants","sodep-multiselect/sodep-multiselect"], function($, acme, $ui, acme_ui,constants,sodepMultiselect) {
   
var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.requiredField','admin.cruds.user.edit.title', 'admin.cruds.user.save.cancelled.title', 'admin.cruds.user.save.cancelled',
	                                          'admin.cruds.user.invite', 'web.generic.save', 'web.new-user.mail.placeholder', 'web.new-user.username.placeholder',
	                                          'web.dialog.buttons.ok', 'web.dialog.buttons.cancel', 'web.generic.password_not_equal']);
	var I18N = acme.I18N_MANAGER.getMessage;
	var userId = null;
	var groupMultiselect = null, groupsUserMultiselect = null;
	var roleMultiselect = null , rolesUserMultiselect = null;
	var tab = null;
	var add = false;
	
	var validateEmail = function(email) { 
	    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    return re.test(email);
	};
	
	var validateUsername = function(username) { 
	    var re = /\S/;
	    return re.test(username);
	};
	
	var saveGroupsUser = function() {
		var groupsId=$('#multiSelectGroups').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/users/addGroups.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'userTabs';
		ajaxRequest.data=JSON.stringify({userId:userId,groupsId:groupsId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectGroupsUser').sodepMultiselect('reload');
				$('#addGroupsButton').attr("disabled",true);
				$('#multiSelectGroups').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var removeGroupsUser = function() {
		var groupsId=$('#multiSelectGroupsUser').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/users/removeGroups.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'userTabs';
		ajaxRequest.data=JSON.stringify({userId:userId,groupsId:groupsId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectGroupsUser').sodepMultiselect('reload');
				$('#removeGroupsButton').attr("disabled",true);
				$('#multiSelectGroups').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var initGroupsTabButtons = function() {
		$('#addGroupsButton').attr("disabled",true);
		$('#removeGroupsButton').attr("disabled",true);
		$('#addGroupsButton').off().click(function(){
			if(!$(this).attr("disabled")) {
				saveGroupsUser();
			}
		});
		$('#removeGroupsButton').off().click(function(){
			if(!$(this).attr("disabled")) {
				removeGroupsUser();
			}
		});
	};
	
	var initGroupsTab = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit")) {
			groupMultiselect=$('#multiSelectGroups').sodepMultiselect({id: 'groupsNotContainingUser', checkboxes : true,
				requestParams:{userId:userId},
				selected : function(item, selectedArray){
					if(selectedArray.length > 0) {
						$('#addGroupsButton').removeAttr("disabled");
					} else {
						$('#addGroupsButton').attr("disabled",true);
					}
				},
			});
			$('#addGroupsButton').show();
			$('#removeGroupsButton').show();
		}
		groupsUserMultiselect=$('#multiSelectGroupsUser').sodepMultiselect({id: 'groups', checkboxes : true,
			requestParams:{userId:userId},
			selected : function(item, selectedArray){
				if(selectedArray.length > 0) {
					$('#removeGroupsButton').removeAttr("disabled");
				} else {
					$('#removeGroupsButton').attr("disabled",true);
				}
			},
		});
	};
	
	var saveRolesUser = function() {
		var rolesId=$('#multiSelectRoles').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/users/addRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'userTabs';
		ajaxRequest.data=JSON.stringify({userId:userId,rolesId:rolesId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectRolesUser').sodepMultiselect('reload');
				$('#addRolesButton').attr("disabled",true);
				$('#multiSelectRoles').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var removeRolesUser = function() {
		var rolesId=$('#multiSelectRolesUser').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/users/removeRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'userTabs';
		ajaxRequest.data=JSON.stringify({userId:userId,rolesId:rolesId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectRolesUser').sodepMultiselect('reload');
				$('#removeRolesButton').attr("disabled",true);
				$('#multiSelectRoles').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var initRolesTabButtons = function() {
		$('#addRolesButton').attr("disabled",true);
		$('#removeRolesButton').attr("disabled",true);
		$('#addRolesButton').off().click(function(){
			if(!$(this).attr("disabled")) {
				saveRolesUser();
			}
		});
		$('#removeRolesButton').off().click(function(){
			if(!$(this).attr("disabled")) {
				removeRolesUser();
			}
		});
	};
	
	var initRolesTab = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit")) {
			roleMultiselect=$('#multiSelectRoles').sodepMultiselect({id: 'rolesNotContainingEntity', checkboxes : true,
				requestParams : { userId : userId, level : 'application'},
				selected : function(item, selectedArray){
					if(selectedArray.length > 0) {
						$('#addRolesButton').removeAttr("disabled");
					} else {
						$('#addRolesButton').attr("disabled",true);
					}
				},
			});
			$('#addRolesButton').show();
			$('#removeRolesButton').show();
		}
		rolesUserMultiselect=$('#multiSelectRolesUser').sodepMultiselect({id: 'roles', checkboxes : true,
			requestParams : { userId : userId, level : 'application'},
			selected : function(item, selectedArray){
				if(selectedArray.length > 0) {
					$('#removeRolesButton').removeAttr("disabled");
				} else {
					$('#removeRolesButton').attr("disabled",true);
				}
			},
		});
	};
	
	var removeErrorMessages = function() {
		$('.error').each(function(index){
			$(this).removeClass('error');
			$(this).find('.errorLabel').remove();
		});
	};
	
	var controlRequiredFields = function() {
		var response=true;
		
		var firstName = $("#first_name").val();
		if (!/\S/.test(firstName)) {
			// string is empty or just whitespace
			$("#first_name_control_group").addClass("error");
			$("#first_name").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		
		var lastName = $("#last_name").val();
		if (!/\S/.test(lastName)) {
			// string is empty or just whitespace
			$("#last_name_control_group").addClass("error");
			$("#last_name").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		
		var mail = $("#mail").val();
		if (!/\S/.test(mail)) {
			// string is empty or just whitespace
			$("#mail_control_group").addClass("error");
			$("#mail").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		
//		var password = $("#password").val();
//		if (!/\S/.test(password)) {
//			// string is empty or just whitespace
//			$("#password_control_group").addClass("error");
//			$("#password").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
//			response = false;
//		}
		
		return response;
	};
	
	var controlPasswordConfirmation = function() {
		var password = $("#password").val();
		var password2 = $("#password_confirmation").val();
		if(password === password2){
			return true;
		} else {
			$("#password_confirmation_control_group").addClass("error");
			$("#password_confirmation").parent().append('<p class="errorLabel">' + I18N('web.generic.password_not_equal') +'</p>');
		}
	};
	
	var saveUser = function(){
		var firstName = $("#first_name").val();
		var lastName = $("#last_name").val();
		var mail = $("#mail").val();
		var password = $("#password").val();
		
		var userObj = {
					"userId" : userId,
					"firstName" : firstName,
					"lastName" : lastName,
					"mail" : mail,
					"password" : password,
					"add" : add
		};
		
		var data = JSON.stringify(userObj);
		
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/users/save.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.data = data;
		ajaxRequest.success = function(response){
			if(response.success) {
				if(!userId){
					userId = response.obj;
					initTabs();
				}
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
			}
		};
		
		$.ajax(ajaxRequest);
	};

	var initFormButtons = function() {
		$("#save").click(function(){
			removeErrorMessages();
			if(controlRequiredFields() && controlPasswordConfirmation()) {
				saveUser();
				$(this).hide();
			}
		});
		
		$('#cancelBtn').click(function(){
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.users_and_groups);
		});
		
		// #2985
		$("#inviteExistingBtn").click(function(){
			removeErrorMessages();
			saveUser();
		});
		
		$('#cancelExistingBtn').click(function(){
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.users_and_groups);
		});
	};
	
	var initAddButtons = function() {
		initGroupsTabButtons();
		initRolesTabButtons();
	};
	
	var initUserTypeRadioButtons = function() {
		add = false;
		$("input:radio[name='userTypeGroup']").click(function(){
		    var radioId=$(this).attr('id');
		    if(radioId==='byMail'){
		    	add = false;
		    	$("#userAssignPassword").attr('disabled', null);
		    	$.when(i18nPromise).then(function(){
		    		if (userId === null) {
		    			$('#save').html(I18N('admin.cruds.user.invite'));
		    		}
					$('#inviteExistingBtn').html(I18N('admin.cruds.user.invite'));
					$("#mail").attr("placeholder", I18N('web.new-user.mail.placeholder'));
				});
		    	$("#mailSpan").show();
		    	$("#usernameSpan").hide();
		    }else{
		    	add = true;
		    	$("#userAssignPassword").attr('disabled', 'disabled');
		    	$.when(i18nPromise).then(function(){
					$('#save').html(I18N('web.generic.save'));
					$('#inviteExistingBtn').html(I18N('web.generic.save'));
					$("#mail").attr("placeholder", I18N('web.new-user.username.placeholder'));
				});
		    	$("#mailSpan").hide();
		    	$("#usernameSpan").show();
		    }
		 });
	};
	
	var getUser = function(userId) {
		//FIXME this is misleading. The response is not a list - jmpr 04/10/2012
		var userPromise = acme_ui.UTIL.downloadList({url:"/cruds/users/get.ajax", data:"userId=" + userId});
		$.when(userPromise).then(function(userResponse){
			if(userResponse.success) {
				var obj = userResponse.obj;
				var mailValue = obj.mail != null ? obj.mail : obj.username;
				$("#first_name").val(obj.firstName);
				$("#last_name").val(obj.lastName);
				$("#mail").val(mailValue);
				if (obj.mail != null) {
					$("#byMail").attr('checked', true).trigger("click");
				} else if (obj.username != null) {
					$("#byUsername").attr('checked', true).trigger("click");
				} 
				
				// should password be sent back? definitely it shouldn't be shown, nor the value of an input type=text set to it, as it 
				// is way to easy to see it.
				//$("#password").val(obj.password); 
			}
		});
		return userPromise;
	};
	
	var startUIEditMode =  function(){
		// #1404
		$.when(i18nPromise).then(function(){
			$('#save').html(I18N('web.generic.save'));
		});
		$('#mail').attr('disabled', 'disabled');
		$('#nextBtn').hide();
		$('#info_control_group').hide(); //no message is shown
		$('#userDataForm').show();
		$('#password_control_group').show();
		$('#first_name').focus();
		disableUserType(true);
	};
	
	var startUINewMode = function(){
		var mailInput = $('#mail').attr('disabled', null);
		disableUserType(false);
		// #1404
		$.when(i18nPromise).then(function(){
			$('#save').html(I18N('admin.cruds.user.invite'));
		});
		mailInput.keypress(function(e){
		   if (e.keyCode == 13) {
		     $('#nextBtn').trigger('click');
		     return false;
		   }
		});
		mailInput.focus();
		$('#nextBtn').show(); 
		$('#info_control_group').hide();
		$('#nextBtn').click(function(){
			var mail = mailInput.val();
			if (!add) {
				if(validateEmail(mail)){
					$('#mail_control_group').removeClass('error');
				} else {
					$('#mail_control_group').addClass('error');
					return false;
				}
			} else {
				if(validateUsername(mail)){
					$('#mail_control_group').removeClass('error');
				} else {
					$('#mail_control_group').addClass('error');
					return false;
				}
			}
			mailInput.attr('disabled', 'disabled');
			$(this).hide();
			$('#info_control_group').hide();
			
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += "/cruds/users/preSave.ajax";
			ajaxRequest.data = 'mail=' + mail + '&add=' + add;
			ajaxRequest.success = function(response) {
				if(response.success){
					$('#userDataForm').show();
					var status = response.obj.status;
					$('#info_control_group').show();
					$('#infoSpan').html(response.unescapedMessage);
					
					if(status==='existing'){
						$("#userDataForm").hide();
						$('#password_control_group').hide();
						$('#password_confirmation_control_group').hide();
						// #2985
						$('#existingDataForm').show();
						$('#save').focus();
						disableUserType(true);
					}else if(status==='non_existing'){
						//crate the new user
						$('#password_control_group').show();
						$('#password_confirmation_control_group').show();
						$('#first_name').focus();
						disableUserType(true);
					}else if(status==='member'){
						$('#info_control_group').show();
						$('#infoSpan').html(response.unescapedMessage);
						$('#mail_control_group').addClass('error');
						mailInput.attr('disabled', null);
						$('#nextBtn').show();
						mailInput.focus();
					}
					
				} else {
					//this shouldn't happen
					$('#info_control_group').show();
					$('#infoSpan').html(response.unescapedMessage);					
				}
			};
			$.ajax(ajaxRequest);
		});
		$('#userDataForm').hide();
		
		$("input:radio[name='assignPasswordGroup']").click(function(){
		    var optionPass=$(this).val();
		    if(optionPass==='user_by_himself'){
		    	$(".passwordControlDiv").hide();
		    }else{
		    	$(".passwordControlDiv").show();
		    }
		  });
	};

	var unableUserEditing = function() {
		$('#first_name').attr('disabled',true);
		$('#last_name').attr('disabled',true);
		$('#password').hide();
		$('#formButtons').hide();
		
		$('#multiSelectGroups').hide();
		$('#addGroupsButton').hide();
		$('#removeGroupsButton').hide();
	
		$('#multiSelectRoles').hide();
		$('#addRolesButton').hide();
		$('#removeRolesButton').hide();
	};
	
	var controlPermissions = function() {
		if(userId!= null && !acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit")) {
			unableUserEditing();
		} else {
			initFormButtons();
			initAddButtons();
			initUserTypeRadioButtons();
		}
	};
	
	var initVars = function() {
		groupMultiselect = null; 
		groupsUserMultiselect = null;
		roleMultiselect = null;
		rolesUserMultiselect = null;
	};
	
	var initTabs = function() {
		$('a[href="#groupsTab"]').click(function () {
			if(!groupMultiselect && !groupsUserMultiselect) {
				initGroupsTab();
			}
		});
		$('a[href="#rolesTab"]').click(function () {
			if(!roleMultiselect && !rolesUserMultiselect) {
				initRolesTab();
			}
		});
		$('#userTabs').show();
		initGroupsTab();
	};
	
	var disableUserType = function (disable) {
		if (disable) {
			$('#byMail, #byUsername').attr('disabled', 'disabled');
		} else {
			$('#byMail, #byUsername').attr('disabled', null);
		}
	};
	
	pkg.start = function(id){
		var dfd = $.Deferred();
		var userPromise;
		if(typeof id != 'undefined') {
			userId=id;
		}else {
			userId=null;
		}
		initVars();
		controlPermissions();
		$.when(i18nPromise).then(function(){
			if(userId) {  //it's in edit mode
				startUIEditMode();
				userPromise=getUser(id);
				initTabs();
			} else {  //it's new
				startUINewMode();
				userPromise={success:true};
			}
			$.when(userPromise).then(function(){
				dfd.resolve();
			});
		});
		return dfd.promise();
	};

	return pkg;
});
