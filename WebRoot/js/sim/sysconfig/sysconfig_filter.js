$(function(){
	//验证规则
	var rule_between = /^\w+,\w+$/;
	var rule_in = /^\w+(,\w+)*$/;
	var rule_is = /^NULL$/;
	var rule_between_ip = /^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))),((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/;
	var rule_in_ip = /^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))(,((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))))*$/;
	var rule_ip = /^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/;
	//点击[AND]按钮
	$('#sysconfig_filter_btn_and').click(function(){
		$('#sysconf_filter_text').selection('insert', {text: ' AND ( ', mode: 'before'});
		$('#sysconf_filter_text').focus();
		$('#sysconf_filter_text').selection('insert', {text: ' )', mode: 'after'});
	});
	//点击[OR]按钮
	$('#sysconfig_filter_btn_or').click(function(){
		$('#sysconf_filter_text').selection('insert', {text: ' OR ( ', mode: 'before'});
		$('#sysconf_filter_text').focus();
		$('#sysconf_filter_text').selection('insert', {text: ' )', mode: 'after'});
	});
	//点击[NOT]按钮
	$('#sysconfig_filter_btn_not').click(function(){
		$('#sysconf_filter_text').selection('insert', {text: ' NOT ( ', mode: 'before'});
		$('#sysconf_filter_text').focus();
		$('#sysconf_filter_text').selection('insert', {text: ' )', mode: 'after'});
	});
	//点击[清除]按钮
	$('#sysconfig_filter_btn_clear').click(function(){
		var selText = $('#sysconf_filter_text').selection();
		if(selText)
			$('#sysconf_filter_text').selection('replace', {text: ''});
		else
			$('#sysconf_filter_text').val('');
		$('#sysconf_filter_text').focus();
	});
	//点击[插入]按钮
	$('#sysconfig_filter_btn_insert').click(function(){
		$('#sysconfig_filter_form').submit();
	});	
	//点击[验证]按钮
	$('#sysconfig_filter_btn_validation').click(function(){
				
	});	
	//点击[完成]按钮
	$('#sysconfig_filter_btn_finish').click(function(){
		var sql =  $('#sysconf_filter_text').val();
		if(sql != "" && !/^\s+$/.test(sql))
			$("textarea[name='filterSql']").val(sql)
		//关闭过滤器编辑窗口
		$('#sysconfig_filter_dialog').panel('close');
	});	
	
	//验证表单实例
	var sysconfig_filter_form_validation = $('#sysconfig_filter_form').validator({ 
	    theme: 'simple_right',
	    stopOnError:true,
	    timely: 2,
	    showOk: ""
	}).data( "validator" );
	//提交表单事件
	$('#sysconfig_filter_form').submit(function(){
    	//表单对象数组[{name:'',value:''}]
    	var formdataarray = $(this).serializeArray();
    	//所有表达式数组
    	var _tmpexp = jQuery.grep(formdataarray,function(o,i){
    		return o.name.indexOf("_exp") != -1;
    	});
    	//属性值数组，过滤掉没有值的属性
    	var _tmpval = jQuery.grep(formdataarray,function(o,i){
    		return o.name.indexOf("_exp") == -1 && o.value != "";
    	});
    	//将有值的表达式添加到属性值对象中[{name:'',value:'',expression:''}]
    	for(var i in _tmpval){
    		var expname = _tmpval[i].name + "_exp";
    		for(var j in _tmpexp){
    			if(expname == _tmpexp[j].name){
    				_tmpval[i].expression = _tmpexp[j].value;
    				break;
    			}
    		}
    	}
    	//表单验证是否通过
    	var isValid = true;
    	//自定义验证表单
		for(var i in _tmpval){
			var obj = _tmpval[i];
		  	var element = "#"+obj.name;
		  	switch(obj.name){
		  		case 'DVC_ADDRESS':
		  		case 'SRC_ADDRESS':
		  		case 'DEST_ADDRESS':
		  			switch(obj.expression){
		  				case 'BETWEEN':
		  					isValid = rule_between_ip.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				case 'IN':
		  					isValid = rule_in_ip.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				case 'IS':
		  					isValid = rule_is.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				default:
		  					isValid = rule_ip.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  			}
		  		break;
		  		default:
		  			switch(obj.expression){
		  				case 'BETWEEN':
		  					isValid = rule_between.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				case 'IN':
		  					isValid = rule_in.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				case 'IS':
		  					isValid = rule_is.test(obj.value) ? sysconf_filter_showok(element) : sysconf_filter_showerror(element);
		  				break;
		  				default:
		  					isValid = sysconf_filter_showok(element);
		  				break;
		  			}		  		
		  		break;
		  	}
	    	//如果表单验证未通过则不继续执行
			if(!isValid)return;
		}
    	
    	//遍历数组并组装SQL语句
    	var sql = "";
    	for(var i in _tmpval){
    		if (i != 0)sql += " AND ";
    		var obj = _tmpval[i];
    		switch(obj.expression){
    			case 'BETWEEN':
    				var arr = obj.value.split(",");
    				if (obj.name == "PRIORITY" || obj.name == "SRC_PORT" || obj.name == "DEST_PORT") {
    					//不加单引号
    					sql += obj.name + " " + obj.expression + " " + arr[0] + " AND " + arr[1];
    				}else{
    					//加单引号
    					sql += obj.name + " " + obj.expression + " '" + arr[0] + "' AND '" + arr[1] + "'";
    				}
    			break;
    			case 'IN':
    				var arr = obj.value.split(",");
    				if (obj.name == "PRIORITY" || obj.name == "SRC_PORT" || obj.name == "DEST_PORT") {
    					//不加单引号
    					sql += obj.name + " " + obj.expression + " (";
						for (var j in arr) {
							if (j == 0)
								sql += arr[j];
							else
								sql += "," + arr[j];
						}
						sql += ")";
    				}else{
    					//加单引号
    					sql += obj.name + " " + obj.expression + " (";
						for (var j in arr) {
							if (j == 0)
								sql += "'" + arr[j] + "'";
							else
								sql += ",'" + arr[j] + "'";
						}
						sql += ")";    					
    				}
    			break;
    			case 'IS':
    				sql += obj.name + " " + obj.expression + " " + obj.value;
    			break;
    			default:
    				if (obj.name == "PRIORITY" || obj.name == "SRC_PORT" || obj.name == "DEST_PORT") {
    					sql += obj.name + " " + obj.expression + " " + obj.value;
    				}else{
    					sql += obj.name + " " + obj.expression + " '" + obj.value + "'";
    				}
    			break;
    		}
    	}
    	sql += " ";
    	//如果SQL语句不为空，则将其插入到文本域
    	if(!/^\s+$/.test(sql))
    		$('#sysconf_filter_text').selection('insert', {text: sql, mode: 'before'});
    	//清除所有条件
    	$('#sysconfig_filter_form :input').not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    	//清除所有验证消息
    	sysconfig_filter_form_validation.cleanUp();
	});
	//显示验证通过
	var sysconf_filter_showok = function(element){
		sysconfig_filter_form_validation.showMsg(element,{type: "ok",msg: " "});
		return true;
	} 
	//显示验证失败
	var sysconf_filter_showerror = function(element){
		sysconfig_filter_form_validation.showMsg(element,{type: "error",msg: "条件输入有误"});
		return false;
	} 
	
});