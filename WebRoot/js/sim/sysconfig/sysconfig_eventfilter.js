var simSysConfEventFilter = {};
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
	
	//如果是修改页面
	if(simSysConfEventFilterRule.editRule){
		var rule = simSysConfEventFilterRule.editRule;
		$("input[name='id']").val(rule.id);
		$("input[name='name']").val(rule.name);
		$("input[name='deviceAddress']").val(rule.deviceAddress);
		$("input[name='srcAddress']").val(rule.srcAddress);
		$("input[name='destAddress']").val(rule.destAddress);
		$("input[name='window']").val(rule.window);
		$("input[name='rate']").val(rule.rate);
		$("textarea[name='description']").val(rule.description);
		if (rule.available) {
			$("input[name='available']").attr('checked',true);
		} else {
			$("input[name='available']").attr('checked',false);
		}
	} else {
		$("input[name='available']").attr('checked',true);
	}
	
	//点击返回按钮事件
	$('#sysconfig_eventfilter_form_cancel_button').bind('click',closeRuleEditorDialog);
	
	//关闭此窗口
	function closeRuleEditorDialog(){
		$("#sysconf_eventfilterrule_editor_dialog").dialog('close');
	}
	
	//表单验证
	var sysconfig_eventfilter_form_validation = $('#sysconfig_eventfilter_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			name:'required;length[1~30]',
			deviceAddress:'ipv4',
			srcAddress:'ipv4',
			destAddress:'ipv4',
			window:'required;range[1~1440];integer[+]',
			rate:'required;range[0~100]',
			description:'length[0~255]'
		},
		valid : function(form){
			var formdata = $(form).serialize();
			$.ajax({
	            url: '/sim/EventFilterRule/save',
	            type: 'POST',
	            data: formdata,
	            dataType:'json',
	            success: function(result){
	            	if(result.status){
	            		closeRuleEditorDialog();
	            	}else{
	            		showErrorMessage(result.message);
	            	}
	            }
	        });	
		}
	}).data("validator");
});