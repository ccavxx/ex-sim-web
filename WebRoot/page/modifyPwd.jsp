<%@page import="org.springframework.web.util.HtmlUtils"%>
<%@page import="com.topsec.tsm.common.SIMConstant"%>
<%@page import="com.topsec.tsm.util.encrypt.RSAUtil"%>
<%@page import="com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil"%>
<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@page contentType="text/html; charset=utf8" pageEncoding="utf8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="zh-CN">
<head>
<title>更改密码</title>
<meta charset="utf-8">
<!-- Bootstrap -->
<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
<!-- EasuUI -->
<link href="/js/jquery-easyui/themes/bootstrap/easyui.css"
	rel="stylesheet" type="text/css">
<link href="/js/jquery-easyui/themes/icon.css" rel="stylesheet"
	type="text/css">
<!-- Validation -->
<link href="/js/validator/jquery.validator.css" rel="stylesheet"
	type="text/css">
<style>
.logo {
	display: block;
	float: left;
	width: 350px;
	background: url('/img/skin/top/logo.png') no-repeat;
	line-height: 50px;
	min-height: 50px;
	filter: progid:DXImageTransform.Microsoft.gradient(enabled=false );
	/*IE下不使用滤镜*/
}

input[type="text"],input[type="password"] {
	height: 30px;
}
</style>
</head>
<body>
	<div class="row-fluid">
		<div class="span12"
			style="height:100px;background-color:rgb(176, 227, 253);padding:20px 0 0 20px;">
			<div class="logo" style="background: url('<%=CommonUtils.getCompanyLogo()%>') no-repeat;" href="javascript:void(0)"></div>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span3"></div>
				<div class="span6">
					<div id="myModal"
						style="border:1px solid #ccc;margin-top:50px;background:url('/img/mdfPwd.png') no-repeat;">
						<div class="modal-header" style="height:55px;border-color:#ccc;">
							<h3 id="myModalLabel" style="line-height:55px;color:red;">
								更改密码</h3>
						</div>
						<div class="modal-body" style="padding-bottom:0px;">
							<form id="userMdfPwdFormId" class="form-horizontal">
								<fieldset>
									<div class="control-group">
										<label class="control-label" for="userNameId">用户名</label>
										<div class="controls">
											<input id="userNameId" placeholder="用户名" type="text"
												name="userName" />
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="oldPasswordId">密码</label>
										<div class="controls">
											<input id="oldPasswordId" placeholder="密码" type="password"
												name="oldPassword" />
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="passwordId">新密码</label>
										<div class="controls">
											<input id="passwordId" placeholder="密码" type="password"
												name="password" />
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="passwordAgainId">确认密码</label>
										<div class="controls">
											<input id="passwordAgainId" placeholder="确认密码"
												type="password" name="passwordAgain" />
										</div>
									</div>
									<div class="control-group">
										<div class="controls">
											<button id="mdfPwdSubmit" type="button"
												class="btn btn-primary">提交更改</button>
											<a href="/" target="_top"
												style="font-size:12px;margin-left:10px;text-decoration:underline;">跳转到登录页面</a>
										</div>
									</div>
								</fieldset>
							</form>
						</div>
					</div>
				</div>
				<div class="span3"></div>
			</div>
		</div>
	</div>
	<script src="/js/global/jquery-1.8.3.js"></script>
	<script src="/js/global/bootstrap.js"></script>
	<script src="/js/global/base64.js"></script>
	<script src="/js/global/cryptico.js"></script>
	<script src="/js/sim/init.js" type="text/javascript"></script>
	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
	<script src="/js/jquery-easyui/datagrid-filter.js"></script>
	<script src="/js/jquery-easyui/datagrid-detailview.js"></script>
	<script src="/js/jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
	<script src="/js/validator/jquery.validator.js"></script>
	<script src="/js/validator/local/zh_CN.js"></script>
	<script src="/js/global/system.js"></script>
	<script type="text/javascript">
		$(function() {
			 <%
				String minCount = SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minCount");
				String minUpperCount = SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minUpperCount");
				String minLowerCount = SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minLowerCount");
				String minNumCount = SafeMgrConfigUtil.getInstance().getSafeMgrConfigValueByName("minNumCount");
				String loginName = HtmlUtils.htmlEscape(request.getParameter("loginName"));
			%>
			system.init("<%=RSAUtil.getB64CachedPublicKey(SIMConstant.COMMON_KEY_PAIR_CACHE_ID,"|")%>",<%=RSAUtil.DEFAULT_KEY_LENGTH%>) ;
			var minCount = <%=minCount%>;
			var minUpperCount = <%=minUpperCount%>;
			var minLowerCount = <%=minLowerCount%>;
			var minNumCount = <%=minNumCount%>;
			var loginName = "<%=loginName%>";

			var $userMdfPwdFormId = $("#userMdfPwdFormId");
			// 验证密码
			function validatePwd() {
				var flag = true;
				var passwordObj = $("#passwordId");
				var passwordVal = passwordObj.val();

				if (passwordVal.replace(/^\s*/, "") == "") {
					$userMdfPwdFormId.validator("showMsg", "#passwordId", { type : "error", msg : "密码不能为空，请输入！" });
					flag = false;
					return false;
				} else {
					if (passwordObj.val().length < parseInt(minCount)) {
						$userMdfPwdFormId.validator("showMsg", "#passwordId", {type: "error", msg:"密码长度必须至少为" + minCount + " 个字符！" });
						flag = false;
						return false;
					}
					if (parseInt(minUpperCount) > 0) {
						if (!checkUpperCaseNum(passwordObj, parseInt(minUpperCount))) {
							$userMdfPwdFormId.validator("showMsg", "#passwordId", { type : "error", msg : "密码长度必须至少含有 " + minUpperCount + " 个大写字母！" });
							flag = false;
							return false;
						}
					}
					if (parseInt(minLowerCount) > 0) {
						if (!checkLowerCaseNum(passwordObj,
								parseInt(minLowerCount))) {
							$userMdfPwdFormId.validator("showMsg", "#passwordId", { type : "error", msg : "密码长度必须至少含有 " + minLowerCount + " 个小写字母！" });
							flag = false;
							return false;
						}
					}
					if (parseInt(minNumCount) > 0) {
						if (!checkNumCaseNum(passwordObj, parseInt(minNumCount))) {
							$userMdfPwdFormId.validator("showMsg", "#passwordId", { type : "error", msg : "密码长度必须至少含有 " + minNumCount + " 个数字！" });
							flag = false;
							return false;
						}
					}
					if (passwordObj.val().length > 20) {
						$userMdfPwdFormId.validator("showMsg", "#passwordId", { type : "error", msg : "密码长度必须少于 20 个字符！" });
						flag = false;
						return false;
					}
					$userMdfPwdFormId.validator("hideMsg", "#passwordId");
					flag = true;
				}
				return flag;
			}
			// 判断字符中str出现大写字母是否至少出现num次
			function checkUpperCaseNum(str, num) {
				var len = str.val().length;
				var m = 0;
				for ( var i = 0; i < len; i++) {
					var c = str.val().charAt(i);
					if (c >= 'A' && c <= 'Z') {
						m++;
					}
				}
				if (m >= num) {
					return true;
				}
				return false;
			}
			// 判断字符中str出现小写字母是否至少出现num次
			function checkLowerCaseNum(str, num) {
				var len = str.val().length;
				var m = 0;
				for ( var i = 0; i < len; i++) {
					var c = str.val().charAt(i);
					if (c >= 'a' && c <= 'z') {
						m++;
					}
				}
				if (m >= num) {
					return true;
				}
				return false;
			}
			// 判断字符中str出现数字是否至少出现num次*
			function checkNumCaseNum(str, num) {
				var len = str.val().length;
				var m = 0;
				for ( var i = 0; i < len; i++) {
					var c = str.val().charAt(i);
					if (c >= 0 && c <= 9) {
						m++;
					}
				}
				if (m >= num) {
					return true;
				}
				return false;
			}
			$userMdfPwdFormId.validator({
				rules:{
					validPassword: function(element){
			            return $.ajax({
			                url: '/sim/authUser/checkOldPassword',
			                type: 'post',
			                data: {userName:$("#userNameId").val(),oldPassword:rsaEncrypt(element.value)},
			                dataType: 'json'
			            });
		        	}
				},
				theme : "simple_right",
				showOk : "",
				fields : {
					"userName" : "required;remote[/sim/authUser/checkUserName];",
					"oldPassword" : "required;validPassword;",
					"password" : "新密码:required;",
					"passwordAgain" : "required;match[password]"
				}
			});
			if (loginName) {
				$("#userNameId").val(loginName).blur();
			}
			$("#mdfPwdSubmit").click(
							function() {
								var userMdfPwdFormIdObj = $userMdfPwdFormId.data("validator");
								$userMdfPwdFormId.trigger("validate");
								var validPass = userMdfPwdFormIdObj.isFormValid();
								if (!validPass || !validatePwd()) {
									return false;
								}
								var dataArray = $userMdfPwdFormId.serializeArray();
								var formdata = {};
								$.map(dataArray, function(data) {
									if(data.name == "password" || data.name == "oldPassword" || data.name == "passwordAgain"){
										formdata[data.name] = rsaEncrypt(data.value) ;
									}else{
										formdata[data.name] = data.value;
									}
								});
								$.post("/sim/authUser/modifyUserPwd", 
										formdata,
										function(dataJson){
											if (dataJson.result == "success") {
												showAlertMessage("密码修改成功！") ;
												setTimeout(function(){
													var loginName = $("#userNameId").val();
													window.location.href = "/page/login.jsp?loginName=" + (loginName ? loginName : "");
												}, 2500) ;
											} else {
												showErrorMessage("更改密码失败：" + dataJson.msg);
											}
										},
										"json"
								);
							});
		});
	</script>
</body>
</html>