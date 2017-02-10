var simSysConfLogFilter = {};
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点
/**
 * 函数错误处理
 * @return true不在浏览器中显示错误,false在浏览器状中显示此错误
 */
window.onerror = function(msg){
	//由于chrome中返回的msg不只自定义的msg还包含系统附加的前辍，所以采用indexOf来判断
	if(msg.indexOf(ONLY_LEAF_CAN_SELECTED) > -1){
		return true ;
	}
	return false ;
}
$(function(){
	//只选择叶子节点
	simSysConfLogFilter.onBeforeSelect = function(node){
		var tree = $(this);
		var isleaf = tree.tree('isLeaf', node.target);
		if(isleaf){
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
	//点击叶子节点后，更新输入框的值
	simSysConfLogFilter.onClick = function(node){
		var isleaf = $(this).tree('isLeaf', node.target);
		if(isleaf){
			$('#sysconfig_logfilter_logsourcetype').combo('setText',node.text);
			$('#sysconfig_logfilter_logsourcetype').combo('setValue',node.value);
			$("input[name='securityObjectType']").val(node.id).blur();
		}
	}
	simSysConfLogFilter.onLoadSuccess = function(node, data){
		//如果是修改页面
		if(simSysConfLogFilterRule.editRule){
			var rule = simSysConfLogFilterRule.editRule;
			$("input[name='id']").val(rule.id);
			$("input[name='name']").val(rule.name);
			$("input[name='securityObjectType']").val(rule.deviceType);
			$('#sysconfig_logfilter_logsourcetype').combo('setText',rule.deviceTypeName);
			$("input[name='available'][value='"+rule.available+"']").attr('checked',true);
			$("input[name='discard'][value='"+rule.discard+"']").attr('checked',true);
			$("textarea[name='filterSql']").val(rule.condition);
		} else {
			$("input[name='available']").attr('checked',true);
		}
	};
	simSysConfLogFilter.displayPathName = function(){
		var cbt = $(this) ; 
		var selectNode = cbt.combotree("tree").tree("getSelected") ;
		cbt.combotree("setText",selectNode.pathName) ;
	};
	//过滤器-默认按钮事件
	$('#sysconfig_logfilter_form_filter_default_btn').bind('click',function(){
		$("textarea[name='filterSql']").val('SELECTOR(TRUE)');
	});
	//点击取消按钮事件
	$('#sysconfig_logfilter_form_cancel_button').bind('click',closeRuleEditorDialog);
	//关闭此窗口
	function closeRuleEditorDialog(){
		$("#sysconf_logfilterrule_editor_dialog").panel('close');
		return false;
	}
	//过滤器-编辑按钮事件
	$('#sysconfig_logfilter_form_filter_edit_btn').bind('click',function(){
		var id = $("input[name='securityObjectType']").val();
		if(!id) {
			showAlertMessage("请选择日志源类型！") ;
			return false;
		}
		var sql = $("textarea[name='filterSql']").val();
		$('<div/>').dialog({   
			id: 'sysconfig_filter_dialog',
		    title: '过滤器编辑窗口', 
		    iconCls: 'icon-filter',
		    width: 850,
		    height: 600,
		    modal:true,
		    cache: false,
		    href: '/sim/LogFilterRule/getFilterFieldById?id='+id+"&filterSql="+encodeURIComponent(sql),   
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
	});	
	
	//初始化表单验证组件，并创建表单验证实例
	var sysconfig_logfilter_form_validation = $('#sysconfig_logfilter_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			name:'required;length[1~30];contentRule;remote[/sim/LogFilterRule/validateLogFilterRule, id];',
			securityObjectType:'required',
			filterSql:'required'
		},
		valid : function(form){
	    	var formdataarray = $(form).serializeArray();
	    	var formdata = {};
	    	$.map(formdataarray, function(data){
	    		formdata[data.name] = data.value;
	    	});		
	        $.ajax({
	            url: '/sim/LogFilterRule/saveOrUpdateSimRule',
	            type: 'POST',
	            data: JSON.stringify(formdata),
	            dataType:'json',
	            contentType:"text/javascript",
	            success: function(res){
	            	if(res && res.status){
	            		closeRuleEditorDialog();
	            	}else{
	            		showErrorMessage(res.message);
	            	}
	            }
	        });	
		}
	}).data( "validator" );	
});