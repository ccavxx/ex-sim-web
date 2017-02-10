/**
 * 
 */


function AlarmQueryForm(){
	this.category1=null;
	this.category2=null;
}

AlarmQueryForm.prototype.getTimeAreaFromCurrent=function (t){
	var time=new Date(simHandler.serverTime.getTime()),_this=this;
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

AlarmQueryForm.prototype.formatTime=function(time){

	return time.getFullYear()+"-"
				+(time.getMonth()+1)+"-"
				+time.getDate()+" "
				+time.getHours()+":"
				+time.getMinutes()+":"
				+time.getSeconds();
};