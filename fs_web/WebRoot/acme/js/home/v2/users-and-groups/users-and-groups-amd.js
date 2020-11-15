define(["jquery", "acme", "jquery-ui", "acme-ui", "cruds/crud-utils", "constants"], function($, acme, $ui, acme_ui, crudutils, constants)  {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.cruds.user.groups.dialog.title',
	                                          'admin.cruds.user.roles.dialog.title',
	                                          'admin.cruds.user.title',
	                                          'web.generic.submit',
	                                          'web.generic.cancel',
	                                          'admin.cruds.user.password_reset_button.title',
	                                          'admin.cruds.user.user_not_selected',
	                                          'admin.cruds.user.cols.groups',
	                                          'admin.cruds.user.cols.roles',
	                                          'admin.cruds.group.title',
	                                          'admin.cruds.group.cols.roles',
	                                          'admin.cruds.group.roles.dialog.title',
	                                          'admin.cruds.group.saved.message',
	                                          'web.grid.row.edit',
	                                          'web.grid.row.delete',
	                                          'web.grid.users.row.deviceDetails',
	                                          'web.grid.users.row.resetPassword',
	                                          'web.usersAndGroups.users.grid.title',
	                                          'web.usersAndGroups.deleteGroup.confirm',
	                                          'web.usersAndGroups.deleteUser.confirm',
	                                          'web.dialog.buttons.yes',
	                                          'web.dialog.buttons.no',
	                                          'web.grid.users.row.cancelInvitation',
	                                          'web.grid.users.row.resendInvitation',
	                                          'web.usersAndGroups.cancelInvitation.confirm',
	                                          'web.home.admin.user.new','web.home.admin.group.new',
	                                          'web.generic.reloadGrid',
	                                          'admin.cruds.user.diassociate.warning.title',
	                                          'admin.cruds.user.diassociate.warning.msg',
	                                          'web.home.admin.devices.identifier.msg',
	                                          'web.home.admin.license.max_user.reached',
	                                          'web.home.admin.devices.field.identifier',
	                                          'admin.cruds.user.blacklist.warning.title',
	                                          'admin.cruds.user.blacklist.warning.msg',
	                                          'web.home.admin.devices.devicePopup.title',
	                                          'web.dialog.buttons.cancel',
	                                          'web.password_reset.dialog.body',
	                                          'web.home.reports.error.emptyReport.title',
	                                          'web.home.reports.error.emptyReport.message'
	                                          ]);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var groupsCrud = null;
	
	var usersCrud = null;
	
	//We'll keep here a cache of the last downloaded users
	var userMap={};
	
	var launchModule = function(launcherId, id){
		if(!launcherId){
			alert('Not Yet Implemented');
			return;
		}
		
		var p = acme.LAUNCHER.launch(launcherId, id);
		return p;
	};
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*710/1024;
		$("#usersGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#groupsGrid").jqGrid('setGridWidth', desireWidth, true);		
	};
	
	var groupsGridComplete = function () {
		$('.editGroupLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit")){
				return false;
			}
			var rowId = $(this).attr('rowid');
			var promise = launchModule(constants.LAUNCHER_IDS.group_edit, rowId);
			$.when(promise).then(function(module){
				//TODO
			});
			return false;
		});

		$('.deleteGroupLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.delete")){
				return false;
			}
			var rowId = $(this).attr('rowid');
			var row = groupsCrud.leGrid.jqGrid('getRowData', rowId);
			var groupName = row.name;
			var dialog = $('<div></div>');
			dialog.html(I18N('web.usersAndGroups.deleteGroup.confirm', [groupName])).dialog({
				buttons : [ 
							{
								text : I18N("web.dialog.buttons.yes"),
								click : function(){
									var ajaxRequest = acme.AJAX_FACTORY.newInstance();
									ajaxRequest.url += "/cruds/groups/delete.ajax";
									ajaxRequest.data = "id=" + rowId;
									ajaxRequest.success = function(response) {
										dialog.dialog('close');
										if(response.success){
											acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
										} else {
											acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
										}
										groupsCrud.refresh();
									};
									$.ajax(ajaxRequest);
								}
							},
							{
								text : I18N("web.dialog.buttons.no"),
								click : function() {
									$(this).dialog('close');
								}
							}
						],
				modal : true,
				width: 400,
				height: "auto",
			});

			return false;
		});
	};
	
	var groupsCustomFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		
		var actions= acme_ui.iconForTable(
				rowId,
				'pencil',
				'editGroupLink',
				I18N('web.grid.row.edit'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit");
				}
				
		);
		actions+=acme_ui.iconForTable(
				rowId,
				'bin',
				'deleteGroupLink',
				I18N('web.grid.row.delete'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:65px;" >'+actions+'</div>';
	};
	var removeDevice=function(userId,deviceId){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/cruds/users/diassociateDevice.ajax";
		ajaxRequest.data = {'userId':userId,'deviceId':deviceId};
		ajaxRequest.success = function(response) {
			$("#devicePopup").dialog('close');
			if(response.success){
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
			}
			usersCrud.refresh();
			
		};
		$.ajax(ajaxRequest);
		
	};
	
	var blacklistDevice = function(deviceId){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/devices/addToBlacklist.ajax";
		ajaxRequest.data = {'deviceId':deviceId};
		ajaxRequest.success = function(response) {
			$("#devicePopup").dialog('close');
			if(response.success){
				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
			}
			usersCrud.refresh();
			
		};
		$.ajax(ajaxRequest);
		
	};
	
	var deviceFriendlyName=function(deviceInfo){
		return deviceInfo.brand+","+deviceInfo.model;
	};
	
	var showDeviceDetails=function(deviceInfo,user){
		$("#device_field_identifier").html(deviceInfo.identifier);
		$("#device_field_brand").html(deviceInfo.brand);
		$("#device_field_model").html(deviceInfo.model);
		$("#device_field_phone").html(deviceInfo.phoneNumber);
		$("#device_field_os").html(deviceInfo.os);
		$("#device_field_release").html(deviceInfo.release);

		
		$("#phoneList li.nav-active").removeClass("nav-active");
		$("#device_"+deviceInfo.id).addClass("nav-active");
		
		$("#deviceRemoveButton").unbind("click" );
		$("#deviceRemoveButton").click(
				function(){
					var deviceName='<strong>'+deviceInfo.brand+", "+deviceInfo.model+'</strong>';
					var msg=I18N('admin.cruds.user.diassociate.warning.msg', [deviceName]);
					acme_ui.confirmation(I18N('admin.cruds.user.diassociate.warning.title'), msg, function(){
						removeDevice(user.id,deviceInfo.id);
					});
				}
		);
		$('#deviceBlacklistButton').unbind("click");
		$('#deviceBlacklistButton').click(
			function(){
				var deviceName='<strong>'+deviceInfo.brand+", "+deviceInfo.model+'</strong>';
				var msg=I18N('admin.cruds.user.blacklist.warning.msg', [deviceName]);
				acme_ui.confirmation(I18N('admin.cruds.user.blacklist.warning.title'), msg, function(){
					blacklistDevice(deviceInfo.id);
				});
			}
		);
		
	};
	var openUserDeviceAssociation=function(user){
		
		var i=0;
		var deviceInfo;
		//build the table of devices
		var html='';
		var phoneList=$("#phoneList").html("");
		if(user.devices&&user.devices.length>0){
			var phoneDoc;
			var active=false;
			for(i=0;i<user.devices.length;i++){
				deviceInfo=user.devices[i].deviceInfo;
				phoneDoc=$('<li id="device_'+deviceInfo.id+'" ><a tabindex="-1"  href="javascript:void(0)" >'+deviceFriendlyName(deviceInfo)+'</a></li>');
				phoneList.append(phoneDoc);
				phoneDoc.click((function(d){
					return function(){
						showDeviceDetails(d,user);
					};
				})(deviceInfo));
				
			}
			//activate the first available phone
			showDeviceDetails(user.devices[0].deviceInfo,user);
			$("#devicePopup").dialog('option','width', 600);
			$("#devicePopup").dialog('option','height',450);
			$("#phoneDetails").show();
			
			
			$("#userWithoutPhones").hide();
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.diassociateDevice")){
				$("#deviceRemoveButton").show();
			}else{
				$("#deviceRemoveButton").hide();
			}
			
		}else{
			$("#devicePopup").dialog('option','width',400);
			$("#devicePopup").dialog('option','height',150);
			$("#phoneDetails").hide();
			$("#userWithoutPhones").show();
			
		}
		
		$("#devicePopup").dialog('option','title', I18N('web.home.admin.devices.devicePopup.title', [user.firstName, user.lastName]));
		$("#devicePopup").dialog('open');
	};
	var usersCompleteGrid  = function () {
		$('.editUserLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit")){
				return false;
			}
			var rowId = $(this).attr('rowid');
			var promise = launchModule(constants.LAUNCHER_IDS.user_edit, rowId);
			$.when(promise).then(function(module){
				//TODO
			});
			return false;
		});

		$('.deleteUserLink').click(function(){
			
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.delete")){
				return false;
			}
			var rowId = $(this).attr('rowid');
			var row = usersCrud.leGrid.jqGrid('getRowData', rowId);
			var userMail = row.mail;
			var dialog = $('<div></div>');
			dialog.html(I18N('web.usersAndGroups.deleteUser.confirm', [userMail])).dialog({
				buttons : [ 
							{
								text : I18N("web.dialog.buttons.yes"),
								click : function(){
									var ajaxRequest = acme.AJAX_FACTORY.newInstance();
									ajaxRequest.url += "/cruds/users/delete.ajax";
									ajaxRequest.data = "id=" + rowId;
									ajaxRequest.success = function(response) {
										dialog.dialog('close');
										if(response.success){
											acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
										} else {
											acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
										}
										usersCrud.refresh();
									};
									
									$.ajax(ajaxRequest);
								}
							},
							{
								text : I18N("web.dialog.buttons.no"),
								click : function() {
									$(this).dialog('close');
								}
							}
						],
				modal : true,
				width: 400,
				height: "auto",
			});
		});
		
		$('.resetPasswordLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit")){
				return false;
			}
			var id = $(this).attr('rowid');
			mailRequest('/password/reset/mail.ajax', id);
			return false;
		});
		
		$('.cancelInvitationLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.cancreate")){
				return false;
			}
			var rowId = $(this).attr('rowid');
			var row = usersCrud.leGrid.jqGrid('getRowData', rowId);
			var userMail = row.mail;
			var dialog = $('<div></div>');
			dialog.html(I18N('web.usersAndGroups.cancelInvitation.confirm', [userMail])).dialog({
				buttons : [
							{
								text : I18N("web.dialog.buttons.yes"),
								click : function(){
									var ajaxRequest = acme.AJAX_FACTORY.newInstance();
									ajaxRequest.url += "/cruds/users/cancelInvitation.ajax";
									ajaxRequest.data = "id=" + rowId;
									ajaxRequest.success = function(response) {
										dialog.dialog('close');
										if(response.success){
											acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
										} else {
											acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
										}
										usersCrud.refresh();
									};
									
									$.ajax(ajaxRequest);
								}
							},
							{
								text : I18N("web.dialog.buttons.no"),
								click : function() {
									$(this).dialog('close');
								}
							}
						],
				modal : true,
				width: 400,
				height: "auto",
			});
		});
		
		$('.resendInvitationLink').click(function(){
			if(!acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.cancreate")){
				return false;
			}
			var id = $(this).attr('rowid');
			mailRequest('/invitation/mail.ajax', id);
			return false;
		});
		
		$('.deviceStatusLink').click(function(){
			var rowId = $(this).attr('rowid');
			openUserDeviceAssociation(userMap[rowId]);
			
		});
	};
	
	var mailRequest = function(path, id){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += path;
		ajaxRequest.data = 'id=' + id;
		
		ajaxRequest.success = function(response){
		   if(response.success){
			   if (!response.content) {
				   acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
			   } else {
				   var urlTagString = '<a id="urlTag" target="_blank" href="'+response.content.url+'">'+I18N('web.grid.users.row.resetPassword')+'</a>';
				   var dialog = $('<div></div>');
					dialog.html(I18N('web.password_reset.dialog.body', [urlTagString])).dialog({
						buttons : [ 
									{
										text : I18N("web.dialog.buttons.cancel"),
										click : function() {
											$(this).dialog('close');
										}
									}
								],
						title : I18N('web.grid.users.row.resetPassword'),
						modal : true,
						width: 400,
						height: "auto",
					});
				   dialog.find('#urlTag').click(function(){
					   dialog.dialog('close');
				   });
			   }
			   
		   } else {
			   acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
		   }
		};
		$.ajax(ajaxRequest);
	};
	
	var usersCustomFormatter = function(cellvalue, options, rowObject) {
		var rowId = options.rowId;
		userMap[rowId]=rowObject;
		var actions;
		
		if(options.colModel.name === 'actions') {
			if(rowObject.member){
				actions= acme_ui.iconForTable(
					rowId,
					'keys',
					'resetPasswordLink',
					I18N('web.grid.users.row.resetPassword'),
					function(){
						return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit");
					}
				);
				actions+= acme_ui.iconForTable(
					rowId,
					'pencil',
					'editUserLink',
					I18N('web.grid.row.edit'),
					function(){
						return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit");
					}
				);
				actions+= acme_ui.iconForTable(
						rowId,
						'bin',
						'deleteUserLink',
						I18N('web.grid.row.delete'),
						function(){
							return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.delete");
						}	
				);
				actions+= acme_ui.iconForTable(
						rowId,
						'iphone',
						'deviceStatusLink',
						I18N('web.grid.users.row.deviceDetails'),
						function(){
							return true;
							//return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.delete");
						}	
				);
			}
			else{
				 actions= acme_ui.iconForTable(
							rowId,
							'message_minus',
							'cancelInvitationLink',
							I18N('web.grid.users.row.cancelInvitation'),
							function(){
								return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit");
							}
							
					);
					actions+= acme_ui.iconForTable(
							rowId,
							'message_plus',
							'resendInvitationLink',
							I18N('web.grid.users.row.resendInvitation'),
							function(){
								return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.edit");
							}
							
					);
			}
			return '<div class="actionButtonOnTable" style="width:96px;" >'+actions+'</div>';
		}
		
		if(options.colModel.name === 'mail'){
			if(rowObject.member){
				return cellvalue;	
			} else {
				return '<i>' + cellvalue + ' (invited)</i>';
			}
		}
		if(options.colModel.name==='devices'){
			if(rowObject.devices){
				return '<a class="deviceStatusLink" rowid="' + rowId + '" >'+rowObject.devices.length+'</a>';
			}
			return 0;
		}
		
		return cellvalue;
		
	};
	
	pkg.resized=function(){
		resizeTables();
	};
	
	var checkMaxUsers=function(){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += "/application/license/status.ajax";
		$.ajax(ajaxRequest).done(function(appLicenseStatus){
			
			if(appLicenseStatus.activeUsers>=appLicenseStatus.maxUsers){
				var msg=I18N('web.home.admin.license.max_user.reached',[appLicenseStatus.maxUsers]);
				$("#messageLicenseUserMax").html(msg);
				$("#messageLicenseUserMax").show();
			}else{
				$("#messageLicenseUserMax").html('');
				$("#messageLicenseUserMax").hide();
			}
		});
		
	};
	
	var initExportUsersActions = function(grid) {
		$('#buttonDownloadCSV').click(function(){
			var numberOfRows = grid.jqGrid('getGridParam', 'reccount');
			if (numberOfRows === 0) {
				acme_ui.HTML_BUILDER.notifyError(I18N('web.home.reports.error.emptyReport.title'), I18N('web.home.reports.error.emptyReport.message'));
				return;
			}
			
			var search = grid.jqGrid('getGridParam', 'search');
			var postData={
					_search: search,
					page: grid.jqGrid('getGridParam', 'page'),
					rows: grid.jqGrid('getGridParam', 'records'),
					sidx: grid.jqGrid('getGridParam', 'sortname'),
					sord: grid.jqGrid('getGridParam', 'sortorder')
			};
			var gridPostData = grid.jqGrid('getGridParam', 'postData');
			var group = gridPostData.group;
			if (group) {
				postData.group = gridPostData.group;
			}
			
			if (search) {
				postData.searchField = gridPostData.searchField;
				postData.searchString = gridPostData.searchString;
				postData.searchOper = gridPostData.searchOper;
			}
			
			acme_ui.UTIL.downloadFile('/cruds/users/downloadCsv.ajax', postData);
		});
		$('#buttonDownloadCSV').tooltip();
	};
	pkg.start = function(){
		
		var dfd = $.Deferred();
		$.when(i18nPromise, crudutils.loaded).then(function(){
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.group.list")) {
				groupsCrud = crudutils.Crud({entity:'groups', 
					caption : I18N('admin.cruds.group.title'), 
					gridId : 'groupsGrid', 
					pagerId : 'groupsPager',
					customFormatter : groupsCustomFormatter,
					gridComplete : groupsGridComplete,
					afterComplete : crudutils.HELPERS.genericAfterComplete,
					add : false,
					edit : false,
					del : false
				});
				
				var groupsGrid =  groupsCrud.showGrid();
				$.when(groupsGrid,i18nPromise).then(function(){
					var leGrid = groupsCrud.leGrid;
					if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.toolbox.group.new")) {
						leGrid.jqGrid('navButtonAdd', '#groupsPager',
						{
							caption : "",
							buttonicon : "ui-icon-plus",
							position : "first",
							title :  I18N('web.home.admin.group.new'),
						    onClickButton : function(){
						    	var promise = launchModule(constants.LAUNCHER_IDS.group_new);
								$.when(promise).then(function(module){
									//TODO
								});
						    }
						});
					}
					leGrid.setGridParam({ onSelectRow : function(id){
							var row = $("#groupsGrid").jqGrid('getRowData',id);
							var groupName = row.name;
							usersCrud.postData({group : id});
							usersCrud.refresh();
							usersCrud.leGrid.jqGrid('setCaption', I18N('web.usersAndGroups.users.grid.title', [groupName])); 
						}
					});
				});
			}
			
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.user.list")) {
				//check if the application still have enough license for more users
				checkMaxUsers();
				usersCrud = crudutils.Crud({entity:'users', 
					caption : I18N('admin.cruds.user.title'), 
					gridId : 'usersGrid', 
					pagerId : 'usersPager', 
					customFormatter : usersCustomFormatter,
					gridComplete: usersCompleteGrid,
					afterComplete : crudutils.HELPERS.genericAfterComplete,
					add : false,
					edit : false,
					refresh : false,
					del : false,
					search : true
				});
				$("#devicePopup").dialog({modal:true,autoOpen:false,width:600,height:450});
				$('#identifier_help').popover({
					title : I18N('web.home.admin.devices.field.identifier'),
					content : I18N('web.home.admin.devices.identifier.msg')
				});
				var usersGridPromise = usersCrud.showGrid();
				$.when(usersGridPromise, i18nPromise).then(function(){
					var leGrid = usersCrud.leGrid;
					
					if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.toolbox.user.new")) {
						leGrid.jqGrid('navButtonAdd', '#usersPager',
						{
							caption : "",
							buttonicon : "ui-icon-plus",
							position : "first",
							title :  I18N('web.home.admin.user.new'),
						    onClickButton : function(){
						    	var promise = launchModule(constants.LAUNCHER_IDS.user_new);
								$.when(promise).then(function(module){
									//TODO
								});
						    }
						});
					}
					
					leGrid.jqGrid('navButtonAdd', '#usersPager',
					{
						caption : "",
						buttonicon : "ui-icon-refresh",
						title :  I18N('web.generic.reloadGrid'),
					    onClickButton : function(){
					    	usersCrud.postData({group : ''});
					    	usersCrud.refresh();
					    	usersCrud.leGrid.jqGrid('setCaption', I18N('admin.cruds.user.title'));
					    }
					});
					
					$('td[title="Reload Grid"], td[title="Recargar lista"]').click(function(){
						$("#fbox_usersGrid_reset").trigger("click");
					});
					
					initExportUsersActions(leGrid);
				});
			}
			dfd.resolve();
		});
		return dfd.promise();
	};
	
	return pkg;
});
