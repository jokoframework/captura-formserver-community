define(["jquery", "acme", "jquery-ui", "acme-ui", "constants", 
        "editor/model", "editor/common", "editor/properties", "editor/toolbox", "editor/main-area"], 
		function($, acme, $ui, acme_ui, constants, MODEL, COMMON, PROPERTIES, TOOLBOX, MAIN_AREA){
	
	//------------------------------------------//
	/**
	 * Here we do ONE time initializations
	 * 
	 * 1. They should not depend on html elements of the page being loaded
	 */
	MODEL.setLoadingSectionId('page_width');
	//------------------------------------------//
	
	/**
	 * Obj returned by this module
	 */
	var pkg = {};
	
	/**
	 * IDs of the html elements used in the editor 
	 */
	var IDS = {
		editorDiv : 'editorDiv',
		toolboxDiv : 'toolboxDiv',
		mainAreaDiv : 'mainAreaDiv',
		propertiesDiv : 'propertiesDiv',
	};
	
	/**
	 * I18N Promise. Resolved once all the labels used in the editor are 
	 * loaded.
	 */
	var i18nPromise = COMMON.loadI18NLabels();
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	/**
	 * Should be called on start to set module variables to their initial values, reset UI state
	 * and initialize other modules 
	 */
	var init = function(){
		COMMON.init();
	};
	
	var disablePublishButtons = function(){
		$('#publishButton').addClass('disabled');
		$('#publishLastVersionButton').addClass('disabled');
		$('#unpublishButton').addClass('disabled');
	};
	
	var enablePublishButtons = function(){
		$('#publishButton').removeClass('disabled');
		$('#publishLastVersionButton').removeClass('disabled');
		$('#unpublishButton').removeClass('disabled');
	};
	
	var renderEditor = function(formId){
		var e = $('#' + IDS.editorDiv);
		var toolboxDiv=$("#"+IDS.toolboxDiv);
		var mainDiv=$("#"+IDS.mainAreaDiv);
		var propertiesDiv=$("#"+IDS.propertiesDiv);
		
		TOOLBOX.setup(toolboxDiv);
		MAIN_AREA.setup(mainDiv);
		PROPERTIES.setup(propertiesDiv);
		if(acme.AUTHORIZATION_MANAGER.hasAuthorizationOnForm(formId,"form.publish")) {
			enablePublishButtons();
		} else {
			disablePublishButtons();
		}
		e.show();
	};
	
	
	
	var openForm = function(formId){
		var dfd = $.Deferred();
		if(formId){
			var promise = MODEL.load(formId);
			$.when(promise, i18nPromise).then(function(){
				renderEditor(formId);
				$('#backWfLink').click(function(){
					acme.LAUNCHER.launch(constants.LAUNCHER_IDS.form_edit, {formId:MODEL.form.id});
				});
				// start is completed, resolved.
				dfd.resolve();
			});
		} else {
			// start is completed, rejected
			dfd.reject();
			throw "Invalid formId";
		}
		return dfd.promise();
	};
	
	// PUBLIC INTERFACE
	pkg.start = function(formId){
		init();
		var promise = openForm(formId);
		return promise;
	};
	
	pkg.stop = function(){
		MAIN_AREA.destroy();
	};
	
	pkg.preUnload = function(){
		var dfd = $.Deferred();
		var promise = dfd.promise();
		if(MODEL.unsavedChanges()){
			var doAction = function(){
				dfd.resolve({unload : true});
			};
			
			var cancelAction = function(){
				dfd.resolve({unload : false});
			};
			
			acme_ui.confirmation(I18N('web.editor.exit.unsaved.title'), I18N('web.editor.exit.unsaved'), doAction, cancelAction);
		} else {
			dfd.resolve({unload : true});
		}
		
		return promise;
	};
	
	return pkg;
});
