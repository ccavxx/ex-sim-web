<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.security.interfaces.RSAPublicKey"%>
<%@page import="java.security.PublicKey"%>
<%@page import="com.topsec.tsm.common.SIMConstant"%>
<%@page import="com.topsec.tsm.util.encrypt.RSAUtil"%>
<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=CommonUtils.getProductName() %></title>
<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
<link href="/css/login.css" rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="denglu">
		<div class="denglu-left">
			<!-- 代码 开始 -->
			<div class="carousel">
				<ul class="carousel-pic">
					<!-- <li><a><img src="/img/login/pictu1.jpg" /> </a> </li> -->
					<li><a><img src="/img/login/pictu2.jpg" /></a> </li>
				</ul>
				<a class="prev" href="javascript:void(0)"></a> <a class="next"
					href="javascript:void(0)"></a>
				<div class="num">
					<ul></ul>
				</div>
			</div>
		</div>
		<div class="denglu-right">
			<div class="donglulog">
				<img src="<%=CommonUtils.getCompanyLogo()%>">
			</div>
			<div class="donglutit">
				<img src="/img/login/yhdl.png" />
			</div>
			<div class="${empty error ? '' : 'alert alert-error' } login-error" id="msg">${error}</div>
			<form id="loginForm" name="loginForm" style="margin-left:60px;" class="form-horizontal" method="post" onsubmit="return checkLogin();" action="/sim/userLogin/login">
				<div class="control-group">
				    <label class="control-label" for="loginName">用户名：</label>
				    <div class="controls">
				      <input type="text" name="loginName" id="loginName">
				    </div>
				</div>
				<div class="control-group">
				    <label class="control-label" for="password">密码：</label>
				    <div class="controls">
				      <input type="password" name="password" id="password" value="${empty validCodeError ? '' : param.password }">
					  <input id="encryptPassword" name="encryptPassword" type="hidden">
				    </div>
				</div>
				<div class="control-group">
				    <label class="control-label" for="validCode">验证码：</label>
				    <div class="controls">
				      <div style="display: inline;">
					      <input type="text" name="validCode" id="validCode" style="width: 50px;">
					      <img id="validCodeImg" src="/validCode" onclick="changeValidCode()" title="点击切换一张" style="width: 70px;height: 27px;cursor: pointer;" >
				      </div>
				    </div>
				</div>
				<div class="but">
					<ul>
						<li class="butjj"><input value="登&nbsp;&nbsp;录" type="submit"
							class="butys"
							onmouseover="this.style.backgroundImage='url(/img/login/but2.png)'"
							onmouseout="this.style.backgroundImage='url(/img/login/but1.png)'" />
						</li>
						<li><input value="重&nbsp;&nbsp;置" class="butys" type="button" onclick="resetLoginInfo()"
							onmouseover="this.style.backgroundImage='url(/img/login/but2.png)'"
							onmouseout="this.style.backgroundImage='url(/img/login/but1.png)'" />
						</li>
					</ul>
					<a href="javascript:mdfPwdPage();" style="margin-left:10px;text-decoration:underline;font-size:12px;">更改密码</a>
				</div>
				<input id="client_public_key" name="clientPublicKey" type="hidden">
			</form>
		</div>
	</div>
	<div class="banq">版权所有：<%=CommonUtils.getCompanyName() %></div>
<!-- 同用户登录提示框开始{ -->
<div id="login_modal" class="modal hide fade" >
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h3>提示</h3>
	</div>
	<div class="modal-body">
		<p id="login_modal_msg"></p>
	</div>
	<div class="modal-footer" style="padding:8px 15px;">
		<button id="login_modal_ok" class="btn btn-primary">确定</button>
		<button id="login_modal_cancel" class="btn btn-primary">取消</button>
	</div>
</div>
<!-- 同用户登录提示框结束} -->
	<script src="/js/global/jquery-1.8.3.min.js" type="text/javascript"></script>
	<script src="/js/global/jquery.md5.js" type="text/javascript"></script>
	<script src="/js/global/bootstrap.min.js"></script>
	<script src="/js/global/jquery.superslide.2.1.1.js" type="text/javascript"></script>
	<script src="/js/global/base64.js" type="text/javascript"></script>
	<script src="/js/global/cryptico.js" type="text/javascript"></script>
	<script src="/js/sim/init.js" type="text/javascript"></script>
	<script src="/js/login/login.js" type="text/javascript"></script>
	<script type="text/javascript">
		$(function(){
			system.init("<%=RSAUtil.getB64CachedPublicKey(SIMConstant.COMMON_KEY_PAIR_CACHE_ID,"|")%>",<%=RSAUtil.DEFAULT_KEY_LENGTH%>) ;
			if(${not empty validCodeError}){
				$("#password")[0].focus() ;
			}else if(${not empty pwdError}){
				$("#password")[0].focus() ;
			}else{
				$("#loginName")[0].focus() ;
			}
			if(${not empty loginName}){
				var loginName = "${fn:replace(fn:replace(loginName,"\\","\\\\"),"\"","\\\"")}" ;
				$("#loginName").val(loginName);
			}
		});
	</script>
</body>
</html>
