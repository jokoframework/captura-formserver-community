define(["jquery", "jquery-ui", "acme", "acme-ui", "editor/model", "editor/properties", "editor/toolbox", "editor/common", "editor/save-as-dialog"], 
		function($, $ui, acme, acme_ui, MODEL, PROPERTIES, TOOLBOX, COMMON, SAVE_AS_DIALOG){

	var that = {};
	
	var IDS = {
		formDiv : 'formDiv',
		publicationDiv:'publicationDiv',
		versionPublished:'versionPublished',
		lastStoredVersion:'lastStoredVersion',
		formLabel:'formLabel',
		mainAreaDiv : 'mainAreaDiv',
		locationLink : 'locationLink'
	};
	
	var formDiv = null;
	
	var mainAreaDiv = null;
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var markUnreachablePages = function(){
		var unreachablePages = MODEL.unreachablePages();
		for(var i = 0; i < MODEL.form.pages.length; i++){
			var page = MODEL.form.pages[i];
			$('div#' + page.instanceId).removeClass("unreachable");
			$('div#' + page.instanceId + " div.pageTitleDiv a.warning_sign").remove();
		}
		
		if(unreachablePages != null && unreachablePages.length){
			for(var i = 0; i < unreachablePages.length; i++){
				var page = unreachablePages[i];
				
				$('div#' + page.instanceId).addClass("unreachable");
				$('div#' + page.instanceId + " div.pageTitleDiv").prepend(unreachableIconStr());
			}

			acme_ui.HTML_BUILDER.notifyError(I18N('web.editor.unreachablePages'), I18N('web.editor.unreachablePages.explanation'));
		}
	};
	
	var pagePropertyChangeListener = function(name, value){
		if(name === 'saveable'){
			markUnreachablePages();
		}
	};
	
	/**
	 * Given the model stored in the var "model", render the form in the mainLayer
	 */
	var renderForm = function(){
		var model = MODEL.form;
		if(model){
			var isPublished = model.published;
			var versionPublished = model.versionPublished;
			var currentVersion = model.version;
			displayPublishButtons(isPublished, versionPublished, currentVersion);
			formDiv.html('');
			//formDiv.append('<h4 id="formLabel">' + model.label +'</h4>');
			//formDiv.append('<h5 id="version">' + I18N('web.generic.version')  + ' : ' + model.version + '</h5>');
			
			versionPublished = versionPublished  ? versionPublished : I18N('web.generic.none') ;
			$("#"+IDS.versionPublished).val(versionPublished);
			$("#"+IDS.lastStoredVersion).val(currentVersion);
			$("#"+IDS.formLabel).html(model.label);
			var locationLink = $("#"+IDS.locationLink);
			if(model.provideLocation){
				locationLink.addClass("locationOn").removeClass("locationOff").attr("title", I18N("web.editor.disable_location"));
			} else {
				locationLink.addClass("locationOff").removeClass("locationOn").attr("title", I18N("web.editor.enable_location"));
			}
			//formDiv.append('<h5 id="versionPublished">' + I18N('web.generic.versionPublished')  + ' : ' + versionPublished +'</h5>');
			
			formDiv.append('<div id="formMessage"></div>');
			$('<div></div>', {"id" : 'pagesDiv', style : 'position:relative'}).appendTo(formDiv);
			//position must be relative because otherwise we have the following problem
			// http://stackoverflow.com/questions/2842432/jquery-position-isnt-returning-offset-relative-to-parent
			// I need to know the position of the pages relative to the pageContainer
			// related to 
			formDiv.click(function(e){
				hideSubcontextMenus();
				PROPERTIES.show({type : model.type, item:model});
				mainAreaDiv.find(".selected").removeClass("selected");
				formDiv.addClass("selected");
				e.stopPropagation();
			});
			
			if(model.pages && model.pages.length){
				var pages =  model.pages;
				var pagesCount = model.pages.length;
				for(var i = 0; i < pagesCount; i++){
					var page = pages[i];
					var pageDiv = addPageDiv(page);
					addElementsToPage(pageDiv, page);
				}
			} else {
				var page = model.newPage(I18N("web.editor.form.newPage.label"));
				page.reachable = true;
				addPageDiv(page);
				formDiv.children('div#pagesDiv').prepend('<div id="startPageDiv"></div>');
				acme_ui.HTML_BUILDER.notifyInfo(I18N('web.editor.info'), I18N('web.editor.form.automatic.newpage'));
			}
		}
		markUnreachablePages();
		MODEL.registerPagePropertyChangeListener('main-area', pagePropertyChangeListener);
	};
	
	var scrollToPage = function(pageDiv){
		var pagesDiv = $("#pagesDiv");
		var top = pageDiv.position().top - 200 + pagesDiv.scrollTop();
		pagesDiv.animate({ scrollTop: top }, 600);
	};
	
	var elementUIClickFactory = function(element, elementUI){
		return function(e){
			hideSubcontextMenus();
			PROPERTIES.show({type : element.type, item:element}); 
			mainAreaDiv.find(".selected").removeClass("selected");
			elementUI.addClass("selected");
			e.stopPropagation();
		};
	};

	var moveToPageClickFactory = function(element, newPage){
		return function(){
			var moved = MODEL.moveToPage(element, newPage);			
			if(moved){
				var elementUI = $('#' + element.instanceId);
				elementUI.appendTo('#' + newPage.instanceId + ' ul');
				elementUI.unbind("contextmenu");
				elementUI.bind("contextmenu", elementUIContextMenuFactory(element, newPage));
			} else {
				alert('Could not move element');
			}
		};
	};
	
	var hideSubcontextMenus = function (){
		$('.subContextMenu').remove();
		$('.contextMenu').hide();
	};
	
	var elementUIContextMenuFactory = function(element, page){
		return function(e){
			hideSubcontextMenus();
			//Unbind old handlers
			$('#elementContextMenu div#moveToPage').unbind();
			//position the context menu
			var top = e.pageY;
			var left = e.pageX;
			$('#elementContextMenu').css({
		        top: top + 'px',
		        left: left + 'px'
		    }).show();
			//items
			$('#elementContextMenu div#moveToPage').mouseover(function(){
				var div = $('#pageListDiv');
				if(!div.length){
					div = $('<div></div>', {id : "pageListDiv"}).appendTo('body');
					div.addClass('subContextMenu');
					div.css({
						position: 'absolute',
						background: '#F5F5F5',
						top : (top + 10) +'px',
						left : (left + 150) + 'px'
					});
					var pages = MODEL.form.pages;
					for(var i = 0; i < pages.length; i++){
						var p = pages[i];
						if(p.instanceId != page.instanceId){
							var subContextMenuItemDiv = $('<div></div>');
							subContextMenuItemDiv.addClass('subContextMenuItem');
							subContextMenuItemDiv.css({padding: '4px', "margin-left" : '4px'});
							subContextMenuItemDiv.html(p.label);
							subContextMenuItemDiv.click(moveToPageClickFactory(element, p));
							div.append(subContextMenuItemDiv);
						}
					}
				}
			});
			
			return false;
		};
	};

	/**
	 * This function is called to add elements from an existing model 
	 * to the page
	 */
	var addElementsToPage = function(pageDiv, page){
		var elements = page.elements;
		var ul = pageDiv.find('ul.sortable');
		for(var i = 0; i < elements.length; i++){
			var element = elements[i];
			var elementUI = $('<li></li>', {id: element.instanceId});
			elementUI.addClass("ui-state-default");
			elementUI.addClass("item");
			
			var deleteSpan = elementUIDeleteSpan(element, elementUI);
			elementUI.html(elementUIHTML(element.proto, element.proto.label));
			elementUI.append(deleteSpan);
			elementUI.data('prototypeId', element.proto.id);
			elementUI.data('elementId', element.id);
			ul.append(elementUI);
			elementUI.click(elementUIClickFactory(element, elementUI));
			elementUI.bind("contextmenu", elementUIContextMenuFactory(element, page));
		}
	};
	
	var elementUIDeleteSpan = function(element, elementUI){
		var span = $('<span></span>', {	
				"id" : element.instanceId + 'DeleteButton', 
				//"class" : 'ui-icon ui-icon-trash', 
				"style" : 'position:absolute; top:2px; right:2px; cursor: pointer',
				"click" : function(){
					elementUI.remove();
					element.remove();
					// If the element is selected it should be 
					// removed from the properties table. See #3028
					if(elementUI.hasClass('selected')){
						var model = MODEL.form; 
						PROPERTIES.show({type : model.type, item:model});
					}
					return false;
				}
			}
		);
		span.html('<i class="icon-remove"></i>');
		return span;
	};
	
	var setupMainUpperToolbar = function(div){
		//div.append('<a href="javascript:void(0)" class="btn btn-primary" id="newPageButton">' + I18N('web.editor.mainlayer.button.newPage') + '</a>' + '&nbsp;' );
		
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(MODEL.form.id,"form.publish")) {
			
//			div.append('<a href="javascript:void(0)" style="display:none" id="unpublishButton" class="btn btn-primary">' + I18N('web.generic.unpublish') + '</a>' + '&nbsp;');
//			div.append('<a href="javascript:void(0)" style="display:none" id="publishLastVersionButton" class="btn btn-primary">' + I18N('web.generic.publishLastVersion') + '</a>' + '&nbsp;');
//			div.append('<a href="javascript:void(0)" style="display:none" id="publishButton" class="btn btn-primary">' + I18N('web.generic.publish') + '</a>' + '&nbsp;');
		}
		// Save and Save As buttons
		/*
		div.append('<div class="formActionsRight">' + 
				'<a href="javascript:void(0)" class="btn btn-primary" id="saveFormLink">' + I18N('web.editor.mainbuttons.save') + '</a>' +
				'&nbsp;' +
				'<a href="javascript:void(0)" class="btn btn-primary" id="saveAsFormLink">' + I18N('web.editor.mainbuttons.saveAs') + '</a>' +
				'&nbsp;' +
				'</div>');
		*/
		$('#newPageButton').click(function(){
			page = MODEL.form.newPage(I18N("web.editor.form.newPage.label"));
			MODEL.unreachablePages();
			var pageDiv = addPageDiv(page);
			scrollToPage(pageDiv);
			pageDiv.trigger('click');
		});
		
		// publish permissions
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(MODEL.form.id,"form.publish")) {
			var publishFunction = function(unpublish) {
				var promise = null;
				if(unpublish){
					promise = MODEL.unpublish();
				} else {
					promise = MODEL.publish();
				}
				if(promise){
					$.when(promise).then(function(response){
						if(response.success){
							acme_ui.HTML_BUILDER.notifySuccess(response.msg.title, response.msg.message);
							var status = response.publishStatus;
							displayPublishButtons(status.isPublished, status.versionPublished, status.currentVersion);
	
							$("#" + IDS.lastStoredVersion).val(status.currentVersion);
							var versionPublished = !status.versionPublished ? I18N("web.generic.none") : status.versionPublished;
							$("#" + IDS.versionPublished).val(versionPublished);
						} else {
							acme_ui.HTML_BUILDER.notifyError(response.msg.title, response.msg.message);
						}
					});
				}
			};
			$('#publishButton').click(function() { publishFunction(false); });
			$('#publishLastVersionButton').click(function() { publishFunction(false); });
			$('#unpublishButton').click(function(){ publishFunction(true); });
		}
		
		var save = function() {
			var savePromise = MODEL.save();
			$.when(savePromise).then(function(response){
				if(response.success){
					acme_ui.HTML_BUILDER.notifySuccess(response.msg.title, response.msg.message);
				} else {
					if(response.msgType && response.msgType == 'INFO'){
						acme_ui.HTML_BUILDER.notifyInfo(response.msg.title, response.msg.message);
					} else {
						acme_ui.HTML_BUILDER.notifyError(response.msg.title, response.msg.message);
					}
				}
				renderForm();
			});
		};
		
		$('#saveFormLink').click(function(){
			if(MODEL.hasFinalPage()){
				 save();
			} else {
				var lastPage = MODEL.getLastPage();
				if(lastPage != null) {
					var dialogParams = {};
					var html = I18N('web.editor.save.nofinalPage.message', [lastPage.label]);
					dialogParams.title = I18N('web.editor.save.nofinalPage.title');
					dialogParams.buttons = [
					   {
						   text : I18N('web.generic.yes'),
						   click : function(){
							   MODEL.makeLastPageFinal();
							   save();
							   $(this).dialog('close');
						   }
					   },
					   {
						   text : I18N('web.generic.no'),
						   click : function(){
							   save();
							   $(this).dialog('close');
						   }
						   
					   }
					];
					
					acme_ui.dialog(html, dialogParams);
				} else {
					// we are saving an empty form
					save();
				}
			}
		});
		
		$('#' + IDS.locationLink).click(function(){
			var model = MODEL.form;
			if(model.provideLocation){
				model.changeProperty("provideLocation", false);
				$(this).addClass("locationOff").removeClass("locationOn").attr("title", I18N('web.editor.enable_location')); 
			} else {
				model.changeProperty("provideLocation", true);
				$(this).addClass("locationOn").removeClass("locationOff").attr("title", I18N('web.editor.disable_location')); 
			}
			$('input#property_provideLocation:checkbox').attr("checked", model.provideLocation);
		});
		
		// #2544
		var projectsPromise = acme_ui.UTIL.downloadList({url:"/editor/projects/list.ajax"});
		$.when(projectsPromise).then(function(response){
			var obj = response.obj;
			if(obj && obj.length){
				$('#saveAsFormLink').click(function(){	
					SAVE_AS_DIALOG.reset();
					SAVE_AS_DIALOG.open();
				});
			} else {
				$('#saveAsFormLink').hide();
			}
		});
	};
	
	var displayPublishButtons = function(isPublished, versionPublished, currentVersion){
		var hasAuthorizationtoPublish=acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(MODEL.form.id,"form.publish");
		if(hasAuthorizationtoPublish){
			if(isPublished) {
				if(versionPublished === currentVersion){
					$("#publishButton").hide();
					$("#publishLastVersionButton").hide();
					$("#unpublishButton").show();
				} else {
					$("#publishButton").hide();
					$("#publishLastVersionButton").show();
					$("#unpublishButton").show();
				}
			} else {
				
				$("#publishButton").show();
				$("#publishLastVersionButton").hide();
				$("#unpublishButton").hide();
			}
		}else{
			//the user doesn't have authorization to publish, so we shouldn't show to him any button
			$("#publishButton").hide();
			$("#publishLastVersionButton").hide();
			$("#unpublishButton").hide();
		}
	};
	
	// returns the position of the new element that was dropped
	var positionOfNewElement = function(htmlList){
		var elementsUI =  htmlList.children();
		for(var i = 0; i < elementsUI.length; i++){
			var e = elementsUI[i];
			if($(e).is('div')){
				return i;
			}
		}
		throw "An error ocurred";
	};
	
	var positionOfElementByInstanceId = function(htmlList, instanceId){
		var elementsUI =  htmlList.children();
		for(var i = 0; i < elementsUI.length; i++){
			var e = elementsUI[i];
			if($(e).attr('id') == instanceId){
				return i;
			}
		}
	};
	
	/**
	 * This function is called when an item is dropped on a page
	 * 
	 * The page is updated to show the item that was dropped.
	 */
	var updatePageDiv = function(event, ui, page, pageDiv){
		acme.LOG.debug('update');
		var htmlList = pageDiv.children('ul');
		if(ui.item.is('div')){
			var position = positionOfNewElement(htmlList);
			var clickedPrototype = TOOLBOX.clickedPrototype();
			var element = page.newElement(clickedPrototype, position);
			
			var elementUI = $('<li></li>', {"id" : element.instanceId});
			elementUI.addClass("ui-state-default");
			elementUI.addClass("item");
			
			elementUI.html(elementUIHTML(clickedPrototype, clickedPrototype.label));
			elementUI.data('prototypeId', clickedPrototype.id);
			elementUI.data('elementId', element.id);
			ui.item.replaceWith(elementUI);
			
			var deleteSpan = elementUIDeleteSpan(element, elementUI);
			
			elementUI.append(deleteSpan);
			elementUI.click(elementUIClickFactory(element, elementUI));
			elementUI.bind("contextmenu", elementUIContextMenuFactory(element, page));
			elementUI.trigger('click');
		} else {
			var elementUI = $(ui.item);
			var instanceId = elementUI.attr('id');
			var newPosition = positionOfElementByInstanceId(htmlList, instanceId);
			var element = page.getElementByInstanceId(instanceId);
			if(element != null){
				page.moveElement(element.position, newPosition);
			}
		}
	};
	
	var elementUIHTML = function(proto, label){
		var iLink = COMMON.iconLink(proto); 
		return iLink + '&nbsp;' + '<span class="word-break" id="liLabel">'  + label + '</span>';
	};
	
	var unreachableIconStr = function() {
		return '<a href="javascript:void(0)" class="glyphicons warning_sign" title="' + I18N('web.editor.page.warning.unreachable') + '"><i></i></a>';
	};
	
	var addPageDiv = function(page, insertOption, refPageId){
		var pageLabel =  page.label;
		var pagesDiv = formDiv.children("div#pagesDiv");
		
		var pageDiv = $('<div></div>', {"id" : page.instanceId, "class" : "page"});
		if(page.saveable){
			pageDiv.addClass("saveable");
		}
		
		if(insertOption == null){
			pageDiv.appendTo(pagesDiv);
		} else if(insertOption =='BEFORE'){
			$('#' + refPageId).before(pageDiv);
		} else if(insertOption == 'AFTER'){
			$('#' + refPageId).after(pageDiv);
		}
		
		var titleDiv=$("<div></div>",{"class":"pageTitleDiv"}).appendTo(pageDiv);
		
		if(!page.reachable){
			pageDiv.addClass("unreachable");
			titleDiv.prepend(unreachableIconStr());
		}
		
		var titleSpan = $('<span></span>', {
				"id" : page.instanceId + 'Label',
				"class" : 'pageLabel' }
		).html(pageLabel ? pageLabel : "");
		
		var deleteSpan = $('<span></span>', {	
				"id" : page.instanceId + 'DeleteButton', 
				// "class" : 'ui-icon ui-icon-trash', 
				"class" : 'pageDelete',
				"click" : function(){
					pageDiv.remove();
					page.remove();
					//Remove if it is being shown in properties
					if(pageDiv.hasClass('selected')){
						var model = MODEL.form; 
						PROPERTIES.show({type : model.type, item:model});
					}
					return false;
				}
			}
		);

		deleteSpan.html('<i class="icon-remove"></i>');
		titleSpan.appendTo(titleDiv);
		deleteSpan.appendTo(titleDiv);
		
		pageDiv.click(function(e){
			hideSubcontextMenus();
			PROPERTIES.show({type: page.type, item:page});
			mainAreaDiv.find(".selected").removeClass("selected");
			pageDiv.addClass("selected");
			e.stopPropagation();
		});
		
		pageDiv.bind("contextmenu", pageContextMenuFactory(page, pageDiv));
		
		var ulSortable = $('<ul></ul>', {id:"ul_" + page.instanceId, "class": "sortable pageElementDiv", style: "overflow:auto"});
		
		ulSortable.appendTo(pageDiv);
		ulSortable.sortable({
			update : function(event, ui) {
				updatePageDiv.apply(this, [event, ui, page, pageDiv]);
			},
			delay : 500
		});
		ulSortable.disableSelection();
		return pageDiv;
	};
	
	var setupSaveAsDialog = function(){
		SAVE_AS_DIALOG.init(function(response){
			if(response.success){
				acme_ui.HTML_BUILDER.notifySuccess(response.msg.title, response.msg.message);
			} else {
				acme_ui.HTML_BUILDER.notifyError(response.msg.title, response.msg.message);
			}
			renderForm(); // FIXME is it OK to re-render the Form?
		});
	};
	
	var pageContextMenuFactory = function(page, pageDiv){
		return function(e){
			hideSubcontextMenus();
			PROPERTIES.show({type: page.type, item:page});
			mainAreaDiv.find(".selected").removeClass("selected");
			pageDiv.addClass("selected");
			
			//Unbind old handlers
			$('#pageContextMenu div#addPageBefore').unbind();
			$('#pageContextMenu div#addPageAfter').unbind();
			//position the context menu
			var top = e.pageY;
			var left = e.pageX;
			$('#pageContextMenu').css({
		        top: top + 'px',
		        left: left + 'px'
		    }).show();
			//items
			$('#pageContextMenu div#addPageBefore').click(function(){
				var newPage = MODEL.form.newPage(I18N("web.editor.form.newPage.label"), page.position);
				MODEL.unreachablePages();
				var newPageDiv = addPageDiv(newPage, 'BEFORE', page.instanceId);
				newPageDiv.trigger('click');
			});
			
			$('#pageContextMenu div#addPageAfter').click(function(){
				var newPage = MODEL.form.newPage(I18N("web.editor.form.newPage.label"), (page.position + 1));
				MODEL.unreachablePages();
				var newPageDiv = addPageDiv(newPage, 'AFTER', page.instanceId);
				newPageDiv.trigger('click');
			});
			
			
			return false;
		};
	};

	// Public Interface
	// --------------------------------------------------------//
	that.setup = function(div){
		setupMainUpperToolbar(div);
		// click anywhere to hide any context menu
		$(document).click(function(){
			hideSubcontextMenus();
		});
		
		formDiv = $('<div></div>', {id : IDS.formDiv}).appendTo(div);
		mainAreaDiv = $('#mainAreaDiv');
		setupSaveAsDialog();
		renderForm();
	};
	
	that.destroy = function(){
		SAVE_AS_DIALOG.destroy();
		$(document).unbind('click');
		// Removes all the divs used for any dialog. 
		$('.editorDialog').remove();
		// This is required because the jquery-ui dialog widget 
		// makes this on destroy: self.element.appendTo(document.body);
		// So, the div used for the dialog becomes garbage because it
		// won't be removed by ACME or anyone else.
		// Even more, it might cause an issue when entering the page again
		// as there will be 2 divs with the same id.
		
	};
	
	return that;
	
});