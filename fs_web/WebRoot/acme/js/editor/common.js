define(["jquery", "jquery-ui", "acme", "acme-ui", "home/data/lookuptable-amd"], function($, $ui, acme, acme_ui,lookupMod){

	var that = {};
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var cache = {};
	
	that.clearCache = function(){
		cache = {};
	};
	
	/**
	 * Removes an element from an array if the condition(e) is true for that element
	 * 
	 * @param array
	 * @param condition A function which is used to check if the array element 
	 * 		should be removed
	 * @returns the removed element or null if no element was removed
	 */
	var removeFromArray = that.removeFromArray = function(array, condition){
		var length = array.length;
		var e = null;
		for(var i = 0; i < length; i++){
			e = array[i];
			if(condition(e)){
				break;
			}
			e = null;
		}
		
		if(i<length){
			array.splice(i,1);
		}
		
		return e;
	};
	
	var removeIndexFromArray = that.removeIndexFromArray = function(array, index){
		var e = null;
		e = array[index];
		if(e == null){
			return null;
		}
		
		array.splice(index, 1);
		
		return e;
	};
	
	/**
	 * All the labels used in the editor are loaded here
	 * 
	 * @returns a promise that's completed when the labels are loaded
	 */
	that.loadI18NLabels = function(){
		var promise = acme.I18N_MANAGER.load([ "web.editor.dialog.title.saveAs",
	      	"web.editor.dialog.buttons.ok",
	      	"web.editor.dialog.buttons.cancel",
	      	"web.editor.dialog.form.invalidName",
	      	"web.editor.dialog.form.notSelected",
	      	"web.editor.toolbox.loadError",
	      	"web.editor.mainlayer.button.newPage",
	      	"web.editor.properties",
	      	"web.editor.properties.label",
	      	"web.editor.properties.selectItem",
	      	"web.editor.properties.type.form",
	      	"web.editor.properties.type.input",
	      	"web.editor.properties.type.select",
	      	"web.editor.properties.type.page",
	      	"web.editor.properties.fieldName",
	      	"web.editor.properties.required",
	      	"web.editor.form.newPage.label",
	      	"web.editor.error.notSaved",
	      	"web.editor.page.id",
	      	"web.editor.properties.position",
	      	"web.editor.element.id",
	      	"web.editor.newProcessItem.label",
	      	"web.editor.select.project",
	      	"web.editor.select.project.noForms",
	      	"web.editor.mainbuttons.cancel",
	      	"web.editor.mainbuttons.save",
	      	"web.editor.mainbuttons.saveAs",
	      	"web.editor.saved",
	      	"web.editor.noChanges",
	      	"web.editor.info",
	      	"web.editor.form.automatic.newpage",
	      	"web.generic.error",
	      	"web.editor.noChanges",
	      	"web.editor.noChanges.Title",
	      	"admin.form.processitem.type.text",
	      	"admin.form.processitem.type.date",
	      	"admin.form.processitem.type.time",
	      	"admin.form.processitem.type.password",
	      	"admin.form.processitem.type.integer",
	      	"admin.form.processitem.type.decimal",
	      	"admin.form.processitem.type.textarea",
	      	"admin.form.processitem.type.select",
	      	"admin.form.processitem.type.location",
	      	"admin.form.processitem.type.photo",
	      	"admin.form.processitem.type.datetime",
	      	"admin.form.processitem.type.headline",
	      	"admin.form.processitem.type.checkbox",
	      	"admin.form.processitem.type.barcode",
	      	"admin.form.processitem.type.signature",
	      	"admin.form.processitem.type.email",
	      	"admin.form.processitem.type.external_link",
	      	"web.editor.properties.saveable", 
	      	"web.editor.exit.unsaved",
	      	"web.editor.exit.unsaved.title",
	      	"web.generic.publishLastVersion",
	      	"web.generic.publish",
	      	"web.generic.unpublish",
	      	"web.generic.none",
	      	"admin.cruds.processitem.cols.type",
	      	"web.generic.version",
	      	"web.generic.versionPublished",
	      	"web.editor.properties.select.filter.label",
	      	"web.editor.properties.select.filter.button",
	      	"web.editor.properties.operators.equals",
	      	"web.editor.properties.dialogs.operators.equals",
	      	"web.editor.properties.dialogs.operators.contains",
	      	"web.editor.properties.dialogs.operators.distinct",
	      	"web.editor.properties.dialogs.selectColumn",
	      	"web.editor.properties.dialogs.selectOperator",
	      	"web.editor.properties.dialogs.selectElement",
	      	"web.editor.properties.dialogs.selectLookup",
	      	"web.editor.dialog.incrementversion.confirmation.title",
	      	"web.editor.dialog.incrementversion.confirmation.message",
	      	"web.editor.properties.page.navigation.label",
	      	"web.editor.properties.page.navigation.button",
	      	"web.editor.properties.dialogs.selectPage", 
	      	"web.editor.properties.dialogs.page.navigation.title",
	      	"web.editor.properties.dialogs.page.navigation.notification.noChanges.title",
	      	"web.editor.properties.dialogs.page.navigation.notification.noChanges.message",
	      	"web.editor.properties.dialogs.page.navigation.notification.changes.title",
	      	"web.editor.properties.dialogs.page.navigation.notification.changes.message",
	      	"web.editor.properties.dialogs.page.navigation.makefinal",
	      	"web.editor.properties.dialogs.page.navigation.makefinal.title",
	      	"web.editor.properties.dialogs.page.navigation.finalPage.notPossible",
	      	"web.editor.properties.dialogs.page.navigation.finalPage.notPossible.title",
	      	"web.generic.yes",
	      	"web.generic.no", 
	      	"web.editor.properties.dialogs.defaultvalue.title",
	      	"web.editor.properties.dialogs.dropdown.filters.selectLookupFirst",
	      	"web.generic.sorry",
	      	"web.editor.properties.dialogs.dropdown.filters.title",
	      	"web.editor.properties.defaultvalue.static",
	      	"web.editor.properties.defaultvalue.dynamic",
	      	"web.editor.save.nofinalPage.message",
	      	"web.editor.save.nofinalPage.title",
	      	"web.editor.properties.staticDefaultValueWillBeLost",
	      	"web.editor.properties.dynamicDefaultValueWillBeLost",
	      	"web.generic.warning",
	      	"web.editor.properties.dialogs.checkFilters",
	      	"web.editor.publish.unsavedChanges",
	      	"web.editor.publish.unsavedChanges.title",
	      	"web.editor.properties.dialogs.noMorePages.message",
	      	"web.editor.properties.dialogs.noMorePages.title",
	      	"web.editor.disable_location",
	      	"web.editor.enable_location",
	      	"web.editor.properties.dialogs.page.navigation.value",
	      	"web.editor.unreachablePages",
	      	"web.editor.unreachablePages.explanation",
	      	"web.editor.page.warning.unreachable",
	      	"web.editor.properties.dropdown.date_not_allowed_as_value",
	      	"web.editor.properties.dialogs.operators.greater_than",
	      	"web.editor.properties.dialogs.operators.less_than",
	      	"web.editor.element.id"
	      	]);
		return promise;
	};
	
	//FIXME This should come from the server
	/**
	 * Function used to get an icon based on the prototype's type
	 *
	 * @param proto ElementPrototype Object
	 * @returns A String like <i class="icon-*"></i> with the class according to the type of proto
	 */
	that.iconLink = function(proto){
		var iconClass="";
		switch (proto.type)
		{
		case "INPUT":
			switch(proto.subtype){
			case "TEXT":
				iconClass="icon-font";
				break;
			case "DATE":
				iconClass="icon-calendar";
				break;
			case "TIME":
				iconClass="icon-time";
				break;
			case "PASSWORD":
				iconClass="icon-lock";
				break;
			case "INTEGER":
				iconClass="icon-integer";
				break;
			case "DECIMAL":
				iconClass="icon-decimal";
				break;
			case "TEXTAREA":
				iconClass="icon-txt-area";
				break;
			case "EMAIL":
				iconClass="icon-envelope";
				break
			case "EXTERNAL_LINK":
				iconClass="icon-globe";
				break
			}
			break;
		case "SELECT":
			iconClass="icon-list-alt";
			break;
		case "LOCATION":
			iconClass="icon-map-marker";
			break;
		case  "PHOTO":
			iconClass="icon-camera";
			break;
		case "HEADLINE":
			iconClass="icon-text-height";
			break;
		case "CHECKBOX":
			iconClass="icon-check";
			break;
		case "BARCODE":
			iconClass="icon-barcode";
			break;
		case "SIGNATURE":
			iconClass="icon-pencil";
			break;
		case "AUDIO":
			iconClass="icon-volume-up";
			break
		default:
			iconClass="icon-chevron-right";
			break;//FIXME use a default icon
		}
		
		return '<i class="'+iconClass+'"></i>';//<span class="protoLabel" >'+proto.label+'</span>';
	};
	
	that.requestLookupTableColumns = function(lookupId){
		var dfd = $.Deferred();
		var loadDefPromise;
		if(!cache.lookupTableColumns){
			cache.lookupTableColumns = {};
		}
		
		if(!cache.lookupTableColumns[lookupId]){
			loadDefPromise=lookupMod.loadLookupDefinition(lookupId);
			$.when(loadDefPromise).then(function(lookupDef){
				cache.lookupTableColumns[lookupId] = lookupDef.fields;
				dfd.resolve({success: true, list : lookupDef.fields});
			});
		} else {
			dfd.resolve({success : true, list : cache.lookupTableColumns[lookupId]});
		}
		
		return dfd.promise();
	};
	
	that.requestLookupList=function(forceReload){
		if(!cache.lookupTables||forceReload){
			var lookupsLoaded=lookupMod.listLookupTables();
			$.when(lookupsLoaded).then(function(list){
				cache.lookupTables=list;
			});
			return lookupsLoaded;
		}else{
			return cache.lookupTables;
		}
	};
	
	that.loadDropdownValues = function(proto){
		var dfd = $.Deferred();
		var values = [];
		if(proto.source == 'EMBEDDED'){
			var embeddedValues = proto.embeddedValues;
			var splitted = embeddedValues.split(/\n/);
			for(var i = 0; i < splitted.length; i++){
				var val = splitted[i];
				val = val.trim();
				if(val.length > 0){
					values.push({label : val, value: val});
				}
			}
			dfd.resolve({success : true, values : values});
		} else {
			var lookupTableId = proto.lookupTableId;
			var lookupLabel = acme.UTIL.decodeHTML(proto.lookupLabel);
			var lookupValue = acme.UTIL.decodeHTML(proto.lookupValue);
			
			var promise = lookupMod.listLookupTableColumnData(lookupTableId, [lookupLabel, lookupValue]);
			$.when(promise).then(function(result){
				if(result.success){
					var requestValues = result.values;
					for(var i = 0; i < requestValues.length; i++){
						var r = requestValues[i];
						values.push( {label : r[0], value : r[1]});
					}
					dfd.resolve({success : true, values : values});
				} else{
					dfd.resolve({success : false, values : values});
				}
			});
			
		}
		return dfd.promise();
	};
	
	that.init=function(){
		//clear the cache of lookup tables. Note that we don't clear the cache of lookup definition since the definition can't be changed. 
		//However, there can't be new lookups, so we need to refresh it
		cache.lookupTables=null;
	};
	that.PROPERTIES_DIALOGS = function() {
		
		var PROPERTIES_DIALOGS_ret = {};
		
		var width = 800;
		
		var height = "auto";
		
		var operatorList = null;
		
		var model = null;
		
		var addOptionsToSelects = function(row, filter){
			var columnSelect = row.find('#columnSelect');
			
			acme_ui.HTML_BUILDER.drawSelectOptions(columnSelect, this.columnList,I18N('web.editor.properties.dialogs.selectColumn'),"-1","columnName","columnName");
			if(filter.column){
				columnSelect.val(filter.column);
			}
			
			var operatorSelect = row.find('#operatorSelect');
			operatorSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectOperator'))); 
			acme_ui.HTML_BUILDER.drawSelectOptions(operatorSelect, operatorList);
			if(filter.operator){
				operatorSelect.val(filter.operator);
			}
			
			var elementSelect = row.find('#elementSelect');
			elementSelect.append(acme_ui.HTML_BUILDER.createSelectOption("-1", I18N('web.editor.properties.dialogs.selectElement')));
			acme_ui.HTML_BUILDER.drawSelectOptions(elementSelect, this.elementList);
			if(filter.rightValue){
				elementSelect.val(filter.rightValue);
			}
		};
		
		var destroy = function(dialog){
			dialog.dialog('destroy');
			// dialog.remove();	
		};
		
		var init = function(div, funcCallback){
			//FIXME this condition depends on the data type
			operatorList = [{id:'EQUALS', label : I18N('web.editor.properties.dialogs.operators.equals')}, 
		                     {id:'CONTAINS', label : I18N('web.editor.properties.dialogs.operators.contains')}, 
		                     {id:'DISTINCT', label : I18N('web.editor.properties.dialogs.operators.distinct')}];
			
			div.dialog({
				buttons : [ 
					{
						text : I18N("web.editor.dialog.buttons.cancel"),
						click : function() {
							var dialog = $(this).dialog("close");
							destroy(dialog);
							//funcCallback(null);
						}
					},
					{
						text : I18N("web.editor.dialog.buttons.ok"),
						click : function(){
							var dialog = $(this);
							dialog.find(".error").removeClass("error");
							var valid = true;
							for(var i = 0; i < model.filters.length; i++){
								var filter = model.filters[i];
								if((!filter.rightValue || filter.rightValue == "-1" 
										|| !filter.column || filter.column == "-1" 
										|| !filter.operator || filter.operator == "-1") 
										&&  !((!filter.rightValue || filter.rightValue == "-1") 
												&& (!filter.column || filter.column == "-1")
												&& (!filter.operator || filter.operator == "-1"))){
									// alert("The filter " + i + " is not complete");
									valid = false;
									dialog.find("#filterContainer").find(".filterRow:eq(" + i + ")").addClass("error");
								}
							}
							
							if(valid){
								var filters = [];
								for(var i = 0; i < model.filters.length; i++){
									var filter = model.filters[i];
									if(filter.rightValue && filter.rightValue != "-1" && filter.column && filter.column != "-1" 
											&& filter.operator && !filter.operator != "-1"){
										filters.push(filter);
									}
								}
								model.filters = filters;
								
								dialog.dialog("close");
								destroy(dialog);
								funcCallback(model);
							} else {
								alert(I18N('web.editor.properties.dialogs.checkFilters'));
							}
						}
					}
				],
				autoOpen : false,
				modal : true,
				width: width,
				height: height,
			});
		};
		
		var loadFilters = function(){
			var filters = this.model.filters;;
			for(var i = 0; i < filters.length; i++){
				var filter = filters[i];
				addFilter.apply(this, [filter]);
			}
			if(!this.model.filters || !this.model.filters.length){
				addFilter.apply(this);
			}
		};
		
		var addFilter = function(filterToLoad){
			var mRef = this;
			model = mRef.model;
			var index = null;
			var filter = null;
			
			if(filterToLoad){
				filter = filterToLoad;
				index = filter.index;
			} else {
				index = model.filters.length;
				
				filter = model.filters[index] = {
					column : null,
					operator : null,
					rightValue : null,
					index : index
				};	
			}
			
			var row = mRef.dialog.find('#' + this.IDs.filterRowDiv).clone().attr("id", null);
			addOptionsToSelects.apply(mRef, [row, filter]);
			
			row.find('#delete').click(function(){
				var currentIndex = filter.index;
				removeIndexFromArray(model.filters, currentIndex);
				row.remove();
				for(var i = currentIndex; i < model.filters.length; i++){
					var f = model.filters[i];
					f.index--;
				}
				
				if(model.filters.length == currentIndex && currentIndex){
					// we removed the last element
					var previous = currentIndex - 1;
					mRef.filterContainer.find('img#add:eq(' + previous + ')').show();
				}
				
				if(!model.filters.length){
					// we removed all the elements
					addFilter.apply(mRef);
				}
			});
			
			row.find('#add').click(function(){
				addFilter.apply(mRef);
			});
			
			row.find('#columnSelect').change(function(){
				filter.column = $(this).val();
			});
			
			row.find('#operatorSelect').change(function(){
				filter.operator = $(this).val();
			});
			
			row.find('#elementSelect').change(function(){
				filter.rightValue = $(this).val();
			});
			
			
			if(index > 0){
				var previous = index - 1;
				mRef.filterContainer.find('img#add:eq(' + previous + ')').hide();
			}
			
			mRef.filterContainer.show();
			row.appendTo(mRef.filterContainer);
			row.show('fast');
		};
		
		PROPERTIES_DIALOGS_ret.loadFilters = loadFilters;
		PROPERTIES_DIALOGS_ret.init = init;
		
		return PROPERTIES_DIALOGS_ret;
	}();
	
	return that;
});