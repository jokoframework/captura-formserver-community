define(
		[ "jquery", "acme", "acme-ui", "constants",
				"home/reports/reports-jqgrid-manager",
				"home/admin/new-form-crud-amd" ,"home/reports/query-amd","editor/model", "ejs/ejs"],
		function($, acme, acme_ui, constants, jqgridmanager, formService, queryModule, modelEditor, _EJS) {

			var pkg = {};
			
			var i18nPromise = acme.I18N_MANAGER.load([
			                                            'web.home.reports.error.emptyReport.title',  
			                                            'web.home.reports.error.emptyReport.message',
			                                            'web.generic.download',
														'web.home.reports.formView.emptyMsg'
			                                         ]);
			var authPromise = acme.AUTHORIZATION_MANAGER.load();
			var i18n = acme.I18N_MANAGER.getMessage;

			// contains the id of the selected form
			var selectedFormId = null;
			// The form might have multiple version, so this variable store the
			// selected version
			var selectedVersion = null;
			// contains the JSON of FormDTO of the selected form
			var selectedForm;
			// The active query. this is JSON of the form {id:2,label:"lll"}
			var activeQuery = null;

			var FORM_LABEL_ID = "form_label";
			
			//an MFForm of the current selectedForm and selectedVersion
			var formModel;

			var formText = '<div class="span12"><form class="form-horizontal form-view-horizontal"><input type="hidden" id="rowId" value="<%= rowId %>" />' +
  		  				'<% for(var i=0; i<elements.length; i++) { %><div  class="control-group" ><label class="control-label" ><b><%= elements[i].label %>:</b>' +
  		  					'</label><%= elements[i].value %></div><% } %></form></div>';
			
			var workflowText = '<div class="span6"><form class="form-horizontal form-view-horizontal"><input type="hidden" id="rowId" value="<%= rowId %>" />' +
						'<% for(var i=0; i<elements.length; i++) { %><div  class="control-group" ><label class="control-label" ><b><%= elements[i].label %>:</b>' +
						'</label><%= elements[i].value %></div><% } %></form></div>' +
						'<div class="span6"><form id="workflowForm" class="form-horizontal form-view-horizontal">' +
							'<h3 style="text-align: center;">Información de Workflow</h3>' +
	                        '<input type="hidden" id="docId" value="<%= docId %>" />' +
	                        '<div class="control-group">' +
	                          '<label for="state" class="col-sm-2 control-label"><b>ESTADO ACTUAL</b></label>' +
	                          '<div class="col-sm-8">' +
	                            '<input type="text" class="form-control" id="state" disabled value="<%= stateName %>">' +
	                          '</div>' +
	                        '</div>' +
	                        '<div class="control-group">' +
	                          '<label for="newState" class="col-sm-2 control-label"><b>NUEVO ESTADO</b></label>' +
	                          '<div class="col-sm-8">' +
	                            '<select class="form-control" id="newState" disabled="disabled">'+
	                            	'<% for(var i=0; i<transitions.length; i++) { %>' +
	                            		'<option value="<%= transitions[i].targetState.id %>">' +
	                            		'<%= transitions[i].targetState.name %>' +
	                            		'</option>' +
	                            	'<% } %>' +
	                            '</select>' +
	                          '</div>' +
	                        '</div>' +
	                        '<div class="control-group">' +
	                          '<label for="comment" class="col-sm-2 control-label"><b>COMENTARIOS</b></label>' +
	                          '<div class="col-sm-8">' +
	                            '<textarea class="form-control" id="comment" placeholder="Comentarios..." disabled="disabled"></textarea>' +
	                          '</div>' +
	                        '</div>' +
	                        '<div class="control-group form-group-last">' +
	                          '<div class="col-sm-offset-2 col-sm-10">' +
	                            '<div class="pull-right col-sm-6">' +
	                              '<button type="button" id="btn-change-state" class="btn btn-default" data-loading-text="Procesando..." disabled="disabled">Cambiar de Estado</button>' +
	                            '</div>' +
	                            '<button type="button" id="btn-cancel-change-state" class="btn btn-primary pull-right">Cancelar</button>' +
	                          '</div>' +
	                        '</div>' +
						'</form>'+ 
						'<div id="workflowHistory_div" style="overflow: auto;">' +
							'<table id="workflowHistoryGrid" ></table>' +
						'</div></div>';
			// CAP-414 Google Static Maps API Key to Captura project
			// Generated from,
			// URL: https://developers.google.com/maps/documentation/static-maps/
			// Account: captura.app@gmail.com
			var STATIC_MAPS_API_KEY = "AIzaSyBh2ogsxnrfjpb_Ps7aaPQuSgoIcDtowFs";
            // The EJS template to be filled with document info
            // The document info is displayed as a vertical form
            var FORM_TEMPLATE = new EJS({//url: acme.VARS.contextPath + '/acme/js/home/reports/form-view-template.txt'
            	text: formText
            });

            var FORM_TEMPLATE_WITH_WORKFLOW = new EJS({//url: acme.VARS.contextPath + '/acme/js/home/reports/form-view-template.txt'
            	text: workflowText 
                      
            });
            
            var IMG_TEMPLATE = new EJS({
               //text:  '<div class="controls"><div class="thumbnail"><img src="'+ acme.VARS.contextPath + '/reports/images/image.jpeg?rowId=<%= rowId %>&field=<%= field %>&formId=<%=formId %>&version=<%= version %>"/></div></div>'
               text:  '<div class="controls" style="width: 35%"><a class="thumbnail" target="_blank" href="'+ acme.VARS.contextPath + '/reports/images/image.jpeg?rowId=<%= rowId %>&field=<%= field %>&formId=<%=formId %>&version=<%= version %>" title="<%= title %>"><img src="'+ acme.VARS.contextPath + '/reports/images/image.jpeg?rowId=<%= rowId %>&field=<%= field %>&formId=<%=formId %>&version=<%= version %>"/></a></div>'
            });

            var LOCATION_TEMPLATE = new EJS({
               text: '<div class="controls" style="width: 35%"><a class="thumbnail" target="_blank" href="<%= href %>" title="<%= title %>" ><img src="https://maps.googleapis.com/maps/api/staticmap?center=<%= location %>&zoom=15&size=600x300&markers=color:red%7C<%= location %>&key='+ STATIC_MAPS_API_KEY +'" /></a></div>'
            });

			var getActiveQueryId=function(){
				return activeQuery&&activeQuery.id;
			};
			
			var renderHeaderBasedOnSelection=function(){
				var title=acme.UTIL.decodeHTML(selectedForm.label);
				$('#' + FORM_LABEL_ID).text(title);
				var versionOptions=[],i=0;
				var publishedVersions=selectedForm.publishedVersions;
				if(publishedVersions&&publishedVersions.length>1){
					//only render the version dropdown if there is at least one version
					for(i=0;i<publishedVersions.length;i++){
						//FIXME i18n missing
						versionOptions[versionOptions.length]={label:"Version "+publishedVersions[i],value:publishedVersions[i]};
					}
					$("#dropdown_version").ACMEBootsrapSelect({list:versionOptions,initialSelectedValue:selectedVersion});
					
				}
				
			};
			var renderQueryFilters=function(p_formModel,p_Query){
				var filterContainer=$("#reportFilterSection");
				
				var modelMap=modelEditor.mapMFElements(p_formModel);
				queryModule.initSelectedFilter(filterContainer,modelMap,p_Query.filterOptions);
			};
			var loadFormModel=function(formId,version){
				var ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/reports/formModel.ajax';
				ajaxRequest.data={"formId":formId,"version":version};
				return $.ajax(ajaxRequest);
			};
			
			//if there is a selected query then this method will show or hide the edit/remove buttons
			var showOrHideQueryOptions=function(){
				if(activeQuery){
					//if there is an active query show the query bar
					$("#reportQueryTitleBox").show();
					//and the query edit buttons (TODO this depends on user permission)
					$('#queryButtons').show();
					
				}else{
					$('#queryButtons').hide();
				}
			};


            var getParameterValue = function(url, name){
                var vars = [], hash;
                var hashes = url.slice(url.indexOf('?') + 1).split('&');
                for(var i = 0; i < hashes.length; i++)
                {
                    hash = hashes[i].split('=');
                    vars.push(hash[0]);
                    vars[hash[0]] = hash[1];
                }
                return vars[name];
            };
            
            var getTransitions = function (originStateData) {
            	if (typeof originStateData === 'object' ) {
            		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
            		ajaxRequest.type = 'GET';
                    ajaxRequest.url += '/api/workflow/transitions/' + originStateData.id;
                    ajaxRequest.data = {formId: formModel.id, version: formModel.version};

                    return $.ajax(ajaxRequest);
            	} else {
            		return [];
            	}
            };
            
            var enableChangeState = function () {
            	$('#workflowForm').find(
                        'textarea,select,button#btn-change-state'
                        ).removeAttr('disabled');
            };
            
            var changeStateAction = function (event) {
            	var form = {};
            	
            	var beforeChange = function() {
                    $(event.target)
                        .parent()
                        .find('.btn')
                        .attr('disabled', true);
                }

                var afterChange = function() {
                    $(event.target)
                        .parent()
                        .find('.btn')
                        .removeAttr('disabled');
                }

                $('#workflowForm').find(
                    'textarea,input,select'
                ).each(function(index, o){
                        form[o.id] = o.value;
                });
                
                if (!form.docId) {
                    console.log('"docId" input element is missing from page');
                    return;
                }

                var data = {
                    formVersion: formModel.version,
                    formId: formModel.id,
                    workflowComment: form.comment
                };

                beforeChange();
                var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                ajaxRequest.url += '/api/workflow/documents/' + form.docId + '/states/' + form.newState;
                ajaxRequest.data = JSON.stringify(data);
                ajaxRequest.contentType = 'application/json';
                ajaxRequest.success = function (response) {
                    if (response.success) {
                        if (response.message) {
                        	acme_ui.HTML_BUILDER.notifySuccess('Cambio de Estado', response.message);
                        } else {
                        	acme_ui.HTML_BUILDER.notifySuccess('Cambio de Estado', 'Cambio de estado realizado con éxito');
                        }
                    } else {
                    	if (response.message) {
                        	acme_ui.HTML_BUILDER.notifyError('Cambio de Estado', response.message);
                        } else {
                        	acme_ui.HTML_BUILDER.notifyError('Cambio de Estado', 'Cambio de estado fallido');
                        }
                    }
                    $('#buttonSearch').click();
                };
                ajaxRequest.error = function (jqXHR, textStatus) {
                	if (jqXHR.status === 404) {
                		acme_ui.HTML_BUILDER.notifyError('Cambio de Estado', 'No existe el recurso que se intenta acceder');
                    }
                };
                ajaxRequest.complete = function (jqXHR, textStatus) {
                	afterChange();
                };
                
                $.ajax(ajaxRequest);

            };
            
            var renderWorkflowHistoryGrid = function (docId, formId, formVersion) {
            	$("#workflowHistoryGrid").jqGrid({
            	   	url: acme.VARS.contextPath + '/api/workflow/history/' + docId + '/' + formId + '/' + formVersion,
            		datatype: "json",
            	   	colNames:['Actualizado al','Estado anterior', 'Estado nuevo', 'Cambiado por','Comentario'],
            	   	colModel:[
            	   		{name:'updatedAt',index:'updatedAt', align:"left", sortable:false, formatter: jqgridmanager.formatter.dateTimeFormatter},
            	   		{name:'oldState',index:'oldState', sortable:false},
            	   		{name:'newState',index:'newState', sortable:false},
            	   		{name:'changedBy',index:'changedBy', sortable:false},
            	   		{name:'comment',index:'comment', align:"right", sortable:false}
            	   	],
            	    viewrecords: true,
            	    height: 'auto',
            	    caption:"Historial de Estados"
            	});
            };
            // In the form tab, displays
            // the 'form view' of the document
            var showFormView = function(rowId) {
                var elements = [], renderData = [], element,
                    elemVal, src, html, a, href, location, stateRowData;

                var rowData = jqgridmanager.gridUtils.getRowData(rowId);
                var workflow = selectedForm.workflow;

                // stores each row id, indexed by the order
                // in which we can see the row on the grid
                var dataIDs = jqgridmanager.gridUtils.getDataIDs();

                var visibleIndex = (jqgridmanager.gridUtils.getRowVisibleIndex(rowId)) - 1;

                // we need this to get each element label
                var map = modelEditor.elementMap(formModel);
                
                if (workflow) {
                	renderData.stateName = "";
                }

                for (var key in rowData) {
                    if (rowData.hasOwnProperty(key) && map.hasOwnProperty(key)) {
                        element = map[key];
                        if (rowData[key].indexOf("</a>") > -1) {
                            // only if we have link (image or location) for the element
                            // we construct the <img/> to be displayed in the form view.
                            if (element.proto.type === 'PHOTO' || element.proto.type === 'SIGNATURE') {
                                elemVal = IMG_TEMPLATE.render({
                                    rowId: rowId,
                                    field: key,
                                    formId: formModel.id,
                                    version: formModel.version,
                                    title: element.label
                                });
                            } else  if (element.proto.type === 'LOCATION') {
                                a = $(rowData[key]);
                                href = a.attr("href");
                                location = getParameterValue(href, 'q');
                                elemVal = LOCATION_TEMPLATE.render({
                                    href: href,
                                    title: a.text(),
                                    location: location
                                });
                            }
                        } else {
                            elemVal = '<label class="control-label-right">' + rowData[key]+ '</label>';
                        }
                        elements.push({
                            "label": element.label,
                            'value': elemVal
                        });
                    } else if (key === 'meta_state') {
                    	renderData.stateName = rowData[key]; 	
                    }
                }

                renderData.rowId = rowId;
                renderData.elements = elements;
                
                if (!workflow) {
                	html = FORM_TEMPLATE.render(renderData);
                }

                var prevRowId = (function() {
                    if (visibleIndex - 1 > -1) {
                        return dataIDs[visibleIndex - 1];
                    }
                    // we wrap around to the last rowId
                    return dataIDs[dataIDs.length - 1];
                })();
                var nextRowId = (function(){
                    if (dataIDs.length > visibleIndex + 1) {
                        return dataIDs[visibleIndex + 1];
                    }
                    // we wrap around to the first rowId
                    return dataIDs[0];
                })();

                // We store as hidden values the next and previous row id
                // in order to implement the navigation.
                /*$(".button-previous").parent().find("#prevRowId").val(prevRowId).end().parent().show();
                $(".button-next").parent().find("#nextRowId").val(nextRowId).end().parent().show();*/
                
                $(".button-previous").parent().parent().find("#prevRowId").val(prevRowId).end().parent().parent().show();
                $(".button-next").parent().parent().find("#nextRowId").val(nextRowId).end().parent().parent().show();
                
                $("#rowNum").text(jqgridmanager.gridUtils.getRowVisibleIndex(rowId));
                $("#rows").text(jqgridmanager.gridUtils.numberOfRows());
                $("#page").text(jqgridmanager.gridUtils.getPage());

                // populate and show the form tab
                if (!workflow) {
                	$("#formView #dataForm").html(html);
                    $("[href=#formView]").tab('show');
                } else {
                	renderData.docId = rowId;
                	stateRowData = jqgridmanager.gridUtils.getStateRowData(rowId);
                	transitionsPromise = getTransitions(stateRowData);
                	$.when(transitionsPromise).then(function(transitions){
                		renderData.transitions = transitions;
                		html = FORM_TEMPLATE_WITH_WORKFLOW.render(renderData);
                    	$("#formView #dataForm").html(html);
                    	renderWorkflowHistoryGrid(rowId, formModel.id, formModel.version);
                        $("[href=#formView]").tab('show');
                        if(transitions.length > 0) {
                        	enableChangeState();
                        }
                	});
                	
                }
                
            };

            // Resets the form tab with a default message
            // and changes focus to the grid tab.
            var resetFormView = function() {
                /*$(".button-previous").parent().parent().hide();
                $(".button-next").parent().parent().hide();*/
                $(".button-previous").parent().parent().parent().parent().hide();
                $(".button-next").parent().parent().parent().parent().hide();
                $("#formView #dataForm").html(i18n('web.home.reports.formView.emptyMsg'));
                $("[href=#gridView]").tab('show');
            };

            var refreshTable=function(query){
				
				var gridPromise = jqgridmanager.showGrid({
					'formModel' : formModel,
					//TODO WE ALREADY HAVE THE QUERY SO USE IT
					'query' : query,
                    'handlers': {
                        'dblClickRowHandler': showFormView, // cap-143
                        'pagingHandler': function() {
                        	resetFormView();
                        }
                    },
                    'workflow' : selectedForm.workflow
				});

                // cap-143
                resetFormView();

				return gridPromise;
			};
			// render the grid based on the user selection
			var renderGridBasedOnSelection = function() {
				var loadQueryPromise;
				
				if(activeQuery){
					loadQueryPromise=queryModule.loadQuery(activeQuery.id);
					
				}else{
					loadQueryPromise=null;
				}
				
				$.when(loadQueryPromise).then(function(queryDef){
					
					//It is very important to render the filter options first. Otherwise, the grid won't have filterOptions
					showOrHideQueryOptions();
					if(queryDef){
						//load the definition of the query, so if the user clicks on the search button we don't need to downlaod it again.
						activeQuery=queryDef;
						renderQueryFilters(formModel,queryDef);
					}
					refreshTable(queryDef);
					
				});
				return loadQueryPromise;
				
			};
			
			var initActions = function(formId) {
				
				$("#dropdown_version").on("itemChanged", function(event, data){
					var selectedVersion=data.value;
					renderFormReports(selectedFormId,selectedVersion);
				});

				var canCreateReports=acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.createReport");
				if(canCreateReports){
					$('#newQuery').click(function() {
						acme.LAUNCHER.launch(constants.LAUNCHER_IDS.reportQuery, {
							'formModel':formModel,
							'workflow' : selectedForm.workflow
						});
					});
					$('#newQuery').tooltip();
					
					$('#editQuery').click(function() {
						acme.LAUNCHER.launch(constants.LAUNCHER_IDS.reportQuery, {
							'formModel':formModel,
							'queryId' : getActiveQueryId(),
							'workflow' : selectedForm.workflow
						});
					});
					$('#editQuery').tooltip();
				}else{
					$('#newQuery').hide();
					$('#editQuery').hide();
				}
				
				$('#buttonDownloadCSV').click(function() {
					if (jqgridmanager.gridUtils.isEmpty()) {
						acme_ui.HTML_BUILDER.notifyError(i18n('web.home.reports.error.emptyReport.title'), i18n('web.home.reports.error.emptyReport.message'));
						return;
					}
					
					var filterContainer=$("#reportFilterSection");
					var filterOptions=queryModule.gatherFilterOptions(filterContainer);
					var queryId = getActiveQueryId();
					var postData={
						formId : formModel.id,
						version : formModel.version,
						timezoneOffset : new Date().getTimezoneOffset()
					};
					
					if (queryId) {
						postData.queryId = queryId;
					}
					
					postData.filterOptions=JSON.stringify(filterOptions);
					acme_ui.UTIL.downloadFile('/reports/downloadcsv.ajax', postData);
				});
				$('#buttonDownloadCSV').tooltip();
				
				$('#buttonDownloadXLS').click(function() {
					if (jqgridmanager.gridUtils.isEmpty()) {
						acme_ui.HTML_BUILDER.notifyError(i18n('web.home.reports.error.emptyReport.title'), i18n('web.home.reports.error.emptyReport.message'));
						return;
					}
					
					var filterContainer=$("#reportFilterSection");
					var filterOptions=queryModule.gatherFilterOptions(filterContainer);
					var queryId = getActiveQueryId();
					 
					var postData={
						formId : formModel.id,
						version : formModel.version,
						timezoneOffset : new Date().getTimezoneOffset()
					};
					
					if (queryId) {
						postData.queryId = queryId;
					}
					
					postData.filterOptions=JSON.stringify(filterOptions);
					acme_ui.UTIL.downloadFile('/reports/downloadxls.ajax', postData);
				});
				$('#buttonDownloadXLS').tooltip();
				
				$('#buttonDownloadPDF').click(function() {
					if (jqgridmanager.gridUtils.isEmpty()) {
						acme_ui.HTML_BUILDER.notifyError(i18n('web.home.reports.error.emptyReport.title'), i18n('web.home.reports.error.emptyReport.message'));
						return;
					}
					
					var filterContainer=$("#reportFilterSection");
					var filterOptions=queryModule.gatherFilterOptions(filterContainer);
					var queryId = getActiveQueryId();

					var postData={
						formId : formModel.id,
						version : formModel.version,
						timezoneOffset : new Date().getTimezoneOffset()
					};
					
					if (queryId) {
						postData.queryId = queryId;
					}
					
					postData.filterOptions=JSON.stringify(filterOptions);
					acme_ui.UTIL.downloadFile('/reports/downloadpdf.ajax', postData);
				});
				$('#buttonDownloadPDF').tooltip();
				
				$('#buttonSearch').click(function(){
					var buttonSearch=$(this);
					buttonSearch.attr("disabled","disabled");
					var p=refreshTable(activeQuery);
					$.when(p).then(function(){
						buttonSearch.removeAttr("disabled");						
					});
					
				});


                $('#buttonDownloadXLSWithPhotos').click(function() {
                    if (jqgridmanager.gridUtils.isEmpty()) {
                        acme_ui.HTML_BUILDER.notifyError(i18n('web.home.reports.error.emptyReport.title'), i18n('web.home.reports.error.emptyReport.message'));
                        return;
                    }

                    var filterContainer=$("#reportFilterSection");
                    var filterOptions=queryModule.gatherFilterOptions(filterContainer);
                    var queryId = getActiveQueryId();

                    var postData={
                        formId : formModel.id,
                        version : formModel.version,
                        timezoneOffset : new Date().getTimezoneOffset()
                    };

                    if (queryId) {
                        postData.queryId = queryId;
                    }

                    postData.filterOptions=JSON.stringify(filterOptions);
                    acme_ui.UTIL.downloadFile('/reports/downloadxlswithphotos.ajax', postData);
                });
                $('#buttonDownloadXLSWithPhotos').tooltip();

                // cap-143 Navigation buttons -------------------------//
                $("#formView").on("click", ".button-next", function(e){
                    e.stopPropagation();
                    e.preventDefault();
                    //var rowId = $(this).parent().find("#nextRowId").val();
                    var rowId = $(this).parent().parent().find("#nextRowId").val();
                    if (rowId) {
                        showFormView(rowId);
                    }
                });

                $("#formView").on("click", ".button-previous", function(e){
                    e.stopPropagation();
                    e.preventDefault();
                    //var rowId = $(this).parent().find("#prevRowId").val();
                    var rowId = $(this).parent().parent().find("#prevRowId").val();
                    if (rowId) {
                        showFormView(rowId);
                    }
                });
                // -------------------------------------------------//

                $("#buttonDownloadRowPDF").click(function() {
                    if (jqgridmanager.gridUtils.isEmpty()) {
                        acme_ui.HTML_BUILDER.notifyError(i18n('web.home.reports.error.emptyReport.title'), i18n('web.home.reports.error.emptyReport.message'));
                        return;
                    }

                    var queryId = getActiveQueryId();

                    var postData={
                        formId : formModel.id,
                        version : formModel.version,
                        timezoneOffset : new Date().getTimezoneOffset()
                    };

                    if (queryId) {
                        postData.queryId = queryId;
                    }

                    postData.rowId = $("#formView #rowId").val();


                    acme_ui.UTIL.downloadFile('/reports/downloadRowPdf.ajax', postData);
                });
                $('#buttonDownloadRowPDF').tooltip();
                
                $("#formView").on("click", "#btn-change-state", function(e){
                	changeStateAction(e);
                });
            };

			var drawQueryList = function(queryList) {
				var activeLabel = null;
				var selector = $('#queryList');
				var i=0;
				var liElement;
				var canDeleteReports;
				
				var highLightSelected = function(selectedElement) {
					$('#queryList').find('.active').removeClass('active');
					selectedElement.parent().addClass("active");
					$("#queryTitle").html(activeQuery.name);
				};
				
				var onClick = function() {
					activeQuery = $(this).data('queryInfo');
					highLightSelected($(this).parent());
					renderGridBasedOnSelection();				
				};
				
				var onClickRemove = function() {
					var queryId = $(this).data('queryId');
					
					var onSuccess = function() {
						var li = $(this).parents('li');
						$(li).remove();
					};
					
					queryModule.deleteQuery(queryId, onSuccess);
				};
				
				selector.find(".divider").nextAll().remove();
				
				for (i = 0; i < queryList.length; i++) {
					liElement = '<li><div><a href="javascript:void(0)" class="applyQuery"  >' + queryList[i].name + '</a>';
					canDeleteReports = acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(selectedFormId, "form.deleteReport");
					
					if (canDeleteReports) {
						liElement += '<i class="icon-remove pull-right removeQuery"></i>' + '</div></li>';
					}
					
					liElement = $(liElement);
					$(liElement).find("a").click(onClick).data('queryInfo', queryList[i]);
					$(liElement).find("i").click(onClickRemove).data('queryId', queryList[i].id);
					
					selector.append(liElement);
					if(activeQuery && queryList[i].id === activeQuery.id){
						//highlight the selected element
						highLightSelected($(liElement).find("span"));
					}
				}
				
				
			};

			//make an ajax request to load the available queries and render them
			//a promise is returned that will be resolved when the query list was loaded
			var loadQueryList = function(pFormId,pVersion) {
				
				var ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/reports/querys/listAllQueries.ajax';
				ajaxRequest.data={
						formId : pFormId,
						version : pVersion
					};
				ajaxRequest.loadingSectionId = 'queryContainer';
				return $.ajax(ajaxRequest);


			};
			var noPublishedVersion=function(){
				$('#form_content').hide();
				$('#no_content').show();
			};
			var getJsonFromUrl = function(url) {
				var result = {};
				url.split("&").forEach(function(part) {
					var item = part.split("=");
					result[item[0]] = decodeURIComponent(item[1]);
				});
				return result;
			};
			var initImageDialog = function() {
				var downloadText = i18n('web.generic.download');
				$('#imageDialog').dialog({
					modal : true,
					autoOpen : false,
					width : 800,
					height : 600,
					buttons : [
						{
						  "text": downloadText,
						  "click": function() {
							  // CAP-162
							  var imageSrcQuery = $(this).find("#image").eq(0).attr('src').split('?')[1];
							  var imageData = getJsonFromUrl(imageSrcQuery);
							  acme_ui.UTIL.downloadFile('/reports/images/image.jpeg', imageData);
						  }
						},
						{
					      "text": "Ok",
						  "click": function() {
							  $(this).dialog('close');
						  }
						},
					],
					open: function() {
						var buttons = $(this).closest(".ui-dialog").find(':button');
						// Remove focus on 'Download' button
						buttons.eq(0).blur().button({
						  icons: {
		                    primary: 'ui-icon-disk'
		                  }
		                });
						// Put focus on Ok
	                    buttons.eq(1).focus().button({
			              icons: {
				            primary: 'ui-icon-check'
				          }
				        });;
	                }
				});
				
				$('#imageLoading').hide();
				$('#image').hide();
				
				$('#image').load(function(){
					var imageHeight = $('#image').height();
					var imageWidth = $('#image').width();
					
					var dialogHeight = $('#imageDialog').height();
					var dialogWidth = $('#imageDialog').width();
					//not sure if this first condition would be ever true
					if (imageWidth > dialogWidth && imageHeight > dialogHeight){
						var widthRatio = imageWidth / dialogWidth;
						var heightRatio = imageHeight / dialogHeight;
						if(widthRatio >= heightRatio){
							$('#image').css('width', '100%');
							$('#image').css('height', "");
						} else {
							$('#image').css('height', '100%');
							$('#image').css('width', "");
						}
					} else if(imageWidth > dialogWidth){
						$('#image').css('width', '100%');
						$('#image').css('height', "");
					} else if(imageHeight > dialogHeight){
						$('#image').css('height', '100%');
						$('#image').css('width', "");
					} 
					$('#imageLoading').hide();
					$('#image').show();
					acme.LOG.debug('image loaded');
				});
			};
			//Render the reports of a form
			//p_selectedVersion can be null and the last version is assumed
			//p_selectedQuery can be null and the default query is assumed (or all columns if there is no custom query)
			var renderFormReports=function(formId,p_selectedVersion,p_selectedQuery){
				selectedVersion=null;activeQuery=null;//unselect previous selection if any
				var isFormLoaded = formService.getFormLastVersion(formId);
				
				// render the query list
				
				$.when(isFormLoaded).then(function(form) {
					
					if(!form.publishedVersions||form.publishedVersions.length<1){
						//Can't display anything if there is no published version
						noPublishedVersion();
						return;
					}
					selectedForm = form;
					selectedFormId=form.id;
					if(form.versionPublished){
						// by default should the published version of the
						// form if any
						selectedVersion = form.versionPublished;
					}else{
						//if there is no publish version currently, then show the latest 
						selectedVersion = form.publishedVersions[0];
					}
					//In order to downlaod the adequate list of queries we need to know the version of the form
					//this is going to be set to the latest version on "loadForm" and we have here the chance to overwrite it.
					if(p_selectedVersion){
						//init with the selected version of the form
						selectedVersion=p_selectedVersion;
					}
					var formModelPromise=loadFormModel(form.id,selectedVersion);
					var queryPromise = loadQueryList(selectedFormId,selectedVersion);
					
					$.when(queryPromise,formModelPromise).then(function(objQuery,objModel) {
						var queryList=objQuery[0];
						//store the formModel for later reference
						formModel=objModel[0];
						var i=0;
						//Prepare the filter section
						var filterContainer=$("#reportFilterSection");
						var initFilterSectionPromise=queryModule.initFilterSection(formModel,filterContainer,selectedForm.workflow);
						if(p_selectedQuery){
							activeQuery=p_selectedQuery;
						}else{
							for ( i = 0; i < queryList.length; i++) {
								if(queryList[i].defaultQuery){
									activeQuery=queryList[i];
									break;
								}
							}
						}
						renderHeaderBasedOnSelection();
						drawQueryList(queryList);
						renderGridBasedOnSelection();
					});

				});
			};
			
			pkg.back=function(){
				
			};
			
			pkg.stop=function(){
				queryModule.destroy();
				$('#imageDialog').dialog('destroy').remove();
				$('#buttonDownloadCSV').tooltip('hide');
				$('#editQuery').tooltip('hide');
				$('#newQuery').tooltip('hide');
				
			};
			// if version is undefined, last version published is selected
			pkg.start = function(options) {
				if (options.formId) {
					var canViewReports=acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(options.formId,"form.viewReport");
					if(canViewReports){
						$.when(i18nPromise).then(function(){
							initImageDialog();
						});
						queryModule.init();
						//This start page doesn't need to return a promise because it shows the progress by itself
						var promise=renderFormReports(options.formId,options.selectedVersion,options.selectedQuery);
						initActions(options.formId);
						return promise;
					}		 
				}
			};

			return pkg;
		});