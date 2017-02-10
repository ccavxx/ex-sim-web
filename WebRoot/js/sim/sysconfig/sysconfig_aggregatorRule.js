/**
 * 归并规则
 * @type 
 */
var simSysConfAggregatorRule = {editRule:null};
/**
 * 格式化状态显示
 */
simSysConfAggregatorRule.formatterAvailable = function(value,row,index){
	if(value == 1)
		return "启用";
	return "禁用";
}
/**
 * 格式化状态列集
 */
simSysConfAggregatorRule.formatterColumnSet = function(value,row,index){
	return value;
}
simSysConfAggregatorRule.formatterOperation = function(value,row,index){
	return "<a title='编辑' style='width:16px;height:16px;cursor: pointer;' class='icon-edit' onclick='simSysConfAggregatorRule.editAggregatorRule(\""+index+"\")'></a>";
}
/**
 * 操作的修改状态
 */
simSysConfAggregatorRule.changeAvailable = function(available){
	var selrows = $('#sysconf_aggregatorRule_table').datagrid('getChecked');

	if(selrows.length > 0) {
		var ids = "";
		$.each(selrows, function(index, selrow){
			if(selrow.available != available){
				var rowid = selrow.id;
				ids += (rowid + ",");
			}
		});
		
		if(ids.length > 1){
			ids = ids.substring(0, ids.length-1);
			$.getJSON(
					'/sim/AggregatorRule/modifyBatchAggregatorRuleAvailable?_time=' + new Date().getTime(),
					{ids:ids, available:available},
					function(data){
						if(data.status){
							simSysConfAggregatorRule.reloadAggregatorRuleTable();
						} else {
			        		showErrorMessage(data.message);
			        	}
					});
		}
	} else {
		showErrorMessage( '请选择一条数据' );
	}
}

/**
 * 打开归并规则编辑窗口
 */
simSysConfAggregatorRule.openRuleEditorDialog = function(){
	var w = $('#sysconf_aggregatorRule_table_panel').panel('panel').width();
	var h = $('#sysconf_aggregatorRule_table_panel').panel('panel').height();
	var top = $('#sysconf_aggregatorRule_table_panel').panel('panel').position().top;
	var left = $('#sysconf_aggregatorRule_table_panel').panel('panel').position().left;

	$("#sysconf_aggregatorRule_editor_dialog").dialog({
		href:'/page/sysconfig/sysconfig_aggregator.html',
		style:{'padding':0,'border':0},
		top:top,
		left:left,
		width:w,
		height:h,
		shadow:false,
		inline:true,
		noheader:true,
		border:false,
		onClose:simSysConfAggregatorRule.reloadAggregatorRuleTable
	});
}
/**
 * 删除归并规则
 */
simSysConfAggregatorRule.removeAggregatorRule = function(){
	var selrows = $('#sysconf_aggregatorRule_table').datagrid('getChecked');

	if(selrows.length > 0) {
		var ids = "";
		$.each(selrows, function(index, selrow){
			var rowid = selrow.id;
			ids += rowid;
			if(index != selrows.length-1){
				ids += ",";
			}
		});
		$.messager.confirm('警告','您确定要删除选中的规则吗？',function(r){   
		    if (r){
		        $.getJSON('/sim/AggregatorRule/deleteBatchAggregatorRule?_time='+new Date().getTime(),{ids:ids},function(data){
		        	if(data.status){
		        		simSysConfAggregatorRule.reloadAggregatorRuleTable();
		        	} else {
		        		showErrorMessage(data.message);
		        	}
		        });
		    }   
		});
	} else {
		showErrorMessage( '请选择一条数据' );
	}
}
/**
 * 重新加载日志归并规则列表
 */
simSysConfAggregatorRule.reloadAggregatorRuleTable = function(){
	$('#sysconf_aggregatorRule_table').datagrid('reload');//重新加载本页
	$('#sysconf_aggregatorRule_table').datagrid('unselectAll');//取消所有选择
	$('#sysconf_aggregatorRule_table').datagrid('uncheckAll');//取消所有选择
	simSysConfAggregatorRule.editRule = null;//每次刷新列表清空此值
}

/**
 * 增加归并规则
 */
simSysConfAggregatorRule.addAggregatorRule = function(){
	simSysConfAggregatorRule.openRuleEditorDialog();
}	
/**
 * 修改归并规则
 */
simSysConfAggregatorRule.editAggregatorRule = function(selRowIndex){
	
	var rows = $('#sysconf_aggregatorRule_table').datagrid('getRows');
	var selrow = rows[selRowIndex];

	if(selrow){
		
		simSysConfAggregatorRule.editRule = selrow;
		simSysConfAggregatorRule.openRuleEditorDialog();
	} else {
		showErrorMessage( '请选择一条数据' );
	}
}
$(function(){
	/**
	 * 初始化归并规则列表
	 */
	$('#sysconf_aggregatorRule_table').datagrid({
	    url : '/sim/AggregatorRule/getAggregatorRuleList',
		idField : 'id',
		fit : true,
		nowrap : false,
		striped : true,
		fitColumns : true,
		pagination : true,
		rownumbers : true,
		singleSelect : true,
		checkOnSelect : false,
		selectOnCheck : false,
	    toolbar:'#sysconf_aggregatorRule_table_toolbar'
	});
});