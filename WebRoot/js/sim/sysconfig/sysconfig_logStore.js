var simSysConfLogStore = {};
$(function(){

	/**
	 * 初始化表单数据
	 */
	var sys_cfg_store_id;
	$.getJSON('/sim/systemConfig/logProtectionEnabled?_time='+new Date().getTime(),function(result){
		$("#enable_log_protection").attr("checked",result.enabled) ;
	}) ;
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey:'sys_cfg_store',nodeType:'IndexService'},function(res){
		if(res && res.status){
			var formdata = res.result;
			var archiveConfig = formdata.archive_path ;
			sys_cfg_store_id = formdata.responseId;
			//$("select[name='systemlongevity']").val(formdata.archive.systemlongevity);// 审计系统日志保存时间
			var archivePathList ;
			if(archiveConfig.archive_path_list == null || archiveConfig.archive_path_list == ""){
				archivePathList = archiveConfig.archive_path ;
			}else{
				archivePathList = archiveConfig.archive_path_list ;
			}
			$('#archive_path').val(archiveConfig.archive_path);//当前存储路径
			$('#archive_path_list').val(archivePathList);//日志存储路径
			$("input[name='alert']").val(archiveConfig.alert);// 磁盘使用率告警上限(%)
			$("input[name='override']").val(archiveConfig.override);// 磁盘使用率(%) 
			$("#isWindows").val(formdata.isWindows);
			//$("#sysconfig_logstore_localpath").val(archiveConfig.archive_path);
			var pathArray = archivePathList.split(";");
			$.each(pathArray,function(index,item){
				if(item){
					var path = item.length > 40 ? item.substring(0,40) : item ;
					var $store_path = $('<div title='+ item+' class="alert alert-warning fade in" style="padding:2px 35px 2px 14px;width:150px;margin:5px 0 0 0px;"></div>');
					$store_path.attr('id',item);
					var $path_btn = $('<button type="button"  class="close" data-dismiss="alert" aria-hidden="true">×</button>');
					$store_path.append($path_btn).append($("<span >"+ path+"</span>"));
					$("#show_storePath").append($store_path);
					
					var opt = $("<option/>").val(item).text(item);
					if(item == archiveConfig.archive_path){
						opt.attr("selected","selected") ;
					}
					$('#archive_path').append(opt);
					//$('#sysconfig_logstore_localpath').append(opt);
				}
			});
			//simSysConfLogStore.logstoreLocalpathLoadTree();
		}
	});
	
	//初始化表单验证组件，并创建表单验证实例
	var sysconfig_logstore_form_validation = $('#sysconfig_logstore_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			archive_path:'required',
			override:'磁盘使用率:required;range[11~95];integer[+0];',
			alert:'磁盘使用率告警上限:required;range[10~94];integer[+0];match[lt, override]'
		}
	}).data( "validator" );
	
	//初始化本地备份下拉树
	/*simSysConfLogStore.logstoreLocalpathLoadTree = function(){
		$("#sysconfig_logstore_localpath").combotree({
			height:24,
			width:200,
			multiple:true,
			cascadeCheck:false,
			url:'/sim/systemConfig/getLocalFileDirectory',
			onClick:simSysConfLogStore.onClick,
			
			onLoadSuccess:simSysConfLogStore.onLoadSuccess
		});
	}
	
	//点击本地备份下拉树节点
	simSysConfLogStore.onClick = function(node){
		if(node){
			var path = node.attributes.path;
			$('#sysconfig_logstore_localpath').combotree('setText',path);
			$('#archive_path').val(node_path);
		}
	}
	//是否是第一次加载下拉树
	var _isFirst = false;
	//当下拉框创建成功后更新显示文本
	simSysConfLogStore.onLoadSuccess = function(node, data){
		if(!_isFirst){
			_isFirst = true;
			var path = $('#archive_path').val();
			$('#sysconfig_logstore_localpath').combotree('setText',path);
		}
	} 
	*/	
	//提交表单事件
	$('#sysconfig_logstore_form').submit(function() {
		//var path = $("#sysconfig_logstore_localpath").val();
		var path = $("#archive_path").val();
		/*if(path.length==0){
			$('#archive_path').val("");
			$('#archive_path').blur();
			return;
		}*/
		//验证表单
		var valid = sysconfig_logstore_form_validation.isFormValid();
		if (!valid)
			return;
		//表单数据
		var formdata = {archive : {}, archive_path : {},enable_log_protection:!!$("#enable_log_protection").attr("checked")};
		formdata.responseId = sys_cfg_store_id;
		//formdata.archive.systemlongevity = $("select[name='systemlongevity'] option:selected").val();
		formdata.archive_path.archive_path_list = $('#archive_path_list').val();
		formdata.archive_path.archive_path = path ;
		formdata.archive_path.alert = $("input[name='alert']").val();
		formdata.archive_path.override = $("input[name='override']").val();
        $.ajax({
            url: '/sim/systemConfig/modifyLogStoreConfig',
            type: 'POST',
            data: JSON.stringify(formdata),
            dataType:'json',
            contentType:"text/javascript",
            success: function(res){
            	if(res && res.status){
            		showAlertMessage(res.message);
				} else {
					showErrorMessage(res.message);
				}
            }
        });
        return false;
	});
	simSysConfLogStore.openStorePath = function(){
		$("#storePath_tree").tree({
			url:'/sim/systemConfig/getLocalFileDirectory',
			checkbox:true,
			cascadeCheck:false,
			onBeforeCheck:simSysConfLogStore.onBeforeCheck,
	        onLoadSuccess:function(node,data){
					var path_list = $('#archive_path_list').val().split(";");
					var tree = $('#storePath_tree');
					var fileSeparator = simHandler.systemInfo.fileSeparator;
					var isWindows = simHandler.systemInfo.isWindows ; 
					var rootPath = isWindows ? "" : "/";
					for(var i = 0;i<path_list.length;i++){
						for(var index in data){
							var dirInfo = data[index] ;
							if(dirInfo.id == path_list[i]){
								tree.tree('check',tree.tree('find',dirInfo.id).target) ;
								continue ;
							}
							if(path_list[i].indexOf(dirInfo.id) != 0){
								continue ;
							}
							var pathArray = path_list[i].split(fileSeparator);
							for(var j = 0;j<pathArray.length;j++){
								var path ;
								if(isWindows && j==0){
									path = pathArray[0] + fileSeparator ;
								}else{
									path = rootPath + pathArray.slice(0,j+1).join(fileSeparator) ;
								}
								var parent_node = tree.tree('find',path);
								if(parent_node){
									tree.tree('expand', parent_node.target);
								}
							}
						}
					}
				}
		});
			$("#storePath_Dialog").dialog("open");
	};
	function getChildNodeList(node) {
		var data = $("#storePath_tree").tree("getChildren",node.target);
  		if (data && data.length>0) {  	
  			var path_list = $('#archive_path_list').val().split(";");
  			for(var i = 0;i<path_list.length;i++){
  				for(var j = 0;j<data.length;j++){ 
  					if(path_list[i] == data[j].id){
  						var node_select = $("#storePath_tree").tree('find',data[j].id);
  						$('#storePath_tree').tree('check', node_select.target);
  					}
  				}
  			}
  		}
  	}

	simSysConfLogStore.closeStorePath = function (){
		$("#storePath_Dialog").dialog("close");
	};
	simSysConfLogStore.addStorePath = function(){
		var checkPath = $("#storePath_tree").tree("getChecked");
		$('#archive_path').empty();
//		$('#sysconfig_logstore_localpath').empty();
		$('#show_storePath').html("");
		var pathList = "";
		$.each(checkPath,function(index,item){
			var path = item.attributes.path;
			var pathText = path.length > 40 ? path.substring(0,40) : path ;
			
			var $store_path = $('<div title='+ path + ' class="alert alert-warning fade in" style="padding:2px 35px 2px 14px;width:150px;margin:5px 0 0 0px;"></div>');
			$store_path.attr('id',path);
			
			var $path_btn = $('<button type="button"  class="close" data-dismiss="alert" aria-hidden="true">×</button>');
			$store_path.append($path_btn).append($("<span>"+ pathText+"</span>"));
			$("#show_storePath").append($store_path);
			
			var opt = $("<option/>").val(item.attributes.path).text(item.attributes.path);
			//$('#sysconfig_logstore_localpath').append(opt);
			$('#archive_path').append(opt);
			
			pathList += item.attributes.path;
			if(index < checkPath.length-1){
				pathList += ";";
			}
			
		});
		$("#archive_path_list").val(pathList);
		//var selectValue = $("#sysconfig_logstore_localpath").val();
		//$('#archive_path').val(selectValue);
		simSysConfLogStore.closeStorePath();
	};
	simSysConfLogStore.onBeforeCheck = function(node,checked){
		var isDisk = true;
		if(checked){
			var isWindows = $("#isWindows").val();
			var checkNode = node.id;
			checkNode =  checkNode.substring(0,3);
			if(isWindows=="true"){
				var checkPath = $("#storePath_tree").tree("getChecked");
				$.each(checkPath,function(index,item){
					var path = item.attributes.path;
					if(node.id.length==3){
						showErrorMessage("根目录不允许作为存储路径！");
						isDisk = false;
						return false;
					}
					if(node.id != path){
						if(path.indexOf(checkNode) >= 0){
							   showErrorMessage("同一分区只能选择一个路径！");
							  // var nodeId = $('#storePath_tree').tree('find',node.id);
							  // $("#storePath_tree").tree("uncheck",nodeId.target);
							   isDisk = false;
							   return false;
						}
					}
				});
			}
		}
		return isDisk;
	};
	simSysConfLogStore.deletePath = function(path){
		$('#archive_path').empty();
//		$('#sysconfig_logstore_localpath').empty();
		var path_list = "";
		var allDiv = $("#sysconfig_logstore_form .alert");
	    $.each(allDiv,function(i,item){
	    	if(this.id != path){
	    		var opt = $("<option/>").val(this.id).text(this.id);
	 		    $('#archive_path').append(opt);
//	 		    $('#sysconfig_logstore_localpath').append(opt);
	 		    path_list += this.id;
	 		    if(i < allDiv.length-1){
	 		    	path_list+=";"; 
		 		}
	    	}
	   });
	    $("#archive_path_list").val(path_list);
	};
	$('#sysconfig_logstore_form .alert').die().live('closed', function (event) {
		var target = event.target;
		var path = $(target).attr('id');
		simSysConfLogStore.deletePath(path);
	});
	$("#storePath").click(function(){
		simSysConfLogStore.openStorePath();
	});
});