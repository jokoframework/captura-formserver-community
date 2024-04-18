define(["jquery", "jquery-ui", "acme", "acme-ui", "editor/model", "editor/common", 
        "editor/defaultvalue-options-dialog", 
        "editor/dropdown-filter-dialog",
        "editor/page-navigation-dialog","home/data/lookuptable-amd",
        "timepicker/timepicker","jquery.numeric"],
		function($, $ui,  acme, acme_ui, MODEL, COMMON, DEFAULTVALUE_OPTIONS_DIALOG, 
				DROPDOWN_FILTER_DIALOG, PAGE_NAVIGATION_DIALOG,lookupMod){
	
	/**
	 * Obj returned by this module
	 */
	var that = {};

	/**
	 * IDs of the html elements used in the editor 
	 */
	var IDS = {
		propertiesTable : 'propertiesTable',
		lookupSelect : 'property_proto_lookupTableId',
		lookupLabelSelect : 'property_proto_lookupLabel',
		lookupValueSelect : 'property_proto_lookupValue',
	};
	
	var propertiesTable = null;
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var selectedLookupTableColumns = null;
	
	var loadLookupColumns = function(proto){
		selectedLookupTableColumns = null;
		
		var dfd = $.Deferred();
		
		var identifier = proto.lookupTableId;
		var labelSelect = $('#' + IDS.lookupLabelSelect).empty();
		var valueSelect = $('#' + IDS.lookupValueSelect).empty();
		
		if(identifier){
			if(identifier != "-1"){
				var requestPromise = COMMON.requestLookupTableColumns(identifier);
				$.when(requestPromise).then(function(response){
					if(response.success){
						var list = response.list;
						selectedLookupTableColumns = list;
						var label = acme.UTIL.decodeHTML(proto.lookupLabel);
						acme_ui.HTML_BUILDER.drawSelectOptions(labelSelect, list,I18N('web.editor.properties.dialogs.selectColumn'),"-1","columnName","columnName");
						labelSelect.val(label);
						
						var value = acme.UTIL.decodeHTML(proto.lookupValue);
						acme_ui.HTML_BUILDER.drawSelectOptions(valueSelect, list,I18N('web.editor.properties.dialogs.selectColumn'),"-1","columnName","columnName");
						valueSelect.val(value);
						
						dfd.resolve({success:true});
					} else {
						dfd.resolve({success:false});
					}
				});
			}
		} else {
			return null;
		}
		return dfd.promise();
	};
	
	/**
	 * Shows the properties of a page on the properties view
	 */
	var showPageProperties = function(page){
		var pageType = I18N("web.editor.properties.type.page");
		propertiesTable.append('<tr class=""><td class="span4 propertyLabel">' + I18N('admin.cruds.processitem.cols.type') +
				'</td><td class="span8">' + pageType + '</td></tr>'); 
		var changeCallback = function(property, value){
			if(property === 'label'){
				$('#' + page.instanceId + 'Label').html(value);
			} else if(property === 'saveable'){
				//
				if(value === true) {
					if(page.flow){
						var dfd = $.Deferred();
						$('<div></div>').html(I18N('web.editor.properties.dialogs.page.navigation.makefinal')).
							dialog({
								autoOpen : false,
								buttons : [
								   {
								     text : I18N('web.generic.yes'),
								     click : function () {
								    	 $(this).dialog('close');
								    	 page.flow = null;
								    	 page.editCommand({"name": "defaultTarget", "value" : null});
								    	 page.editCommand({"name": "targets", "value" : null});
								    	 dfd.resolve({success: true});
								    	 $('#' + page.instanceId).addClass("saveable");
								    	 
								     }
								   },
								   {
									     text : I18N('web.generic.no'),
									     click : function () {
									    	 $(this).dialog('close');
									    	 dfd.resolve({success: false});
									     }
								   }
								],
								width : "300",
								height : "auto",
								title : I18N('web.editor.properties.dialogs.page.navigation.makefinal.title'),
								modal : true,
								open : function(){
									$(this).parent().find('a.ui-dialog-titlebar-close').css('display', 'none');
								},
								closeOnEscape: false,
								close : function() {
									$(this).dialog('destroy');
									$(this).remove();
								}
							}).dialog('open');
						return dfd.promise();
					} else {
						$('#' + page.instanceId).addClass("saveable");
					}
				} else {
					$('#' + page.instanceId).removeClass("saveable");
					//Where is this row hidden?
					$('tr#tr_navigationOptions').show();
				}
			}
		};
		// Copy values to show them
		for(p in page.propertyMetadata){
			if(page.propertyMetadata.hasOwnProperty(p)){
				page.propertyMetadata[p].value = page[p];
			}
		}
		
		appendPropertiesToTable(page, page.propertyMetadata, changeCallback);
		
		propertiesTable.append(
		'<tr id="tr_navigationOptions" class=""><td class="span4 propertyLabel">'
		+ I18N('web.editor.properties.page.navigation.label') +
		'</td><td class="span8"><input id="navigationOptions" type="button" class="btn" value="'
		+ I18N('web.editor.properties.page.navigation.button')
		+'" /></td>');
		
		$('#navigationOptions').click(navigationOptionsClickFactory(page));
		
	};
	
	var navigationOptionsClickFactory = function(page){
		return function(){	
			var callback = function(newFlow){
				if (newFlow) {
					page.flow = newFlow;
					if (newFlow.defaultTarget) {
						if(newFlow.defaultTarget){
							var targetPage = MODEL.getPageByInstanceId(newFlow.defaultTarget);
							if(!targetPage){
								page.editCommand({"name": "defaultTarget", "value" : null});
							} else {
								$('#' + targetPage.instanceId).removeClass("unreachable");
								page.editCommand({"name": "defaultTarget", "value" : targetPage.position});
							}
						}
					}
					
					if (newFlow.targets) {
						var targets = [];
						for (var i = 0; i < newFlow.targets.length; i++) {
							var targetCmd = {};
							var t = newFlow.targets[i];
							// validation should be done in the dialog but since we don't have it yet
							// we are simply going to discard the entries that aren't complete to avoid errors
							if(t.elementId && t.elementId != "-1" && t.operator && t.operator != "-1" && t.value && t.target && t.target != "-1") {
								var e = MODEL.getElementByInstanceId(t.elementId);
								if (e) {
									targetCmd.elementPosition = e.position.toString();
								} else {
									throw "Invalid Element";
								}
								targetCmd.operator = t.operator;
								targetCmd.value = t.value;
		
								var p = MODEL.getPageByInstanceId(t.target);
								if (p) {
									$('#' + p.instanceId).removeClass("unreachable");
									targetCmd.targetPagePosition = p.position;
								} else {
									throw "Invalid Page";
								}
								targets.push(targetCmd);
							} 
						}
						var value = JSON.stringify(targets);
						page.editCommand({"name" : "targets", value : value});
					}
					acme_ui.HTML_BUILDER.notifyInfo(I18N('web.editor.properties.dialogs.page.navigation.notification.changes.title'), 
								I18N('web.editor.properties.dialogs.page.navigation.notification.changes.message', [page.label])); 
				} else {
					acme_ui.HTML_BUILDER.notifyInfo(I18N('web.editor.properties.dialogs.page.navigation.notification.noChanges.title'), 
							I18N('web.editor.properties.dialogs.page.navigation.notification.noChanges.message', [page.label]));
				}
			};

			if(!page.saveable){
				PAGE_NAVIGATION_DIALOG.open(page, callback);
			} else {
				$('<div></div>').html(I18N('web.editor.properties.dialogs.page.navigation.finalPage.notPossible')).
					dialog({
						autoOpen : false,
						buttons : [
				           {
				        	   text : I18N('web.generic.ok'),
				        	   click : function(){
				        		   $(this).dialog('close');
				        	   }
				           }
				        ],
						width : "300",
						closeOnEscape : false,
						height : "auto",
						title : I18N('web.editor.properties.dialogs.page.navigation.finalPage.notPossible.title'),
						modal : true,
						open : function() {
							$(this).parent().find('a.ui-dialog-titlebar-close').css('display', 'none');
						},
						close : function(){
							$(this).dialog('destroy');
			        		$(this).remove();
						}
					}).dialog('open');
			}
		};
	};
	
	/**
	 * Shows the properties of a form on the properties view
	 */
	var showFormProperties = function(form){
		var formType = I18N("web.editor.properties.type.form");
		propertiesTable.append('<tr class=""><td class="span4 propertyLabel">' + I18N('admin.cruds.processitem.cols.type') + 
				'</td><td class="span8">' + formType + "</td></tr>");
		var changeCallback = function(property, value){
			if(property === 'label'){
				$('#formLabel').html(value);
			} else if(property === "provideLocation"){
				var locationLink = $('#locationLink');
				if(value){
					locationLink.addClass("locationOn").removeClass("locationOff").attr("title", I18N('web.editor.disable_location'));
				} else {
					locationLink.addClass("locationOff").removeClass("locationOn").attr("title", I18N('web.editor.enable_location'));
				}
			}
		};
		// Copy values to show them
		for(p in form.propertyMetadata){
			if(form.propertyMetadata.hasOwnProperty(p)){
				form.propertyMetadata[p].value = form[p];
			}
		}
		
		appendPropertiesToTable(form, form.propertyMetadata, changeCallback);
	};
	
	var elementInstanceChangeCallbackFactory = function(element){
		var proto = element.proto;
		return elementInstanceChangeCallback = function(property, value){
			if(property === 'proto_label'){
				//The element on the page is "repainted" to reflect the new label
				var elementUI = $('#' + element.instanceId);
				elementUI.children('span#liLabel').html(value);
			} else if(property === 'proto_source' && proto.type === 'SELECT'){
				if(value === 'EMBEDDED'){
					element.changeProperty('proto_lookupTableId', null);
					element.changeProperty('proto_lookupLabel', null);
					element.changeProperty('proto_lookupValue', null);
					element.changeProperty('itemListFilters', null); 
					
					$('#tr_proto_embeddedValues').show();
					$('#tr_proto_lookupTableId').hide();
					$('#tr_proto_lookupLabel').hide();
					$('#tr_proto_lookupValue').hide();
					$('#tr_filterOptions').hide();
					
					proto.propertyMetadata['embeddedValues'].visible = true;
					proto.propertyMetadata['lookupTableId'].visible = false;
					proto.propertyMetadata['lookupLabel'].visible = false;
					proto.propertyMetadata['lookupValue'].visible = false;
				} else {
					element.changeProperty('proto_lookupTableId', null);
					element.changeProperty('proto_lookupLabel', null);
					element.changeProperty('proto_lookupValue', null);
					element.changeProperty('itemListFilters', null);
					
					$('#tr_proto_embeddedValues').hide();
					$('#tr_proto_lookupTableId').show();
					$('#' + IDS.lookupSelect).children("option:eq(0)").attr("selected", true).val(element.proto.lookupTableId);	
					$('#tr_proto_lookupLabel').show();
					$('#tr_proto_lookupValue').show();
					$('#tr_filterOptions').show();
					
					loadLookupColumns(proto);
					
					proto.propertyMetadata['embeddedValues'].visible = false;
					proto.propertyMetadata['lookupTableId'].visible = true;
					proto.propertyMetadata['lookupLabel'].visible = true;
					proto.propertyMetadata['lookupValue'].visible = true;
					
				}
			} else if(property === 'proto_lookupValue' && selectedLookupTableColumns){
				for(var i = 0; i < selectedLookupTableColumns.length; i++){
					var c = selectedLookupTableColumns[i];
					if(c.columnName === value && c.type === "DATE"){
						acme_ui.HTML_BUILDER.notifyError(I18N('web.generic.error'), I18N('web.editor.properties.dropdown.date_not_allowed_as_value', [value]));
						var dfd = $.Deferred();
						dfd.resolve({success:false});
						return dfd.promise();
					}
				}
			}
			
		};
	};
	
	var prototypeProperties = function(proto){
		var prefixedProperties = {};

		for(p in proto.propertyMetadata){
			if (proto.propertyMetadata.hasOwnProperty(p)){
				proto.propertyMetadata[p].value = proto[p];
				// This is a hack, quick fix to identify properties from prototypes by their name prefix
				// "proto_" - jmpr 18/10/2012
				prefixedProperties['proto_' + p] = proto.propertyMetadata[p];
			}
		}
		return prefixedProperties;
	};
	
	/**
	 * Shows the properties of an "element instance" aka "process item" in the properties table
	 */
	var showElementInstanceProperties = function(element){
		var proto = element.proto;
		var type = I18N("admin.form.processitem.type." + proto.name);
		propertiesTable.append('<tr class=""><td class="span4 propertyLabel">'
				+ I18N('admin.cruds.processitem.cols.type') +
				'</td><td class="span8">' + COMMON.iconLink(proto) + '&nbsp;' + type +'</td></tr>');

		//1. We show the prototype properties
		var protoProperties = prototypeProperties(proto);
		appendPropertiesToTable(element, protoProperties, elementInstanceChangeCallbackFactory(element));
		
		if(proto.type === 'SELECT' && proto.embedded){
			$('#' + IDS.lookupSelect).change(function(){
				element.changeProperty('proto_lookupLabel', null);
				element.changeProperty('proto_lookupValue', null);
				loadLookupColumns(proto);
			});
			//#527
			addFiltersRow(element);
			loadLookupColumns(proto);
		}
		//#527
		if(proto.embedded && ((proto.type ==='INPUT' && proto.subtype ==='TEXT'))){
			modifyDefaultValueRow(element);
		}
		
		// to separate properties
//		propertiesTable.append('<tr></tr>');
//		propertiesTable.append('<tr><td colspan="2"><hr /></td></tr>' );
		//2.  Then we show the instance properties
		// Copy values to show them
		for(p in element.propertyMetadata){
			if(element.propertyMetadata.hasOwnProperty(p)){
				element.propertyMetadata[p].value = element[p];
			}
		}
		appendPropertiesToTable(element, element.propertyMetadata);
		if(!element.tempInstanceId){
			propertiesTable.append('<tr><td class="span4 propertyLabel">'
				 + '</td><td class="span8" style="color:#CCCCCC">[' + I18N('web.editor.element.id') + ' : ' + element.instanceId +']</td></tr>');
		}
	};
	
	var modifyDefaultValueRow = function(element){
		// default value's more options button
		var defaultValueTD = $('#tr_proto_defaultValue').find('td:eq(1)');
		var staticDefaultValueInput = defaultValueTD.children("input");
		staticDefaultValueInput.removeClass('span12').addClass('span10');
		
		var defaultValueRadioButtons = '<div style="margin-bottom:10px"><input id="staticDefaultValueRadio" type="radio" name="defaultValueType" style="margin:0px; margin-bottom:4px" >' + I18N('web.editor.properties.defaultvalue.static') +
			'&nbsp;<input id="dynamicDefaultValueRadio" type="radio" name="defaultValueType" style="margin:0px; margin-bottom:4px">' + I18N('web.editor.properties.defaultvalue.dynamic')  +'</div>';
		
		var dynamicDefaultValueInput = $('<input></input>', { id : "dynamicDefaultValueInput", type : "text", disabled : "disabled", "class" : "span10"});
		var dynamicDefaultValueButton = $('<input></input>', {"type" : 'button', "value" : '...', "class" : "btn span2"}).css("margin-bottom", "10px");
		
		defaultValueTD.prepend(defaultValueRadioButtons);
		var staticRadio = $('#staticDefaultValueRadio').click(function(){
			staticDefaultValueInput.show();
			dynamicDefaultValueInput.hide();
			dynamicDefaultValueButton.hide();
		});
		
		var dynamicRadio = $('#dynamicDefaultValueRadio').click(function(){
			staticDefaultValueInput.hide();
			dynamicDefaultValueInput.show();
			dynamicDefaultValueButton.show();
		});
		
		defaultValueTD.append(dynamicDefaultValueInput);
		defaultValueTD.append(dynamicDefaultValueButton);
		
		if(element.defaultValueLookupTableId){
			dynamicRadio.attr("checked", "checked");
			staticDefaultValueInput.hide();
			dynamicDefaultValueInput.show();
			dynamicDefaultValueButton.show();
			setDynamicDefaultValueInputValue(dynamicDefaultValueInput, element);
		} else {
			staticRadio.attr("checked", "checked");
			staticDefaultValueInput.show();
			dynamicDefaultValueInput.hide();
			dynamicDefaultValueButton.hide();
		}
		
		dynamicDefaultValueButton.click(function(){
			if(element.proto.defaultValue) {
				$('<div></div>').dialog({
					buttons : [ 
					    {
						    text : I18N('web.generic.yes'),
						    click : function() {
						    	setDynamicDefaultValue(element, dynamicDefaultValueInput, staticDefaultValueInput);
						    	$(this).dialog('close');
							}
						},
						{
							text : I18N('web.generic.no'),
							click : function(){
								$(this).dialog('close');
							}
						}
					],
					autoOpen : false,
					modal : true,
					close : function() {
						$(this).dialog('destroy');
						$(this).remove();
					},
					width : "320",
					height : "200", 
					title : I18N("web.generic.warning")
				}).html(I18N("web.editor.properties.staticDefaultValueWillBeLost")).dialog('open');
			} else {
				setDynamicDefaultValue(element, dynamicDefaultValueInput, staticDefaultValueInput);
			}
		});
		
		staticDefaultValueInput.change(function(){
			if(element.defaultValueLookupTableId){
				$('<div></div>').dialog({
					buttons : [ 
					    {
						    text : I18N('web.generic.yes'),
						    click : function() {
						    	element.changeProperty('defaultValueLookupTableId', null);
								element.changeProperty('defaultValueColumn', null);
								element.changeProperty('defaultValueFilters', null);
								dynamicDefaultValueInput.val("");
						    	$(this).dialog('close');
							}
						},
						{
							text : I18N('web.generic.no'),
							click : function(){
								staticDefaultValueInput.val("");
								$(this).dialog('close');
							}
						}
					],
					autoOpen : false,
					modal : true,
					close : function() {
						$(this).dialog('destroy');
						$(this).remove();
					},
					title : I18N("web.generic.warning")
					
				}).html(I18N("web.editor.properties.dynamicDefaultValueWillBeLost")).dialog('open');
			}
		});
	};
	
	var deepCopyFilters = function(filters){
		filters = filters.slice(); 
		for(var i = 0; i < filters.length; i++){
			filters[i] = $.extend({}, filters[i]);
			if(filters[i].column){
				filters[i].column = acme.UTIL.decodeHTML(filters[i].column); //#2200
			}
		}
		return filters;
	};
	
	/**
	 * Shows a dialog where the user can set a default value that 
	 * comes from a lookup table
	 */
	var setDynamicDefaultValue = function(element, dynamicDefaultValueInput, staticDefaultValueInput) {
		var filters = element.defaultValueFilters;
		if(typeof(filters) == "string"){
			filters = JSON.parse(filters);
		}
		
		if(filters){
			filters = deepCopyFilters(filters);
		}
		
		var lookupTableId = element.defaultValueLookupTableId;
		var valueColumn = element.defaultValueColumn ? acme.UTIL.decodeHTML(element.defaultValueColumn) : null;
		var options = {
			lookupTableId : lookupTableId,
			valueColumn : valueColumn,
			filters : filters,
			instanceId : element.instanceId,
			label : element.proto.label
		};
		
		DEFAULTVALUE_OPTIONS_DIALOG.open(options, function(dialogResult){
			if(dialogResult){
				var defaultValueLookupTableId = dialogResult.lookupTableId;
				var defaultValueColumn = dialogResult.valueColumn;
				var filters = dialogResult.filters;
				for(var i = 0; i < filters.length; i++){
					var f = filters[i];
					var e = MODEL.getElementByInstanceId(f.rightValue);
					if(e){
						f.elementPosition = e.position.toString();
						f.pagePosition = e.page.position.toString();
					}
					f.filterType = "DEFAULT_VALUE";
				}
				// the list is send as a JSON
				var filtersStr = JSON.stringify(filters);
				element.changeProperty('defaultValueLookupTableId', defaultValueLookupTableId);
				element.changeProperty('defaultValueColumn', defaultValueColumn);
				element.changeProperty('defaultValueFilters', filtersStr);
				element.changeProperty('proto_defaultValue', null);
				staticDefaultValueInput.val("");
				setDynamicDefaultValueInputValue(dynamicDefaultValueInput, element);
			}
		});
	};
	
	/**
	 * Sets the value to show in the Input after filling in the dialog
	 */
	var setDynamicDefaultValueInputValue = function(input, element){
		
		var lookupsLoaded=COMMON.requestLookupList();
		$.when(lookupsLoaded).then(function(list){
			
				var label = "[...]";
				for(var i = 0; i < list.length; i++){
					var l = list[i];
					if(l.pk == element.defaultValueLookupTableId){
						label = l.name;
						break;
					}
				}
				input.val(label + "." + element.defaultValueColumn);
			
		});
	};

	//#527
	var addFiltersRow = function(element) {
		var proto = element.proto;
		// Show Filter Options row for Dropdowns
		$('#tr_proto_defaultValue').after('<tr id="tr_filterOptions" class=""><td class="span4 propertyLabel">'
			+ I18N('web.editor.properties.select.filter.label') +
			'</td><td class="span8"><input id="filterOptions" class="span12 btn" type="button" value="'
			+ I18N('web.editor.properties.select.filter.button')
			+'" /></td>');
		
		if(proto.source =='EMBEDDED'){
			$('#tr_filterOptions').hide();
		}
			
		$('#filterOptions').click(function(){
			var filters = element.itemListFilters;
			if(typeof(filters) === "string"){
				filters = JSON.parse(filters);
			}
 
			if(filters){
				filters = deepCopyFilters(filters); 
			}
			
			var options = {
				label : proto.label,
				instanceId : element.instanceId,
				lookupTable : $('#' + IDS.lookupSelect).val(),
				filters : filters
			};
			
			if(options.lookupTable && options.lookupTable != "-1"){
				DROPDOWN_FILTER_DIALOG.open(options, function(dialogResult){
					if(dialogResult){
						var filters = [];
						for(var i = 0; i < dialogResult.filters.length; i++){
							var f = dialogResult.filters[i];
							var e = MODEL.getElementByInstanceId(f.rightValue);
							if(e){
								f.elementPosition = e.position.toString();
								f.pagePosition = e.page.position.toString();
							}
							f.filterType = "ITEM_LIST";
							filters.push(f);
							
						}
						// the list is send as a JSON
						var filtersStr = JSON.stringify(filters);
						element.changeProperty('itemListFilters', filtersStr);
					} 
				});
			} else {
				$('<div></div>').html(I18N('web.editor.properties.dialogs.dropdown.filters.selectLookupFirst')).
					dialog({
						buttons : [{
							text : I18N('web.generic.ok'),
							click : function () {
								$(this).dialog('close');
							}
						}],
						autoOpen : false,
						close : function(){
							$(this).dialog('destroy');
							$(this).remove();
						},
						modal : true,
						title : I18N('web.generic.sorry')
					}).dialog('open');
			}
		});
	};
	
	var propertyChangedFactory = function(item, property, input, changeCallback){
		return function(e){
			var val = null;
			if(input.is(':checkbox')){
				//FIXME do I need to convert it to String?
				val = input.is(':checked');
			} else{
				val = input.val();
			}
			
			// Quick & Dirty BUG FIX for #3670
			if(item.page && !item.page.getElementByInstanceId(item.instanceId)){
				// The element doesn't exist anymore. It was removed.
				return;
			}
			
			var promise = null;
			if($.isFunction(changeCallback)){
				promise = changeCallback(property, val);
			}
			
			if(promise){
				$.when(promise).then(function(result){
					if(result.success){
						item.changeProperty(property, val);
					} else {
						//revert change
						if(input.is(':checkbox')){
							input.attr('checked', item[property]);
						} else {
							if(item[property]){
								input.val(item[property]);
							}else{
								input.val("");
							}
						}
					}
				});
			} else {
				item.changeProperty(property, val);
			}
	   };
	};
	
	var orderByImportance = function(properties){
		var props = [];
		for(prop in properties){
			if(properties.hasOwnProperty(prop)){
				var a = {};
				a.name = prop;
				a.importance = properties[prop].importance;
				props.push(a);
			}
		}
		
		props.sort(function(a, b){
			return b.importance - a.importance;
		});
		
		return props;
	};
	
	// The item is the element, page, or form that owns the properties'
	// The changeCallback is a function that is called before the value of the property changes 
	var appendPropertiesToTable = function(item, properties, changeCallback){
		var props = orderByImportance(properties);
		var propertyClass="span12 property";
		for (var i = 0; i < props.length; i++){
	       var prop = props[i].name;
	       
	       var value = properties[prop].value; 
	       value = (value === null || typeof value == "undefined") ? '' : (typeof(value) == "string" ? acme.UTIL.decodeHTML(value) : value); // #2197 //#2212
	       var visible = properties[prop].visible;
           var editable = properties[prop].editable;
           var label = properties[prop].label;
           var type = properties[prop].type;
           
           var trId = 'id="tr_' + prop +'"';
           var htmlString = visible ? '<tr ' + trId  + ' class="">' : '<tr ' + trId  + ' class="" style="display:none">';
           htmlString += '<td class="span4 propertyLabel">' + label + '</td>';
           
           if(editable){
        	   // choose the "widget to show"
        	   propertyClass="span12 property";
        	   if(type === 'STRING' || type === 'INTEGER' || type === "DOUBLE"){ 
        		   
        			   if(type==='DOUBLE' ){
        				   propertyClass+=" decimalProperty ";
        			   }else if(type==='INTEGER' ){
        				   propertyClass+=" integerProperty ";
        			   }
        		   
        		   htmlString += '<td class="span8" style="position:relative"><input type="text" class="'+propertyClass+'" id="property_' + prop  +'" value="' + value +'" /></td>' ;
        	   } else if(type === 'ENUM') {
        		   htmlString += '<td class="span8" style="position:relative"><select class="span12 property" id="property_' + prop + '">';
        		   var options = properties[prop].options;
        		   for(option in options){
        			   if(options.hasOwnProperty(option)){
	        			   var o = options[option];
	        			   var ov = acme.UTIL.decodeHTML(o.value);
	        			   var selected = ov == value ? 'selected="selected"' : '';  
	        			   htmlString += '<option value="' + ov + '" ' + selected + '>' + o.label + '</option>'; //TODO analyze the possibility of injection
        			   }
        		   }
        		   htmlString += '</select></td>';
        	   } else if(type === 'BOOLEAN'){
        		   var checked = 'checked="checked"';
        		   if(!value || value === 'false'){
        			   checked = '';
        		   }
        		   htmlString += '<td class="span8" style="position:relative"><input id="property_' + prop + '" type="checkbox" ' + checked + '/></td>'; 
        	   } else if(type === 'DATE'){
        		   htmlString += '<td class="span8" style="position:relative"><input class="span12 property propertyDatepicker" type="text" id="property_' + prop  +'" value="' + value +'" /></td>' ;
        	   } else if(type === 'TIME'){
        		   htmlString += '<td class="span8" style="position:relative"><input class="span12 property propertyTimepicker" type="text" id="property_' + prop  +'" value="' + value +'" /></td>' ;
        	   } else if(type === 'STRING_LONG'){
        		   htmlString += '<td class="span8" style="position:relative"><textarea id="property_' + prop + '" rows="4" cols="20">' + value +'</textarea></td>';
        	   }
           } else { 
        	   if(type === 'STRING' || type === 'INTEGER' || type === 'DOUBLE' 
        		   || type === 'BOOLEAN' || type === 'DATE' || type === 'TIME'){
        		   htmlString += '<td class="span8 word-break">' + value + '</td>';
        	   } else if (type === 'ENUM') {
        		   var options = properties[prop].options;
        		   var selectLabel = '';
        		   for(option in options){
        			   if(options.hasOwnProperty(option)){
	        			   var o = options[option];
	        			   var ov = acme.UTIL.decodeHTML(o.value);
	        			   if(ov === value){
	        				  selectLabel = o.label;
	        				  break;
	        			   } 
        			   }
        		   }
        		   htmlString += '<td class="span8 word-break">' + selectLabel + '</td>';
        	   }
           }
           
           htmlString += '</tr>';
           propertiesTable.append(htmlString);
           $(propertiesTable).find(".integerProperty").numeric({decimal:false});
           $(propertiesTable).find(".decimalProperty").numeric({ decimal : "." });
           if(editable){
        	   $('#property_' + prop).change(
        			   propertyChangedFactory(item, 
        					   prop, $('#property_' + prop), changeCallback));
        	   $('.propertyDatepicker').datepicker();
        	   $('.propertyTimepicker').timepicker({
					ampm: true
				});
           }
		}
	};
	
	// Public Interface
	// --------------------------------------------------------//
	/**
	 * It shows the item on the properties view
	 */
	that.show = function(ev){
		// Don't delete this! SEE #1651
		// Hack to make any input/select/textarea in the properties table lost the focus
		$('#propertiesLabelLink').focus();
		propertiesTable.html('<tbody class="span12"></tbody>');
		if(ev.type === 'ELEMENT'){
			showElementInstanceProperties(ev.item);
		}else if(ev.type === 'PAGE'){
			showPageProperties(ev.item);
		}else if(ev.type === 'FORM'){
			showFormProperties(ev.item);
		}
		// set focus on the first editable property
		propertiesTable.find('.property:eq(0)').focus();
	};
	
	/**
	 * Should be called one time when the editor is started to prepare all
	 * that's necessary to show element properties
	 * @param div
	 */
	that.setup = function(div){
		//div.html('');
		//div.append('<h4><a id="propertiesLabelLink" href="javascript:void(0)" >' + I18N("web.editor.properties") + '<a></h4>');
		COMMON.clearCache();
		div.append('<table id="' + IDS.propertiesTable + '" class="span12 table table-condensed" ><tr><td>' + I18N("web.editor.properties.selectItem") + '</td></tr></table>');
		propertiesTable = $('#' + IDS.propertiesTable);
		
	};
	
	return that;
});
