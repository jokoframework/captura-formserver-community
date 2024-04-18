<div id="message"></div>
<div id="messageLicenseUserMaxDiv">
	<span class="label label-warning" id="messageLicenseUserMax" style="display:none"></span>
</div>
<div id="groupUserAdminGroupDiv">
	<table cellspacing="0" align="center" id="groupsGrid"></table>
	<div id="groupsPager"></div>
</div>

<div id="groupUserAdminUserDiv">
	<table cellspacing="0" align="center" id="usersGrid"></table>
	<div id="usersPager"></div>
</div>

<div id="userExportSection" >
    <small>${i18n('web.home.reports.exportOptions')}</small>
    <a id="buttonDownloadCSV" class="mfIcon mfIcon-exportCSV" title="${i18n('web.home.reports.csv.tooltip')}" />
</div>

<div id="devicePopup" style="display:none">
	
	<div id="userWithoutPhones" style="display:none">
			${i18n('web.home.admin.no_devices')}
	</div>
		
		<div id="phoneDiv">
			<ul id="phoneList" class="nav nav-tabs nav-stacked">
			</ul>
		</div>
		<div id="phoneDetails">
			<form>
  				<fieldset>
    				
    				<label>${i18n('web.home.admin.devices.field.identifier')}<i class="icon-question-sign" id="identifier_help"></i></label>
    				<span class="input-xlarge uneditable-input" id="device_field_identifier"></span> 
    				
    				<label>${i18n('web.home.admin.devices.field.brand')}</label>
    				<span class="input-xlarge uneditable-input" id="device_field_brand"></span>
    				
    				<label>${i18n('web.home.admin.devices.field.model')}</label>
    				<span class="input-xlarge uneditable-input" id="device_field_model"></span>
    				
    				<#--
    				Do not show the phone number since we are not able to obtain it (see #2964)
    				<label>${i18n('web.home.admin.devices.field.phone_number')}</label>
    				<span class="input-xlarge uneditable-input" id="device_field_phone"></span>
    				-->
    				<label>${i18n('web.home.admin.devices.field.os')}</label>
    				<span class="input-xlarge uneditable-input" id="device_field_os"></span>
    				
    				<label>${i18n('web.home.admin.devices.field.release')}</label>
    				<span class="input-xlarge uneditable-input" id="device_field_release"></span>
    				

				 </fieldset>				 
			</form>
			<div id="deviceButtons">
				<a class="btn btn-danger" href="javascript:void(0)" id="deviceRemoveButton">${i18n('web.home.admin.devices.removeDevice')}</a>
				<a class="btn btn-danger" href="javascript:void(0)" id="deviceBlacklistButton">${i18n('web.home.admin.devices.blacklistDevice')}</a>
			</div>
		</div>
		
	
	<div class="releaseFloat">
	</div>
	
	
</div>

