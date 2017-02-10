function startBackup(){
    $('#showExportDiv').show();
    var time = (new Date()).valueOf();//加个时间戳

    var params="";
	$.ajax({
  	    url :"/sim/sysconfig/sysbackup/getBackupFile?time="+time,  //后台处理程序   
		type:'post',       //数据发送方式   
 		dataType:'json',   //接受数据格式   
		data:params,       //要传递的数据；就是上面序列化的值   
 		success:function(data){
				if(data.result=='success'){
		    		$("#showExportDiv").hide();
		    		if(confirm("备份成功，是否下载备份文件?")){
		    			var time = (new Date()).valueOf();//加个时间戳
		    			var loadurl="/sim/sysconfig/sysbackup/loadBackupFile?time="+time;
		    			window.location = loadurl;
		    		}
		    		//$('#context').load(url);
		    	}else if(data.result=='faile'){
		        	//showAlertMessage("失败!");
		    	}
 			},
 		error:function(){
 			showErrorMessage("请求失败，请重试!");
 		}
	});
}   

$(function() {
	$('#showExportDiv').hide();
});
