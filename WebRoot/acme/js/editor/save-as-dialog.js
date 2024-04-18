/**
 * WF editor "save as" dialog
 */
define(["jquery", "jquery-ui", "acme", "acme-ui", "editor/model"], function($, $ui,  acme, acme_ui, MODEL){
	
	var that = {};
	
	var IDs = {
		dialog : 'saveAsDialog',
		projectSelect : 'projectSelect',
		formInput : 'formName',
		messageDiv : 'formDialogMessage'
	};
	
	var dialog = null;
	
	var saveCallback = null;
	
	var I18N = acme.I18N_MANAGER.getMessage;
	
	var populateProjectSelect = function() {
		var projectSelect = dialog.find('#' + IDs.projectSelect);
		projectSelect.html('');
		var projectsPromise = acme_ui.UTIL.downloadList({url:"/editor/projects/list.ajax"});
		$.when(projectsPromise).then(function(projectsResponse){
			acme_ui.HTML_BUILDER.drawSelectOptions(projectSelect, projectsResponse.obj);
			projectSelect.val(MODEL.form.projectId);
		});
	};
	
	var okClicked = function(){
		var formInput = dialog.find('#' + IDs.formInput);
		var projectSelect = dialog.find('#' + IDs.projectSelect);
		var projectId = projectSelect.val();
		if(projectId){
			var formName = $.trim(formInput.val());
			if(formName){
				var savePromise = MODEL.saveAs(projectId, formName);
				$.when(savePromise).then(function(response){
					saveCallback(response);
				});
				$(this).dialog("close");
			} else {
				acme_ui.HTML_BUILDER.alertError(IDs.messageDiv, I18N('web.generic.error'), I18N('web.editor.dialog.form.invalidName'));
				return;
			}
		} 
	};
	
	// Public Interface
	// --------------------------------------------------------//
	that.init = function(saveFunc){
		if(!$.isFunction(saveFunc)){
			throw "Invalid callback for save as dialog";
		}
		saveCallback = saveFunc;
	
		dialog = $('#' + IDs.dialog).dialog({
			buttons : [ 
				{
					id: "formDialog_button_cancel",
					text : I18N("web.editor.dialog.buttons.cancel"),
					click : function() {
						$('#formDialogMessage').html('');
						$(this).dialog("close");
					}
				},
				{
					id: "formDialog_button_ok",
					text : I18N("web.editor.dialog.buttons.ok"),
					click : function(){
						okClicked.apply(this);
					}
				}
			],
			autoOpen : false,
			modal : true,
			width: 400,
			height: "auto",
			open : function(){
				populateProjectSelect();
			}
		});
	};
	
	that.wasInitialized = function(){
		return dialog != null;
	};
	
	that.reset = function(){
		var formInput = dialog.find('#' + IDs.formInput );
		var projectSelect = dialog.find('#' + IDs.projectSelect);
		formInput.val('');
		projectSelect.val('');
		dialog.dialog("option", "title", I18N('web.editor.dialog.title.saveAs'));
	};
	
	that.open = function(){
		if(!$.isFunction(saveCallback)){
			throw "Invalid callback for save as dialog";
		}
		dialog.dialog('open');
	};
	
	that.destroy = function(){
		if(dialog){
			dialog.dialog('destroy');
			dialog.remove();
			dialog = null;
		}
	};
	
	return that;
});