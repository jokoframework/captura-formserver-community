require([ "jquery", "acme", "jquery-ui", "constants","acme-ui", "notify/ui.notify" ], function($, acme, $ui, constants,acme_ui) {

	/**
	 * Id of the div that will hold the menu
	 */
	var menuItem = "mf_menu_content";

	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.myaccount',
	                                          'web.home.appchanger.dialog.message',
	                                          'web.home.appchanger.dialog.title',
	                                          'web.generic.cancel','web.generic.ok',
	                                          'web.generic.toolbox','web.noUserRights']);
	var authPromise=acme.AUTHORIZATION_MANAGER.load();
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var obtainListOfVisibleChildrens=function(childs){
		var visbleChildrens=[];
		var i=0;
		for (i = 0; i <childs.length; i++) {
			if(childs[i].visible){
				visbleChildrens[visbleChildrens.length]=childs[i];
			}
		}
		return visbleChildrens;
	};
	
	var renderMenu = function(menuList) {
		var i,j;
		var menu ; //'<div class="nav-collapse">'; 
		menu ='';// '<ul class="nav">';
		var visbleChildrens=[];
		var hasChildrens=false;
		for (i = 0; i < menuList.length; i++) {
			if(menuList[i]&&menuList[i].visible){
				if(menuList[i].childrens.length > 0) {
					visbleChildrens=obtainListOfVisibleChildrens(menuList[i].childrens);
					if(visbleChildrens.length>0){
						hasChildrens=true;
						//If the menu has children, then it's a dropdown
						menu+= '<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown">' 
							+ acme_ui.UTIL.capitaliseFirstLetter(menuList[i].title) + '<span class="caret"></span></a>';
						menu+='<ul class="dropdown-menu mfChildMenu">';
						for (j = 0; j < visbleChildrens.length; j++) {
								menu+= '<li><a href="javascript:void(0)" id="item_' + i + '_' + j + '" >' 
								+acme_ui.UTIL.capitaliseFirstLetter(visbleChildrens[j].title) + '</a> </li>';							
						}	
						menu+='</ul></li>';
					}
				}
			} 
			if(!hasChildrens && menuList[i].launcherId&&menuList[i].visible){  
				//it's not a dropdown
				menu+= '<li><a href="javascript:void(0)" id="item_' + i + '" >' 
					+ acme_ui.UTIL.capitaliseFirstLetter(menuList[i].title) + '</a> </li>';
			
			}
		}
		//menu += '</ul>';
		// append the menu in the element with the Id menuItem
		var menuRoot = $("#" + menuItem);
		menuRoot.html(menu);
		var onClickFabric = function(menuItem) {
			return function(item) {
				acme.LAUNCHER.launch(menuItem.launcherId);
				//This is a hack. See #1321
				setTimeout(function(){
					$("#mf_menu_content li").removeClass("active");
					$("#mf_menu_content li").addClass("dropdown");
				}, 500);
				
			};
		};
		for (i = 0; i < menuList.length; i++) {
			visbleChildrens=obtainListOfVisibleChildrens(menuList[i].childrens);
			if(visbleChildrens.length >0) {
				for (j = 0; j < visbleChildrens.length; j++) {
					$("#item_" + i + "_" + j).click(onClickFabric(visbleChildrens[j]));
				}	
			} else {
				$("#item_" + i).click(onClickFabric(menuList[i]));
			}	
		}
		

	};
	
	var onClickProducer=function(launcherId){
		return function(){
			acme.LAUNCHER.launch(launcherId);
		};
	};
	var renderToolbox=function(toolbox){
		var glyphIcon;
		var i;
		var str="";
		
		$("#mf_toolbox").html(str);
		
		if(toolbox){
			childrens=toolbox.childrens;
			if(childrens){
			str='<div class="toolbox sidebar-nav">'; 	
			str+='<ul class="nav nav-list-captura">';
			str+='<li class="nav-header-captura">' + I18N('web.generic.toolbox') +'</li>';
			
			
			for(i=0;i<childrens.length;i++){
				glyphIcon=constants.LANUCHER_ID_TO_GLYPH_ICON[childrens[i].launcherId];
				str+='<li >';
				str+='<a id="toolbox_'+childrens[i].launcherId+'" href="javascript:void(0)" class="glyphicons toolboxIcon '+glyphIcon+'" >'+childrens[i].title+'</a>';
				str+="</li>";
				
			}
			str+="</ul>";
			$("#mf_toolbox").html(str);
			//loop to bind the events for each button
			for(i=0;i<childrens.length;i++){
				$("#toolbox_"+childrens[i].launcherId).click(onClickProducer(childrens[i].launcherId));
			}
		}
		
		
		}
	};
	
	var renderNavigatorOption=function(prefix,menu){
		var currentModule=acme.LAUNCHER.currentModule();
		if(menu.launcherId&&menu.triggerNavigatorLink 
				&& (currentModule&&currentModule.launcherId==menu.launcherId)){
			return '<li><span class="divider">/<span><a href="javascript:void(0)" id="'+prefix+menu.menuId+'" >'+ ' '+menu.title+'</a></li>';
		}else{
			return '<li><span class="divider">/<span>'+ ' '+menu.title+'</li>';
		}
		
	};
	
	var renderNavigator=function(menuActive){
		var html='',node,options=[],prefix="nav_item_",i;
		if(menuActive){
			//html='<ul class="breadcrumb">';
			html='';
			node=menuActive;
			while(node.parent){
				//ascent through the tree until reaching the root
				options[options.length]=renderNavigatorOption(prefix,node);
				node=node.parent;
			}
			//render the info the root
			options[options.length]=renderNavigatorOption(prefix,node);
			
			for(i=options.length-1;i>=0;i--){
				html+=options[i];
			}
			
			//html+='</ul>';
		}
		$("#mf_breadcrumb").html(html);
		
		node=menuActive;
		while(node.parent){
			//ascent through the tree until reaching the root to register the click events
			if(node.launcherId&&node.triggerNavigatorLink){
				$("#"+prefix+node.menuId).click( onClickProducer(node.launcherId) );
			}
			node=node.parent;
		}
		//root click event
		if(node.launcherId&&node.triggerNavigatorLink){
			$("#"+prefix+node.menuId).click( onClickProducer(node.launcherId) );
		}
	};
	
	//This method will search for the first visible page on the menu hierarchy
	var searchFirstPage=function(menuList){
		var i=0;
		var menu;
		for(i=0;i<menuList.length;i++){
			if(menuList[i]&&menuList[i].actionView&&menuList[i].visible){
				return menuList[i];
			}
			if(menuList[i].childrens){
				menu=searchFirstPage(menuList[i].childrens);
				if(menu){
					return menu;
				}
			}
			
		}
		
	};
	$(function() {
		acme.SessionManger.start();
		acme.LAUNCHER.SetRenderToolboxCallback(renderToolbox);
		acme.LAUNCHER.SetRenderMenuCallback(renderMenu);
		acme.LAUNCHER.SetRenderNavigatorCallback(renderNavigator);
		var menuLoaded = acme.LAUNCHER.loadMenu();
		var authoriationLoaded=acme.AUTHORIZATION_MANAGER.load();
		$.when(menuLoaded,authoriationLoaded,i18nPromise).then(function() {
			
			var menuList = acme.LAUNCHER.GetMenuOptions();
			// load the first tab
			$('.nav li').click(function(e) {
				$('.nav li').removeClass('active');
				var $this = $(this);
				if (!$this.hasClass('active')) {
					$this.addClass('active');
				}
			});
			var menuSelectedByDefault=searchFirstPage(menuList);
			if(menuSelectedByDefault){
				var loadingPage=acme.LAUNCHER.launch(menuSelectedByDefault);
				if(!loadingPage){
					acme.LOG.debug("The user doesn't have any active menu");
				}
			}else{
				acme.LOG.debug("The user doesn't have any active menu");
			}
			
		});
		
		$('#appChanger').change(function(){
			$.when(i18nPromise).then(function(){
				var appId = $('#appChanger').val();
				$('<div></div>').html(acme.I18N_MANAGER.getMessage('web.home.appchanger.dialog.message')).
				dialog({
					modal : true,
					title : acme.I18N_MANAGER.getMessage('web.home.appchanger.dialog.title'),
	                closeOnEscape: true,
					width : 300,
					height : 180,
					buttons:[
					    {
					    	text : acme.I18N_MANAGER.getMessage('web.generic.ok'), 
					    	click : function() {
								$(this).dialog('close');
								window.location.href = acme.VARS.contextPath + '/home/home.mob?appId=' + appId;
					    	}
					    },
					    {
					    
							text : acme.I18N_MANAGER.getMessage('web.generic.cancel'),
							click : function() {
								$(this).dialog('close');
							}
					    }
					]
				});
			});
		});
		
		$('#firstSetupLink').click(function(){
			var launcherId = constants.LAUNCHER_IDS.application_settings;
			var p = acme.LAUNCHER.launch(launcherId);
			$.when(p).then(function(module){
				acme.LOG.debug('application settings loaded');
			});
		});
		
		$('#myAccount').click(function(){
			var launcherId = constants.LAUNCHER_IDS.my_account;
			acme.LAUNCHER.launch(launcherId);
		});
		
		$('#appLicense').click(function(){
			var launcherId = constants.LAUNCHER_IDS.app_license;
			acme.LAUNCHER.launch(launcherId);
		});
		
		$("#notificationContainer").notify();
		
	});
});
