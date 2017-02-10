/**
 * @author:wza
 */
var statusshow=true;
al.inportAssetMethod = function(operation){
	var asset_table_panel = $('#al_asset_panel').panel('panel');
	if(!asset_table_panel)return;
//	$(asset_table_panel).parent().css('position','relative');
	var width = $(asset_table_panel).width();
	var height = $(asset_table_panel).height();
	$("#asset_inport_div").dialog({
		top:0,
		left:0,
		width:width,
		inline:true,
		noheader:true,
		collapsed:true,
		modal:true,
		shadow:false,
		border:false,
		style:{'padding':0,'border':0}
	});
	$('#asset_inport_div').dialog('expand',true);
};
	function fileSelected() {
    	
    	try{
    		var file1 = document.getElementById('fileToUpload').files[0];
    	}catch(e){
    		var filepathname=$("#fileToUpload").val();
    		var fileName=filepathname.substring(filepathname.lastIndexOf("\\")+1);
    		var filetypename = fileName.substring(fileName.lastIndexOf('.'), fileName.length);
            
            if (filetypename == '.xls'){
            	$("#uploadFile").show();
            	document.getElementById('fileName').innerHTML ='文件名: ' + fileName;
            }else {

                document.getElementById('fileName').innerHTML = "<span style='color:Red'>错误提示:上传文件应该是.xls后缀,而不应该是" + filetypename + ",请重新选择文件</span>";
//                document.getElementById('fileSize').innerHTML ="";
//                document.getElementById('fileType').innerHTML ="";
                try{
            		$("#importResult").hide();
            	}catch(erro){}
                $("#uploadFile").hide();

            }
    		//substring(this.value.lastIndexOf("\\")+1)
    		
    	}
        try{
        	var file = document.getElementById('fileToUpload').files[0];
            var fileName = file.name;
            var file_typename = fileName.substring(fileName.lastIndexOf('.'), fileName.length);
        
            if (file_typename == '.xls') {//这里限定上传文件文件类型
                if (file) {
                    $("#uploadFile").show();
                    var fileSize = 0;
                    if (file.size > 1024 * 1024)
                        fileSize = (Math.round(file.size * 100 / (1024 * 1024)) / 100).toString() + 'MB';
                    else
                        fileSize = (Math.round(file.size * 100 / 1024) / 100).toString() + 'KB';

                    document.getElementById('fileName').innerHTML = '文件名: ' + file.name;
//                    document.getElementById('fileSize').innerHTML = '大小: ' + fileSize;
//                    document.getElementById('fileType').innerHTML = '类型: ' + file.type;
                    if(file.size > 10*1024 * 1024){
                    	$("#uploadFile").hide();
                        document.getElementById('fileName').innerHTML = "<span style='color:Red'>错误提示:上传文件大小是:"+ fileSize+",超出最大文件大小限制,请重新选择文件</span>";
                        $("#importResult").hide();
                    }
                }
            }
            else {

                $("#uploadFile").hide();
                document.getElementById('fileName').innerHTML = "<span style='color:Red'>错误提示:上传文件应该是.xls后缀而不应该是" + file_typename + ",请重新选择文件</span>";
                try{
            		$("#importResult").hide();
            	}catch(erro){}
//                document.getElementById('fileSize').innerHTML ="";
//                document.getElementById('fileType').innerHTML ="";

            }
        }catch(e){}
    }

    function uploadFile() {
    	var xmlhttp;
        if (window.XMLHttpRequest)
          {// code for IE7+, Firefox, Chrome, Opera, Safari
          xmlhttp=new XMLHttpRequest();
          }
        else
          {// code for IE6, IE5
          xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
          }
        var formdata = '';
        statusshow=true;
        $("#progressNumber").show();
	    $("#progressNumber").progressbar("setValue", 50);
    	$('#uploadfileform').form('submit', {
            url: '/sim/assetlist/uploadExcelFile',
            onSubmit: function() {
                return $(this).form('validate');
            },
            dataType: "json",
            success: function(result) {
            	var jsonRes = null;
            	try{
            		jsonRes = eval('(' + result + ')');
            	}catch(e){}
            	//var obj1 = result.parseJSON(); //由JSON字符串转换为JSON对象
            	//var obj2 = JSON.parse(result); //由JSON字符串转换为JSON对象
            	var uploadInfo='';
            	try{
            		uploadInfo=jsonRes.uploadInfo;
            	}catch(error){}
            	if((undefined!=uploadInfo&&uploadInfo.length>1)||"failed"==uploadInfo){
            		document.getElementById('errorMessagesum').innerHTML = "<span style='color:Red'>提示信息: "+jsonRes.errorInfo+"</span>";
            		return;
            	}else if("success"== jsonRes.message){
//            		showAlertMessage("上传成功!");
//                	try{
//                		formdata=new FormData();
//                		xmlhttp.upload.addEventListener("progress", uploadProgress, false);
//                        xmlhttp.open("POST", "");
//                        xmlhttp.send(formdata);
//                	}catch(er){
//                		var evt={};
//                		evt.lengthComputable=true;
//                		evt.loaded=10;
//                		evt.total=10;
//                		uploadProgress(evt);
//                	}
                	try{
                		$("#importResult").show();
                	}catch(err){}
                	if(undefined==jsonRes.totalCount){
                		document.getElementById('totalCount').innerHTML = "";
                	}else{
//                		document.getElementById('totalCount').innerHTML = "<span >有效设备 总数 " + jsonRes.totalCount + " 个 (有效设备： 所有必填项均已填写)</span>";
                	}
                	if(undefined==jsonRes.marks||jsonRes.marks.length<1){
                		$("#inport_asset_errorTable").datagrid('loadData',{
            				total:0,
            				rows:[]
            			});
                		$("#error_datagrid_div").css("display","none");
        				statusshow=true;
                	}else{
                		$("#inport_asset_errorTable").datagrid('loadData',{
            				total:0,
            				rows:[]
            			});
                    	if(statusshow){
                			try{
                				$("#error_datagrid_div").css("display","block");
                				statusshow=false;
                			}catch(e){}
                			try{
                				$("#inport_asset_errorTable").datagrid({
                            		url:'/sim/assetlist/uploadFileErrorResult'
                        	    });
                			}catch(e2){}
                			
                		}
                	}
                	
                	if(undefined==jsonRes.successCount){
                		document.getElementById('successCount').innerHTML = "";
                	}else{
                		document.getElementById('successCount').innerHTML = "<span style='color:Green'>成功导入条数 " + jsonRes.successCount + " 条</span>";
                	}
                	
                	if(jsonRes.totalCount==0&&jsonRes.summaryErrorContent!="序号: fileError, 内容有误, 请检查后再导入"){
                		document.getElementById('errorMessagesum').innerHTML = "<span style='color:Red'>提示信息: excel[模板]文件中没有设备或 所有行的必填项中均有未填写项目！</span>";
                		//document.getElementById('errorMessagepar').innerHTML = "";
                		return;
                	}
                	if(jsonRes.totalCount-jsonRes.successCount==0){
                		//return;
                	}
                	if(undefined==jsonRes.summaryErrorContent||jsonRes.summaryErrorContent.length<4){
                		document.getElementById('errorMessagesum').innerHTML = "";
                		return;
                	}
                	if(jsonRes.summaryErrorContent=="序号: fileError, 内容有误, 请检查后再导入"){
                		document.getElementById('errorMessagesum').innerHTML = "<span style='color:Blue'>错误信息: 所传文件非正确Excel文件或文件已经损坏！ </span>";
                	}else{
                		document.getElementById('errorMessagesum').innerHTML = "<a id='showOrHide' href='javascript:void(0)'>查看/隐藏 错误详情</a>";
                		$("#showOrHide").linkbutton();
                		$('#showOrHide').bind('click', showOrHide);
                	}
                	//简要错误信息: " + jsonRes.summaryErrorContent + "
//                	if(undefined==jsonRes.particularErrorContent||jsonRes.particularErrorContent<4){
//                		document.getElementById('errorMessagepar').innerHTML = "";
//                		return;
//                	}
                	var temperrpar=jsonRes.particularErrorContent.replace(/<br>/g,'\n');
//                	alert(jsonRes.particularErrorContent);
//                	document.getElementById('errorMessagepar').innerHTML = "<span style='color:Red'>详细错误信息: " + temperrpar + " </span>";
                }else{
                	showErrorMessage("导入失败!");
                }
                
            }
        });
        /*var fd = new FormData();
        fd.append("fileToUpload", document.getElementById('fileToUpload').files[0]);
        var xmlhttp;
        if (window.XMLHttpRequest)
          {// code for IE7+, Firefox, Chrome, Opera, Safari
          xmlhttp=new XMLHttpRequest();
          }
        else
          {// code for IE6, IE5
          xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
          }
        var xhr = xmlhttp;//new XMLHttpRequest();
        xhr.upload.addEventListener("progress", uploadProgress, false);
        xhr.addEventListener("load", uploadComplete, false);
        xhr.addEventListener("error", uploadFailed, false);
        xhr.addEventListener("abort", uploadCanceled, false);
        xhr.open("POST", "/sim/assetlist/uploadExcelFile");
        xhr.send(fd);*/
    	$("#progressNumber").progressbar("setValue", 100);
    	setTimeout(function(){
    		$("#progressNumber").hide();
		},500);
    }

    function uploadProgress(evt) {
        if (evt.lengthComputable) {
            var percentComplete = Math.round(evt.loaded * 100 / evt.total);
            $('#progressNumber').progressbar('setValue', percentComplete);
        }
        else {
            document.getElementById('progressNumber').innerHTML = '无法计算';
        }
    }

    function uploadComplete(evt) {
        /* 服务器返回数据*/
        var message = evt.target.responseText;
      
    }

    function uploadFailed(evt) {
        alert("上传出错.");
    }

    function uploadCanceled(evt) {
        alert("上传已由用户或浏览器取消删除连接.");
    }
    
    function showOrHide() {
		if(statusshow){
			try{
				$("#error_datagrid_div").css("display","block");
				statusshow=false;
			}catch(e){}
			
			$("#inport_asset_errorTable").datagrid({
        		url:'/sim/assetlist/uploadFileErrorResult'
    	    });
			
		}else{
			try{
				$("#error_datagrid_div").css("display","none");
				statusshow=true;
			}catch(e){}
			
		}
    }

