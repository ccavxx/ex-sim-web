<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<html>
	<head>
	    <title><%=CommonUtils.getProductName() %></title>
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <link href="/css/bootstrap.css" rel="stylesheet" media="screen">
	    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
	    <link href="/css/system.css" rel="stylesheet">
	    <style type="text/css">
	    	*{
	    		font-size: 14px;
	    		font-family: "微软雅黑","Helvetica Neue", Helvetica, Arial, sans-serif
	    	}
	    </style>
	</head>
	<body>
		<div class="easyui-layout sim" fit="true">
			<div data-options="region:'north',title:'SSH到${param.ip}',height:68">
				<form class="form-inline" style="padding: 5px;margin: 0px;" onsubmit="return false">
					<input type="hidden" id="ip" value="${param.ip}">
					<input type="hidden" id="connectionId">
					<span>用户名：</span><input id="username" type="text" class="input-medium" value="${accountName}">
					<span>密码：</span><input id="password" type="password" class="input-medium" value="${accountPassword}">
					<span>端口：</span><input id="port" type="text" class="input-mini" value="22">
					<button class="btn" onclick="att.sshLogin()">登录</button>
				</form>
			</div>	
			<div data-options="region:'west',width:600,split:true">
				<div style="padding: 5px;">
					<div class="input-append">
					  <input id="command" style="width:450px;height: 24px;" onkeydown="att.onKeyDown(event)">
					  <button class="btn" onclick="att.sshExec()">执行命令</button>
					</div>
				</div>
				<div>
					<textarea id="output" style="width: 99%;height:80%;" readonly="readonly"></textarea>
				</div>
			</div>
			<div data-options="region:'center',width:300">右侧树</div>
		</div>
	</body>
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
  	<script src="/js/sim/asset/asset_tools.js"></script>
  	<script src="/js/global/system.js"></script>
</html>