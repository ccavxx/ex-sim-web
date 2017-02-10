(function(){
	tasklist();
})();
//展现事后报表
function tasklist(){
	$('#log_statistics_table').datagrid({   
		url:'/sim/logReport/taskList',
		fit : true,
		singleSelect:true,
		multiple:true,
		selectOnCheck:false,
		checkOnSelect:false,
		fitColumns: true,
		pagination:true,
		toolbar:'#reportToolBar',
		pageSize : 20,//默认选择的分页是每页5行数据
        pageList : [20,30,40],//可以选择的分页集合
        columns : [[
         	       {field:'taskId',checkbox:true},
                    {field:'taskName',title:'名称',width:170,formatter:titleFormatter},
                    {field:'deviceType',title:'日志源类型',width:150},
                    {field:'creater',title:'创建者',width:70},
                    {field:'intervalTxt',title:'时间间隔',width:70,formatter:intervalFormatter},
                    {field:'beginTime',title:'开始时间',width:100},
                    {field:'endTime',title:'结束时间',width:100},
                    {field:'taskState',title:'状态',width:50,formatter: stateFormatter},
                    {field:'progress',title:'进度(%)',width:40},
                    {field:'diagram',title:'结果类型',width:50,formatter:diagramFormatter},
                    {field:'no_field',title:'操作',align:'left',width:100,formatter:operationFormatter}
         	    ]]
	    
	}); 
	var pager = $('#log_statistics_table').datagrid('getPager');	// get the pager of datagrid
	pager.pagination({//分页栏下方文字显示
	           beforePageText: '第',//页数文本框前显示的汉字  
               afterPageText: '页    共 {pages} 页',  
               displayMsg:'当前显示{from}到{to}, 共{total}条记录'
     });
}
function titleFormatter(value,row,index){
   return "<span title='"+value+"'>"+value+"</span>";
}
function intervalFormatter(value,row,index){
   var interval =null ;
   if(row["interval"] == "user_define"){
	   interval = row.logIntervalStart + "至" + row.logIntervalEnd;
	   return "<a class='icon-timer' style='cursor:help;padding-right:5px;' title='"+interval+"'></a>" + value;
   }else{
	   return value ;
   }
}
function stateFormatter(value,row,index){
   if(value == "2"){
	   return "执行成功";
   }else if(value == "-1"){
	   return "执行失败";
   }else if(value == "1"){
	   return "正在执行";
   }else if(value == "-2"){
	   return "取消执行";
   }else if(value == "0"){
	   return "未执行";
   }
}
function diagramFormatter(value,row,index){
	   var returnVal = "";
	   if(value=="1"){
		   returnVal="柱状图";
	   }
	   if(value == "5"){
		   returnVal = "饼图";
	   }
	   if(value == "6"){
		   returnVal = "曲线图";
	   }
	   if(value == "7"){
		   returnVal = "表格";
	   }
	   return returnVal;
}
function operationFormatter(value,row,index){
   var taskstatus = row.taskState;
   var execute = "<a title=\"执行\" style=\"cursor:pointer;margin-right:5px;\" class=\"icon-start\" onclick=\"runNowTask("+row.taskId+")\"></a>";
   var edit = "<a title=\"编辑\" class=\"icon-edit handCss\" onclick=\"excuteTask("+row.taskId+",'edit')\"></a>";
   var del = "<a title=\"删除\" class=\"icon-remove handCss\" onclick=\"delTask("+row.taskId+",0"+",'"+ row.taskName+"')\"></a>";
   var view = "<a title=\"预览\" class=\"icon-search handCss\" onclick=\"excuteTask("+row.taskId+",'view')\"></a>";
   var exportExcel = "<a title=\"导出excel\" style=\"cursor:pointer;width:16px;height:16px;\"  class=\"icon-excel\" onclick=\"expTask("+row.taskId+")\"></a>";
   var cancel = "<a title=\"取消\" class=\"icon-undo handCss\" onclick=\"cancelTask("+row.taskId+")\"></a>";
   if(taskstatus == "2"){
	   return  execute + edit +del + view + exportExcel;
   }else if(taskstatus == "-1" || taskstatus == "-2" || taskstatus == "0"){
	   return execute + edit + del;
   }else if(taskstatus == "1"){
	   return  cancel + del;
   }
}
function reloadData(){
//	var queryParams = $('#cxdm').datagrid('options').queryParams;  
//    queryParams.who = who.value;  
//    queryParams.type = type.value;  
//    queryParams.searchtype = searchtype.value;  
//    queryParams.keyword = keyword.value;  
//    //重新加载datagrid的数据  
    $("#log_statistics_table").datagrid('reload'); 
}
function runNowTask(taskId){
	$.ajax({
		url:'/sim/logReport/runNowTask?taskId= '+ taskId,
		type:'post',
		dataType:'json',
		success:function(data){
			if(data.status == true){
				reloadData();
			}
			if(data.status == false){
				showErrorMessage("主题执行失败!");
				return false;
			}
		}
	   
	});
}
//取消任务
function cancelTask(id){
	$.messager.confirm('提示','确定取消正在执行的主题吗？',function(r){
		if (r){   
			$.getJSON('/sim/logReport/cancelTask?id='+ id+"&_time="+new Date().getTime(),
				function(result){
					if(result.status == true){
						reloadData();
					}else{
						showErrorMessage("取消执行失败!");
					}
				}
			);
			$('#log_statistics_table').datagrid('uncheckAll');
		}
	});
}
//导出任务报表数据
function expTask(id){
	window.location="/sim/logReport/exportTaskExcel?taskid=" + id;
}
//删除任务
function delTask(id,status,name){
	if(status == "1"){//批量删除
		var allCheckedRow = $("#log_statistics_table").datagrid("getChecked") ;
		$.each(allCheckedRow,function(index,row){
			id += row.taskId + ",";
		}) ;
	}
	var confirmTitle = "确定删除吗？";
	if(name){
		confirmTitle = "确定删除统计主题"+name +"吗?";
	}
	if(id){
		$.messager.confirm('提示',confirmTitle,function(r){
			if (r){   
				$.ajax({
					url:'/sim/logReport/delete?id=' + id,
					type:'post',
					dataType:'json',
					success:function(data){
						if(data.status == true){
							reloadData();
						}
						if(data.status == false){
							showErrorMessage("删除失败!");
							return false;
						}
					}
				   
				});
				$('#log_statistics_table').datagrid('uncheckAll');
			}else{
				return;
			}
		});
	}else{
		showAlertMessage("请选择要删除的信息!");
		return;
	}

	
}
//判断任务是否执行完成
function excuteTask(id,flag){
	var url="";
	if(flag == "edit"){//编辑
		var allRow = $("#log_statistics_table").datagrid("getRows") ;
		var editRow = "";
		$.each(allRow,function(index,row){
			if(row.taskId == id){
				editRow = row ;
				return false ;
			}
		}) ;
		if(!editRow){
			return ;
		}
		simHandler.log_stat = {} ;
		simHandler.log_stat.operation = "update" ;
		simHandler.log_stat.editTask = editRow ;
		url = "/page/log/logtaskcreate2.html";
	}else if(flag == "view"){//预览
		simHandler.log_stat = {} ;
		simHandler.log_stat.taskId = id ;
	   url = "/page/log/logstatistics.html";
	}
	viewPanel(url);
}
function  viewPanel(url){
	var width = $('#log_subject_panel').layout('panel','center').width();
	var height= $('#log_subject_panel').layout('panel','center').height();
	var subject_div= $("<div id='logtask_dialog'></div>");
	subject_div.appendTo($("#log_subject_panel"));
	simHandler.log_edit_statistics_dialog = $('#logtask_dialog').dialog({
		href:url,
		style:{'padding':0,'border':0},
		width:width,
		height:height+25,
		top:0,
		left:0,
		inline:true,
		noheader:true,
		collapsed:true,
		modal:true,
		shadow:false,
		border:false,
		onCollapse:onViewCollapseDialog
	}).dialog('expand',true);
}
var count=0;
//当折叠日志查询弹出窗口后
function onViewCollapseDialog(){
	count++;
	if(count%2 == 0 && simHandler.log_edit_statistics_dialog){
		simHandler.log_edit_statistics_dialog.dialog('destroy');
		simHandler.log_edit_statistics_dialog = null;
	}
}
function closeViewDialog(){
	simHandler.log_edit_statistics_dialog.dialog('collapse',true);
}