<div class="span12">
	<div id="message"></div>
	<form class="form-horizontal">
		<fieldset>
			<legend><h3><div id="legend">${title}</id></h3></legend>
			<div class="control-group" id="name_control_group">
				<label class="control-label" for="group_name" >
					${i18n('admin.cruds.group.cols.name')} <strong style="color:red">*</strong>
				</label>
				<div class="controls">
					<input type="text" id="group_name"></input>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="group_description">${i18n('admin.cruds.group.cols.description')}</label>
				<div class="controls">
					<input type="text" class="input-xlarge" id="group_description"></input>
				</div>
			</div>
			
			<div class="form-actions">
				<a href="javascript:void(0)" class="btn btn-primary" id="save">${i18n('web.generic.save')}</a>
			</div>
			
			<div class="row-fluid">
				<div class="span12">
					<div id="groupTabs" class="tabbable" style="display:none">
						<ul class="nav nav-tabs">
							<li class="active"><a href="#usersTab" data-toggle="tab">${i18n('web.generic.users')}</a></li>
							<li><a href="#rolesTab" data-toggle="tab">${i18n('web.generic.roles')}</a></li>
						</ul>
						<div class="tab-content">
							<div id="usersTab" class="tab-pane active">
								<div>
									<div id="multiSelectUsers" class="inlineDiv multiselect"></div>
									<div id="multiSelectUsersGroup" class="inlineDiv multiselect"></div>
									<div class="releaseFloat" />
								</div>
								<div class="form-actions">
									<a href="javascript:void(0)" class="btn" id="addUsersButton" style="display:none">${i18n('admin.cruds.group.addUsers')}</a>&nbsp;
									<a href="javascript:void(0)" class="btn" id="removeUsersButton" style="display:none">${i18n('admin.cruds.group.removeFromGroup')}</a>
								</div>
							</div>
							<div id="rolesTab" class="tab-pane">
								<div>
									<div id="multiSelectRoles" class="inlineDiv multiselect"></div>
									<div id="multiSelectRolesGroup" class="inlineDiv multiselect"></div>
									<div class="releaseFloat" />
								</div>
								<div class="form-actions">
									<a href="javascript:void(0)" class="btn" id="addRolesButton" style="display:none">${i18n('admin.cruds.group.addRoles')}</a>&nbsp;
									<a href="javascript:void(0)" class="btn" id="removeRolesButton" style="display:none">${i18n('admin.cruds.group.removeRoles')}</a>
								</div>
							</div>
						</div>						
					</div>
				</div>
			</div>
		</fieldset>
	</form>
</div>
