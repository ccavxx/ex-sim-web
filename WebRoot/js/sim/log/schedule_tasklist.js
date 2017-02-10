(function(){
	tasklist();
})();
//展现事后报表
function tasklist(){
	$('#taskReport_table').datagrid({   
		url:'/sim/scheduleStatTask/getList',
		fit : true,
		fitColumns : true,
		singleSelect:true,
		multiple:true,
		selectOnCheck:false,
		checkOnSelect:false,
		pagination:true,
		toolbar:'#taskReportToolBar',
		view: detailview,
		detailFormatter:function(index,row){    
		        return '<div style="padding:5px 0"><table id="ddv' + index + '"></table></div>';    
		}, 
		onExpandRow:onExpandRow,
		pageSize : 20,//默认选择的分页是每页5行数据
        pageList : [20,30,40],//可以选择的分页集合
	    columns : [[{field:'ck',checkbox:true},
	               {field:'name',title:'名称',width:100,formatter:function(value,row,index){
	            	   return "<span title='"+ value+"'>"+value+"</span>";
	               }},
	               {field:'status',title:'执行状态',width:50,formatter: function(value,row,index){
	            	   var status = value;
	            	   if(status == "0"){
	            		   status = "等待执行";
	            	   }else if(status == "1"){
	            		   status = "正在执行";
	            	   }else if(status == "2"){
	            		   status = "执行完成";
	            	   }
	            	   return status;
	               }},
	               {field:'period',title:'执行周期',width:50},
	               {field:'beginTime',title:'开始时间',width:50},
	               {field:'endTime',title:'结束时间',width:50},
	               {field:'creator',title:'创建人',width:50},
	               {field:'enabled',title:'状态',width:50,formatter: function(value,row,index){
	            	   return value == true ? "启用":"禁用";
	               }},
	               //{field:'time',title:'周期时间',width:100},
	               {field:'no_field',title:'操作',align:'left',width:80,formatter: function(value,row,index){
            		   var returnStr= "<a title='编辑' class='icon-edit handCss' onclick=\"openScheduleDialog('update',"+row.id+")\"></a>"+
	            		              "<a title='删除' class='icon-remove handCss' onclick=\"delScheduleStatTask("+ row.id +",'"+row.name+"')\"></a>";
            		   var stateCls = row.enabled ? "icon-disable" : "icon-enabled" ;
            		   var title = row.enabled ? "禁用" : "启用" ;
            		   returnStr += "<a title='"+title+"' class='"+stateCls+" handCss' onclick=\"changeStatus("+!row.enabled+","+row.id+")\"></a>";
	            	   if(row.status == "2"){
	            			  returnStr += "<a title='预览' class='icon-search handCss' onclick=\"ifCompleteTask('view',"+row.id+")\"></a><a title='导出' onclick=\"ifCompleteTask('export',"+row.id+")\"  class='icon-export handCss'/></a>";
	            	   }
	                  return returnStr;
	               }}
	               ]]
	    
	}); 
	var pager = $('#taskReport_table').datagrid('getPager');	// get the pager of datagrid
	pager.pagination({//分页栏下方文字显示
	           beforePageText: '第',//页数文本框前显示的汉字  
               afterPageText: '页    共 {pages} 页',  
               displayMsg:'当前显示{from}到{to}, 共{total}条记录'
     });
}
function changeStatus(enabled,id){
	$.getJSON('/sim/scheduleStatTask/changeStatus?id='+ id + '&enabled=' + enabled + '&time='+new Date(),function(result){
		$("#taskReport_table").datagrid("reload") ;
		if(!result.status){
			showErrorMessage(result.message) ;
		}
	}) ;
	
}
function onExpandRow(index,row){
	    $("#ddv"+index).datagrid({
        url:'/sim/scheduleStatTask/getSubjectById?id='+row.id,
        fitColumns:true,
        singleSelect:true,
        loadMsg:'',
        height:'auto',
        columns:[[
            {field:'taskName',title:'主题名称',width:100},
            {field:'dataSourceName',title:'资产类型',width:100},
            {field:'intervalTxt',title:'时间间隔',width:100},
            {field:'progress',title:'进度(%)',width:100},
            {field:'diagram',title:'结果类型',width:100,formatter:formatterDiagram},
            {field:'creater',title:'创建者',width:100}
        ]],
        onResize:function(){
            $("#taskReport_table").datagrid('fixDetailRowHeight',index);
        },
        onLoadSuccess:function(data){
        	if(data.total == 0){
        		$(this).datagrid('appendRow', {subject:'<div style="text-align:center;color:red">没有相关记录！</div>' })
        		       .datagrid('mergeCells', {index: 0, field: 'subject', colspan: 5 });
        	}
            setTimeout(function(){
                $('#taskReport_table').datagrid('fixDetailRowHeight',index);
            },0);
        }
    }) ;
}
function formatterDiagram(value){
	 var returnVal = '';
	  if(value=='1'){
  		   returnVal='柱状图';
  	   }else if(value == '5'){
  		   returnVal = '饼图';
  	   }else if(value == '6'){
  		   returnVal = '曲线图';
  	   }else if(value == "7"){
  		   returnVal = "表格";
  	   }
  	   return returnVal;
}
function formatterSort(){
	return "<span><a class='icon-up' style='cursor: pointer;' title='上移' onclick=\"moveRow(event,this,true)\"></a>&nbsp;&nbsp;<a style='cursor: pointer;' onclick=\"moveRow(event,this,false)\" class='icon-down' title='下移'></a></span>";
}
//删除任务
function delScheduleStatTask(id,name){
	var confirmTitle = "确定删除吗？";
	if(name){
		confirmTitle = "确定删除任务"+ name +"吗?";
	}
	var scheduleId = id;
	if(!id){
		var allCheckedRow = $("#taskReport_table").datagrid("getChecked") ;
		$.each(allCheckedRow,function(index,row){
			scheduleId += row.id + ",";
		}) ;
	}
	
	if(scheduleId){
		$.messager.confirm('提示',confirmTitle,function(r){
			if (r){   
				$.ajax({
					url:'/sim/scheduleStatTask/delete?id=' + scheduleId,
					type:'post',
					dataType:'json',
					success:function(data){
						if(data.status == true){
							$('#taskReport_table').datagrid('reload');
						}
						if(data.status == false){
							showErrorMessage("删除失败!");
							return false;
						}
					}
				   
				});
				$('#taskReport_table').datagrid('uncheckAll');
			}else{
				return;
			}
		});
	}
	
	}
function exportTask(scheduleId){
	window.location ="/sim/scheduleStatTask/export?taskId="+scheduleId;
}
//判断任务是否执行完成
function ifCompleteTask(operator,scheduleId){
	$.ajax({
		url:'/sim/scheduleStatTask/getStatus?id=' + scheduleId,
		type:'post',
		dataType:'json',
		success:function(data){
			if(operator == "view"){
				if(data == "2"){
					openScheduleDialog(operator,scheduleId);
				}else{
				  showErrorMessage("任务未执行成功，不能预览!");
				  return false;	
				}
			}else{
				if(data == "2"){
					exportTask(scheduleId);
				}else{
				  showErrorMessage("任务未执行成功，不能导出!");
				  return false;	
				}
			}
		}
			
			
	});
}
var taskReport_dialog;
function  openScheduleDialog(operator,scheduleId){
	simHandler.schedule_stat={schedule_operator:operator,schedule_Id:scheduleId};
	var url = "";
	if(operator == "update"){
		 url = "/sim/scheduleStatTask/getById?id="+scheduleId;
	}else if(operator == "add"){
		 url = "/page/log/scheduleTaskCreate.jsp";
	}else if(operator == "view"){
		//url = "/page/log/scheduleTaskView.html";
		url="/sim/scheduleStatTask/previewSubjectResult?scheduleId="+scheduleId;
	}
	
	var width = $('#log_taskReport_panel').layout('panel','center').width();
	var height= $('#log_taskReport_panel').layout('panel','center').height();
	var schedule_task_div= $("<div id='taskReport_dialog'></div>");
	schedule_task_div.appendTo($("#log_taskReport_panel"));
	simHandler.taskReport_dialog = $('#taskReport_dialog').dialog({
		href:url,
		style:{'padding':0,'border':0},
		width:width,
		height:height+22,
		top:0,
		left:0,
		inline:true,
		noheader:true,
		collapsed:true,
		modal:false,
		shadow:false,
		border:false,
		onCollapse:closeTaskReporDialog
	}).dialog('expand',true);
}
var count=0;
//当折叠日志查询弹出窗口后
function closeTaskReporDialog(){
	count++;
	if(count%2 == 0 && simHandler.taskReport_dialog){
		simHandler.taskReport_dialog.dialog('destroy');
		simHandler.taskReport_dialog = null;
	}
}
function closeScheduleDialog(){
	$('#taskReport_table').datagrid('reload');
	if(simHandler.taskReport_dialog){
		simHandler.taskReport_dialog.dialog('destroy');
		simHandler.taskReport_dialog = null;
	}
	
}


