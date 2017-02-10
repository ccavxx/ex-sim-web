var groupCollection;
var logFields ;
var topNumberData=[{ label: '5', value: '5' },{ label: '10', value: '10' },{ label: '20', value: '20' },{label:'30',value:'30'},{ label: '50', value: '50' }];
var topNumberTableData = [{ label: '50', value: '50' },{ label: '100', value: '100' },{ label: '500', value: '500' },{label:'1000',value:'1000'},{ label: '10000', value: '10000' }];
$("#topNumber").combobox({
	valueField: 'value',
	textField: 'label',
	editable:false,
	data:topNumberData
});
(function(){
	var logsource_host;
	var logsource_name;
	var startTime;
	var endtime;
	var deviceTypeId ;
	var groupId ;
	var conditionNames ;
	var operators ;
	var queryContent ;
	var queryType ;
	var search_condition = "" ;
	var taskName = "" ;
	var diagram = "";
	var top = "";
	var interval = "";
	var axisField = "";
	var groupTopFields = "";
	$("#taskOperator").val(simHandler.log_stat.operation) ;
	if(simHandler.log_stat.operation == "add"){
		$("#subject_back_btn").click(function(){
			simHandler.log_statistics_dialog.dialog('collapse',true);
		});
		logsource_host=simHandler.log_stat.dataSourceIp;
		logsource_name = simHandler.log_stat.dataSourceName;
		startTime = simHandler.log_stat.startTime;
		endtime =simHandler.log_stat.endTime;
		deviceTypeId = simHandler.log_stat.deviceType;
		groupId = simHandler.log_stat.groupId;
		conditionNames = simHandler.log_stat.conditionNames ;
		operators = simHandler.log_stat.operators ;
		queryContent = simHandler.log_stat.queryContent ;
		queryType = simHandler.log_stat.queryType ;
		top = topNumberData[0].value;
		var textArr=simHandler.log_stat.queryCondition.split("××");
		for(var k=1;k<textArr.length;k++){
			var newArr=textArr[k].split("×");
			for(var n=0;n<newArr.length;n++){
				search_condition += "【"+newArr[n]+"】";
				if(n<newArr.length-1){
					search_condition+="&nbsp;&nbsp;";
				}
			}
		}
	}else{
		$("#subject_back_btn").click(function(){
			simHandler.log_edit_statistics_dialog.dialog('collapse',true);
		});
		var editTask = simHandler.log_stat.editTask ;
		var searchObject = editTask.searchObject ;
		taskName = editTask.taskName ;
		startTime = editTask.queryStartTime;
		endtime = editTask.queryEndTime;
		search_condition = editTask.searchCondition ;
		diagram = editTask.diagram;
		axisField = editTask.axisField;
		groupTopFields = editTask.groupTopFields;
		interval = searchObject.interval.toLowerCase();
		deviceTypeId = searchObject.type ;
		groupId = searchObject.group ;
		logsource_host = searchObject.host;
		if(!editTask.host){
			logsource_name = "全部";
		}else{
			logsource_name = editTask.host;
		}
		
		conditionNames = searchObject.conditionNames ;
		operators = searchObject.operators ;
		queryContent = searchObject.queryContents ;
		queryType = searchObject.queryTypes ;
		top = searchObject.top;
		selectOptionInterval(searchObject.interval.toLowerCase());
		
		$("#taskId").val(editTask.taskId) ;
		judgeDiagram(diagram);
		$("input[type=radio][value = '" + diagram + "']").attr("checked","checked");
		
	}
	if(top){
		$("#topNumber").combobox("setValue",top);
	}else{
		$("#topNumber").combobox("setValue",5);
	}
	$("#taskName").val(taskName);
	$("#deviceTypeId").val(deviceTypeId);
	$("#groupId").val(groupId);
	$("#hostId").val(logsource_host);
	$("#conditionName").val(conditionNames);
	$("#operator").val(operators);
	$("#queryContent").val(queryContent);
	$("#queryType").val(queryType);
	$('#startDate').val(startTime);
	$('#endDate').val(endtime);
	
	$.ajax({
		url:"/sim/logReport/getLogInterval",
		dataType:'json',
		success:function(intervalData){
			for(var i in intervalData){
				var value = intervalData[i].value;
				var label = intervalData[i].label;
				var opt = $("<option/>").val(value).text(label);
				if(simHandler.log_stat.operation == "add"){
					if( value == "user_define"){
						opt.attr("selected","selected") ;
					}
				}else{
					if( value == interval){
						opt.attr("selected","selected") ;
					}
				}
				$('#intervalId').append(opt);
			}
		}
    });
	
	
	
	if(!logsource_host){
		logsource_host="全部";
	}
	var content = "<div>设备："+logsource_name+"&nbsp;&nbsp;&nbsp;&nbsp;</div>";
	if(search_condition != ""){
		$("#searchCondition").val(search_condition);
		content+="<div>过滤条件："+search_condition+"</div>";
		
	}
	$("#queryCondition").html(content);
 	$.ajax({
			url:"/sim/logReport/getTableHaderProperty?type=" + deviceTypeId + "&group=" + groupId,
			dataType:'json',
			success:function(data){
				groupCollection = data;
				logFields = groupCollection["visibleFields"] ;
				if(simHandler.log_stat.operation == "update"){
					var searchObject = simHandler.log_stat.editTask.searchObject ;
					initGroupColumns(searchObject) ;
					initFunctionColumns(searchObject) ;
					initOrderColumns(searchObject) ;
					$("#categoryAxisField").combobox("setValue",axisField);
					$("#groupTopFields").combobox("setValue",groupTopFields);
					//清空当前编辑的editTask
					simHandler.log_stat = null ;
				}
			}
	});
	 
})();

function initGroupColumns(searchObject){
	var groupColumns = searchObject.groupColumns ; 
	if(groupColumns){
		var groupLabelItems = new Array();
		var groupValueItems = new Array() ;
		var groupFunctions = searchObject.groupFunctions ;
		var groupTopFields = [{text:"全部",value:""}] ;
		var categoryAxisField = new Array() ;
		for(var groupIndex in groupColumns){
			var groupFieldName = groupColumns[groupIndex] ;
			var groupField = getRowByFieldName(logFields, groupFieldName) ;
			var groupFunctionValue = groupFunctions[groupIndex] ; 
			var groupFunctionName = "";
			if(groupFunctionValue != "default"){
				//从分组字段的分组函数列表中查找同名的函数
				$.each(groupField.statMethods,function(index,option){
					if(option.value == groupFunctionValue){
						groupFunctionName = "[分组方式:"+option.label+"]" ;
						return false ;
					}
				}) ;
			}
			groupTopFields.push({text:groupField.alias,value:groupField.name}) ;
			categoryAxisField.push({text:groupField.alias,value:groupField.name}) ;
			groupLabelItems.push(groupField.alias + groupFunctionName) ;
			groupValueItems.push(groupField.name + ":" +groupFunctionValue) ;
		}
		$("#groupvalue").val(groupValueItems.join()) ;
		$("#groupid").html(groupLabelItems.join("<br/>")) ;
		$("#groupTopFields").combobox("loadData",groupTopFields) ;
		$("#categoryAxisField").combobox("loadData",categoryAxisField) ;
	}
}
function initFunctionColumns(searchObject){
	var functionName = searchObject.functionName ; 
	var functionField = searchObject.functionField ;
	if(functionName && functionField){
		$("#functionName").val(functionField+":"+functionName);
		$("#statistics_functionName").html(getFunctionDesc(functionName, functionField));
	}
}
function initOrderColumns(searchObject){
	var orderColumns = searchObject.orderColumns ; 
	if(orderColumns){
		var orderLabelItems = new Array();
		var orderValueItems = new Array() ;
		for(var orderIndex in orderColumns){
			var orderFieldName = orderColumns[orderIndex] ;
			var orderField = getRowByFieldName(logFields, orderFieldName) ;
			if(orderFieldName == "${result}"){
				orderLabelItems.push("统计结果") ;
			}else{
				orderLabelItems.push(orderField.alias) ;
			}
			orderValueItems.push(orderFieldName) ;
		}
		$("#orderbyColumm").val(orderValueItems.join()) ;
		$("#orderby_id").html(orderLabelItems.join("<br/>")) ;
	}
}
function getRowIndexByFieldName(data,id){
	var rowIndex = -1 ;
	$.each(data,function(index,field){
		if(field.name == id){
			rowIndex = index ;
			return false ;//停止循环
		}
	}) ;
	return rowIndex ;
}
function getRowByFieldName(data,id){
	var row = {} ;
	$.each(data,function(index,field){
		if(field.name == id){
			row = field ;
			return false ;//停止循环
		}
	}) ;
	return row ;
}
//选择分组，弹出属性框dialog
function showGroupDialog(){
	initCheckProperty();
	$("#groupDialog").dialog('open').dialog('setTitle','选择分组属性');
	
}
//分组，获取勾选的分组属性，展现在页面
function check_group() {
	var checkRows = $("#group_table").datagrid("getChecked") ;
	var id = "";
	var str = "" ;
	var groupTopFields = [{text:"全部",value:""}] ;
	var categoryAxisField = new Array() ;
	$.each(checkRows,function(index,row){
		var rowId = row.name ;
		var selectOption = $("#" + rowId.replace(":","\:") + "statMethod option:selected");//选中的分组方式
		id += row.name;
		str += row.alias ;
		if(selectOption.length > 0){
			id  +=  ":"+selectOption.val() ;
			str += "[分组方式：" + selectOption.html() + "]";
		}else{
			id  +=  ":default";
		}
		groupTopFields.push({text:row.alias,value:row.name}) ;
		categoryAxisField.push({text:row.alias,value:row.name}) ;
		if(index < checkRows.length-1){
			str += "<br/>";
			id += ",";
		}
	});
    $("#groupid").html(str);
    $("#groupvalue").val(id);
    $("#groupTopFields").combobox("loadData",groupTopFields) ;
    $("#categoryAxisField").combobox({data:categoryAxisField,valueField:'value',   
        textField:'text'  
}) ;
    $("#orderby_id").html("");
	$("#orderbyColumm").val("");
    $('#groupDialog').dialog('close');
}
//选择统计方式，弹出属性框dialog
function showStatistics(){
	 init_functionName();
	$("#statisticsDialog").dialog('open').dialog('setTitle','选择统计方式');
}
//统计方法，获取勾选的统计方法和字段，展现在页面
function check_functionName(){
	 var radioVal=$("input[name^='functionname']:checked").val();
	 if(!radioVal){
		 return false;
	 }
	 var radioValArr = radioVal.split(":");
	 var staticfuntion = radioValArr[1];
	 var str = getFunctionDesc(staticfuntion, radioValArr[0]);
	 $("#functionName").val(radioVal);
	 $("#statistics_functionName").html(str);
		
	 $("#orderby_id").html("");
	 $("#orderbyColumm").val("");
		
	 $('#statisticsDialog').dialog('close');
}
function getFunctionDesc(staticfuntion,functionField){
	var fieldRow = getRowByFieldName(logFields, functionField) ;
	var str = "";
	if(staticfuntion == "count"){
	    str = "统计记录(" + fieldRow.alias + ")";
	 }else if(staticfuntion == "sum"){
		str = "求和(" + fieldRow.alias + ")";
	 }else if(staticfuntion == "avg"){
		str = "求平均(" + fieldRow.alias + ")";
	 }
	return str ;
}
//选择排序字段，弹出属性框dialog
function showOrderbyDialog(){
	init_orderby();
	$("#orderbyDialog").dialog('open').dialog('setTitle','选择排序属性');
}
//排序，获取勾选的排序字段，展现在页面
function check_orderby(){
	var checkRows = $("#orderby_table").datagrid("getChecked") ;
	var id = "";
	var str = "" ;
	$.each(checkRows,function(index,row){
		id += row.name;
		str += row.alias ;
		if(index < checkRows.length-1){
			str += "<br/>";
			id += ",";
		}
	});
    $("#orderby_id").html(str);
    $("#orderbyColumm").val(id);
    $('#orderbyDialog').dialog('close');
}
//用于分组初始化加载属性信息
function initCheckProperty(){
	var rowdata = logFields;
	$('#group_table').datagrid({
		 height:280,
		 width:400,
		 columns: [[
			{field:'name',checkbox:true},
			{field:'alias',title:'属性名称',width:100},
			{field:'type',title:'数据类型',width:50},
			{field:'statMethod',title:'分组方式',width:150,formatter:function(value,row,index){
				var optionVal = row.statMethods;
				if(!optionVal || optionVal.length == 0){
					return "" ;
				}
				var selectText = "<select id='" + row.name + "statMethod' style='width:140px;height:25px;font-size:12px;margin-bottom:0px'>";
				for(var i = 0;i<optionVal.length;i++){
					var option = optionVal[i];
					selectText += "<option value='" + option["value"] + "'>" + option["label"] + "</option>";
				}
				selectText += "</select>";
				return selectText;
			}}
		 ]],
		 singleSelect:true,
		 multiple:true,
		 selectOnCheck:false,
		 checkOnSelect:false,
		 fitColumns: true,
		 data:rowdata,
	     onLoadSuccess:function(data){
	    	 $(".datagrid-header-check input[type='checkbox']").attr("disabled","disabled");
	    	var selectId=$("#groupvalue").val().split(",");
	    	if(selectId && selectId.length > 0){
	    		for(var index in selectId){
	    			var fieldAndStatMethod = selectId[index].split(":") ;//[0]字段[1]分组方式，例如:SRC_ADDRESS:default
	    			var field = fieldAndStatMethod[0] ; 
	    			var rowIndex =getRowIndexByFieldName(rowdata, fieldAndStatMethod[0]) ;
	    			$(this).datagrid("checkRow",rowIndex) ;
	    			if(fieldAndStatMethod.length > 0){
	    				$("#"+field+"statMethod").val(fieldAndStatMethod[1]) ;
	    			}
	    		}
	    	}
	    },
	    onCheck:function(index,rowData){
	    	var checkRows = $("#group_table").datagrid("getChecked") ;
	    	if(checkRows.length >2){
	    		$(this).datagrid("uncheckRow",index);
	    		showAlertMessage("柱状图和曲线图最多选择两个字段，饼图只能选择一个字段");
	    		return false;
	    	}
	    }
	});
}
//统计方法初始化属性信息
function init_functionName(){
	    var rowdata=logFields;
		$("#statistics_table").datagrid({
			 height:277,
			 width:427,
			 singleSelect:true,
			 selectOnCheck:false,
			 checkOnSelect:false,			 
			 columns: [[
						{field:'alias',title:'参数名称',width:100},
						{field:'type',title:'数据类型',width:100},
						{field:'name',title:'函数方法',width:200,formatter:function(value,row,index){
								if(row.type != ""){
									if(row.type == "int" || row.type == "long" || row.type == "double"){
										return createFunctionRadio(row.name,"count","统计记录")+"&nbsp;&nbsp;&nbsp;"+
											   createFunctionRadio(row.name,"sum","求和")+"&nbsp;&nbsp;&nbsp;"+
											   createFunctionRadio(row.name,"avg","求平均") ;
									}else if(row.type != "undefined"){
										return createFunctionRadio(row.name,"count","统计记录");
									}
								}
						 	}
						}
					 ]],
			data:rowdata,
			onLoadSuccess:function(data){
		    	var selectId=$("#functionName").val();
		    	if(selectId){
	    			$("#"+selectId.replace(":","\\:")).attr("checked",true) ;
		    	}
		    }
		});
}
function createFunctionRadio(id,fun,funName){
	return "<input type='radio' id='"+id+":"+fun+"' name='functionname' value='"+id+":"+fun+"'>"+funName ;
}
///排序，初始化属性
function init_orderby(){
	var data=$("#groupvalue").val();
	var paramData = new Array();
    if(data.length > 0){
    	var groupData = data.split(",");
    	for(var i = 0 ; i < groupData.length;i++){
    		var field = groupData[i].split(":");
    		var fieldData = getRowByFieldName(logFields, field[0]) ;
			paramData.push({alias:fieldData.alias,name:fieldData.name});
    	}
    }	
    paramData.push({alias:"统计结果",name:"${result}"});
	$("#orderby_table").datagrid({
		 height:276,
		 width:298,
		 columns: [[
			{field:'name',checkbox:true},
			{field:'alias',title:'排序字段',width:100},
			{field:'sort',title:'排序',width:50,formatter:function(value,row,index){
            	return "<span><a class='icon-up' style='cursor: pointer;' title='上移' onclick=\"move(event,this,true)\"></a>&nbsp;&nbsp;<a style='cursor: pointer;' onclick=\"move(event,this,false)\" class='icon-down' title='下移'></a></span>";
            }}]],
	    multiple:true,
	    singleSelect:false,
		selectOnCheck:false,
		checkOnSelect:false,
		data:paramData,
		fitColumns: true,
	    onLoadSuccess:function(data){
	    	var selectId=$("#orderbyColumm").val().split(",");
	    	if(selectId && selectId.length > 0){
	    		for(var index in selectId){
	    			var orderFieldName = selectId[index] ;
    				var rowIndex = getRowIndexByFieldName(paramData,orderFieldName ) ;
    				$(this).datagrid("checkRow",rowIndex) ;
	    		}
	    	}
	    }
	});
}
//判断选择的结果类型
function judgeDiagram(diagramValue){
	$("#groupTopFields").combobox();
	if(diagramValue == "7"){
		$("#groupTopFields").combobox({ disabled: true });
		$("#groupTopFields").combobox("setValue","全部");
		$("#chartxAxis").hide();
		$("#topNumber").combobox({data:topNumberTableData});
		$("#topNumber").combobox("setValue",50);
		
	}else{
		if(diagramValue=="5"){
			$("#chartxAxis").hide() ;
		}else{
			$("#chartxAxis").show() ;
		}
		$("#groupTopFields").combobox("enable");
		$("#topNumber").combobox({data:topNumberData});
		$("#topNumber").combobox("setValue",5);
//		$("#categoryAxisField").show();
	}
	
}
//提交任务
function submitTask(){
	var taskname = $("#taskName").val();
	if(taskname == ""){
		showAlertMessage("主题名称不能为空!");
		return;
	}else if(taskname.length>30){
		showAlertMessage("主题名称长度不能超过30!");
		return;
	}else{
		if(!/^([0-9a-zA-Z_\.\u4e00-\u9fa5-]+)$/g.test(taskname)){
		   showAlertMessage("主题名称只允许汉字、字母、数字、点、下划线和中划线");
		   return;
		}
	}
	var operation = $("#taskOperator").val() ;
	var taskId = $("#taskId").val() ;
	$.ajax({
	 url:"/sim/logReport/isTaskNameExist",
	 type:"post",
	 data:{taskName:taskname,operation:operation,taskId:taskId},
	 dataType:'json',
	 success:function(data){
		 if(data.result){
			 showAlertMessage("主题名称已存在!");
			 return;
		 }else{
			if($("#groupvalue").val()==""){
					$("#groupid").html("请选择分组字段");
					return;
			}
			if($("#functionName").val()==""){
				$("#statistics_functionName").html("请选择函数方法");
				return;
			}
//			var topNumber=$("#topNumber").combobox("getValue");
//			if(topNumber == ""){
//				showAlertMessage("top不能为空，请选择!");
//				return;
//			}
			saveTaskName();
		 }
	 }
	});
}

function saveTaskName(){
	//var selectOptionVal = $("#intervalId").find("option:selected").val();
	var diagram = $("input[name='diagram']:checked").val();
	if(diagram == "5"){
		if($("#groupvalue").val().split(",").length>1){
		  showAlertMessage("结果类型选择饼图时，分组只能选择一个字段。");
		  return false;
		}
	}else{
		if($("#groupvalue").val().split(",").length > 2){
			  showAlertMessage("结果类型柱状图或者曲线图时，分组最多可以选择两个字段。");
			  return false;
			}
		if(diagram != "7"){
			var categoryAxisField = $("#categoryAxisField").combobox('getValue');
			if(categoryAxisField == "" ){
				showAlertMessage("横轴不许为空，请选择!");
				return false;
			}
		}
	}
	 var dataArray = $("#add_taskForm").serializeArray();
	 var formdata = {};
 	 $.map(dataArray,function(data){
 		 if(data.name == "host" || data.name == "groupTopFields"){
 			 if(data.value == "全部"){
  				formdata[data.name] = "";
  			 }else{
  				formdata[data.name] = data.value;
  			 }
  		 }else{
  			formdata[data.name] = data.value;
  		 }
 	});	
	$.ajax({
		url:'/sim/logReport/addTask',
		type: 'POST',
        data: JSON.stringify(formdata),
        dataType:'json',
        contentType:"text/javascript",
		success:function(data){
			if(data.status==true){
				if($("#taskOperator").val() == "update"){
					$('#log_statistics_table').datagrid('reload');
					simHandler.log_edit_statistics_dialog.dialog('collapse',true);
				}else{
					simHandler.log_statistics_dialog.dialog('collapse',true);
				}
			}else{
				showAlertMessage(data.message);
				return false;
			}
		}
	});
}
function selectOptionInterval(optionVal){
	if(optionVal == "user_define"){
		$("#user_define_time").show();
	}else{
		$("#user_define_time").hide();
	}
}
function move(e, target, isUp) {
    var $view = $(target).closest('div.datagrid-view');
    var index = $(target).closest('tr.datagrid-row').attr('datagrid-row-index');
    var $row = $view.find('tr[datagrid-row-index=' + index + ']');
 
    if (isUp) {
        $row.each(function() {
            $(this).prev().before($(this));
        });
    } else {
        $row.each(function() {
            $(this).before($(this).next());
        });
    }
    $row.removeClass('datagrid-row-over');
    e.stopPropagation();
}

