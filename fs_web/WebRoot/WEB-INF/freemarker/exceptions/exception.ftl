<html>
	<head>
		<title>Uncaught Exception</title>
	<head>
	<body style="background-color">
		<div style="text-align:center; color: red">
			<h1>Something is wrong!</h1>
			<b>${exceptionType}</b>
			<br />
			${message!}
			<br />
			<br />
			<img src="${rc.contextPath}/res/images/tumblebeast.png" alt="tumblebeast" />
			<br />
			<br/>
			<div style="text-align : left">
				<pre style="background: black; padding:2px; font-family: monospace; font-size: 14px; color: #C0C0C0; width:100%; height:400px; overflow: auto;">${stackTrace}</pre>
				</div>
		</div>
	</body>
</html>