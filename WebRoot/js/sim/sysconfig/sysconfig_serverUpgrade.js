var serverUpgrade = {};

/**
 * 保存提交
 */
serverUpgrade.save_submit = function() {
	var uploadType = "server";
	if (serverUpgrade.upLoad_validate(uploadType)) {
		serverUpgrade.fileCheck(true, uploadType);
	}
}

/**
 * 开启提示窗口
 */
serverUpgrade.openNodeUpgradeDialog = function(message){
	 showAlertMessage(message);
}

/**
 * ajax异步提交上传文件
 */
serverUpgrade.ajaxFileUpload = function() {
	$("#theServerUpgradeForm").form("submit", {
		url : "/sim/sysconfig/upgrade/uploadFile",
	    onSubmit: function(){
	    	return true ;
	    },
	    success:function(data){
	    	data = JSON.parse(data);
			//从服务器返回的json中取出message中的数据,其中message为在controller中定义的变量
			var res = data.resultLoad;
			if (res == "success") {
				$.messager.confirm('提示', '上传成功，是否重启系统?', function(r){
                	if (r){
						window.location.href = "/sim/sysconfig/upgrade/restart";
					} else {
						$('#sysconfig_container').panel('refresh',"/sim/sysconfig/upgrade/serverUpgrade");
					}
				});
			} else if (res == "disable") {
				serverUpgrade.openNodeUpgradeDialog("选择的升级包不可用!");
	        	return false;
			} else if (res == "tooLarge") {
				serverUpgrade.openNodeUpgradeDialog("选择的升级包太大!");
	        	return false;
			} else if (res == "oVersion") {
				serverUpgrade.openNodeUpgradeDialog("选择的升级包版本过低!");
	        	return false;
			} else if (res == "1") {
				serverUpgrade.openNodeUpgradeDialog("系统已经升级成功!");
			} else if (res == "2") {
				serverUpgrade.openNodeUpgradeDialog("系统已经升级失败!");
			}
			$('#sysconfig_container').panel('refresh',"/sim/sysconfig/upgrade/serverUpgrade");
	    },
	    onLoadError:function(){
	    	serverUpgrade.openNodeUpgradeDialog("服务器响应失败");
	    }
	});
}

/**
 * 检查版本格式是否正确
 */
serverUpgrade.checkVesion = function(name, type) {
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

	//var isNum = isInteger(versionFrom) && isInteger(versionTo);
	if (true) {
		var res = versionTo > versionFrom;
		return res;
	} else {
		return false;
	}
}

/*上传校验*/
serverUpgrade.upLoad_validate = function(uploadType) {
	var checkPath;
	if (document.getElementById("theServerFile")) {
		checkPath = $("#theServerFile").val();
	} else {
		checkPath = "";
	}

	var pattern;
	if (checkPath != "") {
		var pos = checkPath.lastIndexOf("\\");
		var len = checkPath.length;
		var nameVal = checkPath.substring(pos + 1, len);//上传文件名
		if (uploadType == "server") {
			pattern = /^WEB_\d+_\d+_\d{3}_\d{3}-\d+_\d+_\d{3}_\d{3}\.sp$/;//服务器升级包文件名匹配的格式
		} else if (uploadType == "node") {
			pattern = /^(AUDITOR|AGENT)_\d+_\d+_\d{3}_\d{3}-\d+_\d+_\d{3}_\d{3}\.sp$/;//节点升级包文件名匹配的格式
		} else if (uploadType == "rule") {
			pattern = /^RULE_\d{3}\.sp$/;//节点升级包文件名匹配的格式
		}
		if (pattern.test(nameVal)) {
			if (serverUpgrade.checkVesion(nameVal, uploadType)) {//版本号是否正确
				return true;
			} else {
				serverUpgrade.openNodeUpgradeDialog('选择升级包版本不正确!');
			}
		} else {
			serverUpgrade.openNodeUpgradeDialog('选择升级包格式不正确!');
		}
	} else {
		serverUpgrade.openNodeUpgradeDialog('选择一个升级包!');
	}
	return false;
}

/**
 * 检查fileName是否重复，文件是否可用
 */
serverUpgrade.fileCheck = function(ifSubmit, uploadType) {
	var checkPath;
	if (document.getElementById("theServerFile")) {
		checkPath = $("#theServerFile").val();
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
						serverUpgrade.ajaxFileUpload();
					}
				} else {
					serverUpgrade.openNodeUpgradeDialog('相同升级包已经存在！');
					return false;
				}
			}
		});
}
