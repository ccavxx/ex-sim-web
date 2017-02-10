var nodeUpgradeUpFile = {};
/**
 * 保存提交
 */
nodeUpgradeUpFile.saveUpFile = function() {
	var uploadType = "node";
	if (nodeUpgradeUpFile.upLoad_validate(uploadType) && nodeUpgradeUpFile.validate_Desc()) {
		nodeUpgradeUpFile.fileCheck(true, uploadType);
	}
}

/**
 * 开启提示窗口
 */
nodeUpgradeUpFile.openNodeUpgradeDialog = function(message){
	showAlertMessage(message);
}

/**
 * 校验描述
 */
nodeUpgradeUpFile.validate_Desc = function() {
	var dsObj = document.getElementById("theNodeUpgradeDisc");
	if (nodeUpgradeUpFile.textAreaNoBank(dsObj)) {
		if (dsObj.value.length < 80) {//长度
			dsObj.value = Trim(dsObj.value);
			return true;
		} else {
			nodeUpgradeUpFile.openNodeUpgradeDialog("长度不能超过80个字符");
		}
	} else {
		return true;//可以为空
	}
}

/**
 * 以下主要是判断textArea为空
 */
nodeUpgradeUpFile.textAreaNoBank = function(obj) {
	var textAreaVal = obj.value;
	while (textAreaVal.indexOf("\r\n") >= 0) {
		textAreaVal = textAreaVal.replace("\r\n", "");
	}
	if (Trim(textAreaVal).length != 0) {
		return true;
	} else {
		obj.value = "";
		return false;
	}
}

/**
 * 上传校验
 */
nodeUpgradeUpFile.upLoad_validate = function(uploadType) {
	var checkPath;
	if (document.getElementById("theNodeUpgradeFile")) {
		checkPath = $("#theNodeUpgradeFile").val();
	} else {
		checkPath = "";
	}

	var pattern;
	if (checkPath != "") {
		var pos = checkPath.lastIndexOf("\\");
		var len = checkPath.length;
		var nameVal = checkPath.substring(pos + 1, len);//上传文件名
		if (uploadType == "node") {
			pattern = /^(WEB|AUDITOR|AGENT)_\d+_\d+_\d{3}_\d{3}-\d+_\d+_\d{3}_\d{3}\.sp$/;//节点升级包文件名匹配的格式
		}
		if (pattern.test(nameVal)) {
			if (nodeUpgradeUpFile.checkVesion(nameVal, uploadType)) {//版本号是否正确
				return true;
			} else {
				nodeUpgradeUpFile.openNodeUpgradeDialog("选择升级包版本不正确!");
			}
		} else {
			nodeUpgradeUpFile.openNodeUpgradeDialog("选择升级包格式不正确!");
		}
	} else {
		nodeUpgradeUpFile.openNodeUpgradeDialog("选择一个升级包!");
	}
	return false;
}

/**
 * 检查fileName是否重复，文件是否可用
 */
nodeUpgradeUpFile.fileCheck = function(ifSubmit, uploadType) {
	var checkPath;
	if (document.getElementById("theNodeUpgradeFile")) {
		checkPath = $("#theNodeUpgradeFile").val();
	} else {
		checkPath = "";
	}

	var pos = checkPath.lastIndexOf("\\");
	var len = checkPath.length;
	var nameVal = checkPath.substring(pos + 1, len);//上传文件名
	var time = (new Date()).valueOf();//加个时间戳
	var url = "/sim/sysconfig/upgrade/checkFile?time=" + time;
	url = encodeURI(url);
	var param = {
		name : nameVal,
		path : checkPath,
		type : uploadType
	};

	$.ajax({
			url : url, //后台处理程序
			type : 'post', //数据发送方式
			dataType : 'json', //接受数据格式
			data : param, //要传递的数据；就是上面序列化的值
			success : function(data) {
				if (data.status) {
					if (ifSubmit) {
						nodeUpgradeUpFile.ajaxFileUpload();
					}
				} else {
					nodeUpgradeUpFile.openNodeUpgradeDialog("相同升级包已经存在！");
					return false;
				}
			}
		});
}

/**
 * 检查版本格式是否正确
 */
nodeUpgradeUpFile.checkVesion = function(name, type) {

	if (type == "rule") {
		return true;
	}

	var names = name.split("-");
	var lens = name.split("_");
	var len = lens[0].length + lens[1].length + 2;
	var versionFrom = names[0].substring(len, names[0].length);
	while (versionFrom.indexOf("_") > 0) {
		versionFrom = versionFrom.replace("_", "");
	}

	var versionTo = names[1].substring(0, names[1].length - 3);
	while (versionTo.indexOf("_") > 0) {
		versionTo = versionTo.replace("_", "");
	}

	if (true) {
		var res = versionTo > versionFrom;
		return res;
	} else {
		return false;
	}
}

/**
 * ajax异步提交上传文件
 */
nodeUpgradeUpFile.ajaxFileUpload = function() {
	$("#theNodeUpgradeFileForm").form("submit", {   
		url : "/sim/sysconfig/upgrade/uploadNodeFile", 
	    onSubmit: function(){
	    },
	    success:function(data){
	    	data = JSON.parse(data);
			//从服务器返回的json中取出message中的数据,其中message为在controller中定义的变量
			var res = data.resultLoad;
			if (res == "success") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("文件上传成功!");
			} else if (res == "disable") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("选择的升级包不可用!");
	        	return false;
			} else if (res == "tooLarge") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("选择的升级包太大!");
	        	return false;
			} else if (res == "oVersion") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("选择的升级包版本过低!");
	        	return false;
			} else if (res == "1") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("升级包上传成功!");
			} else if (res == "2") {
				nodeUpgradeUpFile.openNodeUpgradeDialog("升级包上传失败!");
			}
			simSysConfNodeUpgrade.closeUploadFileDialog();
	    },
	    onLoadError:function(){
	    	nodeUpgradeUpFile.openNodeUpgradeDialog("服务器响应失败");
	    }
	});
}
