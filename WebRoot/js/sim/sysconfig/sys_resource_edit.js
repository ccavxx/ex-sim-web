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
	if(simSysConfResource.editResource){
		var resource = simSysConfResource.editResource;
		$("#sysconfig_resource_form input[name='id']").val(resource.id);
		$("#sysconfig_resource_form input[name='name']").val(resource.name);
		$("#sysconfig_resource_form input[name='startIP']").val(resource.startIp);
		$("#sysconfig_resource_form input[name='endIP']").val(resource.endIp);
	}
	
	//点击返回按钮事件
	$('#sysconfig_resource_form_cancel_button').bind('click',closeResourceEditorDialog);
	
	//关闭此窗口
	function closeResourceEditorDialog(){
		$("#sysconf_resource_editor_dialog").dialog('close');
	}
	
	/**
	 * 表单验证、提交
	 */
	var sysconfig_resource_form_validation = $('#sysconfig_resource_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			name:'required;length[1~30]',
			startIP:'required;ipv4',
			endIP:'required;ipv4'
		},
		valid : function(form){
			var formdata = $(form).serialize();
			$.ajax({
	            url: '/sim/resource/saveIpLocation',
	            type: 'POST',
	            data: formdata,
	            dataType:'json',
	            success: function(result){
	            	if(result.status){
	            		closeResourceEditorDialog();
	            	}else{
	            		showErrorMessage(result.message);
	            	}
	            }
	        });	
		}
	}).data("validator");
});