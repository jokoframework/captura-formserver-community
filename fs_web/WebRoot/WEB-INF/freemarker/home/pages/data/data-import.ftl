<div class="form-area" style="text-align:left">

<legend><h3>${i18n('web.dataimport.label')}</h3></legend> 

<div id="message"/>

<form class="form-horizontal" id="dataImport_uploadForm" method="post" action="${rc.contextPath}/data/import/file-upload.mob" enctype="multipart/form-data" target="uploadIframe">
	<fieldset>
		<div class="control-group">
      		<label class="control-label" for="selectFile">
      			${i18n('web.dataimport.lookuptable.chooseFile')}
      		</label>
      		<div class="controls">
        		<input class="input-large" type="file" name="csv" id="csv" id="selectFile"/>
       		</div>
    	</div>
    	<div class="control-group">
    		<label class="controls">
    			<small>The file must be in UTF-8 encoding</small>
    		</label>
    	<div>
    </fieldset>
</form>

<form class="form-horizontal" id="dataImport_parseCustomization" style="display:none">
 	<fieldset>
 		<div class="control-group">
      		<label class="control-label" for="dataImport_lookuptable">
      			${i18n('web.dataimport.lookuptable.name.label')} <strong style="color:red">*</strong>
      		</label>
      		<div class="controls">
        		<input type="text" name="lookuptable" id="dataImport_lookuptable" value="${lookuptable!}"></input>
       		</div>
    	</div>
		<div class="control-group">
      		<label class="control-label">
      			${i18n('web.dataimport.lookuptable.delimiter.label')} 
      		</label>
      		<div class="controls">
      			<label class="checkbox inline">
        			<input type="checkbox" id="dataImport_delimiter_tab" />${i18n('web.dataimport.lookuptable.delimiter.tab')} 
				</label>
				<label class="checkbox inline">
					<input type="checkbox" id="dataImport_delimiter_semicolon" />${i18n('web.dataimport.lookuptable.delimiter.semicolon')} 
				</label>
				<label class="checkbox inline">
					<input type="checkbox" id="dataImport_delimiter_comma" />${i18n('web.dataimport.lookuptable.delimiter.comma')} 
				</label>
				<label class="checkbox inline">
					<input type="checkbox" id="dataImport_delimiter_space" />${i18n('web.dataimport.lookuptable.delimiter.space')}
				</label>
				<label class="checkbox inline">
					<input type="checkbox" id="dataImport_delimiter_colon" />${i18n('web.dataimport.lookuptable.delimiter.colon')}	
       			</label>
       		</div>
    	</div>
		<div class="control-group">
      		<label class="control-label" for="dataImport_textQualifier">
      			${i18n('web.dataimport.lookuptable.qualifier.label')}
      		</label>
      		<div class="controls">
        		<select id="dataImport_textQualifier" class="span2">			
					<#list textQualifiers?keys as key>
						<option value="${key}">${textQualifiers[key]}</option>
					</#list>
				</select>
       		</div>
    	</div>
		<div class="control-group">
      		<label class="control-label" for="dataImport_columnheader">
      			${i18n('web.dataimport.lookuptable.columnheader.label')}
      		</label>
      		<div class="controls">
      			<label class="checkbox inline">
        			<input type="checkbox" id="dataImport_columnheader"/>
					${i18n('web.dataimport.lookuptable.columnheader.usefirstrow')}
				</label>
       		</div>
    	</div>
		<div class="control-group">
      		<label class="control-label" for="dataImport_previewDiv">
      			${i18n('web.dataimport.lookuptable.preview')}
      		</label>
    	</div>
    	<div class="control-group">
    	    <div class="control-label">
      			<div id="dataImport_previewDiv"></div>
       		</div>
       	</div>
    	<div class="form-actions" id="formButtons">
			<a href="javascript:void(0)" class="btn btn-primary" id="dataImport_importButton">
				${i18n('web.dataimport.lookuptable.importButton.label')}
			</a>
		</div>	
	</fieldset>
</form>
</div>

<iframe name="uploadIframe" id="uploadIframe" width="0"
					marginheight="0" marginwidth="0" frameborder="0" scrolling="no" style="height:0"></iframe>

<!-- Le javascript -->
<script type="text/javascript" src="${rc.contextPath}/res/js/json2.js"></script>





