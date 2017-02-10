/****/


function EventPrioritySum(){
	this.condition={"startTime":"","endTime":"","priority":-1};
	this.showId=null;
	this.labelInfo=null;
	this.timeUnit="day";
}
EventPrioritySum.prototype.initLabel=function(){
	var _this=this;
	$.ajax({//优先级
	    type: "post",
	    url: "../../sim/eventMonitor/jsondata?json=evt_priority",
	    dataType:"json",
	    async : false,
	    success: function(data){
	    	_this.labelInfo=data;
	   }
	}); 
};

EventPrioritySum.prototype.loadPrioritys=function(_show,id){
	var _this=this,date=new Date(simHandler.serverTime.getTime());
	var starttime,endtime=date.Format("yyyy-MM-dd HH:mm:ss");
		if(this.timeUnit=='day'){
			date.setTime(date.getTime()-24*60*60*1000);
		}
		
		if(this.timeUnit=='week'){
			date.setTime(date.getTime()-7*24*60*60*1000);
		}
		
		if(this.timeUnit=='month'){
			date.setTime(date.getTime()-30*24*60*60*1000);
		}
		starttime=date.Format("yyyy-MM-dd HH:mm:ss");
	
	this.showId=id;
	this.condition.startTime=starttime;
	this.condition.endTime=endtime;
	 
	if(_this.labelInfo){
		$.ajax({
		    type: "post",
		    url: "../../sim/event/levelStatisticByTime",
		    data: {"startTime":starttime,"endTime":endtime},
		    dataType:"json",
		    success: function(data){
		    	_show(id,data);
		   }
		}); 
	}
};
