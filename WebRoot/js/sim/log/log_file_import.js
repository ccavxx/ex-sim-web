 	function initCheckbox(node){
		if(node.state=='open'){
			return '<input  type=\'checkbox\' id="'+node.id+'" name="nodeFileName" value="'+node.id+'" onclick="vaildateFile(this.value);">'+node.text;
		}else{
			return node.text;
		}
		
	}
 	//验证导入的文件是否是同一设备类型
 	var str="";
 	function vaildateFile(nodeId){
 		if(document.getElementById(nodeId).checked){
 		var node = $('#log_import_tree').tree('find', nodeId);//子节点
 		var monthNode=$('#log_import_tree').tree('getParent',node.target);//父节点月份
 		var yearNode=$('#log_import_tree').tree('getParent',monthNode.target);//父节点年份
 		var dataSourceNode=$('#log_import_tree').tree('getParent',yearNode.target);//父节点日志源
 		var deviceNode=$('#log_import_tree').tree('getParent',dataSourceNode.target);//父节点设备类型
 		if(str!=""){
 			if(str!=deviceNode){
 				showMsg('alert-error', '请选择同一设备下的文件');
				return;
 			}
 		}
 			str=deviceNode;
 		}
 	}
 	
 	function showMsg(cls, msg) {
		$('#log_import_msg').empty().removeClass().addClass('alert ' + cls).append(msg).fadeIn(500);
	}
$(function() {
	$("#uploadFfile").cnfileupload();
	// 为标签页增加点击事件，用于改变确定按钮的点击事件
	$('#log_import_submit').click('#ftp', doImport);
	$(".log-import ul li").bind('click', function(event) {
		var target = event.target;
		var fileLocation = $(target).attr('href');
		//fileLocation = fileLocation.replace('#','');
		$('#log_import_submit').unbind("click").click(fileLocation, doImport);
	});

	function doImport(event) {
		$('#raw_log_content').empty();
		var id = event.data;
		if (id == "#ftp") {
			doUploadFtpFile();
		} else if(id == "#local"){
			doUploadLocalFile();
		}
		simBackupLogSearchHandler.importFlag=true;
	}
	
	function doUploadFtpFile(){
		var nodes=$("input[name='nodeFileName']:checked");
		if(nodes==null || nodes=="" || nodes.length==0){
			showMsg('alert-error', '请选择文件');
			return;
		}
		var nodeValue="";
		var selectDiffType = false;
		$.each(nodes,function(i,n){
			var node = $('#log_import_tree').tree('find', n.value);//子节点
	 		var monthNode=$('#log_import_tree').tree('getParent',node.target);//父节点月份
	 		var yearNode=$('#log_import_tree').tree('getParent',monthNode.target);//父节点年份
	 		var dataSourceNode=$('#log_import_tree').tree('getParent',yearNode.target);//父节点日志源
	 		var deviceNode=$('#log_import_tree').tree('getParent',dataSourceNode.target);//父节点设备类型
	 		if(nodeValue != "" && nodeValue != deviceNode){
 				selectDiffType = true;
				return false;
	 		}else{
	 			nodeValue = deviceNode;
	 		}
		});
 		if(selectDiffType){
 			showMsg('alert-error', '请选择同一设备下的文件');
 			return;
 		}
		var logPath="";
		$.each(nodes,function(i,path){
			logPath += path.value;
			if(i < nodes.length-1){
				logPath+=",";
			}
		});
		var msg = '<div style="text-align:center;"><img src="/img/loading_withoutstate.jpg" /></div>';
		$('#log_import_msg').empty().removeClass().addClass('alert ' + 'alert-error').append(msg);
		$.getJSON('/sim/logHistory/createIndex',
				{fileLocation:'ftp',filePath:logPath,req_time:new Date().getTime()},
				function(response){
					if(response.success){
						simLogQueryHandler.getFirstQueryParams();
						$("#log_import_dialog").dialog('close');
					}else{
						showMsg('alert-error', response.message);
					}
				}
		);
    }
	function doUploadLocalFile() {
		var filename = $("#uploadFfile").val();
		if (!filename) {
			showMsg('alert-error', '请选择需要上传的文件');
			return false;
		}
		var point = filename.lastIndexOf(".");
		var type = filename.substr(point);
		if (type != ".zip") {
			showMsg('alert-error', '请上传zip格式文件');
			return false;
		}
		var path = "/sim/logHistory/uploadFile?date=" + new Date();
		var msg = '<div style="text-align:center;"><img src="/img/loading_withoutstate.jpg" /></div>';
		$('#log_import_msg').empty().removeClass().addClass('alert ' + 'alert-error').append(msg);
		$('#uploadFileForm').form('submit', {
            url: path,
            dataType:"json",
            success: function(responseText) {
            	var response = eval('(' + responseText + ')') ;
            	if(response.success){
            	    simLogQueryHandler.getFirstQueryParams();
				    $("#log_import_dialog").dialog('close');
				}else{
					showMsg('alert-error',response.message);
				}
            }
		});
	}
});