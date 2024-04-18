<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<h3><span id="legend"> ${i18n('admin.cruds.form.title')}</span></h3> 
		</div>	
	</div>	
	
	<#-- Fields of the page-->
	<div class="row-fluid"  >
		<div class="span12">
			<form class="form-horizontal">
				<div style="display:none" class="control-group" id="project_select_control_group">
						<label class="control-label" for="project_select">
							${i18n('web.generic.project')} <strong style="color:red">*</strong>
						</label>
						<div class="controls">
							<select id="project_select">
								<option value="none">${i18n('web.data.select.project')}</option>
								<#list projects as p>
									<option value="${p.id}">${p.label}</option>
								</#list>
			     			<select>
						</div>
				</div>
				<div style="display:none" class="control-group" id="project_link_control_group">
						<label class="control-label" for="project_link">
							${i18n('web.generic.project')} 
						</label>
						<div class="controls">
							<a href="javascript:void(0)" class="btn btn-link" id="project_link" sytle="display:none" ></a>
							<input type="text" class="disabled" disabled="disabled" id="project_label" sytle="display:none" ></input>
						</div> 
					</div>
    				<div class="control-group" id="form_control_group_label">
      					<label class="control-label" for="form_label" >
      						${i18n('admin.cruds.form.cols.label')} <strong style="color:red">*</strong>
      					</label>
      					<div class="controls">
        					<input type="text" class="disabled" disabled="disabled" id="form_label"></input>
       					</div>
    				</div>
    				<div style="display:none" class="control-group" id="description_control_group">
      					<label class="control-label" for="form_description">${i18n('admin.cruds.form.cols.description')}</label>
      					<div class="controls">
        					<input type="text" class="input-xlarge" id="form_description" ></input>
       					</div>
    				</div>
    				<div style="display:none" class="control-group" id="version-control-group">
      					<label class="control-label" for="form_version">${i18n('admin.cruds.form.cols.version')}</label>
      					<div class="controls">
        					<input type="text" id="form_version" class="disabled" disabled="disabled"></input>
       					</div>
    				</div>
    				<div style="display:none" class="control-group" id="version-published-control-group">
      					<label class="control-label" for="form_version_published">${i18n('admin.cruds.form.cols.versionPublished')}</label>
      					<div class="controls">
        					<input type="text" id="form_version_published" class="disabled" disabled="disabled"></input>
       					</div>
    				</div>
    				<div class="form-actions hideIfNoEdit" id="formButtons">
						<a href="javascript:void(0)" class="btn btn-primary" id="saveForm" style="display:none" >${i18n('web.generic.save')}</a>
						<a href="javascript:void(0)" id="publishButton" class="btn btn-primary" style="display:none">${i18n('web.generic.publish')}</a>
						<a href="javascript:void(0)" id="publishLastVersionButton" class="btn btn-primary" style="display:none">${i18n('web.generic.publishLastVersion')}</a>
						<a href="javascript:void(0)" id="unpublishButton" class="btn btn-primary" style="display:none">${i18n('web.generic.unpublish')}</a>
						<a href="javascript:void(0)" class="btn btn-inverse" id="deleteForm" style="display:none">${i18n('web.generic.delete')}</a>
						&nbsp;&nbsp;<a href="javascript:void(0)" class="btn btn-info" id="goEditor" style="display:none">${i18n('admin.cruds.form.button.goEditor')}</a>
					</div>
					
					
			</form>
		</div>
	</div>
	<#-- Start of row for tab panel-->
	<#-- #3665-->
	<div class="row-fluid" style="display:none">
		<div class="span12">
			<div id="formTabs" class="tabbable" style="display:none">
				<ul class="nav nav-tabs">
					<li class="active"><a href="#usersAuth" data-toggle="tab">${i18n('web.generic.users')}</a></li>
					<li><a href="#groupsAuth" data-toggle="tab">${i18n('web.generic.groups')}</a></li>
				</ul>
				<div class="tab-content">
					<div id="usersAuth"  class="tab-pane active">
						<div>
							<div id="multiSelectUsers" class="inlineDiv" > </div>
							<div id="multiSelectUserRoles" class="inlineDiv" ></div>
							<div class="releaseFloat"></div>
						</div>
						<div  class="form-actions hideIfNoEdit"   >
							<a href="javascript:void(0)" class="btn" id="addUserRolesButton" style="display:none">${i18n('web.generic.addUser')}</a>
						</div>
						<div  >
							<table id="userRolesGrid" class="scroll"></table>
							<div id="userRolesPager"></div>
						</div>
					</div>
					<div id="groupsAuth"  class="tab-pane">
						<div>
							<div id="multiSelectGroups" class="inlineDiv" > </div>
							<div id="multiSelectGroupRoles" class="inlineDiv" ></div>
							<div class="releaseFloat"></div>
						</div>
						<div  class="form-actions hideIfNoEdit" >
							<a href="javascript:void(0)" class="btn" id="addGroupRolesButton" style="display:none">${i18n('web.generic.addGroup')}</a>
						</div>
						<div  >
							<table id="groupRolesGrid" class="scroll"></table>
							<div id="groupRolesPager"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div><#-- Start of row for tab panel-->
	
</div>