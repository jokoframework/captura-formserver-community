<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Chake</title>
    
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
		</div>
</div>


			
<div class="recoveryBox content" >
	<p class="lead" >${i18n("web.account.activation.title")}</p>
	
	<form id="activationForm">
		<label class="control-label" for="captcha">${i18n('web.account.activation.message')}</label>
		<br/>
		<div class="captchaGroup">
			<div class="control-group">
				<div class="controls">
					<img src="${rc.contextPath}/api/public/captcha" id="captcha_img" />
					<a id="refreshCaptchaButton" tabindex="-1" href="javascript:void(0)" class="glyphicons refresh"><i></i></a>
				</div>					
			</div>
			<input  id="captcha" type="text"  />
		</div>
	</form>
	<div id="progressBlock"  style="display:none;" >
    		<img src="${rc.contextPath}/res/img/login-ajax.gif"  />
    </div>
	<div class="recoveryBottom" >
		
		<div id="activationMessage" style="float:left"  >
		</div>
		<input type="button" id="acceptBtn" value="${i18n('web.account.activation.accept')}" class="btn btn-success"  style="float:right;"/>
		<div style="clear:both" />
	</div>
	</div>
</div>


<script type="text/javascript" data-main="${rc.contextPath}/acme/js/account-activation.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>


</body>

</html>