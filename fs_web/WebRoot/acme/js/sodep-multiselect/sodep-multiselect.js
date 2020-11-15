define(["jquery", "acme", "jquery-ui", "acme-ui"], function($, acme, $ui, acme_ui) {


	var i18nPromise = acme.I18N_MANAGER.load(['web.generic.ok',
	                                          'web.generic.cancel',
	                                          'web.generic.error',
	                                          'web.multiselect.invalidId',
	                                          "web.multiselect.selectAll",
	                                          "web.multiselect.unselectAll",
	                                          "web.multiselect.filter"]);
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var errorDialog = function(message){
		$.when(i18nPromise).then(function(){
			var title = I18N('web.generic.error');
			var ok = I18N('web.generic.ok');
			var msg = message ? message : I18N('web.generic.error');
			$('<div></div>').html(msg).dialog({
				title : title,
	            resizable : false,
	            autoOpen : true,
	            width : 400,
	            height : 180,
	            modal : true,
	            buttons:
	            	[{ 	text: ok,
	            		click : function() {
	            			$(this).dialog('close');
	            			$(this).dialog('destroy');
	            		}
	            	}]
			});
		});
	};
	
	var requestModel = function(){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += this.options.modelURI + this.options.id;
		var params = this.options.requestParams;
		if(this.options.requestParams && this.options.initialParams){
			params = $.extend({}, this.options.requestParams, this.options.initialParams);
		} else if(this.options.initialParams){
			params = this.options.initialParams;
		}
		ajaxRequest.data = JSON.stringify({params : params});
		
		ajaxRequest.contentType = 'application/json; charset=utf-8';
		var dfd = $.Deferred();
		var sodep = this.sodep;
		
		ajaxRequest.success = function(response) {
			if (response.success === true) {
				sodep.model = response.obj; 
			} else {
				sodep.model = false;  
				sodep.requestModelMessage = response.message;
			}
			dfd.resolve();
		};
		
		ajaxRequest.error = function(jqXHR, textStatus, errorThrown){
			dfd.resolve();
		};
		
		$.ajax(ajaxRequest);
		
		sodep.requestModelPromise = dfd.promise();
		return sodep.requestModelPromise;
	};
	
	var containerDivScroll = function(div, e){
		var scrollHeight =  div.prop('scrollHeight');
		var scrollTop = div.scrollTop();
		var height = div.height();
		var self = this;
		if(scrollTop && (scrollTop + height) == scrollHeight){
			var promise = requestItems.apply(this);
			$.when(promise).then(function(){
				drawItems.apply(self);
			});
		};
	};
	
	var requestFilteredItems = function(self){
		// If there's a current request, we should let it finish
		$.when(self.sodep.requestItemsPromise).then(function(){
			self.sodep.items = null;
			self.sodep.pageNumber = 1;
			var promise = requestItems.apply(self);
			// A new request has been issued
			$.when(promise).then(function(){
				self.sodep.uielements.itemsUl.html('');
				drawItems.apply(self);
			});
		});
	};
	
	var filterInputAction = function(e){
		if(this.sodep.filterTimeout){
			clearTimeout(this.sodep.filterTimeout);
		}
		var self = this;
		var timeoutFunc = function(){
			requestFilteredItems(self);
		};
		this.sodep.filterTimeout = setTimeout(timeoutFunc, this.options.filterDelay);
	};
	
//	var filterInputChange = function(e){
//		acme.LOG.debug("change, e = " + e);
//	};
	
	/**
	 * This functions initializes 
	 */
	var initUI = function(){
		var self = this;
		var divElement = this.element;
		divElement.addClass('multiselect');
		
		var prefix = divElement.attr('id'); //TODO if no id, choose a random str for prefix
		divElement.html('');
		var labelDiv = $('<div></div>', {"id" : prefix + '_multiselect_label', "class" : 'multiselect_label' }).html('&nbsp');
		divElement.append(labelDiv);
		
		if(self.options.allButtons){
			$.when(i18nPromise).then(function(){
				divElement.append('<br/>');
				var selectAllLink = $('<a></a>', { "click" : function(){
						self.selectAll();
					}, "class" : "all_link"
				}).html(I18N('web.multiselect.selectAll'));
				
				var unselectAllLink = $('<a></a>', { "click" : function(){
						self.unselectAll();
					}, "class" : "all_link"
				}).html(I18N('web.multiselect.unselectAll'));
				
				divElement.append(selectAllLink);
				divElement.append(' | ');
				divElement.append(unselectAllLink);
			});
		}
		
		if(this.options.width){
			divElement.css('width', this.options.width);
		}
		if(this.options.height){
			divElement.css('height', this.options.height);
		}
		
		var mainDiv = $('<div></div>', {"id" : prefix + '_multiselect_main', "class" : 'multiselect_main'});
		
		divElement.append(mainDiv);
		var containerDiv = $('<div></div>', {"id" : prefix + '_multiselect_container', "class" : 'multiselect_container'});
		// handle the scrolling to get more data
		containerDiv.scroll(function(e){
			containerDivScroll.apply(self,[containerDiv, e]);
		});
		
		var filterDiv = $('<div></div>', {"id" : prefix + "_multiselect_filter" ,"class" : 'multiselect_filter'});
		var filterInput = $('<input></input>', {"id" : prefix + '_multiselect_filter', "class" : 'multiselect_filter search'});
		
		filterInput.keyup(function(e){
			filterInputAction.apply(self);
		});
		
		filterDiv.append(filterInput);
		
		$.when(i18nPromise).then(function(){
			filterInput.attr('placeholder', I18N("web.multiselect.filter"));
		});
		
		filterInput.bind({
			copy : function(){
				filterInputAction.apply(self);
			},
			paste : function(){
				filterInputAction.apply(self);
			},
			cut : function(){
				filterInputAction.apply(self);
			}
		});
		
		var itemsUl = $('<ul></ul>', {"id" : prefix + '_multiselect_list', "class" : 'multiselect_list'});
		containerDiv.append(itemsUl);
		
		mainDiv.append(containerDiv);
		divElement.append(filterDiv);
		// calculate dimensions of different items
		mainDiv.css("height", divElement.height() - (filterDiv.outerHeight(true) + labelDiv.outerHeight(true)));
		var filterInputPadding = filterInput.outerWidth() - filterInput.width();
		
		
		this.sodep.uielements = {};
		this.sodep.uielements.mainDiv = mainDiv;
		this.sodep.uielements.containerDiv = containerDiv;
		this.sodep.uielements.itemsUl = itemsUl;
		this.sodep.uielements.filterInput = filterInput;
	};
	
	var applyModelToUI = function(){
		var label = this.sodep.model.label;
		var multiselectLabel = this.element.find('.multiselect_label');
		multiselectLabel.html(label);
	};
	
	var readRequestBuilder = function(){
		var requestObject = {};
		var filter = this.sodep.uielements.filterInput.val();
		requestObject.filter = filter;
		requestObject.pageNumber = this.sodep.pageNumber;
		requestObject.pageLength = this.options.pageLength;
		requestObject.order = this.options.order;
		requestObject.params = this.options.requestParams ? this.options.requestParams : null;
		return requestObject;
	};
	
	var requestItems = function(){
		//TODO loading icon
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += this.options.listItemsURI + this.options.id;
		ajaxRequest.data = JSON.stringify(readRequestBuilder.apply(this));
		ajaxRequest.contentType = 'application/json; charset=utf-8';
		var prefix = this.element.attr('id');
		ajaxRequest.loadingSectionId = prefix + '_multiselect_main';
		
		var dfd = $.Deferred();
		var sodep = this.sodep;
		
		ajaxRequest.success = function(response){
			if(response.success === true){
				if(response.items && response.items.length > 0){
					var selectedItems = sodep.selectedItems;
					// why this? e.g. it may be the case that the item was selected but it was later filtered
					for(var i = 0; i < response.items.length; i++){
						var item = response.items[i];
						var inArray = ($.inArray(item.id, selectedItems) != -1);
						item.selected = (sodep.selectionMode !== 'ALL' && inArray /*selectedItems.indexOf(item.id) >= 0*/) || 
									(sodep.selectionMode === 'ALL' && !inArray);
					}
					
					if(!sodep.items) {
						sodep.items = response.items;
					} else {
						var items = [];
						for(var i = 0; i < response.items.length; i++){
							var responseItem = response.items[i];
							var alreadyExists = false;
							for(var j = 0; j < sodep.items.length; j++){
								var existingItem = sodep.items[j];
								alreadyExists = responseItem.id === existingItem.id;
								if(alreadyExists){
									acme.LOG.debug('discarded already existing item'); //It's better than nothing
									break;
								}
							}
							
							if(!alreadyExists){
								items.push(responseItem);
							}
						}
						sodep.items = sodep.items.concat(items);
					}
					sodep.pageNumber += 1;
				}
			} else {
				sodep.items = null;
				sodep.requestItemsMessage = response.message;
			}
			dfd.resolve();
		};
		
		ajaxRequest.error = function(jqXHR, textStatus, errorThrown){
			dfd.resolve();
		};
		
		$.ajax(ajaxRequest);
		
		sodep.requestItemsPromise = dfd.promise();
		return sodep.requestItemsPromise;
	};
	
	var itemClickFactory = function(self, li, item){
		return function(){
			acme.LOG.debug("item clicked id=" + item.id + ", label=" + item.label);
			var checkboxes = self.options.checkboxes;
			if(self.options.single){
				self.sodep.selectedItems = [];
				for(var i = 0; i < self.sodep.items.length; i++){
					if(self.sodep.items[i].id != item.id){
						self.sodep.items[i].selected = false;
					}
				}
				// remove whatever marks an item as selected
				self.sodep.uielements.itemsUl.find('.multiselect_item').removeClass('selected_item');
				if(checkboxes){
					self.sodep.uielements.itemsUl.find('input.item_checkbox[id!="check_' + item.id  + '"]').attr('checked', false);
				}
			}
			
			if(item.selected){
				li.removeClass("selected_item");
				// it might be the case that the user didn't click on the checkbox
				if(checkboxes){
					li.find('input[type="radio"]').attr('checked', false);
				}
				
				if(self.sodep.selectionMode !== 'ALL') {
					var idx = $.inArray(item.id, self.sodep.selectedItems); 
						// self.sodep.selectedItems.indexOf(item.id);
					self.sodep.selectedItems.splice(idx, 1);
				} else {					
					self.sodep.selectedItems.push(item.id);
				}
				item.selected = false;
			} else {
				li.addClass("selected_item");
				if(checkboxes){
					li.find('input[type="radio"]').attr('checked', true);
				}
				
				if(self.sodep.selectionMode !== 'ALL') {
					self.sodep.selectedItems.push(item.id);
				} else {
					var idx = $.inArray(item.id, self.sodep.selectedItems);
						// self.sodep.selectedItems.indexOf(item.id);
					self.sodep.selectedItems.splice(idx, 1);
				}
				item.selected = true;
			}
			
			if(self.options.selected && $.isFunction(self.options.selected)){
				self.options.selected({id : item.id, selected : item.selected}, self.sodep.selectedItems, self.sodep.selectionMode);
			}
							
			//FIXME implement "selected" as a proper event
			//trigger the selected event, so plugin user's can bind to ir
//			var eventObj = {"isSelected" : item.selected, "id" : item.id, "label" : item.labels[0], "element" : li};
//			self._trigger("selected", eventObj, {});
		};
	};
	
	var navigateLinkClickFactory = function(self, li, item, func){
		return function(){
			if(func && $.isFunction(func)){
				var arg = {id : item.id, selected : item.selected};
				func(arg, self.sodep.selectedItems);
			} else{
				throw "Invalid click function";
			}
			return false;
		};
	};
	
	var checkAuthorization=function(id,authorization){
		
	};
	var buildLinkDiv = function(c, n, item, li, linkWidth){
		var rightMarginOffset = 0;
		//var topMarginOffset=-4;
		if(!$.isFunction(c.func)){
			throw "click function was not defined"; 
		}
		if(c.topMargin){
			topMarginOffset=c.topMargin;
		}
		var linkDiv = $('<div></div>', { "class" : 'item_link', click : navigateLinkClickFactory(this, li, item, c.func)});
		linkDiv.css('right', (linkWidth * n + rightMarginOffset)  + 'px');
		//linkDiv.css('top', topMarginOffset  + 'px');
		
		var iconStr;
		var icon=c.icon;
		if(!c.icon){
			icon='pencil';
		}
		
		var classAvailability='multiSelectIcon';
		if(c.authorizationFunction && $.isFunction(c.authorizationFunction)){
			//this element requires authorization
			if(!c.authorizationFunction(item)){
				classAvailability='multiSelectIconDisabled';
			}
		}
		linkDiv.html('<a href="" class="glyphicons '+classAvailability+' '+icon+'"  ></a>');
		if(c.tooltip){
			linkDiv.attr("title", c.tooltip);
			linkDiv.tooltip({placement : 'top'});
			linkDiv.mouseenter(function(e){
				e.stopPropagation();
			}).mouseleave(function(e){
				e.stopPropagation();
			});
		}
		return linkDiv;
	};
	
	var drawItems = function(){
		var self = this;
		var sodep = this.sodep;
		var items = sodep.items;
		if(items){
			var ul = sodep.uielements.itemsUl;
			var pageLengthOdd = (this.options.pageLength % 2);
			for(var i = 0; i < items.length; i++){
				var item = items[i];
				if(!item.drawn){
					var iMod = (pageLengthOdd + i) % 2;
					var modClass = iMod ? 'even_item' :  'odd_item';
					var selectedClass = item.selected ? ' selected_item' : '';
					var li = $('<li></li>', {"id" : item.id, "class" : 'multiselect_item ' + modClass  + selectedClass});
					
					var itemDiv = $('<div></div>', { "class" : "item_container"});
					li.append(itemDiv);
					var labelDiv = $('<div></div>', { "class" : 'item_label' });
					itemDiv.append(labelDiv);
					
					labelDiv.html('<div>' + item.label + '</div>');

					var clickOption = self.options.click;
					var showInactive = self.options.showInactive;
					var nLinks = 0;
					var linkWidth = 30; //FIXME harcoded value based on css style
					if(clickOption){
						if($.isFunction(clickOption)){
							nLinks = 1;
							var linkDiv = buildLinkDiv.apply(self, [{func: clickOption}, 0, item, li, linkWidth]);
							itemDiv.append(linkDiv);
						} else if($.isArray(clickOption)){
							nLinks = clickOption.length;
							for(var n = 0; n < nLinks; n++){
								var c = clickOption[n];
								var linkDiv = buildLinkDiv.apply(self, [c, n, item, li, linkWidth]);
								itemDiv.append(linkDiv);
							}
						} else if(typeof(clickOption) == 'object')  {
							nLinks = 1;
							var linkDiv = buildLinkDiv.apply(self, [clickOption, 0, item, li, linkWidth]);
							itemDiv.append(linkDiv);
						} else {
							throw "Invalid parameter for click " + clickOption;
						}
					} 
					
					if(self.options.selected){
						labelDiv.addClass("selectable_label");
						li.click(itemClickFactory(self, li, item));
					}
					
					var labelSubDiv = labelDiv.children('div');
					if(self.options.checkboxes){
						var checked = item.selected ? 'checked="checked"' : '';
						labelSubDiv.prepend('<input type="radio" id="check_' + item.id + '" class="item_checkbox" ' + checked +' />&nbsp');
					}
					
					ul.append(li);
					// calculate dimensions
					labelSubDiv.css('width', labelDiv.width() - (nLinks * linkWidth));
					if(item.tooltip){
						labelSubDiv.attr("title", item.tooltip);
						labelSubDiv.tooltip({placement : 'bottom'});
					}
					item.drawn = true;
					if(showInactive) {
						if (!item.active && !li.hasClass('not_active_item')) {
							li.addClass('not_active_item');
							li.attr("title", "Application is NOT active");
						} else if(item.active) {
							if(li.hasClass('not_active_item')) {
								li.removeClass('not_active_item');
							}
							li.attr("title", "Application is active");
						}
					}
				} else {
					var li = ul.children('li#' + item.id);
					if (item.selected && !li.hasClass('selected_item')) {
						li.addClass('selected_item');
						if(self.options.checkboxes){
							li.children('input[type=radio]"').attr('checked', true);
						}
					} else if(!item.selected && li.hasClass('selected_item')) {
						li.removeClass('selected_item');
						if(self.options.checkboxes){
							li.children('input[type=radio]"').attr('checked', false);
						}
					}
				}
			}
		}
	};
	
	var hideTooltips = function(){
		if(this.sodep && this.sodep.uielements && this.sodep.uielements.itemsUl){
			var items = this.sodep.uielements.itemsUl.find("div.item_link");
			items.tooltip('hide');
			items = this.sodep.uielements.itemsUl.find("div.item_label div");
			items.tooltip('hide');
		}
	};
	
	var WIDGET  = { 
			
		options : {
			id: false,
			height : false,
			width : false,
			select : null, // a function that is called when an item is selected //FIXME consider a name refactoring
			click : null, //FIXME consider refactoring
			allButtons : false,
			single : false,
			autoRequest : true,
			checkboxes : true,
			requestParams : null,
			initialParams : null,
			order : {"column" : 0, "criteria" : 'asc'},
			pageLength : 10,
			modelURI: '/api/ui/multiselect/loadModel/',
			listItemsURI : '/api/ui/multiselect/listItems/',
			actionURI : '/api/ui/multiselect/action/',
			filterDelay : 500, 
			showInactive: false
		},
		
		/**
		 * When the plugin is created, the model is loaded.
		 * 
		 * The model defines the title, the images and the columns, etc. 
		 * The "look" of the multiselect.
		 */
		_create : function(){
			this.sodep = {};
			if(!this.options.id){
				$.when(i18nPromise).then(function(){
					errorDialog(I18N('web.multiselect.invalidId'));
				});
				throw "undefined id";
			}
			if(!this.multiselectInitialized){ //is this check even necessary? - jmpr 13/12/12
				initUI.apply(this);
				this.multiselectInitialized = true;
			}
			requestModel.apply(this);
		},
		
		/**
		 * The model should be loaded. Now with the model we request the items.
		 */
		_init : function(){
			var self = this;
			$.when(self.sodep.requestModelPromise).then(function(){
				applyModelToUI.apply(self);
				if (self.sodep.model && self.options.autoRequest === true){
					self.sodep.pageNumber = 1;
					self.sodep.selectionMode = 'NONE';
					self.sodep.selectedItems = [];
					self.reload();
				} else {
					errorDialog(self.sodep.requestModelMessage);
				}
			});
		},
		
		_setOption : function(key, value){
			if(key === 'selected'){
				// TODO
			} else{
				$.Widget.prototype._setOption.apply(this, arguments);
			}
		},
		
		getItem : function(id){
			var i, item = null;
			for(i = 0; i < this.sodep.items.length ; i++) {
				if(this.sodep.items[i].id === id) {
					item = this.sodep.items[i];
					return {id: item.id, label: item.label, tooltip:item.tooltip};
				}
			}
			
		},
		
		destroy : function(){
			$.Widget.prototype.destroy.call(this);
			hideTooltips.apply(this);
		},
		
		reload : function(){
			//FIXME refactor
			var self = this;
			hideTooltips.apply(this);
			self.clear();
			
			var promise = requestItems.apply(self);
			$.when(promise).then(function(){
				drawItems.apply(self);
			});
		},
		
		clear : function(){
			this.sodep.pageNumber = 1;
			this.sodep.selectedItems = [];
			this.sodep.items = [];
			this.sodep.uielements.itemsUl.html('');
			this.sodep.selectionMode = 'NONE';
		},
		
		selected : function(){
			// This a useful defensive copy whenever items are values and not objects
			// else every object should also be copied
			var ret = [];
			var selectedItems = this.sodep.selectedItems;
			if(selectedItems){
				for(var i = 0; i < selectedItems.length; i++){
					var item = selectedItems[i];
					ret.push(item);
				}
			}
			return ret;
		},
		
		selectAll : function(){
			var self = this;
			if(this.options.single){
				throw "Cannot select all in single mode";
			}
			this.sodep.selectionMode = 'ALL';
			var items = this.sodep.items;
			if(items){
				for(var i = 0; i < items.length; i++){
					items[i].selected = true;
				}
				drawItems.apply(self);
			}
			if(self.options.selected && $.isFunction(self.options.selected)){
				self.options.selected({id : -1, selected : true}, self.sodep.selectedItems, this.sodep.selectionMode);
			}
		},
		
		unselectAll : function(){
			var self = this;
			this.sodep.selectionMode = 'NONE';
			this.sodep.selectedItems = [];
			var items = this.sodep.items;
			if(items){
				for(var i = 0; i < items.length; i++){
					items[i].selected = false;
				}
				drawItems.apply(self);
			}
			if(self.options.selected && $.isFunction(self.options.selected)){
				self.options.selected({id : -2, selected : false}, self.sodep.selectedItems, this.sodep.selectionMode);
			}
		},
		
		selectionMode : function(){
			if(this.sodep.selectionMode){
				return this.sodep.selectionMode;
			}
			return 'NONE';
		}
		
	};

	$.widget("ui.sodepMultiselect", WIDGET);
	
});
