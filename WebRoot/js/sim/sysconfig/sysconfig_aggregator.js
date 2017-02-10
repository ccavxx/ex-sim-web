var simSysConfaggregator = {};

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
	simSysConfaggregator.onBeforeSelect = function(node){
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
	simSysConfaggregator.onClick = function(node){
		var isleaf = $(this).tree('isLeaf', node.target);
		if(isleaf){
			$('#sysconfig_aggregator_logsourcetype').combo('setText',node.text);
			$('#sysconfig_aggregator_logsourcetype').combo('setValue',node.value);
			$("input[name='securityObjectType']").val(node.id).blur();
			var tempUrl = "/sim/AggregatorRule/getFieldAliasSelect?deviceType="+node.id + "&_time="+new Date().getTime();
			$.getJSON( tempUrl, function(groupFieldsJson){
				if(groupFieldsJson){
					$("#aggregatorDevicecolumnset").html("");
					var firstFlag = true;
					var tempCheckbox = '';
					$.each(groupFieldsJson,function(key,val){
						if(firstFlag){
							tempCheckbox += '<label class="checkbox inline" style="margin-left:10px"><input value="' + key + '" name="columnSet" type="checkbox"> ' + val + '</label>';
							firstFlag = false;
						}else{
							tempCheckbox += '<label class="checkbox inline" ><input value="' + key + '" name="columnSet" type="checkbox"> ' + val + '</label>';
						}
					});
					$("#aggregatorDevicecolumnset").append(tempCheckbox);
					$("#aggregatorDevicecolumnsetOutDiv").css("display","block");
				}
			});
		}
	}
	simSysConfaggregator.onLoadSuccess = function(node, data){
		//如果是修改页面
		if(simSysConfAggregatorRule.editRule){
			var rule = simSysConfAggregatorRule.editRule;
			$("input[name='id']").val(rule.id);
			$("input[name='name']").val(rule.name);
			$("input[name='module']").val(rule.module);
			$("input[name='available'][value='"+rule.available+"']").attr('checked',true);
			$('#sysconfig_aggregator_logsourcetype').combo('setText',rule.deviceTypeName);
			$("input[name='securityObjectType']").val(rule.deviceType);
			$("#maxCountId").val(rule.maxCount);
			$("#timeOutId").val(rule.timeOut);
			$("#timesId").val(rule.times);
			$("input[name='base']").val(1);
			$("textarea[name='filterSql']").val(rule.condition);
//赋值 checkboxs
			var columnSets = rule.columnSet.split(",");
			var tempUrl = "/sim/AggregatorRule/getFieldAliasSelect?deviceType="+rule.deviceType + "&_time="+new Date().getTime();
			$.getJSON( tempUrl, function(groupFieldsJson){
				if(groupFieldsJson){
					$("#aggregatorDevicecolumnset").html("");
					var firstFlag = true;
					var tempCheckbox = '';
					$.each(groupFieldsJson,function(key,val){
						var checkFlag = "";
						if($.inArray(key, columnSets)>=0){
							checkFlag = 'checked="true"';
						}
						if(firstFlag){
							tempCheckbox += '<label class="checkbox inline" style="margin-left:10px"><input value="' + key + '" ' + checkFlag + ' name="columnSet" type="checkbox"> ' + val + '</label>';
							firstFlag = false;
						}else{
							tempCheckbox += '<label class="checkbox inline" ><input value="' + key + '" ' + checkFlag + ' name="columnSet" type="checkbox"> ' + val + '</label>';
						}
					});
					$("#aggregatorDevicecolumnset").append(tempCheckbox);
					$("#aggregatorDevicecolumnsetOutDiv").css("display","block");
				}
			});
		} else {
			$("input[name='available']").attr('checked',true);
		}
	}

	//关闭此窗口
	simSysConfaggregator.closeAggregatorEditorDialog = function(){
		$("#sysconf_aggregatorRule_editor_dialog").dialog("close");
		return false;
	}
	//点击取消按钮事件
	$("#sysconfig_aggregator_form_cancel_button").bind('click',simSysConfaggregator.closeAggregatorEditorDialog);

	//归并规则-默认按钮事件
	$('#sysconfig_aggregator_form_default_btn').bind('click',function(){
		$("textarea[name='filterSql']").val('SELECTOR(TRUE)');
	});
	//归并规则-编辑按钮事件
	$('#sysconfig_aggregator_form_edit_btn').bind('click',function(){
		var deviceType = $("input[name='securityObjectType']").val();
		if(!deviceType){
			showAlertMessage("请选择日志源类型！") ;
			return false;
		}
		var sql = $("textarea[name='filterSql']").val();
		$('<div/>').dialog({   
			id: 'sysconfig_filter_dialog',
		    title: '归并规则条件编辑窗口', 
		    iconCls: 'icon-filter',
		    width: 850,
		    height: 600,
		    modal:true,
		    cache: false,
		    href: '/sim/LogFilterRule/getFilterFieldById?id='+deviceType + "&filterSql="+encodeURIComponent(sql),   
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
	var sysconfig_aggregator_form_validation = $('#sysconfig_aggregator_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			name:'required;length[1~30];remote[/sim/AggregatorRule/validateAggregatorRule, id];',
			securityObjectType:'required',
		 /* maxCount:'required;range[1~500];integer[+]', */
			timeOut:'required;range[10~100];integer[+]',
			times:'required;range[2~100];integer[+]',
			filterSql:'required',
			columnSet:"checked[1~10]"
		},
		valid : function(form){
			var formdataarray = $(form).serialize();
			$.ajax({
				url: '/sim/AggregatorRule/saveOrUpdateAggregatorScene',
				type: 'POST',
				data: formdataarray,
				dataType:'json',
				success: function(res) {
					if(res && res.status) {
						simSysConfaggregator.closeAggregatorEditorDialog();
					} else {
						showErrorMessage(res.message);
					}
				}
			});
		}
	}).data( "validator" );
});
