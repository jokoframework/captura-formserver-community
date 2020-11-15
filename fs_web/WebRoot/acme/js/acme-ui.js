define([ "jquery", "require", "acme", "jquery-ui", "notify/ui.notify" ], function($,
		require, acme) {

	var pkg = {};
	
	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.ok','web.generic.cancel']);
	var i18n = acme.I18N_MANAGER.getMessage;
	
	var dialog = function(html, params){
		params = params || {};
		$.when(i18nPromise).then(function(){	
			var defaultButtons = 
			[{
				text : i18n('web.generic.ok'),
	        	click : function(){
	        		$(this).dialog('close');
	        	}
			}];
			
			$('<div></div>').html(html).dialog({
				close : function(){
					$(this).dialog('destroy');
					$(this).remove();
				},
				autoOpen : false,
				closeOnEscape : false,
				modal : true,
				height : params.height ? params.height : "auto",
				width : params.width ? params.width : "400",
				title : params.title ? params.title : "",
				buttons : params.buttons ? params.buttons:defaultButtons
			}).dialog('open');
		});
		return i18nPromise;
	};
	
	var confirmation=function(p_Title,message,doAction,cancelAction){
		$('<div></div>').html(message)
		.dialog( { 	
			buttons : [
		  	           {	
		  	        	   id : "confirmation_ok_button",
		  	        	   text : i18n('web.generic.ok'),
		  	        	   click : function(){
		  	        		  if($.isFunction(doAction)){
		  	        			 doAction();
		  	        		  }
					  	      $(this).dialog("close");
		  	        	   }
		  	           },
		  	           {
		  	        	   id : "confirmation_cancel_button",
		  	        	   text : i18n('web.generic.cancel'),
		  	        	   click : function(){
		  	        		 if($.isFunction(cancelAction)){
		  	        			 cancelAction();
		  	        		 }
		  	        		 $(this).dialog("close");
		  	        	   }
		  	           }
		  	        ],
			autopen : false,
			modal : true,
			width: "320",
			height: "180",
			closeOnEscape: false,
			title : p_Title,
			close : function() {
				$(this).dialog('destroy');
				$(this).remove();
			}
		}).dialog('open');
	};
	//This is an utility method that can be used to produce selectable icons for our jqgrid tables
	//The method receives:
	//* ID:The id of the element that can be used later on the callback function to get the row on which the use has clicked on
	//* icon: One of the valid icons on the glyphicon set
	//* classForFurtherSelect: a CSS class that can later be used to hook callback to this function .For example: 
//	#("."+classForFurtherSelect).click(function(){
//	var entityId = $(this).attr('rowid');
	//Do something with the id
//	});
	//*caption: The tooltip for the element
	//* authFunc: If this is a function and return true the element will be enable. Otherwise, the element will just be disable and the classForFurtherSelect won't be applied. 
	//This way the user can simple use the same selector to apply the callback function to all elements and it will only be applied to the authorized elements
	var iconForTable=function(id,icon,classForFurtherSelect,caption,authFunc){
		var iconOnTable='iconOnTablesDisabled';
		var classforDelete='';
		var authorized=false;
		var classSelector='';
		if(authFunc&&$.isFunction(authFunc)){
			authorized=authFunc();
		}
		if(authorized) {
			var rowId = id;
			classSelector=classForFurtherSelect;
			iconOnTable='iconOnTablesEnable';
		}
		return '<div class="'+classSelector+' iconOnTable " rowid="' + rowId + '" ><a href="javascript:void(0)" class="glyphicons '+iconOnTable+' '+icon+' " title="' + caption + '"></a></div>';
	};
	var errorMessage=function(p_Title,p_message){
		$('<div></div>').html(p_message).dialog({
			title : p_Title,
			resizable : false,
			autoOpen : true,
			width : 400,
			height : 180,
			modal : true,
			buttons : [ {
				text : 'reload',
				click : function() {
					acme.UTIL.reloadWorld();
				}
			} ],
			close : function() {
				acme.UTIL.reloadWorld();
			}
		});
	};
	function HtmlBuilder(){
		var that={};
		that.createSelectOption=function(value,label,selected){
			var selectStr="";
			if(selected){
				selectStr='selected="selected"';
			};
			return '<option value="'+value+'" '+selectStr+' >'+label+'</option>';
		};
		
		that.createCheckButton=function(id,label,checked){
			var checkedStr='';
			if(checked){
				checkedStr='checked="checked"';
			};
			return '<label class="checkbox"><input type="checkbox" id="'+ id +'" ' + checkedStr + '>' + label +'</input></label>';
		};
		
		//Receives a list of label/value pairs and creates a select 
		that.createSelect=function(id,list,selectedValue){
			var i=0;
			var html='<select id="'+id+'" >';
			for(i=0;i<list.length;i++){
				html+=that.createSelectOption(list[i].value,list[i].label,selectedValue===list[i].value);
			}
			html+='</select>';
			return $(html);
		};
		that.drawSelectOptions = function(select, list, initialLabel, initialValue, label, value) {
			var i;
			
			if(initialLabel) {
				select.append(that.createSelectOption(initialValue, initialLabel));
			}
			
			for(i = 0; i < list.length; i++) {
				if(label && value) {
					select.append(that.createSelectOption(list[i][value], list[i][label]));
				} else if(typeof list[i].id !== 'undefined' && typeof list[i].label !== 'undefined') {
					select.append(that.createSelectOption(list[i].id, list[i].label));
				} else {
					select.append(that.createSelectOption(list[i], list[i]));
				}	
			}
		};
		
		//this methods suppose that the list parameter is ordered by group property
		that.drawSelectOptionsWithGroups=function(select,list,initialLabel,label,value){
			var i=0,group=false,html="";
			if(initialLabel) {
				select.append(that.createSelectOption(0,initialLabel));
			}
			while(i<list.length) {
				group=list[i].group;
				html += '<optgroup label="' + group + '">';
				for(;i<list.length && list[i].group === group; i++) {
					if(label && value) {
						html += that.createSelectOption(list[i][value],list[i][label]);
					} else {
						html += that.createSelectOption(list[i].id,list[i].label);
					}
				}
				html += '</optgroup>';
			}
			select.append(html);
		};
		
		that.drawCheckBoxs=function(div,list){
			var i;
			for(i=0;i<list.length;i++){
				div.append(that.createCheckButton(div.attr("id") + list[i].id,list[i].label));
			}
		};
		
		that.alertSuccess = function(id, title, message, timeout){
			$("#"+id).html('<div class="alert alert-success" style="width:50%; padding:0px;">' + 
			                  	'<a class="close" data-dismiss="alert" href="javascript:void(0)">×</a>' +
			                  	'<h4 class="alert-heading">' + title +'</h4>' + message +
			                  '</div>');
			if (timeout !== false){
				setTimeout(function() {
					  $("#"+id).empty();
				}, 5000);
			}
		};
		
		that.notifySuccess = function(title, message){
			var msg=message||'';
			$("#notificationContainer").notify("create", "success-template" ,{
			    title: title,
			    message : msg
			});
		};
		
		that.alertError = function(id, title, message, timeout){
			$("#"+id).html('<div class="alert alert-error" style="width:50%; padding:0px;">' + 
			                  	'<a class="close" data-dismiss="alert" href="javascript:void(0)">×</a>' +
			                  	'<h4 class="alert-heading">' + title +'</h4>' + message +
			                  '</div>');
			if (timeout !== false){
				setTimeout(function() {
					  $("#"+id).empty();
				}, 10000);
			}
		};
		
		that.notifyError = function(title, message){
			var msg=message||'';
			$("#notificationContainer").notify("create","error-template" ,{
			    title: title,
			    message: msg
			});
		};
		
		that.alertInfo = function(id, title, message, timeout){
			$("#"+id).html('<div class="alert alert-info" style="width:50%; padding:0px;">' + 
			                  	'<a class="close" data-dismiss="alert" href="javascript:void(0)">×</a>' +
			                  	'<h4 class="alert-heading">' + title +'</h4>' + message +
			                  '</div>');
			if (timeout !== false){
				setTimeout(function() {
					  $("#"+id).empty();
				}, 10000);
			}
		};
		
		that.notifyInfo = function(title, message){
			$("#notificationContainer").notify("create","info-template" ,{
			    title: title,
			    message: message
			});
		};
		
		//colModel is a vector of the type {label:label,name:name}
		that.drawTable=function(id,url,colModel) {
			var dfd = $.Deferred();
			var i,j,html,rows;
			var promiseRows=pkg.UTIL.downloadList(url);
			$.when(promiseRows).then(function(rowsResponse) {
				rows=rowsResponse.obj;
				html='<table class="table table-striped table-bordered table-condensed"><thead><tr>';
				html+="<th>#</th>";
				for(i=0; i < colModel.length;i++) {
					html+="<th>" + colModel[i].label +"</th>";
				}
				html+="</tr></thead><tbody>";
				for(i=0;i < rows.length ; i++) {
					html+="<tr>";
					html+="<td>"+ (i+1) +"</td>";
					for(j=0; j < colModel.length ; j++) {
						html+="<td>" + rows[i][colModel[j].name] + "</td>";
					}
					html+="</tr>";
				}
				html+="</tbody></table>";
				$("#"+id).append(html);
				dfd.resolve();
			},function(){
				dfd.reject();
			});
			return dfd.promise();
		};
		return that;
	};
	
	function Util() {
		var that={};
		//FIXME how does this function honor its name? Where's the list? It's being used to download other things. I find the name misleading. - jmpr 04/10/2012
		that.downloadList=function(url,loadingSectionId){
			var dfd = $.Deferred();
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += url.url;
			if(url.data) {
				ajaxRequest.data=url.data;
			}
			if(loadingSectionId) {
				ajaxRequest.loadingSectionId = loadingSectionId;
			}
			ajaxRequest.success=function(list){
				//urlToDataList[url]=list;
				dfd.resolve({success:true,obj:list});
			};
			ajaxRequest.error=function(){
				dfd.reject({success:false});
			};
			var promise=dfd.promise();		
			$.ajax(ajaxRequest);
			return  promise;
		};
		that.downloadFile=function(url, data, method) {
			// url and data options required
			if (url && data) {
				// data can be string of parameters or array/object
				data = typeof data == 'string' ? data : jQuery.param(data);
				// split params into form inputs
				var inputs = '';
				jQuery.each(data.split('&'), function() {
					var pair = this.split('=');
					var name=pair[0];
					var value=pair[1]==='null'?'':pair[1];
					inputs += '<input type="hidden" name="' +name
							+ '" value="' +value + '" />';
				});
				// send request
				jQuery(
						'<form action="' + acme.VARS.contextPath + url
								+ '" method="' + (method || 'post') + '"  enctype="multipart/form-data" >'
								+ inputs + '</form>').appendTo('body')
						.submit().remove();
			}
			
		};
		that.capitaliseFirstLetter=function (string){
		    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
		};
		return that;
	};
	
	(function($, undefined) {
		
		//This component is a wrapper to create a list of check boxs
		$.widget("ui.ACMEmultiCheckBox", {
			options : {
				//the list that contains all valid options
				listUrl : "",
				optionList:[],
				//a list that contains the selected elements
				selectedUrl:"",
				selectedOptions:[]
			},
			SelectCheckBox:function(id){
				$("#" + this.element.attr("id") + id).attr("checked","checked");
			},
			SelectCheckBoxs:function(list){
				var i;
				for(i=0;i<list.length;i++){
					this.SelectCheckBox(list[i].id);
				}
			},
			//Return an array with id values checked
			GetCheckedItems:function(){
				var vec=[];
				var element=this.element;
				$('#'+this.element.attr('id') + ' input:checked').each( function () {
					vec.push(this.id.substring(element.attr('id').length));
				});
				return vec;
			},
			_create : function() {
				var self=this;
				var element = this.element;
				var promiseWholeList, promiseSelectedItems;
			
				if (element.is("div")) {
					if(this.options.listUrl === ""){
						promiseWholeList={success:true,obj:this.options.optionList};
					}else{
						promiseWholeList=pkg.UTIL.downloadList(this.options.listUrl);
					}
					
					if(this.options.selectedUrl === ""){
						//if the user has provided a list of selected items at construction time, then we use the list instead of the URL
						promiseSelectedItems={success:true,obj:this.options.selectedOptions};
					}else{
						promiseSelectedItems=pkg.UTIL.downloadList(this.options.selectedUrl);
					}
					
					$.when(promiseWholeList,promiseSelectedItems).then(function(responseWholeList,responseSelectedList){
						if(responseWholeList.success && responseSelectedList.success){
							pkg.HTML_BUILDER.drawCheckBoxs(element,responseWholeList.obj);
							self.SelectCheckBoxs(responseSelectedList.obj);
						}else{
							throw "Failed loading CheckBoxs";
						}
					});
				} else {
					throw "The element " + element.id + " is not a div";
				}
			},
			_init : function() {

			}
			
		});
		
		//This is a widget that will render two comboboxes, one next to eachother
		//The user will be presented with buttons that will "move" an item from one combobox to the other
		//The left side combobox is the list of available items and the right side is the list of selected items
		//The list of available items is automatically build by this componentes based on the "completeList-selectedList". Thus, every item that is on the completeList and has not been selected
		//options:
		//completeListURL and completeListArray: Is a list of objects of the form {id:x,label:llll} that will be used to render the dropdown
		//selectedListUrl and selectedListArray: Is a list of objects of the form {id:x,label:lll} or [id1,id2,id3...] (just a plain list of IDs)
		$.widget("ui.ACME2SideMultiSelect", {
			options : {
					//the list that contains all valid items. T
					completeListURL : "",
					completeListArray:[],
					
					availableLabel:'Available',
					//a list that contains the selected elements
					selectedListURL:"",
					selectedListArray:[],
					
					selectedLabel:'Selected'
			},
			
			_create : function() {
				var self = this;
				var element = this.element;
				
				if (element.is("div")) {
					var table = $('<table></table>');
					var row = $('<tr></tr>');
					var column1 = $('<td></td>');
					var column2 = $('<td></td>');
					var column3 = $('<td></td>');
					var column4 = $('<td></td>');
					var select1 = $('<label for="select1">' + this.options.availableLabel + '</label><select multiple="multiple" id="select1"></select>');
					var select2 = $('<label for="select2">' + this.options.selectedLabel + '</label><select multiple="multiple" id="select2"></select>');
					
					column2.append('<a href="javascript:void(0)" class="btn btn-mini" id="btn-right"><i class="icon-arrow-right"></i></a>');
					column2.append('</br>');
					column2.append('<a href="javascript:void(0)" class="btn btn-mini" id="btn-left"><i class="icon-arrow-left"></i></a>');
					
					column4.append('<a href="javascript:void(0)" class="btn btn-mini" id="btn-up"><i class="icon-arrow-up"></i></a>');
					column4.append('</br>');
					column4.append('<a href="javascript:void(0)" class="btn btn-mini" id="btn-down"><i class="icon-arrow-down"></i></a>');
					
					column1.append(select1);
					column3.append(select2);
					row.append(column1);
					row.append(column2);
					row.append(column3);
					row.append(column4);
					table.append(row);
					element.append(table);
					$.when(this._load()).then(function() {
						self._initButtons();
					});
					
				} else {
					throw "The element " + element.id + " is not a div";
				}
			},
			
			_clear : function() {
				this.element.find("#select1").empty();
				this.element.find("#select2").empty();
			},
			
			_init : function() {

			},
			
			_load : function() {
				var dfd = $.Deferred();
				var self = this;
				var element = this.element;
				var promiseCompleteList, promiseSelectedList;
				
				if (this.options.completeListURL === "") {
					promiseCompleteList = {
						success: true,
						obj: this.options.completeListArray
					};
				} else {
					promiseCompleteList = pkg.UTIL.downloadList(this.options.completeListURL, element.attr('id'));
				}
					
				if (this.options.selectedListURL === "") {
					promiseSelectedList = {
						success: true,
						obj: this.options.selectedListArray
					};
				} else {
					promiseSelectedList = pkg.UTIL.downloadList(this.options.selectedListURL, element.attr('id'));
				}
	
				$.when(promiseCompleteList, promiseSelectedList).then(function(responseCompleteList, responseSelectedList) {
					if(responseCompleteList.success && responseSelectedList.success){
						var availableList = [];
						var completeList = responseCompleteList.obj;
						var selectedList = responseSelectedList.obj || [];
						var idToItem = {};
						
						var available;
						var objectToCompare;
						
						var i = 0;
						var j = 0;
						
						var selectedListBuilt = [];
						
						// Only used in ACME2SideMultiSelectWithCombination
						var combination;
						
						// An object is a combination if it contains a _ and does not contain meta_
						// Or contains meta and anthore _CRITERIA suffix
						var isACombination = function(objectToCompare) {
							if (typeof objectToCompare !== 'string') {
								return false;
							}
							
							if (objectToCompare.indexOf('meta_') !== -1 ) {
								// is meta, split like ['meta', 'someName', 'someOtherCriteria']
								return objectToCompare.split('_').length === 3;
							}
							
							if (objectToCompare.indexOf('_') !== -1) {
								return true;
							}
							
							return false; 
						};
						
						/* 
						 * availableList = completeList - selectedList
						 * 
						 * We loop through the complete list of elements and append to the availableList
						 * the ones that are not selected (not in selectedList).
						 * 
						 */
						for(i = 0; i < completeList.length; i++){
							var item = completeList[i];
							available = true;
							
							for(j = 0; j < selectedList.length && available; j++) {
								objectToCompare = (typeof selectedList[j] == 'object') ? selectedList[j].id : selectedList[j];
								
								/* 
								 * ACME2SideMultiSelectWithCombination concatenates the elements with a given combination,
								 * so we need to remove that part before making the comparison. E.g. element5_combination
								 */
								
								if (isACombination(objectToCompare)) {
									objectToCompare = objectToCompare.substring(0, objectToCompare.lastIndexOf('_'));
								}
								
								if(item.id === objectToCompare) {
									available = false;
								}
							}
							
							if(available) {
								availableList[availableList.length] = item;
							}
							
							idToItem[completeList[i].id] = completeList[i];
						}
						
						/*
						 * We build selectedListBuilt by looping through selectedList and appending the objects
						 * mapped in idToItem. selectedList only contains the IDs and not the whole object, that's why
						 * we need this step and the object idToItem.
						 */ 
						for(i = 0; i < selectedList.length; i++) {
							objectToCompare = (typeof selectedList[i] == 'object') ? selectedList[i].id : selectedList[i];
							
							if (isACombination(objectToCompare)) {
								combination = objectToCompare.substring(objectToCompare.lastIndexOf('_'));
								objectToCompare = objectToCompare.substring(0, objectToCompare.lastIndexOf('_'));
								selectedListBuilt[selectedListBuilt.length] = idToItem[objectToCompare];
								
								// concatenate the combination value again
								selectedListBuilt[selectedListBuilt.length - 1].id += combination;
								
								// concatenate the combination to the label
								selectedListBuilt[selectedListBuilt.length - 1].label += combination.replace('_', ' - ');
							} else {
								selectedListBuilt[selectedListBuilt.length] = idToItem[objectToCompare];
							}
						}
						
						pkg.HTML_BUILDER.drawSelectOptions(element.find("#select1"), availableList);
						pkg.HTML_BUILDER.drawSelectOptions(element.find("#select2"), selectedListBuilt);
						dfd.resolve();
					} else{
						throw "Failed loading 2SideMultiselect";
					}
				});
				
				return dfd.promise();
			},
			
			reload : function() {
				this._clear();
				this._load();
			},
			
			getSelectedItems : function() {
				var result = [];
				var options = this.element.find("#select2 option");
				for(var i=0;i < options.length ;i++) {
					result.push(options[i].value);
				}
				return result;
			},
			
			_initButtons : function() {
				var self = this;
				this.element.find('#btn-right').click(function(){
					var options = self.element.find("#select1 option:selected").map(function() {
						return {id:this.value,label:this.label};
					});
					for(var i = 0; i < options.length; i++) {
						self.element.find("#select2").append(pkg.HTML_BUILDER.createSelectOption(options[i].id,options[i].label));
					}
					self.element.find("#select1 option:selected").remove();
				});
				
				this.element.find('#btn-left').click(function(){
					var options = self.element.find("#select2 option:selected").map(function() {
						return {id:this.value,label:this.label};
					});
					for(var i=0; i < options.length ;i++) {
						self.element.find("#select1").append(pkg.HTML_BUILDER.createSelectOption(options[i].id,options[i].label));
					}
					self.element.find("#select2 option:selected").remove();	
				});
				
				this.element.find('#btn-up').click(function(){
					var options = self.element.find("#select2 option:selected");
					var selected = [];
					for(var i = 0; i < options.length; i++) {
						var prev = $(options[i]).prev();
						if(prev.length){
							$(prev).before(pkg.HTML_BUILDER.createSelectOption(options[i].value,options[i].label));
							$(options[i]).remove();
							selected.push(options[i].value);
						}
					}
					self.element.find("#select2").val(selected);
				});
				
				this.element.find('#btn-down').click(function(){
					var options = self.element.find("#select2 option:selected");
					var selected = [];
					var lastElementIndex = options.length - 1;
					for(var i = 0; i < options.length; i++) {
						var currentIndex = lastElementIndex - i;
						var next = $(options[currentIndex]).next();
						if(next.length) {
							$(next).after(pkg.HTML_BUILDER.createSelectOption(options[currentIndex].value, options[currentIndex].label));
							$(options[currentIndex]).remove();
							selected.push(options[currentIndex].value);
						}
					}
					self.element.find("#select2").val(selected);
				});
			}
		});
		
		$.widget('ui.ACME2SideMultiSelectWithCombination', $.ui.ACME2SideMultiSelect, {
			options : {
				optionList: [],
				defaultIndex: 0,
				multipleSelect: false,
				combinationLabel: ""
			},
			_create : function() {
	            $.ui.ACME2SideMultiSelect.prototype._create.call(this);
	            var optionList = this.options.optionList;	            
	            var labelStr = '<label for="selectCombination">' + this.options.combinationLabel + '</label>';
	            var selectStr;

	            if (this.options.multipleSelect) {
	            	selectStr = '<select class="selectCombination" size="4"></select>';
	            } else {
	            	selectStr = '<select class="selectCombination"></select>';
	            } 
	            
	            var select = $(selectStr);
	            var column = $("<td></td>");
	            var i;
	            
	            for (i = 0; i < optionList.length; i++) {
	            	select.append('<option value="' + optionList[i] + '">' + optionList[i] + '</option>');
	            }
	            
	            column.append(labelStr);
	            column.append(select);
	            $(this.element).find('tr td:nth-child(2)').before(column);
			},
			_initButtons : function() {
				var self = this;

				$.ui.ACME2SideMultiSelect.prototype._initButtons.call(this);
				
				// unbind the event handlers from the superclass
				this.element.find("#btn-right").off('click');
				this.element.find("#btn-left").off('click');
				
				this.element.find('#btn-right').click(function(){
					var options = self.element.find("#select1 option:selected").map(function() {
						var combinationValue = $(".selectCombination").val();
						
						if (combinationValue === null) {
							combinationValue = $('.selectCombination option:eq(0)').val();
						}
						
						var id = this.value + "_" + combinationValue;
						var label = this.label + " - " + combinationValue;
						
						return {id:id, label:label};
					});
					
					for(var i = 0; i < options.length; i++) {
						self.element.find("#select2").append(pkg.HTML_BUILDER.createSelectOption(options[i].id, options[i].label));
					}
					
					self.element.find("#select1 option:selected").remove();
				});
				
				this.element.find('#btn-left').click(function(){
					var options = self.element.find("#select2 option:selected").map(function() {
						var label = this.label.substring(0, this.label.indexOf('-') - 1);
						var value = this.value.substring(0, this.value.lastIndexOf('_'));
						return {id:value, label:label};
					});
					
					for(var i=0; i < options.length ;i++) {
						self.element.find("#select1").append(pkg.HTML_BUILDER.createSelectOption(options[i].id, options[i].label));
					}
					
					self.element.find("#select2 option:selected").remove();
				});
			}
		}); 
		
		//This is an extenstion to the boostrap dropdown that allows to use it as a combobox
		//This is an example of usage:
//		$("#anyDivId").ACMEBootsrapSelect({list:selectOptions,initialSelectedValue:2});
//		$("#anyDivId").on("itemChanged", function(event, data){
//			alert('changed item '+data.label);
//		});
		$.widget("ui.ACMEBootsrapSelect", {
			options : {
					//A list of objects to be used on the select
				  //The objects must be of the form {label:"xxx",value:2}
				 	list : [],
					//The value of an element provided on the list
					//If the selectedElement is not on the provided list, then it will behave as nothing has been selected
					initialSelectedValue:null
			},
			selectedItem:null
			,
			//Since the list.options is of the form {label:"xxx",value:2}, this method will search for an item whose value is equal to the given parameter
			//the method might return null if there is no object with the given selectedValue
			//Every time the selection changed this method will fire the custom event "itemChanged"

			_obtainItem:function(list,selectedValue){
				var i=0;
				for(i=0;i<list.length;i++){
					if(list[i].value===selectedValue){
						return list[i];
					}
				}
				return null;
			},
			_paintListForSelect:function(selectedItem){
				var self=this;
				var element=self.element;
				var ulE=element.find("ul");
				var list=self.options.list;
				var i=0;
				var liElement;
				var onClick=function(){
					var item=$(this).data('c_item');
					//alert('click on element '+item.label);
					self._paintListForSelect(item);
					self.element.trigger('itemChanged',[item]);
				};
				//store the selectedvalue
				this.selectedItem=selectedItem;
				ulE.empty();
				for(i=0;i<list.length;i++){
					if(!selectedItem || selectedItem.value!=list[i].value){
						//paint all elements but not the selected
						liElement=$('<li><a href="javascript:void(0)" >'+list[i].label+'</a></li>');
						liElement.find("a").click(onClick).data('c_item',list[i])
						ulE.prepend(liElement);
					}
				};
				//paint the selectedElement
				element.find(".selectedItem").text(selectedItem.label);
			},
			_create : function() {
				var self = this;
				var element = this.element;
				var html;
				if (element.is("div")) {
					
					html='<a class="btn dropdown-toggle" data-toggle="dropdown" href="javascript:void(0)">';
					html+='<span class="selectedItem" ></span>';
					html+='<span class="caret"></span>';
					html+='</span>';
					html+='</a>';
					html+='<ul class="dropdown-menu" >';
					html+='</ul>';
					
					element.addClass("btn-group");
					element.html(html);
					
				}else{
					throw "The element "+this.element.id+" is not a div";
				}
			},
			
			_clear : function() {
				var self = this;
				var element = this.element;
				element.empty();
			},
			
			_init : function() {
				var self = this;
				var element = this.element;
				var selectedItem=this.options.initialSelectedValue;
				var selectedItem=self._obtainItem(this.options.list,this.options.initialSelectedValue);
				self._paintListForSelect(selectedItem);
				
			},
			
			_load : function() {
				
			},
			
			reload : function() {
				this._clear();
				this._load();
			},
			
			getSelectedItems : function() {
				
			}
		});
	}($));
	

	
	// PUBLIC OBJECTS
	pkg.HTML_BUILDER=HtmlBuilder();
	pkg.UTIL=Util();
	pkg.confirmation=confirmation;
	pkg.dialog=dialog;
	pkg.errorMessage=errorMessage;
	pkg.iconForTable=iconForTable;
	return pkg;
});
