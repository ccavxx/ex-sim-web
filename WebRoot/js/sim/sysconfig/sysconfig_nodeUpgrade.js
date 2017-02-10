/**
 * 节点升级
 * @type 
 */
var simSysConfNodeUpgrade = {editUpgrade:null};

/**
 * 打开上传升级包窗口
 */
simSysConfNodeUpgrade.openUploadFileDialog = function(urlTemp){
	var url = '/page/sysconfig/sysconfig_nodeUpgrade_upFile.html';
	if(urlTemp){
		url = urlTemp;
	}
	var w = $('#sysconf_nodeUpgrade_table_panel').panel('panel').width();
	var h = $('#sysconf_nodeUpgrade_table_panel').panel('panel').height();
	var top = $('#sysconf_nodeUpgrade_table_panel').panel('panel').position().top;
	var left = $('#sysconf_nodeUpgrade_table_panel').panel('panel').position().left;

	$("#sysconf_nodeUpgrade_editor_dialog").dialog({
		href: url,
		style:{'padding':0,'border':0},
		top:top,
		left:left,
		width:w,
		height:h,
		shadow:false,
		inline:true,
		noheader:true,
		border:false,
		onClose:simSysConfNodeUpgrade.reloadNodeUpgradeTable
	});
}

/**
 * 关闭窗口
 */
simSysConfNodeUpgrade.closeUploadFileDialog = function(urlTemp){
	$("#sysconf_nodeUpgrade_editor_dialog").dialog("close");
}

/**
 * 重新加载节点升级包列表
 */
simSysConfNodeUpgrade.reloadNodeUpgradeTable = function(){
	$('#sysconf_nodeUpgrade_table').datagrid('reload');//重新加载本页
	$('#sysconf_nodeUpgrade_table').datagrid('unselectAll');//取消所有选择
	simSysConfNodeUpgrade.editUpgrade = null;//每次刷新列表清空此值
}

/**
 * 设置升级计划
 */
simSysConfNodeUpgrade.configNodeUpgradePlan = function(){
	simSysConfNodeUpgrade.openUploadFileDialog("/sim/sysconfig/upgrade/nodeUpgradeRefreshPlan");
}

$(function(){
	/**
	 * 初始化节点升级包列表
	 */
	$('#sysconf_nodeUpgrade_table').datagrid({
	    url : '/sim/sysconfig/upgrade/queryUpdateNodeList',
		idField : 'id',
		border:false,
		scrollbarSize:0,
		fit:true,
		nowrap : false,
		striped : true,
		fitColumns : true,
		pagination : true,
		rownumbers : true,
		singleSelect : true,
		toolbar:'#sysconf_nodeUpgrade_table_toolbar',
		onLoadSuccess : function(data){
			$('.node_upgrade_table_row_btn').linkbutton();
		}
	});
});