var propertyData="";
var topNumberData=[{ label: '30', value: '30' },{ label: '50', value: '50' },{ label: '100', value: '100' },
                   { label: '200', value: '200' },{ label: '全部', value: '' }];
function selectOptionInterval(optionVal){
	if(optionVal == "USER_DEFINE"){
		$("#edit_user_define_time").show();
	}else{
		$("#edit_user_define_time").hide();
	}
}
(function(){
	//datagrid选择行的数据
	var row=$("#log_statistics_table").datagrid("getSelected");
	 $("#edit_deviceTypeId").val(row.searchobj.type);
	 $("#edit_groupId").val(row.searchobj.group);
	 $("#edit_hostId").val(row.searchobj.host);
	 $("#edit_pageSizeId").val(row.searchobj.page);
	 $("#edit_pageNoId").val(row.searchobj.perPage);
	 $("#edit_taskid").val(row.id);
	 
	 $("#edit_conditionName").val(row.searchobj.conditionNames);
	 $("#edit_operator").val(row.searchobj.operators);
	 $("#edit_queryContent").val(row.searchobj.queryContents);
	 $("#edit_queryType").val(row.searchobj.queryTypes);
	 var interval = row.searchobj.interval;
	 $("#edit_searchCondition").val(row.conditonName);
	 $("#edit_intervalId option[value='"+interval+"']").attr("selected", "selected");
	 if(interval == "USER_DEFINE"){
		$("#edit_user_define_time").show();
		$('#edit_startDate').val(row.start);
		$('#edit_endDate').val(row.end);
	 }else{
		$("#edit_user_define_time").hide();
	 }
	 selectOptionInterval(interval);
	
	 //获取全部属性
	 $.ajax({
			url:"/sim/logReport/getTableHaderProperty?type="+row.searchobj.type+"&group="+row.searchobj.group,
			dataType:'json',
			success:function(data){
				propertyData=data;
			}
		});
	 var host=row.searchobj.host;
	 if(host==""){
		 host="全部";
	 }
	 var content="<div>日志源："+host+"&nbsp;&nbsp;&nbsp;</div>";
	 var queryConditon="";
	 
	 var searchCondition= $("#edit_searchCondition").val().split(",");
	 for(var i=0;i<searchCondition.length;i++){
		 if(searchCondition[i]!=""){
			  queryConditon+="【"+searchCondition[i]+"】";
			  if(i<queryConditon.length-1){
				  queryConditon+="&nbsp;&nbsp;";
			  }
		 }
	 }
	 if(queryConditon!=""){
		 content+="<div>过滤条件："+queryConditon+"</div>";
	 }
	 $("#edit_queryCondition").html(content);
	
	 
	//任务名称
	$("#edit_taskName").val(row.taskname);
	//结果类型
	var diagram=$("input[name='diagram']");
	for(var i=0;i<diagram.size();i++){
		if(diagram[i].value==row.diagram){
			diagram[i].checked="checked";
		}
	}
	if(row.diagram=="5"){
		 $("#edit_topNumber").combobox({
		    	width:175,
		 	    valueField: 'value',
				textField: 'label',
				editable:false,
				data: [{
					label: '30',
					value: '30'
				},{
					label: '50',
					value: '50'
				}]
		    });
	}else{
		 $("#edit_topNumber").combobox({
		    	width:175,
		 	    valueField: 'value',
				textField: 'label',
				editable:true,
				data: topNumberData
		    });
	}
    $("#edit_topNumber").combobox("setValue",row.searchobj.top);
	//分组属性
	var groupname=row.groupname;
	//分组属性的值
	var groupvalue=row.groupvalue;
	var groupname_str="";
	var groupvalue_str="";
	for(var j=0;j<groupname.length;j++){
		groupname_str+=groupname[j];
		groupvalue_str+=groupvalue[j];
		if(j<groupname.length-1){
			groupname_str+="<br/>";
			groupvalue_str+=",";
		}
	}
	$("#edit_groupid").html(groupname_str);
	$("#edit_groupvalue").val(groupvalue_str);
	//全部属性list
	 fieldhead=row.property;
	 fieldtype=row.typelist;
	 //统计方式
	 $("#edit_function_method").html(row.functionname);
	 $("#edit_functionName").val(row.funtionvalue);
	 //排序字段
	 $("#edit_orderbyColumm").val(row.orderbyvalue);
	 
	 var ordercolumn=row.orderbycolumn;
	 var orderbyvalue_str="";
	 for(var k=0;k<ordercolumn.length;k++){
		 orderbyvalue_str+=ordercolumn[k];
			if(k<ordercolumn.length-1){
				orderbyvalue_str+="<br/>";
			}
		}
	 //用于页面展现已选的排序字段
	 $("#edit_orderby_id").html(orderbyvalue_str);
	 
})();
//判断选择的结果类型
function judgeDiagram(diagramValue){
	if(diagramValue=="5"){
		$("#edit_topNumber").combobox({
			width:175,
	 	    valueField: 'label',
			textField: 'value',
			editable:false,
			data: [{
				label: '30',
				value: '30'
			},{
				label: '50',
				value: '50'
			}]
		});
	}else{
		$("#edit_topNumber").combobox({
			width:175,
	 	    valueField: 'label',
			textField: 'value',
			editable:true,
			data:topNumberData
		});
	}
}
//编辑分组
function edit_showGroupDialog(){
	edit_checkProperty();
	$("#edit_groupDialog").dialog('open').dialog('setTitle','选择分组属性');
}

//分组，获取勾选的属性，用于展现在页面
function edit_checkgroup(){
	var checkedArr = $("input[name='groupid']:checked");
	var str = "";
	var id = "";
	for(var i=0;i<checkedArr.size();i++){
		
		var checkValue = checkedArr[i].value.split(":");
		
        var optval = $("#"+checkValue[2] + "function").find("option:selected").val();
		
		var checkVal = checkedArr[i].value + ":" + (optval ? optval : "");
		
		var opttext = $("#" +checkValue[2] + "function").find("option:selected").text(); 
		
		str += checkValue[0];
		
		if(opttext){
			str += "[分组方式：" + opttext + "]";
		}
		 
		id += checkVal;
		
		if(i < checkedArr.size()-1){
			str += "<br/>";
			id += ",";
		}
	}
	$("#edit_groupid").html("");
	$("#edit_groupid").html(str);
	$("#edit_groupvalue").val(id);
	$("#edit_orderby_id").html("");
    $("#edit_orderbyColumm").val("");
	$('#edit_groupDialog').dialog('close');
}
//用于分组初始化属性信息
function edit_checkProperty(){
	var rowdata=edit_initProperty();
	$('#edit_grouptable').datagrid({
		 height:280,
		 width:368,
		 columns: [[
			{field:'groupid',checkbox:true},
			{field:'name',title:'属性名称',width:100,formatter:function(value,row){
				/*if(row["type"]=="Date"){
					return "<span>" + "<span>" + value + "</span>" + 
								"<se>" +
							        "<input name='day' type='radio' value='$DD'/>每日" +
									"<input name='day' type='radio' value='$MM'/>每月" +
									"<input name='day' type='radio' value='$YY'/>每年" +
								"</span>" +
							"</span>"
				}else{
					return value ;
				}*/
				return value ;
			}},
			{field:'type',title:'数据类型',width:40},
			{field:'otherValues',title:'分组方式',width:40,formatter:function(value,row,index){
				var optionVal = row.otherValues;
				if(optionVal != null && optionVal.length == 0){
					return "" ;
				}
				var selectId =  row.groupid.split(":")[2] ;
				var selectText = "<select id='" + selectId + "function' style='width:50px;height:25px;font-size:12px;margin-bottom:0px'>";
				for(var i = 0;i<optionVal.length;i++){
					var option = optionVal[i].split(":") ;
					selectText += "<option value='" + option[0] + "'>" + option[1] + "</option>";
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
	    	var checkid = $("input[name='groupid']");
	    	var selectid = $("#edit_groupvalue").val().split(",");
	    	for(var i = 0 ; i < checkid.size() ; i++){
	    		 for(var j = 0 ; j < selectid.length ; j++){
	    			 var selectValue = selectid[j].split(":").length > 3 ? selectid[j].substring(0,selectid[j].lastIndexOf(":")):selectid[j];
	    			 if(checkid[i].value == selectValue){
	    				  $("input[value='"+ checkid[i].value + "']").attr("checked","checked");
	    				  var optval =  selectid[j].split(":").length > 3 ? selectid[j].substring(selectid[j].lastIndexOf(":") + 1,selectid[j].length) : "";
	    			      var seleid = checkid[i].value.split(":");
	    			      $("#" + seleid[2] + "function option[value=" + optval + "]").attr("selected","selected");
	    			 }
	    		 }
	    	 }
	    	 $("input[name='groupid']:checked").length == $("input[name='groupid']").length ? $("#edit_grouptable").parent().find("div.datagrid-header-check").children("input[type='checkbox']").eq(0).attr("checked",true):$("#edit_grouptable").parent().find("div.datagrid-header-check").children("input[type='checkbox']").eq(0).attr("checked",false);
	    },
	    onUncheckAll:function(rows){
	    	var checkid=$("input[name='groupid']");
	    	for(var i=0;i<checkid.length;i++){
	   		    if(checkid[i].checked){
	   		    	checkid[i].checked="";
	   		    }
	   	    }
			
	    }
	});
}
//编辑统计方式
function edit_showStatistics(){
	 edit_init_functionName();
     $("#edit_statisticsDialog").dialog('open').dialog('setTitle','选择统计方式');
}
//根据函数方法，筛选属性参数
function edit_init_functionName(){
		var dataRow=edit_initProperty();
		$("#edit_statisticstable").datagrid({
			 height:277,
			 width:427,
			 singleSelect:true,
			 columns: [[
						{field:'name',title:'参数名称',width:100},
						{field:'type',title:'参数数据类型',width:100},
						{field:'groupid',title:'函数方法',width:200,formatter:function(value,row,index){
								if(row.type!=""){
									if(row.type=="int" || row.type=="long"){
										return "<input type=\"radio\" id=\""+row.groupid+":count\" name=\"functionname\"  value=\""+row.groupid+":count\">统计记录&nbsp;&nbsp;&nbsp;<input type=\"radio\" id=\""+row.groupid+":sum\" name=\"functionname\" value=\""+row.groupid+":sum\">求和&nbsp;&nbsp;&nbsp;<input type=\"radio\" id=\""+row.groupid+":avg\" name=\"functionname\" value=\""+row.groupid+":avg\">求平均";
									}else{
										if(row.type != "undefined"){
											return "<input type=\"radio\" id=\""+row.groupid+":count\"  name=\"functionname\" value=\""+row.groupid+":count\">统计记录";
										}
									}
							}
						 }
						}
					 ]],
			data:dataRow,
			onLoadSuccess:function(data){
		    	var checkid=$("input[name='functionname']");
		    	var selectid=$("#edit_functionName").val().replace(/^\s+|\s+$/g, "");
		    	for(var i=0;i<checkid.length;i++){
		    			 if(checkid[i].value==selectid){
		    				document.getElementById(selectid).checked="true";
		    			 }
		    	 }
		    }
		});
}
//统计方式，获取勾选的统计方式，展现在页面
function edit_checfunctionName(){
	var radioVal=$("input[name^='functionname']:checked").val();
		var str="";
		 if(radioVal.split(":")[3]=="count"){
			str+="统计记录("+radioVal.split(":")[0]+")";
		 }
		 if(radioVal.split(":")[3]=="sum"){
			str+="求和("+radioVal.split(":")[0]+")";
		 }
		if(radioVal.split(":")[3]=="avg"){
			str+="求平均("+radioVal.split(":")[0]+")";
		}
		    //后台传值
		    $("#edit_functionName").val(radioVal);
		    //页面展现
			$("#edit_function_method").html(str);
			
			$("#edit_orderby_id").html("");
		    $("#edit_orderbyColumm").val("");
		
	$('#edit_statisticsDialog').dialog('close');
}
//编辑排序
function edit_showOrderbyDialog(){
	edit_initorderby();
     $("#edit_orderbyDialog").dialog('open').dialog('setTitle','选择排序字段');
	
}
//排序，获取勾选的排序属性，用于展现在页面
function edit_checkorderby(){
	var checkedArr=$("input[name='edit_orderid']:checked");
	var str="";
	var id="";
	for(var i=0;i<checkedArr.size();i++){
		str+=checkedArr[i].value.split(":")[0];
		id+=checkedArr[i].value;
		if(i<checkedArr.size()-1){
			str+="<br/>";
			id+=",";
		}
	}
	$("#edit_orderby_id").html("");
	$("#edit_orderby_id").html(str);
    $("#edit_orderbyColumm").val(id);
	$("#edit_orderbyDialog").dialog('close');
}
///排序初始化属性信息
function edit_initorderby(){
	var data = $("#edit_groupvalue").val();
	var functionParam = $("#edit_functionName").val();
	var functionfield = functionParam.split(":");
	var paramData = [];
    if(data.length == 0){
    	 if(functionfield){
    		 var edit_orderid = functionfield[0]+':'+functionfield[1]+':'+functionfield[2];
 		     paramData.push({name:functionfield[0],edit_orderid:edit_orderid});
    	 }
    }else{
    	var groupData = data.replace(/^\s+|\s+$/g, "").split(",");
    	var flag = false;
    	for(var i = 0 ; i < groupData.length ; i++){
    		var field = groupData[i].split(":");
    		if(functionParam){
    			 if(field[2] != functionfield[2]){
    				 paramData.push({name:field[0],edit_orderid:groupData[i]});
    				 flag = true;
    			 }else{
    				 if(groupData.length == 1 ){
    					var fieldName = data[0].split(":");
    					if(fieldName[0]){
    						paramData.push({name:fieldName[0],edit_orderid:groupData[0]});
    					}
    				 }
    			 }
    		}else{
    			paramData.push({name:field[0],edit_orderid:groupData[i]});
    		}
    	}
    	if(flag){
    	  if(functionfield){
    		  var order_id = functionfield[0]+':'+functionfield[1]+':'+functionfield[2];
    		  paramData.push({name:functionfield[0],edit_orderid:order_id});
    		}
    	}
    }	
	
	paramData.push({name:"统计结果",edit_orderid:"统计结果:${result}:${result}"});
	$("#edit_orderbytable").datagrid({
		 height:276,
		 width:298,
		 singleSelect:true,
		 multiple:true,
		 selectOnCheck:false,
		 checkOnSelect:false,
		 fitColumns: true,
		 columns: [[
		            {field:'edit_orderid',checkbox:true},
					{field:'name',title:'排序字段',width:100},
		            {field:'sort',title:'排序',formatter:function(value,row,index){
		            	return "<span><a class='icon-up' style='cursor: pointer;' title='上移' onclick=\"moveUp('edit_orderbytable',"+index+",'edit_orderid')\"></a>&nbsp;&nbsp;<a  style='cursor: pointer;' onclick=\"moveDown('edit_orderbytable',"+index+",'edit_orderid')\" class='icon-down' title='下移'></a></span>";
		            }}
				 ]],
		data:paramData,
	    onLoadSuccess:function(data){
	    	var order_checkid=$("input[name='edit_orderid']");
	    	var order_selectid=$("#edit_orderbyColumm").val().replace(/^\s+|\s+$/g, "").split(",");
	    	for(var i=0;i<order_checkid.length;i++){
	    		 for(var j=0;j<order_selectid.length;j++){
	    			 if(order_checkid[i].value==order_selectid[j]){
	    				 $("input[value='"+order_checkid[i].value+"']").attr("checked","checked");
	    			 }
	    		 }
	    		
	    	 }
	    	 $("input[name='edit_orderid']:checked").length==$("input[name='edit_orderid']").length?$("#edit_orderbytable").parent().find("div.datagrid-header-check").children("input[type='checkbox']").eq(0).attr("checked",true):$("#edit_orderbytable").parent().find("div.datagrid-header-check").children("input[type='checkbox']").eq(0).attr("checked",false);
	    },
    onUncheckAll:function(rows){
    	var checkid=$("input[name='edit_orderid']");
    	for(var i=0;i<checkid.length;i++){
   		    if(checkid[i].checked){
   		    	checkid[i].checked="";
   		    }
   	    }
    }
	});
}
//初始化属性信息json
function edit_initProperty(){
	 var rowData = new Array() ;
		if(propertyData){
			for(var i=0;i<propertyData.length;i++){
				var id = propertyData[i].headerText+':'+propertyData[i].dataType+':'+propertyData[i].dataField ;
				var name = propertyData[i].headerText;
				var type = propertyData[i].dataType ;
				var otherValues = propertyData[i].otherValues ;
				rowData.push({groupid:id,name:name,type:type,otherValues:otherValues}) ;
			}
		}
	return rowData ;
}
//提交任务
function updateTask(){
	var taskname=$("#edit_taskName").val().replace(/^\s+|\s+$/g, "");
	if(taskname==""){
		showAlertMessage("任务名称不能为空!");
		return false;
	}else if(taskname.length>30){
		showAlertMessage("任务名称长度不能超过30!");
		return false;
	}else{
		if(!/^[\w\u4E00-\u9FA5]+$/g.test(taskname)){    
			showAlertMessage("任务名称只能输入汉字、字母、数字和下划线");
			return false;
		} 
	}
	if($("#edit_groupvalue").val()==""){
		$("#edit_groupid").html("请选择分组字段");
		return false;
	}
	if($("#edit_functionName").val()==""){
		$("#edit_function_method").html("请选择函数方法");
		return false;
	}
	var topNumber=$("#edit_topNumber").combobox("getValue");
	var dataArray=$("#edit_taskForm").serializeArray();
	var formdata = {};
 	 $.map(dataArray,function(data){
 		 if(data.name!="top"){
 			formdata[data.name] = data.value;
 		 }else if(data.name=="host"){
 			 if(data.value=="全部"){
 				formdata[data.name] ="";
 			 }else{
 				formdata[data.name]=data.value;
 			 }
 		 }else if(data.name=="topNumber"){
 			 formdata[data.name]=topNumber;
 		 }
 	});	
	$.ajax({
		 url:'/sim/logReport/addTask',
		 type: 'POST',
         data: JSON.stringify(formdata),
         dataType:'json',
         contentType:"text/javascript",
		 success:function(data,status){
			if(data.status==true){
				showAlertMessage("修改成功!");
				tasklist();
				closeViewDialog();
			}
			else if(data.status==false){
				showAlertMessage("任务名称已存在!");
				return false;
			}
			else{
				showErrorMessage("修改失败!");
				return false;
			}
		}
	});
}
