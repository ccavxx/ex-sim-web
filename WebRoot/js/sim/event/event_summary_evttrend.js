function EventTrend() {
	this.condition={"startTime":"","endTime":"","scope":"day"};
}

EventTrend.prototype.loadTrend = function(_show, id) {
	var _this=this,date=new Date(simHandler.serverTime.getTime());
	var starttime,endtime=date.Format("yyyy-MM-dd HH:mm:ss");
	var scope = this.condition.scope;
	if(scope=='day'){
		date.setTime(date.getTime()-24*60*60*1000);
	}
	if(scope=='week'){
		date.setTime(date.getTime()-7*24*60*60*1000);
	}
	if(scope=='month'){
		date.setTime(date.getTime()-30*24*60*60*1000);
	}
	starttime=date.Format("yyyy-MM-dd HH:mm:ss");
	this.showId=id;
	this.condition.startTime=starttime;
	this.condition.endTime=endtime;
//	var date = new Date(simHandler.serverTime.getTime()),timestamp = date.Format("yyyyMMdd");
//	this.condition.time=timestamp;
	$.ajax({
		type : "post",
		url : "/sim/event/eventRiverDataByTime",
		data: {"startTime":starttime,"endTime":endtime, "scope":scope},
		dataType:"json",
	    async : false,
		success : function(data) {
			_show(id,data);
		}
	});
};
