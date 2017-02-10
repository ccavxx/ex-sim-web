<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="x-ua-compatible" content="IE=8">
    <title>RDP</title>
   	<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
    <link href="/css/system.css" rel="stylesheet" type="text/css">
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
    
   
</head>
<body>
    <script type="text/javascript">
    		function openMSTSC(ip,port){
			   	if(!port){
			   		port = 3389 ;
			   	}
			   	var objShell = new ActiveXObject("wscript.shell");      
			   	objShell.Run("mstsc /v:"+ip+":"+port+" /f");      
		   }
			function login(){
				openMSTSC($("#ip").val(), $("#username").val(), $("#password").val(), $("#port").val()) ;
			}
	</script>
	<div class="easyui-layout sim" fit="true">
		<div id="loginInfo" data-options="region:'north',title:'远程桌面到${param.ip}',height:68">
			<form id="loginForm" class="form-inline" style="padding: 5px;margin: 0px;" onsubmit="return false" onkeydown="enterHandler(event, login)">
				<input id="ip" type="hidden" value="${param.ip}"/>
				<span>用户名：</span><input id="username" type="text" class="input-small" value="${accountName}" autocomplete="off">
				<span>密码：</span><input id="password" type="password" class="input-small" value="${accountPassword}" autocomplete="off">
				<span>端口：</span><input id="port" type="text" class="input-mini" value="${empty defaultPort ? '3389' : defaultPort}">
				<button type="button" class="btn" onclick="login();">连接</button>
			</form>
		</div>
	</div>
</body>
</html>