var nodeUpgradeRefresh = {};
/**
 * 动态表单展示
 */
nodeUpgradeRefresh.displayDiv = function (selectId,val){
	$("#"+selectId).find("option").each(function(){
		$("#"+selectId+"_"+$(this).val()).css("display","none");
	});
	if(val=="perTime"){
		$("#poll_time_type").trigger("change") ;
	}
	$("#"+selectId+"_"+val).css("display","block");
}
/**
 * 表单验证
 */
nodeUpgradeRefresh.planTypeValidate = function(){
	var upgradePlanType = $("#upgradePlanType").val();
	var isSubmit = false;
	if (upgradePlanType) {//执行一次
		var spcOneVal = $("#spcOneVal").datetimebox("getValue");
		var pollTimeType = $("#poll_time_type").val();
		if (upgradePlanType == "spcOne" && spcOneVal) {
			isSubmit = true;
		}else if (upgradePlanType == "perTime" && pollTimeType) {
			var showElements = $("#poll_time_type").find("option:selected").attr("showElements");
			var re=new RegExp(",","g");
			var elementsResult = showElements.replace(re,",#");
			var flag = true;
			$("#"+elementsResult).each(function(){
				var tempVal;
				if($(this).children("input").length>0){
					tempVal = $(this).children("input").val();
				}
				if($(this).children("select").length>0){
					tempVal = $(this).children("select").val();
				}
				if(!tempVal||tempVal==""){
					flag = false;
					return;
				}
			});
			isSubmit = flag;
		}
	}
	return isSubmit;
}
/**
 * 开启提示窗口
 */
 nodeUpgradeRefresh.openNodeUpgradeDialog = function(message){
	 showAlertMessage(message);
}

/**
 * 表单提交
 */
nodeUpgradeRefresh.saveSubmit = function(){
	$("#nodeUpgradeRefreshForm").form("submit", {
		url : "/sim/sysconfig/upgrade/uploadNodePlanSave",
	    onSubmit: function(){
	    	var flagTemp = nodeUpgradeRefresh.planTypeValidate();
	    	if(flagTemp == true) {
	    		simHandler.openLoadingModal($("#sysconf_nodeUpgrade_editor_dialog"));
	    	} else {
	    		nodeUpgradeRefresh.openNodeUpgradeDialog("设置参数有误");
	    	}
	    	return flagTemp;
	    },
	    success:function(data){
	    	simHandler.closeLoadingModal($("#sysconf_nodeUpgrade_editor_dialog"));
	    	try{
	    		//从服务器返回的json中取出message中的数据,其中message为在controller中定义的变量
	    		data = $.parseJSON(data);
	    		var res = data.resultLoad;
	    		if (res == "success") {
	    			nodeUpgradeRefresh.openNodeUpgradeDialog("设置升级计划成功!");
	    		} else if (res == "emptyFlag") {
	    			nodeUpgradeRefresh.openNodeUpgradeDialog("设置升级计划不可用!");
	    			return false;
	    		} else {
	    			nodeUpgradeRefresh.openNodeUpgradeDialog("设置升级计划失败!");
	    		}
	    	} catch(e) {
	    		nodeUpgradeRefresh.openNodeUpgradeDialog("设置参数有误，请重新填写");
	    	}
			simSysConfNodeUpgrade.closeUploadFileDialog();
	    },
	    onLoadError:function(){
	    	simHandler.closeLoadingModal($("#sysconf_nodeUpgrade_editor_dialog"));
	    	nodeUpgradeRefresh.openNodeUpgradeDialog("服务器响应失败");
	    }
	});
}
