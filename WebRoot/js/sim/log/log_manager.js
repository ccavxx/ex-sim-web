//备份某一天可选的日期
function CurentTime() {
            var now = simHandler.serverTime;
            var year = now.getFullYear();       //年
            var month = now.getMonth() + 1;     //月
            var day = now.getDate()-1;            //日
            var hh = now.getHours();            //时
            var mm = now.getMinutes();          //分
            var clock = year + "-";
            if (month < 10)
                clock += "0";
            clock += month + "-";
            if (day < 10)
                clock += "0";
            clock += day + " ";
            if (hh < 10)
                clock += "0";
            clock += hh + ":";
            if (mm < 10) clock += '0';
            clock += mm;
            return (clock);
        }
$('#backupDate').combobox({
	onSelect: function(param){
		 if(param.value=="1"){
			 $("#selectScopeDay").attr("style","display:''");
			 $("#selectOneDay").attr("style","display:none");
			 $("#datetime").val("");
		 }
		 if(param.value=='0'){
			 $("#selectScopeDay").attr("style","display:none");
			 $("#selectOneDay").attr("style","display:''");
			 $("#startDate").val("");
			 $("#endDate").val("");
		 }
	}
});
$("#addBtn").click(function(){
	$('#logDataTree').tree({
		url:'/sim/logSearch/getTree?flag=0',
		checkbox:true
	});
	$("#logDialog").dialog('open').dialog('setTitle','选择日志源');
});
$("#closeBtn").click(function(){
	$("#logDialog").dialog('close');
});
function isOptionExist(selectObj, optionValue) {
	for ( var i=0;i<selectObj.length;i++) {
		if (selectObj[i].text == optionValue) {
			return true;
		}
	}
	return false;
}
function addDataSourceOption(value,content,text) {
	var selectObj = $("#backupContent").children();
	if (isOptionExist(selectObj, value)) {
		return;
	} else {
		
		$("#backupContent").append("<option value='" + content + "'>" + value + "</option>");
	}
}
$("#okBtn").click(function(){
	var getAllNode=$("#logDataTree").tree("getChecked");
	if(getAllNode==""){
		showAlarmMessage("请选择要备份的日志!");
		return;
	}
	var text = "";
	for(var i=0;i<getAllNode.length;i++) { 
		 if($("#logDataTree").tree("isLeaf",getAllNode[i].target)){
			 var parentNode=$("#logDataTree").tree("getParent",getAllNode[i].target);
			 var nodeContent=parentNode.text+"---->"+getAllNode[i].text;
			 text += getAllNode[i].text;
			 if(i < getAllNode.length-1){
				 text += ",";
			 }
			 if(i == getAllNode.length-1){
				 label = text;
				 $("#backupContent").attr("label",label);
			 }
    	    addDataSourceOption(nodeContent,getAllNode[i].id);
		 }
	}
	if(document.getElementById("backupContent").options.length>0){
		$("#backupContent").removeAttr("disabled");
	}
	$("#logDialog").dialog('close');
});
$("#deleteBtn").click(function(){
		var obj = $("#backupContent").children("option:selected").length;
		if (obj > 0) {
			$("#backupContent").children("option:selected").remove();
			
		} else {
			showAlarmMessage("请选择要删除的内容!");
		}

	if(document.getElementById("backupContent").options.length==0){
		$("#backupContent").attr("disabled","disabled");
	}
});
$("#backupBtn").click(function(){
	var selectValue = $('#backupDate').combobox('getValue');
	var oneDate = $('#datetime').val();
	var startDate = $('#startDate').val();
	var endDate = $('#endDate').val();
	var backupContent = document.getElementById("backupContent");
	var options = backupContent.options;
	if(options.length == 0){
		showAlertMessage("请选择备份内容");
		return false;
	}
	var dataSourceId = "";
	var dataSourceName = $("#backupContent").attr('label');
	$.each(options,function(index,option){
		dataSourceId += option.value;
		if(index < options.length-1){
			dataSourceId+=",";
		 }
		
	});
	var url = "";
	var startTime = "";
	var endTime = "";
	if(selectValue=='1'){
		if(startDate==""){
			showAlertMessage("请选择开始日期！");
			return false;
		}
		if(endDate==""){
			showAlertMessage("请选择结束日期！");
			return false;
		}
		if(startDate>endDate){
			showAlertMessage("结束日期不能小于开始日期!");
			return false;
		}
		startTime = startDate+" 00:00:00";
		endTime =endDate+" 23:59:59";
	}else if(selectValue=='0'){
		 if(oneDate==""){
			 showAlertMessage("请选择备份日期！");
			 return false;
		 }
		 startTime = oneDate + " 00:00:00";
		 endTime = oneDate + " 23:59:59";
		
	}
	url="/sim/logBackUp/backupLog?startDate=" + startTime + "&endDate=" + endTime + "&dataSourceList=" + encodeURI(dataSourceId) +"&dataSourceName=" + encodeURI(dataSourceName);
	$("#backupBtn").linkbutton("disable") ;
    $.post(url,function(data){
    	$("#backupBtn").linkbutton("enable") ;
		if(data.bakresult == "true"){
			showAlertMessage("备份任务已提交！");
		}else{
			if(data.bakresult == "未设置备份路径，无法进行备份！"){
				$("<div></div>").dialog({
					id:'backup_path_config_dialog',
					href:'/page/sysconfig/sysconfig_logbackup.html',
					title:'日志备份策略配置',
					width:800,
					height:435,
					modal:true,
					onClose:function(){
						$(this).dialog('destroy');
					}
				});	
				showAlertMessage("请先配置备份路径！");
			}else{
				showAlertMessage(data.bakresult);
			}
		}
	},"json");

});
