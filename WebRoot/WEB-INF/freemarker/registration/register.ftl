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

<#if registrationDisabled>
	<h1 style="text-align:center">${i18n('web.registration.disabled')}</h1>
<#else>
<div class="marketing" >
	<h1>${i18n('web.registration.label01')}</h1>
</div>	
<div class="container-fluid mf_container">
	<div class="row-fluid">	
		<div class="span6 content">
		<div class="row-fluid">	
			<div class="span12">
			<form id="registrationForm" >
				
				<div class="smallInputs" >
					<div style="width:50%;float:left" >
						<div class="control-group">
							<label class="control-label" for="firstName">${i18n('web.generic.firstname')}</label>
							<div class="controls">
								<input name="firstName" id="firstName" type="text" style="width:80%" />
								<span id="firstName_validation_msg" class="validation_msg help-inline" />
							</div>
						</div>
					</div>
					<div style="width:50%;float:right" >
						<div class="control-group">
							<label class="control-label" for="lastName">${i18n('web.generic.lastname')}</label>
							<div class="controls">
								<input name="lastName" id="lastName" type="text" style="width:80%" />
								<span id="lastName_validation_msg" class="validation_msg help-inline" />
							</div>
						</div>
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="mail">${i18n('web.generic.mail')}</label>
					<div class="controls">
						<input name="mail" id="mail" type="text" />
						<span id="mail_validation_msg" class="validation_msg help-inline" ></span>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="password">${i18n('web.generic.password')}</label>
					<div class="controls">
						<input name="password" id="password" type="password" />
						<span id="password_validation_msg" class="validation_msg help-inline" ></span>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="password2">${i18n('web.generic.confirmPassword')}</label>
					<div class="controls">
						<input name="password2" id="password2" type="password" />
					</div>
				</div>
				<label class="control-label" for="captcha">${i18n('web.registration.captcha')}</label>
				<div class="captchaGroup">
				<div class="control-group">
					<div class="controls">
						<img src="${rc.contextPath}/api/public/captcha" id="captcha_img" />
						<a id="refreshCaptchaButton" href="javascript:void(0)" class="glyphicons refresh"><i></i></a>
					</div>					
				</div>
				<input  id="captcha" type="text"  />
				</div>
			</form>
			</div>
			</div>
			<div class="row-fluid">	
				<div class="span9">
					
					<div id="registrationMessage"   ></div>
				</div>
				<div class="span2">
					<input type="button" id="submitUser" value="${i18n('web.registration.submit')}" class="btn btn-success"  >
				</div>
				<div class="span1">
				</div>
			</div>
		</div>
		<div class="span6">
		<div class="marketing" >
		   <h3>${i18n('web.registration.marketing.title')}</h3>
		   <p class="lead" >
		   	${i18n('web.registration.marketing.opt1')}
		   <p>
  			<div class="registerThumbnail" >
  				<img src="${rc.contextPath}/res/images/formExample.png" width="200px" ></img>
  			</div>
  			<p class="lead" >
		   		${i18n('web.registration.marketing.opt2')} 
		   	<p>
		   	<div>
		   		<!-- img src="${rc.contextPath}/res/images/logo_ios.png" width="50px" id="ios_img" ></img -->
		   		<img src="${rc.contextPath}/res/images/logo_android.png" width="50px" id="android_img" ></img>
		   	</div>
		   	</div>
		   	
			<div id="someOtherFeatures" >
				<p class="lead" >${i18n('web.registration.marketing.opt3')}</p>
				<p class="lead" >${i18n('web.registration.marketing.opt4')}</p>
				<p class="lead" >${i18n('web.registration.marketing.opt5')}</p>
				<p class="lead" >${i18n('web.registration.marketing.opt6')}</p>
			</div>
			
		</div>
	</div>	

	<div class="row-fluid footerEmpty">
	
	</div>
</div>
</#if>

<!-- Le javascript -->
<script type="text/javascript" src="${rc.contextPath}/res/js/json2.js"></script>
<script type="text/javascript" data-main="${rc.contextPath}/acme/js/registration.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>
<script type="text/javascript" src="${rc.contextPath}/res/glyphicons/scripts/modernizr.js"></script>
<#-- 
<#include "/templates/footer.ftl">
-->
