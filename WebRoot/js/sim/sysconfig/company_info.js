$(function(){
		$("#companyLogoFile").cnfileupload();
	});
importSubmit = function() {

	var fileval = $("#companyLogoFile").val();
	var companyName = $("#companyName").val();
	var productName = $("#productName").val();

	if (!fileval) {
		if(!companyName&&!productName){
			showAlertMessage("请填写您要修改的内容");
		}else{
			wordLengthControl(companyName,productName);
		}
	} else {
		var point = fileval.lastIndexOf(".");
		var type = fileval.substr(point).toLocaleLowerCase();
		if (type != ".png" && type != ".jpg" && type != ".bmp" && type != ".pcx" && type != ".tiff" && type != ".gif" && type != ".jpeg" && type != ".tga" && type != ".exif" && type != ".fpx" && type != ".svg" && type != ".psd" && type != ".cdr" && type != ".pcd" &&type != ".dxf" && type != ".ufo" && type != ".eps" && type != ".cdr" && type != ".swf") {
			showAlertMessage("不匹配的文件格式");
			cancelSubmit();
		}else{
			wordLengthControl(companyName,productName);
		}
	}
}
wordLengthControl = function(companyName,productName){
	if(productName.length > 20){
		showAlertMessage("产品名称长度不得超过20");
	}else if(companyName.length > 20){
		showAlertMessage("版权所有公司名称长度不得超过20");
	}else{
		ajaxFileUpload();
	}
}
ajaxFileUpload = function() {
	var companyName = $("#companyName").val();
	var productName = $("#productName").val();
	$.ajaxFileUpload({
		url : "/sim/systemConfig/modifyCompanyInfo?companyName=" + encodeURI(companyName)+"&productName=" + encodeURI(productName),
		secureuri : false,
		fileElementId : "companyLogoFile",
		dataType : "json",
		success : function(data, status){
			showAlertMessage(data.message);
			if(data.message=="修改成功"){
				window.location.href = "/sim/index/";
			}
		},
		error : function(data, status, e) {
			showErrorMessage("文件上传失败");
		},complete: function(xmlHttpRequest) {  
            $("#companyLogoFile").on("change", function(){  
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
restoreDefault = function(){
	$.messager.confirm("警告","此操作会将有关logo配置信息恢复成天融信有关内容，您确定要恢复默认吗？",function(result){
		if(!result) return ;
		$.ajax({
			url:"/sim/systemConfig/restoreDefault",
			type:'post',
			async: false,
			success:function(data){
				showAlertMessage("恢复默认成功");
				window.location.href = "/sim/index/";
			}
		});
	}) ;
}
cancelSubmit = function(){
	$("#company_info").form("reset");
}