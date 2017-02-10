//@ sourceURL=schedule_taskcreate.js
var lastEditRow = undefined;
var deviceTypeEditor = {
		type:'combotree',
		options:{url:'/sim/logReport/dataSourceTree',
				 editable:false,
				 textToField:"dataSourceName",
				 onBeforeLoad:beforeDataSourceTreeLoad,
				 onHidePanel:resetDeviceTypeText,
				 onBeforeSelect:beforeCategoreSelect,
				 onSelect:onSelectDataSource
		}
};
var intervalEditor = {
		type : 'combobox',
		options : {
			url : '/sim/logReport/getLogInterval',
			textToField : "intervalTxt",
			editable : false,
			textField : 'label'
		}
};
function deviceTypeFmt(value,row,index){
	return nvl(row["dataSourceName"]) ;
}
function intervalFmt(value,row,index){
	var intervalTxt = row["intervalTxt"] ;
	var content = intervalTxt ;
	if(row["interval"] == "user_define"){
		var title = row.logBeginTime && row.logEndTime ? row.logBeginTime + "至" + row.logEndTime : "无";
		var userDefineTimeId = "user_define_time"+index ;
		content = "<a class='icon-timer' style='cursor:help;' title='"+title+"'></a>" ; 
		content += "<span style='margin:0px 5px;'>"+intervalTxt+"</span>";
		content += "<a onclick='openTimeSelector(event,\""+userDefineTimeId+"\","+index+")' id='"+userDefineTimeId+"'><i class='hand icon-calendar'></i></a>"
	}
	return content ;
}
/**
 * 在日志源树加载以前追加taskId参数
 * @param node
 * @param param
 */
function beforeDataSourceTreeLoad(node,param){
	var editRow = $("#subject_table").datagrid("getRows")[lastEditRow]
	param.taskId = editRow.id ;
}

function beforeCategoreSelect(node){
	var tree = $(this) ;
	if(node.attributes.level >= 3){
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
function onSelectDataSource(node){
	var row = $("#subject_table").datagrid("getRows")[lastEditRow];
	var level = node.attributes.level ;
	if(level == 3){//选中是设备类型
		row["deviceType"] = node.id ;
		row["host"] = null ;
	}else if(level == 4){//选中的日日志源
		row["deviceType"] = node.attributes.securityObjectType ;
		row["host"] = node.attributes.ip ;
	}
}
function resetDeviceTypeText(){
	var cbt = $(this) ;
	var selectItem = cbt.combotree("tree").tree("getSelected") ;
	var text = cbt.combotree("getText") ;
	if(selectItem != null && text != selectItem.pathName){
		cbt.combotree("setText",selectItem.pathName) ;
	}
}

//formatter
function formatterSort(value,row,index){
	return "<span><a class='icon-up' style='cursor: pointer;' title='上移' onclick=\"moveRow(event,this,true)\"></a>&nbsp;&nbsp;<a style='cursor: pointer;' onclick=\"moveRow(event,this,false)\" class='icon-down' title='下移'></a></span>";
}
var isRenderer = false ;
function openTimeSelector(event,timerId,rowIndex){
	event = event ? event : window.event ;
	if(event.stopPropagation){
		event.stopPropagation();
	}else{
		window.event.cancelBubble = true ;
	}
	if(isRenderer){
		isRenderer = false ;
		return ;
	}
	var servertime = simHandler.serverTime ;
	var optionSet = {
		    startDate: moment(servertime).subtract('hours', 1),
		    endDate: moment(servertime),
		    maxDate: false,
		    minDate: moment(servertime).subtract('year',10),
		    showDropdowns:true,
		    timePicker: true,//显示时间选择器
		    timePickerIncrement: 10,//时间间隔10分钟
		    timePicker12Hour: false,//24小时制
		    locale: locales['zh-CN'],
		    format: 'YYYY-MM-DD HH:mm:ss',
		    opens: 'left',
		    ranges: {
		       '今天': [moment(servertime).startOf('day'), moment(servertime).endOf('day')],
		       '昨天': [moment(servertime).subtract('days', 1).startOf('day'), moment(servertime).subtract('days', 1).endOf('day')],
		       '最近一月': [moment(servertime).subtract('month', 1).startOf('day'), moment(servertime).subtract('days', 1).endOf('day')],
		       '本月': [moment(servertime).startOf('month'), moment(servertime).endOf('month')]
		    },
		    onMonthYearChange:function(isLeft,month,year){
		    	if(isLeft){
		    		var oldStartDate = this.startDate.clone() ;
		    		this.setStartDate(year+"-" + (month+1) + "-01 00:00:00") ;
		    		this.oldStartDate = oldStartDate ;
		    	}else{
		    		var oldEndDate = this.endDate.clone() ;
		    		var endDate = new Date() ;
		    		endDate.setUTCFullYear(year, month, this.endDate.date()); 
		    		endDate.setUTCHours(15, 59, 59, 0);//UTC时间15点相当于中国23 
		    		this.setEndDate(endDate) ;
		    		this.oldEndDate = oldEndDate ;

		    	}
		    }
		};
	var timer = $("#"+timerId) ;
	timer.daterangepicker(optionSet, function(start,end){
		var table = $("#subject_table") ;
		var row = table.datagrid("getRows")[rowIndex] ;
		row["logBeginTime"] = start.format("YYYY-MM-DD HH:mm:ss") ;
		row["logEndTime"] = end.format("YYYY-MM-DD HH:mm:ss") ;
		table.datagrid("refreshRow",rowIndex) ;
	});
	isRenderer = true ;
	simHandler.showDaterangepickerWin(timerId) ;
}

function closeTaskListDialog(){
	$("#taskList_Dialog").dialog('close');
}
//获取主题数据
function getTaskList(){
	$('#taskList_table').datagrid({   
		url:'/sim/logReport/getAll',
		fit : true,
		fitColumns : true,
		singleSelect:true,
		multiple:true,
		selectOnCheck:false,
		checkOnSelect:false,
		width:750,
		height:300,
	    columns : [[{field:'taskId',checkbox:true},
	               {field:'taskName',title:'主题名称',width:300},
	               {field:'dataSourceName',title:'日志源',width:200},
	               {field:'intervalTxt',title:'时间间隔',width:100},
	               {field:'diagram',title:'结果类型',width:100,formatter:formatterDiagram}
	               ]],
	   onLoadSuccess:function(data){
	    	var selectedRows = $('#subject_table').datagrid('getRows');
    		if(data.rows.length > 0){
    			$.each(selectedRows, function(index, item){
    				var rowIndex = getRowIndex(data.rows,item.id);
    				$("#taskList_table").datagrid("checkRow",rowIndex) ;
    			});
    		}
	    }
	}); 
	$("#taskList_Dialog").dialog('open').dialog('setTitle','选择主题');
}
//对已选中主题进行勾选
function getRowIndex(data, id){
	var rowIndex = -1 ;
	$.each(data,function(index,field){
		if(field.id == id){
			rowIndex = index ;
			return false ;//停止循环
		}
	}) ;
	return rowIndex ;
}


function onClickCell(rowIndex, field, value){
	if(lastEditRow != -1){
		endRowEdit(lastEditRow) ;
	}
	if(field == 'dataSource' || field == 'interval'){
		var table = $("#subject_table") ;
		lastEditRow = rowIndex ;
		table.datagrid("beginEdit",rowIndex) ;
		var ed = table.datagrid('getEditor', {index:rowIndex,field:field});
	}
}

function endRowEdit(editRowIndex){
	if(editRowIndex == -1){
		return ;
	}
	 var editors = $('#subject_table').datagrid('getEditors',editRowIndex);
	 var currentRow = $("#subject_table").datagrid("getRows")[editRowIndex] ;
	 if(editors.length > 0){
		 for ( var i = 0; i < editors.length; i++) {
			 var ed = editors[i] ;
			 if(ed.field == "dataSource"){
				 var toField = $(ed.target).combotree('options').textToField ; 
				 if(toField){
					 var selectNode = $(ed.target).combotree("tree").tree("getSelected") ;
					 if(selectNode != null){
						 currentRow[toField] = selectNode.pathName;
					 }
				 }
			 }else if(ed.field == "interval"){
				 var toField = $(ed.target).combo('options').textToField ; 
				 if(toField){
					 currentRow[toField] = $(ed.target).combobox("getText");
				 }
			 }
		 }
	 }
	 $("#subject_table").datagrid("endEdit",editRowIndex) ;
	 lastEditRow = -1 ;
}
//获取已选的主题
function getCheckTheme(){
	var checkRows = $("#taskList_table").datagrid("getChecked") ;
	$('#subject_table').datagrid("loadData",checkRows);
	closeTaskListDialog();
}

function moveRow(e, target, isUp) {
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

//信息保存
function validateName(){
	var taskName = $("#taskName").val();
	var operation = $("#schedule_operator").val();
	var taskId = $("#schedule_Id").val();
	if(taskName == ""){
		showAlertMessage("任务名称不能为空!");
		return;
	}else if(taskName.length>30){
		showAlertMessage("任务名称长度不能超过30!");
		return;
	}else{
		if(!/^([0-9a-zA-Z_\.\u4e00-\u9fa5-]+)$/g.test(taskName)){
		   showAlertMessage("任务名称只允许汉字、字母、数字、点、下划线和中划线");
		   return;
		}
	}
	$.ajax({
		 url:'/sim/scheduleStatTask/isNameExist',
		 type:"post",
		 data:{name:taskName,operation:operation,taskId:taskId},
		 dataType:'json',
		 success:function(data){
			 if(data.result){
				 showAlertMessage("任务名称已存在!");
				 return;
			 }else{
				 saveScheduleStatTask();
			 }
		 }
	});
}
function saveScheduleStatTask(){
	endRowEdit(lastEditRow) ;
	var email = $("#email").val();
	var exp_email = /^[\w\+\-]+(\.[\w\+\-]+)*@[a-z\d\-]+(\.[a-z\d\-]+)*\.([a-z]{2,4})$/i;
	if(email){
		var error_email = "";
		var emailArr = email.split(";");
		$.each(emailArr,function(index,item){
			if(!exp_email.test(item)){
				if(error_email){
					error_email += ",";
				}
				error_email += item;
			}
		});
		if(error_email){
			showAlertMessage("邮箱：" + error_email + "格式不正确");
	        return;
		}
		
	}
	var validPollTime = pollTime.validatePollTime() ;
	if(validPollTime != null){
		showAlertMessage(validPollTime) ;
		return false;
	}
	var subjectRows = $('#subject_table').datagrid("getRows");
	if(subjectRows.length == 0){
		showAlertMessage("主题不能为空，请选择主题！");
        return;
	}
	for(var i=0;i<subjectRows.length;i++){
		var subject = subjectRows[i] ;
		if(subject.interval == "user_define" && (subject.logBeginTime == null || subject.logEndTime == null)){
			showAlertMessage("“" + subject.taskName + "”没有选择时间范围！");
			return ;
		}
	}
	subjectRows = $.map(subjectRows,function(row){
		return {
			subjectId : row.id,
			securityObjectType : row.deviceType,
			host : row.host,
			logBeginTime : row.logBeginTime,
			logEndTime : row.logEndTime,
			interval : row.interval
		};

	}) ;
	var data = {
		subjects:subjectRows
	}
	$("#scheduleForm :input").each(function(index,el){
		data[el.name] = $(el).val() ;
	}) ;
	var str = JSON.stringify(data) ;
	$.ajax({
		type:"post",
		url:"/sim/scheduleStatTask/save",
		processData:false,
		data:str,
		success:function (result) {
			if (result.success) {
				closeScheduleDialog();
			} else {
				showAlertMessage(result.message);
			}
		},
		dataType : "json",
		contentType:"application/json"
	});
}
$(function(){
	$("#poll_time_type").trigger("change") ;
	if(simHandler.schedule_stat.schedule_operator == "add"){
		$("#poll_time_container [name='min']").val(10);
	}
	
	if($("#schedule_operator").val() == "update"){
		var data = {id:$("#schedule_Id").val(),_time:new Date().getTime()} ;
		$.getJSON("/sim/scheduleStatTask/getSubjectById",data,function(data){
			$("#subject_table").datagrid("loadData",data) ;
		})
	}
});
