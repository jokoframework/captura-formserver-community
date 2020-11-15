define([ "jquery", "acme", "jquery-ui", "acme-ui", "timepicker/timepicker", "home/process-item/process-item-model-amd","constants", "home/data/lookuptable-amd"], 
		function($, acme, $ui, acme_ui, timepicker, process_item, constants,lookupMod) {

	var pkg = {};

	var processItemPkgLoadDfd = $.Deferred();	
	pkg.loaded = processItemPkgLoadDfd.promise();
	
	var I18N =  acme.I18N_MANAGER;
	var i18nPromise = acme.I18N_MANAGER.load(['web.home.admin.processitem.new',
	                                          'web.home.admin.processitem.edit',
	                                          'web.generic.ok', 'web.generic.cancel',
	                                          'admin.processitem.dialog.saveas.title',
	                                          'web.generic.project','web.generic.form',
	                                          'web.generic.action','admin.processitem.processItemVersion',
	                                          'web.generic.upgrade','admin.form.processitem.forms.title',
	                                          'admin.cruds.processitem.delete.confirmation.message',
	                                          'admin.cruds.processitem.delete.confirmation.title',
	                                          'web.generic.ok',
	                                          'web.generic.cancel'
	                                          ]);
	var I18Nmsg = acme.I18N_MANAGER.getMessage;
	
	var saveAsDialog = null;
	
	var dfd=null;
	
	require(["order!jqgrid/grid.locale-" + I18N.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
	
		pkg.ProcessItemManager = function(params) {				
			
			// Element ID used within the module
			var idPrefix = "processItem";
			var eidPrefix = "#" + idPrefix;
			var eid = {
				title : eidPrefix + "_title",
				type : eidPrefix + "_type",
				label : eidPrefix + "_label",
				poolGroup : eidPrefix + "_poolGroup",
				pool : eidPrefix + "_pool",
				poolLinkGroup : eidPrefix + "_poolLinkGroup",
				poolLink : eidPrefix + "_poolLink",
				tabs : eidPrefix + "_tabs",
				properties : eidPrefix + "_propertiesContainer",
				saveButton : eidPrefix + "_save",
				saveAsButton : eidPrefix + "_saveas",
				deleteButton : eidPrefix + "_delete",
				clearButton : eidPrefix + "_clear",
				required : eidPrefix + "_required",
				container : eidPrefix + "_container",
				dropdownSourceGroup : eidPrefix + "_property_source_group",
				dropdownSource : eidPrefix + "_property_source",
				lookupIdentifier : eidPrefix + "_property_lookupIdentifier",
				lookupLabel : eidPrefix + "_property_lookupLabel",
				lookupValue : eidPrefix + "_property_lookupValue",
				embeddedGroup : eidPrefix + "_property_embedded_group",
				embedded : eidPrefix + "_property_embedded",
				versionGroup : eidPrefix + "_version_group",
				version : eidPrefix + "_version",
				formsContainer : eidPrefix + "_formsContainer",
				formsGrid : eidPrefix + "_formsGrid",
				formsPager : eidPrefix + "_formsPager",
				formsMessage : eidPrefix + "_formsMessage",
				saveAsDialog : eidPrefix + "_saveAsDialog",
				messageDialog : eidPrefix + "_messageDialog",
				poolGroupDialog : eidPrefix + "_poolGroupDialog",
				poolDialog : eidPrefix + "_poolDialog",
				newLabelGroupDialog : eidPrefix + "_newLabelGroupDialog",
				newLabelDialog : eidPrefix + "_newLabelDialog",
				upgradeAll : eidPrefix + "_upgradeAll",
				upgradeAllContainer : eidPrefix + "_upgradeAllContainer",
			};
			
			// URI used within the module
			var uri = {
				edit : "/processItems/edit.ajax",
				save : "/processItems/save.ajax",
				deleteProcessItem : "/processItems/delete.ajax",
				typeMetadata : "/processItems/typeMetadata.ajax",
				poolList : "/pools/list.ajax",
				pool : "/cruds/pools/get.ajax",
				processItem : "/processItems/get.ajax",
				processItemForms : "/processItem/paging/forms.ajax",
				upgrade : "/processItems/upgradeInForm.ajax",
				upgradeAll : "/processItems/upgradeInAllForms.ajax",
			};
	
			var defaultType = "text";
			
			// Information about the process item ids
			var processItemRootId = null;
			var processItemVersion = null;
			var processItemId = null;
			
			var poolList=null;
			
			// An object with information about the process item
			var processItemToEdit = null;
			var editMode = false;
			
			// Metadata about process item types. Used to know what to show according to
			// the selected type
			var typeManager = null;
			var poolManager = null;
	
			// --- UI state
			// The tab element of the process item. Shows properties and forms
			var processItemTabs = null;
			var formGrid = null;
			var showUpgradeAllButton = false;
	
			// Lifecycle
			var callbacks = {
				afterFailureSave : null,
				afterSucessSave : null,
				afterCancel : null
			};
			
			var resizeTables=function(){
				var desireWidth=$(window).width()*710/1024;
				$(eid.formsGrid).jqGrid('setGridWidth', desireWidth, true);
						
			};
			
			// This is used by jqGrid to be able to use PageData class
			var jsonReader =  { 
				    root: "data", 
				    repeatitems: false,
				    page: "pageNumber",
				    total: "totalPages",
				    records: "totalCount",
				    id: "0"
			};
			
			// ///////////////////////////////////////////////////////////////////////////////
			// -- Utils -----
	
			// Default GET Ajax promise function
			var newJSONGETPromise = function(uri, options) {
				var ajaxOptions = acme.AJAX_FACTORY.newInstance();
				ajaxOptions.url += uri;
				ajaxOptions.contentType = 'application/json; charset=utf-8';
				ajaxOptions.type = "GET";
				ajaxOptions = $.extend({}, ajaxOptions, options);
				return $.ajax(ajaxOptions);
			};
	
			// Default GET Ajax promise function
			var newJSONPOSTPromise = function(uri, options,loadingId) {
				var ajaxOptions = acme.AJAX_FACTORY.newInstance();
				ajaxOptions.url += uri;
				ajaxOptions.contentType = 'application/json; charset=utf-8';
				ajaxOptions.type = "POST";
				ajaxOptions.loadingSectionId = loadingId;
				ajaxOptions = $.extend({}, ajaxOptions, options);
				return $.ajax(ajaxOptions);
			};
	
			// Populate a Select control
			// @param options an array of maps. E.g. [{"value1" : "label1"}, {"value2" :
			// "label2"},]
			var pupulateSelect = function(selectEID, optionList, params) {
				var select = $(selectEID);
				if (select.prop) {
					var options = select.prop('options');
				} else {
					var options = select.attr('options');
				}
				$('option', select).remove();
	
				// Add none option at the start
				if (params && params.withNoneOption) {
					var noneValue = "none";
					if (!(typeof params.noneValue === 'undefined')) {
						noneValue = params.noneValue;
					}
					options[options.length] = new Option("", noneValue);
					select.val(noneValue);
				}
	
				// Sort alphabetically
				if (params && params.sortedAlphabetically) {
					optionList.sort(function(a, b) {
						for ( var key in a) {
							var value1 = a[key];
							break;
						}
						for ( var key in b) {
							var value2 = b[key];
							break;
						}
						return value1.localeCompare(value2);
					});
				}
	
				$.each(optionList, function(index, element) {
					for ( var key in element) {
						options[options.length] = new Option(element[key], key);
					}
				});
	
				// Set selected option
				if (params && params.selectedOption) {
					select.val(params.selectedOption);
				}
			};
			
			var populatePoolSelect = function(div) {
				$(div).html('');
				var poolPromise;
				if(poolList == null) { 
					poolPromise = acme_ui.UTIL.downloadList({url:uri.poolList});
				} else {
					poolPromise = {success:true, obj:poolList};
				}
				$.when(poolPromise).then(function(response){
					poolList=response.obj;
					acme_ui.HTML_BUILDER.drawSelectOptions($(div), response.obj,null,null,"name","id");
				});
				return poolPromise;
			};
	
			var getValueBaseOnElement = function(elementID) {
				var inputElement = $(elementID);
				if (inputElement.is(':checkbox') || inputElement.is(':radio')) {
					return inputElement.is(':checked');
				} else {
					var value = inputElement.val();
					value = $.trim(value);
					if (!(value)) {
						return null;
					}
					return value;
				}
			};
			
			var setValueBaseOnElement = function(elementID, leValue){
				var element = $(elementID);
				if (element.is(':checkbox') || element.is(':radio')) {
					if (leValue != false) {
						element.attr("checked", "checked");
					} 
				} else {
					element.val(leValue);							
				}
			};
			
			var clearValueBaseOnElement = function(elementID) {
				var element = $(elementID);
				if (element.is(':checkbox') || element.is(':radio')) {
					element.removeAttr('checked');
				} else {
					element.val("");							
				}
			};
	
			// -- END Utils -----
			// ///////////////////////////////////////////////////////////////////////////////
	
			// ///////////////////////////////////////////////////////////////////////////////
			// -- Ajax -----
	
			// AJAX promise to get the metadata of process item types
			var loadProcessItemTypesPromise = function() {
				var typePromise = newJSONGETPromise(uri.typeMetadata);
				typePromise.done(function(response) {
					// initialize a ProcessItemManager
					typeManager = process_item.TypeManager(response.obj.elements);
					// Populate select UI control
					var options = typeManager.getOptions();
					pupulateSelect(eid.type, options, {
						selectedOption : defaultType,
						sortedAlphabetically : true
					});
				}).fail(function(error) {
					alert("There's an error while loading type metadata: " + error);
				});
				return typePromise;
			};
	
			var loadPool = function(poolId) {
				var poolPromise = acme_ui.UTIL.downloadList({url:uri.pool,data:{poolId:poolId}});
				$.when(poolPromise).then(function(response){	
					$(eid.poolLink).html(response.obj.name);
				});
				return poolPromise;
			};
	
			// AJAX promise that gets all pool of the current application
			var loadProcessItemPromise = function() {
				var options = {
					data : "rootId=" + processItemRootId 
				};
				var processItemPromise = newJSONGETPromise(uri.processItem, options);
				processItemPromise.done(function(response) {
					if (response.success == true) {
						processItemToEdit = response.obj;
						processItemRootId = response.obj.rootId;
						processItemId = response.obj.id;
						processItemVersion = response.obj.version;
					} else {
						acme_ui.HTML_BUILDER.notifyError(response.message, "");
					}
				}).fail(function(error) {
					// FIXME
					alert("There's an error while loading process item: " + error);				
				});
				return processItemPromise;
			};
			
			var editActionPromise = function(jsonData) {
				var options = {
					data : JSON.stringify(jsonData)
				};
				var editPromise = newJSONPOSTPromise(uri.edit, options,'page_width');
				editPromise.done(function(response) {
					if (response.success === true) {
						acme_ui.HTML_BUILDER.notifySuccess(response.message, "");					
						processItemRootId = response.obj.rootId;
						processItemVersion = response.obj.version;
						processItemId = response.obj.id;
						processItemToEdit = response.obj;
						
						updateVersionInfo();
						if(formGrid) {
							$(eid.formsGrid).trigger("reloadGrid");
						} 
						
						if (callbacks.afterSucessSave) {
							callbacks.afterSucessSave();
						}
					} else {
						var bulletPoints="";
						if(response.content) {
							bulletPoints = "<ul>";
							$.each(response.content, function(key, value) {
								bulletPoints += "<li>" + key + ": " + value + "</li>";
							});
							bulletPoints += "</ul>";
						}
						acme_ui.HTML_BUILDER.notifyError(response.message, bulletPoints);
						$(id.message).show();
						if (callbacks.afterFailureSave) {
							callbacks.afterFailureSave();
						}
					}
				}).fail(function(error) {
					// FIXME
					alert("There's an error while saving the process item: " + error);
				});
				return editPromise;
			};
			
			var saveActionPromise = function(jsonData,divError) {
				var options = {
					data : JSON.stringify(jsonData)
				};
				var savePromise = newJSONPOSTPromise(uri.save, options,'page_width');
				savePromise.done(function(response) {
					if (response.success === true) {
						acme_ui.HTML_BUILDER.notifySuccess(response.message, "");	
						processItemRootId = response.obj.rootId;
						processItemVersion = response.obj.version;
						processItemId = response.obj.id;
						processItemToEdit = response.obj;
						
						changeNatureToEdit();
						updateVersionInfo();
						
						if (callbacks.afterSucessSave) {
							callbacks.afterSucessSave();
						}
					} else {
						// res.message, I18N('admin.cruds.user.saved.message',
						// [postdata.mail])
						var bulletPoints = "<ul>";
						$.each(response.content, function(key, value) {
							bulletPoints += "<li>" + key + ": " + value + "</li>";
						});
						bulletPoints += "</ul>";
						acme_ui.HTML_BUILDER.notifyError(response.message, bulletPoints);
						if (callbacks.afterFailureSave) {
							callbacks.afterFailureSave();
						}
					}
				}).fail(function(error) {
					// FIXME
					alert("There's an error while saving the process item: " + error);
				});
				return savePromise;
			};
	
			// -- END Ajax -----
			// ///////////////////////////////////////////////////////////////////////////////
	
			var loadLookupIdentifier = function() {
				var lookupSelect = $(eid.lookupIdentifier);			
				var promiseLookupsLoaded=lookupMod.listLookupTables();
				$.when(promiseLookupsLoaded).then(function(lookupList){
					lookupSelect.empty();
					acme_ui.HTML_BUILDER.drawSelectOptions(lookupSelect, lookupList,null,null,"name","pk");					
				});
				return promiseLookupsLoaded;
				/*
				var promiseLUTList = acme_ui.UTIL.downloadList({url:'/lookuptable/list-metadata.ajax'});
				var dfd = $.Deferred();
				
				$.when(promiseLUTList).then(function(responseList) {
					lutList.empty();
					acme_ui.HTML_BUILDER.drawSelectOptions(lutList, responseList.obj);				
					$.when(loadLookupColumns()).then(function(){
						dfd.resolve();
					});
				});
				return dfd.promise();*/
			};
			
			var loadLookupColumns = function() {
				var lutList = $(eid.lookupIdentifier);
				var labelList = $(eid.lookupLabel);
				var valueList = $(eid.lookupValue);
				var lookupId = lutList.val();
				var lookupLoaded;
				var dfd = $.Deferred();
				if(lookupId){
					var lookupLoaded = lookupMod.loadLookupDefinition(lookupId);
					$.when(lookupLoaded).then(function(lookupDef){
						var list=lookupDef.fields;
						labelList.empty();
						valueList.empty();
						acme_ui.HTML_BUILDER.drawSelectOptions(labelList,list,null,null,"columnName","columnName");
						acme_ui.HTML_BUILDER.drawSelectOptions(valueList,list,null,null,"columnName","columnName");
						dfd.resolve();
					});					
					
				} else {
					dfd.resolve();
				}
				return dfd.promise();
			};
			
			var changeNatureToEdit = function() {	
				editMode = true;
				
				loadPool(processItemToEdit.pool);
				$(eid.label).val(processItemToEdit.label);
				$(eid.poolGroup).hide();
				$(eid.poolLinkGroup).show();
				$(eid.poolLink).click(function() {
					acme.LAUNCHER.launch(constants.LAUNCHER_IDS.pool_edit, processItemToEdit.pool);
				});
				
				$(eid.type).attr('disabled', 'disabled');
				$(eid.embedded).attr('disabled', 'disabled');
				$(eid.lookupIdentifier).attr('disabled', 'disabled');
				$(eid.lookupLabel).attr('disabled', 'disabled');
				$(eid.lookupValue).attr('disabled', 'disabled');
				$(eid.dropdownSource).attr('disabled', 'disabled');
				
				$(eid.title).text(I18Nmsg('web.home.admin.processitem.edit'));
				initSaveAsDialog();
				$(eid.saveAsButton).show();
				$(eid.deleteButton).show();
				initSaveAction();
				
				if(formGrid) {
					$(eid.formsGrid).trigger("reloadGrid");
				} 
			};
			
			var updateVersionInfo = function() {
				if (editMode) {
					$(eid.version).show();
					$(eid.versionGroup).show();				
					$(eid.version).val(processItemVersion);
				} else {
					$(eid.versionGroup).hide();
				}
			};
			
			var hideAllProperties = function() {
				$(eid.properties).find("fieldset").each(function(index, element) {
					$(element).find("div.control-group").hide();
				});
			};
			
			var hideAllPropertiesExcept = function(eidList) {
				$(eid.properties).find("fieldset").each(function(index, element) {
					if($.inArray("#" + element, eidList) == -1) {
						$(element).find("div.control-group").hide();
					}
				});
				// FIXME Workaround. This shows the elements in the list but causes a blink
				$.each(eidList, function(index, element){
					$(element).show();
				});
			};
	
			var handleDropdownSourceChange = function(selectedValue) {
				hideAllPropertiesExcept([eid.dropdownSourceGroup]);
				var sourceAlternatives = typeManager.getTypeElementsByKey("select");				
				var elements = sourceAlternatives[selectedValue];
				
				if (selectedValue == "dynamic") { //dynamic is source : embedded // don't know why, but that's the way it is - jmpr 15/05/2013
					// dynamic
					$(eid.embeddedGroup).show();
					addOptionToEmbbededSelect(null, {
						includeAdd:true,
						cleanOptions:true
					});	
				} else { // manual is source : lookup // don't know why, but that's the way it is - jmpr 15/05/2013
					// manual	
					var promise = null;
					if(!processItemToEdit){ //#2201 ... quite messy, I'm sorry - jmpr
						promise = loadLookupIdentifier();
					}
					$.each(elements, function(index, element) {
						var elementEid = eidPrefix + "_property_" + element + "_group";	
						//var inputEid =  eidPrefix + "_property_" + element;
						$(elementEid).show();
					});
					return promise;
				}
			};
			
			var handleTypeChange = function(selectedValue) {
				hideAllProperties();
				//FIXME this is a workaround for #1879
				if(selectedValue==='headline'){
					$('#processItem_required_group').hide();
				} else {
					$('#processItem_required_group').show();
				}
				
				var elements = typeManager.getTypeElementsByKey(selectedValue);
	
				$(eid.label).val('');
				$(eid.required).removeAttr('checked');;
				if (selectedValue == "select") {
					// Show dropdown select
					$(eid.dropdownSourceGroup).show();
					
					if(processItemToEdit) {
						var selectedSource = processItemToEdit.source; // manual : lookup, dynamic : embedded

						if(processItemToEdit.lookupLabel){ //#2200
							processItemToEdit.lookupLabel = acme.UTIL.decodeHTML(processItemToEdit.lookupLabel);
						}
						
						if(processItemToEdit.lookupValue){ //#2200
							processItemToEdit.lookupValue = acme.UTIL.decodeHTML(processItemToEdit.lookupValue);
						}
					}else {
						selectedSource = "manual";
						$(eid.dropdownSource).val(selectedSource);
					}
					//this is quite messy ... #2201
					return handleDropdownSourceChange(selectedSource);				
					
				} else {
					// basic type
					$.each(elements, function(index, element) {
						var elementEid = eidPrefix + "_property_" + element + "_group";	
						var inputEid =  eidPrefix + "_property_" + element;
						$(inputEid).val('');
						$(elementEid).show();
						if (element == "defaultValue") {						
							if (selectedValue == "date") {
								$(inputEid).datepicker();								
							} else if(selectedValue == "datetime") {
								$(inputEid).datetimepicker({
									ampm: true
								});
							} else if (selectedValue == "time") {
								$(inputEid).timepicker({
									ampm: true
								});
							} else {
								$(inputEid).datetimepicker("destroy");
								$(inputEid).timepicker("destroy");
								$(inputEid).datepicker("destroy");
							} 											
						}
					});				
				}
			};
	
			var loadProcessItemProperties = function(type) {
				var json = {};			
				if(type == "select") {
					// Dropdow type
					var sourceType = $(eid.dropdownSource).val();
					json.source = getValueBaseOnElement(eid.dropdownSource);
					json.multiple = false; // Currently, multiple is not implemented
					if(sourceType === "dynamic"){
						json["options"] = [];
						$(eid.embedded).find("tr").each(function(indexTr, elementTr){
							var obj = {};
							$(elementTr).find("input").each(function(){
								var current = $(this);
								obj[current.attr('type')] = getValueBaseOnElement(current);
							});
							json["options"].push(obj);
						});
					} else {
						// manual
						json.lookupIdentifier = getValueBaseOnElement(eid.lookupIdentifier);;
						json.lookupLabel = getValueBaseOnElement(eid.lookupLabel);
						json.lookupValue = getValueBaseOnElement(eid.lookupValue);						
					}
				} else {
					// basic type
					var elements = typeManager.getTypeElementsByKey(type);
					$.each(elements, function(index, element) {
						var elementEid = eidPrefix + "_property_" + element;
						var value = getValueBaseOnElement(elementEid);
						json[element] = value;
					});
				}
				return json;
			};
	
			// returns a JSON object with data from the form
			var loadData = function() {
				var json = {};
				if(editMode) {
					json.pool = processItemToEdit.pool;
				} else {
					json.pool = getValueBaseOnElement(eid.pool);
				}
				json.version = processItemVersion;
				json.rootId = processItemRootId;
				json.id = processItemId;
				json.type = getValueBaseOnElement(eid.type);
				json.label = getValueBaseOnElement(eid.label);
				json.required = getValueBaseOnElement(eid.required);
				var propertiesJson = loadProcessItemProperties(json.type);
				return $.extend(json, propertiesJson);
			};
			
			var populateSelectFields = function(processItemToEdit) {
				var selectType = processItemToEdit.source;
				$(eid.dropdownSource).attr("disabled", "disabled");
				$(eid.dropdownSource).val(selectType);
				if("manual" == selectType) {				
					var lutList = $(eid.lookupIdentifier);	
					var promiseLUTList = acme_ui.UTIL.downloadList({url:'/lookuptable/list-metadata.ajax'});
					$.when(promiseLUTList).then(function(responseList) {
						lutList.empty();
						acme_ui.HTML_BUILDER.drawSelectOptions(lutList,responseList.obj);
						var identifierSelected = processItemToEdit.lookupIdentifier;
						var labelSelected = processItemToEdit.lookupLabel;
						var valueSelected = processItemToEdit.lookupValue;
							
						lutList.val(identifierSelected);
						
						var labelList = $(eid.lookupLabel);
						var valueList = $(eid.lookupValue);					
						var promiseLUTList = acme_ui.UTIL.downloadList({
								url:'/lookuptable/columns.ajax',
								data:{ 'lutId':identifierSelected }
						});
						$.when(promiseLUTList).then(function(responseList) {
							labelList.empty();
							valueList.empty();
							acme_ui.HTML_BUILDER.drawSelectOptions(labelList,responseList.obj);
							acme_ui.HTML_BUILDER.drawSelectOptions(valueList,responseList.obj);
							labelList.val(labelSelected);
							valueList.val(valueSelected);
							// Disable dropdown manual properties
							lutList.attr("disabled", "disabled");
							labelList.attr("disabled", "disabled");
							valueList.attr("disabled", "disabled");
						});					
					});
					
				} else{
					// Embbeded
					var optionList = processItemToEdit.options;
					generateSelectOptions(optionList, processItemToEdit.defaultValue);				
				}
			};
			
			var generateSelectOptions = function(optionList, defaultValue){
				$.each(optionList, function(index, optionMap){
					if (index == 0) {
						addOptionToEmbbededSelect(optionMap["text"], {
							includeAdd:true,
							cleanOptions:true,
							selected:(optionMap["radio"] === 'true')
						});
					} else {
						addOptionToEmbbededSelect(optionMap["text"], {
							includeDel:true,
							selected:(optionMap["radio"] === 'true')
						});
					}				
				});			
			};
			
			// Used to fill the form fiels using the already loaded
			// processItemToEdit variable
			var populateFormFields = function(){
				var type = processItemToEdit.type;
				setValueBaseOnElement(eid.type, type);
				
				// Show only the properties of the selected type
				var promise = handleTypeChange(type);			
				setValueBaseOnElement(eid.label, processItemToEdit.label);
				setValueBaseOnElement(eid.required, processItemToEdit.required);
				setValueBaseOnElement(eid.pool, processItemToEdit.pool);
				// In the updateVersionInfo method this is used
				processItemVersion = processItemToEdit.version;			
				var elements = typeManager.getTypeElementsByKey(type);
				// basic type
				$.each(elements, function(index, element) {
					var elementEid = eidPrefix + "_property_" + element;
					setValueBaseOnElement(elementEid, processItemToEdit[element]);				
				});
				if (type == "select") { //this is quite messy ... #2201
					$.when(promise).then(function(){
						populateSelectFields(processItemToEdit);
					});

				} 
			};
	
			var validSaveForm = function() {
				// TODO Check the form elements that are going to be used for thesave action
				// Mark the error in the fields if exist
				// Return true if success 
				return true;
			};
			
			var addOptionToEmbbededSelect = function(value, settings){
				if(typeof value == 'undefined'){
					return;
				} 
				var includeAdd = settings["includeAdd"] || false;
				var includeDel = settings["includeDel"] || false;
				var cleanOptions = settings["cleanOptions"] || false;
				var disable = settings["disabled"] || false;
				var isSelected = settings["selected"] || false;
				
				var dynamicOptions = $(eid.embedded);
				
				var radio = $('<input>').attr('type', 'radio').attr('name', 'default_select_option');
				if(isSelected){
					radio.attr('checked', true);
				}
				
				var text = $('<input>').attr('type', 'text');
				if(value){
					text.val(value);
				}
				if(disable){
					text.attr("disabled", "disabled");
				}
				
				var tr = $('<tr>');			
				tr.append($('<td>').append(radio));
				tr.append($('<td>').append(text));
				
				if(includeAdd){
					var img = $('<img>').attr('src', acme.VARS.contextPath + '/res/img/add.png');
					tr.append($('<td>').append(img));
					img.click(function(){
						//null, false, true
						addOptionToEmbbededSelect(null, {
							includeDel:true
						});					
					});
				}
				
				if (includeDel){
					var img = $('<img>').attr('src', acme.VARS.contextPath + '/res/img/delete.png');
					tr.append($('<td>').append(img));
					img.click(function(){					
						tr.remove();
					});
				}
				if (cleanOptions) {				
					dynamicOptions.empty().append(tr);
				} else {
					dynamicOptions.append(tr);
				}
			};
			
			var initSaveAction = function() {
				if(editMode) {
					// save button
					$(eid.saveButton).off().click(function() {
						//Check and show wheres the error
						if(validSaveForm()){
							var jsonData = loadData();
							editActionPromise(jsonData);
						}
					});
				} else {
					// save button
					$(eid.saveButton).off().click(function() {
						//Check and show wheres the error
						if(validSaveForm()){
							var jsonData = loadData();
							saveActionPromise(jsonData);
						}
					});
				}
			};
			
			var deleteProcessItem = function() {
				var html = I18Nmsg('admin.cruds.processitem.delete.confirmation.message');
				$('<div></div>').html(html).dialog({ 	
					buttons : [
				  	           {	
				  	        	   id : "confirmation_ok_button",
				  	        	   text : I18Nmsg('web.generic.ok'),
				  	        	   click : function(){
				  	        		 var ajaxRequest = acme.AJAX_FACTORY.newInstance();
					  					ajaxRequest.url += uri.deleteProcessItem;
					  					ajaxRequest.data = "id=" + processItemRootId;
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
				  	        	   text : I18Nmsg('web.generic.cancel'),
				  	        	   click : function(){
				  	        			$(this).dialog("close");
			  	        				$(this).dialog('detroy');
					  	        		$(this).remove(); 
				  	        	   }
				  	           }
				  	        ],
					autopen : true,
					modal : true,
					width: "320",
					height: "180",
					title : I18Nmsg('admin.cruds.processitem.delete.confirmation.title')
				});
			};
			
			var initActions = function() {
				// On change, show only the elements for a particular type
				$(eid.type).off().change(function() {
					var selectedValue = $(this).val();
					handleTypeChange(selectedValue);
				});
				
				
				initSaveAction();
				
				$(eid.saveAsButton).off().click(function(){
					resetFormDialog();
					saveAsDialog.dialog('open');
				});
				
				$(eid.deleteButton).off().click(function(){
					deleteProcessItem();
				});
				
				// Source select for dropdowns
				$(eid.dropdownSource).off().change(function(){
					var selectedValue = $(this).val();
					handleDropdownSourceChange(selectedValue);
				});
				
				$(eid.lookupIdentifier).off().change(function() {	
					loadLookupColumns();
				});
				
			};
			
			var formLabelFormatter = function(cellvalue, options, rowObject){
				var formId = rowObject.formId;
				if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.read.web")) {
					return '<a href="javascript:void(0)" formid="' + formId + '" class="goEditorLink">' + cellvalue + '</a>';
				} else {
					return cellvalue;
				}	
			};
			
			var projectLabelFormatter = function(cellvalue, options, rowObject){
				var projectId = rowObject.projectId;
				if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnProject(projectId,"project.read.web")) {
					return '<a href="javascript:void(0)" projectid="' + projectId + '" class="goProjectLink">' + cellvalue + '</a>';
				} else {
					return cellvalue;
				}	
			};
			
			var actionFormatter = function(cellvalue, options, rowObject){
				
				var rowId = options.rowId;
				var actions='';
				if(processItemToEdit.version != rowObject.processItemVersion) {
					var formId = rowObject.formId;
					if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.design")) {
						showUpgradeAllButton = true;
						return '<a href="javascript:void(0)" formid="' + formId + '" class="glyphicons iconOnTablesEnable refresh upgradeLink" title="' + I18Nmsg('web.generic.upgrade') + '"><i></i></a>';
					}
					actions= acme_ui.iconForTable(
							rowId,
							'refresh',
							'upgradeLink',
							I18N('web.generic.upgrade'),
							function(){
								var hasDesign= acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.design");
								if(hasDesign){
									showUpgradeAllButton=true;
								}
								return hasDesign;
							}
							
					);
				}
				
				
				return '<div class="actionButtonOnTable" style="width:45px;" >'+actions+'</html>';
			};
			
			var upgradeProcessItem = function(processItemRootId, formId) {
				ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += uri.upgrade;
				ajaxRequest.data={processItemId:processItemRootId,formId:formId};
				ajaxRequest.loadingSectionId = 'processItem_formsContainer';
				ajaxRequest.success = function(response){
					if(response.success) {
						acme_ui.HTML_BUILDER.notifySuccess(response.title, "");
						showUpgradeAllButton = false;
						$(eid.formsGrid).trigger("reloadGrid");
					} else {
						acme_ui.HTML_BUILDER.notifyError(response.title,"");
					}
				};
				$.ajax(ajaxRequest);
			};
			
			var upgradeAll = function() {
				ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += uri.upgradeAll;
				ajaxRequest.data={processItemId:processItemToEdit.rootId};
				ajaxRequest.loadingSectionId = 'processItem_formsContainer';
				ajaxRequest.success = function(response){
					if(response.success) {
						acme_ui.HTML_BUILDER.notifySuccess(response.title, "");
						showUpgradeAllButton = false;
						$(eid.formsGrid).trigger("reloadGrid");
					} else {
						acme_ui.HTML_BUILDER.notifyError(response.title,"");
					}
				};
				$.ajax(ajaxRequest);
			};
			
			var formsGridComplete = function(){
				if(showUpgradeAllButton) {
					$(eid.upgradeAllContainer).show();
				} else {
					$(eid.upgradeAllContainer).hide();
				}
				$(".goEditorLink").click(function(){
					var formId = $(this).attr('formid');
					acme.LAUNCHER.launch(constants.LAUNCHER_IDS.form_edit,{formId:formId});
				});
				$(".goProjectLink").click(function(){
					var projectId = $(this).attr('projectid');
					var editorPromise=acme.LAUNCHER.launch(constants.LAUNCHER_IDS.project_edit,projectId);
				});
				$(".upgradeLink").click(function(){
					var formId = $(this).attr('formid');
					upgradeProcessItem(processItemToEdit.rootId, formId);
				});
			};
			
			var initFormsGrid = function() {	
				formGrid = $(eid.formsGrid).jqGrid({
				   	url: acme.VARS.contextPath + uri.processItemForms,
					datatype: "json",
					jsonReader: jsonReader,
				   	colNames:['FormId',I18Nmsg('web.generic.project'),I18Nmsg('web.generic.form'), I18Nmsg('admin.processitem.processItemVersion'),I18Nmsg('web.generic.action')],
				   	colModel:[
				   		{name:'formId',index:'formId', width:55, hidden:true,sortable:false},
				   		{name:'projectName',index:'projectName', width:90,formatter:projectLabelFormatter,sortable:false},
				   		{name:'formName',index:'formName', width:100, formatter:formLabelFormatter,sortable:false},
				   		{name:'processItemVersion',index:'processItemVersion', width:70,sortable:false},
				   		{name:'action', index:'action', width:70, align:"center", formatter:actionFormatter,sortable:false}
				   	],			   
				   	rowNum:10,
				   	rowList:[10,20,30],
				   	pager: eid.formsPager,
				   	sortname: 'id',
				    viewrecords: true,
				    sortorder: "desc",
				    rownumbers: true,
			        autowidth : true,
			        width : '100%',
					height : '100%',
					gridComplete:formsGridComplete,
					postData : {
				    	rootId : function() {
				    		return processItemRootId;
				    	}
				    },
				    caption:I18Nmsg('admin.form.processitem.forms.title')
				});
				$(eid.formsGrid).jqGrid('navGrid', eid.formsPager, {edit:false,add:false,del:false});
				
				$(eid.upgradeAll).click(function(){
					upgradeAll();
				});
				resizeTables();
			};
	
			var initProcessItemTabs = function() {
				tab = $(eid.tabs).show();
			};
	
			var clearProcessItemState = function() {
				processItemId = null;
				processItemToEdit = null;
				processItemRootId = null;
				processItemVersion = null;
				formGrid = null;
				showUpgradeAllButton = false;
			};
			
			var resetFormDialog = function(){
				var poolSelect = $(eid.poolDialog);
				var newLabelInput = $(eid.newLabelDialog);
				
				newLabelInput.val('');
				poolSelect.val('');
				
				saveAsDialog.dialog( "option", "title", I18Nmsg('admin.processitem.dialog.saveas.title'));
			};
			
			var initSaveAsDialog = function(){
				var poolSelect = $(eid.poolDialog);
				var newLabelInput = $(eid.newLabelDialog);
				var messageDialog = $(eid.messageDialog);
				
				saveAsDialog = $(eid.saveAsDialog).dialog({
					buttons : [ 
						{
							text : I18Nmsg("web.generic.cancel"),
							click : function() {
								$(messageDialog).html('');
								$(this).dialog("close");
							}
						},
						{
							text : I18Nmsg("web.generic.ok"),
							click : function() {
								var jsonData = loadData();
								jsonData.pool = $(eid.poolDialog).val();
								jsonData.label = $(eid.newLabelDialog).val();
								saveActionPromise(jsonData);
								$(messageDialog).html('');
								$(this).dialog("close");
							}
						}
					],
					autoOpen : false,
					modal : true,
					width: 400,
					height: "auto",
					open : function(){
						populatePoolSelect(eid.poolDialog);
					}
				});
			};
			
			var initNewOperation = function(poolId) {
				// Now we are in new mode
				editMode = false;
				
				$(eid.poolGroup).show();
				
				clearProcessItemState();
				
				var typesPromise = loadProcessItemTypesPromise();
				var poolsPromise = populatePoolSelect(eid.pool);
				
				$.when(typesPromise, poolsPromise, i18nPromise).then(function() {
					$(eid.title).text(I18Nmsg('web.home.admin.processitem.new'));
					handleTypeChange(defaultType);
					// Everything is loaded. Unlock the buttons
					initProcessItemTabs();
					// Select thee pool based on the provided id
					$(eid.pool).val(poolId);
					// TODO Disable buttons initially
					// enableSaveAction(false);
					// New operation can always change the type
					$(eid.type).removeAttr('disabled');
					$(eid.dropdownSource).removeAttr('disabled');
					$(eid.lookupIdentifier).removeAttr('disabled');
					$(eid.lookupLabel).removeAttr('disabled');
					$(eid.lookupValue).removeAttr('disabled');
					
					initActions();
					
					updateVersionInfo();
					
					$(eid.formsMessage).show();
					$(eid.formsContainer).hide();
					
					// Show the properties
					$(eid.properties).show();
					
					dfd.resolve();
				});
			};
			
			pkg.resized=function(){
				resizeTables();
			};
	
			var initEditOperation = function(rootId) {
				editMode = true;
				
				processItemRootId = rootId;
				initSaveAsDialog();
				$(eid.saveAsButton).show();
				$(eid.deleteButton).show();
				$(eid.poolLinkGroup).show();
				$(eid.poolLink).click(function() {
					acme.LAUNCHER.launch(constants.LAUNCHER_IDS.pool_edit, processItemToEdit.pool);
				});
				
				var typesPromise = loadProcessItemTypesPromise();
				var processItemPromise = loadProcessItemPromise();			
				
				$.when(typesPromise,processItemPromise, i18nPromise).then(function() {	
					loadPool(processItemToEdit.pool);
					// Show the properties
					$(eid.properties).show();
					$(eid.title).text(I18Nmsg('web.home.admin.processitem.edit'));
					// Everything is loaded. Unlock the buttons
					initProcessItemTabs();
					populateFormFields();
					// Edition doesn't let the type
					$(eid.type).attr('disabled', 'disabled');
					
					initActions();
					
					updateVersionInfo();
					
					$(eid.formsMessage).hide();		
					$(eid.formsContainer).show();
					
					initFormsGrid();
					
					dfd.resolve();
				});		
			};
			
			var init = function() {
				$(eid.versionInfo).hide();
				
				// Version filed is always disabled
				$(eid.version).hide();
				$(eid.version).attr('disabled', 'disabled');
			};
			
			// Public interface: Methods and attributes
			var that = {};		
			
			that.initNewOperation = initNewOperation;
			that.initEditOperation = initEditOperation;
			init();
			return that;
		};
		
		processItemPkgLoadDfd.resolve();

	});
	
	pkg.stop = function(){
		if(saveAsDialog) {
			saveAsDialog.dialog('destroy');
			saveAsDialog.remove();
			saveAsDialog = null;
		}
	};
	
	pkg.resized=function(){
		resizeTables();
	};
	
	pkg.start = function(options) {
		dfd=$.Deferred();
		poolList=null;
		$.when(pkg.loaded,i18nPromise).then(function() {		
			var piManager = pkg.ProcessItemManager();	
			if (options) {
				var rootId = options.rootId;
				var poolId = options.poolId;
				if (rootId) {
					piManager.initEditOperation(rootId);
				} else {
					piManager.initNewOperation(poolId);			
				}
			} else {
				piManager.initNewOperation();
			}		
		});
		return dfd.promise();
	};
	
	return pkg;
});