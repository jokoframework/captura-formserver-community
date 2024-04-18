define(["jquery", "acme", "jquery-ui", "acme-ui", "constants"], function($, acme, $ui, acme_ui, constants) {
	
	var that = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.toolbox']);
	
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
	
	that.renderMenu = function(menuList) {
		var menuItem = "mf_menu_content";
		var i,j;
		var menu ='';// '<ul class="nav">';
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
			if(!hasChildrens && menuList[i].launcherId){  
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
	
	that.renderToolbox = function(toolbox){
		var glyphIcon;
		var i;
		var str="";
		
		$("#mf_toolbox").html(str);
		var dfd = $.Deferred();
		if(toolbox){
			$.when(i18nPromise).then(function(){
				childrens=toolbox.childrens;
				if(childrens){
					str='<div class="toolbox sidebar-nav">'; 	
					str+='<ul class="nav nav-list-captura">';
					str+='<li class="nav-header-captura">' + I18N('web.generic.toolbox') +'</li>';
					
					for(i=0;i<childrens.length;i++){
						glyphIcon=constants.LANUCHER_ID_TO_GLYPH_ICON[childrens[i].launcherId];
						str+='<li >';
						str+='<a id="toolbox_'+childrens[i].launcherId+'" href="javascript:void(0)" class="glyphicons toolboxIcon '+glyphIcon+'" ><i></i>'+childrens[i].title+'</a>';
						str+="</li>";
						
					}
					str+="</ul>";
					$("#mf_toolbox").html(str);
					//loop to bind the events for each button
					for(i=0;i<childrens.length;i++){
						$("#toolbox_"+childrens[i].launcherId).click(onClickProducer(childrens[i].launcherId));
					}
					dfd.resolve();
				}
			});
		} else {
			dfd.resolve();
		}
		
		return dfd.promise();
	};
	
	var renderNavigatorOption = function(prefix,menu){
		return '<li><span class="divider">/<span><a href="javascript:void(0)" id="'+prefix+menu.menuId+'" >'+ ' '+menu.title+'</a></li>';
	};
	
	that.renderNavigator = function(menuActive){
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
	
	return that;
});