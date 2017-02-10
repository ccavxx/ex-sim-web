$(function(){
	var formdata = {responseId:'', jmssendlog:{}} ;

	// 初始化form validator
	var sysconfig_jmsforward_form_validation = null;
	function initFormValidator(){
		sysconfig_jmsforward_form_validation = $('#sysconfig_jmsforward_form').validator({
			theme: 'simple_right',
			stopOnError:true,
			timely: 2,
			rules: simHandler.rules,
			showOk: "",
			valid: function(form) {
				var formdataarray = $(form).serializeArray();
				formdata.jmssendlog = {};
				$.map(formdataarray,function(data){
					var value = data.name == "pass" ? rsaEncrypt(data.value) : data.value ;  
					formdata.jmssendlog[data.name] = value;
				});
				$.ajax({
					url: '/sim/systemConfig/modiryJMSForwardConfig',
					type: 'POST',
					data: JSON.stringify(formdata),
					dataType:'json',
					contentType:"text/javascript",
					success: function(res){
						if(res && res.status){
		            		showAlertMessage(res.message);
		            	} else {
		            		showErrorMessage(res.message);
		            	}
					}
				});
			}
		}).data("validator");
	}
	// 开启验证
	function setFieldForOn(){
		sysconfig_jmsforward_form_validation.setField({
	        receivers: "required;ipv4",
	        port:"required;range[0~65535];integer",
	        topic:"required",
	        frequency:"required;range[1~1000];integer[+]",
	        filterSql : "required",
	        user:"contentRule"
		});
	}
	// 关闭部分验证
	function setFieldForOff(){
		sysconfig_jmsforward_form_validation.setField({
	        receivers: "ipv4",
	        port:"range[0~65535];integer",
	        topic:null,
	        frequency:"range[1~1000];integer[+]",
	        filterSql : "required",
	        user:"contentRule"
		});
	}
	// 初始化 form 数据
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey: 'sys_cfg_jmssendlog', nodeType: 'Auditor'},function(res){
		if(res && res.status){
			initFormValidator() ;
			var data = res.result ;
			formdata.responseId = data.responseId ;
			if(data.jmssendlog.send=='true') {
				setFieldForOn() ;
			} else {
				setFieldForOff() ;
			}
			data.jmssendlog.pass = rsaDecrypt(data.jmssendlog.pass) ;
			$('#sysconfig_jmsforward_form').form('load', data.jmssendlog) ;
		}
	}) ;
	// 为状态绑定事件
	$("#sysconfig_jmsforward_form [name=send]:checkbox").change(function() {
		if(this.checked) {
			setFieldForOn();
		} else {
			$("#sysconfig_jmsforward_form").validator("cleanUp");
			setFieldForOff();
		}
	}) ;
	// 过滤器-编辑按钮事件
	$('#sysconfig_jmsforward_form_filter_edit_btn').bind('click',function(){
		var sql = $("textarea[name='filterSql']").val();
		$('<div/>').dialog({   
			id: 'sysconfig_filter_dialog',
		    title: '过滤器编辑窗口', 
		    iconCls: 'icon-filter',
		    width: 850,
		    height: 600,
		    modal:true,
		    cache: false,
		    href: '/sim/LogFilterRule/getFilterFieldById?id=Log/Forward/Filter&filterSql='+encodeURIComponent(sql),   
		    onClose : function() {
		    	//*****销毁时间选择框*****
		    	$('#filter_editor_form .form-datetime').datetimepicker('remove');
		    	//*****销毁右键菜单*****
		    	$('#filter_etitor_tree_menu').menu('destroy');
		    	//*****销毁此弹出窗口*****
				$(this).dialog('destroy');
			}
		}); 
		return false ;
	}) ;
	// 过滤器-默认按钮事件
	$('#sysconfig_jmsforward_form_filter_default_btn').bind('click',function(){
		$("textarea[name='filterSql']").val('SELECTOR(TRUE)');
	});	
});