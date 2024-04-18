define([ "jquery", "acme", "jquery-ui", "acme-ui", "constants" ], function($, acme, $ui, acme_ui, constants) {

			var pkg = {};

			var role = null;
			var authGroupList = null;
			var columnsNumber = 3;

			var i18nPromise = acme.I18N_MANAGER.load([ 'web.generic.submit','web.generic.cancel' ]);

			var I18N = acme.I18N_MANAGER.getMessage;

			var permissions=[];
			
			var authorizationMap={};
			/**
			 * Receive an authorization id such as "app.admin.web" and replace
			 * the "." (dots) with _ (slashes). Jquery has a special meaning for
			 * the dots, so we can't use them as an id
			 */
			var getCheckBoxId = function(authId) {
				return "auth_"+authId.replace(/\./g,"_");
				
			};
			/**
			 * Return true if the authorization was explicitly granted by the user
			 * */
			var isExplicitlyGranted=function(auth){
				return $.inArray(auth, permissions) >= 0;
			};
			
			var checkAuthorization=function(auth,level){
				var checkboxId=getCheckBoxId(auth),i;
				$("#"+checkboxId).attr('checked','checked');
				if(!level){
					//if the level is undefined it means that the authorization was explicitly set by the user
					//we need to check the dependent authorizations
					var authorization=authorizationMap[auth];
					if(authorization && authorization.dependantAuthorization){
						for(i=0;i<authorization.dependantAuthorization.length;i++){
							checkAuthorization(authorization.dependantAuthorization[i].id,1);
						}
					}
				}else{
					//if its it not from the first level, then we need to disable it. 
					//The idea is to highlight the fact that this authorization will be granted and the user can't modify it
					$("#"+checkboxId).attr('disabled','disabled');
				}
			};
			
			var checkGrantedAuthorizations=function(){
				var i;
				//deselect all authorizations
				$(".authorizationCheckbox").removeAttr('checked');
				$(".authorizationCheckbox").removeAttr('disabled');
				//check the authorization in permissions and their dependencies
				for(i=0;i<permissions.length;i++){
					checkAuthorization(permissions[i]);
				}
				
			};
			
			
			var grantPermission=function(auth){
				if($.inArray(auth, permissions)<0){
					//only do something if the value has not been granted yet
					permissions.push(auth);
					
				}
				
			};
			var denyPermission=function(auth){
				var index=$.inArray(auth, permissions);
				if(index>=0){
					//only do something if the authorization is currently granted
					permissions.remove(index);					
				}
			};
			
			/**
			 * Check the checkboxes for the authorization granted to the role
			 */
			var initValues = function() {
				var i, auth;
				for (i = 0; i < role.grants.length; i++) {
					auth = role.grants[i];
					grantPermission(auth.id);
				}
				checkGrantedAuthorizations();
			};

			/**
			 * */
			var initToolTipDesc = function() {
				var i, j, auth;
				for (i = 0; i < authGroupList.length; i++) {
					for (j = 0; j < authGroupList[i].authorizations.length; j++) {
						auth = authGroupList[i].authorizations[j];
						$('#' + getCheckBoxId(auth.id) + '_icon').popover({
							title : auth.name,
							content : auth.description
						});
					}
				}

			};

			var cancelData = function() {
				var i, j, auth;
				for (i = 0; i < authGroupList.length; i++) {
					for (j = 0; j < authGroupList[i].authorizations.length; j++) {
						auth = authGroupList[i].authorizations[j];
						$("#" + getCheckBoxId(auth.id)).removeAttr("checked");
					}
				}
				initValues();
				acme.LAUNCHER.launch(constants.LAUNCHER_IDS.roles_crud);
				
			};

			var saveData = function() {
				var i, j, ajaxRequest, auth;
				
				ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/cruds/roles/saveAuths.ajax';
				ajaxRequest.contentType = 'application/json; charset=utf-8';
				ajaxRequest.data = JSON.stringify({
					roleId : role.id,
					authsId : permissions
				});
				ajaxRequest.success = function(obj) {
					var promise = acme.LAUNCHER.launch(constants.LAUNCHER_IDS.roles_crud);
					$.when(promise).then(function(response) {
						if(obj.success) {
							acme_ui.HTML_BUILDER.notifySuccess(obj.title, obj.message);
						} else {
							acme_ui.HTML_BUILDER.notifyError(obj.title, obj.message);
						}	
					});
				};
				$.ajax(ajaxRequest);
			};

			var initButtons = function() {
				if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.role.edit")) {
					var html = "";
					html += '<a href="javascript:void(0)" class="btn" id="cancelForm">'
							+ I18N('web.generic.cancel') + '</a>';
					html += '<a href="javascript:void(0)" class="btn btn-primary" id="submitForm">'
							+ I18N('web.generic.submit') + '</a>';
					$('#formButtons').append(html);
					$("#cancelForm").click(cancelData);
					$("#submitForm").click(saveData);
					$('#formButtons').show();
				}
			};

			
			
			/**
			 * Draw the different permission groups and the authorization within each group.
			 */
			var drawPermissionGroup = function(div, authorizations) {
				var i, j, html = "";
				var authId,id,disabledStr="";
				if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.role.edit")) {
					disabledStr = ' disabled="disabled" ';
				}
				html += '<table class="table table-striped table-bordered" cellpadding="20"><tbody>';
				for (i = 0; i < authorizations.length;) {
					html += "<tr>";
					for (j = 0; j < columnsNumber; j++, i++) {
						if(i < authorizations.length) {
							authorizationMap[authorizations[i].id]=authorizations[i];
							if(authorizations[i].visible){
								//only show the authorization if its has been marked as visible
								html += "<td>";
								id = getCheckBoxId(authorizations[i].id);
								html += '<label class="checkbox"><input class="authorizationCheckbox" type="checkbox" id="'+ id +'"' + disabledStr +'>' + authorizations[i].name +'   </input><i class="icon-question-sign" id="'+ id +'_icon"></i></label>';
								html += "</td>";
							}
							
						}
					}
					html += "</tr>";
				}
				html += "</tbody></table>";
				div.append(html);
				var checkFunction=function(auth){
					return function(){
						var checkbox=getCheckBoxId(auth);
						var isChecked=$(this).attr("checked");
						if(isChecked){
							grantPermission(auth);
						}else{
							denyPermission(auth);
						}
						checkGrantedAuthorizations();
					};
				};
				for (i = 0; i < authorizations.length;i++) {
					id = getCheckBoxId(authorizations[i].id);
					$("#"+id).click(checkFunction(authorizations[i].id));
				}
			};

			var initPage = function() {
				var i, permissionsDiv;
				$("#role_name").val(role.name);
				$("#role_description").val(role.description);
				permissionsDiv = $("#permissions");
				permissions=[];
				authorizationMap={};
				
				// #3904 Corregir permisos que no tienen sentido
				var hiddenPermissions = ['Pool', 'Connector Repository', 'Contenedor'];
				
				for (i = 0; i < authGroupList.length; i++) {
					if ($.inArray(authGroupList[i].name, hiddenPermissions) === -1) {
						permissionsDiv.append('<legend><h4>'+ authGroupList[i].name + '</h4></legend>');
						drawPermissionGroup(permissionsDiv, authGroupList[i].authorizations);
						permissionsDiv.append("</br>");
					}
				}
				initValues();
				initButtons();
				initToolTipDesc();
			};

			pkg.start = function(roleId) {
				var ajaxRequest;
				var dfd = $.Deferred();
				
				ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/crud/role/get.ajax';
				ajaxRequest.data={"roleId":roleId};
				var rolePromise=$.ajax(ajaxRequest);
				
				ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/authorizationGroup/list.ajax';
				ajaxRequest.data={"roleId":roleId};
				var authGroupPromise=$.ajax(ajaxRequest);
				
				var pagePromise=dfd.promise();
				$.when(rolePromise, authGroupPromise, i18nPromise).then( 
						function(roleResponse, authGroupResponse) {
							role = roleResponse[0];
							authGroupList = authGroupResponse[0];
							initPage();
							dfd.resolve();
				});
				return pagePromise;
			};

	return pkg;
});