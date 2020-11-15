<div class="span12">
	<div id="message"></div>
	<form id="mailForm" class="form-horizontal">
		<legend><h3><div id="legend">${title}</id></h3></legend>
		<div class="control-group" id="user_type_control_group" style="padding-left:180px">
			<label class="radio-inline">
			  <input type="radio" name="userTypeGroup" id="byMail" value="false" checked> ${i18n('admin.cruds.user.cols.mail')}
			</label>
			<label class="radio-inline">
			  <input type="radio" name="userTypeGroup" id="byUsername" value="true"> ${i18n('admin.cruds.user.cols.username')}
			</label>
		</div>
		<div class="control-group" id="mail_control_group">
			<label class="control-label" for="mail" >
				<span id="mailSpan">${i18n('admin.cruds.user.cols.mail')}</span> <span id="usernameSpan" style="display:none">${i18n('admin.cruds.user.cols.username')}</span> <strong style="color:red">*</strong>
			</label>
			<div class="controls" id="mail_control_group">
				<div class="input-prepend">
					<input id="mail" class="input-xlarge" style="border-radius: 4px 4px;" type="text" placeholder="${i18n('web.new-user.mail.placeholder')}"> <input type="button" id="nextBtn" value="${i18n('web.new-user.mail.next')}" class="btn btn-primary" /> 
				</div>
			</div>
		</div>
		<div class="control-group" id="info_control_group" style="display:none">
			<div class="controls">
				<span id="infoSpan"></span>
			</div>
		</div>
	</form>
	<form id="userDataForm" class="form-horizontal" style="display:none">
		<fieldset>
			<div class="control-group" id="first_name_control_group">
				<label class="control-label" for="first_name" >
					${i18n('admin.cruds.user.cols.firstName')} <strong style="color:red">*</strong>
				</label>
				<div class="controls">
					<input type="text" id="first_name"></input>
				</div>
			</div>
			
			<div class="control-group" id="last_name_control_group">
				<label class="control-label" for="last_name" >
					${i18n('admin.cruds.user.cols.lastName')} <strong style="color:red">*</strong>
				</label>
				<div class="controls">
					<input type="text" id="last_name"></input>
				</div>
			</div>
			
			<div class="control-group" id="choosePassword" style="padding-left:180px">
				<label class="radio">
  					<input type="radio" name="assignPasswordGroup" id="assignPassword" value="assign" checked>
  					${i18n('web.new-user.assignPassword')}
				</label>
				<label class="radio">
  					<input type="radio" name="assignPasswordGroup" id="userAssignPassword" value="user_by_himself">
  					${i18n('web.new-user.userAssignsHisPassword')}
				</label>
			</div>
			
			<div class="control-group passwordControlDiv" id="password_control_group" >
				<label class="control-label" for="password" >
					${i18n('admin.cruds.user.cols.password')}
				</label>
				<div class="controls">
					<input type="password" id="password"></input>
				</div>
			</div>
			
			<div class="control-group passwordControlDiv" id="password_confirmation_control_group"  >
				<label class="control-label" for="password_confirmation" >
					${i18n('web.generic.confirmPassword')}
				</label>
				<div class="controls">
					<input type="password" id="password_confirmation"></input>
				</div>
			</div>
		
			<div class="form-actions" id="formButtons">
				<a href="javascript:void(0)" class="btn btn-primary" id="save">${i18n('web.generic.save')}</a>
				<a href="javascript:void(0)" class="btn" id="cancelBtn">${i18n('web.new-user.cancel')}</a>
			</div>
		</fieldset>
	</form>
	<#-- #2985 -->
	<form id="existingDataForm" class="form-horizontal" style="display:none">
		<fieldset>
			<div class="form-actions" id="formButtons">
				<a href="javascript:void(0)" class="btn btn-primary" id="inviteExistingBtn">${i18n('admin.cruds.user.invite')}</a>
				<a href="javascript:void(0)" class="btn" id="cancelExistingBtn">${i18n('web.new-user.cancel')}</a>
			</div>
		</fieldset>
	</form>
	<div id="userGroupsAndRoles" class="row-fluid">
		<div class="span12">
			<div id="userTabs" class="tabbable" style="display:none">
				<ul class="nav nav-tabs">
					<li class="active"><a href="#groupsTab" data-toggle="tab">${i18n('web.generic.groups')}</a></li>
					<li><a href="#rolesTab" data-toggle="tab">${i18n('web.generic.roles')}</a></li>
				</ul>
				<div class="tab-content">
					<div id="groupsTab" class="tab-pane active">
						<div>
							<div id="multiSelectGroups" class="inlineDiv multiselect"></div>
							<div id="multiSelectGroupsUser" class="inlineDiv multiselect"></div>
							<div class="releaseFloat" />
						</div>
						<div class="form-actions">
							<a href="javascript:void(0)" class="btn" id="addGroupsButton" style="display:none">${i18n('admin.cruds.user.addGroups')}</a>&nbsp<a href="javascript:void(0)" class="btn" id="removeGroupsButton" style="display:none">${i18n('admin.cruds.user.removeGroups')}</a>
						</div>
					</div>
					<div id="rolesTab" class="tab-pane">
						<div>
							<div id="multiSelectRoles" class="inlineDiv multiselect"></div>
							<div id="multiSelectRolesUser" class="inlineDiv multiselect"></div>
							<div class="releaseFloat" />
						</div>
						<div class="form-actions">
							<a href="javascript:void(0)" class="btn" id="addRolesButton" style="display:none">${i18n('admin.cruds.user.addRoles')}</a>&nbsp;<a href="javascript:void(0)" class="btn" id="removeRolesButton" style="display:none">${i18n('admin.cruds.user.removeRoles')}</a>
						</div>
					</div>
				</div>						
			</div>
		</div>
	</div>
</div>
