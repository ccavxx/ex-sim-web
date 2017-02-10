<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="zh-CN">
	<head>
		<meta charset="utf-8">
		<title><%=CommonUtils.getProductName() %></title>
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <link href="/css/bootstrap.css" rel="stylesheet" media="screen">
	    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
	    <link href="/css/system.css" rel="stylesheet" type="text/css">
	    <style type="text/css">
	    	*{
	    		font-size: 14px;
	    		font-family: "微软雅黑","Helvetica Neue", Helvetica, Arial, sans-serif
	    	}
	    	.shell_result{
	    		background-color: #000 ; 
	    		color: #FFF ; 
	    		font-size: 16px; 
	    		cursor: default; 
	    		padding: 0px; 
	    		margin: 0px; 
	    		border:0px; 
	    		width: 100%; 
	    		height: 100%;
	    	}
	    	.horizon-mask-msg{
	    		padding: 5px 5px 10px 30px;
	    		z-index:2000;
	    	}
	    	#tree_menu{
	    		-webkit-border-radius:4px;
	    		-moz-border-radius:4px;
	    		border-radius:4px;
	    		padding: 0px;
	    	}
	    </style>
	</head>
	<body>
		<div class="easyui-layout sim" fit="true">
			<div id="loginInfo" data-options="region:'north',title:'${type}到${param.ip}[未登录]',height:68">
				<form id="loginForm" class="form-inline" style="padding: 5px;margin: 0px;" onsubmit="return false" onkeydown="att.enterHandler(event, att.remoteLogin)">
					<input type="hidden" id="type" value="${type}">
					<input type="hidden" id="ip" value="${param.ip}">
					<input type="hidden" id="connectionId">
					<span>用户名：</span><input id="username" type="text" class="input-small" value="${accountName}" autocomplete="off">
					<span>密码：</span><input id="password" type="password" class="input-small" value="${accountPassword}" autocomplete="off">
					<span>端口：</span><input id="port" type="text" class="input-mini" value="${defaultPort}">
					<span>字符集：</span>
					<select id="charset" style="width: 100px;" onchange="att.changeCharset()">
						<option value="">无</option>
						<option value="UTF-8">UTF-8</option>
						<option value="GBK">GBK</option>
						<option value="UNICODE">UNICODE</option>
						<option value="GB18030">GB18030</option>
					</select>
					<c:forEach items="${clientProperties}" var="inputItem">
						<span>${inputItem.label}：</span><input name="property_${inputItem.inputName}" type="text" class="input-small">
					</c:forEach>
					<button type="button" class="btn" onclick="att.remoteLogin(false)">登录</button>
					<button type="button" class="btn" onclick="att.quitLogin()">退出</button>
				</form>
			</div>
			<div id="resultPanel" data-options="region:'center',closed:true,border:false">
				<div class="easyui-layout" data-options="fit:true">
					<c:choose>
						<c:when  test="${type eq 'ssh' or type eq 'telnet' or 
						                 type eq 'mysql' or type eq 'sqlserver' or 
						                 type eq 'db2' or type eq 'oracle' or 
						                 type eq 'informix'}">
							<div data-options="region:'west',fit:true,border:false">
								<div class="easyui-layout" data-options="fit:true">
									<div style="padding:5px;height:36px;overflow:hidden;" data-options="region:'north'">
										<div class="input-append" style="margin-bottom:0">
										  <input id="command" type="text" style="width:450px;font-size:14px;" onkeydown="att.enterHandler(event,att.execCommand)">
										  <button type="button" class="btn" onclick="att.execCommand()">执行命令</button>
										</div>
									</div>
									<div data-options="region:'center'">
										<c:choose>
											<c:when test="${type eq 'ssh' or type eq 'telnet' }">
												<textarea id="resultContainer" 
												          style="background-color: #000 ; color: #FFF ; font-size: 16px; cursor: default; padding: 0px; margin: 0px; border:0px; width: 100%; height: 100%;"
												          readonly="readonly"></textarea>
											</c:when>
											<c:otherwise>
												<div id="resultContainer" style="width: 100%;height: 100%;"></div>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</c:when>
						<c:when test="${type eq 'sftp' or type eq 'ftp' }">
							<div data-options="region:'center',border:false">
								<ul id="dir_tree"/>
								<div id="tree_menu" class="easyui-menu" style="width:100px;">
									<div onclick="att.downloadFile()" data-options="iconCls:'icon-download'">下载</div>
									<div onclick="att.deleteFile()" data-options="iconCls:'icon-remove'">删除</div>
									<div onclick="att.refreshDir()" data-options="iconCls:'icon-refresh'">刷新</div>
									<div onclick="att.createDir()" data-options="iconCls:'icon-add'">创建目录</div>
								</div>
							</div>
							<div data-options="region:'south',height:40,border:false">
								<div style="height: 1px;background-color: #80B0CE"/>					
								<form class="form-inline" style="padding: 5px;margin: 0px;" onsubmit="return false">
									<button type="button" class="btn" onclick="att.createDir()">创建目录</button>
									<button type="button" class="btn" onclick="att.downloadFile()">下载</button>
									<button type="button" class="btn" onclick="att.deleteFile()">删除</button>
									<input id="uploadFile" type="file" class="horizon-fileupload" name="uploadFile"/>
									<a class="easyui-linkbutton" data-options="iconCls:'icon-import',plain:false" href="#" onclick="att.uploadFile()">上传</a>　
								</form>
							</div>
						</c:when>
					</c:choose>
				</div>
			</div>
		</div>
	</body>
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
  	<script src="/js/validator/jquery.validator.js"></script>
  	<script src="/js/sim/asset/asset_tools.js"></script>
  	<script src="/js/global/system.js"></script>
  	<script src="/js/global/ajaxfileupload.js"></script>
  	 <script type="text/javascript">
	     $(function(){
	       $.messager.defaults={ok:"确定",cancel:"取消"};
	     });
	    </script>
  	
</html>