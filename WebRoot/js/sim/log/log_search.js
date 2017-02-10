$(function(){
	//默认表单ID，如果存在则为查询按钮绑定事件
	var defaultFormId = $(".log-query ul li:first a").attr('href');
	if(!!defaultFormId)
		$('#log_search_submit').click(defaultFormId+="_form", doSearch);
		
	
	
  	
	
	var groupid="#tab4";
	//为所有标签页增加点击事件，用于改变查询按钮的点击事件
	$(".log-query ul li").bind('click',function(event){
		var target = event.target;
		var formid = $(target).attr('href')+"_form";
		groupid=$(target).attr('href');
		$('#log_search_submit').unbind( "click" ).click(formid, doSearch);
	});
	
	//点击取消按钮收回查询面板
	$('#log_search_cancel').click(function(){
		$("#log_query_dialog").trigger('onCancelSearch');
	});
	
	//为选项【其他】增加change事件，当其被选中后，启用后边的输入框，否则禁用。
	$("input[type='checkbox'][value='customized']").change(
		function () {
	    	if($(this).attr('checked')){
	    		$(this).next(':first').removeAttr('disabled');
	    	}else{
	    		$(this).next(':first').attr('disabled',true);
	    	}
	  	}
	);
	//查询条件，日志时间onchange事件
	$('select[name="operator_START_TIME"]').change(function(){
		var selectValue=$(this).children('option:selected').val();
		if(selectValue=="between"){
            $(groupid+"_endtime").show();
            $(groupid+"_endtime").val("");
            $(groupid+"_totime").show();
	    }else{
	    	$(groupid+"_endtime").hide();
	    	$(groupid+"_totime").hide();
	    	$(groupid+"_endtime").val("");
	    }
	}) ;
	//查询条件，日志时间onchange事件
	$('select[name="operator_MSG_ID"]').change(function(){
		var selectValue=$(this).children('option:selected').val();
		if(selectValue=="between"){
            $(groupid+"_msg").show();
            $(groupid+"_tomsg").show();
            $(groupid+"_msg").val("");
	    }else{
	    	$(groupid+"_msg").hide();
	    	$(groupid+"_tomsg").hide();
	    	$(groupid+"_msg").val("");
	    }
	}) ;
	//初始化表单验证规则
	$('.form-horizontal').validator({
		theme : 'simple_right',
		stopOnError : true,
		timely : 2,
		showOk : "",
		rules : simHandler.rules,
		valid : doSubmitForm
	}).data("validator");
	
	/**
	 * 点击查询按钮，提交表单
	 */
	function doSearch(event){
		$('#raw_log_content').empty();
		var formid = event.data;
		$(formid).submit();
	}
	
	/**
	 * 提交表单，执行查询
	 */
	function doSubmitForm(form){
		var fieldNames = "", fieldValues = "", fieldOperators = "", fieldTypes = "", groupId = "";
		var fieldValueMap = {},fieldOperatorMap = {},fieldTypeMap = {};
		//获取表单数据
		var formDataArray = $(form).serializeArray();
		//过滤出所有运算符表单数据
		var operatorArray = jQuery.grep(formDataArray, function (o, i) {
			if(o.name == 'groupId')groupId = o.value;
			return o.name.indexOf("operator_") != -1;
		});
		//遍历运算符数据
		$.each(operatorArray,function(i, o){
			var operatorName = o.name;
			var operatorValue = o.value;
			var fieldName = operatorName.replace("operator_","");
			fieldOperatorMap[fieldName] = operatorValue;
			var expr = "input[name='"+fieldName+"']";
			var fieldValueFormData = $(form).find(expr);
			//过滤表单中所有复选框、文本框、隐藏文本框 并排除掉复选框是[其他]的那个
			$.each($(fieldValueFormData).filter(":checked,:text,:hidden").not(":checkbox[value='customized']"),function(j, target){
				var _val = $(target).val();
				if(fieldName=="START_TIME"){
					    _val=_val.replace(/\s+/g, "");
						_val=_val.replace(/\-/g, "");
						_val=_val.replace(/\:/g,"");
				}
				if(!!_val){
					if(!!fieldValueMap[fieldName]){
						if(operatorValue=="between" && fieldName=="START_TIME"){
							fieldValueMap[fieldName] += "#"+_val;
						}else if(operatorValue=="between" && fieldName=="MSG_ID"){
							fieldValueMap[fieldName] += "#"+_val;
						}else{
						  fieldValueMap[fieldName] += " "+_val;
						}
					}else{
						fieldValueMap[fieldName] = _val;
					}
				}
			});
			//获得所有类型
			expr = "input:hidden[name='"+fieldName+"_type']";
			$.each($(form).find(expr),function(j, t){
				var _val = $(t).val();
				fieldTypeMap[fieldName] = _val;
			});
		});
		for(var name in fieldValueMap){
			fieldNames += ","+name;
			
			fieldValues += ","+fieldValueMap[name].replace(/^\'|\'$/g,"");
			fieldOperators += ","+fieldOperatorMap[name];
			//由于filter条件没有type,默认是string
			if(!!fieldTypeMap[name])
				fieldTypes += ","+fieldTypeMap[name];
			else
				fieldTypes += ",string";
		}
		//删除第一个字符[,]
		if(fieldNames.length > 1)fieldNames = fieldNames.substr(1);
		if(fieldValues.length > 1)fieldValues = fieldValues.substr(1);
		if(fieldOperators.length > 1)fieldOperators = fieldOperators.substr(1);
		if(fieldTypes.length > 1)fieldTypes = fieldTypes.substr(1);
		
		//构造提交查询数据
		var searchParams = {};
		searchParams.pageNo = 1;
		searchParams.conditionName = fieldNames;
		searchParams.operator = fieldOperators;
		searchParams.queryContent = fieldValues;
		searchParams.queryStartDate = $('#begin_time').val();
		searchParams.queryEndDate = $('#end_time').val();
		searchParams.queryType = fieldTypes;
		searchParams.group = groupId;
		searchParams.host = $('#query_host').val();
		searchParams.deviceType = $('#query_deviceType').val();
		searchParams.nodeId = $('#query_nodeId').val();
		$("#log_query_dialog").trigger('onSubmitSearch',searchParams);
		createLogTableStatus=false;
		$("#log_query_dialog").trigger('onCancelSearch');
	}
});