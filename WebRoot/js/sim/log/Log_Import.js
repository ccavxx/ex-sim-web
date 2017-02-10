function onSelectLogImport(){
	var nameArr=$('input:radio[name="fileLocation"]:checked').val();
	if(nameArr=='ftp'){
		$('#customFileId').attr("style","display:none");
		$('#backupFileId').attr("style","display:block");
	}
	if(nameArr=='local'){
		$('#customFileId').attr("style","display:block");
		$('#backupFileId').attr("style","display:none");
	}
}
function onSubmitFile(){
	var filename=$("input[name='uploadFfile']").val();
	if(filename==""){
		showAlarmMessage("请选择需要上传的文件");
		return false;
	}
	var point=filename.lastIndexOf(".");
	var type = filename.substr(point);
	
	if(type!=".zip"){
		showAlertMessage("请上传zip文件");
		return false;
	}
	$.ajaxFileUpload({
		url:"/sim/logHistory/uploadFile",//用于文件上传的服务器端请求地址
		secureuri : false,//一般设置为false
		fileElementId :"uploadFfile",//文件上传空间的id属性  <input type="file" id="file" name="file" />
		dataType : "json",//返回值类型 一般设置为json
		success : function(data, status) //服务器成功响应处理函数
		{
			var result = data.result;
			if(result=="false"){
				showErrorMessage("上传的文件大小超过2G");
				return false;
	        }else if(result=="true"){
	        	showAlertMessage("上传成功");
	        	createIndex(filename);
	        	return false;
	        }else{
	        	showErrorMessage('上传失败，请重新上传!');
	        	return false;
	        }
		},
		error : function(data, status, e)//服务器响应失败处理函数
		{
			showErrorMessage('上传失败，请重新上传!');
		}
	});
}
function onLogImportFile(){
	//var selectNode=$('#logImportTree').tree('getSelected');
	var checkNode=$("#logImportTree").tree('getChecked');
	if(checkNode==null || checkNode==""){
		showAlertMessage("请选择节点！");
		return;
	}
	var nodes= $('#logImportTree').tree('getChecked');
	var str = '';
	var flag=true;
	$.each(nodes,function(i,n){
	     if(str!=''){
	        if(str!=$("#logImportTree").tree("getParent",n.target).id){
	        	showAlertMessage("请选择同一节点下的日志！");
	            flag=false;
	            return false;
	        }
	     }else{
	    	 str+=$("#logImportTree").tree("getParent",n.target).id;
	     }
	});
	if(flag){
	var filePath= $('#logImportTree').tree('getChecked');
	var logPath="";
	$.each(filePath,function(i,path){
		logPath+=path.id;
		if(i<filePath.length-1){
			logPath+=",";
		}
	});
	var fileLocation=$('input:radio[name="fileLocation"]:checked').val();
	$.ajax({
		url:'/sim/logHistory/createIndex?fileLocation='+fileLocation+'&fileName='+'&filePath='+logPath,
		type:'post',
		async: false,
		success:function(data){
			if(data.result=="true"){
			}else{
				showErrorMessage("导入失败，请重新导入!");
				return;
			}
			
		}
	});
	}
}
function createIndex(filename){
	var fileLocation=$('input:radio[name="fileLocation"]:checked').val();
	$.ajax({
		url:'/sim/logHistory/createIndex?fileName='+filename+'&fileLocation='+fileLocation+'&filePath=',
		type:'post',
		async:false,
		success:function(data){
			showAlertMessage(data);
		}
	});
}
