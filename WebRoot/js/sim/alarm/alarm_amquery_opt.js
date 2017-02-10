/**
 * 查询操作
 */

/***
 * 重设表格双击
 * @param rowIndex
 * @param rowData
 * @param _this
 */
AlarmQuery.prototype.onDblClickRowCallBack=function(rowIndex, rowData,_this){//其它页面调用复写扩展
	var _rowdata = [];
	$.each(_this.columns, function(i, cls) {
		$.each(cls, function(j, cs) {
			_rowdata.push({
				f1 : cs.title,
				f2 : "<input type='text' value='"
						+ (!rowData[cs.field] ? "" : rowData[cs.field])
						+ "' readonly/>"
			});
		});
	});
	
	var $alarm_detail=$("<div id='alarm_detail_id'></div>");
	$alarm_detail.append("<div style='width: 420px;margin: 0 auto;border:0px'><table id='_am_detail_table_id' ></table></div>");
	$alarm_detail.dialog({   
	    title: '告警',   
	    width: 600,   
	    closed: true,   
	    cache: false,  
	    resizable:true,
	    modal: true ,
		onClose : function() {
			$(this).dialog("destroy");
		} 
	});   
	$("#_am_detail_table_id").datagrid({// 列表详情
		columns : [ [ {
			field : "f1",
			width : 100
		}, {
			field : "f2",
			width : 250
		} ] ],
		showHeader : false,
		width : 400,
		style:{borderWidth:0},
		data : _rowdata
	});
	
	
	$("#alarm_detail_id").dialog("move",{top:$(document).scrollTop()+($(window).height()-600)*0.5});
	$alarm_detail.dialog("open");
};


var alarmQuery=null;
var alarmQueryForm=null;
(function(){
	alarmQuery=new AlarmQuery();
	alarmQueryForm=new AlarmQueryForm();
	alarmQuery.initColums({"json":"alarm_query_colums"});//加载列集
	if(simHandler["param"]){
		simHandler["param"]["priority"]="(PRIORITY="+simHandler["param"]["priority"]+")";
		alarmQuery.queryAlarm("#alarm_query_table_id", simHandler["param"]);	
		simHandler["param"]=null;	
	}else{
		var timeArea=alarmQueryForm.getTimeAreaFromCurrent(60);
		alarmQuery.queryAlarm("#alarm_query_table_id", {"startTime":timeArea["st"],"endTime":timeArea["et"]});
	}
	
})();



function showAlarmEvent(){
	$("<div id='alarm_eq_div_id'></div>").dialog({   
	    title: '查询',   
	    width: 500,   
	    closed: false,   
	    cache: false,  
	    resizable:true,
	    iconCls:"icon-search",
	    href: "/page/alarm/alarmQueryForm.html",   
	    modal: true ,
		onClose : function() {
			$(this).dialog("destroy");
		},
		onOpen:function(){
		
		}
	});
	/**
	 * 重新调整位置
	 */
	$("#alarm_eq_div_id").dialog("move",{top:$(document).scrollTop()+($(window).height()-600)*0.5});
}


function doAlarmQuery(){
	var level=_alarmQueryForm.level.value,
		priority="(PRIORITY="+level+")",
		fields=null,
		queryParams = {
			"deviceIp" : _alarmQueryForm.deviceIp.value,
			"startTime" :_alarmQueryForm.startTime.value,
			"endTime" : _alarmQueryForm.endTime.value,
			"priority" : priority,
			"eventName" : _alarmQueryForm.eventName.value,
			"srcIp" : _alarmQueryForm.srcIp.value,
			"destIp" : _alarmQueryForm.destIp.value,
			"fields" : fields,
			"header" : null
		};
	 alarmQuery.queryAlarm("#alarm_query_table_id",queryParams);
	 $("#alarm_eq_div_id").dialog("destroy");
}



