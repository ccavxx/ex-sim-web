$(function(){
	//初始化表单数据
	var formdata = {responseId:'', sendlog:{}};

	// 初始化验证表单
	var sysconfig_syslogforward_form_validation = null;
	function initFormValidator(){
		sysconfig_syslogforward_form_validation = $('#sysconfig_syslogforward_form').validator({
		    theme : 'simple_right',
			stopOnError : true,
			timely : 2,
			rules : simHandler.rules,
			showOk : "",
		    valid: function(form) {
		    	var formdataarray = $(form).serializeArray();
		    	formdata.sendlog = {};
		    	$.map(formdataarray,function(data){
		    		var val = data.value;
		    		formdata.sendlog[data.name] = val;
		    	});
		        $.ajax({
		            url: '/sim/systemConfig/modirySyslogForwardConfig',
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
	// 开启表单验证
	function setFieldForOn(){
		sysconfig_syslogforward_form_validation.setField({
	        receivers : "required;ipv4List",
			port : "required;range[0~65535];integer",
			frequency : "required;range[1~1000];integer[+]",
			filterSql : "required",
			spliter:"length[0~5]",
			prefix:"length[0~15]"			
		});
	}
	// 关闭部分表单验证
	function setFieldForOff(){
		sysconfig_syslogforward_form_validation.setField({
	        receivers : "ipv4List",
			port : "range[0~65535];integer",
			frequency : "range[1~1000];integer[+]",
			filterSql : "required"
		});
	}
	// 初始化表单数据
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey:'sys_cfg_sendlog',nodeType:'Auditor'},function(res){
		if(res && res.status){
			initFormValidator();
			var data = res.result;
			formdata.responseId = data.responseId;
			if(data.sendlog.send=='true') {
				setFieldForOn() ;
			} else {
				setFieldForOff() ;
			}
			$('#sysconfig_syslogforward_form').form('load',data.sendlog);
		}
	});
	// 为状态字段绑定事件
	$("#sysconfig_syslogforward_form [name=send]:checkbox").change(function() {
		if(this.checked) {
			setFieldForOn();
		} else {
			$("#sysconfig_syslogforward_form").validator("cleanUp");
			setFieldForOff();
		}
	});
	// 过滤器-编辑按钮事件
	$('#sysconfig_syslogforward_form_filter_edit_btn').bind('click',function(){
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
		return false;
	});
	// 过滤器-默认按钮事件
	$('#sysconfig_syslogforward_form_filter_default_btn').bind('click',function(){
		$("textarea[name='filterSql']").val('SELECTOR(TRUE)');
	});
});