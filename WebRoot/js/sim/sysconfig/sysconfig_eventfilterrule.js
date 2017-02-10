/**
 * 事件过滤规则
 * @type 
 */
var simSysConfEventFilterRule = {editRule:null};
$(function(){
	
	/**
	 * 编辑按钮
	 */
	simSysConfEventFilterRule.formatterOperation = function(value,row,index){
		 return "<a title='编辑' style='width:16px;height:16px;cursor: pointer;' class='icon-edit' onclick='simSysConfEventFilterRule.editEventFilterRule(\""+index+"\")'></a>";
	}
	
	/**
	 * 初始化过滤规则列表
	 */
	$('#sysconf_eventfilterrule_table').datagrid({
	    url : '/sim/EventFilterRule/getAll',
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
		nowrap : true,
	    columns:[[
	        {field:"id",checkbox:true,width:3},
	        {field:'nameEscapeHtml',title:'事件名称',width:10,sortable:true},
	        {field:'deviceAddress',title:'设备地址',width:10,sortable:true},
	        {field:'srcAddress',title:'源地址',width:10,sortable:true},
	        {field:'destAddress',title:'目的地址',width:10,sortable:true},
	        {field:'window',title:'时间间隔（分钟）',width:10,sortable:true},
	        {field:'rate',title:'速度',width:10,sortable:true},
	        {field:'available',title:'状态',width:10,sortable:true,formatter:formatterAvailable},
	        {field:'descr',title:'事件描述',sortable:true,width:10,formatter:simHandler.eventDescFormatter},
	        {field:'creater',title:'创建人',width:5,sortable:true},
	        {field:'operation', title:'操作', width:5, align:'center', formatter:simSysConfEventFilterRule.formatterOperation}
	    ]],
	    toolbar:'#sysconf_eventfilterrule_table_toolbar',
	    onDblClickRow:function(rowIndex, rowData) {
    		simSysConfEventFilterRule.editEventFilterRule(rowIndex);
    	}
	});
	
	/**
	 * 格式化状态显示
	 */
	function formatterAvailable(value,row,index){
		if(value) {
			return "启用";
		}
		return "禁用";
	}
	
	/**
	 * 	启用/禁用选中信息
	 */
	simSysConfEventFilterRule.changeAvailable = function(available){
		var selrows = $('#sysconf_eventfilterrule_table').datagrid('getChecked');
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
				$.messager.confirm("确认","你确定要禁用选中的规则吗？",function(r){
					if(r) {
						$.getJSON('/sim/EventFilterRule/changeState?_time=' + new Date().getTime(),
								{ids:ids, available:available},
								function(data){
									if(data.status){
										reloadEventFilterRuleTable();
									} else {
						        		showErrorMessage(data.message);
						        	}
								}
						);
					}
				});
			}
		} else {
			showErrorMessage( '请选择一条数据' );
		}
	}

	/**
	 * 打开事件过滤规则编辑窗口
	 */
	function openRuleEditorDialog(){
		var w = $('#sysconfig_container').panel('panel').width();
		var h = $('#sysconfig_container').panel('panel').height();
		var top = $('#sysconf_eventfilterrule_table_panel').panel('panel').position().top;
		var left = $('#sysconf_eventfilterrule_table_panel').panel('panel').position().left;
		
		$("#sysconf_eventfilterrule_editor_dialog").dialog({
			href:'/page/sysconfig/sysconfig_eventfilter.html',
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
			onClose:reloadEventFilterRuleTable
		});		
	}

	/**
	 * 删除过滤规则
	 */
	simSysConfEventFilterRule.removeEventFilterRule = function(){

		var selrows = $('#sysconf_eventfilterrule_table').datagrid('getChecked');

		if (selrows.length > 0) {
			var ids = "";
			$.each(selrows, function(index, selrow) {
				var rowid = selrow.id;
				ids += rowid;
				if (index != selrows.length-1) {
					ids += ",";
				}
			});
			$.messager.confirm('警告', '您确定要删除选中的规则吗？', function(r) {
				if (r) {
					$.getJSON('/sim/EventFilterRule/delete?_time=' + new Date().getTime(), {ids:ids}, function(data) {
						if (data.status) {
							reloadEventFilterRuleTable();
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
	 * 重新加载事件过滤规则列表
	 */
	function reloadEventFilterRuleTable(){
		$('#sysconf_eventfilterrule_table').datagrid('reload');//重新加载本页
		$('#sysconf_eventfilterrule_table').datagrid('unselectAll');//取消所有选择
		$('#sysconf_eventfilterrule_table').datagrid('uncheckAll');//取消所有选择
		simSysConfEventFilterRule.editRule = null;//每次刷新列表清空此值
	}

	/**
	 * 增加过滤规则
	 */
	simSysConfEventFilterRule.addEventFilterRule = function(){
		openRuleEditorDialog();
	}

	/**
	 * 修改过滤规则
	 */
	simSysConfEventFilterRule.editEventFilterRule = function(selRowIndex){
		var rows = $('#sysconf_eventfilterrule_table').datagrid('getRows');
		var selrow = rows[selRowIndex];
		if(selrow){
			simSysConfEventFilterRule.editRule = selrow;
			openRuleEditorDialog();
		}else{
			showErrorMessage( '请选择一条数据' );
		}
	}
});