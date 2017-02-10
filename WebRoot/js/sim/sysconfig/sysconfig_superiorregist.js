$(function(){
	var $form = $("#sysconfig_superior_regist_form");
	//初始化表单验证组件，并创建表单验证实例
	var sysconfig_superior_form_validation = $form.validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			registIp:'required;ipv4;',
			registName:'required;length[1~30];'
		}
	}).data( "validator" );
	//初始化表单提交方法
	$form.submit(function(){
		var validFlag = sysconfig_superior_form_validation.isFormValid();
		if(validFlag){
			var formdataarray = $form.serialize();
			$.ajax({
				url: "/sim/systemConfig/superiorConfigRegist",
				type: "POST",
				data: formdataarray,
				dataType:'json',
				success: function(res) {
					if(res && res.status) {
						showAlertMessage(res.message);
						var operVal = $("#superior_regist_operator").val();
						if(operVal === "delete") {
							$("#sysconfig_superior_regist_form").find("input").val("");
						} else {
							$("#superior_regist_resourceId").val(res.result);
						}
						initBtn();
						var updateRegistIp = $("#updateRegistIp").val();
						var updateRegistName = $("#updateRegistName").val();
						$("#deleteRegistIp").text(updateRegistIp);
						$("#deleteRegistName").text(updateRegistName);
					} else {
						showErrorMessage(res.message);
					}
				}
			});
		}
		return false;
	});
	//判断是否存在resourceId
	function submitFlag(){
		var resourceId = $("#superior_regist_resourceId").val();
		if(!resourceId) {
			return false;
		} else {
			return true;
		}
	}
	//为operator赋值并提交
	function changeOperator(operatorVal){
		$("#superior_regist_operator").val(operatorVal);
		setValidField(operatorVal);
		$form.submit();
	}
	//根据操作设置验证信息
	function setValidField(operatorVal){
		if(operatorVal === "delete"){
			sysconfig_superior_form_validation.setField({
				registIp:'required;ipv4;',
				registName:'length[0~30];'
			});
		}
		if(operatorVal === "regist" || operatorVal === "update"){
			sysconfig_superior_form_validation.setField({
				registIp:'required;ipv4;',
				registName:'required;length[1~30];'
			});
		}
	}
	//绑定提交事件
	$("#sysconfig_superior_regist_form_regist_button").click(function(){
		changeOperator("regist");
	});
	$("#sysconfig_superior_regist_form_update_button").click(function(){
		var flag = submitFlag();
		if(!flag){
			return;
		}
		changeOperator("update");
	});
	$("#sysconfig_superior_regist_form_delete_button").click(function(){
		var flag = submitFlag();
		if(!flag){
			return;
		}
		changeOperator("delete");
	});
	function initBtn() {
		if($("#superior_regist_resourceId").val()){
			$("#sysconfig_superior_regist_form_regist_btn_div").css("display", "none");
			$("#sysconfig_superior_regist_form_update_btn_div").css("display", "inline-block");
		}else{
			$("#sysconfig_superior_regist_form_regist_btn_div").css("display", "inline-block");
			$("#sysconfig_superior_regist_form_update_btn_div").css("display", "none");
		}
	}
	initBtn();
});
