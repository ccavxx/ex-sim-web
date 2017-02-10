function mdfPwdPage(){
	var loginName = $("#loginName").val();
	window.location.href = "/page/modifyPwd.jsp?loginName=" + (loginName ? loginName : "");
}
function resetLoginInfo(){
	$("#loginName").val("") ;
	$("#password").val("") ;
	$("#validCode").val("") ; 
	$("#loginName")[0].focus() ;
}
//切换验证码
function changeValidCode(){
	$("#validCodeImg").attr("src","/validCode?_time="+new Date().getTime()) ;
}
function openWarnWindow(msg){
	$("#login_modal_msg").text(msg);
	$("#login_modal").modal("show");
}
function closeWarnWindow(){
	$("#login_modal").modal("hide");
}
$("#login_modal").on("shown", function(){
	$("#login_modal_ok").focus();
});
function checkLogin(){
	var loginName = $("#loginName").val() ;
	var password = $("#password").val() ;
	if(loginName == "" || password == ""){
		$("#msg").addClass("alert alert-error").html("账号、密码不能为空！") ;
		return false ;
	}	
	if(loginName.indexOf("<") >= 0 || loginName.indexOf(">") >= 0 || loginName.indexOf('"') >= 0 || loginName.indexOf("'") >= 0){
		 $("#msg").html("<font color='red'>输入的内容不允许包含特殊字符！</font>");
		    return false;
	}
	var url = "/sim/userLogin/checkLogin?loginName="+loginName+"&_time="+new Date().getTime() ;
	var existFlag = false;
	var ip;
	$.ajax({
		url:url,
		async:false,
		type: "GET",
		dataType:"JSON",
		success:function(result){
			if(result.exist){
				existFlag = true;
				ip = result.ip;
			}
		}
	});
	$("#password").val("") ;
	$("#encryptPassword").val(rsaEncrypt(password)) ;
	if(existFlag) {
		openWarnWindow(loginName + "已在" + ip + "上登录，是否继续登录?");
		return false;
	}
	return true;
}
/*鼠标移过，左右按钮显示*/
/*
$(".carousel").hover(function() {
	$(this).find(".prev,.next").fadeTo("show", 0.1);
}, function() {
	$(this).find(".prev,.next").hide();
})
*/
/*鼠标移过某个按钮 高亮显示*/
/*
$(".prev,.next").hover(function() {
	$(this).fadeTo("show", 0.7);
}, function() {
	$(this).fadeTo("show", 0.1);
})
$(".carousel").slide({
	titCell : ".num ul",
	mainCell : ".carousel-pic",
	effect : "fold",
	autoPlay : true,
	delayTime : 700,
	autoPage : true
});
*/
$("#login_modal_ok").click(function(){
	$("#loginForm").removeAttr("onsubmit").submit();
});
$("#login_modal_cancel").click(function(){
	closeWarnWindow();
});
