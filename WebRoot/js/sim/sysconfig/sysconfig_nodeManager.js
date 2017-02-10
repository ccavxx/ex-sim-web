/**
 * 节点管理
 * @type
 */
var simSysConfNodeManage = {};
$(function(){
	/**
	 * 初始化节点管理列表
	 */
	$('#sysconf_nodeManager_table').datagrid({
	    url : '/sim/node/allNodePage',
		idField : 'resourceId',
		border : false,
		fit : true,
		fitColumns : true,
		nowrap : false,
		striped : true,
		rownumbers : true,
		pagination : true,
		remoteSort : false,
		singleSelect:true,
	    columns:[[
	        {field:'state',title:'状态',width:3,formatter:formatterState},
	        {field:'resourceName',title:'名称',editor:'text',sortable:true,width:20,formatter:formatterName},
	        {field:'ip',title:'IP地址',width:15,
	        	sortable:true,
	        	sorter : function(a1, a2) {
					var a = a1.split('.');
					var b = a2.split('.');
					for(var i = 0; i < 4; i++) {
						var ai = parseInt(a[i]);
						var bi = parseInt(b[i]);
						if(ai == bi){
							continue ;
						}
						return ai-bi ;
					}
					return 0 ;
	        	}
	        },
	        {field:'entyType',title:'类型',sortable:true,width:20},
	        {field:'version',title:'版本',sortable:true,width:20},
	        {field:'aliveTime',title:'运行时间',sortable:true,width:20},
	        {field:'operation',title:'操作',width:10,align:'left',formatter:formatterOperation}
	    ]],
		onLoadSuccess : function(data){
			$('.status_active_class').linkbutton({   
			    iconCls: 'icon-status-1',
			    plain:true
			});
			$('.status_no_active_class').linkbutton({   
			    iconCls: 'icon-status-2',
			    plain:true
			});
			$('.operation_active_class').linkbutton({plain:true});
		}
	});
	/**
	 * 格式化状态显示
	 */
	function formatterState(value,row,index){
		if(value==0){
			return '<a class="status_active_class" href="#" title="在线"></a>';
		}
		return '<a class="status_no_active_class" href="#" title="不在线"></a>';
	}
	/**
	 * 名称单元格显示
	 */
	function formatterName(value,row,index){
		var result = '<div class="editable" style="display:inline-block;" onmouseenter="simSysConfNodeManage.nodeNameEditMouseenter(this);" onmouseleave="simSysConfNodeManage.nodeNameEditMouseleave(this);">';
		result += value;
		result += '</div><div class="editable_input" style="display: none;">';
		result += '<input type="text" style="height:22px;width:120px;margin:0;padding:0;" maxlength="30" name="name" value="'+value+'" onblur="simSysConfNodeManage.nodeNameEditOnblur(\''+row.resourceId+'\',this)"/></div>';
		return result;
	}
	/**
	 * 操作按钮显示
	 */
	function formatterOperation(value,row,index){
		var btnDownload = '<a class="icon-export icon16" href="javascript:void(0)" title="下载诊断信息"';
		btnDownload += 'onclick="simSysConfNodeManage.downloadLog('+row.resourceId+',\''+row.ip+'\',\''+row.type+'\',\''+row.nodeId+'\')"';
		btnDownload += '></a>';
		if(row.action == "delAction"){
			btnDownload+= "<a class='icon-remove icon16' style='padding-left:5px;' href='#' title='删除节点' onclick='simSysConfNodeManage.deleteNode(\""+row.nodeId+"\","+index+")'></a>";
		}
		if(row.protectState != undefined){
			var protectStateIcon = row.protectState ? "icon-disable-protection" : "icon-enable-protection" ;
			var protectStateTitle = row.protectState ? "禁用自保护" : "启用自保护" ;
			btnDownload+= "<a class='icon16 "+protectStateIcon+"' href='#' title='"+protectStateTitle+"' onclick='simSysConfNodeManage.changeProtectState(\""+row.nodeId+"\","+!row.protectState+")'></a>"
		}
		if(row.showPcap){
			btnDownload += "<a href='/sim/pcap/ui?nodeId="+row.nodeId+"' class='icon16 icon-monitor' title='抓包' target='_blank'></a>" ;
		}
		return btnDownload;
	}
	simSysConfNodeManage.deleteNode = function(nodeId,rowIndex){
		var row = $("#sysconf_nodeManager_table").datagrid("getRows")[rowIndex] ;
		var nodeName = row.resourceName ;
		$.messager.confirm("确认","你确定要删除节点"+nodeName+"吗？",function(answer){
			if(answer){
				$.getJSON("/sim/node/delete?nodeId="+nodeId+"&_time="+new Date().getTime(),function(result){
					if(result.success){
						$("#sysconf_nodeManager_table").datagrid("reload") ;
					}else{
						showErrorMessage(result.message) ;
					}
				}) ;
			}
		}) ;
	}
	simSysConfNodeManage.changeProtectState = function(nodeId,state){
		var url = "/sim/node/changeProtectState?nodeId="+nodeId+"&state="+state ;
		$.getJSON(url,function(result){
			if(result.success){
				$("#sysconf_nodeManager_table").datagrid("reload") ;
			}else{
				showErrorMessage(result.message) ;
			}
		}) ;
	}
	simSysConfNodeManage.queryNodes = function() {
		var $sysconf_nodeManager_query_Form = $("#sysconf_nodeManager_query_Form");
		var queryParams = {};
		queryParams.resourceName = $sysconf_nodeManager_query_Form.find("[name=resourceName]").val();
		queryParams.state = $sysconf_nodeManager_query_Form.find("[name=state]").val();
		queryParams.ip = $sysconf_nodeManager_query_Form.find("[name=ip]").val();
		$('#sysconf_nodeManager_table').datagrid({
			queryParams : queryParams
		});
	}
	/**
	 * 编辑名称监听添加图标
	 */
	simSysConfNodeManage.nodeNameEditMouseenter = function(it){
		// 为编辑按钮绑定函数
		$(it).append($("<i class='icon-edit' style='margin-left:6px;height:16px;width:30px;'/>").click(nodeNameShowInput));
	}
	/**
	 * 编辑名称监听移除图标
	 */
	simSysConfNodeManage.nodeNameEditMouseleave = function(it){
		$(it).children("i").remove();
	}
	/**
	 * 编辑名称监听显示表单输入框并取得焦点
	 */
	nodeNameShowInput = function(){
		$(this).parent().css("display","none") ;
		$(this).parent().siblings().css("display","block");
		$(this).parent().siblings().find("input").val($(this).parent().text());
		$(this).parent().siblings().find("input").focus();
	}
	/**
	 * 编辑名称监听·名称单元格编辑提交
	 */
	simSysConfNodeManage.nodeNameEditOnblur = function(resourceId,it){
		var oldVal = $.trim($(it).parent().siblings().text());
		var newVal = $.trim($(it).val());
		var lengFlag = false;
		if(newVal.length === 0){
			showAlertMessage("节点名称不能为空");
			lengFlag = true;
		}
		if(newVal.length > 30){
			showAlertMessage("节点名称长度不能大于30");
			lengFlag = true;
		}
		$(it).parent().css("display","none") ;
		$(it).parent().siblings().css("display","inline-block");
		if(lengFlag){
			$(it).val(oldVal);
			return;
		}
		if(oldVal!=newVal && resourceId){
			$.post('/sim/node/editNodeName',{resourceId:resourceId,resourceName:newVal},function(data){
				if(data.status){
					$(it).parent().siblings().text(newVal);
					var datagrid = $("#sysconf_nodeManager_table"); 
					var allRows = datagrid.datagrid("getRows") ;
					var editRowIndex = datagrid.datagrid("getRowIndex",resourceId) ;
					allRows[editRowIndex].resourceName = newVal ;
				}else{
					$(it).val(oldVal);
					showAlertMessage("节点名称修改失败,请检查输入数据");
				}
				$('#sysconf_nodeManager_table').datagrid("unselectAll");
			},'json');
		}
	}
	/**
	 * 下载日志
	 */
	simSysConfNodeManage.downloadLog = function( resourceId, ip, type, nodeId ){
		if( resourceId&&ip&&type&&nodeId ){
			$.getJSON('/sim/node/checkNodeIsOnline?_time='+new Date().getTime(),{resourceId:resourceId,ip:ip,type:type,nodeId:nodeId,timestamp:new Date()},
					checkNodeIsOnlineCallBack);
		}
	}
	/**
	 * 下载日志·回调函数
	 */
	function checkNodeIsOnlineCallBack(json){
		 if(json){
	 	 	var isOnline=json.isOnline;
	 	 	if(isOnline==false){
	 	 		if(json.ip=='127.0.0.1'&&json.type=="Auditor"){
	 	 			var url="/sim/node/sendLogCommandForKernel?timestamp="+new Date();
					window.location = url;
	 	 		}else{
	 	 			showAlertMessage("当前节点已经下线,无法下载日志!");
	 	 		}
	 	 	}else if(isOnline==true){
 	 		 	var url="/sim/node/sendLogCommand?nodeId="+json.nodeId+"&timestamp="+new Date();
 	 		 	window.location = url;
	 	 	}
	 	}
	}
});