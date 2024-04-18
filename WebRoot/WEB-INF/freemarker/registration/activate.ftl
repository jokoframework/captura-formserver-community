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
			<a href="${rc.contextPath}/login/login.mob" class="btn btn-small btn-primary" style="float:right" >Sign in</a>
		</div>
</div>


			
<div class="activationBox" >
	<p class="lead" >${message!}</p>	
	<p class="lead" >
	 	<#if showInvitationToLogin>${i18n('web.invitation.proceed_to')} <a href="${rc.contextPath}/login/login.mob">${i18n('web.invitation.login')}</a></#if>
	</p>
	<p >
		${hintOnError!}
	</p>
</div>


<script data-main="${rc.contextPath}/acme/js/activate.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>


</body>

</html>




