/**
 * aap
 * 告警规则配置
 * @returns
 */
var aap = {
};
aap.openFilter = function(){
	var securityObjectType = $("#securityObjectType").val();
	if(!securityObjectType)return false;
	$('<div/>').dialog({   
		id: 'sysconfig_filter_dialog',
	    title: '过滤器编辑窗口', 
	    iconCls: 'icon-filter',
	    width: 850,
	    height: 450,
	    modal:true,
	    cache: false,
	    href: '/sim/monitor/alarmPolicyFilter?securityObjectType='+securityObjectType,   
	    onClose : function() {
	    	//*****销毁时间选择框*****
	    	$('#filter_editor_form .form-datetime').datetimepicker('remove');
	    	//*****销毁右键菜单*****
	    	$('#filter_etitor_tree_menu').menu('destroy');
	    	//*****销毁此弹出窗口*****
			$(this).dialog('destroy');
		}
	}); 
	return false;
} ;
