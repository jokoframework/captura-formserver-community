<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>${i18n('web.generic.title')}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Captura Mobile Forms">
    <meta name="author" content="Captura">
    <link type="text/css" href="${rc.contextPath}/res/css/jquery-ui/smoothness/jquery-ui-1.8.18.custom.css" rel="stylesheet" />
    <link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap.css" rel="stylesheet">
	<link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
	<link type="text/css" href="${rc.contextPath}/res/css/main.css" rel="stylesheet" />
	
	<style type="text/css">
      body {
        	padding-top: 30px;
      }
    </style>
 
    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
  </head>
  <body>
  	 <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container-fluid">
          <a class="brand" href="javascript:void(0)"><img src="${rc.contextPath}/res/img/captura-header.png" /></a>
          <div class="nav-collapse">
            <ul class="nav">
			  <li id="registerMenu"><a href="${rc.contextPath}/registration/register.mob">${i18n('web.generic.register')}</a></li>
              <li id="loginMenu"><a href="${rc.contextPath}/login/login.mob">${i18n('web.generic.login')}</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
     </div>
     <br />
     <div class="container-fluid">
