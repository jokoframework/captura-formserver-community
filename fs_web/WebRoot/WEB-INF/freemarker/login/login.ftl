<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="UTF-8">
		<title>${i18n('web.generic.title')}</title>

		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="Captura Mobile Forms">
		<meta name="author" content="Captura">

		<link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap.css" rel="stylesheet">
		<link type="text/css" href="${rc.contextPath}/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
		<link type="text/css" href="${rc.contextPath}/res/glyphicons/css/glyphicons.css" rel="stylesheet" />

		<link type="text/css" href="${rc.contextPath}/res/css/outside.css" rel="stylesheet" />

		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
	</head>
	<body>
		<div class="loginWrapper">
			<div  class="loginHeader" >
				<#-- #3038 -->
				<img src="${rc.contextPath}/res/images/captura-login.png" />
			</div>
			
			<div class="content loginBox">
				<div id="messageBlock">
					<span id="messageSection"></span>
				</div>
				<div id="progressBlock" style="display:none; margin:0px; padding:0px">
					<img src="${rc.contextPath}/res/img/login-ajax.gif"/>
				</div>
				<div class="loginContent">
					<form id="login" method="post" action="#">
						<input id="username" name="username" placeholder="${i18n('web.generic.mail')}" type="text"  data-required="required" class="email" disabled="disabled" />
						<input id="password" name="password" placeholder="${i18n('web.generic.password')}" data-required="required" type="password" class="pass" disabled="disabled"/>
						<button id="submitLogin" type="submit" class="btn btn-primary" style="width:100%;" disabled="disabled"> ${i18n('web.login.submit')}</button>    
					</form>
					<div id="forgotPasswordBox" >
						<a href="${rc.contextPath}/account/recovery.mob" >${i18n('web.login.cantAccesAccount')}</a>
					</div>
				</div>		
			</div> 
			<#-- We currently don't support online registration, so it doesn't make sense to have this link. 27/08/2014
			<div id="registerBox" >
				<a id="registerLink" href="${rc.contextPath}/registration/register.mob" >${i18n('web.generic.register')}</a>
			</div> -->
		</div>
		<div class="footerEmpty"></div>
		<script type="text/javascript" data-main="${rc.contextPath}/acme/js/login.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
	</body>
</html>