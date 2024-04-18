define([ "jquery", "acme","jquery-ui", "acme-ui"], function($, acme,$ui,acme_ui) {
	
	
	var pkg={};
	
	
	var aferAppCreated;
	
	var openCreateAppPopup=function(successCallback){
		$("#createAppPopup").dialog("open");
		$('#owner').empty();
		var promise = acme_ui.UTIL.downloadList({url:'/admin/users/allActive.ajax'});
		$.when(promise).then(function(response){
			if(response.success){
				var users = response.obj;
				for(var i = 0; i < users.length; i++){
					var u = users[i];
					var fullName = u.firstName + " " + u.lastName + " (" + u.mail + ")" ;
					var option = acme_ui.HTML_BUILDER.createSelectOption(u.id, fullName);
					$('#owner').append(option);
				}
			}
		});
		aferAppCreated=successCallback;
	};
	
	var createApp=function(){
		var appName = $("#applicationName").val();
		var ownerId = $('#owner').val();
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/admin/createApp.ajax';
		ajaxRequest.data={ 'appName' : appName, 'ownerId' : ownerId};
		//ajaxRequest.loadingSectionId = 'formTabs';
		ajaxRequest.success = function(obj){
			if(obj.success) {
				acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
				if(aferAppCreated&&$.isFunction(aferAppCreated)){
					aferAppCreated();
				}
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
			$("#createAppPopup").dialog("close");
		};
		$.ajax(ajaxRequest);
		
	};
	var reloadSystemParameters=function(){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/admin/reloadParameters.ajax';
		return $.ajax(ajaxRequest);
		
	};
	
	var reloadi18n=function(){
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/admin/reloadi18n.ajax';
		return $.ajax(ajaxRequest);
		
	};
	
	//Server info - About popup
	var openServerInfoPopup=function(successCallback){
		$("#serverInfoPopup").dialog("open");
		$("#showServerInfoTable").empty();
		acme_ui.HTML_BUILDER.drawTable("showServerInfoTable", {url:'/admin/showServerInfo.ajax'}, [{'label':'Property','name':'name'}, {'label': 'Value', 'name': 'value'}]);
	};
	
	pkg.init=function(){
		$("#createAppPopup").dialog({
				autoOpen : false,
				modal : true,width:500});
		
		$("#createAppButton").click(createApp);
		
		//server info popup
		$("#serverInfoPopup").dialog({
			autoOpen : false,
			modal : true,width:700});
	
		// Application options popup.
		$("#appMoreOptionsPopup").dialog({
				autoOpen : false,
				modal : true,width:500});	
	};
	
	pkg.openCreateAppPopup=openCreateAppPopup;
	pkg.reloadSystemParameters=reloadSystemParameters;
	pkg.reloadi18n=reloadi18n;
	pkg.openServerInfoPopup=openServerInfoPopup;
	return pkg;
});