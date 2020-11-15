define(["jquery", "acme", "jquery-ui", "acme-ui", "constants","sodep-multiselect/sodep-multiselect"], function($, acme, $ui, acme_ui,constants,sodepMultiselect) {
   
	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.requiredField',
	                                          'admin.cruds.pool.edit.title',
	                                          'admin.cruds.processitem.title',
	                                          'admin.cruds.processitem.cols.id',
	                                          'admin.cruds.processitem.cols.label',
	                                          'admin.cruds.processitem.cols.type',
	                                          'admin.cruds.user.cols.id','admin.cruds.user.cols.mail',
	                                          'admin.cruds.user.cols.lastName','admin.cruds.user.cols.firstName',
	                                          'admin.cruds.user.cols.roles',
	                                          'admin.cruds.group.cols.id','admin.cruds.group.cols.name',
	                  						  'admin.cruds.group.cols.description','admin.cruds.group.cols.roles',
	                  						  'admin.cruds.pool.userGrid.title','admin.cruds.pool.groupGrid.title',
	                  						  'web.generic.action','web.generic.delete',
	                  						  'admin.cruds.pool.delete.confirmation.message',
	                  						  'admin.cruds.pool.delete.confirmation.title',
	                  						  'web.generic.cancel', 'web.generic.ok',
	                  						'web.generic.revokeAuthorization.group','web.generic.revokeAuthorization.user'
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
	
	var poolId=null;
	
	var poolMultiselect=null,processItemMultiselect=null,processItemGrid=null;
	var userMultiselect=null,userRolesMultiselect=null,userRolesGrid=null;
	var groupMultiselect=null,groupRolesMultiselect=null,groupRolesGrid=null;
	
	
	var resizeTables=function(){
		var desireWidth=$(window).width()*710/1024;
		$("#processItemGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#userRolesGrid").jqGrid('setGridWidth', desireWidth, true);
		$("#groupRolesGrid").jqGrid('setGridWidth', desireWidth, true);		
	};
	
	var processItemActions=function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'new_window',
				'gotoProcessItemLink',
				I18N('web.processes.processitem.tooltip.manager'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:35px;" >'+actions+'</html>';
	};
	
	var initProcessItemsView = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
			if(!poolMultiselect){
				poolMultiselect=$('#poolMultiselect').sodepMultiselect({id : 'pools', single : true, checkboxes : true,
					requestParams:{excludePool:poolId},
					selected : function(item, selectedArray){
						if(item.selected){
							$('#processItemMultiselect').sodepMultiselect('option', 'requestParams', {'pool' : item.id});
							$('#processItemMultiselect').sodepMultiselect('reload');
						} else {
							$('#processItemMultiselect').sodepMultiselect('option', 'requestParams', {excludePool : poolId});
							$('#processItemMultiselect').sodepMultiselect('reload');
						}
						$("#addProcessItemButton").attr("disabled",true);
					},
					click :[
					        {
					        	tooltip : I18N('web.processes.pool.tooltip.manager'),
					        	icon: 'new_window',
					        	func:function(item, selectArray){
									acme.LAUNCHER.launch(constants.LAUNCHER_IDS.pool_edit, item.id);
								}
					        }
					        ] 
				});
			}
			
			if(!processItemMultiselect){
				processItemMultiselect=$('#processItemMultiselect').sodepMultiselect({id : 'processItems', checkboxes : true, single : true,
					requestParams:{excludePool:poolId},
					selected : function(item, selectedArray){
						if(selectedArray.length > 0) {
							$("#addProcessItemButton").removeAttr("disabled");
						}else {
							$("#addProcessItemButton").attr("disabled",true);
						}
					},
					click :[
					        {
					        	tooltip : I18N('web.processes.processitem.tooltip.manager'),
					        	icon: 'new_window',
					        	func:function(item, selectArray){
									acme.LAUNCHER.launch(constants.LAUNCHER_IDS.process_item_edit, {rootId:item.id});
								}
					        }
					        ] 
				});
			}
			
		}
		if(!processItemGrid){
			processItemGrid=$("#processItemGrid").jqGrid({
				url : acme.VARS.contextPath + '/cruds/pools/processItem/paging/read.ajax',
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
				pager:'#processItemPager',
				postData: {poolId:poolId},
				caption:I18N('admin.cruds.processitem.title'),
				colNames:[I18N('admin.cruds.processitem.cols.id'),I18N('admin.cruds.processitem.cols.label'),
				          I18N('admin.cruds.processitem.cols.type'),I18N('web.generic.action')],
				colModel:[{name:'id',index:'id', width:10, hidden:true},
				          {name:'label',index:'label',align:"center", width:100},
				          {name:'type',index:'type',align:"center", width:20,sortable:false},
				          {name:'action', index:'action',fixed:true, width:40, align:"center", formatter:processItemActions,sortable:false}
				          ]
			}).navGrid('#processItemPager',{edit:false,add:false,del:false});
		}
		resizeTables();
		
	};
	
	
	var deleteUserRolesFormatter = function(cellvalue, options, rowObject){
		var rowId=options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'remove',
				'deleteUserRolesLink',
				I18N('web.generic.revokeAuthorization.group'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:35px;" >'+actions+'</html>';
	};
	
	var userRolesGridComplete = function(){
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
			$(".deleteUserRolesLink").click(function(){
				var entityId = $(this).attr('rowid');
				removeEntityRoles(entityId,userRolesGrid);
			});
		}
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
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
			if(!userMultiselect){
				userMultiselect=$('#multiSelectUsers').sodepMultiselect({id: 'users', single : true,
					selected : function(item, selectedArray){
						// $('#multiSelectUserRoles').sodepMultiselect('option', 'requestParams', null);
						$('#multiSelectUserRoles').sodepMultiselect('reload');
						$("#addUserRolesButton").attr("disabled",true);
					}
				});
			}
			if(!userRolesMultiselect){
				userRolesMultiselect=$('#multiSelectUserRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
					requestParams : {level : 'pool'},
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
			
		}
		if(!userRolesGrid){
			userRolesGrid=$("#userRolesGrid").jqGrid({
				url : acme.VARS.contextPath + '/cruds/pools/userAuth/paging/read.ajax',
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
				pager:'#userRolesPager',
				postData: {poolId:poolId},
				gridComplete: userRolesGridComplete,
				caption:I18N('admin.cruds.pool.userGrid.title'),
				colNames:[I18N('admin.cruds.user.cols.id'),I18N('admin.cruds.user.cols.mail'),
				          I18N('admin.cruds.user.cols.lastName'),I18N('admin.cruds.user.cols.firstName'),
				          I18N('admin.cruds.user.cols.roles'),I18N('web.generic.action')],
				colModel:[{name:'id',index:'id', width:120, hidden:true},
				          {name:'mail',index:'mail', width:230},
				          {name:'lastName',index:'lastName', width:150},
				          {name:'firstName',index:'firstName', width:100},
				          {name:'roles',index:'roles', width:150, formatter : rolesFormatter},
				          {name:'action', index:'action', width:50, align:"center", formatter:deleteUserRolesFormatter}
				]
			}).navGrid('#userRolesPager',{edit:false,add:false,del:false});
		}
		resizeTables();
		
	};
	
	var deleteGroupRolesFormatter = function(cellvalue, options, rowObject){
		var rowId=options.rowId;
		var actions= acme_ui.iconForTable(
				rowId,
				'remove',
				'deleteGroupRolesLink',
				I18N('web.generic.revokeAuthorization.group'),
				function(){
					return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit");
				}
				
		);
		return '<div class="actionButtonOnTable" style="width:35px;" >'+actions+'</html>';
	};
	
	var groupRolesGridComplete = function(){
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
			$(".deleteGroupRolesLink").click(function(){
				var entityId = $(this).attr('rowid');
				removeEntityRoles(entityId,groupRolesGrid);
			});
		}
	};
	
	var initGroupAuthsView = function() {
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
			if(!groupMultiselect){
				groupMultiselect=$('#multiSelectGroups').sodepMultiselect({id: 'groups', single : true,
					selected : function(item, selectedArray){
						// $('#multiSelectGroupRoles').sodepMultiselect('option', 'requestParams', null);
						$('#multiSelectGroupRoles').sodepMultiselect('reload');
						$("#addGroupRolesButton").attr("disabled",true);
					}
				});
			}
			if(!groupRolesMultiselect){
				groupRolesMultiselect=$('#multiSelectGroupRoles').sodepMultiselect({id : 'roles', autoRequest : true, checkboxes:true,
					requestParams:{level : 'pool'},
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
			
		}
		groupRolesGrid=$("#groupRolesGrid").jqGrid({
			url : acme.VARS.contextPath + '/cruds/pools/groupAuth/paging/read.ajax',
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
			postData: {poolId:poolId},
			gridComplete: groupRolesGridComplete,
			caption:I18N('admin.cruds.pool.groupGrid.title'),
			colNames:[I18N('admin.cruds.group.cols.id'),I18N('admin.cruds.group.cols.name'),
			          I18N('admin.cruds.group.cols.description'),I18N('admin.cruds.group.cols.roles'),
			          I18N('web.generic.action')],
			colModel:[{name:'id',index:'id', width:10, hidden:true},
			          {name:'name',index:'name', width:100},
			          {name:'description',index:'description',hidden:true, width:100},
			          {name:'roles',index:'roles', width:300, formatter : rolesFormatter},
			          {name:'action', index:'action', width:50, align:"center", formatter:deleteGroupRolesFormatter}
			]
		}).navGrid('#groupRolesPager',{edit:false,add:false,del:false});
		resizeTables();
	};
	
	var saveEntityRoles = function(entityId,rolesId,grid) {
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/pools/saveEntityRoles.ajax';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.data=JSON.stringify({poolId:poolId,entityId:entityId,rolesId:rolesId});
		ajaxRequest.loadingSectionId = 'poolTabs';
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
		ajaxRequest.url += '/cruds/pools/removeEntityRoles.ajax';
		ajaxRequest.data={poolId:poolId,entityId:entityId};
		ajaxRequest.loadingSectionId = 'poolTabs';
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
	
	
	
	var removeErrorMessages = function() {
		$('.error').each(function(index){
			$(this).removeClass('error');
			$(this).find('.errorLabel').remove();
		});
	};
	
	var controlRequiredFields = function() {
		var response=true;
		var name = $("#pool_name").val();
		if (!/\S/.test(name)) {
			// string is empty or just whitespace
			$("#name_control_group").addClass("error");
			$("#pool_name").parent().append('<p class="errorLabel">' + I18N('web.generic.requiredField') +'</p>');
			response = false;
		}
		return response;
	};

	var savePool = function() {
		var name = $("#pool_name").val();
		var description = $("#pool_description").val();
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/pools/save.ajax';
		ajaxRequest.loadingSectionId = 'page_width';
		ajaxRequest.contentType='application/json; charset=utf-8';
		ajaxRequest.data=JSON.stringify({poolId:poolId,name:name,description:description});
		ajaxRequest.success = function(response){
			if(response.success) {
				if(!poolId) {
					poolId=response.obj;
					acme.AUTHORIZATION_MANAGER.setAuthorizations(response.computedAuthorizations);
					initTabs();
					initUI();
				}
				acme_ui.HTML_BUILDER.notifySuccess(response.title,response.message);
				$('#legend').empty().append(I18N('admin.cruds.pool.edit.title'));
				
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.title,response.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	var importProcessItem = function() {
		var elementId=$('#processItemMultiselect').sodepMultiselect('selected')[0];
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/cruds/pools/importProcessItem.ajax';
		ajaxRequest.data={poolId:poolId,elementId:elementId};
		ajaxRequest.loadingSectionId = 'poolTabs';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
				processItemGrid.trigger("reloadGrid");
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
		};
		$.ajax(ajaxRequest);
	};
	
	
	
	var deletePool = function(){
		$('<div></div>').html(I18N('admin.cruds.pool.delete.confirmation.message'))
		.dialog( { 	
			buttons : [
		  	           {	
		  	        	   id : "confirmation_ok_button",
		  	        	   text : I18N('web.generic.ok'),
		  	        	   click : function(){
		  	        			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		  	        			ajaxRequest.url += "/cruds/pools/delete.ajax";
		  	        			ajaxRequest.data = "id=" + poolId;
		  	        			ajaxRequest.loadingSectionId = "page_width";
		  	        			ajaxRequest.success = function(response){
		  	        				if (response.success) {
		  	        					var launchId = constants.LAUNCHER_IDS.process_manager;
		  	        					if (launchId) {
		  	        						acme.LAUNCHER.launch(launchId);
		  	        					} else {
		  	        						throw "Invalid launcher id";
		  	        					}
		  	        					acme_ui.HTML_BUILDER.notifySuccess(response.title, response.unescapedMessage); 
		  	        				} else {
		  	        					acme_ui.HTML_BUILDER.notifyError(response.title, response.unescapedMessage); 
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
			title : I18N('admin.cruds.pool.delete.confirmation.title')
		});

	};
	
	var loadPool = function(poolId) {
		var poolPromise = acme_ui.UTIL.downloadList({url:"/cruds/pools/get.ajax", data:"poolId=" + poolId});
		$.when(poolPromise).then(function(poolResponse){
			if(poolResponse.success) {
				var obj = poolResponse.obj;
				$("#pool_name").val(obj.name);
				$("#pool_description").val(obj.description);
			} 
		});
		return poolPromise;
	};
	
	var disablePoolEditing = function() {
		$("#pool_name").attr('disabled',true);
		$("#pool_description").attr('disabled',true);
		$('#poolProcessItemsEdit').hide();
		$('#usersAuthEdit').hide();
		$('#groupsAuthEdit').hide();
		$('#formButtons').hide();
	};
	
	var initCallbacks=function(){
		$("#addProcessItemButton").click(function(){
			importProcessItem();
		});
		$("#addUserRolesButton").click(function(){
			saveUserRoles();			
		});
		$("#addGroupRolesButton").click(function(){
			saveGroupRoles();
		});
		
		$("#save").click(function(){
			removeErrorMessages();
			if(controlRequiredFields()) {
				savePool();
			}
		});
		
		$('#delete').click(function(){
			deletePool();
		});
		
		
		$("#processItemGrid").on("click", ".gotoProcessItemLink", function(event){
				var processItemId = $(this).attr('rowid');
				acme.LAUNCHER.launch(constants.LAUNCHER_IDS.process_item_edit, {rootId:processItemId});
				
							
		});
		
		
		
	};
	var initUI = function() {
		
		if(poolId!=null){
			var hasAccessToSaveOrDelete=false;
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.edit")) {
				$("#save").show();
				hasAccessToSaveOrDelete=true;
				$(".displayOnEdit").show();
				$(".hideIfNoEdit").show();
				$(".editableField").removeAttr('disabled').removeClass("disabled");
			}
			if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnPool(poolId,"pool.delete")) {
				$("#delete").show();
				hasAccessToSaveOrDelete=true;
			}
			if(hasAccessToSaveOrDelete===false){
				$(".hideIfNoEditNorDelete").hide();
			}
			$("#poolTabs").show();
		}else{
			$(".editableField").removeAttr('disabled').removeClass("disabled");
			$("#formButtons").show();
			$("#save").show();
		}
		
		
	};
	
	var initVars = function() {
		poolMultiselect=null,processItemMultiselect=null,processItemGrid=null;
		userMultiselect=null,userRolesMultiselect=null,userRolesGrid=null;
		groupMultiselect=null,groupRolesMultiselect=null,groupRolesGrid=null;
	};
	
	
	var initTabs = function() {
		$('a[href="#poolProcessItems"]').click(function () {
			initProcessItemsView();
		});
		$('a[href="#usersAuth"]').click(function () {
			initUserAuthsView();
		});
		$('a[href="#groupsAuth"]').click(function () {
			initGroupAuthsView();
			
		});
		
		initProcessItemsView();
		
		
	};

	pkg.resized=function(){
		resizeTables();		
	};
	
	pkg.start = function(id){
		var dfd = $.Deferred();
		var poolPromise;
		if(typeof id != 'undefined') {
			poolId=id;
		}else {
			poolId=null;
		}
		initVars();
		initCallbacks();
		initUI();
		$.when(i18nPromise).then(function(){
			require(["order!jqgrid/grid.locale-" + acme.I18N_MANAGER.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
				if(poolId) { //it's in edit mode
					poolPromise = loadPool(poolId);
					initTabs();
				} else {  //it's in new mode
					poolPromise = {success:true};
				}
				$.when(poolPromise).then(function(){
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
	pkg.newProcessItem = function() {
		acme.LAUNCHER.launch(constants.LAUNCHER_IDS.process_item_new, {poolId:poolId});
	};
	
	return pkg;
});
