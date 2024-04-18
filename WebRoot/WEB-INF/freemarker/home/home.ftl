<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>${i18n('web.home.title')}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link type="text/css" href="${rc.contextPath}/res/css/jquery-ui/smoothness/jquery-ui-1.8.18.custom.css" rel="stylesheet" />
    <link type="text/css" href="${rc.contextPath}/res/css/jqgrid/ui.jqgrid.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap-responsive.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/res/css/main.css" rel="stylesheet"/>
    <link type="text/css" href="${rc.contextPath}/res/css/editor.css" rel="stylesheet" /> 
    
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
        
          
          <a class="brand" href="javascript:void(0)" style="background-color:#4dbfe3"><img src="${rc.contextPath}/res/img/captura-header.png" /></a>
          
          <div class="nav-collapse collapse">
                    <div class="btn-group pull-right">
				
				<#if available_apps?? && available_apps?size gt 1>
				  <ul class="nav">
				  <li>
				  <div style="text-align:right;">
					<select id="appChanger">
						<#list available_apps as a>
							<#if a.id == app.id>
								<option selected="selected" value="${a.id}">${a.name}</option>
							<#else>
								<option value="${a.id}">${a.name}</option>
							</#if>
						</#list>
					</select>
				   </div>
				   </li>
				   </ul>
				<#elseif user.rootUser>
					  <span class="badge mfRootApp">
						${app.name}
					  </span>
				</#if>
				
				<#--USERNAME-->
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
			  	<#--FIN USERNAME-->
			  	
			   </div>
            <ul class="nav mfMenu" id="mf_menu_content" >
              
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
    
    <div id="mf_main_div" class="container-fluid mfContainer">
      <div class="row-fluid">
        <div class="span3" id="mf_toolbox">
          
        </div><!--/span of the toolbox-->
        <div class="span9" id="page_width" >
			<div class="row-fluid">
				<div class="span12" >
					<div id="mf_navigator_content"  >
						<ul class="mfBreadcrumb" id="mf_breadcrumb" >
						</ul>
					</div>
					<div class="working-area" >
						<div class="row-fluid" id="mf_content" >
							
						</div>
					</div>
				</div>
			</div>
        </div><!--/span-->
      </div><!--/row-->

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
    <!-- Le javascript
    ================================================== -->
    
    
	<script type="text/javascript" data-main="${rc.contextPath}/acme/js/"  src="${rc.contextPath}/acme/js/require-jquery.js"></script>
	<script type="text/javascript">
		requirejs.config({
			urlArgs:"d=${deployId}"
		});
	</script>
	<script type="text/javascript" src="${rc.contextPath}/acme/js/home.js?d=${deployId}"></script>
	<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>
	<script type="text/javascript" src="${rc.contextPath}/res/glyphicons/scripts/modernizr.js"></script>

  </body>
</html>
