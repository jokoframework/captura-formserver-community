define(["jquery", "acme", "jquery-ui", "acme-ui", "constants","sodep-multiselect/sodep-multiselect"], function($, acme, $ui, acme_ui,constants,sodepMultiselect) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['admin.cruds.project.new.title','admin.cruds.project.edit.title',
	                                          'web.generic.requiredField','web.generic.delete','web.generic.action',
	                                          'web.generic.error',
	                                          'admin.cruds.user.cols.id','admin.cruds.user.cols.mail',
	                                          'admin.cruds.user.cols.lastName','admin.cruds.user.cols.firstName',
	                                          'admin.cruds.user.cols.roles',
	                                          'admin.cruds.group.cols.id','admin.cruds.group.cols.name',
	                  						  'admin.cruds.group.cols.description','admin.cruds.group.cols.roles',
	                  						  'admin.cruds.project.userGrid.title','admin.cruds.project.groupGrid.title',
	                  						  'admin.cruds.project.formGrid.title',
	                  						  'admin.cruds.form.cols.id','admin.cruds.form.cols.label',
	                  						  'admin.cruds.form.cols.version','admin.cruds.form.cols.versionPublished',
	                  						  'admin.cruds.form.cols.editForm','admin.cruds.form.cols.openEditor',
	                  						  'admin.cruds.project.delete.confirmation.title', 'admin.cruds.project.delete.confirmation.message', 
	                  						  'web.generic.cancel',
	                  						  'web.generic.publish', 'web.generic.unpublish','web.generic.none',
	                  						  'web.generic.publishLastVersion','admin.cruds.project.invalid.duplicated','web.generic.revokeAuthorization.user',
	                  						  'web.generic.revokeAuthorization.group']);
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var jsonReader =  { 
		    root: "data", 
		    repeatitems: false,
		    page: "pageNumber",
		    total: "totalPages",
		    records: "totalCount",
		    id: "0"
	};
	
	var projectId = null;
	
	var userMultiselect=false,groupMultiselect=false, userRolesMultiselect=false,groupRolesMultiselect=false;
	var projectMultiselect=false, formMultiselect=false;
	var userRolesGrid=false,groupRolesGrid=false,formsGrid=false;
	var tab=false;
	
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*710/1024;
		$("#formsGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#userRolesGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#groupRolesGrid").jqGrid('setGridWidth', desireWidth, true);
	};
	var deleteUserRolesFormatter = function(cellvalue, options, rowObject){
		var actions= acme_ui.iconForTable(
				options.rowId,
				'remove',
				'deleteUserRolesLink',
				I18N('web.generic.revokeAuthorization.user'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:35px;" >'+actions+'</html>';
	};
	
	var initDelegatesUserRolesGrid = function(){
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")) {
			$("#userRolesGrid").on("click", ".deleteUserRolesLink", function(event){
				var entityId = $(this).attr('rowid');
				removeEntityRoles(entityId,userRolesGrid);
			});
			$("#userRolesGrid").on("mouseover", ".deleteUserRolesLink", function(event){
				$(this).tooltip('show');
			});
		}
	};
	
	var destroyUserGridToolTips = function() {
		$("#userRolesGrid").find(".deleteUserRolesLink").each(function(event){
			$(this).tooltip('hide');
		});
	};
	
	//FIXME there's an exact same method on new-form-crud-amd.js
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
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")) {
			userMultiselect=$('#multiSelectUsers').sodepMultiselect({id: 'users', single : true,
				requestParams:{level : 'project'},
				selected : function(item, selectedArray){
					//	$('#multiSelectUserRoles').sodepMultiselect('option', 'requestParams', null);
					$('#multiSelectUserRoles').sodepMultiselect('reload');
					$("#addUserRolesButton").attr("disabled",true);
				}
			});
		
			userRolesMultiselect=$('#multiSelectUserRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
				requestParams : {level : 'project'},
				initialParams : {label : 'web.generic.availableRoles'},
				selected : function(item, selectedArray){
					var user=$('#multiSelectUsers').sodepMultiselect('selected');
					if(selectedArray.length > 0 && user.length > 0) {
						//if the user has made a valid selection (a user plus a role) then enable the button "addUserRolesButton"
						$("#addUserRolesButton").removeAttr("disabled");
					}else {
						$("#addUserRolesButton").attr("disabled",true);
					}
				}
			});
			$("#addUserRolesButton").show();
		}	
		var width=$(window).width();
		
		userRolesGrid=$("#userRolesGrid").jqGrid({
			url : acme.VARS.contextPath + '/cruds/projects/userAuth/paging/read.ajax',
			datatype: 'json',
			jsonReader: jsonReader,
		    mtype: 'POST',
		    viewrecords: true,
		    rownumbers: true,
	        autowidth : true,
		    rowNum : 5,
	        rowList : [5,10,15],
	        pager:'#userRolesPager',
	        width : '100%',
			height : '100%',
	        postData: {projectId:projectId},
			caption:I18N('admin.cruds.project.userGrid.title'),
			colNames:[I18N('admin.cruds.user.cols.id'),I18N('admin.cruds.user.cols.mail'),
			          I18N('admin.cruds.user.cols.lastName'),I18N('admin.cruds.user.cols.firstName'),
			          I18N('admin.cruds.user.cols.roles'),I18N('web.generic.action')],
			colModel:[{name:'id',index:'id', width:0,hidden:true,sortable:false},
			          {name:'mail',index:'mail', width:230},
			          {name:'lastName',index:'lastName', width:150},
			          {name:'firstName',index:'firstName', width:100},
			          {name:'roles',index:'roles', width:150, formatter : rolesFormatter,sortable:false},
			          {name:'action', index:'action', align:"center",width:50, formatter:deleteUserRolesFormatter,sortable:false}
			]
		}).navGrid('#userRolesPager',{edit:false,add:false,del:false,search:false});
		initDelegatesUserRolesGrid();
		resizeTables();
	};
	
	//creates a custom jqgrid formatter that output the string "delete"
	var deleteGroupRolesFormatter = function(cellvalue, options, rowObject){
		var actions= acme_ui.iconForTable(
				options.rowId,
				'remove',
				'deleteGroupRolesLink',
				I18N('web.generic.revokeAuthorization.group'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit");
				}
				
		);
		
		return '<div class="actionButtonOnTable" style="width:35px;" >'+actions+'</html>';
	};
	
	var initDelegatesGroupRolesGrid = function(){
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")) {
			$("#groupRolesGrid").on("click", ".deleteGroupRolesLink", function(event){
				var entityId = $(this).attr('rowid');
				removeEntityRoles(entityId,groupRolesGrid);
			});
			$("#groupRolesGrid").on("mouseover", ".deleteGroupRolesLink", function(event){
				$(this).tooltip('show');
			});
		}
	};
	
	var destroyGroupGridToolTips = function() {
		$("#groupRolesGrid").find(".deleteGroupRolesLink").each(function(event){
			$(this).tooltip('hide');
		});
	};
	
	var initGroupAuthsView = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")) {
			groupMultiselect=$('#multiSelectGroups').sodepMultiselect({id: 'groups', single : true,
				selected : function(item, selectedArray){
					// $('#multiSelectGroupRoles').sodepMultiselect('option', 'requestParams', null);
					$('#multiSelectGroupRoles').sodepMultiselect('reload');
					$("#addGroupRolesButton").attr("disabled",true);
				}
			});
			groupRolesMultiselect=$('#multiSelectGroupRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
				requestParams : {level : 'project'},
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
			$("#addGroupRolesButton").show();
		}
		groupRolesGrid=$("#groupRolesGrid").jqGrid({
			url : acme.VARS.contextPath + '/cruds/projects/groupAuth/paging/read.ajax',
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
			postData: {projectId:projectId},
			caption:I18N('admin.cruds.project.groupGrid.title'),
			colNames:[I18N('admin.cruds.group.cols.id'),I18N('admin.cruds.group.cols.name'),
			          I18N('admin.cruds.group.cols.description'),I18N('admin.cruds.group.cols.roles'),
			          I18N('web.generic.action')],
			colModel:[{name:'id',index:'id', width:10, hidden:true,sortable:false},
			          {name:'name',index:'name', width:100},
			          {name:'description',index:'description',hidden:true, width:100},
			          {name:'roles',index:'roles', width:300, formatter : rolesFormatter,sortable:false},
			          {name:'action', index:'action', align:"center", width:50, formatter:deleteGroupRolesFormatter,sortable:false}
			]
		}).navGrid('#groupRolesPager',{edit:false,add:false,del:false,search:false});
		initDelegatesGroupRolesGrid();
		resizeTables();
	};
	
	var formActionsFormatter = function(cellvalue, options, rowObject){
		var html = "";
		var formId = options.rowId;
		
		
			if(rowObject.published && rowObject.version === rowObject.versionPublished){
				html+=acme_ui.iconForTable(formId,'hand_down','unpublishLink',I18N('web.generic.unpublish'),function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.publish");
				});
				html+=acme_ui.iconForTable(formId,'hand_up','publishLink',I18N('web.generic.publish'),function(){
					return false;//do not activate the publish link if the form is currently published
				});
			}else{
				html+=acme_ui.iconForTable(formId,'hand_down','unpublishLink',I18N('web.generic.unpublish'),function(){
					return false;//do not activate the unpublish form it is currently not publish
				});
				html+=acme_ui.iconForTable(formId,'hand_up','publishLink',I18N('web.generic.publish'),function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.publish");
				});
			}
			
			html+=acme_ui.iconForTable(formId,'edit','openEditorLink',I18N('admin.cruds.form.cols.openEditor'),function(){
				return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.design");
			});
			
			return '<div class="actionButtonOnTable" style="width:95px;" >'+html+'</div>';
	};
	
	var formLabelFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		return '<a href="javascript:void(0)" rowid="' + rowId + '" class="editFormLink">' + cellvalue + '</a>';
	};
	
	var formVersionPublishedFormatter = function(cellvalue, options, rowObject){
		if(rowObject.versionPublished === null) {
			return I18N('web.generic.none');
		} else {
			return rowObject.versionPublished;
		}
	};
	
	var initDelegatesFormsGrid = function() {
		$("#formsGrid").on("mouseover", ".openEditorLink, .unpublishLink, .publishLink", function(event){
			$(this).tooltip('show');
		});
		
		$("#formsGrid").on("click", ".editFormLink", function(event){
			var formId = $(this).attr('rowid');
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.form_edit,{formId:formId});
		});
		
		$("#formsGrid").on("click", ".openEditorLink", function(event){
			var formId = $(this).attr('rowid');
			acme.LAUNCHER.launch(constants.LAUNCHER_IDS.editor,formId);
		});
		
		$("#formsGrid").on("click", ".publishLink", function(event){
			var formId = $(this).attr('rowid');
			ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/cruds/forms/publish.ajax';
			ajaxRequest.data={rootId:formId};
			ajaxRequest.success = function(obj){
				if(obj.success) {
					acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
					destroyFormGridToolTips();
					formsGrid.trigger("reloadGrid");
				} else {
					acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
				}
			};
			$.ajax(ajaxRequest);
		});
		
		$("#formsGrid").on("click", ".unpublishLink", function(event){
			var formId = $(this).attr('rowid');
			ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/cruds/forms/unpublish.ajax';
			ajaxRequest.data={rootId:formId};
			ajaxRequest.success = function(obj){
				if(obj.success) {
					acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
					destroyFormGridToolTips();
					formsGrid.trigger("reloadGrid");
				} else {
					acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
				}
			};
			$.ajax(ajaxRequest);
		});
	};
	
	var destroyFormGridToolTips = function() {
		$("#formsGrid").find(".openEditorLink, .unpublishLink, .publishLink").each(function(event){
			$(this).tooltip('hide');
		});
	};
	
	var initFormsView = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")) {
			if(!projectMultiselect){
				projectMultiselect=$('#projectMultiselect').sodepMultiselect({id: 'projects', single : true, checkboxes : true,
					requestParams:{auth : 'project.read.web',excludeProject:projectId},
					selected : function(item, selectedArray){
						if(item.selected){
							$('#formMultiselect').sodepMultiselect('option', 'requestParams', {'project' : item.id});
							$('#formMultiselect').sodepMultiselect('reload');
						} else {
							$('#formMultiselect').sodepMultiselect('option', 'requestParams', {excludeProject:projectId});
							$('#formMultiselect').sodepMultiselect('reload');
						}
						$("#addFormButton").attr('disabled',true);
					},
				});
			}
			if(!formMultiselect){
				formMultiselect=$('#formMultiselect').sodepMultiselect({id : 'forms', single : true, checkboxes : true,
					requestParams:{excludeProject:projectId},
					selected : function(item, selectedArray){
						if(selectedArray.length > 0) {
							$("#addFormButton").removeAttr("disabled");
						}else {
							$("#addFormButton").attr("disabled",true);
						}
					},
				});
			}
			
			
			
		}
		if(!formsGrid){
			formsGrid=$("#formsGrid").jqGrid({
				url : acme.VARS.contextPath + '/cruds/projects/forms/paging/read.ajax',
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
				pager:'#formsPager',
				postData: {projectId:projectId},
				caption:I18N('admin.cruds.project.formGrid.title'),
				colNames:[I18N('admin.cruds.form.cols.id'),I18N('admin.cruds.form.cols.label'),
				          I18N('admin.cruds.form.cols.version'),I18N('admin.cruds.form.cols.versionPublished'),
				          I18N('web.generic.action')],
				colModel:[{name:'id',index:'id', width:10, hidden:true},
				          {name:'label',index:'label',align:"center", width:150, formatter:formLabelFormatter},
				          {name:'version',index:'version',align:"center", width:50,sortable:false},
				          {name:'versionPublished',index:'versionPublished',align:"center", width:55, formatter:formVersionPublishedFormatter,sortable:false},
				          {name:'action', index:'action', width:95,fixed:true, align:"center", formatter:formActionsFormatter,sortable:false}
				          
				]
			}).navGrid('#formsPager',{edit:false,add:false,del:false,search:false});
		}
		resizeTables();
		
	};
	
	var saveEntityRoles = function(entityId,rolesId,grid) {
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/projects/saveEntityRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'projectTabs';
		ajaxRequest.data=JSON.stringify({projectId:projectId,entityId:entityId,rolesId:rolesId});
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
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/projects/removeEntityRoles.ajax';
		ajaxRequest.data={projectId:projectId,entityId:entityId};
		ajaxRequest.loadingSectionId = 'projectTabs';
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
	
	var importForm = function() {
		var formId = $('#formMultiselect').sodepMultiselect('selected')[0];
		if (formId) {
			ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/cruds/projects/importForm.ajax';
			ajaxRequest.loadingSectionId = 'projectTabs';
			ajaxRequest.data = { projectId : projectId, formId : formId};
			ajaxRequest.success = function(response){
				if(response.success) {
					acme_ui.HTML_BUILDER.notifySuccess(response.title,response.message);
					acme.AUTHORIZATION_MANAGER.setAuthorizations(response.computedAuthorizations);
					formsGrid.trigger("reloadGrid");
				} else {
					acme_ui.HTML_BUILDER.notifyError(response.title,response.message);
				}
			};
			$.ajax(ajaxRequest);
		}
	};
	
	
	
	var removeErrorMessages = function() {
		$('.error').each(function(index){
			$(this).removeClass('error');
			$(this).find('.errorLabel').remove();
		});
	};
	
	var controlRequiredFields = function() {
		var label = $("#project_label").val();
		if (!/\S/.test(label)) {
				// string is empty or just whitespace
				$("#project_control_group").addClass("error");
				$("#project_label").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
				return false;
		}
		return true;
	};
	
	var saveProject = function() {
		var label = $("#project_label").val();
		var description = $("#project_description").val();
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/projects/save.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.data=JSON.stringify({projectId:projectId,label:label,description:description});
		ajaxRequest.success = function(response) {
			if (response.success) {
				if (!projectId) {
					// if projectId is null it means that the user is
					// "creating" a new project, therefore we need to
					// enable the tabs where the user can edit the
					// form,users and groups
					//If the projectId wasn't null it means that the user was actually editing, so the tabs must already be enable (and we don't want to enable them twice)
					projectId = response.obj;
					//if a user has created an object his newly computed authorization will come after a successfull operation
					acme.AUTHORIZATION_MANAGER.setAuthorizations(response.computedAuthorizations);
					initTabs();
					initUI();
					
				}
				acme_ui.HTML_BUILDER.notifySuccess(response.title,response.message);
				$('#legend').empty().append(I18N('admin.cruds.project.edit.title'));				
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title,response.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	
	
	var deleteProject = function(){
		$('<div></div>').html(I18N('admin.cruds.project.delete.confirmation.message'))
		.dialog( { 	
			buttons : [
		  	           {	
		  	        	   id : "confirmation_ok_button",
		  	        	   text : I18N('web.generic.ok'),
		  	        	   click : function(){
		  	        		   var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		  	        		   ajaxRequest.url += "/cruds/projects/delete.ajax";
		  	        		   ajaxRequest.data = "id=" + projectId;
		  	        		   ajaxRequest.loadingSectionId = "page_width";
		  	        		   ajaxRequest.success = function(response) {
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
		  	        			$(this).dialog("close");
	  	        				$(this).dialog('detroy');
			  	        		$(this).remove();
		  	        	   }
		  	           },
		  	           {
		  	        	   id : "confirmation_cancel_button",
		  	        	   text : I18N('web.generic.cancel'),
		  	        	   click : function(){
		  	        		  $(this).dialog('close'); 
		  	        		  $(this).dialog('detroy');
		  	        		  $(this).remove();
		  	        	   }
		  	           }
		  	        ],
			autopen : true,
			modal : true,
			width: "320",
			height: "180",
			title : I18N('admin.cruds.project.delete.confirmation.title')
		});		
	};
	
	var loadProject = function(projectId) {
		var projectPromise = acme_ui.UTIL.downloadList({url:"/cruds/projects/get.ajax", data:"projectId=" + projectId});
		$.when(projectPromise).then(function(projectResponse){
			if(projectResponse.success) {
				var obj = projectResponse.obj;
				$("#project_label").val(obj.label);
				$("#project_description").val(obj.description);
			}
		});
		return projectPromise;
	};
	
	
	
	var initCallbacks=function(){
		$("#addUserRolesButton").click(function(){
			saveUserRoles();
			
		});
		$("#addGroupRolesButton").click(function(){
			saveGroupRoles();
		});
		$("#addFormButton").click(function(){
			importForm();			
		});
		
		$("#saveProject").click(function(){
			removeErrorMessages();
			if(controlRequiredFields()) {
				saveProject();
			}
		});
		
		$("#deleteProject").click(function(){
			deleteProject();
		});
	};
	var initUI = function() {
		
		if(projectId){
			//edit mode
			var hasAccessToSaveOrDelete=false;
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.edit")){
				$("#saveProject").show();
				hasAccessToSaveOrDelete=true;
				$(".displayOnEdit").show();
				$(".hideIfNoEdit").show();
				$(".editableField").removeAttr('disabled').removeClass("disabled");
			}
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.delete")){
				$("#deleteProject").show();
				hasAccessToSaveOrDelete=true;
			}
			if(hasAccessToSaveOrDelete===false){
				$(".hideIfNoEditNorDelete").hide();
			}
			$("#projectTabs").show();
			
		}else{
			$(".editableField").removeAttr('disabled').removeClass("disabled");
			$("#formButtons").show();
			$("#saveProject").show();
		}
		
	};
	
	var initVars = function() {
		userMultiselect=false,groupMultiselect=false,userRolesMultiselect=false,groupRolesMultiselect=false;
		projectMultiselect=false, formMultiselect=false;
		userRolesGrid=false,groupRolesGrid=false,formsGrid=false;
	};
	
	var initTabs = function() {
		$('a[href="#formsImport"]').click(function () {
			initFormsView();
			
		});
		$('a[href="#usersAuth"]').click(function () {
			initUserAuthsView();		
		});
		$('a[href="#groupsAuth"]').click(function () {
			initGroupAuthsView();	
			
		});
		initFormsView();
		
	};
	
	pkg.resized=function(){
		resizeTables();		
	};
	pkg.start = function(id){
		var dfd = $.Deferred();
		var projectPromise;
		if(typeof id != 'undefined') {
			projectId = id;
		}else {
			projectId = null;
		}
		initVars();
		initUI();
		initCallbacks();
		initDelegatesFormsGrid();
		$.when(i18nPromise).then(function(){
			require(["order!jqgrid/grid.locale-" + acme.I18N_MANAGER.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
				if(projectId) { //it's in edit mode
					projectPromise = loadProject(projectId);
					initTabs();
				} else {  //it's in new mode
					projectPromise = {success:true};
				}
				$.when(projectPromise).then(function(){
					
					
					dfd.resolve();
				});
			});
		});
		return dfd.promise();
	};
	pkg.stop=function(){
		initVars();
	};
	pkg.newForm = function() {
		var projectPromise=acme.LAUNCHER.launch(constants.LAUNCHER_IDS.form_new,{projectId:projectId});
	};
		
	return pkg;
});
