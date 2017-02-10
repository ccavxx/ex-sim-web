$(function(){
	//加载图片的存储路径
	$.ajax({   
  	    url :'/sim/sysconfig/getReportFooterImage?reportId=report_footer_image',  //后台处理程序   
		type:'post',       //数据发送方式   
 		dataType:'json',   //接受数据格式   
 		success:function(data){
				if(data.result=='success'){
		    		$("#reportFooterImage").html("<img src='"+data.imagePath+"'>");
		    	}else if(data.result=='fail'){
		    	}
 			}
		});
})


function save_submit(){
	if(checkChange()){
		//异步上传
		$.ajaxFileUpload({
				url : "/sim/sysconfig/modifyReportFooterImage",//用于文件上传的服务器端请求地址
				secureuri : false,//一般设置为false
				fileElementId : "upLoad",//文件上传空间的id属性  <input type="file" id="file" name="file" />
				dataType : "json",//返回值类型 一般设置为json
				success : function(data, status) //服务器成功响应处理函数
				{
					var result = data.result;
					if(result=="success"){
					$('#sysconfig_container').panel('refresh',"/page/sysconfig/sysconfig_reportFooterImage.html");
					} else if(result=="toLarge"){
			        	$("#warning").html("<p>上传图片大于1M,请重新上传小于1M的图片</p>");
			        	return false;
			        }else if(result=="formatWrong"){
			        	$("#warning").html("<p>上传图片类型必须是Jpg、Png、Bmp</p>");
			        	return false;
			        }else if(result=="toBig"){
			        	$("#warning").html("<p>上传图片宽度范围35－500像素，高度范围15－500像素</p>");
			        	return false;
			        }
				},
				error : function(data, status, e)//服务器响应失败处理函数
				{
					showErrorMessage( '上传图片失败，请重新上传!' );
				}
			})
	}
}


//上传之前检查是否已经选择了上传的图片
function checkChange(){
		var upLoad = document.getElementsByName("upLoad");

	    if($(upLoad[0]).val()=="")
	    {
	    	showAlertMessage('请点击浏览选择上传的图片 !');
            return false;	    
	    }
        return true;
}



function recoverImage(){

$.ajax({   
  	    url :'/sim/sysconfig/recoverImage?reportId=report_footer_image',  //后台处理程序   
		type:'post',       //数据发送方式   
 		dataType:'json',   //接受数据格式   
 		success:function(data){
				if(data.result=='success'){
		    		$('#sysconfig_container').panel('refresh',"/page/sysconfig/sysconfig_reportFooterImage.html");
		    	}
 			}
		});

}