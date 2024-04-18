define(["jquery", "acme","home/reports/query-amd","editor/model"],  function($, acme,queryModule,model) {

	var pkg = {};
	
	//loads the i18n labels of this module and the labels of the meta columns
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.loading',
	                                          'web.data.show.noFields',
	                                          'web.data.show.showImage',
	                                          'web.data.show.noImage',
	                                          'web.data.show.showSignature',
	                                          'web.data.show.noSignature'].concat(queryModule.metaColumni18nKeys));
	
	var I18N = acme.I18N_MANAGER;
	var I18Nmessage = acme.I18N_MANAGER.getMessage;
	
	var filterSectionId="reportFilterSection";
	var jsonReader =  { 
		root: "data", 
		repeatitems: false,
		page: "pageNumber",
		total: "totalPages",
		records: "totalCount",
		id: "id"
	};
	
	var stateRowsData = {};
	
	var globalFormModel;
	
	// Custom Formatters -------
	var imageFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var name = options.colModel.name;
		if(cellvalue){
			return '<a href="' + name + '" data="' + rowId + '" class="imageLink glyphicons picture"><i></i>'+I18Nmessage('web.data.show.showImage')+'</a>';
		} else {
			return I18Nmessage('web.data.show.noImage');
		}
	};
	
	var signatureFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var name = options.colModel.name;
		if(cellvalue){
			return '<a href="' + name + '" data="' + rowId + '" class="imageLink"><i></i>'+I18Nmessage('web.data.show.showSignature')+'</a>';
		} else {
			return I18Nmessage('web.data.show.noSignature');
		}
	};
	
	var checkboxFormatter = function(cellvalue, options, rowObject){
		var rowId = options.rowId;
		var name = options.colModel.name;
		if(cellvalue){
			return '<div style="text-align:center"><i class="icon-ok"></i></div>';
		} else {
			return '';
		}
	};

    var getRowData = function(rowId){
        return $("#dataGrid").jqGrid('getRowData', rowId);
    };
    
    var getStateRowData = function(rowId){
        return stateRowsData[rowId];
    };

    var getDataIDs = function() {
      return $("#dataGrid").jqGrid('getDataIDs');
    };

    var getRowVisibleIndex = function(rowId){
      return $("#dataGrid").jqGrid('getInd', rowId);
    };

	var numberOfRows = function() {
		return $('#dataGrid').jqGrid('getGridParam', 'reccount');
	};
	
	var isEmpty = function() {
		return numberOfRows() == 0;
	};
	
	var getPage = function() {
		return $('#dataGrid').jqGrid('getGridParam', 'page');
	};
	
	var locationFormatter = function(cellvalue, options, rowObject){
		if(cellvalue){
			var loc=cellvalue.latitude+","+cellvalue.longitude;
			return '<a target="_blank" href="http://maps.google.com?q=' + loc +'" class="glyphicons globe" ><i></i> Google Maps</a>';
			
		}
		return "not available"; // FIXME i18n
	};
	
	var dateTimeFormatter=function(cellValue, options, rowObject){
		 if (cellValue) {
	        var d = new Date();
	        d.setISO8601(cellValue);
	        var op = $.extend({}, $.jgrid.formatter.date, options); 
	        //http://www.trirand.com/jqgridwiki/doku.php?id=wiki:predefined_formatter
	        var formattedDate = $.fmatter.util.DateFormat('', d, 'd/m/Y H:i:s', op);
	        return formattedDate;
	     } else {
	        return '';
	     }
	};
	var dateFormatter=function(cellValue, options, rowObject){
		 if(cellValue) {
	        	var d=new Date();
	        	d.setISO8601(cellValue);
	        	//http://www.trirand.com/jqgridwiki/doku.php?id=wiki:predefined_formatter
	            return $.fmatter.util.DateFormat(
	                '', 
	                d, 
	                'd/m/Y', 
	                $.extend({}, $.jgrid.formatter.date, options)
	            );
	        } else {
	            return '';
	        }
	};
	var timeFormatter=function(cellValue, options, rowObject){
		if(cellValue) {
        	var d=new Date();
        	d.setISO8601(cellValue);
        	//http://www.trirand.com/jqgridwiki/doku.php?id=wiki:predefined_formatter
            return $.fmatter.util.DateFormat(
                '', 
                d, 
                'ShortTime', 
                $.extend({}, $.jgrid.formatter.date, options)
            );
        } else {
            return '';
        }
	};
	
	var stateFormatter=function(cellValue, options, rowObject){
		stateRowsData[options.rowId] = cellValue;
		if (cellValue) {
			return cellValue.name;
		} else {
			return '';
		}
	};
	
	//This method return a string representation of the java Object (py.com.sodep.mobileforms.api.services.metadata.forms.elements.Options)
	//This are the rules for the format:
	//If the value match the label, then just show one of them
	//If the label is empty or null (do not show it)
	//Otherwise, show the format label (value)
	var formatSelectOption=function(opt){
		if(opt.label){
			if(opt.value===opt.label){
				return opt.value;
			}
			return opt.label +"( "+opt.value+" )";
		}
		return opt.value;
	};
	var selectFormatter=function(cellValue, options, rowObject){
		var i=0;
		var str='';
		if(cellValue) {
			for(i=0;i<cellValue.length;i++){
				str+=formatSelectOption(cellValue[i]);
				if(i<cellValue.length-1){
					str+=", ";
				}
			}
		}
		return str;
	};
	
	var showImagePopup = function(id, field) {
		var currentSrc = $('#image').attr("src");
		var src = acme.VARS.contextPath + "/reports/images/image.jpeg?rowId=" + id + "&field=" + field + '&formId=' + globalFormModel.id + '&version=' + globalFormModel.version;
		if(currentSrc === undefined || currentSrc != src){
			$('#image').hide();
			$('#image').css("height", "");
			$('#image').css("width", "");
			$('#image').attr("src", src);
			$('#imageLoading').show();
		} else {
			$('#imageLoading').hide();
			$('#image').show();
		}
		$('#imageDialog').dialog('open');
		return false;
	};
	
	var gridComplete = function(){
		$('a.imageLink').click(function(){
			var data = $(this).attr('data');
			var href = $(this).attr('href');
			showImagePopup(data, href);
			return false;
		});
	};
	
	
	
	
	var initTable=function(formModel,query, handlers, workflow){
		
		var dfd = $.Deferred();
		globalFormModel=formModel;
		
		$('#dataGrid').jqGrid('GridUnload');
		
		var colNames,i=0,element;
		var elementInstanceMap=model.elementMap(formModel);
		//The second parameters is false in order to avoid including parameters that are only for output purpose (such as headline)
		var allElementsArray=model.listElements(formModel,false);
		var selectedColumNames=[];
		var columnModel=[];
		var postData = {'formId':formModel.id, 'version':formModel.version};
		
		var filterSectionDiv=$("#"+filterSectionId);
		var filterOptions=queryModule.gatherFilterOptions(filterSectionDiv);
		postData.filterOptions=JSON.stringify(filterOptions);

		if(query){
			//if there is a query then show the columns of the query
			//the object query contains an array that has the instanceIDs of the "viewable" elements
			for(i=0;i<query.selectedTableColumns.length;i++){
				var selectedTableColumn = query.selectedTableColumns[i];
				if(selectedTableColumn.startsWith(queryModule.META_PREFIX)){
					if (selectedTableColumn !== 'meta_stateId') {
						columnModel[columnModel.length]={'id':selectedTableColumn,'name':selectedTableColumn};
					} else {
						columnModel[columnModel.length]={'id':'meta_state','name':'meta_state'};
					}
					//for the name use the i18n values stored on the query module
					selectedColumNames[selectedColumNames.length]=I18Nmessage(queryModule.mapMetaColumnToi18n(selectedTableColumn));
				}else{
					element=elementInstanceMap[query.selectedTableColumns[i]];
					selectedColumNames[selectedColumNames.length]=element.label;
					columnModel[columnModel.length]={'id':element.id,'name':element.id,'index':element.id};					
				}
				
			}
			postData['queryId']=query.id;
		}else{
			//display all elements
			//allEle
			for(i=0;i<allElementsArray.length;i++){
				element=allElementsArray[i];
				selectedColumNames[selectedColumNames.length]=element.label;
				columnModel[columnModel.length]={'id':element.id,'name':element.id,'index':element.id};				
			}
			
			/* 
			 * The two meta columns were added after:
			 * #2281 Reports. As a user I would like to see "received at" and "user" column by default
			 * http://gohan.sodep.com.py/redmine/issues/2281  
			 */
			
			columnModel[columnModel.length] = {'id': 'meta_mail', 'name': 'meta_mail'};
			selectedColumNames[selectedColumNames.length] = I18Nmessage(queryModule.mapMetaColumnToi18n('meta_mail'));;
			
			columnModel[columnModel.length] = {'id': 'meta_receivedAt', 'name': 'meta_receivedAt'};
			selectedColumNames[selectedColumNames.length] = I18Nmessage(queryModule.mapMetaColumnToi18n('meta_receivedAt'));;
			
			columnModel[columnModel.length] = {'id': 'meta_savedAt', 'name': 'meta_savedAt'};
			selectedColumNames[selectedColumNames.length] = I18Nmessage(queryModule.mapMetaColumnToi18n('meta_savedAt'));;
			
			// cap-376
			if (workflow) {
				columnModel[columnModel.length] = {'id': 'meta_state', 'name': 'meta_state'};
				selectedColumNames[selectedColumNames.length] = I18Nmessage(queryModule.mapMetaColumnToi18n('meta_stateId'));;
			}
			
		}
		
		var metaCol;
		//assign custom formatter based on the data type
		//disable sorting where it doesn't makes sense (picture, location)
		for(i=0;i<columnModel.length;i++){
			
			if(columnModel[i].id.startsWith(queryModule.META_PREFIX)){
				metaCol=columnModel[i].id.substr(queryModule.META_PREFIX.length);
				if(metaCol==='receivedAt' || metaCol === 'savedAt'){
					columnModel[i].formatter=dateTimeFormatter;
				}else if (metaCol==='user'){
					
				}else if (metaCol==='location'){
					columnModel[i].formatter=locationFormatter;
					columnModel[i].sortable=false;
				}else if (metaCol==='state'){
					columnModel[i].formatter=stateFormatter;
					columnModel[i].sortable=false;
				}
			}else{
				element=elementInstanceMap[columnModel[i].id];
				if(element.proto.type==='INPUT'){
					//TEXT("text"), DATE("date"), TIME("time"), DATETIME("datetime"), PASSWORD("password"), INTEGER("integer"), DECIMAL(
					//"decimal"), TEXTAREA("textarea");
					if(element.proto.subtype==='TEXT'){
						
					}else if(element.proto.subtype==='DATE'){
						columnModel[i].formatter=dateFormatter;
					}else if(element.proto.subtype==='TIME'){
						columnModel[i].formatter=timeFormatter;
					}else if(element.proto.subtype==='DATETIME'){
						columnModel[i].formatter=dateTimeFormatter;
					}else if(element.proto.subtype==='PASSWORD'){
						
					}else if(element.proto.subtype==='INTEGER'){
						
					}else if(element.proto.subtype==='DECIMAL'){
						
					}else if(element.proto.subtype==='TEXTAREA'){
						
					}
				}else if(element.proto.type==='PHOTO'){
					columnModel[i].formatter=imageFormatter;
					columnModel[i].sortable=false;
				}else if(element.proto.type==='LOCATION'){
					columnModel[i].formatter=locationFormatter;
					columnModel[i].sortable=false;
				}else if(element.proto.type==='SELECT'){
					columnModel[i].formatter=selectFormatter;
				}else if(element.proto.type==='CHECKBOX'){
					columnModel[i].formatter=checkboxFormatter;
				}else if(element.proto.type==='SIGNATURE'){
					columnModel[i].formatter=signatureFormatter;
				}
			}
			
		}
		
		/* 
		 * If there's a query and it has sorting columns use those
		 * else use meta_receivedAt as default
		 */
		var sortColumn = 'meta_receivedAt';
		var sortOrder = 'desc';
		var hasSortingColumns = false;
		var hasDefaultSortingColumn = false;
		
		if (query && query.selectedSortingColumns.length > 0) {
			sortColumn = '';
			sortOrder = '';
			hasSortingColumns = !hasSortingColumns;
		}
		
		for (i = 0; i < columnModel.length; i++) {
			if (columnModel[i].id === sortColumn) {
				hasDefaultSortingColumn = true;
				break;
			}
		}
		
		var leGrid = $('#dataGrid');
		leGrid.jqGrid('GridUnload');
		leGrid.jqGrid({
			datatype : 'json',
			jsonReader : jsonReader,
			sortable : true,
			colNames : selectedColumNames,
			colModel : columnModel,
			viewrecords: true,
			mtype: 'POST',
			autowidth : true,
            shrinkToFit : false,
			width : '100%',
			height : '100%',
			url : acme.VARS.contextPath +  '/reports/read.ajax',
			postData: postData,
			pager: '#dataPager',
			rowNum : 10,
			rowList : [10, 20, 30],
			sortname: sortColumn,
			sortorder: sortOrder,
            ondblClickRow: (handlers && handlers.dblClickRowHandler) || null,
            onPaging: (handlers && handlers.pagingHandler) || null,
			gridComplete : function(){
				gridComplete();
				dfd.resolve();
				
			}
		}).navGrid('#dataPager',
				{del:false, add:false, edit:false, search:false}, 
				{}, //  default settings for edit
				{}, //  default settings for add
				{},  // delete instead that del:false we need this
				{}, // search options
				{} /* view parameters*/);

		if (hasSortingColumns || !hasDefaultSortingColumn) {
			// hide the sort order arrow in the column title
			$('.s-ico').hide();
		}
		
		return dfd.promise();
	};
	
	
	var initImageLoad = function() {
		$('#image').load(function(){
			$('#imageLoading').hide();
			$('#image').show();
		});
	};
	
	pkg.showGrid = function(options) {
		if(!options.formModel){
			throw 'the option formModel, is required to start';
		}
		
		var dfd = $.Deferred();
		require(["order!jqgrid/grid.locale-" + I18N.currentLanguage(), "order!jqgrid/jqGrid"], function(jqLocale, jqGrid){
			
			var p=initTable(options.formModel,options.query, options.handlers, options.workflow);
			$.when(p).then(function(){
				dfd.resolve();
			});
			
			
		});
		return dfd.promise();
	};
	
	pkg.formatter = {};
	pkg.formatter.dateFormatter = dateFormatter;
	pkg.formatter.dateTimeFormatter = dateTimeFormatter;
	
	pkg.gridUtils = {};
	pkg.gridUtils.isEmpty = isEmpty;
	pkg.gridUtils.numberOfRows = numberOfRows;

    pkg.gridUtils.getDataIDs = getDataIDs;
    pkg.gridUtils.getRowData = getRowData;
    pkg.gridUtils.getRowVisibleIndex = getRowVisibleIndex;
    pkg.gridUtils.getPage = getPage;
    pkg.gridUtils.getStateRowData = getStateRowData;

    return pkg;
});
