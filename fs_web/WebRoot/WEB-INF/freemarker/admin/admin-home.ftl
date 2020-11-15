<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>${i18n('web.admin.home.title')}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link type="text/css" href="${rc.contextPath}/res/css/jquery-ui/smoothness/jquery-ui-1.8.18.custom.css" rel="stylesheet" />
    <link type="text/css" href="${rc.contextPath}/res/css/jqgrid/ui.jqgrid.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap-responsive.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/res/css/main.css" rel="stylesheet"/>
    
	<link type="text/css" href="${rc.contextPath}/res/css/bootstrap-mf.css" rel="stylesheet"/>
	<link type="text/css" href="${rc.contextPath}/res/css/glyphicon-mf.css" rel="stylesheet"/>
	<link type="text/css" href="${rc.contextPath}/res/css/sodep-multiselect/sodep-multiselect.css" rel="stylesheet"/>
	<link type="text/css" href="${rc.contextPath}/res/css/notify/ui.notify.css" rel="stylesheet" />
	<link type="text/css" href="${rc.contextPath}/res/glyphicons/css/glyphicons.css" rel="stylesheet" />
	
	
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="${rc.contextPath}/acme/js/html5.js"></script>
    <![endif]-->

    
  </head>
  <body>
	<div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner mfMenu" id="topMenu" >
        <div class="container-fluid">
          <a data-target=".nav-collapse" data-toggle="collapse" class="btn btn-navbar mfCollaped">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          
          <a class="brand" href="javascript:void(0)"><img src="${rc.contextPath}/res/img/captura-header.png" /></a>
          
          <div class="nav-collapse collapse">
               <div class="btn-group pull-right">
               	<!--USERNAME-->
				<a class="btn dropdown-toggle" href="javascript:void(0)" data-toggle="dropdown">
					<i class="icon-user"></i>
					${user.firstName} ${user.lastName}
					<span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
					<li><a href="javascript:void(0)" id="myAccount">${i18n('web.home.myaccount')}</a></li>
					<li class="divider"></li>
					<li><a href="${rc.contextPath}/login/logout.mob">${i18n('web.generic.logout')}</a></li>
					<li class="divider"></li>
					<li><a href="http://www.captura.com.py/privacidad.html" target="_blank">${i18n('web.generic.privacy')}</a></li>	
					<li class="divider"></li>
					<li><a href="http://www.captura.com.py/ayuda/" target="_blank">${i18n('web.generic.help')}</a></li>				
				</ul>
			   </div>
            <ul class="nav mfMenu" id="mf_menu_content" >
            <li><a href="javascript:void(0)" id="menu_home">Home</a> </li>
              <li><a href="javascript:void(0)" id="menu_unexpectedErrors">${i18n('web.home.uncaughtException')}</a> </li>
              <li><a href="javascript:void(0)" id="menu_systemParameters">${i18n('web.home.systemParameters')}</a> </li>
              <li><a href="${rc.contextPath}/swagger-ui/index.html" target="_blank">WEB API</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
    
    <div id="mf_main_div" class="container-fluid mfContainer">
      <div class="row-fluid">
        <div id="mf_toolbox" class="span3">
			<div class="toolbox sidebar-nav">
				<ul class="nav nav-list-captura">
					<li class="nav-header-captura">${i18n('web.generic.toolbox')}</li>
					<li>
						<a class="glyphicons toolboxIcon folder_open" href="javascript:void(0)" id="toolbox_createApp">
							<i></i>${i18n('web.root.home.toolbox.createApp')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon circle_ok" href="javascript:void(0)" id="toolbox_pendingRegistrations">
							<i></i>${i18n('web.root.home.toolbox.pendingRegistrations')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon user_add" target="_blank" href="${rc.contextPath}/registration/register.mob" id="toolbox_register">
							<i></i>${i18n('web.root.home.toolbox.register')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon restart" href="javascript:void(0)" id="toolbox_reloadParameters">
							<i></i>${i18n('web.root.home.toolbox.reloadParameters')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon restart" href="javascript:void(0)" id="toolbox_reloadi18n">
							<i></i>${i18n('web.root.home.toolbox.reloadi18n')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon circle_info" href="javascript:void(0)" id="toolbox_serverInfo">
							<i></i>${i18n('web.root.home.toolbox.serverInfo')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon stats" href="javascript:void(0)" id="toolbox_dataUsage">
							<i></i>${i18n('web.root.home.toolbox.dataUsage')}
						</a>
					</li>
					<li>
						<a class="glyphicons toolboxIcon circle_remove" href="javascript:void(0)" id="toolbox_failedDocuments">
							<i></i>${i18n('web.root.home.toolbox.failedDocuments')}
						</a>
					</li>
				</ul>
			</div>
        </div>
        <div class="span9" id="page_width" >
			<div class="row-fluid">
				<div class="span12" >
					<div id="mf_navigator_content" >
						<ul class="mfBreadcrumb" id="mf_breadcrumb" >
							<li><span class="divider">/<span><a id="nav_item_100" href="javascript:void(0)"> ${i18n('web.admin.home.breadCrumb')}</a></span></span></li>
						</ul>
					</div>
					<div class="working-area" >
						<div class="row-fluid" id="mf_content" >
							<div class="span12">
								<div class="checkbox">
									<label>
										<input type="checkbox" id = "onlyActiveApps" checked/>
										<span>${i18n('web.admin.home.onlyActive')}</span>
									</label>
								</div>
								<div id="applicationContainer">
									<div id="applicationMultiselect" class="inlineDiv"></div>
									<div class="inlineDiv" style="padding-top: 24px;">
										<ul>
											<li>${i18n('web.admin.home.totalApps')} : <span id="totalApps"></span></li></br>
											<li>${i18n('web.admin.home.activeApps')} : <span id="activeApps"></span></li>
											<li>${i18n('web.admin.home.inactiveApps')} : <span id="inactiveApps"></span></li>
										</ul>
									</div>
									<div class="releaseFloat"></div>
								</div>
							</div>						
						</div>
						<div class="row-fluid">
							<div class="span12">
								<div id="appStats" style="display:none">
									<hr/>
  						  			<h1 id="appStats_AppName"></h1>
  						  			<hr/>
  						  			<h4>Object Count</h4>
    								<table class="table table-bordered table-striped" style="width:300px">
    									<tr>
    										<td style="width:100px">Project Count</td>
    										<td><span id="appStats_projectCount"></span></td>
    									</tr>
    									<tr>
    										<td>Form Count</td>
    										<td><span id="appStats_formCount"></span></td>
    									</tr>
    									<tr>
    										<td>User Count</td>
    										<td><span id="appStats_userCount"></span></td>
    									</tr>
    								</table>
    								<h4>Last logins</h4>
    								<table id = "appStats_loginsTable" class="table table-bordered table-striped">
    								</table>
    								<h4>Document Stats</h4>
    								<table id = "appStats_documentCount" class="table table-bordered table-striped">
    								</table>
    							</div>
							</div>
						</div>
					</div>
				</div>
			</div>
        </div>
      </div>

      <hr>

      <footer>
        <p class="pull-left mfCompanyName" >&copy; Captura ${year?c}</p>
        <p class="pull-right" ><img width="105" height="15" src="${rc.contextPath}/res/img/captura-footer.png"></p>
      </footer>

    </div><!--/.fluid-container-->
	<div id="notificationContainer" style="display:none">
        <div id="success-template" class="ui-notify-message-style-success">
            <a class="ui-notify-cross ui-notify-close" href="javascript:void(0)">x</a>
            <h1><i class="icon-ok-sign"></i> t{title}</h1>
            <p>t{message}</p>
        </div>
        <div id="error-template" class="ui-notify-message-style-error">
            <a class="ui-notify-cross ui-notify-close" href="javascript:void(0)">x</a>
            <h1><i class="icon-remove-sign"></i> t{title}</h1>
            <p>t{message}</p>
        </div>
        <div id="info-template" class="ui-notify-message-style-info">
            <a class="ui-notify-cross ui-notify-close" href="javascript:void(0)">x</a>
            <h1><i class="icon-info-sign"></i> t{title}</h1>
            <p>t{message}</p>
        </div>
    </div>
    
    </div>
    	<div id="createAppPopup" style="display:none;" title="${i18n('web.root.home.toolbox.createApp')}">
    		<form class="form-horizontal">
    			<div class="control-group">
					<label class="control-label" for="applicationName">${i18n('web.application.application.name.label')}</label>
					<div class="controls">
						<input name="applicationName" id="applicationName" type="text" />						
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="owner">${i18n('web.root.createApp.owner')}</label>
					<div class="controls">
						<select name="owner" id="owner" />
						</select>						
					</div>
				</div>
				
				<div class="control-group">
					<div class="controls">
						<input type="button" id="createAppButton" value="${i18n('web.generic.save')}" class="btn btn-primary">
					</div>
				</div>
    		</form>
    	</div>
    	<div id="serverInfoPopup" style="display:none;" title="${i18n('web.root.home.toolbox.serverInfoPopup')}">
    		<form class="form-horizontal">				
				<div class="control-group">
					<div id="showServerInfoTable" class="control-group">
					</div>
				</div>
    		</form>
    	</div>
    	
    </div>

	<div id="appMoreOptionsPopup" style="display:none;" title="${i18n('web.root.appOptions.popup')}" >
    		<form class="form-horizontal">
				<input id="app_id" name="app[id]" type="hidden" value="">
				<fieldset class="control-group">
  					<legend class="t1-label control-label">
    					${i18n('web.root.appOptions.popup.workflow.label')}
  					</legend>
  					<div class="controls login-verification-controls">
    					<div class="control-list">
      						<label class="t1-label checkbox">
        						<input id="has_workflow" name="app[has_workflow]" type="checkbox" value="1">
								${i18n('web.root.appOptions.popup.workflow.checkbox')}
        					</label>
							<p>${i18n('web.root.appOptions.popup.workflow.description')}</p>
    					</div>
  					</div>
				</fieldset>
				<div class="control-group">
					<div class="controls span2">
						<input id="settings_save" class="btn btn-primary btn-block" disabled="disabled" value="${i18n('web.generic.save')}" />
						<span class="spinner-small settings-save-spinner"></span>
					</div>
				</div>
    		</form>
    </div>
    
    
    <!-- Le javascript
    ================================================== -->
    
	<script type="text/javascript" data-main="${rc.contextPath}/acme/js/" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
	<script type="text/javascript">
		requirejs.config({
			urlArgs:"d=${deployId}"
		});
	</script>
	<script type="text/javascript" src="${rc.contextPath}/acme/js/admin-home.js?d=${deployId}"></script>
	<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>
	
	<script type="text/javascript" src="${rc.contextPath}/res/glyphicons/scripts/modernizr.js"></script>

  </body>
</html>
