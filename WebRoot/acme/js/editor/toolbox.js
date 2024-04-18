define(["jquery", "jquery-ui", "acme", "acme-ui", "editor/common"],
		function($, $ui, acme, acme_ui, COMMON){
	
	var that = {};
	
	/**
	 * The prototype of the last item that was clicked
	 */
	var lastClickedPrototype = null;
	
	var clickedPrototypeFatory = function(proto){
		return function(){
			lastClickedPrototype = proto;
		};
	};
	
	var renderToolbox = function(cfg, div){
		div.html('');
		div.append('<div id="accordion"></div>');
		var accordion = $('#accordion');
		for(var i = 0; i < cfg.sections.length; i++){
			var section = cfg.sections[i];
			accordion.append('<h3><a style="color:#0088CC" href="javascript:void(0)">' + section.name + '</a></h3>');
			var sectionId = "accordion_section_" + section.id;
			
			accordion.append('<div id="'+ sectionId + '" style="overflow: hidden; position: static;" '
					+ ' class="toolboxSection"></div>');

			var tableId = "table_section_" + section.id;
			
			$('#' + sectionId).append('<div id="' + tableId + '" ></div>');
			var prototypes = section.prototypes;
			var prototypesTable = $('#' + tableId);
			for(var j = 0; j < prototypes.length; j++){
				var proto = prototypes[j];
				
				var prototypeDiv = $('<div></div>', {id: proto.id, "class": "draggable toolboxItem" });
				//#541
				var iLink = COMMON.iconLink(proto);
				var protoLabel='<span class="protoLabel" >'+proto.label+'</span>';
				
				prototypeDiv.html(iLink + protoLabel);
				prototypeDiv.draggable({	
					containment : "window", 
					helper : "clone", 
					revert : "invalid",
					connectToSortable : ".sortable",
					start : clickedPrototypeFatory(proto)
				});
				
				prototypeDiv.click(clickedPrototypeFatory(proto));
				
				
				prototypesTable.append(prototypeDiv);
				
			}
		}
		
		accordion.accordion({
			//autoHeight: false,
			navigation: false, 
			fillSpace: true,
			collapsible : true
		});
	};
	
	/**
	 * Sync request!
	 */
	var requestConfig = function(){
		var config = null;
		var ajax = acme.AJAX_FACTORY.newInstance();
		ajax.url += "/editor/toolbox/config.ajax";
		ajax.type = "GET";
		ajax.async = false;
		ajax.success = function(response){
			if(response.success){
				config = response.obj;
			}else{
				// This is meant to be an alert of some kind, not a log
				alert(I18N("web.editor.toolbox.loadError"));
			}
		};
		$.ajax(ajax);
		return config;
	};
	
	// Public Interface
	// --------------------------------------------------------//
	that.setup = function(div){
		var config = requestConfig();
		renderToolbox(config, div);
	};
	
	that.clickedPrototype = function(){
		return lastClickedPrototype;
	};
	
	return that;
	
});