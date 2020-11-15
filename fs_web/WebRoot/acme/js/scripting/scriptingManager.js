define([ "jquery", "acme", "jquery-ui"],  function($, acme, jqueryui){
	
	var pkg = {};
	
	pkg.Manager = function(){
		var that = {};
		
		var enableButtons = function(){
			$('#executeButton').attr('disabled', null);
			$('#saveButton').attr('disabled', null);
		};
		
		var disableButtons = function(){
			$('#executeButton').attr('disabled', 'disabled');
			$('#saveButton').attr('disabled', 'disabled');
		};
		
		var ajaxLoading = function(){
			disableButtons();
			$('#ajaxLoading').show();
		};
		
		var ajaxDone = function(){
			enableButtons();
			$('#ajaxLoading').hide();
		};
		
		var saveScriptAjaxCallBack = function(res){
			ajaxDone();
			if(res.success) {
				$('#saveDialog').dialog('close');
				var scripts = res.content.scripts;
				if(scripts){
					var html='<ul>';
					for (var i = 0; i < scripts.length; i++) {
						var script = scripts[i];
						html += '<li><a class="scriptLink" href="javascript:void(0)" data="' + script.id + '">' + script.name + '</a></li>';
					}
					html+='</ul>';
					$('#scriptList').html(html);
					$('a.scriptLink').click(function(){
						var id = $(this).attr('data');
						that.loadScript(id);
						return false;
					});
				}
			}else{
				alert('Error saving the script:' + res.message);
			}
			enableButtons();
		};
		
		var executeScriptAjaxCallBack = function(res){
			ajaxDone();
			if(res.success){
				$('#out').html(res.content.out);
				$('#tabs').tabs('select', 1);
			}else{
				alert('Error saving the script');
			}
		};
		
		var loadScriptAjaxCallBack = function(res){
			if(res.success) {
				MF.Scripting.editor.setCode(res.content.script.script);
				$('#tabs').tabs('select', 0);
			}else{
				alert('Error loading the script');
			}
		};
		
		var formSubmit = function(url, callback){
			ajaxLoading();
			var code = MF.Scripting.editor.getCode();
			$('#script').val(code);
			var data = $('#scriptForm').serialize();
			var ajaxRequest = acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url = url;
			ajaxRequest.data = data;
			ajaxRequest.success = callback;
			$.ajax(ajaxRequest);
		};
		
		
		
		that.loadScript = function(id){
			var ajaxRequest =  acme.AJAX_FACTORY.newInstance();
			ajaxRequest.url = 'get_script.ajax';
			ajaxRequest.data = 'id=' + id;
			ajaxRequest.success = loadScriptAjaxCallBack;
			$.ajax(ajaxRequest);
		};
		
		that.executeScript = function(){
			formSubmit('execute_script.ajax', executeScriptAjaxCallBack);
		};
		
		
		that.saveScript = function(){
			formSubmit('save_script.ajax', saveScriptAjaxCallBack);
		};
		
		return that;
	};
	return pkg;
});