var aboutUs = {}
/**
 * 保存提交
 */
aboutUs.importSubmit = function() {

	var fileval = $("#theLicenseFile").val();

	if (!fileval) {
		showAlertMessage("请选择许可文件");
	} else {
		var point = fileval.lastIndexOf(".");
		var type = fileval.substr(point);

		if (type != ".xml" && type != ".lic") {
			showAlertMessage("不匹配的文件格式");
			aboutUs.cancelSubmit();
		}else{
			aboutUs.ajaxFileUpload();
		}
	}
}
/**
 * 触发文件表单的点击事件
 */
aboutUs.licenseFileClick = function(){
	$("#theLicenseFile").click();
}
/**
 * ajax异步提交上传文件
 */
aboutUs.ajaxFileUpload = function() {
	$.ajaxFileUpload({
		url : "/sim/systemConfig/licenseImport",
		secureuri : false,
		fileElementId : "theLicenseFile",
		dataType : "json",
		success : function(data, status){
			//从服务器返回的json中取出message中的数据,其中message为在controller中定义的变量
			showAlertMessage(data.message);
			aboutUs.cancelSubmit();
		},
		error : function(data, status, e) {
			showErrorMessage(e);
			aboutUs.cancelSubmit();
		},complete: function(xmlHttpRequest) {  
            $("#theLicenseFile").on("change", function(){  
            	var fileVal = $(this).val();
				var fileNameArray;
				if(fileVal.valueOf("\\")){
					fileNameArray = fileVal.split("\\");
				} else if(fileVal.valueOf("/")) {
					fileNameArray = fileVal.split("/");
				}
				var leg = fileNameArray.length;
				if(leg > 0){
					var fileId = $('input[name="fileid"]').attr('value');
					$("#" + fileId).val(fileNameArray[leg-1]);
				}
            });  
        }
	});
}

/**
 * 取消提交·清空表单
 */
aboutUs.cancelSubmit = function(){
	$("#license_file_import_form").form("reset");
}