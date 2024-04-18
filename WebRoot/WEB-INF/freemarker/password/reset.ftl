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
	<style type="text/css">
      
    </style>
 
    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
  </head>
  <body>
  	
<div class="mf_header">
		<div >
      <img src="${rc.contextPath}/res/img/captura-header.png"  />
			<a href="${rc.contextPath}/login/login.mob" class="btn btn-small btn-primary" style="float:right" >${i18n('web.login.submit')}</a>
		</div>
</div>

	<div class="recoveryBox content" >
			<#if validToken>
			
		    <p class="lead" >${legend}</p>
		    <div id="mainDiv">
				<form id="reset" method="post">
					<fieldset>
						<input type="hidden" name="resetToken" value="${resetToken}" />
						<input type="hidden" name="mail" value="${mail}" />
						<label>${i18n('web.generic.enterPassword')}</label>
						<input id="password1" type="password" name="password1" /> 
						<label>${i18n('web.generic.confirmPassword')}</label>
						<input id="password2" type="password" name="password2" /> 
						<br/>
						
					</fieldset>
				</form>
			</div>
			<div id="progressBlock"  style="display:none;" >
    				<img src="${rc.contextPath}/res/img/login-ajax.gif"  />
    		</div>
			<div class="recoveryBottom" >
				
				<div id="resetMessage" style="float:left;width:60%"  >
				</div>
				<input type="button" id="okButton" value="${i18n('web.generic.ok')}" class="btn btn-success"  style="float:right;"/>
				<div style="clear:both" />
			</div>
		<#else>
			${invalidMessage}
		</#if>
	</div>
	
	<script type="text/javascript" src="${rc.contextPath}/res/js/json2.js"></script>
	<script type="text/javascript" data-main="${rc.contextPath}/acme/js/reset.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
</body>







