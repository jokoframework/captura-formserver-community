<#include "/templates/header.ftl">

<div id="messageDiv">
	${message} 
	<#if loginURI??>
	 <br />
	 <br />
	<p>${i18n('web.invitation.proceed_to')} <a href="${loginURI}">${i18n('web.invitation.login')}</a></p>
	</#if>
</div>

<!-- Le javascript -->
<script type="text/javascript" src="${rc.contextPath}/bootstrap/js/bootstrap.js"></script>
<#include "/templates/footer.ftl">