<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<h3><span id="legend">${i18n('admin.cruds.role.permission.title')}</span></h3> 
		</div>	
	</div>
	<#-- Fields of the page-->
	<div class="row-fluid">	
		<form class="form-horizontal">
  				<fieldset>
  					<div class="control-group">
      					<label class="control-label" for="role_name">${i18n('admin.cruds.role.permission.role')}</label>
      					<div class="controls">
        					<input type="text" class="disabled" id="role_name" disabled="disabled">
       					</div>
    				</div>
    				<div class="control-group">
      					<label class="control-label" for="role_description">${i18n('admin.cruds.role.permission.description')}</label>
      					<div class="controls">
        					<input type="text" class="input-xlarge disabled" id="role_description" disabled="disabled">
       					</div>
    				</div>
    				<div id="permissions"></div>
    				<div class="form-actions" id="formButtons" style="display:none"></div>
  				</fieldset>
		</form>
	</div>
</div>