
/**
 * 事件查询列表，设置列格式
 */
(function(){
	var priority_labels= null;
	$.ajax({
		type : "post",
		url : "/sim/eventRestQuery/jsondata",
		data:{"json":"evt_priority"},
		async : false,
		dataType : "json",
		success : function(data) {
			priority_labels=data;
		}
	});
	
	EventQuery.prototype.columFormat=function(cffs){
		$.each(cffs,function(i,cff){
			if(cff['field']=='PRIORITY'){
				cff.formatter = simHandler.levelFormatter ;
			}else if(cff['field'] == 'LOG_COUNT'){
				cff.formatter = function(value,row,index){
					return "<span style='margin-left:18px;' title='查看关联日志' class=\"badge badge-info hand\" onclick=\"eventQuery.doCorrelateOnRowIndex("+index+")\">"+value+"</span>" ;
				} ;
			}
		});
	};
})();