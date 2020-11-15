define(["jquery", "acme", "jquery-ui", "acme-ui", "constants","sodep-multiselect/sodep-multiselect"], function($, acme, $ui, acme_ui,constants,sodepMultiselect) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.cruds.form.new.title','admin.cruds.form.edit.title',
	                                          'web.generic.requiredField','web.generic.delete','web.generic.action',
	                                          'web.generic.error', 'web.generic.none',
	                                          'admin.cruds.user.cols.id','admin.cruds.user.cols.mail',
	                                          'admin.cruds.user.cols.lastName','admin.cruds.user.cols.firstName',
	                                          'admin.cruds.user.cols.roles',
	                                          'admin.cruds.group.cols.id','admin.cruds.group.cols.name',
	                                          'admin.cruds.group.cols.description','admin.cruds.group.cols.roles',
	                  						  'admin.cruds.project.userGrid.title','admin.cruds.project.groupGrid.title',
	                  						  'admin.cruds.project.error.userRepeated','admin.cruds.project.error.groupRepeated',
	                  						  'web.generic.publish','web.generic.publishLastVersion','web.generic.unpublish',
	                  						  'admin.cruds.form.delete.confirmation.message', 'admin.cruds.form.delete.confirmation.title',
	                  						  'web.generic.cancel', 'web.generic.cancel','admin.cruds.form.missingRequiredFields','web.generic.revokeAuthorization.user',
	                  						  'web.generic.revokeAuthorization.group'
	                  						  ]);
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var jsonReader =  { 
		    root: "data", 
		    repeatitems: false,
		    page: "pageNumber",
		    total: "totalPages",
		    records: "totalCount",
		    id: "0"
	};
	
	
	var form=null;
	
	var userMultiselect=false,groupMultiselect=false, userRolesMultiselect=false,groupRolesMultiselect=false;
	var userRolesGrid=false,groupRolesGrid=false;
	var tab=false;
	
	//return true if its in edit mode
	var isEditMode=function(){
		return form!=null;
	};
	var deleteUserRolesFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'remove',
				'deleteUserRolesLink',
				I18N('web.generic.revokeAuthorization.user'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:95px;" >'+actions+'</html>';
	};
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*710/1024;
		$("#userRolesGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#groupRolesGrid").jqGrid('setGridWidth', desireWidth, true);		
	};
	var deleteGroupRolesFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'remove',
				'deleteGroupRolesLink',
				I18N('web.generic.revokeAuthorization.group'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:95px;" >'+actions+'</html>';
	};
	
	//Initialize the delegates for the table of users
	var initDelegatesUserRolesGrid = function(){
			$("#userRolesGrid").on("click", ".deleteUserRolesLink", function(event){
				if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit")) {
					var entityId = $(this).attr('rowid');
					removeEntityRoles(entityId,userRolesGrid);
				}
			});
			$("#userRolesGrid").on("mouseover", ".deleteUserRolesLink", function(event){
				$(this).tooltip('show');
			});
		
	};
	//Initialize the delegates for the table of groups
	var initDelegatesGroupRolesGrid = function(){
		
			$("#groupRolesGrid").on("click", ".deleteGroupRolesLink", function(event){
				if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit")) {
					var entityId = $(this).attr('rowid');
					removeEntityRoles(entityId,groupRolesGrid);
				}
			});
			$("#groupRolesGrid").on("mouseover", ".deleteGroupRolesLink", function(event){
				$(this).tooltip('show');
			});
		
	};
	
	var destroyUserGridToolTips = function() {
		$("#userRolesGrid").find(".deleteUserRolesLink").each(function(event){
			$(this).tooltip('hide');
		});
	};
	
	//FIXME there's an exact same method on new-project-crud-amd.js
	var rolesFormatter = function(cellvalue, options, rowObject){
		var str = ''; 
		if(rowObject.roles){
			var roles = rowObject.roles;
			if(roles.length > 0){
				for(var i = 0; i < roles.length - 1; i++){
					var role = roles[i];
					str += role.name + ', ';
				}
				str += roles[roles.length - 1].name;
			}
		}
		
		return str;
	};
	
	var initUserAuthsView = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit")) {
			if(!userMultiselect){
				userMultiselect=$('#multiSelectUsers').sodepMultiselect({id: 'users', single : true,
					selected : function(item, selectedArray){
						// $('#multiSelectUserRoles').sodepMultiselect('option', 'requestParams', null);
						$('#multiSelectUserRoles').sodepMultiselect('reload');
						$("#addUserRolesButton").attr("disabled",true);
					}
				});
			}
			$('#usersAuthEdit').show();
			if(!userRolesMultiselect){
				userRolesMultiselect=$('#multiSelectUserRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
					requestParams: {level : 'form'},
					initialParams : {label : 'web.generic.availableRoles'},
					selected : function(item, selectedArray){
						var user=$('#multiSelectUsers').sodepMultiselect('selected');
						if(selectedArray.length > 0 && user.length > 0) {
							$("#addUserRolesButton").removeAttr("disabled");
						}else {
							$("#addUserRolesButton").attr("disabled",true);
						}
					}
				});
			}
			$("#addUserRolesButton").show();
		}
		if(!userRolesGrid){
			userRolesGrid=$("#userRolesGrid").jqGrid({
				url : acme.VARS.contextPath + '/cruds/forms/userAuth/paging/read.ajax',
				datatype: 'json',
				jsonReader: jsonReader,
				mtype: 'POST',
				viewrecords: true,
				rownumbers: true,
				autowidth : false,
				rowNum : 5,
				rowList : [5,10,15],
				width : '100%',
				height : '100%',
				pager:'#userRolesPager',
				postData: {rootId:form.id},
				caption:I18N('admin.cruds.project.userGrid.title'),
				colNames:[I18N('admin.cruds.user.cols.id'),I18N('admin.cruds.user.cols.mail'),
				          I18N('admin.cruds.user.cols.lastName'),I18N('admin.cruds.user.cols.firstName'),
				          I18N('admin.cruds.user.cols.roles'),I18N('web.generic.action')],
				          colModel:[{name:'id',index:'id', width:10, hidden:true,sortable:false},
				                    {name:'mail',index:'mail', width:230},
				                    {name:'lastName',index:'lastName', width:150},
				                    {name:'firstName',index:'firstName', width:100},
				                    {name:'roles',index:'roles', width:150, formatter : rolesFormatter,sortable:false},
				                    {name:'action', index:'action', width:50, align:"center", formatter:deleteUserRolesFormatter,sortable:false}
				                    ]
			}).navGrid('#userRolesPager',{edit:false,add:false,del:false,search:false,search:false});
		}else{
			userRolesGrid.trigger("reloadGrid");
		}
		
		resizeTables();
	};
	
	
	
	
	
	var destroyGroupGridToolTips = function() {
		$("#groupRolesGrid").find(".deleteGroupRolesLink").each(function(event){
			$(this).tooltip('hide');
		});
	};
	
	
	var initGroupAuthsView = function() {
		
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit")) {
				if(!groupMultiselect){
					groupMultiselect=$('#multiSelectGroups').sodepMultiselect({id: 'groups', single : true,
						selected : function(item, selectedArray){
							$('#multiSelectGroupRoles').sodepMultiselect('reload');
							$("#addGroupRolesButton").attr("disabled",true);
						}
					});
				}
				if(!groupRolesMultiselect){
					groupRolesMultiselect=$('#multiSelectGroupRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
						requestParams : {level : 'form'},
						initialParams : {label : 'web.generic.availableRoles'},
						selected : function(item, selectedArray){
							var group=$('#multiSelectGroups').sodepMultiselect('selected');
							if(selectedArray.length > 0 && group.length > 0) {
								$("#addGroupRolesButton").removeAttr("disabled");
							}else {
								$("#addGroupRolesButton").attr("disabled",true);
							}
						}	
					});
				}
				$("#addGroupRolesButton").show();
			}
			if(!groupRolesGrid){
				groupRolesGrid=$("#groupRolesGrid").jqGrid({
					url : acme.VARS.contextPath + '/cruds/forms/groupAuth/paging/read.ajax',
					datatype: 'json',
					jsonReader: jsonReader,
					mtype: 'POST',
					viewrecords: true,
					rownumbers: true,
					autowidth : true,
					rowNum : 5,
					rowList : [5,10,15],
					width : '100%',
					height : '100%',
					pager:'#groupRolesPager',
					postData: {rootId:form.id},
					caption:I18N('admin.cruds.project.groupGrid.title'),
					colNames:[I18N('admin.cruds.group.cols.id'),I18N('admin.cruds.group.cols.name'),
					          I18N('admin.cruds.group.cols.description'),I18N('admin.cruds.group.cols.roles'),
					          I18N('web.generic.action')],
					          colModel:[{name:'id',index:'id', width:10, hidden:true,sortable:false},
					                    {name:'name',index:'name', width:100},
					                    {name:'description',index:'description', hidden:true,width:100},
					                    {name:'roles',index:'roles', width:300, formatter : rolesFormatter,sortable:false},
					                    {name:'action', index:'action', width:50, align:"center", formatter:deleteGroupRolesFormatter,sortable:false}
					                    ]
				}).navGrid('#groupRolesPager',{edit:false,add:false,del:false,search:false});
				
			}else{
				groupRolesGrid.trigger("reloadGrid");
			}
			resizeTables();
	};
	
	var saveEntityRoles = function(entityId,rolesId,grid) {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/saveEntityRoles.ajax';
		ajaxRequest.data=JSON.stringify({rootId:form.id,entityId:entityId,rolesId:rolesId});
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'formTabs';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
				grid.trigger("reloadGrid");
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var removeEntityRoles = function(entityId,grid) {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/removeEntityRoles.ajax';
		ajaxRequest.data={rootId:form.id,entityId:entityId};
		ajaxRequest.loadingSectionId = 'formTabs';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
				destroyUserGridToolTips();
				destroyGroupGridToolTips();
				grid.trigger("reloadGrid");
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var saveUserRoles = function() {
		var userId=$('#multiSelectUsers').sodepMultiselect('selected')[0];
		var rolesId=$('#multiSelectUserRoles').sodepMultiselect('selected');
		saveEntityRoles(userId,rolesId,userRolesGrid);
	};
	
	var saveGroupRoles = function() {
		var groupId=$('#multiSelectGroups').sodepMultiselect('selected')[0];
		var rolesId=$('#multiSelectGroupRoles').sodepMultiselect('selected');
		saveEntityRoles(groupId,rolesId,groupRolesGrid);
	};
	
	var initButtonsCallback=function(){
		$("#addUserRolesButton").click(function(){
			saveUserRoles();			
		});
		$("#addGroupRolesButton").click(function(){
			saveGroupRoles();			
		});
		$("#saveForm").click(function(){
			removeErrorMessages();
			if(controlRequiredFields()) {
				saveForm();
			}
		});
		$("#goEditor").off().click(function(){
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.editor,form.id);
		});
		$("#deleteForm").click(function(){
			deleteForm();
		});
		$("#publishButton").click(function(){
			removeErrorMessages();
			publishForm(form.id);
		});
		$("#publishLastVersionButton").click(function(){
			removeErrorMessages();
			publishForm(form.id);
		});
		$("#unpublishButton").click(function(){
			removeErrorMessages();
			unpublishForm(form.id);
		});
		$("#project_link").click(function(){
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.project_edit,$("#project_select").val());
		});
		initDelegatesUserRolesGrid();
		initDelegatesGroupRolesGrid();
	};
	
	var removeErrorMessages = function() {
		$('.error').each(function(index){
			$(this).removeClass('error');
			$(this).find('.errorLabel').remove();
		});
	};
	
	var controlRequiredFields = function() {
		var response=true;
		
		if(!isEditMode()){
			if($("#project_select").val()==='none') {
				$("#project_select_control_group").addClass("error");
				$("#project_select").parent().find(".errorLabel").remove();
				$("#project_select").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
				response = false;
			}
		}
		var label = $("#form_label").val();
		if (!/\S/.test(label)) {
			// string is empty or just whitespace
			$("#form_control_group_label").addClass("error");
			$("#form_label").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		if(!response){
			acme_ui.HTML_BUILDER.notifyError(I18N('web.generic.requiredField'),I18N('admin.cruds.form.missingRequiredFields'));
		}
		
		return response;
	};

	var saveForm = function() {
		var label = $("#form_label").val();
		var description = $("#form_description").val();
		var projectId ;
		var formId;
		if(isEditMode()){
			projectId=form.projectId;
			formId=form.id;
		}else{
			projectId=$("#project_select").val();
			formId=null;
		}
			
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/save.ajax';
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.data=JSON.stringify({projectId:projectId,rootId:formId,label:label,description:description});
		ajaxRequest.success = function(response){
			if(response.success) {
					
					if(response.computedAuthorizations){
						acme.AUTHORIZATION_MANAGER.setAuthorizations(response.computedAuthorizations);
					}
					initUI(response.obj);
				acme_ui.HTML_BUILDER.notifySuccess(response.title,response.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title,response.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var publishForm = function(formId) {
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/publish.ajax';
		ajaxRequest.data={rootId:formId};
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				form=obj.obj;
				$("#form_version").val(form.version);
				$("#form_version_published").val(formVersionPublishedFormatter(form.versionPublished));
				showOrHidePublishButtons();
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var unpublishForm = function(formId) {
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/unpublish.ajax';
		ajaxRequest.data={rootId:formId};
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				form=obj.obj;
				$("#form_version").val(form.version);
				$("#form_version_published").val(formVersionPublishedFormatter(form.versionPublished));
				showOrHidePublishButtons();
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var showOrHidePublishButtons = function(){
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.publish")){
			$("#formButtons").show();
			if(form.published) {
				if(form.version === form.versionPublished){
					$("#publishButton").hide();
					$("#publishLastVersionButton").hide();
					$("#unpublishButton").show();
				} else {
					$("#publishButton").hide();
					$("#publishLastVersionButton").show();
					$("#unpublishButton").show();
				}
			} else {
				$("#publishButton").show();
				$("#publishLastVersionButton").hide();
				$("#unpublishButton").hide();
			}
		}else{
			$("#publishButton").hide();
			$("#publishLastVersionButton").hide();
			$("#unpublishButton").hide();
		}
		
	};
	
	
	
	var deleteForm = function(){
		var title=I18N('admin.cruds.form.delete.confirmation.title');
		var msg=I18N('admin.cruds.form.delete.confirmation.message');
		acme_ui.confirmation(title,msg,function(){
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
	       		ajaxRequest.url += "/cruds/forms/delete.ajax";
	       		ajaxRequest.data = "id=" + form.id;
	       		ajaxRequest.loadingSectionId = "page_width";
	       		ajaxRequest.success = function(response){
	       			if (response.success) {
	       				var launchId = constants.LAUNCHER_IDS.process_manager;
	       				if (launchId) {
	       					acme.LAUNCHER.launch(launchId);
	       				} else {
	       					throw "Invalid launcher id";
	       				}
	       				acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message); 
	       			} else {
	       				acme_ui.HTML_BUILDER.notifyError(response.title, response.message); 
	       			}
	       		};
	       		$.ajax(ajaxRequest);
		});
		
	};
	
	
	//return a FormDTO with it last version
	var getFormLastVersion = function(formId) {
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/forms/getLastVersion.ajax';
		ajaxRequest.data={"formId":formId};
		return $.ajax(ajaxRequest);
	};
	
	
	var formVersionPublishedFormatter = function(versionPublished){
		if(versionPublished === null) {
			return I18N('web.generic.none');
		} else {
			return versionPublished;
		}
	};
	
	//Based on the authorizations hide/show the adequate elements
	var initUI = function(formObj) {
		form=formObj;
		
		if(isEditMode()){
			$("#form_label").val(form.label);
			$("#form_description").val(form.description);
			$("#form_version").val(form.version);
			$("#form_version_published").val(formVersionPublishedFormatter(form.versionPublished));
			$("#project_link").html(form.projectName);
			$("#project_label").val(form.projectName);
			$("#project_select").val(form.projectId);
			
			
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.edit")){
				$("#form_label").removeAttr('disabled').removeClass("disabled");
				$("#saveForm").show();
				$("#addUserRolesButton").show();
				$("#addGroupRolesButton").show();
				$(".hideIfNoEdit").show();
			}else{
				$(".hideIfNoEdit").hide();
			}
			
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.design")){
				$("#formButtons").show();
				$('#goEditor').show();
			}
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(form.id,"form.delete")){
				$("#formButtons").show();
				$("#deleteForm").show();
			}
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(form.projectId,"project.read.web")){
				$("#project_link").show();
				$("#project_label").hide();
			}else{
				$("#project_label").show();
				$("#project_link").off();
				$("#project_link").hide();
			}
			showOrHidePublishButtons();
			$("#version-control-group").show();
			$("#version-published-control-group").show();
			$("#project_link_control_group").show();
			$("#project_select_control_group").hide();
			// #3665
			// initTabs();
		}else{
			$("#formButtons").show();
			$("#version-control-group").hide();
			$("#version-published-control-group").hide();
			$("#project_link_control_group").hide();
			
			$("#project_select_control_group").show();
			//if its a new form, then let the user add it
			$("#form_label").removeAttr('disabled').removeClass("disabled");
			$("#saveForm").show();
		}
		
	};
	
	var initVars = function() {
		userMultiselect=false,groupMultiselect=false, userRolesMultiselect=false,groupRolesMultiselect=false;
		userRolesGrid=false,groupRolesGrid=false;
	};
	
	var initTabs = function() {
		$('a[href="#usersAuth"]').click(function () {
			//when the user authorization is selected make sure that the multiselects are loaded
			initUserAuthsView();			
		});
		$('a[href="#groupsAuth"]').click(function () {
			initGroupAuthsView();
		});
		$('#formTabs').show();
		initUserAuthsView();
	};
	
	
	//--Start of public methods//
	
	pkg.getFormLastVersion=getFormLastVersion;
	
	pkg.resized=function(){
		resizeTables();
	};
	
	pkg.start = function(options){
		var dfd = $.Deferred();
		
		var formId=null;
		if(options && options.formId) {
			formId=options.formId;
		}
		initVars();
		initButtonsCallback();
		
		$.when(i18nPromise).then(function(){
			require(["order!jqgrid/grid.locale-" + acme.I18N_MANAGER.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
				var formPromise;
				if(formId) {  //it's in edit mode
					formPromise=getFormLastVersion(formId);
				} else {  //it's in new mode
					if(options && typeof options.projectId != 'undefined') {
						$("#project_select").val(options.projectId);
					}
					//equal the promise to null so it will immediately be resolved and the GUI initiliazed for creation
					formPromise=null;
					
				}
				$.when(formPromise).then(function(formObj){
					initUI(formObj);
					dfd.resolve();
				});
			});
		});
		return dfd.promise();
	};
	pkg.stop=function(){
		//release the pointers to the DOM
		initVars();
	};
	return pkg;
});
