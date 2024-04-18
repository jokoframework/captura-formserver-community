<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<h3><span id="legend"> ${title}</span></h3> 
		</div>	
	</div>
	<#-- Fields of the page-->
	<div class="row-fluid"  >
		<form class="form-horizontal">
			<div class="control-group" id="name_control_group">
				<label class="control-label" for="pool_name" >
					${i18n('admin.cruds.pool.cols.name')} <strong style="color:red">*</strong>
				</label>
				<div class="controls">
					<input type="text" id="pool_name" class="disabled editableField" disabled="disabled" ></input>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="pool_description">${i18n('admin.cruds.pool.cols.description')}</label>
				<div class="controls">
					<input type="text" class="disabled editableField"  disabled="disabled"  id="pool_description"></input>
				</div>
			</div>
			<div class="form-actions hideIfNoEditNorDelete" id="formButtons">
				<a href="javascript:void(0)" class="btn btn-primary displayOnEdit" id="save" style="display:none">${i18n('web.generic.save')}</a>
				<a href="javascript:void(0)" class="btn btn-inverse" id="delete" style="display:none">${i18n('web.generic.delete')}</a>
			</div>
		</form>
	</div>
	
	<#-- Start of row for tab panel-->
	<div class="row-fluid">
		<div class="span12">
			<div id="poolTabs" class="tabbable" style="display:none">
				<ul class="nav nav-tabs">
					<li class="active"><a href="#poolProcessItems" data-toggle="tab">${i18n('web.generic.processItems')}</a></li>
					<li><a href="#usersAuth" data-toggle="tab">${i18n('web.generic.users')}</a></li>
					<li><a href="#groupsAuth" data-toggle="tab">${i18n('web.generic.groups')}</a></li>
				</ul>
			</div>
			<div class="tab-content">
				<div id="poolProcessItems" class="tab-pane active">
					<div>
						<div id="poolMultiselect" class="inlineDiv" ></div>
						<div id="processItemMultiselect" class="inlineDiv" ></div>
						<div class="releaseFloat"  ></div>
					</div>
					<div  class="form-actions hideIfNoEdit" >
						<a href="javascript:void(0)" class="btn displayOnEdit" id="addProcessItemButton" style="display:none">${i18n('admin.cruds.pool.button.addProcessItem')}</a>
					</div>
					<div>
						<table id="processItemGrid"></table>
						<div id="processItemPager"></div>
					</div>
				</div>
				<div id="usersAuth" class="tab-pane">
					<div>
						<div id="multiSelectUsers" class="inlineDiv" ></div>
						<div id="multiSelectUserRoles" class="inlineDiv" ></div>
						<div class="releaseFloat"  ></div>
					</div>
					<div  class="form-actions hideIfNoEdit" >
						<a href="javascript:void(0)" class="btn displayOnEdit" id="addUserRolesButton" style="display:none">${i18n('web.generic.addUser')}</a>
					</div>
					<div>
						<table id="userRolesGrid"></table>
						<div id="userRolesPager"></div>
					</div>					
				</div>
				<div id="groupsAuth" class="tab-pane">
					<div>
						<div id="multiSelectGroups" class="inlineDiv" ></div>
						<div id="multiSelectGroupRoles" class="inlineDiv" ></div>
						<div class="releaseFloat"  ></div>
					</div>
					<div  class="form-actions hideIfNoEdit" >
						<a href="javascript:void(0)" class="btn displayOnEdit" id="addGroupRolesButton" style="display:none">${i18n('web.generic.addGroup')}</a>
					</div>
					<div>
						<table id="groupRolesGrid"></table>
						<div id="groupRolesPager"></div>
					</div>
				</div>				
			</div>
		</div>
	</div>	<#-- End of row for tabs-->
</div>
