define(["jquery", "jquery-ui", "acme", "acme-ui", "editor/model", "editor/common"], function($, $ui,  acme, acme_ui, MODEL, COMMON){
	
	var that = {};
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var IDs = { 
		dialog : 'dropdownFilterDialog',
		filterContainerDiv : 'filterContainer',
		filterRowDiv : 'filterRow'
	};

	var model = {
		lookupTable : null,
		filters : null
	};
	
	/**
	 * jQuery dialog object
	 */
	var dialog = null;
	
	var filterContainer = null;
	
	var elementList = null;
	
	var columnList = null;
	
	var init = function(callback){
		dialog = $('#' + IDs.dialog);
		COMMON.PROPERTIES_DIALOGS.init(dialog, callback);
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
		dialog.dialog('option', 'title', I18N('web.editor.properties.dialogs.dropdown.filters.title', [options.label]));
		filterContainer = dialog.find('#' + IDs.filterContainerDiv);
		filterContainer.children('div.filterRow').remove();

		model.lookupTable = options.lookupTable;
		if(!model.lookupTable){
			throw new  "Select a LookupTable!";
		}
		
		if(options.filters){
			model.filters = options.filters;
		} else {
			model.filters = [];
		}
		
		elementList = listOtherElements(options.instanceId);
		var columnsPromise = COMMON.requestLookupTableColumns(model.lookupTable);
		$.when(columnsPromise).then(function(response){
			if(response.success){
				columnList = response.list;
				COMMON.PROPERTIES_DIALOGS.loadFilters.apply({
					IDs : IDs,
					model : model,
					dialog : dialog,
					columnList : columnList,
					elementList : elementList,
					filterContainer : filterContainer
				});
			} else {
				throw "An error ocurred when trying to load the column list for lookup table id : " + model.lookupTable;
			}
		});
		
		dialog.dialog('open');
	};
	
	return that;
});