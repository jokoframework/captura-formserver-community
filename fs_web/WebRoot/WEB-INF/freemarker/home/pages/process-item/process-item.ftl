<div class="span12" >
	<#-- Title of the page-->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<h3><span id="processItem_title"></span></h3> 
		</div>	
	</div>
	
	<#-- Fields for the project -->
	<div class="row-fluid pageTitleBlock"  >
		<div class="span12">
			<form class="form-horizontal">
				<div class="control-group" id="processItem_poolGroup" style="display:none">
      					<label class="control-label" for="processItem_pool">
      						${i18n('admin.form.processitem.common.storedinpool')} <strong style="color:red">*</strong>
      					</label>
      					<div class="controls">
        					<select id="processItem_pool"></select>
       					</div>
    				</div>
    				<div class="control-group" id="processItem_poolLinkGroup" style="display:none">
      					<label class="control-label" for="processItem_poolLink">
      						${i18n('admin.form.processitem.common.storedinpool')}
      					</label>
      					<div class="controls">
        					<a href="javascript:void(0)" id="processItem_poolLink"></a>
       					</div>
    				</div>
  					<div class="control-group">
      					<label class="control-label" for="processItem_type">${i18n('admin.form.processitem.common.type')} <strong style="color:red">*</strong></label>
      					<div class="controls">
        					<select id="processItem_type"></select>
       					</div>
    				</div>
    				<div class="control-group">
      					<label class="control-label" for="processItem_label" >
      						${i18n('admin.form.processitem.common.label')} <strong style="color:red">*</strong>
      					</label>
      					<div class="controls">
        					<input type="text" id="processItem_label"></input>
       					</div>
    				</div>
    				<div id="processItem_version_group" class="control-group">
      					<label class="control-label" for="processItem_version">${i18n('admin.form.processitem.common.version')}</label>
      					<div class="controls">
      						<input id="processItem_version" type="text"/>
        				</div>
    				</div> 
    				<div id="processItem_required_group" class="control-group">
      					<label class="control-label" for="processItem_required">${i18n('admin.form.processitem.common.required')}</label>
      					<div class="controls">
        					<input id="processItem_required" type="checkbox"/>
       					</div>
    				</div>
    				<div id="processItem_propertiesContainer" style="display: none;">
							<!-- Basic Process item properties -->
							<fieldset id="processItem_propertiesFields">
								<div id="processItem_property_min_group" class="control-group">
			      					<label class="control-label" for="processItem_property_min">${i18n('admin.form.processitem.input.minimum')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_min" type="text"/>
			       					</div>
			    				</div>
			    				<div id="processItem_property_max_group" class="control-group">
			      					<label class="control-label" for="processItem_property_max">${i18n('admin.form.processitem.input.maximum')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_max" type="text"/>
			       					</div>
			    				</div>
			    				<!--<div id="processItem_property_readonly_group" class="control-group">
			      					<label class="control-label" for="processItem_property_readonly">${i18n('admin.form.processitem.input.readonly')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_readonly" type="checkbox"/>
			       					</div>
			    				</div>-->
			    				<div id="processItem_property_defaultValue_group" class="control-group">
			      					<label class="control-label" for="processItem_property_defaultValue">${i18n('admin.form.processitem.input.defaultValue')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_defaultValue" type="text"/>
			       					</div>
			    				</div>
			    				<div id="processItem_property_defaultLongitude_group" class="control-group">
			      					<label class="control-label" for="processItem_property_defaultLongitude">${i18n('admin.form.processitem.input.defaultLongitude')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_defaultLongitude" type="text"/>
			       					</div>
			    				</div>
			    				<div id="processItem_property_defaultLatitude_group" class="control-group">
			      					<label class="control-label" for="processItem_property_defaultLatitude">${i18n('admin.form.processitem.input.defaultLatitude')}</label>
			      					<div class="controls">
			        					<input id="processItem_property_defaultLatitude" type="text"/>
			       					</div>
			    				</div>
			    				
			    				<!-- Dropdown type select -->
								<div id="processItem_property_source_group" class="control-group">
			      					<label class="control-label" for="processItem_property_source">${i18n('admin.form.processitem.select.source')}</label>
			      					<div class="controls">
			      						<select id="processItem_property_source">
											<option value="manual">${i18n('admin.form.processitem.select.manualSource')}</value>
											<option value="dynamic">${i18n('admin.form.processitem.select.dynamicSource')}</value>
										</select>
			       					</div>
			    				</div>
			    				
			    				<!-- Manual lookup table -->
			    				<div id="processItem_property_lookupIdentifier_group" class="control-group">
				      					<label class="control-label" for="processItem_property_lookupIdentifier">
				      						${i18n('admin.form.processitem.select.identifier')} <strong style="color:red">*</strong>
				      					</label>
				      					<div class="controls">
				      						<select id="processItem_property_lookupIdentifier"></select>
				       					</div>
				    			</div>
				    			<div id="processItem_property_lookupLabel_group" class="control-group">
				      					<label class="control-label" for="processItem_property_lookupLabel">
				      						${i18n('admin.form.processitem.select.label')} <strong style="color:red">*</strong>
				      					</label>
				      					<div class="controls">
				      						<select id="processItem_property_lookupLabel"></select>
				       					</div>
				    			</div>
				    			<div id="processItem_property_lookupValue_group" class="control-group">
				      					<label class="control-label" for="processItem_property_lookupValue">
				      						${i18n('admin.form.processitem.select.value')} <strong style="color:red">*</strong>
				      					</label>
				      					<div class="controls">
				      						<select id="processItem_property_lookupValue"></select>
				       					</div>
				    			</div>
				    				
				    			<!-- Dynamic/Embedded lookup table -->	
			    				<div id="processItem_property_embedded_group" class="control-group">
				      					<label class="control-label">${i18n('admin.form.processitem.select.optionLabel')}</label>
				      					<div class="controls">
				      						<table id="processItem_property_embedded">
											</table>	
				       					</div>
				    			</div>
				    		</fieldset>
						</div>	      				
    				
    				<div class="form-actions">
						<a href="javascript:void(0)" class="btn btn-primary" id="processItem_save">${i18n('web.generic.save')}</a>
						<a href="javascript:void(0)" class="btn btn-primary" id="processItem_saveas" style="display: none;">${i18n('web.generic.saveas')}</a>
						<a href="javascript:void(0)" class="btn btn-inverse" id="processItem_delete" style="display: none;">${i18n('web.generic.delete')}</a>
					</div>
					<div id="processItem_tabs" class="tabbable">
						<ul class="nav nav-tabs">							
							<li class="active"><a href="#processItem_formsTab" data-toggle="tab">${i18n('admin.form.processitem.forms')}</a></li>
						</ul>
						<div class="tab-content">
							<div id="processItem_formsTab" class="tab-pane active">
								<div id="processItem_formsMessage">
									<br/>
									<p style="padding: 10 5 10 5px;">${i18n('admin.form.processitem.forms.noFormsUsingProcessItem')}</p>
								</div>
								<div id="processItem_formsContainer">
									<table id="processItem_formsGrid"></table>
									<div id="processItem_formsPager"></div>
									<div class="form-actions" id="processItem_upgradeAllContainer" style="display:none">														
										<a href="javascript:void(0)" class="btn btn-primary" id="processItem_upgradeAll">${i18n('web.generic.upgradeAll')}</a>
									</div>
								</div>
							</div>
						</div>
					</div>
			</form>
		</div>
	</div>
	
	<#--Start of tabs-->	
	
</div>

<#-- This is a jQuery dialog that is shown when the user clicks save as button-->
<div id="processItem_saveAsDialog" style="display:none">
       <div id="processItem_messageDialog"></div>
       <table cellpading="10px" cellspacing="10px">
        	<tr id="processItem_poolGroupDialog">
            	<td>
                	${i18n('web.generic.pool')}
                </td>
                <td>
                	<select id="processItem_poolDialog"></select>
                </td>
            </tr>
            <tr id="processItem_newLabelGroupDialog" style="vertical-align:top">
            	<td>
            		${i18n('web.generic.newLabel')}
            	</td>
            	<td>
                 	<input id="processItem_newLabelDialog" type="text"/>
            	</td>
            </tr>
       </table>
</div>
</div>
