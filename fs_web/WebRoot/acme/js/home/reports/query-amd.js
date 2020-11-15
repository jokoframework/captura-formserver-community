define(["jquery", "acme", "acme-ui", "constants","editor/model","editor/common","jquery-ui-timepicker-addon","jquery.numeric", "home/reports/fileNameInput"],
		function($, acme, acme_ui,constants,model,COMMON_EDITOR,timePickerMod, JQ_NUMERIC, fileNameInput) {
            //this module manage the creation of queries for the reports
            var pkg = {};
            var META_PREFIX = "meta_";
            var metaColumni18nKeys = ['web.home.reports.column.meta.mail', 'web.home.reports.column.meta.receivedAt', 'web.home.reports.column.meta.location', 'web.home.reports.column.meta.savedAt', 'web.home.reports.column.meta.state'];
            var operatorI18n = ['web.home.reports.operator.equals', 'web.home.reports.operator.greater', 'web.home.reports.operator.less', 'web.home.reports.operator.between', 'web.home.reports.operator.like'];
            var i18nPromise = acme.I18N_MANAGER.load(['web.home.querys.new.title',
                'web.home.querys.edit.title',
                'web.generic.requiredField',
                'web.home.reports.column.meta.mail',
                'web.home.reports.column.meta.receivedAt',
                'web.home.reports.column.meta.location', 'web.home.querys.metadataLabel',
                'web.home.querys.addFilterDialog_title', 'web.home.querys.delete.confirmation.title',
                'web.home.querys.delete.confirmation.message',
                'web.generic.available',
                'web.generic.selected',
                'web.home.querys.save.error.title',
                'web.home.querys.save.error.message',
                'web.home.querys.section.elementsFileNames.inputHelp',
                'web.home.reports.column.meta.savedAt'
            ].concat(metaColumni18nKeys).concat(operatorI18n));

            var loadRequiredI18n = function () {
                return i18nPromise;
            };
            var i18n = acme.I18N_MANAGER.getMessage;


            var numberOperator = [
                {label: operatorI18n[0], value: 'EQUALS'},
                {label: operatorI18n[1], value: 'GT'},
                {label: operatorI18n[2], value: 'LT'},
            ];

            var dateOperators = [
                {label: operatorI18n[0], value: 'EQUALS'},
                {label: operatorI18n[1], value: 'GT'},
                {label: operatorI18n[2], value: 'LT'}
            ];
            var stringOperators = [
                {label: operatorI18n[0], value: 'EQUALS'},
                {label: operatorI18n[4], value: 'LIKE'}
            ];
            
            var stateOperators = [
                {label: operatorI18n[0], value: 'EQUALS'}              
            ];
            //This is a map of the meta column id to its i18nValue
            var metai18nMap = {
                'meta_mail': metaColumni18nKeys[0],
                'meta_receivedAt': metaColumni18nKeys[1],
                'meta_location': metaColumni18nKeys[2],
                'meta_savedAt': metaColumni18nKeys[3],
                'meta_stateId' : metaColumni18nKeys[4]
            };
            var mapMetaColumnToi18n = function (metaColumnId) {
                return metai18nMap[metaColumnId] || metaColumnId;
            };


            var queryDTO;
            var formModel;
            var workflow;

            //An array of the form {'element':element,operator:,value:xxx}, element is the JSON of MFElement, operatior is one of .... and value is the selected value that the user wanted to use
            var selectedFilterList = [];


            /**This method will add elements of the form {id:"meta_xx",label:"yyyy"} to the array. These are columns that represent the meta data associated with each row**/
            var addMetadataColumnsDef = function (list) {
                list[list.length] = {id: 'meta_mail', label: '* ' + i18n(mapMetaColumnToi18n('meta_mail'))};
                list[list.length] = {id: 'meta_receivedAt', label: '* ' + i18n(mapMetaColumnToi18n('meta_receivedAt'))};
                list[list.length] = {id: 'meta_location', label: '* ' + i18n(mapMetaColumnToi18n('meta_location'))};
                list[list.length] = {id: 'meta_savedAt', label: '* ' + i18n(mapMetaColumnToi18n('meta_savedAt'))};
                //cap-376
                if (workflow) {
                	list[list.length] = {id: 'meta_stateId', label: '* ' + i18n(mapMetaColumnToi18n('meta_stateId'))};
            	}
                
                return list;
            };
            /**Based on the selectedForm will show the data*/
            var showFormData = function () {
                $('#formLabelOnReport').html(formModel.label);
                $('#formVersion').html(formModel.version);
            };
            var loadQuery = function (queryId) {

                var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                ajaxRequest.url += '/reports/querys/getQuery.ajax';
                ajaxRequest.data = {queryId: queryId};

                return $.ajax(ajaxRequest);

            };

            var setFormModel = function (pFormModel) {
            	if (!formModel) {
            		formModel = pFormModel;
            	}
            };
            
            var init2SideMultiSelects = function (elementList, selectedTableColumns, selectedSortingColumns) {
                //this is an array that do not contains elements of type PICTURE
                var downloableColumns = [];
                var i = 0;

                for (i = 0; i < elementList.length; i++) {
                    if (elementList[i].id.startsWith("meta_") || elementList[i].proto.type !== 'PHOTO') {
                        downloableColumns[downloableColumns.length] = elementList[i];
                    }
                }

                $('#tableColumns').ACME2SideMultiSelect({
                    completeListArray: elementList,
                    selectedListArray: selectedTableColumns,
                    selectedLabel: i18n('web.generic.selected'),
                    availableLabel: i18n('web.generic.available')
                });

                $('#sortingColumns').ACME2SideMultiSelectWithCombination({
                    completeListArray: elementList,
                    selectedListArray: selectedSortingColumns,
                    multipleSelect: false,
                    optionList: ['ASC', 'DESC'],
                    selectedLabel: i18n('web.generic.selected'),
                    availableLabel: i18n('web.generic.available')
                });
            };

            var createOperatorSelect = function (filterId, validOperators, selectedOperator) {
                var translatedOperator = [];
                var i = 0;
                var operatorObj = {};
                for (i = 0; i < validOperators.length; i++) {
                    operatorObj = {};
                    operatorObj.value = validOperators[i].value;
                    operatorObj.label = i18n(validOperators[i].label);
                    translatedOperator[translatedOperator.length] = operatorObj;
                }
                var select = acme_ui.HTML_BUILDER.createSelect("filterOperator_" + filterId, translatedOperator, selectedOperator);
                select.addClass('filterOperator');
                return select;
            };
            var createFilterDiv = function (filterId, validOperators, proto, operator, value) {


                var div = $('<div class="queryFilter control-group"><label class="control-label" for="filter_' + filterId + '">' + proto.label + '</label><div class="controls"></div></div>');
                var controls = div.find(".controls");
                var selectOperator = createOperatorSelect(filterId, validOperators, operator);
                controls.append(selectOperator);
                var input = $('<input type="text" class="filterInput"  />');
                if (typeof value != 'undefined') {
                    input.val(value);
                }
                controls.append(input);
                var deleteButton = $('<a class="glyphicons glyphiconsFilter bin clickeable"><i></i></a>');
                deleteButton.click(
                    function () {
                        div.remove();
                    }
                );
                controls.append(deleteButton);
                return div;

            };
            
            var createStateFilterDiv = function (filterId, validOperators, proto, operator, value) {
            	
                var div = $('<div class="queryFilter control-group"><label class="control-label" for="filter_' + filterId + '">' + proto.label + '</label><div class="controls"></div></div>');
                var controls = div.find(".controls");
                var selectOperator = createOperatorSelect(filterId, validOperators, operator);
                controls.append(selectOperator);
                var select = $('<select class="filterInput"  />');
                controls.append(select);
                var statesPromise = getStates(formModel.id, formModel.version);
                $.when(statesPromise).then(function (states) {
                	loadStatesSelect(select, states);
                });
                if (typeof value != 'undefined') {
                    select.val(value);
                }
                var deleteButton = $('<a class="glyphicons glyphiconsFilter bin clickeable"><i></i></a>');
                deleteButton.click(
                    function () {
                        div.remove();
                    }
                );
                controls.append(deleteButton);
                return div;

            };
            var getStates = function (formId, version) {

                var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                ajaxRequest.type = 'GET';
                ajaxRequest.url += '/api/workflow/states';
                ajaxRequest.data = {formId: formId, version: version};

                return $.ajax(ajaxRequest);

            };
            var loadStatesSelect = function (select, states) {
            	var option = $('<option>');
            	option.val("").html("");
            	select.append(option);
            	for (var i=0; i < states.length; i++) {
            		option = $('<option>');
            		option.val(states[i].id).html(states[i].name);
            		select.append(option);
            	} 
            };
            //Add a new filter option for the element using the value
            //element can't be null
            //operator can be null and the default operator of the type will be assumed
            //value can be null meaning that there is no default query.
            var addFilterOption = function (filterContainer, element, operator, value) {

                var filter = {};
                filter.element = element;
                filter.operator = operator;
                filter.value = value;
                //store the filter on the array of selected filters
                selectedFilterList[selectedFilterList.list] = filter;
                //paint the filter
                var filterDiv = null;
                var d;
                if (element.proto.type === 'INPUT') {
                    //TEXT("text"), DATE("date"), TIME("time"), DATETIME("datetime"), PASSWORD("password"), INTEGER("integer"), DECIMAL(
                    //"decimal"), TEXTAREA("textarea");
                    if (element.proto.subtype === 'TEXT'
                        || element.proto.subtype === 'TEXTAREA'
                        ) {
                        filterDiv = createFilterDiv(selectedFilterList.length, stringOperators, element.proto, operator, value);

                    } else if (element.proto.subtype === 'DATE' || element.proto.subtype === 'DATETIME') {
                        //We don't set the value of the datepicker because it needs to be translated to a Date in order to be correctly handled by the datepicker
                        filterDiv = createFilterDiv(selectedFilterList.length, dateOperators, element.proto, operator, null);
                        var datepicker = $(filterDiv).find(".filterInput").datetimepicker();

                        datepicker.click(
                            function () {
                                //The menu of bootsrap has a z-index of 1030.
                                //I couldn't set this via CSS files because the plugin assigns this somewhere as a fixed element style value.
                                //Do not try to remove the "click" events, putting this inline won't work on firefox
                                $("#ui-datepicker-div").css('z-index', '1031');
                            }
                        );

                        if (value) {
                            d = new Date();
                            d.setISO8601(value);
                            datepicker.datetimepicker("setDate", d);
                        }

                    } else if (element.proto.subtype === 'TIME') {

                    } else if (element.proto.subtype === 'PASSWORD') {

                    } else if (element.proto.subtype === 'INTEGER') {
                        filterDiv = createFilterDiv(selectedFilterList.length, numberOperator, element.proto, operator, value);
                        $(filterDiv).find(".filterInput").numeric({decimal: false});

                    } else if (element.proto.subtype === 'DECIMAL') {
                        filterDiv = createFilterDiv(selectedFilterList.length, numberOperator, element.proto, operator, value);
                        $(filterDiv).find(".filterInput").numeric({ decimal: "." });
                    }
                } else if (element.proto.type === 'SELECT') {
                	var metaColumnName = parseMetaColumnName(element.instanceId);
                	if (metaColumnName !== 'stateId') {
                		filterDiv = createFilterDiv(selectedFilterList.length, stringOperators, element.proto, operator, value);
                	} else {
                		filterDiv = createStateFilterDiv(selectedFilterList.length, stateOperators, element.proto, operator, value);
                	}
                    
                }

                if (filterDiv) {
                    filterDiv.data('element', element);
                    filterContainer.find(".addFilterButtonDiv").before(filterDiv);
                }
            };

            var createNewFilterSection = function (page_id, title) {
                var pageContent = $('<div class="accordion-group"><div class="accordion-heading"><a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#page_' + page_id + '">' + title + '</a></div></div>');
                var pageContentBody = $('<div class="accordion-body collapse in" id="page_' + page_id + '"></div>');
                pageContent.append(pageContentBody);
                var pageContentInner = $('<div class="accordion-inner"></div>');
                pageContentBody.append(pageContentInner);
                return pageContent;
            };

            var createFilterPossibility = function (element, filterContainer) {
                var iLink = COMMON_EDITOR.iconLink(element.proto);
                var protoLabel = '<span class="protoLabel" >' + element.proto.label + '</span>';
                var divElement = $('<div class="reportFilterElement toolboxItem" >' + iLink + protoLabel + '</div>');
                divElement.data('element', element);
                divElement.click(function () {
                    var element = $(this).data('element');
                    addFilterOption(filterContainer, element);
                    $("#addFilterDialog").dialog('close');
                });
                return divElement;
            };

            //These methos will create a process item element that represent a metadata but has the same structure as an MFElement.
            //This way, the selection process of filters, developed for regular process item, can be reused for metadata
            var createMetaProcessItemMail = function () {
                var element = {};
                element.instanceId = 'meta_mail';
                element.proto = {};
                //The email will use the same operators available to a string search and will display the icon assigned to the TEXT process item
                element.proto.type = 'INPUT';
                element.proto.subtype = 'TEXT';//this type is just to use the icon assigned to the TEXT
                element.proto.label = i18n(mapMetaColumnToi18n('meta_mail'));
                return element;
            };
            
            var createMetaProcessItemDates = function (metaName) {
            	var metaDates = ['receivedAt', 'savedAt'];
            	var elements = [];
            	for (var i = 0; i < metaDates.length; i++) {
            		elements.push(createMetaProcessItemDate(metaDates[i]));
            	}
            	return elements;
            };
            
            var createMetaProcessItemDate = function (metaName) {
                var element = {};
                element.instanceId = 'meta_' + metaName;
                element.proto = {};
                //The recevedAt will use the same operators available to a process item of type DATE and will display the icon assigned to DATE process item.
                element.proto.type = 'INPUT';
                element.proto.subtype = 'DATE';
                element.proto.label = i18n(mapMetaColumnToi18n('meta_' + metaName));
                return element;
            };

            var createMetaProcessItemState = function () {
                var element = {};
                element.instanceId = 'meta_stateId';
                element.proto = {};
                //The state will use the same operators available to a string search and will display the icon assigned to the SELECT process item
                element.proto.type = 'SELECT';
                element.proto.label = i18n(mapMetaColumnToi18n('meta_stateId'));
                return element;
            };
            
            /**
             * This method initialize the resources that only need to be done once. This are going to be destroyed later on the destroy method
             * **/
            var init = function () {
                $.when(i18nPromise).then(function () {
                    var dialogTitle = i18n('web.home.querys.addFilterDialog_title');
                    var dialogDiv = $('<div id="addFilterDialog" title="' + dialogTitle + '" >' +
                        '<div class="accordion" id="addFilterDialog_content_collapside"  >' +
                        '</div>' +
                        '</div>');
                    $('body').append(dialogDiv);
                    //Bootrap menu has a z-index of 3000, so we need something bigger.
                    dialogDiv.dialog({autoOpen: false, modal: true, zIndex: 3002});

                    $("#addFilterButton").click(
                        function () {
                            $("#addFilterDialog").dialog('open');
                        }
                    );
                });

            };
            var destroy = function () {
                $("#addFilterDialog").dialog('destroy');
                $("#addFilterDialog").remove();
                //cap-376
                workflow = undefined;
            };
            var initFilterSection = function (formModel, filterContainer, workflowDefined) {
                $.when(i18nPromise).then(function () {
                    var i = 0;
                    //Initialize the dialog

                    //delete previous content
                    $("#addFilterDialog_content_collapside").html("");
                    var collapOfContent = $("#addFilterDialog_content_collapside");
                    var pageContent, pageContentInner, page, element, divElement, numberOfElements;
                    //creates a section for the metadata
                    pageContent = createNewFilterSection('metadataPage', i18n('web.home.querys.metadataLabel'));
                    pageContentInner = pageContent.find('.accordion-inner');

                    //creates elements for the metadata properties, so we can handle them as if they were regular process item
                    element = createMetaProcessItemMail();
                    divElement = createFilterPossibility(element, filterContainer);
                    pageContentInner.append(divElement);

                    var elements = createMetaProcessItemDates();
                    for (var i = 0; i < elements.length; i ++) {
                    	divElement = createFilterPossibility(elements[i], filterContainer);
                    	pageContentInner.append(divElement);
                	}
                    
                    //cap-376 workflow metadata filter
                    if (workflow === undefined) {
                    	workflow = workflowDefined; 
                    } 
                    
                    if (workflow) {
                    	element = createMetaProcessItemState();
	                    divElement = createFilterPossibility(element, filterContainer);
	                    pageContentInner.append(divElement);
                    }
                    //////////////////////////////////
                    
                    collapOfContent.append(pageContent);


                    //add a section for every available page
                    for (i = 0; i < formModel.pages.length; i++) {
                        page = formModel.pages[i];

                        //add the title of the page as header of the section
                        pageContent = createNewFilterSection(page.id, page.label);
                        pageContentInner = pageContent.find('.accordion-inner');

                        numberOfElements = 0;
                        for (var j = 0; j < page.elements.length; j++) {
                            element = page.elements[j];

                            if (element.proto.type === 'INPUT' || element.proto.type === 'SELECT') {
                                divElement = createFilterPossibility(element, filterContainer);
                                pageContentInner.append(divElement);
                                numberOfElements++;
                            }
                        }
                        if (numberOfElements > 0) {
                            //only add a section for the page if it contains at least one element that can be used as a filter.
                            collapOfContent.append(pageContent);
                        }

                    }
                    
                    setFormModel(formModel);
                });


            };

            /**
             * A meta column is different from the others since it has the "meta_" prefix. The implementation of this method just checks that the strings starts with "meta_".
             * Even-though the current implementation is very simple, this method will assure backwards compatibility if the meta differentiator changes in the future.
             *
             * */
            var isMetaColumn = function (elementId) {
                return elementId.startsWith(META_PREFIX);
            };

            /**This is a funciton that obtain the column name of a meta column. A meta column starts with meta_ and this method will return the string that is after the dash.
             * Although this is very simple the implementation method might change in the future and this method will assure backwards compatibility
             * */
            var parseMetaColumnName = function (elementId) {
                if (isMetaColumn(elementId)) {
                    return elementId.substr(META_PREFIX.length);
                }
                throw 'The elememnt ' + elementId + 'is not a meta column';
            };

            var initSelectedFilter = function (filterContainer, modelMap, filterOptions) {
                //delete all previous content of the filter
                filterContainer.find(".queryFilter").remove();
                //This is a public method so it is very important to check if the i18n values were already loaded.
                $.when(i18nPromise).then(
                    function () {
                        if (!filterOptions) {
                            //do nothing if there are no filter selected
                            return;
                        }
                        var i = 0, element, metaColumn;

                        for (i = 0; i < filterOptions.length; i++) {

                            if (isMetaColumn(filterOptions[i].elementId)) {
                                metaColumn = parseMetaColumnName(filterOptions[i].elementId);
                                if (metaColumn === 'mail') {
                                    element = createMetaProcessItemMail();
                                } else if (metaColumn === 'receivedAt' || metaColumn === 'savedAt') {
                                    element = createMetaProcessItemDate(metaColumn);
                                } else if (metaColumn === 'stateId') {
                                    element = createMetaProcessItemState();
                                } else {
                                    //This is a situation that should never happened. Since the aforementioned columns are the supported for the elements
                                    throw 'Not supported meta column ' + metaColumn;
                                }
                            } else {
                                element = modelMap[filterOptions[i].elementId];
                            }
                            addFilterOption(filterContainer, element, filterOptions[i].operator, filterOptions[i].value);
                        }


                    }
                );

            };
            var removeErrorMessages = function () {
                $('.error').each(function (index) {
                    $(this).removeClass('error');
                    $(this).find('.errorLabel').remove();
                });
            };

            var controlRequiredFields = function () {
                var response = true;
                var label = $("#queryName").val();
                if (!/\S/.test(label)) {
                    // string is empty or just whitespace
                    $("#name-control-group").addClass("error");
                    $("#queryName").parent().append('<p class="errorLabel">' + i18n('web.generic.requiredField') + '</p>');
                    response = false;
                }
                return response;
            };

            //This method will examine the html and collect the selected filter (if any)
            //The returned object is an array with items of the form {elementId:element.id,operator:op,value:inputFilter} , op is the operator selected on the dropdown and
            //inputFilter is the default value that the system will use for this query
            var gatherFilterOptions = function (filterSection) {
                var filterSelection = [];
                var i = 0;
                var d;
                filterSection.find(".queryFilter").each(function (divElement) {
                    //An instance of MFElement that represents the process item selected for the filter
                    var element = $(this).data('element');
                    filterSelection[i] = {};
                    filterSelection[i].elementId = element.instanceId;
                    filterSelection[i].operator = $(this).find(".filterOperator").val();

                    if (element.proto.type === 'INPUT') {
                        if (element.proto.subtype === 'INTEGER' ||
                            element.proto.subtype === 'DECIMAL' ||
                            element.proto.subtype === 'TEXTAREA' ||
                            element.proto.subtype === 'TEXT'
                            ) {
                            //these are elements that have an operator (see on this file the method addFilterOption)
                            filterSelection[i].value = $(this).find(".filterInput").val();
                        } else if (element.proto.subtype === 'DATE'
                            || element.proto.subtype === 'DATETIME') {
                            d = $(this).find(".filterInput").datetimepicker("getDate");
                            //Dates are transmitted in ISO8601
                            if (d) {
                                filterSelection[i].value = d.toISOString();
                            }
                        }
                    } else if (element.proto.type === 'SELECT') {
                        filterSelection[i].value = $(this).find(".filterInput").val();
                    }


                    i++;
                });
                return filterSelection;
            };

            var serializeElementsFileNames = function () {
                var arr = $("#elementsFileNamesForm").serializeArray();
                var obj = {};
                $.map(arr, function (input, index) {
                    if (!input.value) {
                        return;
                    }
                    obj[input.name] = input.value;
                });
                return obj;
            };


            var saveQuery = function () {
                var name = $('#queryName').val();
                var defaultQuery = $('#defaultQuery')[0].checked;
                var downloadLocationsAsLinks = $('#checkboxGoogleMapsLinks').prop('checked');
                var tableColumns = $('#tableColumns').ACME2SideMultiSelect('getSelectedItems');
                var selectedSortingColumns = $('#sortingColumns').ACME2SideMultiSelectWithCombination('getSelectedItems');
                var elementsFileNames = serializeElementsFileNames();

                var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                ajaxRequest.url += '/reports/querys/saveQuery.ajax';
                ajaxRequest.loadingSectionId = 'page_width';
                ajaxRequest.contentType = 'application/json; charset=utf-8';

                var queryDef = {
                    id: queryDTO && queryDTO.id,
                    formId: formModel.id,
                    version: formModel.version,
                    name: name,
                    defaultQuery: defaultQuery,
                    selectedTableColumns: tableColumns,
                    selectedSortingColumns: selectedSortingColumns,
                    downloadLocationsAsLinks: downloadLocationsAsLinks,
                    elementsFileNames: elementsFileNames
                };

                var filterDiv = $("#selectedFiltersForm");
                queryDef.filterOptions = gatherFilterOptions(filterDiv);
                ajaxRequest.data = JSON.stringify(queryDef);
                ajaxRequest.success = function (response) {
                    if (response.success) {
                        queryDTO = response.obj;
                        acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
                        //change the title to edit mode
                        $('#pageTitle').html(i18n('web.home.querys.edit.title'));
                        $("#deleteQuery").show();
                    } else {
                        acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
                    }
                };
                $.ajax(ajaxRequest);
            };
            var deleteQuery = function (queryId, onSuccessCallback) {
                function doDelete() {
                    var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                    ajaxRequest.url += '/reports/querys/deleteQuery.ajax';
                    //ajaxRequest.data={'queryId':queryDTO.id};
                    ajaxRequest.data = {'queryId': queryId};
                    ajaxRequest.success = function (response) {
                        if (response.success) {
                            var launcherId = constants.LAUNCHER_IDS.reports;
                            acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
                            acme.LAUNCHER.launch(launcherId, {formId: formModel.id, selectedVersion: formModel.version, selectedQuery: null});
                            if (onSuccessCallback !== undefined) {
                                onSuccessCallback();
                            }
                        } else {
                            acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
                        }
                    };
                    $.ajax(ajaxRequest);
                }

                var title = i18n('web.home.querys.delete.confirmation.title');
                var message = i18n('web.home.querys.delete.confirmation.message');

                acme_ui.confirmation(title, message, doDelete);
            };

            var initActions = function () {
                function goBack() {
                    var launcherId = constants.LAUNCHER_IDS.reports;
                    acme.LAUNCHER.launch(launcherId, {formId: formModel.id, selectedVersion: formModel.version, selectedQuery: queryDTO});
                };
                $('#backLink').click(goBack);
                $('#backButton').click(goBack);

                $('#saveQuery').click(function () {
                    removeErrorMessages();
                    if (controlRequiredFields()) {
                        saveQuery();
                    } else {
                        acme_ui.HTML_BUILDER.notifyError(i18n('web.home.querys.save.error.title'), i18n('web.home.querys.save.error.message'));
                    }
                });
                $('#deleteQuery').click(function () {
                    deleteQuery(queryDTO.id);
                });
            };


   var initElementsFileNames = function (elementList, elementsFileNames) {
       var input, element, selectedFileName, inputArr = [];

       var uniqueElements = fileNameInput.utils.toArrayOfUniqueLabels(elementList);

       var elementDiv = $("#elementsFileNamesForm .control-group:first");

       // clean-up before reloading inputs
       $("#elementsFileNamesForm .control-group").not(':first').remove();

       var inputHelp = i18n("web.home.querys.section.elementsFileNames.inputHelp");

       for (var i = 0; i < elementList.length; i++) {
            var element = elementList[i];
            if (!element.id.startsWith("meta_") && element.proto.type === 'PHOTO') {
                elementDiv.find(".control-label").text(element.label);
                selectedFileName = fileNameInput.getFileNameByElement(element.id, elementsFileNames);
                // what the user sees
                input = $("<input/>", {
                    type: "text",
                    placeholder: inputHelp
                }).addClass('span3');

                // what will be stored in server
                hidden = $("<input/>", {
                    type: "hidden",
                    name: element.id
                });
                elementDiv.find("div.controls").html(input);
                elementDiv.find("div.controls").append(hidden);
                elementDiv.clone().insertAfter("#elementsFileNamesForm .control-group:last");
                configInputs(element.id, uniqueElements, selectedFileName);
            }
        }

        elementDiv.remove();
   };


    var configInputs = function(inputName, uniqueElements, selectedFileName) {
        var hiddenBox = $("#elementsFileNamesForm input[name="+ inputName +"]");
        var inputBox = hiddenBox.prev("input");

        inputBox.bind("input", function(){
            fileNameInput.updateSyntaxValues(this, hiddenBox, uniqueElements);
        })

        inputBox
        .bind( "keydown", fileNameInput.autocompleteSettings.keyDownHandler)
        .autocomplete({
            source: fileNameInput.autocompleteSettings.makeSourceHandler(uniqueElements),
            search: fileNameInput.autocompleteSettings.searchHandler,
            focus: fileNameInput.autocompleteSettings.focusHandler,
            select: fileNameInput.autocompleteSettings.makeSelectHandler('value', hiddenBox, uniqueElements)
         });

        // <initalization>
        (function() {
            var labels = [], labelCounts = {};

            if (selectedFileName) {
                var plainMessage = selectedFileName;
                $.each(uniqueElements, function(index, elem) {
                    var textPlain = "[" + elem.label + "]";
                    plainMessage = plainMessage.replace(new RegExp(fileNameInput.utils.regexpEncode("[" + elem.id + "]"), 'g'), textPlain);
                });
                inputBox.val(plainMessage);
                hiddenBox.val(selectedFileName);
            }
        })();
        // </initalization>
    };

	pkg.stop = function(){
		destroy();
	};
	//receive the following options
	//formModel: an MFForm on which it operates (required)
	//queryId: If its null then the module starts in "new" mode. Otherwise, it will display the data previously associated with this query
	//workflow: flag to show workflow info
	pkg.start = function(options) {
		
		if(!options.formModel) {
			throw 'the option formModel, is required to start';
		}
		
		init();
		formModel = options.formModel;
		workflow = options.workflow;
		showFormData();
		var dfd = $.Deferred();
		$.when(i18nPromise).then(function() {
			var availableColumns = model.listMFElements(formModel,false);
            var readOnlyElements = model.listMFElements(formModel,false);
            var modelMap = model.mapMFElements(formModel);
			availableColumns = addMetadataColumnsDef(availableColumns);

            var filterContainer = $("#selectedFiltersForm");
			
			if(typeof options.queryId != 'undefined') {
				//Edit mode
				$('#pageTitle').html(i18n('web.home.querys.edit.title'));
				
				$.when(loadQuery(options.queryId)).then(function (query) {
					queryDTO = query;
					
					$('#queryName').val(query.name);
					$('#defaultQuery').prop('checked',query.defaultQuery);
					$('#checkboxGoogleMapsLinks').prop('checked', query.downloadLocationsAsLinks);
					
					init2SideMultiSelects(availableColumns, query.selectedTableColumns, query.selectedSortingColumns);
					
					if(query.filterOptions) {
						initSelectedFilter(filterContainer, modelMap, query.filterOptions);
					}

                    if (query.elementsFileNames) {
                        initElementsFileNames(readOnlyElements, query.elementsFileNames);
                    }
					
					$("#deleteQuery").show();
					dfd.resolve();
				});
			} else {
				//new mode
				queryDTO=null;
				$('#pageTitle').html(i18n('web.home.querys.new.title'));
				init2SideMultiSelects(availableColumns,[],[]);
                initElementsFileNames(readOnlyElements);
				$("#deleteQuery").hide();
				dfd.resolve();
			} 
			initFilterSection(formModel,filterContainer);
			initActions();
			
		});
		return dfd.promise();
	};
	
	pkg.setFormModel = setFormModel;
	pkg.loadQuery=loadQuery;
	pkg.deleteQuery = deleteQuery;
	pkg.META_PREFIX=META_PREFIX;
	pkg.mapMetaColumnToi18n=mapMetaColumnToi18n;
	pkg.metaColumni18nKeys=metaColumni18nKeys;
	pkg.initSelectedFilter=initSelectedFilter;
	pkg.gatherFilterOptions=gatherFilterOptions;
	pkg.loadRequiredI18n=loadRequiredI18n;
	pkg.initFilterSection=initFilterSection;
	pkg.init=init;
	pkg.destroy=destroy;
	return pkg;
});