/**
 * 监视对象列表
 * @returns
 */
var ml = {};

/**
 * 新建监视对象
 */
ml.addMonitor = function(){
	ml.showForm("add");
}

/**
 * 编辑监视对象
 */
ml.editMonitor = function(id){
	ml.showForm("edit", id) ;
}

/**
 * 显示监视对象表单页面
 */
ml.showForm = function(operation, resourceId){
	var ml_table_panel = $("#ml_panel").panel("panel");
	if(!ml_table_panel)return;
	var w = $(ml_table_panel).width();
	var h = $(ml_table_panel).height();
	$("#ml_dialog").addClass("horizonCss-dialog-centerDiv");
	var url = '/sim/monitor/monitorForm?operation='+operation+"&id="+resourceId;
	ml.ml_dialog = $("#ml_dialog").dialog({
		href:url,
		top:0,
		left:0,
		width:w,
		height:h,
		inline:true,
		noheader:true,
		modal:true,
		shadow:false,
		border:false,
		buttons:[{text:"保存",
			handler:ml.saveMonitor
		},{
			text:"返回",
			handler:ml.closeMonitor
		}],
		onClose:ml.reloadMonitorTable
	});
}

/**
 * 提交监视对象表单
 */
ml.saveMonitor = function(){
	$("#mf_form").submit();
}

/**
 * 关闭监视对象编辑窗口
 */
ml.closeMonitor = function(){
	$("#ml_dialog").dialog('close');
}

/**
 * 启用、禁用监视对象
 */
ml.changeMonitor = function(state){
	var selectRows = $("#ml_table").datagrid("getChecked");
	if (selectRows.length > 0) {
		
		if (state) {
			var isHaveForbidden;
			//遍历所选监视对象是否含有禁用监视对象
			for ( var i = 0; i < selectRows.length; i++) {
				if (selectRows[i].available == 0) {
					isHaveForbidden = true;
					break;
				}
			}
			if (isHaveForbidden) {//点击启用按钮时，勾选的监视对象中有禁用监视对象才会弹提示框
				$.messager.confirm("确认","你确定要启用选中的监视对象吗？",function(r){
					if(!r) return;
					ml.changeState(selectRows, state);
				});
			} else {
				showAlertMessage("监视对象已启用");
			}
		}else{
			var isHaveEnabled;
			//遍历所选监视对象是否含有启用监视对象
			for ( var i = 0; i < selectRows.length; i++) {
				if (selectRows[i].available == 1) {
					isHaveEnabled = true;
					break;
				}
			}
			if (isHaveEnabled) {//点击禁用按钮时，勾选的监视对象中有启用监视对象才会弹提示框
				$.messager.confirm("确认","你确定要禁用选中的监视对象吗？",function(r){
					if(!r) return;
					ml.changeState(selectRows, state);
				});
			} else {
				showAlertMessage("监视对象已禁用");
			}
		}
	} else {
		var operation = state == 1 ? "启用" : "禁用";
		showAlertMessage("请选择要"+operation+"的监视对象！");
	}
}

/**
 * 启用、禁用入库操作
 */
ml.changeState = function(selectRows, state){
	var id = "";
	$.each(selectRows, function(index, selectRow){
		if(selectRow.available != state){
			var rowid = selectRow.resourceId;
			id += (rowid + ",");
		}
	});
	if(id.length > 1){
		id = id.substring(0, id.length-1);
		$.getJSON('/sim/monitor/switchState?_time=' + new Date().getTime(),
				{id:id, available:state},
				function(data){
					if(data.status){
						ml.reloadMonitorTable();
					} else {
						ml.reloadMonitorTable();
		        		showErrorMessage(data.message);
		        	}
				}
		);
	}
}

/**
 * 删除监视对象
 */
ml.deleteSelectMonitors = function(){
	var selectRows = $("#ml_table").datagrid("getChecked") ;
	if(selectRows.length > 0){
		$.messager.confirm("警告","你确定要删除选中的监视对象吗？",function(r){
			if(!r) return;
			var id = "";
			$.each(selectRows,function(index,row){
				id = id + row.resourceId+",";
			});
			$.getJSON('/sim/monitor/delete?id='+id,function(result){
				if(result.status){
					ml.reloadMonitorTable();
				} else {
					showErrorMessage(result.message);
				}
			});
		});
	}else{
		showAlertMessage("请选择要删除的监视对象！");
	}
}

/**
 * 操作列删除单个监视对象
 */
ml.deleteOneMonitor = function(id){
	$.messager.confirm("警告","你确定要删除选中的监视对象吗？",function(r){
		if(!r) return ;
		$.getJSON('/sim/monitor/delete?id='+id,function(result){
			if (result.status) {
				ml.reloadMonitorTable();
			} else {
				showErrorMessage(result.message);
			}
		});
	}) ;
};

/**
 * 表格状态列格式化
 */
ml.stateFormatter = function(value,row,index){
	var html;
	if (value) {
		html = "启用";
	} else {
		html = "禁用";
	}
	return html ;
}

/**
 * 表格操作列格式化
 */
ml.operationFormatter = function(value,row,index){
	var id = row.resourceId ;
	var html = "<a href='#' class='icon-edit hand icon16' title='编辑' onclick=\"ml.editMonitor('"+id+"')\"></a>" +"&nbsp&nbsp";
	html += "<a href='#' class='icon-remove hand icon16' title='删除' onclick=\"ml.deleteOneMonitor('"+id+"')\"></a>" ;
	return html ;
}

/**
 * 表格双击事件
 */
ml.monitorDblClickRow = function(rowIndex, rowData){
	var rowData = $(this).datagrid("getRows")[rowIndex];
	ml.editMonitor(rowData.resourceId);
}

/**
 * 重新加载监视对象列表
 */
ml.reloadMonitorTable = function (){
	$('#ml_table').datagrid('reload');//重新加载本页
	$('#ml_table').datagrid('unselectAll');//取消所有选择
	$('#ml_table').datagrid('uncheckAll');//取消所有选择
}

/**
 * 列表工具栏查询
 */
ml.searchQuery = function(){
	var data = {
			  ip:$("#ml_toolbar_form input[name=ip]").val(),
			  name:$("#ml_toolbar_form input[name=name]").val(),
			  dataSourceType:$("#ml_toolbar_dataSourceType").combotree("getValue"),
			  collectMethod:$("#ml_toolbar_collectMethod").combobox("getValue"),
			  state:$("#ml_toolbar_state").combobox("getValue")
	};

	$("#ml_table").datagrid({
		queryParams:data
	});
}

/**
 * 列表工具栏清空
 */
ml.clearCondition = function(){
	$('#ml_toolbar_form').form('reset');
	var data = {};
	$("#ml_table").datagrid({
		queryParams:data
	});
}

/**
 * 监视对象类型组合框选择前事件
 */
ml.beforeDataSourceSelect = function(node){
	var tree = $("#ml_toolbar_dataSourceType").combotree("tree") ;
	if(tree.tree("isLeaf",node.target)){
		return true ;
	}else{
		if(node.state == "closed"){
			tree.tree("expand",node.target);
		}else{
			tree.tree("collapse",node.target);
		}
		throw new Error(ONLY_LEAF_CAN_SELECTED) ;//抛出错误，阻止下拉tree被关闭
	}
}

$(function(){
	
});