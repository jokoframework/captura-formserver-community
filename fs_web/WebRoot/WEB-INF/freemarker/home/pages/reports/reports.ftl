<div class="span12" >
<#-- Title of the page and the version-->

<div class="row-fluid pageTitleBox"  >
	<div class="span10">
		<div class="pageTitleLabel" > ${i18n('web.home.reports.title')} <span id="form_label"></span> </div>  
	</div>	
	<div class="span2" id="version_container" >
		<div id="dropdown_version" >	
		</div>		
	</div>
	
</div>

<#-- Section for the grid and the query selector-->
<div class="row-fluid" id="form_content" >
	<div class="span10 reportContainer">
		<div id="reportQueryTitleBox" >
			<div id="queryTitle" ></div>
			<div  id="queryButtons">
				<div class="btn-group" >
					<div class="mfProButton"><a  id="editQuery" href="javascript:void(0)" class=" glyphicons pencil reportGlyph" title="${i18n('web.home.reports.edit.tooltip')}" ><span>&nbsp;</span></a> </div>
					
				</div>
			</div>
			<div style="clear:both" />
			
		</div>
		
		<div id="reportTableContainer" >
			<div id="reportFilterDiv" >
			
				<div class="accordion" id="accordion2">
  					<div class="accordion-group">
    					<div class="accordion-heading">
      						<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">${i18n('web.home.querys.searchOptions_title')}</a>
    					</div>
    					<div id="collapseOne" class="accordion-body collapse in">
      						<div class="accordion-inner">
        						<form class="form-horizontal" id="reportFilterSection" >
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
  				</div>
			
			
				
			</div>
			<div id="searchContainer">
				<button class="btn btn-primary" type="button" id="buttonSearch" >${i18n('web.home.reports.button.search')}</button>
			</div>

            <!-- Nav tabs -->
            <ul class="nav nav-tabs">
                <li class="active">
                    <a href="#gridView" data-toggle="tab">${i18n('web.generic.grid')}</a>
                </li>
                <li>
                     <a href="#formView" data-toggle="tab">${i18n('web.generic.form')}</a>
                </li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="gridView">
                    <div id="dataTable_div">
                        <table id="dataGrid"></table>
                        <div id="dataPager"></div>
                    </div>

                    <div id="reportExportSection" >
                        <small>${i18n('web.home.reports.exportOptions')}</small>
                        <a id="buttonDownloadCSV" class="mfIcon mfIcon-exportCSV" title="${i18n('web.home.reports.csv.tooltip')}" />
                        <a id="buttonDownloadPDF" class="mfIcon mfIcon-exportPDF" title="${i18n('web.home.reports.pdf.tooltip')}" />
                        <a id="buttonDownloadXLS" class="mfIcon mfIcon-exportXLS" title="${i18n('web.home.reports.xls.tooltip')}" />
                        <a id="buttonDownloadXLSWithPhotos" class="mfIcon mfIcon-exportZIP" title="${i18n('web.home.reports.xlswithphotos.tooltip')}" />
                    </div>

                </div>
                <div class="tab-pane" id="formView">
                    <div style="text-align: center; display:none;">
						<div style="float: left; display: inline-block; color: grey;">
                            ${i18n('web.home.reports.formView.showingDoc')}: <b id="rowNum"></b>/<b id="rows"></b> - ${i18n('web.generic.page')} <b id="page"></b>
                        </div>                    
                    
                    	<!--
                        <div style="margin: 0 auto; text-align: center; width: inherit; display: inline-block;">
                            <input type="button" class="btn button-previous" style="float: left;" value="${i18n('web.generic.previous')}" />
                            <input type="button" class="btn button-next" style="float: left;" value="${i18n('web.generic.next')}"  />
                            <input type="hidden" id="prevRowId"  />
                            <input type="hidden" id="nextRowId" />
                        </div>
                        -->
                        
                        <nav>
						  <ul class="pager">
						    <li><a class="button-previous" href="#">${i18n('web.generic.previous')}</a></li>
						    <li><a class="button-next" href="#">${i18n('web.generic.next')}</a></li>
						    <li><input type="hidden" id="prevRowId"  /></li>
                            <li><input type="hidden" id="nextRowId" /></li>
						  </ul>
						</nav>

                        <div id="reportExportSection" style="display:none;">
                            <small>${i18n('web.home.reports.exportOptions')}</small>
                            <a id="buttonDownloadRowPDF" class="mfIcon mfIcon-exportPDF" title="${i18n('web.home.reports.pdf.tooltip')}" />
                        </div>
                        
                    </div>
                    <div class="container-fluid" id="dataForm">
                        ${i18n('web.home.reports.formView.emptyMsg')}
                    </div>
                    
                    <div style="text-align: center; display:none;">
                    	<!--
                        <div style="margin: 0 auto; text-align: center; width: inherit; display: inline-block;">
                            <input type="button" class="btn button-previous" style="float: left;" value="${i18n('web.generic.previous')}" />
                            <input type="button" class="btn button-next" style="float: left;" value="${i18n('web.generic.next')}" />
                            <input type="hidden" id="prevRowId" />
                            <input type="hidden" id="nextRowId" />
                        </div>
                        -->
                        
                        <nav>
						  <ul class="pager">
						    <li><a class="button-previous" href="#">${i18n('web.generic.previous')}</a></li>
						    <li><a class="button-next" href="#">${i18n('web.generic.next')}</a></li>
						    <li><input type="hidden" id="prevRowId"  /></li>
	                        <li><input type="hidden" id="nextRowId" /></li>
						  </ul>
						</nav>

                    </div>

                </div>
            </div>

		</div>

	</div>
	<div class="span2">
		<div class="well" id="queryContainer">
			<ul class="nav nav-list" id="queryList" style="text-align:left">
				<li class="nav-header">${i18n('web.home.reports.querys.title')}</li>
				<li class="divider"></li>
			</ul>
			<ul class="nav nav-list" >
				<li class="divider"></li>
			</ul>
			<a href="javascript:void(0)" id="newQuery" title="${i18n('web.home.reports.createQuery.tooltip')}" > ${i18n('web.home.reports.newQuery')} </a>
		</div>
	</div>
</div>

<#-- The message that will appear if a user tries to see the reports of an unpublished form -->
<div class="row-fluid" id="no_content" style="display:none" >
	<div class="span12">${i18n('web.home.reports.noVersionIsPublished')} </div>
</div>
</div>
<div id="imageDialog" style="overflow: auto; display:none; text-align:center">
	<div id="imageLoading" style="text-align:center;vertical-align: middle; width: 20%; margin: auto">
		<img src="${rc.contextPath}/res/img/loading2.gif" alt="${i18n('web.data.show.image.loading')}"  />
		<br/>${i18n('web.data.show.image.loading')}
	</div>
	<img id="image" alt="${i18n('web.forms.image')}" style="margin:auto"/>
</div>

