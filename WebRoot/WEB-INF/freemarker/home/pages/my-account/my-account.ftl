<div class="span12" >
	<div class="row-fluid" >
		<div class="span6 settingsBox preferences" >
			<div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('myAccount.userData.title')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody">
		    		<form class="form-horizontal">
		    			<div class="control-group">
		    				<label class="control-label" for="form">
								 ${i18n('web.generic.name')}
							</label>
							<div class="controls">
								<input type="text" id="firstName" value="${firstName}"></input>
							</div>
						</div>
						<div class="control-group">
		    				<label class="control-label" for="form">
								 ${i18n('web.generic.lastname')}
							</label>
							<div class="controls">
								<input type="text" id="lastName" value="${lastName}"></input>
							</div>
						</div>
						<div class="control-group">
		    				<label class="control-label" for="form">
								 ${i18n('web.generic.mail')}
							</label>
							<div class="controls">
								<input type="text" id="mail" value="${mail}" readonly></input>
							</div>
						</div>
						<div class="control-group">
    						<div class="controls">
						      <a href="javascript:void(0)" class="btn btn-primary" id="saveUserData">${i18n('myAccount.changePreferences')}</a>
    						</div>
  						</div>		    			
		    		</form>
		    	</div>
		    </div>
		</div>
		<div class="span6 settingsBox">
			<div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('web.home.myaccount.connector.title')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody" style="padding-bottom:50px;padding-left:50px;padding-top:30px;">
		    		<a href="${rc.contextPath}/cr/configuration.txt" class="btn btn-info" >${i18n('myAccount.downloadCRConfiguration')}</a>
		    	</div>
		    </div>	
		</div>
	</div>
	<div class="row-fluid">
		<div class="span6 settingsBox">
			
		    <div class="row-fluid">
		    	<div class="span12 settingTitle">
		    		${i18n('web.home.myaccount.password.title')}
		    	</div>
		    </div>
		    <div class="row-fluid">
		    	<div class="span12 settingsBody">
		    		<form class="form-horizontal">
		    			<div class="control-group">
		    				<label class="control-label" for="form">${i18n('web.home.myaccount.password.field.old')}</label>
		    				<div class="controls">
		    					<input type="password" class="inputPass" id="old_pass" ></input>
		    				</div>
		    			</div>
		    			<div class="control-group">
		    				<label class="control-label" for="form">${i18n('web.home.myaccount.password.field.new')}</label>
		    				<div class="controls">
		    					<input type="password" class="inputPass" id="new_pass" ></input>
		    				</div>
		    			</div>
		    			<div class="control-group">
		    				<label class="control-label" for="form">${i18n('web.home.myaccount.password.field.confirm')}</label>
		    				<div class="controls">
		    					<input type="password" class="inputPass" id="confirm_pass" ></input>
		    				</div>
		    			</div>
		    			
		    			<div class="control-group">
    						<div class="controls">
						      <a href="javascript:void(0)" class="btn btn-primary" id="changePasswordBtn"  >${i18n('web.home.myaccount.password.change')}</a>
    						</div>
  						</div>
		    		</form>
		    	</div>
		    </div>					
		</div>
		<div class="span6">	
			<div class="settingsBox preferences" >
				<div class="row-fluid">
			    	<div class="span12 settingTitle">
			    		${i18n('web.home.myaccount.preferences.title')}
			    	</div>
			    </div>
			    <div class="row-fluid">
			    	<div class="span12 settingsBody">
			    		<form class="form-horizontal">
			    			<div class="control-group">
			    				<label class="control-label" for="form">
									 ${i18n('myAccount.language')}
								</label>
								<div class="controls">
									<select id="selectOfLanguage">
										<option value="en">English</option>
										<option value="es">Spanish</option>				
									</select>
								</div>
							</div>
							<#if available_apps?? && available_apps?size gt 1>
							<div class="control-group">
								<label class="control-label" for="form">
										 ${i18n('myAccount.defaultApplication')}
								</label>
								<div class="controls">
									<select id="defaultApplicationSelect" >
										<#list available_apps as a>
											<#if a.id == app.id>
												<option selected="selected" value="${a.id}">${a.name}</option>
											<#else>
												<option value="${a.id}">${a.name}</option>
											</#if>
										</#list>				
									</select>
								</div>
							</div>
							</#if>
							<div class="control-group">
	    						<div class="controls">
							      <a href="javascript:void(0)" class="btn btn-primary" id="savePreferencesButton">${i18n('myAccount.changePreferences')}</a>
	    						</div>
	  						</div>		    			
			    		</form>
			    	</div>
			    </div>
			</div>
	</div>
	
	
</div>


