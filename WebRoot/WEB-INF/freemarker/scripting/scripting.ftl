<html>
	<head>
		<title>Scripting</title>
		<link type="text/css" href="${rc.contextPath}/res/css/jquery-ui/ui-lightness/jquery-ui-1.8.16.custom.css" rel="stylesheet" />

		<script type="text/javascript" src="${rc.contextPath}/res/js/codemirror/codemirror.js"></script>
		<script type="text/javascript" data-main="${rc.contextPath}/acme/js/scripting.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
	</head>
	<body>	
		<div id="tabs">
			<ul>
				<li><a href="#code"><b>Groovy Script</b></a></li>
				<li><a href="#output">Console</a></li>
				<li><a href="#list">Saved Scripts</a></li>
			</ul>
			<div id="code">
				<div style="width:100%; padding:0px">
				<form id="scriptForm" method="post" action="script.ajax" accept-charset="UTF-8" enctype="application/x-www-form-urlencoded">
					<input type="hidden" name="scriptName" id="scriptNameHidden">
					<div style="background-color: #FFFFFF;width:100%; padding:0px; height: 400px; border: 1px solid #CCCCCC; text-align: left">
						<textarea style="width:100%; height: 100%;font-family: monospace; font-size: 12px;" name="script" id="script">${script}</textarea>
					</div>
				</form>
				<div id="ajaxLoading" style="text-align: center"><img src="${rc.contextPath}/res/images/ajax-loader.gif" /> </div>
				<div style="text-align: center; margin-top: 10px">
					<input type="button" value="Execute" id="executeButton" />
					<input type="button" value="Save" id="saveButton" />
				</div>
				</div>
			</div>
			<div id="output">
				<pre id="out" style="background: black; padding:2px; font-family: monospace; font-size: 14px; color: #C0C0C0; width:100%; height:400px; overflow: auto;">${out}</pre>
			</div>
			<style type="text/css">
				.scriptLink{}
			</style>
			<div id="list">
				<div id="scriptList" style="height: 400px; overflow: auto">
					<ul>
						<#list scripts as s>
						<li><a href="javascript:void(0)" class="scriptLink" data="${s.id}">${s.name}</a></li>
						</#list>
					</ul>
				</div>
			</div>
			<div id="saveDialog">
				<br/>Name : <input type="text" id="scriptName"/><span id="scriptNameErrorMessage" style="color: #EEEEEE; background-color: red"></span><br />
			</div>
		</div>
		<style type="text/css">
		.CodeMirror-line-numbers {
		  font-family: "DejaVu Sans Mono", courier, monospace;
		  font-size: 10pt;
		  color: black;
		  background-color: transparent;
		  line-height: 16px;
		  padding-top: .4em;
		  background-color: #EEEEEE
		}
		</style>
		<script type="text/javascript">
		var params = { parserfile: ["tokenizegroovy.js", "parsegroovy.js"],
		  path: "${rc.contextPath}/res/js/codemirror/",
		  stylesheet: "${rc.contextPath}/res/css/codemirror/groovycolors.css",
		  lineNumbers : true,
		  textWrapping: false 
		 };
		  if(!MF){
		  	var MF ={};
		  };
		  MF.Scripting = {};
		  MF.Scripting.editor = CodeMirror.fromTextArea("script", params);
		</script>
	</body>
</html>