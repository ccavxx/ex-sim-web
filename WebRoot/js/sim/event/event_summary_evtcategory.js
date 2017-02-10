
function EventCategory(){
	this.condition = {"startTime":"","endTime":""};
	this.showId = null;
	this.timeUnit = "day";
	this.typeCgy = 0;
	this.typePry = 0;
	this.typeTrd = 0;
	this._HORIZON_DAY = "（天）";
	this._HORIZON_WEEK = "（周）";
	this._HORIZON_MONTH = "（月）";
}
/**
 * 更新 panel 标题
 * @param panelId
 * @param unit
 */
EventCategory.prototype.setEventPanelTitle = function(panelId, unit){
	var $panel = $("#" + panelId);
	var optionsTemp = $panel.panel("options");
	var title = optionsTemp.title.replace(this._HORIZON_DAY, "")
								.replace(this._HORIZON_WEEK, "")
								.replace(this._HORIZON_MONTH, "");
	switch(unit){
		case "day":
			title += this._HORIZON_DAY;
			break;
		case "week":
			title += this._HORIZON_WEEK;
			break;
		case "month":
			title += this._HORIZON_MONTH;
			break;
	}
	$panel.panel("setTitle", title);
};
EventCategory.prototype.loadCategorys=function(_show,_id){
	var _this=this,date=new Date(simHandler.serverTime.getTime());
	
	var starttime,
		endtime=date.Format("yyyy-MM-dd HH:mm:ss");
		
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
	this.condition.startTime=starttime;
	this.condition.endTime=endtime;
	this.showId=_id;
	
	
	
		$.ajax({
		    type: "post",
		    url: "../../sim/event/categoryStaticsticByTime",
		    data: {"startTime":starttime,"endTime":endtime},
		    dataType:"json",
		    success: function(data){
		     _show(_this.showId,data);
		   }
		}); 
};


