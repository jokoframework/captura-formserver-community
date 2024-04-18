define(["acme", "jquery","acme-ui", "sodep-multiselect/sodep-multiselect", "constants"], function(acme, $, acme_ui, sodepMultiselect, constants) {
	
	var pkg={};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.processes.project.tooltip.manager',
	                                          'web.processes.form.tooltip.manager',
	                                          'web.processes.form.tooltip.reports',
	                                          'web.processes.form.tooltip.editor',
	                                          'web.processes.pool.tooltip.manager',
	                                          'web.processes.processitem.tooltip.manager',
	                                          'web.generic.notYetImplemented']);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var hasAuth = acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp;
	
	var launchModule = function(launcherId, id){
		if(!launcherId){
			alert('Please define a launcher for handling the view/edition of the item');
			return;
		}
		
		var p = acme.LAUNCHER.launch(launcherId, id);
		return p;
	};
	
	var initProjectMultiselect = function(){
		$('#projectMultiselect').sodepMultiselect({id: 'projects', single : true, checkboxes : true,
			requestParams:{auth : 'project.read.web'}, 
			selected : function(item, selectedArray){
				if(item.selected){
					if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.form.list")) {
						$('#formMultiselect').sodepMultiselect('option', 'requestParams', {'project' : item.id});
						$('#formMultiselect').sodepMultiselect('reload');
					} else {
						$('#formMultiselect').sodepMultiselect('option', 'requestParams', null);
						$('#formMultiselect').sodepMultiselect('reload');
					}
				} else {
					$('#formMultiselect').sodepMultiselect('option', 'requestParams', null);
					$('#formMultiselect').sodepMultiselect('reload');
				}
			},
			click : 
				[
					 {
						tooltip : I18N('web.processes.project.tooltip.manager'),
						icon: 'cogwheel',
						func: function(item, selectArray){
							var launcherId = constants.LAUNCHER_IDS.project_edit;
							launchModule(launcherId, item.id);
						}
					 }
				]
		});
	};
	
	var initFormMultiselect = function(){
		$('#formMultiselect').sodepMultiselect({id : 'forms', autoRequest : true, checkboxes : false,
			click : 
				[
					{ 
						  tooltip : I18N('web.processes.form.tooltip.manager'),
						  icon: 'cogwheel',
						  func : function(item, selectArray){
							  var launcherId = constants.LAUNCHER_IDS.form_edit;
							  if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.open")){
								  launchModule(launcherId, {formId:item.id});
							  }
							  
						  },
						  authorizationFunction:function(item){
								return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.open");
						  }
					}
					,
					{ 
						 	tooltip : I18N('web.processes.form.tooltip.reports'),
						 	icon:'notes',
							func : function(item, selectArray){
								var launcherId = constants.LAUNCHER_IDS.reports;
								if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.viewReport")){
									acme.LAUNCHER.launch(launcherId, {formId:item.id});
								}
								
							},
							authorizationFunction:function(item){
								return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.viewReport");
							}
							
							
					 }
					
					,
					 { 
						 	tooltip : I18N('web.processes.form.tooltip.editor'),
							icon: 'pencil',
							func : function(item, selectArray){
								var launcherId = constants.LAUNCHER_IDS.editor;
								if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.design")){
									acme.LAUNCHER.launch(launcherId, item.id);
								}
								
							},
							authorizationFunction:function(item){
								return acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(item.id,"form.design");
							}
					 }
		        ]
		});
	};
	
	var initPoolMultiselect = function(){
		$('#poolMultiselect').sodepMultiselect({id : 'pools', single : true, checkboxes : true,
			selected : function(item, selectedArray){
				if(item.selected){
					if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnCurrentApp("application.processItem.list")) {
						$('#processItemMultiselect').sodepMultiselect('option', 'requestParams', {'pool' : item.id});
						$('#processItemMultiselect').sodepMultiselect('reload');
					} else {
						$('#processItemMultiselect').sodepMultiselect('option', 'requestParams', null);
						$('#processItemMultiselect').sodepMultiselect('reload');
					}
				} else {
					$('#processItemMultiselect').sodepMultiselect('option', 'requestParams', null);
					$('#processItemMultiselect').sodepMultiselect('reload');
				}
			},
			click : [
				        {
				        	tooltip : I18N('web.processes.pool.tooltip.manager'),
				        	icon: 'cogwheel',
							func : function(item, selectArray){
								var launcherId = constants.LAUNCHER_IDS.pool_edit;
								launchModule(launcherId, item.id);
							}
				        }
					]
		});
	};
	
	var initProcessItemMultiselect = function(){
		$('#processItemMultiselect').sodepMultiselect({id : 'processItems', autoRequest : true,checkboxes : false,
			click : 
			[
				{
					tooltip : I18N('web.processes.processitem.tooltip.manager'),
					icon: 'cogwheel',
					func : function(item, selectArray){
						var launcherId = constants.LAUNCHER_IDS.process_item_edit;
						launchModule(launcherId, {rootId : item.id});
					}
				}
			]
		});
	};
	
	pkg.start = function(){
		$.when(i18nPromise).then(function(){
			if(hasAuth("application.project.list")) {
				initProjectMultiselect();
			}
			
			if(hasAuth("application.form.list")) {
				initFormMultiselect();
			}
// 			#3665 requested to hide pools and process items
//			if(hasAuth("application.pool.list")) {
//				initPoolMultiselect();
//			}
//			
//			if(hasAuth("application.processItem.list")) {
//				initProcessItemMultiselect();
//			}
		});
		return i18nPromise;
	};
	
	return pkg;
});
