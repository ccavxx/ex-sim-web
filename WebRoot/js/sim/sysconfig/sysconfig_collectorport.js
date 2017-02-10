$(function(){
	/**
	 * 初始化表单数据
	 */
	var sys_cfg_port_id;
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey:'sys_cfg_port',nodeType:'Collector'},function(res){
		if(res && res.status){
			sys_cfg_port_id = res.result.responseId;
			for(var block_key in res.result){
				if(typeof res.result[block_key] == 'object')
				for(var item_key in res.result[block_key]){
					var select = $("select[name='" + item_key + "_list']");
					select.children().remove();
					var item_values = res.result[block_key][item_key];
					for(var i in item_values){
						var opt = $('<option/>').val(item_values[i]).text(item_values[i]);
						select.append(opt);
					}
				}
			}
		}
	});
	
	/**
	 * 创建表单验证实例，并初始化参数
	 */
	var sysconfig_collectorport_form_validation = $('#sysconfig_collectorport_form').validator({
	    theme: 'simple_right',
	    stopOnError:true,
	    timely: 2,
	    rules: simHandler.rules,
	    fields: {
	    	syslog_port:"integer;range[0~65535]",
	    	snmp_port:"integer;range[0~65535]",
	    	netflow_port:"integer;range[0~65535]"
	    }
	}).data( "validator" );
	
	/**
	 * 表单中所有添加端口按钮点击事件
	 */
	$('button.addport').bind('click', function(event) {
		// 找到输入框
		var portinput = $(this).parent().find('input');
		// 输入框名称
		var portinputname = portinput.attr('name');
		// 输入框值
		var portinputvalue = portinput.val();
		// 输入框验证是否成功
		var isvalid = $("input[name='" + portinputname + "']").isValid();
		if (!isvalid)
			return;
		// 判断输入框是否是数字
		if (!/^\d+$/.test(portinputvalue))
			return;
		//判断输入数字是否和列表里的端口重复
		var options = $("option");
		var isRepeat = false; 
		if(options && options.length > 0){
			for(var i = 0, len = options.length; i < len; i++){
				if(portinputvalue == options[i].value){
					showAlertMessage('和列表端口重复');
					isRepeat = true;
				}
			}
		}
		if(isRepeat)
			return;
		// 清空输入框
		portinput.val('');
		var selectelement = $("select[name='" + portinputname + "_list']");
	 	if(selectelement[0].length>29){
	 		sysconfig_collectorport_form_validation.showMsg(selectelement[0],{type: "error",msg: "至多存在30个选项"});
	 		return;
	 	}else{
	 		sysconfig_collectorport_form_validation.showMsg(selectelement[0],{type: "ok",msg: " "});
	 	}
		var opt = $('<option/>').val(portinputvalue).text(portinputvalue);
		// 将输入框的值加入到下拉框中
		selectelement.append(opt);

	});
	
	/**
	 * 表单中所有删除端口按钮点击事件
	 */
	$('button.removeport').bind('click',function(event){
		// 找到下拉框
		var select = $(this).parent().find('select');
		// 找到选择的下拉选项并删除
		select.find('option:selected').remove();
	});
	
	/**
	 * 表单提供事件
	 */
	$("#sysconfig_collectorport_form").submit(function(){
		// 定义表单数据格式
		var formdata = {
			responseId : sys_cfg_port_id,
			syslog : {
				syslog_port : []
			},
			snmp : {
				snmp_port : []
			},
			netflow : {
				netflow_port : []
			}
		};
		for(var block_key in formdata){
			if(typeof formdata[block_key] == 'object'){
				for(var item_key in formdata[block_key]){
				 	var selectelement = $("select[name='"+item_key+"_list']");
				 	//验证每个下拉框选项
					var isFieldValid = sysconfig_collectorport_form_validation.test(selectelement[0],"seloptsgte1");
					if(!isFieldValid){
						// 验证未通过的提示错误信息
						sysconfig_collectorport_form_validation.showMsg(selectelement[0],{type: "error",msg: "至少存在一个选项"});
						return;
					}else{
						// 验证通过显示OK
						sysconfig_collectorport_form_validation.showMsg(selectelement[0],{type: "ok",msg: " "});
						// 验证通过的将下拉列表选项添加到数组中
						var options = selectelement.children();
						$.each(options,function(index,option){
							formdata[block_key][item_key].push($(option).val());
						});
					}					
				}
			}

		}
		// 提交表单
        $.ajax({
            url: '/sim/systemConfig/modifyCollectorPortConfig',
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
	});
});