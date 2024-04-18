define(["jquery", "acme", "jquery-ui", "acme-ui"], function($, acme, $ui, acme_ui) {
	
	var pkg = {};
	var uploadedFilename = null;
	var DFL_TEXT_QUALIFIER = "all";
	var currentLines = null;
	// #3078
	var MAX_COLUMNS = 30;
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.ok',
	                                          'web.generic.error',
	                                          'web.generic.unexpectedException',
	                                          'web.dataimport.lookuptable.maxColumns']);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var controlJQIDs = {
		"tab" : "#dataImport_delimiter_tab",
		"semicolon" : "#dataImport_delimiter_semicolon",
		"space" : "#dataImport_delimiter_space",
		"comma" : "#dataImport_delimiter_comma",
		"qualifier" : "#dataImport_textQualifier",
		"header" : "#dataImport_columnheader",
		"colon" : "#dataImport_delimiter_colon"
	};
	
	var computeDelimiter = function() {
		var delimitersList = new Array() ;
		if($(controlJQIDs["tab"]).prop("checked")){
			delimitersList.push("\t");
		}
		if($(controlJQIDs["comma"]).prop("checked")){
			delimitersList.push(",");
		}
		if($(controlJQIDs["semicolon"]).prop("checked")){
			delimitersList.push(";");
		}
		if($(controlJQIDs["space"]).prop("checked")){
			delimitersList.push(" ");
		}
		if($(controlJQIDs["colon"]).prop("checked")){
			delimitersList.push(":");
		}
		if(delimitersList.length == 0){
			return "";
		} else if (delimitersList.length == 1)  {
			return delimitersList[0];
		} else {
			var i = 1;
			var regexStr = delimitersList[0];
			for(; i<delimitersList.length; i++) {
				regexStr += "|" + delimitersList[i];
			}
			return new RegExp(regexStr);
		}
		
	};
	
	var trimStringUsingQualifier = function(str, qualifier) {
		if(str){					
			if(qualifier == "none"){
				return str;
			} else if(qualifier == "quote"){
				return str.replace(/^'/,"").replace(/'$/, "");
			} else if(qualifier == "doublequote"){
				return str.replace(/^\"/,"").replace(/\"$/, "");
			} else {
				return str.replace(/^'/,"").replace(/'$/, "").replace(/^\"/,"").replace(/\"$/, "");
			}			
		} else {
			return "";
		}
	};
	
	var redrawPreview = function() {		
		var previewDiv = $("#dataImport_previewDiv");
		if(currentLines){ 
			// Read options
			var firstLineAsHeader = $(controlJQIDs["header"]).prop("checked");	
			var qualifier = $(controlJQIDs["qualifier"]).val();
			var computedDelimiter = computeDelimiter();
			
			var table = $("<table>");
			table.addClass("dynamicPreviewTable");
			var columns = 0;
			$.each(currentLines, function(i, line) {
				var row = $("<tr>");
				
				var parts = line.split(computedDelimiter);
				if (parts && parts.length > 0) {
					$.each(parts, function(j, element) {
						element = trimStringUsingQualifier(element, qualifier);
						if(i == 0){
							if(firstLineAsHeader) {
								row.append("<th>" + element + "</th>");
							}
							if(j == 0){
								columns = parts.length;
							}
						} else {
							row.append("<td>" + element + "</td>");
						}	
					});
					table.append(row);
					
				}			
			});
			previewDiv.empty();
			if(columns > MAX_COLUMNS){
				previewDiv.append('<span class="label label-warning" style="margin-bottom : 2px">' + I18N('web.dataimport.lookuptable.maxColumns', [MAX_COLUMNS]) +'</span><br/>');
			}
			previewDiv.append(table);
			
			previewDiv.show("slow");
		}
	};
	
	var setCurrentLines = function(lines) {
		currentLines = lines;
	};
	
	var showParsingControls = function(response) {
		$("#dataImport_parseCustomization").show("slow");
		uploadedFilename = response.content.filename;
		setCurrentLines(response.content.previewLines);		
		redrawPreview();		
	};
	
	var initImportOptions = function() {
		$(controlJQIDs["tab"]).prop("checked", false);
		$(controlJQIDs["semicolon"]).prop("checked", false);
		$(controlJQIDs["colon"]).prop("checked", false);
		$(controlJQIDs["space"]).prop("checked", false);
		$(controlJQIDs["comma"]).prop("checked", true);
		$(controlJQIDs["qualifier"]).val(DFL_TEXT_QUALIFIER);
		$("#dataImport_lookuptable").val("");
		$(controlJQIDs["header"]).prop("checked", true);
		$.each(controlJQIDs, function(key, value){
			$(value).change(function(){
				redrawPreview();
			});
		});
	};
	
	
	var  buildJSONData = function(){
		var data = {};
		data.lookupTableName = $("#dataImport_lookuptable").val();
		data.useTab = $("#dataImport_delimiter_tab").prop("checked");
		data.useColon = $("#dataImport_delimiter_colon").prop("checked");
		data.useSemicolon = $("#dataImport_delimiter_semicolon").prop("checked");
		data.useComma = $("#dataImport_delimiter_comma").prop("checked");
		data.useSpace = $("#dataImport_delimiter_space").prop("checked");
		data.textQualifier= $("#dataImport_textQualifier").val();
		data.useFirstRowAsHeader =  $("#dataImport_columnheader").prop("checked");
		data.filename = uploadedFilename;
		return JSON.stringify(data);
	};
	
	var showError = function(title, message){
		$.when(i18nPromise).then(function(){
			$('<div></div>').html(message).dialog({
				buttons : [ {
					text : I18N('web.generic.ok'),
					click : function(){
						$(this).dialog('close');
					}
				}],
				modal : true,
				width: "320",
				height: "180",
				title : title,
				closeOnEscape : false, 
				close : function(){
					$(this).dialog('destroy');
					$(this).remove();
				}
			}).dialog('open');
		});
	};
	
	var initIFrameForUploading = function() {
		$('#uploadIframe').load(function() {
			var iframe = window.document.getElementById('uploadIframe');
			// http://stackoverflow.com/questions/1088544/javascript-get-element-from-within-an-iframe
			var document = iframe.contentDocument || iframe.contentWindow.document;
			var responseStr = $(document).find('body').html();
			if(responseStr){
				try {					
					var response = JSON.parse(responseStr);
					if(response.success===true){
						//alert = acme_ui.HTML_BUILDER.alertSuccess;
						showParsingControls(response);
					} else {
						showError(response.title, response.message);
					}												
				} catch (err) {
					$.when(i18nPromise).then(function(){
						showError(I18N('web.generic.error'), I18N('web.generic.unexpectedException'));
					});
				}
			}
			//$('#mf_content_progress').hide();
		});
	};
	
	var initActions = function() {
		// 
		$("#csv").change( function() {
			// 0. Show upload icon 
			//$('#mf_content_progress').show();
			// 1. do ajax request
			$("#dataImport_uploadForm").trigger('submit');
		});
		
		$("#dataImport_importButton").click(function(){
			var jsonData = buildJSONData();
			
//			if(!jsonData.valid){
//				acme_ui.HTML_BUILDER.alertError("message", "Invalid options", "Invalid options");
//			} else {
				var ajaxRequest = acme.AJAX_FACTORY.newInstance();
				ajaxRequest.url += '/data/import/csv.mob';
				ajaxRequest.type = 'POST';
				ajaxRequest.data = jsonData;
				ajaxRequest.contentType = 'application/json; charset=utf-8';
				ajaxRequest.loadingSectionId = 'page_width';
				ajaxRequest.success = function(response, postdata) {
					if (response.success) {
						$("#dataImport_parseCustomization").hide();
						acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
						initImportOptions();
						$("#csv").val("");
					} else {
						acme_ui.HTML_BUILDER.notifyError(response.title, response.message);
					}
				};			
				$.ajax(ajaxRequest);
//			}			
		});
	};
	
	pkg.start = function() {		
		// Initial state
		initIFrameForUploading();
		initImportOptions();
		initActions();
	};
	
	return pkg;
});