<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<h3><span id="legend"> ${title}</span></h3> 
		</div>	
	</div>	
	
	<#-- Fields for the project -->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<form class="form-horizontal">
				<div class="control-group" id="project_control_group">
					<label class="control-label" for="project_label" >
						${i18n('admin.cruds.project.cols.label')} <strong style="color:red">*</strong>
					</label>
					<div class="controls">
						<input type="text" class="disabled editableField" disabled="disabled" id="project_label"></input>
					</div>
    			</div>
    			<div class="control-group">
					<label class="control-label" for="project_description">${i18n('admin.cruds.project.cols.description')}</label>
					<div class="controls">
						<input type="text" class="span5 disabled editableField" disabled="disabled"  id="project_description"></input>
					</div>
				</div>
				<div class="form-actions hideIfNoEditNorDelete" id="formButtons">
					<a href="javascript:void(0)" class="btn btn-primary" id="saveProject" style="display:none" >${i18n('web.generic.save')}</a>
					<a href="javascript:void(0)" class="btn btn-inverse" id="deleteProject"  style="display:none" >${i18n('web.generic.delete')}</a>
				</div>
			</form>
		</div>
	</div><#--End of row for fields-->
	<#-- Start of row for tab panel-->
	<div class="row-fluid">
		<div class="span12">
			<div id="projectTabs" class="tabbable" style="display:none">
				<ul class="nav nav-tabs">
					<li class="active"><a href="#formsImport" data-toggle="tab">${i18n('web.generic.forms')}</a></li>
					<li><a href="#usersAuth" data-toggle="tab">${i18n('web.generic.users')}</a></li>
					<li><a href="#groupsAuth" data-toggle="tab">${i18n('web.generic.groups')}</a></li>
				</ul>
				
				<div class="tab-content">
					<#--Start of tab for forms -->
					<div id="formsImport" class="tab-pane active">
						<div>
							<div id="projectMultiselect" class="inlineDiv" ></div>
							<div id="formMultiselect" class="inlineDiv" ></div>
							<div class="releaseFloat" class="inlineDiv" ></div>
						</div>
						<div  class="form-actions hideIfNoEdit" >
							<a href="javascript:void(0)" class="btn displayOnEdit" id="addFormButton" style="display:none">${i18n('web.generic.addForm')}</a>
						</div>
						<div  >
							<table id="formsGrid"></table>
							<div id="formsPager"></div>
						</div>
					</div>
					<#--Start of tab for users -->
					<div id="usersAuth" class="tab-pane">
						<div>
							<div id="multiSelectUsers" class="inlineDiv" ></div>
							<div id="multiSelectUserRoles" class="inlineDiv" ></div>
							<div class="releaseFloat"  ></div>
						</div>
						<div  class="form-actions hideIfNoEdit" >
							<a href="javascript:void(0)" class="btn displayOnEdit" id="addUserRolesButton" style="display:none">${i18n('web.generic.addUser')}</a>
						</div>
						<div  >
							<table id="userRolesGrid"></table>
							<div id="userRolesPager"></div>
						</div>
					</div>
					<#--Start of tab for groups -->
					<div id="groupsAuth" class="tab-pane">
						<div>
							<div id="multiSelectGroups" class="inlineDiv" ></div>
							<div id="multiSelectGroupRoles" class="inlineDiv" ></div>
							<div class="releaseFloat" class="inlineDiv" ></div>
						</div>
						<div  class="form-actions hideIfNoEdit" >
							<a href="javascript:void(0)" class="btn displayOnEdit" id="addGroupRolesButton" style="display:none">${i18n('web.generic.addGroup')}</a>
						</div>
						<div  >
							<table id="groupRolesGrid"></table>
							<div id="groupRolesPager"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div><#--End of row for tabs-->
</div>
