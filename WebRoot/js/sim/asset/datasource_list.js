/**
 * 日志源列表
 * @returns
 */
var dl = {};

/**
 * 新建日志源
 */
dl.addDataSource = function(){
	dl.showForm("add");
}

/**
 * 编辑日志源
 */
dl.editDataSource = function(id){
	dl.showForm("edit", id) ;
}

/**
 * 显示日志源表单页面
 */
dl.showForm = function(operation, resourceId){
	var dl_table_panel = $("#dl_panel").panel("panel");
	if(!dl_table_panel)return;
	var w = $(dl_table_panel).width();
	var h = $(dl_table_panel).height();
	$("#dl_dialog").addClass("horizonCss-dialog-centerDiv");
	var url = '/sim/datasource/dataSourceForm?operation='+operation+"&id="+resourceId;
	dl.dl_dialog = $("#dl_dialog").dialog({
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
			handler:dl.saveDataSource
		},{
			text:"返回",
			handler:dl.closeDataSource
		}],
		onClose:dl.reloadDataSourceTable
	});
}

/**
 * 提交日志源表单
 */
dl.saveDataSource = function(){
	$("#dsf_form").submit();
}

/**
 * 关闭日志源编辑窗口
 */
dl.closeDataSource = function(){
	$("#dl_dialog").dialog('close');
}

/**
 * 启用、禁用日志源
 */
dl.changeDataSource = function(state){
	var selectRows = $("#dl_table").datagrid("getChecked");
	if (selectRows.length > 0) {
		
		if (state) {
			var isHaveForbidden;
			//遍历所选日志源是否含有禁用日志源
			for ( var i = 0; i < selectRows.length; i++) {
				if (selectRows[i].available == 0) {
					isHaveForbidden = true;
					break;
				}
			}
			if (isHaveForbidden) {//点击启用按钮时，勾选的日志源中有禁用日志源才会弹提示框
				$.messager.confirm("确认","你确定要启用选中的日志源吗？",function(r){
					if(!r) return;
					dl.changeState(selectRows, state);
				});
			} else {
				showAlertMessage("日志源已启用");
			}
		}else{
			var isHaveEnabled;
			//遍历所选日志源是否含有启用日志源
			for ( var i = 0; i < selectRows.length; i++) {
				if (selectRows[i].available == 1) {
					isHaveEnabled = true;
					break;
				}
			}
			if (isHaveEnabled) {//点击禁用按钮时，勾选的日志源中有启用日志源才会弹提示框
				$.messager.confirm("确认","你确定要禁用选中的日志源吗？",function(r){
					if(!r) return;
					dl.changeState(selectRows, state);
				});
			} else {
				showAlertMessage("日志源已禁用");
			}
		}
	} else {
		var operation = state == 1 ? "启用" : "禁用";
		showAlertMessage("请选择要"+operation+"的日志源！");
	}
}

/**
 * 启用、禁用入库操作
 */
dl.changeState = function(selectRows, state){
	var id = "";
	$.each(selectRows, function(index, selectRow){
		if(selectRow.available != state){
			var rowid = selectRow.resourceId;
			id += (rowid + ",");
		}
	});
	if(id.length > 1){
		id = id.substring(0, id.length-1);
		$.getJSON('/sim/datasource/switchState?_time=' + new Date().getTime(),
				{id:id, available:state},
				function(data){
					if(data.status){
						dl.reloadDataSourceTable();
					} else {
						dl.reloadDataSourceTable();
		        		showErrorMessage(data.message);
		        	}
				}
		);
	}
}

/**
 * 删除日志源
 */
dl.deleteSelectDataSources = function(){
	var selectRows = $("#dl_table").datagrid("getChecked") ;
	if(selectRows.length > 0){
		$.messager.confirm("警告","你确定要删除选中的日志源吗？",function(r){
			if(!r) return;
			var id = "";
			$.each(selectRows,function(index,row){
				id = id + row.resourceId+",";
			});
			$.getJSON('/sim/datasource/delete?id='+id,function(result){
				if(result.status){
					dl.reloadDataSourceTable();
				} else {
					showErrorMessage(result.message);
				}
			});
		});
	}else{
		showAlertMessage("请选择要删除的日志源！");
	}
}

/**
 * 操作列删除单个日志源
 */
dl.deleteOneDataSource = function(id){
	$.messager.confirm("警告","你确定要删除选中的日志源吗？",function(r){
		if(!r) return ;
		$.getJSON('/sim/datasource/delete?id='+id,function(result){
			if (result.status) {
				dl.reloadDataSourceTable();
			} else {
				showErrorMessage(result.message);
			}
		});
	}) ;
};

/**
 * 表格状态列格式化
 */
dl.stateFormatter = function(value,row,index){
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
dl.operationFormatter = function(value,row,index){
	var id = row.resourceId ;
	var html = "<a href='#' class='icon-edit hand icon16' title='编辑' onclick=\"dl.editDataSource('"+id+"')\"></a>" +"&nbsp&nbsp";
	html += "<a href='#' class='icon-remove hand icon16' title='删除' onclick=\"dl.deleteOneDataSource('"+id+"')\"></a>" ;
	return html ;
}

/**
 * 存储原始日志列格式化
 */
dl.saveRawLogFormatter = function(value,row,index){
	var html;
	if (value) {
		html = "是";
	} else {
		html = "否";
	}
	return html;
}

/**
 * 覆盖日志时间列格式化
 */
dl.overwriteEventTimeFormatter = function(value,row,index){
	var html;
	if (value) {
		html = "是";
	} else {
		html = "否";
	}
	return html;
}

/**
 * 日志保存时间列格式化
 */
dl.archiveTimeFormatter = function(value,row,index){
	if (value == '10000m') {
		return "永远";
	}
	return value.substring(0,value.length-1)+"个月";
}

/**
 * 报表保存时间列格式化
 */
dl.reportKeepTimeFormatter = function(value,row,index){
	if (value == '10000m') {
		return "永远";
	}
	return value.substring(0,value.length-1)+"个月";
}

/**
 * 表格双击事件
 */
dl.dataSourceDblClickRow = function(rowIndex, rowData){
	var rowData = $(this).datagrid("getRows")[rowIndex];
	dl.editDataSource(rowData.resourceId);
}

/**
 * 重新加载日志源列表
 */
dl.reloadDataSourceTable = function(){
	$('#dl_table').datagrid('reload');//重新加载本页
	$('#dl_table').datagrid('unselectAll');//取消所有选择
	$('#dl_table').datagrid('uncheckAll');//取消所有选择
}

/**
 * 列表工具栏查询
 */
dl.searchQuery = function(){
	var data = {
			  ip:$("#dl_toolbar_form input[name=ip]").val(),
			  name:$("#dl_toolbar_form input[name=name]").val(),
			  dataSourceType:$("#dl_toolbar_dataSourceType").combotree("getValue"),
			  collectMethod:$("#dl_toolbar_collectMethod").combobox("getValue"),
			  state:$("#dl_toolbar_state").combobox("getValue")
	};

	$("#dl_table").datagrid({
		queryParams:data
	});
}

/**
 * 列表工具栏清空
 */
dl.clearCondition = function(){
	$('#dl_toolbar_form').form('reset');
	var data = {};
	$("#dl_table").datagrid({
		queryParams:data
	});
}

/**
 * 日志源类型组合框选择前事件
 */
dl.beforeDataSourceSelect = function(node){
	var tree = $("#dl_toolbar_dataSourceType").combotree("tree") ;
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