define([ "jquery", "jquery-ui", "acme", "acme-ui", "editor/model", "editor/common"], function($, $ui, acme, acme_ui, MODEL, COMMON) {

	var that = {};
	
	var width = 800;
	
	var height = "auto";

	var I18N = acme.I18N_MANAGER.getMessage;

	var IDs = {
		dialog : 'pageNavigationDialog',
		defaultTargetSelect : 'defaultTargetSelect',
		targetContainerDiv : 'targetContainer',
		targetRowDiv : 'targetRow'
	};

	var flow = null;
	
	/**
	 * jQuery dialog object
	 */
	var dialog = null;
	
	var defaultTargetSelect = null;
	
	var targetContainer = null;
	
	var elementList = null;
	
	var pageList = null;
	
	var operatorList = null;
	
	var changed = false;
	
	var textOperatorList = null;
	
	var numericOperatorList = null;
	
	var selectOperatorList = null;
	
	var init = function(page, callback){
		textOperatorList = [{id:'EQUALS', label : I18N('web.editor.properties.dialogs.operators.equals')},
         {id:'CONTAINS', label : I18N('web.editor.properties.dialogs.operators.contains')}, 
         {id:'DISTINCT', label : I18N('web.editor.properties.dialogs.operators.distinct')}];
		
		numericOperatorList =  [{id:'EQUALS', label : I18N('web.editor.properties.dialogs.operators.equals')},
         {id:'GT', label : I18N('web.editor.properties.dialogs.operators.greater_than')}, 
         {id:'LT', label : I18N('web.editor.properties.dialogs.operators.less_than')}];
		
		selectOperatorList = [{id:'EQUALS', label : I18N('web.editor.properties.dialogs.operators.equals')},
		 {id:'DISTINCT', label : I18N('web.editor.properties.dialogs.operators.distinct')}];
		
		dialog = $('#' + IDs.dialog);
		changed = false;
		defaultTargetSelect = dialog.find('#' + IDs.defaultTargetSelect).empty();
		targetContainer = dialog.find('#' + IDs.targetContainerDiv);
		targetContainer.children('div.target').remove();
		elementList = listElements(page);
		dialog.dialog({
			buttons : [ 
				{
					text : I18N("web.editor.dialog.buttons.cancel"),
					click : function() {
						var dialog = $(this).dialog("close");
						dialog.dialog('destroy');
						// callback(null);
					}
				},
				{
					text : I18N("web.editor.dialog.buttons.ok"),
					click : function(){
						//TODO validation control
						if(changed){
							callback(flow);
						} else {
							callback(null);
						}
						var dialog = $(this).dialog("close");
						dialog.dialog('destroy');
						
					}
				}
			],
			autoOpen : false,
			modal : true,
			width: width,
			height: height
		});
	};
	
	var addOptionsToSelects = function(row, target){
		var elementSelect = row.find('#elementSelect');
		elementSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectElement')));
		acme_ui.HTML_BUILDER.drawSelectOptions(elementSelect, elementList);
		if(target.elementId){
			elementSelect.val(target.elementId);
		}
		
		if(target.value){
			var index = elementSelect.find('option:selected').index();
			index--;
			if(index >= 0){
				var changeElementPromise = changeValueInputAccordingToElement(row, index, target);
				$.when(changeElementPromise).then(function(){
					row.find('#valueInput').val(target.value);
				});
				changeConditionSelectAccordingToElement(row, index, target);
			}
		}
		
		var pageSelect = row.find('#pageSelect');
		pageSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectPage'))); 
		acme_ui.HTML_BUILDER.drawSelectOptions(pageSelect, pageList);
		if(target.target){
			pageSelect.val(target.target);
		}
	};
	
	var changeValueInputAccordingToElement = function(row, index, target){
		var proto = elementList[index].proto;
		var valueInput = row.find('#valueInput');
		var newInput = null;
		var dfd = $.Deferred();
		if(proto.type == "SELECT"){
			newInput = $('<select></select>', {id : "valueInput", "class" : "span12"});
			$.when(COMMON.loadDropdownValues(proto)).then(function(response){
				if(response.success){
					var values = response.values;
					for(var i = 0; i < values.length; i++){
						var option = acme_ui.HTML_BUILDER.createSelectOption(values[i].value, values[i].label);
						newInput.append(option);
					}
					if(values.length > 0 && !target.value){
						target.value = values[0].value;
					}
					dfd.resolve({success : true});
				} else {
					alert("Couldn't load dropdown values");
					dfd.resolve({success : false});
				}
			});
		} else {
			dfd.resolve({success : true});
			newInput = $("<input></input>", { id: "valueInput", "class" : "span12", "placeholder" : I18N('web.editor.properties.dialogs.page.navigation.value')});
		} 
		
		if(proto.type == "DATE"){
			newInput.datepicker();
		} else if(proto.type == "DATETIME") {
			newInput.timepicker({
				ampm: true
			});
		}
		
		valueInput.after(newInput);
		valueInput.remove();
		
		if(newInput != null){
			newInput.change(function(){
				target.value = $(this).val();
				changed = true;
			});
		}
		return dfd.promise();
	};
	
	var changeConditionSelectAccordingToElement = function(row, index, target){
		var proto = elementList[index].proto;
		var operatorSelect = row.find('#operatorSelect');
		operatorSelect.empty();
		operatorSelect.removeAttr("disabled");
		operatorSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectOperator'))); 
		if(proto.type == 'INPUT' && (proto.subtype == 'INTEGER' || proto.subtype == 'DECIMAL')){
			acme_ui.HTML_BUILDER.drawSelectOptions(operatorSelect, numericOperatorList);
		} else if(proto.type == 'SELECT') {
			acme_ui.HTML_BUILDER.drawSelectOptions(operatorSelect, selectOperatorList);
		} else {
			acme_ui.HTML_BUILDER.drawSelectOptions(operatorSelect, textOperatorList);
		}
		
		if(target.operator){
			operatorSelect.val(target.operator);
		}
	};
	
	var addTarget = function(targetToLoad){
		var index = null;
		var target = null;
		
		if(targetToLoad){
			target = targetToLoad;
			index = target.index;
		} else {
			index = flow.targets.length;
			target = flow.targets[index] = {
				elementId : null,
				operator : null,
				value : null,
				target : null,
				index : index
			};	
		}
		
		var row = dialog.find('#' + IDs.targetRowDiv).clone().attr("id", null);
		addOptionsToSelects(row, target);
		
		row.find('#delete').click(function(){
			var currentIndex = target.index;
			COMMON.removeIndexFromArray(flow.targets, currentIndex);
			row.remove();
			for(var i = currentIndex; i < flow.targets.length; i++){
				var t = flow.targets[i];
				t.index--;
			}
			
			if(flow.targets.length == currentIndex && currentIndex){
				// we removed the last element
				var previous = currentIndex - 1;
				targetContainer.find('img#add:eq(' + previous + ')').show();
			}
			
			if(!flow.targets.length){
				// we removed all the elements
				addTarget(null);
			}
			changed = true;
		});
		
		row.find('#add').click(function(){
			addTarget();
			changed = true;
		});
		
		row.find('#elementSelect').change(function(){
			target.elementId = $(this).val();
			var index = $(this).find('option:selected').index();
			index--;
			if(index >= 0){
				changeValueInputAccordingToElement(row, index, target);
				changeConditionSelectAccordingToElement(row, index, target);
			} else {
				var valueInput = row.find('#valueInput');
				var newInput = $("<input></input>", { id: "valueInput", "class" : "span12", "placeholder" : I18N('web.editor.properties.dialogs.page.navigation.value'), disabled : ""});
				valueInput.after(newInput);
				valueInput.remove();
				var operatorSelect = row.find('#operatorSelect');
				operatorSelect.empty();
				operatorSelect.attr("disabled");
			}
			changed = true;
		});
		
		row.find('#operatorSelect').change(function(){
			target.operator = $(this).val();
			changed = true;
		});
		
		row.find('#valueInput').change(function(){
			target.value = $(this).val();
			changed = true;
		});
		
		row.find('#pageSelect').change(function(){
			target.target = $(this).val();
			changed = true;
		});
		
		
		if(index > 0){
			var previous = index - 1;
			targetContainer.find('img#add:eq(' + previous + ')').hide();
		}
		
		row.appendTo(targetContainer);
		row.show();
	};
	
	var loadTargets = function(){
		for(var i = 0; i < flow.targets.length; i++){
			var target = flow.targets[i];
			addTarget(target);
		}
		if (i == 0) {
			addTarget(null);
		}
	};
	
	var listElements = function(page){
		var elements = [];
		for(var i = 0; i < page.elements.length; i++){
			var element = page.elements[i];
			var proto = element.proto;
			if((proto.type == "INPUT" && 
					(proto.subtype == "TEXT" || proto.subtype == "TEXTAREA" || proto.subtype == "INTEGER" || proto.subtype == "DECIMAL"))
				|| proto.type == "SELECT"){
				
				var e = {
					id : element.instanceId,
					label : element.proto.label,
					position : element.position,
					page : {
						position : page.position
					},
					proto : proto
				};
				elements.push(e);
			} 
			
		}
		return elements;
	};
	
	var listOtherPages = function(page){
		var pages = MODEL.listPages();
		var list = [];
		for(var i = 0; i < pages.length; i++){
			var p = pages[i];
			if(p.position > page.position){
				list.push(p);
			}
		}

		return list;
	};
	
	var noMorePagesDialog = function(){
		$('<div></div>').html(I18N('web.editor.properties.dialogs.noMorePages.message')).
		dialog({
			autoOpen : false,
			modal : true,
			title : I18N('web.editor.properties.dialogs.noMorePages.title'),
			buttons : [ {
			    text : I18N("web.editor.dialog.buttons.ok"),
				click : function() {
					var dialog = $(this).dialog("close");
				}
			} ],
			width : "300",
			height : "200",
			closeOnEscape: false,
			close : function() {
				$(this).dialog('destroy');
				$(this).remove();
			}
		}).dialog('open');
	};
	
	var setFlow = function(page){
		if(page.flow){
			flow = $.extend({}, page.flow);
			if(page.flow.targets) {
				flow.targets = page.flow.targets.slice(); 
				for(var i = 0; i < page.flow.targets.length; i++){
					flow.targets[i] = $.extend({}, page.flow.targets[i]); 
				}
			} else {
				flow.targets = [];
			}
		} else {
			flow = {
				defaultTarget : null,
				targets : []
			};
		}
	};
	
	var loadDefaultTargetSelectOptions = function(){
		defaultTargetSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectPage'))); 
		acme_ui.HTML_BUILDER.drawSelectOptions(defaultTargetSelect, pageList);
		
		if(flow.defaultTarget){
			defaultTargetSelect.val(flow.defaultTarget);
		}
		defaultTargetSelect.change(function(){
			flow.defaultTarget = $(this).val();
			changed = true;
		});
		
	};
	
	that.open = function(page, callback){
		pageList = listOtherPages(page);
		if(!pageList.length){
			noMorePagesDialog();
		} else {
			init(page, callback);
			dialog.dialog('option', 'title', I18N("web.editor.properties.dialogs.page.navigation.title", [page.label]));
			setFlow(page);
			loadDefaultTargetSelectOptions();
			loadTargets(); 
			dialog.dialog('open');
		}
	};
	
	return that;

});