require([ "jquery", "acme", "scripting/scriptingManager"],  function($, acme, scripting){
	
	var scriptingManager = scripting.Manager();
	
	$(function(){
		$('#ajaxLoading').hide();
		$('#tabs').tabs();
		$('#tabs').tabs('select', 0);
		$('#saveDialog').dialog({ 
			autoOpen: false, 
			modal:true,
			title: 'Script\'s name',
			buttons: {
					Cancel : function() {
						$(this).dialog("close");
					},
					Save: function() {
						var scriptName = $('#scriptName').val();
						$('#scriptNameHidden').val(scriptName);
						scriptingManager.saveScript();
					}
				}
			});
				

		$('#saveButton').click(function(){
			$('#scriptName').val('');
			$('#saveDialog').dialog('open');
		});
		
		$('#executeButton').click(function(){
			scriptingManager.executeScript();
			return false;
		});
		
		$('a.scriptLink').click(function(){
			var id = $(this).attr('data');
			scriptingManager.loadScript(id);
			return false;
		});
		
	});
	
});