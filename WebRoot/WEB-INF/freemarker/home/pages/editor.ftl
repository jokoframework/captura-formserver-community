<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock">
		<div class="span12">
			<div class="titleBlock">
				<span id="legend" class="title"> ${i18n('web.editor.title')}</span>
				<a href="javascript:void(0)" id="backWfLink" class="backLink" ><i class="icon-arrow-left"></i> ${i18n('web.editor.backToFormManager')}</a>
			</div> 
		</div>	
	</div>
	
	<div class="row-fluid" >
		<div class="span3">
			<div id="toolboxDiv"></div>
		</div>
		<div class="span6">
			<div class="mainFormDiv">
				<div id="mainFormTitleDiv">
					<span id="formLabel"></span>
					<a id="locationLink" href="javascript:void(0)" class="glyphicons direction locationOff" style="margin:0px; padding:0px; width:20px; height:16px; margin:1px">
						<i></i>
					</a>
					<div class="pull-right">
						<a href="javascript:void(0)" class="btn btn-small btn-primary" id="saveFormLink"> ${i18n('web.editor.mainbuttons.save')} </a>
						<a href="javascript:void(0)" class="btn btn-small btn-primary" id="saveAsFormLink"> ${i18n('web.editor.mainbuttons.saveAs')} </a>
					</div>
					<div style="clear:both">
					</div>
				</div>
				<div id="pageButtonDiv">
					<a href="javascript:void(0)" class="btn btn-mini btn-info" id="newPageButton"> ${i18n('web.editor.mainlayer.button.newPage')} </a>
				</div>
				<div id="mainAreaDiv">
				</div>
			</div>
		</div>
		<div class="span3">
			<div class="row-fluid editorPanelBox"  id="propertiesBox">
				<div class="span12">
					<div class="row-fluid">
						<div class="span12 editPanelBoxTitle" style="padding:10px">
							<a id="propertiesLabelLink" href="javascript:void(0)" style="text-decoration:none">${i18n("web.editor.properties")}</a>
						</div>
					</div>
					<div class="row-fluid editorPanelBoxInner">
						<#-- Div that shows the properties of the selected element-->
						<div class="span12" id="propertiesDiv"></div>
					</div>
				</div>
			</div>
			
			<#--Div for publication-->
			<div id="publicationDiv" class="row-fluid editorPanelBox">
				<div class="span12">
					<div class="row-fluid">
						<div class="span12 editPanelBoxTitle" style="padding:10px">${i18n('web.editor.properties.manage.publication')}</div>
					</div>
					<div class="row-fluid editorPanelBoxInner">
						<div class="span12">
							<table class="span12 table table-condensed">
								<tr>
									<td class="span4 propertyLabel">${i18n('web.generic.publishedVersion')}</td>
									<td class="span8">
										<input type="text" disabled id="versionPublished" class="span3" style="margin-bottom:0px" />
		      							<a href="javascript:void(0)" style="display:none" id="unpublishButton" class="btn btn-mini">${i18n('web.generic.unpublish')}</a>
									</td>
								</tr>
								<tr>
									<td class="span4 propertyLabel">${i18n('web.generic.version')}</td>
									<td class="span8">
										<input type="text" disabled id="lastStoredVersion" class="span3" style="margin-bottom:0px" />
										<a href="javascript:void(0)" style="display:none;" id="publishLastVersionButton" class="btn btn-mini btn-warning">${i18n('web.generic.publishLastVersion')}</a>
										<a href="javascript:void(0)" style="display:none;" id="publishButton" class="btn btn-mini btn-warning">${i18n('web.generic.publish')}</a>
									</td>
								</tr>
							</table>
						</div>
					</div>
				</div>
			</div><#--End of Div for publication-->
			
		</div><#--End of the third column-->
	</div>
	
	
</div><#-- End of visible components -->

<#--
This section contains the different popups that can be displayed from the editor
-->
<div id="saveAsDialog" style="display:none" class="editorDialog">
       <div id="saveAsMessage"></div>
       <table cellpading="10px" cellspacing="10px">
        	<tr id="projectSelectTr">
            	<td>
                	${i18n('web.generic.project')}
                </td>
                <td>
                	<select id="projectSelect"></select>
                </td>
            </tr>
            <tr id="formNameTr" style="vertical-align:top">
            	<td>
            		${i18n('web.generic.newLabel')}
            	</td>
            	<td>
                 	<input id="formName" type="text"  />
            	</td>
            </tr>
       </table>
</div>
<#-- Default value Dialog -->
<div id="defaultvalueOptionsDialog" style="display:none" class="editorDialog container-fluid">
	<div id="defaultvalueOptionsDialogMessage"></div>
	<div class="row-fluid">
		<div class="span12" style="text-align:center">
			<h5>${i18n('web.editor.properties.dialogs.defaultvalue.heading')}</h5>
		</div>
	</div>
	<div class="row-fluid" style="margin-top:10px">
		<div class="span12" style="margin-bottom:10px">
			${i18n('web.editor.properties.dialogs.defaultvalue.section1.explanation')}
		</div>
	</div>
	<div class="row-fluid">
		<div class="span2">${i18n('web.editor.properties.dialogs.lookuptable.label')}</div>
		<div class="span10"><select id="lookupTableSelect"></select></div>
	</div>
	<div class="row-fluid" style="border-bottom: 1px solid #e5e5e5;">
		<div class="span2">${i18n('web.editor.properties.dialogs.value.label')}</div>
		<div class="span10"><select id="valueSelect"></select></div>
	</div>
	<div id="filterSection" class="row-fluid" style="display:none; margin-top:10px">
		<div class="span12">
			<div class="row-fluid">
				<div class="span12" style="margin-bottom:10px">
					<em>${i18n('web.editor.properties.dialogs.defaultvalue.section2.explanation')}</em>
				</div>
			</div>
			<div>
				<div class="span3">
					<strong>${i18n('web.editor.properties.dialogs.defaultvalue.lookupColumn')}</strong>
				</div>
				<div class="span3">
					<strong>${i18n('web.editor.properties.dialogs.defaultvalue.condition')}</strong>
				</div>
				<div class="span6">
					<strong>${i18n('web.editor.properties.dialogs.defaultvalue.valueOf')}</strong>
				</div>
			</div>
			<div id="filterContainer" class="row-fluid" style="display:none;">
			</div>
		</div>
	</div>
	<div id="filterRow" class="row-fluid filterRow control-group" style="display:none">
		<div class="span3">
			<select class="span12" id="columnSelect"></select>
		</div>
		<div class="span3">
			<select class="span12" id="operatorSelect"></select>
		</div>
		<div class="span3">
			<select class="span12" id="elementSelect"></select>
		</div>
		<div id="actions" class="span3"><img id="delete" src="${rc.contextPath}/res/img/delete.png">&nbsp;<img id="add" src="${rc.contextPath}/res/img/add.png"></div>
	</div>
</div>
<#-- Dropdown filter Dialog -->
<div id="dropdownFilterDialog" style="display:none" class="editorDialog container-fluid">
	<div id="dropdownFilterDialogMessage"></div>
	<div class="row-fluid">
		<div class="span12" style="text-align:center">
			<h5>${i18n('web.editor.properties.dialogs.dropdown.filters.heading')}</h5>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<em>${i18n('web.editor.properties.dialogs.dropdown.filters.explanation')}</em>
		</div>
	</div>
	<div id="filterHeaders" class="row-fluid" style="margin-top:10px">
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.dropdown.lookupColumn')}</strong>
		</div>
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.dropdown.condition')}</strong>
		</div>
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.dropdown.valueOf')}</strong>
		</div>
		<div class="span3">
		</div>
	</div>
	<div id="filterContainer" class="row-fluid" style="display:none">
	</div>
	<div id="filterRow" class="row-fluid filterRow control-group" style="display:none">
		<div class="span3">
			<select class="span12" id="columnSelect"></select>
		</div>
		<div class="span3">
			<select class="span12" id="operatorSelect"></select>
		</div>
		<div class="span3">
			<select class="span12" id="elementSelect"></select>
		</div>
		<div id="actions" class="span3"><img id="delete" src="${rc.contextPath}/res/img/delete.png">&nbsp;<img id="add" src="${rc.contextPath}/res/img/add.png"></div>
	</div>
</div>
<#-- Navigation Dialog -->
<div id="pageNavigationDialog" style="display:none;" class="editorDialog container-fluid">
	<div class="row-fluid">
		<div class="span12" style="text-align:center">
			<h5>${i18n('web.editor.properties.dialogs.page.navigation.heading')}</h5>
		</div>
	</div>
	<div class="row-fluid" style="border-bottom: 1px solid #e5e5e5;">
		<div class="span12" style="margin-bottom:10px">
			<em>${i18n('web.editor.properties.dialogs.page.navigation.explanation')}</em>
		</div>
	</div>
	<div id="targetHeaders" class="row-fluid" style="margin-top:10px">
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.page.navigation.element')}</strong>
		</div>
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.page.navigation.condition')}</strong>
		</div>
		<div class="span2">
			<strong>${i18n('web.editor.properties.dialogs.page.navigation.value')}</strong>
		</div>
		<div class="span3">
			<strong>${i18n('web.editor.properties.dialogs.page.navigation.targetPage')}</strong>
		</div>
		<div class="span1"></div>
	</div>
	<div id="targetContainer" class="row-fluid" style="margin-top:10px">
	</div>
	<div class="row-fluid" style="margin-top:10px"> 
		<div class="span12">
			${i18n('web.editor.properties.dialogs.page.navigation.default.label')}&nbsp;<select id="defaultTargetSelect"></select>
		</div>
	</div>
	<div id="targetRow" class="row-fluid target" style="display:none">
		<div class="span3">
			<select class="span12" id="elementSelect"></select>
		</div>
		<div class="span3">
			<select class="span12" id="operatorSelect"></select>
		</div>
		<div class="span2">
			<input class="span12" id="valueInput" placeholder="${i18n('web.editor.properties.dialogs.page.navigation.value')}" disabled />
		</div>
		<div class="span3">
			<select class="span12" id="pageSelect"></select>
		</div>
		<div class="span1" id="actions"><img id="delete" src="${rc.contextPath}/res/img/delete.png">&nbsp;<img id="add" src="${rc.contextPath}/res/img/add.png"></div>
	</div>
</div>

<div id="elementContextMenu" class="contextMenu" style="display: none;">
	<div id="moveToPage" class="contextMenuItem">${i18n('web.editor.processItem.moveToPage')}</div>
</div>

<div id="pageContextMenu" class="contextMenu" style="display:none">
	<div id="addPageBefore" class="contextMenuItem">${i18n('web.editor.page.contextmenu.insertPage.before')}</div>
	<div id="addPageAfter" class="contextMenuItem">${i18n('web.editor.page.contextmenu.insertPage.after')}</div>
</div>