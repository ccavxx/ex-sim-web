/**
 * 日志过滤规则
 * @type 
 */
var simSysConfLogFilterRule = {editRule:null};
$(function(){
	/**
	 * 编辑按钮
	 */
	simSysConfLogFilterRule.formatterOperation = function(value,row,index){
		 return "<a title='编辑' style='width:16px;height:16px;cursor: pointer;' class='icon-edit' onclick='simSysConfLogFilterRule.editLogFilterRule(\""+index+"\")'></a>";
	}
	/**
	 * 初始化过滤规则列表
	 */
	$('#sysconf_logfilterrule_table').datagrid({   
	    url : '/sim/LogFilterRule/getLogFilterRuleList',
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
	    columns:[[
	        {field:"ck",checkbox:true,width:3},
	        {field:'nameEscapeHtml',title:'规则名称',width:10,sortable:true},   
	        {field:'deviceTypeName',title:'日志源类型',width:20,sortable:true},   
	        {field:'conditionEscapeHtml',title:'条件',width:57,sortable:true},
	        {field:'available',title:'状态',width:5,sortable:true,formatter:formatterAvailable},
	        {field:'creater',title:'创建人',width:5,sortable:true},
	        {field:'operation', title:'操作', width:5, align:'center', formatter:simSysConfLogFilterRule.formatterOperation}
	    ]],
	    toolbar:'#sysconf_logfilterrule_table_toolbar'
	});
	/**
	 * 格式化状态显示
	 */
	function formatterAvailable(value,row,index){
		if(value == 1)
			return "启用";
		return "禁用";
	}
	/**
	 * 格式化是否丢弃显示
	 */
	function formatterDiscard(value,row,index){
		if(value=="true")
			return "是";
		return "否";
	}
	/**
	 * 	启用/禁用选中信息
	 */
	simSysConfLogFilterRule.changeAvailable = function(available){

		var selrows = $('#sysconf_logfilterrule_table').datagrid('getChecked');

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
				$.getJSON('/sim/LogFilterRule/modifyBatchLogFilterRuleAvailable?_time=' + new Date().getTime(),
						{ids:ids, available:available},
						function(data){
							if(data.status){
								reloadLogFilterRuleTable();
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
	 * 打开日志过滤规则编辑窗口
	 */
	function openRuleEditorDialog(){
		var w = $('#sysconfig_container').panel('panel').width();
		var h = $('#sysconfig_container').panel('panel').height();
		var top = $('#sysconf_logfilterrule_table_panel').panel('panel').position().top;
		var left = $('#sysconf_logfilterrule_table_panel').panel('panel').position().left;
		
		$("#sysconf_logfilterrule_editor_dialog").dialog({
			href:'/page/sysconfig/sysconfig_logfilter.html',
			style:{'padding':0,'border':0},
			top:top,
			left:left,
			width:w,
			height:h,
			inline:true,
			noheader:true,
			shadow:false,
			fit:true,
			border:false,
			onClose:reloadLogFilterRuleTable
		});		
	}

	/**
	 * 删除过滤规则
	 */
	simSysConfLogFilterRule.removeLogFilterRule = function(){

		var selrows = $('#sysconf_logfilterrule_table').datagrid('getChecked');

		if(selrows.length > 0) {
			var ids = "";
			$.each(selrows, function(index, selrow){
				var rowid = selrow.id;
				ids += rowid;
				if(index != selrows.length-1){
					ids += ",";
				}
			});
			$.messager.confirm('警告', '您确定要删除选中的规则吗？', function(r){
				if (r){
					$.getJSON('/sim/LogFilterRule/deleteBatchLogFilterRule?_time='+new Date().getTime(),{ids:ids},function(data){
						if(data.status) {
							reloadLogFilterRuleTable();
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
	 * 重新加载日志过滤规则列表
	 */
	function reloadLogFilterRuleTable(){
		$('#sysconf_logfilterrule_table').datagrid('reload');//重新加载本页
		$('#sysconf_logfilterrule_table').datagrid('unselectAll');//取消所有选择
		$('#sysconf_logfilterrule_table').datagrid('uncheckAll');//取消所有选择
		simSysConfLogFilterRule.editRule = null;//每次刷新列表清空此值
	}

	/**
	 * 增加过滤规则
	 */
	simSysConfLogFilterRule.addLogFilterRule = function(){
		openRuleEditorDialog();
	}

	/**
	 * 修改过滤规则
	 */
	simSysConfLogFilterRule.editLogFilterRule = function(selRowIndex){
		
		var rows = $('#sysconf_logfilterrule_table').datagrid('getRows');
		var selrow = rows[selRowIndex];

		if(selrow){

			simSysConfLogFilterRule.editRule = selrow;
			openRuleEditorDialog();
		}else{
			showErrorMessage( '请选择一条数据' );
		}
	}
});