<%@ page language="java" pageEncoding="utf-8"%>
<link href="/css/login.css" rel="stylesheet" type="text/css" />
<div class="lock_background">
	<div class="pic01"></div>
	<div class="input_sd" align="center">
			<div style="font-size:200%;text-align:center;margin-top:30%;">${sessionScope.sid.userName}已锁定</div></br>
			<div id="error" style="font-size:200%;text-align:center;color: red"></div><br>
			<span style="font-size:200%">密码：</span>
			<input id="password" type="password" placeholder="密码" onkeydown="enterEventHandler(event,unlock)"/>&emsp;
			<a style="font-size:200%;" href="javascript:unlock();"><img src="/img/lock_img.jpg"></a>
	</div>
	<div class="pic02"></div>
</div>
