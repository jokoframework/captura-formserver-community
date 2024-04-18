define([ "jquery", "jquery-ui", "acme", "acme-ui", "editor/model", "editor/common","home/data/lookuptable-amd"], function($, $ui,  acme, acme_ui, MODEL, COMMON,lookupMod){
	
	var that = {};
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var IDs = { 
		dialog : 'defaultvalueOptionsDialog',
		lookupTableSelect : 'lookupTableSelect',
		valueSelect : 'valueSelect',
		filterContainerDiv : 'filterContainer',
		filterRowDiv : 'filterRow',
		filterSectionDiv : 'filterSection'
	};
	
	var model = {
		lookupTable : null,
		value : null,
		filters : null
	};
	
	/**
	 * jQuery dialog object
	 */
	var dialog = null;
	
	var lookupTableSelect = null;
	
	var valueSelect = null;
	
	var filterContainer = null;
	
	var elementList = null;
	
	var columnList = null;
	
	var init = function(callback){
		dialog = $('#' + IDs.dialog);
		COMMON.PROPERTIES_DIALOGS.init(dialog, callback);
	};
	
	var columnsPromise = null;

	var loadValueSelect = function(){
		if(model.lookupTableId){
			columnsPromise = COMMON.requestLookupTableColumns(model.lookupTableId);
			$.when(columnsPromise).then(function(response){
				if(response.success){
					var list = columnList = response.list;
					
					acme_ui.HTML_BUILDER.drawSelectOptions(valueSelect, list,I18N('web.editor.properties.dialogs.selectColumn'),"-1","columnName","columnName");
					if(model.valueColumn){
						valueSelect.val(model.valueColumn);
					}
					valueSelect.off().change(function(){
						var val = valueSelect.val();
						filterContainer.children('div.filterRow').remove();
						model.filters = [];
						if(val != "-1"){
							model.valueColumn = val;
							loadFilters();
							$('#' + IDs.filterSectionDiv).show();
						} else {
							$('#' + IDs.filterSectionDiv).hide();
						}
					});
				} else {
					throw "An error ocurred when trying to load the column list for lookup table id : " + model.lookupTableId;
				}
				columnsPromise = null;
			});
		}
	};
	
	var loadFilters = function(){
		$.when(columnsPromise).then(function(){
			COMMON.PROPERTIES_DIALOGS.loadFilters.apply({
				IDs : IDs,
				model : model,
				dialog : dialog,
				columnList : columnList,
				elementList : elementList,
				filterContainer : filterContainer
			});
		});
	};
	
	var listOtherElements = function(instanceId){
		var list = MODEL.listElements();
		COMMON.removeFromArray(list, function(p){
			return p.id === instanceId;
		});
		return list;
	};
	
	that.open = function(options, callback){
		init(callback);
		dialog.dialog('option', 'title', I18N("web.editor.properties.dialogs.defaultvalue.title", [options.label]));
		
		lookupTableSelect = dialog.find('#' + IDs.lookupTableSelect).empty();
		valueSelect = dialog.find('#' + IDs.valueSelect).empty();
		filterContainer = dialog.find('#' + IDs.filterContainerDiv);
		filterContainer.children('div.filterRow').remove();
		
		elementList = listOtherElements(options.instanceId);
			
		model.lookupTableId = options.lookupTableId ? options.lookupTableId : null;
		model.valueColumn = options.valueColumn ? options.valueColumn : null;
		model.filters = options.filters ? options.filters : [];
		
		var lookupTablePromise = lookupMod.listLookupTables();
		$.when(lookupTablePromise).then(function(list){
			
				 
				acme_ui.HTML_BUILDER.drawSelectOptions(lookupTableSelect, list,I18N('web.editor.properties.dialogs.selectLookup'),"-1","name","pk");
				
				if(options.lookupTableId){
					lookupTableSelect.val(model.lookupTableId);
					loadValueSelect();
					loadFilters();
					$('#' + IDs.filterSectionDiv).show();
				} else { 
					$('#' + IDs.filterSectionDiv).hide();
				}
				
				lookupTableSelect.off().change(function(){
					var val = lookupTableSelect.val();
					valueSelect.empty();
					filterContainer.children('div.filterRow').remove();
					if(val != "-1"){
						model.lookupTableId = val;
						model.valueColumn = null;
						model.filters = [];
						loadValueSelect();
					} else {
						$('#' + IDs.filterSectionDiv).hide();
					}
				});
			
		});
		
		dialog.dialog('open');
	};
	
	return that;
});