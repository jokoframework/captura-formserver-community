define(["jquery", "acme", "jquery-ui", "acme-ui", "constants","sodep-multiselect/sodep-multiselect"], function($, acme, $ui, acme_ui,constants,sodepMultiselect) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.requiredField','admin.cruds.group.edit.title']);
	var I18N = acme.I18N_MANAGER.getMessage;
	var groupId=false;
	var userMultiselect=false, usersGroupMultiselect=false;
	var roleMultiselect=false, rolesGroupMultiselect=false;
	var tab=false;
	
	var saveUsersGroup = function() {
		var usersId=$('#multiSelectUsers').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/groups/addUsers.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'groupTabs';
		ajaxRequest.data=JSON.stringify({groupId:groupId,usersId:usersId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectUsersGroup').sodepMultiselect('reload');
				$('#addUsersButton').attr("disabled",true);
				$('#multiSelectUsers').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var removeUsersGroup = function() {
		var usersId=$('#multiSelectUsersGroup').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/groups/removeUsers.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'groupTabs';
		ajaxRequest.data=JSON.stringify({groupId:groupId,usersId:usersId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectUsersGroup').sodepMultiselect('reload');
				$('#removeUsersButton').attr("disabled",true);
				$('#multiSelectUsers').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var initUsersTabButtons = function() {
		$('#addUsersButton').attr("disabled",true);
		$('#removeUsersButton').attr("disabled",true);
		$('#addUsersButton').click(function(){
			if(!$(this).attr("disabled")) {
				saveUsersGroup();
			}
		});
		$('#removeUsersButton').click(function(){
			if(!$(this).attr("disabled")) {
				removeUsersGroup();
			}
		});
	};
	
	var initUsersTab = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit")) {
			userMultiselect=$('#multiSelectUsers').sodepMultiselect({id: 'usersNotInGroup', checkboxes : true,
				requestParams:{groupId:groupId},
				selected : function(item, selectedArray){
					if(selectedArray.length > 0) {
						$('#addUsersButton').removeAttr("disabled");
					} else {
						$('#addUsersButton').attr("disabled",true);
					}
				},
			});
			$('#addUsersButton').show();
			$('#removeUsersButton').show();
		}
		usersGroupMultiselect=$('#multiSelectUsersGroup').sodepMultiselect({id: 'users', checkboxes : true,
			requestParams:{groupId:groupId},
			selected : function(item, selectedArray){
				if(selectedArray.length > 0) {
					$('#removeUsersButton').removeAttr("disabled");
				} else {
					$('#removeUsersButton').attr("disabled",true);
				}
			},
		});
	};
	
	var saveRolesGroup = function() {
		var rolesId=$('#multiSelectRoles').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/groups/addRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'groupTabs';
		ajaxRequest.data=JSON.stringify({groupId:groupId,rolesId:rolesId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectRolesGroup').sodepMultiselect('reload');
				$('#addRolesButton').attr("disabled",true);
				$('#multiSelectRoles').sodepMultiselect('reload');
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var removeRolesGroup = function() {
		var rolesId=$('#multiSelectRolesGroup').sodepMultiselect('selected');
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/groups/removeRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'groupTabs';
		ajaxRequest.data=JSON.stringify({groupId:groupId,rolesId:rolesId});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				$('#multiSelectRolesGroup').sodepMultiselect('reload');
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
		$('#addRolesButton').click(function(){
			if(!$(this).attr("disabled")) {
				saveRolesGroup();
			}
		});
		$('#removeRolesButton').click(function(){
			if(!$(this).attr("disabled")) {
				removeRolesGroup();
			}
		});
	};
	
	var initRolesTab = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit")) {
			roleMultiselect=$('#multiSelectRoles').sodepMultiselect({id: 'rolesNotContainingEntity', checkboxes : true,
				requestParams : { groupId : groupId, level : 'application' },
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
		rolesGroupMultiselect=$('#multiSelectRolesGroup').sodepMultiselect({id: 'roles', checkboxes : true,
			requestParams : { groupId : groupId, level : 'application' },
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
		var name = $("#group_name").val();
		if (!/\S/.test(name)) {
			// string is empty or just whitespace
			$("#name_control_group").addClass("error");
			$("#group_name").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		return response;
	};

	var saveGroup = function() {
		var name = $("#group_name").val();
		var description = $("#group_description").val();
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/groups/save.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.data=JSON.stringify({groupId:groupId,name:name,description:description});
		ajaxRequest.success = function(obj){
			if(obj.success) {
				if(!groupId) {
					groupId=obj.obj;
					initTabs();
				}
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
				$('#legend').empty().append(I18N('admin.cruds.group.edit.title'));
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var initFormButtons = function() {
		$("#save").click(function(){
			removeErrorMessages();
			if(controlRequiredFields()) {
				saveGroup();
			}
		});
	};
	
	var initAddButtons = function() {
		initUsersTabButtons();
		initRolesTabButtons();
	};
	
	var getGroup = function(groupId) {
		var groupPromise = acme_ui.UTIL.downloadList({url:"/cruds/groups/get.ajax", data:"groupId=" + groupId});
		$.when(groupPromise).then(function(groupResponse){
			if(groupResponse.success) {
				var obj = groupResponse.obj;
				$("#group_name").val(obj.name);
				$("#group_description").val(obj.description);
			}
		});
		return groupPromise;
	};

	var unableGroupEditing = function() {
		$('#group_name').attr('disabled',true);
		$('#group_description').attr('disabled',true);
		$('#save').hide();
		
		$('#multiSelectUsers').hide();
		$('#addUsersButton').hide();
		$('#removeUsersButton').hide();
	
		$('#multiSelectRoles').hide();
		$('#addRolesButton').hide();
		$('#removeRolesButton').hide();
	};
	
	var controlPermissions = function() {
		if(groupId!= null && !acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit")) {
			unableGroupEditing();
		} else {
			initFormButtons();
			initAddButtons();
		}
	};
	
	var initVars = function() {
		userMultiselect=false, usersGroupMultiselect=false;
		roleMultiselect=false, rolesGroupMultiselect=false;
	};
	
	var initTabs = function() {
		$('a[href="#usersTab"]').click(function () {
			if(!userMultiselect && !usersGroupMultiselect) {
				initUsersTab();
			}
		});
		$('a[href="#rolesTab"]').click(function () {
			if(!roleMultiselect && !rolesGroupMultiselect) {
				initRolesTab();
			}
		});
		$('#groupTabs').show();
		initUsersTab();
	};
	
	pkg.start = function(id){
		var dfd = $.Deferred();
		var groupPromise;
		if(typeof id != 'undefined') {
			groupId=id;
		}else {
			groupId=null;
		}
		initVars();
		controlPermissions();
		$.when(i18nPromise).then(function(){
			if(groupId) {  //it's in edit mode
				groupPromise=getGroup(id);
				initTabs();
			} else {  //it's new
				groupPromise={success:true};
			}
			$.when(groupPromise).then(function(){
				dfd.resolve();
			});
		});
		return dfd.promise();
	};
	
	return pkg;
});
