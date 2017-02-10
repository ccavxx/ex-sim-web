/**
 * 表单数据处理
 * @returns {EventQueryForm}
 */
function EventQueryForm(){
	this.category1=null;
	this.category2=null;
	this.eventName=null;
}

EventQueryForm.prototype.init=function(){
	// dataSourceDeviceTree.load("#deviceTypeId");//初始化表单 日志类型树
	 $('#ssdt').datetimebox({   //初始化日历
			    required: false,   
			    showSeconds: true  
	 }); 
	 $('#sedt').datetimebox({   
	    required: false,   
	    showSeconds: true  
	 });
	 $('#event_hour_select_id').combobox({//初始化选择时间  
		    url:'../../sim/eventMonitor/jsondata?json=recent_times',   
		    valueField:'time',   
		    textField:'text'
	 });
};

EventQueryForm.prototype.formatTime=function(time){
	var month=time.getMonth()+1,date=time.getDate(),hours=time.getHours(),minu=time.getMinutes(),second=time.getSeconds();
	return time.getFullYear()+"-"
				+(month<10?('0'+month):month)+"-"
				+(date<10?('0'+date):date)+" "
				+(hours<10?('0'+hours):hours)+":"
				+(minu<10?('0'+minu):minu)+":"
				+(second<10?('0'+second):second);
};


EventQueryForm.prototype.getTimeAreaFromCurrent=function (t){
	var time=simHandler.newServerDate(),_this=this;
	
	switch (t) {
		case 0://当天  天
			time.setHours(0, 0, 0, 0) ;
	 	    startTimeStr=_this.formatTime(time);
	 		time.setHours(23, 59, 59, 0); 
	 		endTimeStr=_this.formatTime(time);
			break;
		case 1://昨天 天
	 		var date=time.getDate();
	 		time.setDate(date-1);
	 		time.setHours(0, 0, 0, 0) ;
	 	    startTimeStr=_this.formatTime(time);
	 	    time.setHours(23, 59, 59, 0); 
	 		endTimeStr=_this.formatTime(time);
	 		
			break;
		
		default://一定事件范围内  分钟
			endTimeStr=_this.formatTime(time);
			var tt=time.getTime();
			time.setTime(tt-t*60*1000);
			startTimeStr=_this.formatTime(time);
			break;
	}
	return {"st":startTimeStr,"et":endTimeStr};
};


EventQueryForm.prototype.dealFormTimeArea=function(){
	 //事件处理
	 var timeType= evtQueryForm.timeType, timeArea={},_this=this;
	 $.each(timeType,function(i,ttype){
	 	if(ttype.checked){
	 		switch (parseInt(ttype.value)) {
			case 2:
				/*timeArea["st"]=$("#ssdt").datetimebox('getValue');
				timeArea["et"]=$("#sedt").datetimebox('getValue');*/
				timeArea["st"]=$("#ssdt").val();
				timeArea["et"]=$("#sedt").val();
				break;
			default://默认查询最近一段事件内的
				  var reVal=parseInt($("#event_hour_select_id").combobox("getValue"));
				  timeArea=_this.getTimeAreaFromCurrent(reVal);
				break;
			}
	 	}
	 });
	 return timeArea;
};

EventQueryForm.prototype.dealFormPriority=function (){
	
	var level=evtQueryForm.level.value;
	if(parseInt(level)!=-1){
		return "(PRIORITY="+level+")";
	}
	return null;
};
EventQueryForm.prototype.reset=function(){
	$(evtQueryForm).form("reset");
};
