
<div class="span12" >
	<div class="row-fluid pageTitleBox"  >
		<div class="span11">
			<div class="pageTitleLabel" >
				<span id="pageTitle" class="title" > </span>
				<a href="javascript:void(0)" id="backLink" class="backLink" ><i class="icon-arrow-left"></i> ${i18n('web.home.querys.backToReports')}</a>
			</div>  
		</div>	
	</div>
	
	<div class="row-fluid" >
		<div class="span2">
			<div class="reportStep" >${i18n('web.home.querys.step')} 1.</div>
			<div class="reportSubTitle" >${i18n('web.home.querys.section.generalInfo')}</div>
		</div>
		<div class="span10">
			<div class="reportSelectSection" >
			<form >
				<div class="control-group">
					<label class="control-label" for="form">
						 ${i18n('web.generic.form')}
					</label>
					<div class="controls">
						<span id="formLabelOnReport" class="input-xlarge uneditable-input"></span>
					</div> 
				</div>
				<div class="control-group">
					<label class="control-label" for="version">
						 ${i18n('web.generic.version')}
					</label>
					<div class="controls">
						<span id="formVersion" class="input-xlarge uneditable-input"></span>
					</div> 
				</div>
				<div class="control-group"  id="name-control-group">
					<label class="control-label" for="name">
						 ${i18n('web.home.querys.name')} <strong style="color:red">*</strong>
					</label>
					<div class="controls">
						<input type="text" id="queryName"></input>
					</div> 
				</div>
				<div class="control-group">
					<label class="control-label" for="name">
						 ${i18n('web.home.querys.setAsDefaultQuery')}
					</label>
					<div class="controls">
						<label class="checkbox">
							<input type="checkbox" id="defaultQuery"> 
						</label>
					</div>	 
				</div>
			</form>
			</div>
		</div>
	</div>
	<#--
	Columns that are going to be visible on the table
	-->
	<div class="row-fluid reportSection" >
		<div class="span2">
			<div class="reportStep" >${i18n('web.home.querys.step')} 2.</div>
			<div class="reportSubTitle" >${i18n('web.home.querys.section.tableColumns')}</div>
			<span class="help-block">${i18n('web.home.querys.section.tableColumns.help')}</span>
		</div>
		<div class="span10">
			<div class="reportSelectSection" >
				<div id="tableColumns"></div>
				<p>
  					<small>* ${i18n('web.home.querys.metadataLabel')}</small>
				</p>
				<label class="checkbox">
					<input id="checkboxGoogleMapsLinks" type="checkbox">
					<small>${i18n('web.home.querys.googleMapsLinks')}</small>
				</label>
			</div>
		</div>		
	</div>
	
	<#-- Choose the sorting columns --> 
	<div class="row-fluid reportSection">
		<div class="span2">
			<div class="reportStep" >${i18n('web.home.querys.step')} 3.</div>
			<div class="reportSubTitle" >${i18n('web.home.querys.section.sortingColumns')}</div>
			<span class="help-block">${i18n('web.home.querys.section.sortingColumns.help')}</span>
		</div>
		<div class="span10">
			<div class="reportSelectSection" >
				<div id="sortingColumns"></div>
				<p>
  					<small>* ${i18n('web.home.querys.metadataLabel')}</small>
				</p>
			</div>
		</div>		
	</div>
	
	<#--
	Filter selection
	-->
	<div class="row-fluid reportSection" >
		<div class="span2">
			<div class="reportStep" >${i18n('web.home.querys.step')} 4.</div>
			<div class="reportSubTitle" >${i18n('web.home.querys.section.filter')}</div>
			<span class="help-block">${i18n('web.home.querys.section.filter.help')}</span>
		</div>
		<div class="span10">
			<div class="reportSelectSection" >
				<form  id="selectedFiltersForm" >
					<div class="control-group addFilterButtonDiv" >
						<label class="control-label" for="addFilterButton"></label>
						<div class="controls">
							<a id="addFilterButton" class="clickeable" >${i18n('web.home.querys.addFilterButton')}</a>
						</div>
					</div>
				</form>
			</div>
		</div>		
	</div>

    <#--
        Image naming section
    -->
    <div class="row-fluid reportSection" >
        <div class="span2">
            <div class="reportStep" >${i18n('web.home.querys.step')} 5.</div>
            <div class="reportSubTitle" >${i18n('web.home.querys.section.elementsFileNames')}</div>
            <span class="help-block">${i18n('web.home.querys.section.elementsFileNames.help')}</span>
        </div>
        <div class="span10">
            <div class="reportSelectSection" >
                <form id="elementsFileNamesForm" >
                    <div class="control-group" >
                        <label class="control-label">
                        </label>
                        <div class="controls">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>


    <div class="row-fluid " >
		<div class="span12">
			<div class="form-actions">
				<a href="javascript:void(0)" class="btn btn-primary" id="saveQuery">${i18n('web.generic.save')}</a>
				<a href="javascript:void(0)" class="btn btn-inverse" id="deleteQuery" >${i18n('web.generic.delete')}</a>
				<a href="javascript:void(0)" class="btn " id="backButton" >${i18n('web.home.querys.backToReports')}</a>
			</div>		
		</div>		
	</div>
	
</div>