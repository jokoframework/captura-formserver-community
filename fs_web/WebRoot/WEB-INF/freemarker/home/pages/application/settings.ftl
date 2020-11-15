
<div class="span12">
	<div class="row-fluid">
		<div class="span6 settingsBox preferences">
			<div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('web.settings.preferences')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody">
		    		<form class="form-horizontal">
		    			<div class="control-group">
    						<label class="control-label" for="applicationName">${i18n('web.settings.preferences.applicationName')}</label>
    						<div class="controls">
      							<input type="text" id="applicationName" value="${applicationName!}" maxlength="${maxAppnameLength}" />
    						</div>
  						</div>
  						<div class="control-group">
    						<label class="control-label" for="defaultLanguage">${i18n('web.settings.preferences.defaultLanguage')}</label>
    						<div class="controls">
      							<select id = "defaultLanguage">
									<#list languages as language>
										<#if language == defaultLanguage>
											<option value="${language}" selected="selected">${language}</option>
										<#else>
											<option value="${language}">${language}</option>
										</#if>
									</#list>
								</select>
    						</div>
  						</div>
  						<div class="control-group">
    						<div class="controls">
						      <a type="submit" id="savePreferences" class="btn btn-primary" href="javascript:void(0)" >${i18n('web.generic.save')}</a>
    						</div>
  						</div>
		    		</form>
		    	</div>
		    </div>
		 
			
		</div>
		<div class="span6 settingsBox contact">
			<div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('web.settings.aplicationInfo')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody">
		    		<form class="form-horizontal">
		    			<div class="control-group">
    						<label class="control-label" for="contactName">${i18n('web.settings.aplicationInfo.owner_name')}</label>
    						<div class="controls">
      							<span class="input-xlarge uneditable-input" id="contactName" >${contactFullName}</span>
    						</div>
  						</div>
  						<div class="control-group">
    						<label class="control-label" for="contactMail">${i18n('web.settings.aplicationInfo.owner_mail')}</label>
    						<div class="controls">
      							<span class="input-xlarge uneditable-input" id="contactName" >${contactEmail}</span>
    						</div>
    					</div>
  						<div class="control-group">
    						<label class="control-label" for="appId">${i18n('web.settings.aplicationInfo.appId')}</label>
    						<div class="controls">
      							<span class="input-xlarge uneditable-input" id="appId" >${applicationId}</span>
    						</div>
  						</div>    						
  						
		    		</form>
		    	</div>
		    </div>
		   
		</div>
	</div>
	
	
	<div class="row-fluid" style="margin-top:10px" >
		<div class="span12 settingsBox">
			<div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('web.settings.applicationLicense')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody">
		    		<div class="row-fluid">
		    			<div class="span6">
							<form class="form-horizontal">
							
							<div class="control-group">
								<label class="control-label" for="maxUsers">${i18n('web.settings.applicationLicense.maxUsers')}</label>
								<div class="controls">
									<span class="input-xlarge uneditable-input" id="maxUsers" >${license.maxUsers}</span>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="maxDevices">${i18n('web.settings.applicationLicense.maxDevices')}</label>
								<div class="controls">
									<span class="input-xlarge uneditable-input" id="maxDevices" >${license.maxDevices}</span>
								</div>
							</div>
							<div class="control-group">
									<label class="control-label" for="license_owner">${i18n('web.settings.applicationLicense.owner')}</label>
									<div class="controls">
										<span class="input-xlarge uneditable-input" id="license_owner" >${license.owner!}</span>
									</div>
								</div>
								<div class="control-group">
								<label class="control-label" for="validUntil">${i18n('web.settings.applicationLicense.validUntil')}</label>
									<div class="controls">
										<span class="input-xlarge uneditable-input" id="validUntil" >${validUntil}</span>
									</div>
								</div>																			
						</form>
					</div>
						<div class="span6">
		    				<div class="row-fluid" style="padding-top:50px";>
								<div class="span6" >
									<blockquote>
  										<p>${i18n('web.settings.license.invitation')}</p>  							
									</blockquote>
		    					</div>		
								<div class="span6" >
								<form class="form-inline" 
		    						id="license_uploadForm" method="post" action="${rc.contextPath}/application/settings/license/upload.mob" enctype="multipart/form-data" target="uploadIframe">
		    						<input class="input-large" type="file" name="licenseFile" id="licenseFile" id="selectFile"/>
		    						<input type="submit" value="${i18n('web.settings.applicationLicense.upload')}" class="btn btn-primary"></input>		    			
								</form>
							</div>	
						</div>
		    			</div>
				</div>
				
		    	
		    	
			</div>
			</div>
		</div>
	</div>
	
</div>

<iframe name="uploadIframe" id="uploadIframe" width="0" 
marginheight="0" marginwidth="0" frameborder="0" scrolling="no" style="height:0">
</iframe>