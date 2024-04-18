define(["jquery", "acme", "editor/common", "jquery-ui" ], function($, acme, COMMON, $ui){

	var that = {};	

	var I18N = acme.I18N_MANAGER.getMessage;
	
	var selectedProject = {};
	
	var model = {};
	
	var commands = [];
	
	/**
	 * Used to give unique ids to pages
	 */
	var pCounter = 0;
	
	/**
	 * Used to give unique ids to elements
	 */
	var eCounter = 0;
	
	var loadingSectionId = null;
	
	var postCommands = function(url) {
		var dfd = $.Deferred();
		var savePromise = dfd.promise();
		
		var ajax = acme.AJAX_FACTORY.newInstance();
		ajax.url += url;
		ajax.contentType = 'application/json; charset=utf-8';
		ajax.data = JSON.stringify(commands);
		if(loadingSectionId){
			ajax.loadingSectionId = loadingSectionId;
		}
		ajax.success = function(response){
			if(response.success){
				acme.LOG.debug('command execution successful');
				if(response.obj){
					that.form = model = response.obj;
					init();
					bind();
					// mark unreachable pages
					that.unreachablePages();
					if(response.computedAuthorizations){
						acme.AUTHORIZATION_MANAGER.setAuthorizations(response.computedAuthorizations);
					}
				} 
				
				dfd.resolve({success : true, form : model, msg : {title : response.title, message : response.message }});
			} else {
				acme.LOG.debug('command execution failed');
				var message = null;
				if(response.message){
					message = response.message;
				} else {
					message = I18N('web.editor.error.notSaved');
				}

				dfd.resolve({success : false, msg : {title : response.title, message : message }});
			}
			
		};
		$.ajax(ajax);
		
		return savePromise;
	};
	
	var publishSuccessCallback =  function(dfd, response){
		if(response.success) {
			that.form = model = response.obj;
			init();
			bind();
			var isPublished = model.published;
			var versionPublished = model.versionPublished;
			var currentVersion = model.version;
			var status = {isPublished : isPublished, versionPublished : versionPublished, currentVersion : currentVersion };
			dfd.resolve({success:true, form:model, publishStatus : status, msg : {title:response.title, message: response.message} });
		} else {
			dfd.resolve({success:false, msg : {title:response.title, message: response.message}});
		}
	};
	
	var init = function(){
		commands = [];
		eCounter = 0;
		pCounter = 0;
	};
	
	/**
	 * Binds methods to an existing model items 
	 */
	var bind = function(){
		model.changeProperty = changeProperty;
		model.newPage = newPage;
		
		if(model.pages){
			for(var i = 0; i < model.pages.length; i++){
				var page = model.pages[i];
				page.changeProperty = changeProperty;
				page.editCommand = editCommand;
				page.newElement = newElement;
				page.remove = removePageFactory(page);
				page.containingArray = model.pages;
				if(page.elements){
					for(var j = 0; j < page.elements.length; j++){
						var element = page.elements[j];
						element.page = page;
						element.changeProperty = changeProperty;
						element.editCommand = editCommand;
						element.remove = removeElementFactory(element);
						element.containingArray = page.elements;
					}
				} else {
					page.elements = [];
				}
				
				page.getElementByInstanceId = getByFactory('instanceId', page.elements);
				page.getElementById =  getByFactory('id', page.elements);
				page.getElementByPosition = getByFactory('position', page.elements);
				page.moveElement = moveElementInPage;
				page.move = movePage;
			}
		} else {
			model.pages = [];
		}
		
		model.getPageByInstanceId = getByFactory('instanceId', model.pages);
		model.getPageById = getByFactory('id', model.pages);
		model.getPageByPosition = getByFactory('position', model.pages);
	};
	
	var moveElementInPage = function(from, to){
		if(from != to){
			var element = this.elements[from];
			var start = 0, end = 0, add = 0;
			if(from < to){
				start = from + 1;
				end = to;
				add = -1;
			} else if(from > to) {
				start = to;
				end = from - 1;
				add = 1;
			}
			
			for(var i = start; i <= end; i++){
				var e = this.elements[i];
				e.position += add;
			}
			
			element.position = to;
			this.elements.sort(function(e1, e2){
				return e1.position - e2.position;
			});
			
			var command = newCommand('EDIT', this);
			command.attributes.push({"name": 'moveElement', "from"  : from.toString(), "to" : to.toString()});
			commands.push(command);
		}
		
	};
	
	var movePage = function(from, to){
		if(from != to){
			var page = this;
			var start = 0, end = 0, add = 0;
			if(from < to){
				start = from + 1;
				end = to;
				add = -1;
			} else if(from > to) {
				start = to;
				end = from - 1;
				add = 1;
			}
			
			for(var i = start; i <= end; i++){
				var p = model.pages[i];
				p.position += add;
			}
			
			page.position = to;
			model.pages.sort(function(p1, p2){
				return p1.position - p2.position;
			});
			
			var command = newCommand('EDIT', model);
			command.attributes.push({"name": 'movePage', "from"  : from.toString(), "to" : to.toString()});
			commands.push(command);
		}
	};
	
	var getByFactory = function(prop, items){
		return function(value){
			return getBy(prop, items, value);
		};
	};
	
	var removePageFactory = function(page){
		return function(){
			var removed = COMMON.removeFromArray(model.pages, function(e){
				return e === page;
			});
			if(removed){
				var command = newCommand('DELETE', removed);
				commands.push(command);
				// i = removed.position because the page has already been removed
				for(var i = removed.position; i< model.pages.length; i++){
			        var p = model.pages[i];
			        p.position = p.position - 1;
			    }
				
				for(var i = 0; i < model.pages.length; i++){
					 var p = model.pages[i];
					 if(p.flow){
						 if(p.flow.defaultTarget == page.instanceId){
							 p.flow.defaultTarget = null;
						 }
						 if(p.flow.targets){
							var targets = p.flow.targets;
							for(var j = 0; j < targets.length; j++){
								var target = targets[i];
								if(target.target == page.instanceId){
									targets[j] = 1;
								}
							}
							while(COMMON.removeFromArray(targets, function(e){
								return e === 1;
							}));
						 }
						
					 }
				}
			} else {
				acme.LOG.error('failed to remove page');
			}
		};
	};
	
	var removeElementFactory = function(element){
		return function(){
			var removed = COMMON.removeFromArray(this.page.elements, function(e){
				return e === element;
			});
			
			if(removed){
				var command = newElementCommand('DELETE', removed);
				commands.push(command);
				// i = removed.position because the element has already been removed
				for(var i = removed.position; i < this.page.elements.length; i++){
					var e = this.page.elements[i];
					e.position += - 1;
				}
				
				var page = this.page;
				var elements = page.elements;

				for(var i = 0; i < elements.length; i++){
					var e = elements[i];
					// Remove all the default value filters that depend on the removed element
					if(e.defaultValueFilters){
						for(var j = 0; j < e.defaultValueFilters.length ; j++){
							var f = e.defaultValueFilters[j];
							if(f.rightValue === element.instanceId){
								e.defaultValueFilters[j] = 1;
							}
						}
						while(COMMON.removeFromArray(e.defaultValueFilters, function(x){
							return x === 1;
						}));
					}
					
					if(e.itemListFilters){
						// Remove all the item list filters that depend on the removed element
						for(var j = 0; j < e.itemListFilters.length ; j++){
							var f = e.itemListFilsters[j];
							if(f.rightValue === element.instanceId){ // FIXME there's no instanceId if form wasn't saved yet
								e.itemListFilters[j] = 1;
							}
						}
						while(COMMON.removeFromArray(e.itemListFilters, function(x){
							return x === 1;
						}));
					}
					
					if(page.flow && page.flow.targets){
						// Remove all the flow elements that depend on the removed element
						var targets = page.flow.targets;
						for(var j = 0; j < targets.length; j++){
							var t = targets[j];
							if(t.elementId === element.instanceId){ // FIXME there's no instanceId if form wasn't saved yet
								targets[j] = 1;
							}
						}
						while(COMMON.removeFromArray(targets, function(x){
							return x === 1;
						}));
					}
				}
				
			} else {
				acme.LOG.error('failed to remove element');
			}
		};
	};
	
	var getBy = function(prop, items, val){
		for(var i = 0; i < items.length; i++){
			var item = items[i];
			if(item[prop] === val){
				return item;
			}
		}
		return null;
	};
	
	var editCommand = function(attributes){
		var command = null;
		var cmdType = 'EDIT';
		if(this.type === 'ELEMENT'){
			command = newElementCommand(cmdType, this);
		}else{
			command = newCommand(cmdType, this);
		}
		
		command.attributes.push(attributes); 
		commands.push(command);
	};
	
	/**
	 * Changes the value of the attribute with the given name.
	 * It also creates an edit command;
	 */
	var changeProperty = function(name, value){
		// 1st create the command
		// The command has to be created first because it needs the
		// unchanged model first
		var command = null;
		var cmdType = 'EDIT';
		if(this.type === 'ELEMENT'){
			command = newElementCommand(cmdType, this);
		}else{
			command = newCommand(cmdType, this);
		}
		
		// 2nd change the JS model
		if(this.type === 'ELEMENT'){
			// if an element is being edited, the change may refer to a 
			// change in the prototype
			if(name.substring(0, 6) === 'proto_'){
				this.proto[name.substring(6)] = value;
			} else {
				// if(this[name]){
				this[name] = value;
			//	}
			}
		} else {
			// if(this[name]){
				this[name] = value;
			// }
		}
		
		if (this.type === 'PAGE') {
			if(registeredPropertyChangeListeners){
				if(registeredPropertyChangeListeners.pageListeners){
					var listeners = registeredPropertyChangeListeners.pageListeners; 
					for(var l in listeners){
						if(listeners.hasOwnProperty(l)){
							listeners[l](name, value);
						}
					}
				}
			}
		}
		
		// If all went OK, push the command
		command.attributes.push({'name': name, 'value': (value !== null ? value.toString() : null)}); 
		commands.push(command);
	};
	
	//TODO consider making an ajax call to get the new page object and then bind it 
	// So, the server would return the template of a new Page
	var newPage = function(label, desiredPosition){
		// Java - PageModelDTO
		var page = {};
		page.id = 0;
		
		page.position = model.pages.length;
		page.label = label;
		page.elements = [];
		page.saveable = false;
		
		page.type = 'PAGE';		
		page.instanceId = newPageInstanceId();
		
		//This are the properties that will be visible in the properties view
		page.propertyMetadata = {};
		page.propertyMetadata.label = {"label" : I18N("web.editor.properties.label"), "editable" : true, "visible" : true, type: 'STRING'};
		page.propertyMetadata.saveable = {"label" : I18N("web.editor.properties.saveable"), "editable" : true, "visible" : true, type: 'BOOLEAN'};
		//FIXME repeated in bind()
		page.changeProperty = changeProperty;
		page.editCommand = editCommand;
		page.newElement = newElement;
		page.remove = removePageFactory(page);
		//FIXME repeated in bind()
		page.getElementByInstanceId = getByFactory('instanceId', page.elements);
		page.getElementById =  getByFactory('id', page.elements);
		page.getElementByPosition = getByFactory('position', page.elements);
		page.moveElement = moveElementInPage;
		page.move = movePage;
		
		page.containingArray = model.pages;
		this.pages.push(page);
		
		var command = newAddPageCommand(page);
		commands.push(command);
		// The page is added to the final, here we change its position if necessary
		var lastPagePosition = this.pages.length -1; 
		if(typeof(desiredPosition) !== "undefined" && desiredPosition !== null && desiredPosition != lastPagePosition){
			page.move(lastPagePosition, desiredPosition);
		}
		
		this.pages.sort(function(p1, p2){
			return p1.position - p2.position;
		});
		

		// #1919
		// If there is no default jump target defined for a page, then assume that the target is its next page
//		if(page.position > 0){
//			var position = page.position;
//			var previousPage = this.pages[position - 1];
//			if(!previousPage.saveable) {
//				if(!previousPage.flow){
//					previousPage.flow = {};
//				}
//				if(!previousPage.flow.defaultTarget) {
//					previousPage.flow.defaultTarget = page.instanceId;
//					previousPage.editCommand({"name": "defaultTarget", "value" : position});
//				}
//			}
//		}

		return page;
	};
	
	var newElement = function(prototype, position){
		var element = {};
		element.id = 0;
		element.position = position;
		element.instanceId = newElementInstanceId();
		element.tempInstanceId = true;
		element.type = 'ELEMENT';
		element.required = false;
		element.propertyMetadata = {};
		
		if(prototype.template && prototype.type && prototype.type != 'HEADLINE'){
			//FIXME shouldn't this be editable only if the prototype allows it?
			element.propertyMetadata.required = { "editable" : true, "visible" : true, "label" : I18N("web.editor.properties.required"), type : 'BOOLEAN'};
		}
		
		if(prototype.template){
			var newProto = $.extend({}, prototype);
			newProto.propertyMetadata = {};
			//deep copy!
			for(a in prototype.propertyMetadata){
			    if(prototype.propertyMetadata.hasOwnProperty(a)){
			    	newProto.propertyMetadata[a] = $.extend({}, prototype.propertyMetadata[a]);
			    }
			}

			newProto.template = false;
			newProto.embedded = true;
			element.proto = newProto;
		} else {
			element.proto = prototype;
		}
		
		element.page = this;
		element.containingArray = this.elements;
		element.remove = removeElementFactory(element);
		element.changeProperty = changeProperty;
		// change the position property of the elements
		for(var i = position; i < this.elements.length; i++){
			var e = this.elements[i];
			e.position++;
		}
		// add the new element to the array
		this.elements.push(element);
		// sort the array by the position property
		this.elements.sort(function(e1, e2){
			return e1.position - e2.position;
		});
		
		var command = newAddElementCommand(element);
		commands.push(command);
		
		return element;
	};
	
	/**
	 * 	Creates a new instance of a command given the command's type and the object.
	 * 
	 *  Sets the basic properties
	 */
	var newCommand = function(type, obj){
		var command = {};
		command.type = type;
		command.ref = {};
		if(obj){
			command.ref.id = obj.id;
			command.ref.type = obj.type;
			if(obj.position || obj.position === 0)
				command.ref.position = obj.position;
		}
		command.attributes = [];
		return command;
	};
	
	// A unique id for a Page
	var newPageInstanceId = function(){
		while(document.getElementById('page' + pCounter)){
			pCounter++;
		}
		return 'page' + pCounter;
	};
	
	
	var newAddPageCommand = function(page){
		var command = newCommand('ADD', page);
		var lang = acme.I18N_MANAGER.currentLanguage();
		command.attributes.push({"name" : 'label', "language" : lang, "value" : page.label});
		command.attributes.push({"name" : 'position', "value" : page.position});
		return command;
	};
	
	var newElementCommand = function(cmdType, element){
		var command = newCommand(cmdType, element);
		command.ref.container = {};
		var page = element.page;
		command.ref.container.id = page.id;
		command.ref.container.position = page.position;
		command.ref.container.type = page.type;
		return command;
	};
	
	var newAddElementCommand = function(element){
		var command  = newElementCommand('ADD', element); 
		command.attributes.push({"name" : 'position', value : element.position}); 
		command.attributes.push({"name" : 'prototypeId', value : element.proto.id});
		return command;
	};
	
	// A unique id for an element
	var newElementInstanceId = function(){
		while(document.getElementById('element' + eCounter)){
			eCounter++;
		}
		return 'element' + eCounter;
	};
	
	// Public Interface
	// --------------------------------------------------------//
	/**
	 * Saves the changes made on the form by sending the commands
	 * Refreshes the model once it has been successfully saved
	 */
	that.save = function(){
		var url ='/editor/form/save.ajax?formId=' + model.id;
		var dfd = $.Deferred();
		var promise = dfd.promise();
		if(that.unsavedChanges()){
			if(model.wasPublished){
				var title = I18N('web.editor.dialog.incrementversion.confirmation.title');
				var message = I18N('web.editor.dialog.incrementversion.confirmation.message');
				//FIXME decouple UI from model!
				$('<div></div>').html(message).dialog({
					buttons : [ 
							{
								text : I18N("web.editor.dialog.buttons.cancel"),
								click : function() {
									$(this).dialog('close');
								}
							},
							{
								text : I18N("web.editor.dialog.buttons.ok"),
								click : function(){
									$(this).dialog('close');
									$.when(postCommands(url)).then(function(response){
										dfd.resolve(response);
									});
								}
							}
						],
						autoOpen : false,
						modal : true,
						width: 600,
						height: "auto",
						title : title,
						closeOnEscape: false,
						close : function() {
							$(this).dialog('destroy');
							$(this).remove();
						}
				}).dialog('open');
			} else {
				// No version published
				$.when(postCommands(url)).then(function(response){
					dfd.resolve(response);
				});
			}
		} else {
			dfd.resolve({success : false, msg : {message : I18N('web.editor.noChanges'), title : I18N('web.editor.noChanges.Title')}, msgType : 'INFO'});
		}
		return promise;
	};
	
	that.saveAs = function(projectId, formLabel){
		var url = encodeURI('/editor/form/saveAs.ajax?formId=' + model.id + '&projectId=' + projectId  + '&formLabel=' + formLabel);
		return postCommands(url);
	};
	
	that.setLoadingSectionId = function(id){
		loadingSectionId = id;
	};
	
	that.publish = function(formId) {
		if(!that.unsavedChanges()){
			var dfd = $.Deferred();
			var publishPromise = dfd.promise();
			ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url += '/editor/publish.ajax';
			ajaxRequest.data = {formId : model.id};
			if(loadingSectionId){
				ajaxRequest.loadingSectionId = loadingSectionId;
			}
			ajaxRequest.success = function(response){
				publishSuccessCallback(dfd, response);
			};
			$.ajax(ajaxRequest);
			return publishPromise;
		} else {
			//FIXME decouple UI from model!
			var message = I18N('web.editor.publish.unsavedChanges');
			var title = I18N('web.editor.publish.unsavedChanges.title');
			$('<div></div>').html(message).dialog({
				buttons : [ 
						{
							text : I18N("web.editor.dialog.buttons.ok"),
							click : function(){
								$(this).dialog('close');
							}
						}
					],
					autoOpen : false,
					modal : true,
					width: 400,
					height: "auto",
					title : title,
					closeOnEscape: false,
					close : function() {
						$(this).dialog('destroy');
						$(this).remove();
					}
			}).dialog('open');
		}
	};
	//FIXME it's a copy paste of that.publish
	that.unpublish = function(formId) {
		var dfd = $.Deferred();
		var unpublishPromise = dfd.promise();
		ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/editor/unpublish.ajax';
		ajaxRequest.data = {formId : model.id};
		if(loadingSectionId){
			ajaxRequest.loadingSectionId = loadingSectionId;
		}
		ajaxRequest.success = function(response){
			publishSuccessCallback(dfd, response);
		};
		$.ajax(ajaxRequest);
		return unpublishPromise;
	};
	
	that.unsavedChanges = function(){
		return commands && commands.length > 0;
	};
	
	/**
	 * Loads the form with the given id.
	 * When the deferred is resolved the attribute success will mark if the request could be made or no.
	 * Teh attribute form of the resolved object will contain the full model. This is an enhanced MFForm
	 */
	that.load = function(formId){
		var dfd = $.Deferred();
		var loadPromise = dfd.promise();
		
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		//returns an object of type MFForm
		ajaxRequest.url += "/editor/form/model.ajax?formId=" + formId;
		ajaxRequest.type = "GET";
		
		ajaxRequest.success = function(response){
			if(response.success){
				that.form = model = response.obj;
				if(!selectedProject || !selectedProject.id){
					selectedProject.id = model.projectId;
				}
				init();
				bind();
				// mark unreachable pages
				that.unreachablePages();
				dfd.resolve({success:true, form:model});
			} else {
				dfd.resolve({success:false});
			}
		};
		$.ajax(ajaxRequest);
		
		return loadPromise;
	};
	
	that.getElementByInstanceId = function(instanceId){
		if(!instanceId){
			throw "No instanceId";
		}
		
		var element = null;
		for(var i = 0; i < model.pages.length; i++){
			var page = model.pages[i];
			element = page.getElementByInstanceId(instanceId);
			if(element){
				break;
			}
		}
		return element;
	};
	
	that.getPageByInstanceId = function(instanceId){
		if(!instanceId){
			throw "No instanceId";
		}
		
		return model.getPageByInstanceId(instanceId);
	};
	
	//This method is similar to listElements but returns an enhanced version of an MFElement. 
	//The enhanced version has id=element.instanceId and label=element.proto.label
	that.listMFElements=function(p_model,includeOutputElements){
		var list=[];
		var element;
		for(var i = 0; i < p_model.pages.length; i++){
			var page = p_model.pages[i];
			for(var j = 0; j < page.elements.length; j++){
				if(includeOutputElements || !page.elements[j].proto.outputOnly){
					element=acme.UTIL.clone(page.elements[j]);
					element.proto=acme.UTIL.clone(page.elements[j].proto);
					//augment the element object with the expected fields
					element.id=element.instanceId;
					element.label=element.proto.label;
					list[list.length]=element;
				}
			}
		}
		return list;
	};
	that.mapMFElements=function(p_model){
		var list=that.listMFElements(p_model),i=0,element;
		var map={};
		for(i=0;i<list.length;i++){
			map[list[i].instanceId]=list[i];
		}
		return map;
	};
	//The first parameter is the model on with this method will operate. If its null then the model loaded on this method will be used (backwards compatibility)
	//
	//Deprecated (use listMFElements)
	that.listElements = function(p_model,includeOutputElements){
		if(!p_model){
			//backwards compatibility. Use the internal model attribute
			p_model=model;
		}
	
		var all = [];
		for(var i = 0; i < p_model.pages.length; i++){
			var page = p_model.pages[i];
			for(var j = 0; j < page.elements.length; j++){
				if(includeOutputElements || !page.elements[j].proto.outputOnly){
					var element = page.elements[j];
					var e = {
						id : element.instanceId,
						label : element.proto.label,
						position : element.position,
						page : {
							position : page.position
						},
						proto:element.proto
					};
					all.push(e);
				}
				
			}
		}
		return all;
	};
	//This method returns a map from the elementInstandeId to its definition
	//deprecated (use mapMFElements)
	that.elementMap=function(p_model){
		var elements=that.listElements(p_model);
		var map={},i=0;
		for(i=0;i<elements.length;i++){
			map[elements[i].id]=elements[i];
		}
		return map;
	};
	that.listPages = function(p_model){
		if(!p_model){
			//backwards compatibility. Use the internal model attribute
			p_model=model;
		}
		var all = [];
		for(var i = 0; i < p_model.pages.length; i++){
			var page = p_model.pages[i];
			var p = {
				id : page.instanceId,
				label : page.label,
				position : page.position,
			};
			all.push(p);
		}
		
		return all;
	};
	
	that.hasFinalPage = function(){
		for(var i = 0; i < model.pages.length; i++){
			var page = model.pages[i];
			if(page.saveable){
				return true;
			}
		}
		return false;
	};
	
	that.getLastPage = function(){
		if(model.pages.length){
			return model.pages[model.pages.length - 1];
		}
		return null;
	};
	
	that.makeLastPageFinal = function(){
		if(model.pages.length){
			var page = model.pages[model.pages.length - 1];
			page.changeProperty("saveable", true);
			return true;
		}
		return false;
	};
	
	var markAsReachable = function(page){
		page.reachable = true;
		if(page.flow && !page.saveable){
			var flow = page.flow;
			if(flow.defaultTarget){
				var defaultTargetPage = model.getPageByInstanceId(flow.defaultTarget);
				markAsReachable(defaultTargetPage);
			}
			
			if(flow.targets){
				for(var i = 0; i < flow.targets.length; i++){
					var tid = flow.targets[i].target;
					var targetPage = model.getPageByInstanceId(tid);
					if(targetPage){
						markAsReachable(targetPage);
					}
				}
			}
			
		} else if(!page.saveable) {
			var nextPosition = page.position + 1;
			var nextPage = model.getPageByPosition(nextPosition);
			if(nextPage) {
				markAsReachable(nextPage);
			}
		}
		
	};
	
	that.unreachablePages = function(){
		if(model.pages.length){
			var page = model.pages[0];
			for(var i = 1; i < model.pages.length; i++){
				model.pages[i].reachable = false;
			}
			
			markAsReachable(page);
			var unreachables = [];
			
			for(var i = 1; i < model.pages.length ; i++){
				var page = model.pages[i];
				if(!page.reachable){
					unreachables.push(page);
				}
			}
			return unreachables;
		}
	};
	
	var registeredPropertyChangeListeners = {
			formListeners : {},
			pageListeners : {},
			elementListener : {}
	};
	
	that.registerPagePropertyChangeListener = function(name, func){
		registeredPropertyChangeListeners.pageListeners[name] = func;
	};
	
	that.unregisterPagePropertyChangeListener = function(name){
		registeredPropertyChangeListeners.pageListeners[name] = null;
	};
	
	that.moveToPage = function(element, newPage){
		var removed = COMMON.removeFromArray(element.page.elements, function(e){
			return e === element;
		});
		
		if(removed){
			var oldPosition = element.position;
			var oldPage = element.page;
						
			var command = newElementCommand('EDIT', element);
			command.attributes.push({"name": 'page', "value" : newPage.position.toString()});
			commands.push(command); 
			// the element is appended to the page i.e. added at the end
			element.position = newPage.elements.length;
			newPage.elements.push(element);
			
			// change the position of the remaining elements on the old page
			for(var i = oldPosition; i < oldPage.elements.length; i++){
				oldPage.elements[i].changeProperty("position", i);
			}
			element.page = newPage;
			return true;
		}
		return false;
	};
	
	return that;
});
