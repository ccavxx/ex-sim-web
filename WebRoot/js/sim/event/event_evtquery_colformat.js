
/**
 * 事件查询列表，设置列格式
 */
(function(){
	var priority_labels= null;
	$.ajax({
		type : "post",
		url : "../../sim/eventMonitor/jsondata",
		data:{"json":"evt_priority"},
		async : false,
		dataType : "json",
		success : function(data) {
			priority_labels=data;
		}
	});
	
	EventQuery.prototype.columFormat=function(cffs){
		
		var argumTemp = "";
		
		if(arguments.length > 1){
			argumTemp = ("," + arguments[1]);
		}
		$.each(cffs,function(i,cff){
			if(cff['field']=='PRIORITY'){
				cff.formatter = simHandler.levelFormatter ;
			}else if(cff['field'] == 'LOG_COUNT'){
				cff.formatter = function(value,row,index){
					var spTemp = "<span style='margin-left:18px;' title='查看关联日志' class=\"badge badge-info hand\" onclick=\"eventQuery.doCorrelateOnRowIndex(";
					spTemp += (index + argumTemp + ")\">" + value + "</span>");
					return spTemp;
				} ;
			}else if(cff['field'] == 'DESCR'){
				cff.formatter = function(value,row,index){
					var spTemp = "<span title ='"+value+"'>"+ value +"</span>";
					return spTemp;
				} ;
			}
		});
	};
})();