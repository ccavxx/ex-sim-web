<%@page import="com.topsec.tal.base.util.StringUtil"%>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.topsec.tsm.license.GetServerLicenseCodeUtil"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="zh-CN">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>获取服务器机器码</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style type="text/css">
body,
.control-label {
	font-size: 12px;
	font-weight: bolder;
}
</style>
<!-- Bootstrap -->
<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
<!-- EasuUI -->
<link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
<link href="/js/jquery-easyui/themes/icon.css" rel="stylesheet" type="text/css">
<!-- sim-icon -->
<link href="/css/sim-icon.css" rel="stylesheet" type="text/css">
<link href="/css/system.css" rel="stylesheet">
</head>
<body>
	<%
		request.setCharacterEncoding("utf-8");
		String companyName = StringUtil.trim(request.getParameter("paramKey"));
		String companyNameVal = "";
		if (StringUtil.isNotBlank(companyName)) {
			companyNameVal = GetServerLicenseCodeUtil.getLicenseCode(companyName);
			request.setAttribute("companyNameVal", companyNameVal);
			response.getWriter().write(companyNameVal);
		} else {
			companyName = "";
		}
	%>
<div class="easyui-panel" data-options="title:'获取服务器机器码',fit:true,headerCls:'sim-panel-header',bodyCls:'sim-panel-body'">
	<div class="alert alert-info">
		<strong>描述 </strong>
		请输入公司名称来获取对应的服务器机器码。
	</div>
	<div class="row-fluid">
		<div class="span7">
			<form class="bs-docs-example form-horizontal" >
				<div class="control-group">
					<label class="control-label">公司名称</label>
					<div class="controls"><input type="text" maxlength="100"　style="width:150px" id="companyNameId" /></div>
				</div>
				<div class="control-group">
					<label class="control-label">服务器机器码</label>
					<div class="controls"><textarea id="textareaVal" readonly style="width:350px; height:80px"></textarea></div>
				</div>
				<div class="control-group">
					<div class="controls" >
						<a class="easyui-linkbutton" id="copyTextBtn" data-options="iconCls:'icon-apply'" >复制到粘贴板</a>　<a class="easyui-linkbutton" href="/" data-options="iconCls:'icon-cancel'" >返回登录页面</a>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>
</body>
</html>
<script src="/js/global/jquery-1.8.3.js"></script>
<script src="/js/jquery-easyui/jquery.easyui.js"></script>
<!-- 复制到剪切板 -->
<script type="text/javascript" src="/js/global/ZeroClipboard.js"></script>
<script src="/js/global/system.js"></script>
<script src="/js/sim/sysconfig/licensegen.js"></script>