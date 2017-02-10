function AlarmCgDisplayer() {
	this.mainboxId = null;
	this.catogramId = null;
	this.reqURL = null;
	this.condition = null/* {"startTime":null,"endTime":null} */;
	if(arguments.length&&arguments.length>=2){
		this.mainboxId=arguments[0];
		this.catogramId=arguments[1];
		if(arguments.length==3){
			this.reqURL = arguments[2];
		}
		if(arguments.length==4){
			this.condition =arguments[3];
		}
	}
};

AlarmCgDisplayer.prototype.update = function() {//默认添加一个DIV
	$(this.mainboxId).empty();
	if(!arguments[0]){
		var $catogram = $("<div id='' style='margin: 0 auto;width:100%;height:100%;'></div>");
		$catogram.attr("id", this.catogramId.charAt(0) != "#" ? this.catogramId
				: this.catogramId.substring(1));
		$(this.mainboxId).append($catogram);
	}
};

AlarmCgDisplayer.prototype.load = function(c, f,u) {
	this.update(u);
	this.condition = c;
	var _this = this;
	$.ajax({
		type : "post",
		url : _this.reqURL,
		data : _this.condition,
		async : false,
		dataType : "json",
		success : function(data) {
			f(data, _this);
		}
	});
};

function getMonthTimeScope(){
	var date=simHandler.newServerDate();
	return {"endTime":date.Format("yyyy-MM-dd HH:mm:ss"),"startTime":(date.setMonth(date.getMonth()-1)>0?date.Format("yyyy-MM-dd HH:mm:ss"):"1970-01-01 00:00:00"/*不执行*/)};
}
function doAlarmLevel(ct){
	
	var alarmCgDisplayer = new AlarmCgDisplayer("#_alarm_level_id","#_alarm_level_cg_id"),timeScope=getMonthTimeScope();
	alarmCgDisplayer.reqURL = "../../sim/alarm/levelStatisticByTime";
	if(ct==0){//饼图
	 
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var _piedata = [];
				$.each(data, function(i, c) {
					_piedata.push({
						name : c['p_label'],
						y : c['opCount'],
						sliced : false,
						selected : true
						
					});
				});
				loadPie(_this.catogramId, _piedata);
			}
		});
	}
	
	if(ct==1){//柱状图
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var c2d={'categories':[],'data':[]};
				$.each(data, function(i, c) {
					c2d.categories.push(c['p_label']);
					c2d.data.push(c['opCount']);
				});
				loadColums(_this.catogramId,c2d,function(c,y){showQueryfrom(c);});
			}
		});
		
	}
	
	if(ct==2){//表格
		/*"startTime" : "2014-01-01 00:00:00",
			"endTime" : "2014-04-01 00:00:00"*/
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var $table=$("<table></table>").appendTo(_this.mainboxId),fvalues=[];
				$.each(data, function(i, c) {
					fvalues.push({priority:c["p_label"],count:c["opCount"]});
				
				});
				$table.datagrid({   
					singleSelect : true,
				   	data:fvalues,
				    columns:[[   
				        {field:'priority',title:'优先级',width:100},   
				        {field:'count',title:'数量',width:200},   
				    ]],
				    onClickRow:function(rowIndex, rowData){
				    	
				    }
				});
			}
			
		}, true);
	}

}

function doAlarmDev(ct){
	var alarmCgDisplayer = new AlarmCgDisplayer("#_alarm_category_id","#_alarm_category_cg_id"),
	timeScope=getMonthTimeScope();
	alarmCgDisplayer.reqURL="../../sim/alarm/devStatisticByTime";
	
	if(ct==0){
		/*"startTime" : "2014-01-01 00:00:00",
			"endTime" : "2014-04-01 00:00:00"*/
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var _piedata = [];
				$.each(data, function(i, c) {
					_piedata.push({
						name : c['src'],
						y : c['total'],
						sliced : false,
						selected : true
						
					});
				});
				loadPie(_this.catogramId, _piedata);
			}
		});
	}
	
	if(ct==1){
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var c2d={'categories':[],'data':[]};
				$.each(data, function(i, c) {
					c2d.categories.push(c['src']);
					c2d.data.push(c['total']);
				});
				loadColums(_this.catogramId,c2d,function(c,y){showQueryfrom(c);});
			}
		});
	}
	
	if(ct==2){//表格
		alarmCgDisplayer.load(timeScope, function(data, _this) {
			if (data) {
				var $table=$("<table></table>").appendTo(_this.mainboxId),fvalues=[];
				$.each(data, function(i, c) {
					fvalues.push({src:c["src"],count:c["total"]});
				
				});
				$table.datagrid({   
					singleSelect : true,
				   	data:fvalues,
				    columns:[[   
				        {field:'src',title:'设备',width:180},   
				        {field:'count',title:'数量',width:200},   
				    ]],
				    onClickRow:function(rowIndex, rowData){
				    	
				    }
				});
			}
			
		}, true);
	}
}

function doAlarmTrend(ct){
	var alarmCgDisplayer = new AlarmCgDisplayer("#_alarm_trend_id","#_alarm_trend_cg_id"),date=simHandler.newServerDate();
	
	alarmCgDisplayer.reqURL="../../sim/alarm/scopeStatisticByTime";
	var time=date.Format("yyyy-MM-dd HH:mm:ss");
	if(ct==0){
			alarmCgDisplayer.load({
				"time" : time/*"2014-03-10 00:00:00" */
			}, function(data, _this) {
				if(data){
					var _values = [], maxTime = {"year":null,"month":null,"day":null}, minTime = {"year":null,"month":null,"day":null},time;
					$.each(data,function(j,c){
						_values.push(c["total"]);
						time=c["time"];
						if (!minTime["year"]) {
							minTime["year"] = parseInt(time.substring(0, 4));
							minTime["month"] = parseInt(time.substring(5, 7));
							minTime["day"] = parseInt(time.substring(8));
						}
						maxTime["year"] = parseInt(time.substring(0, 4));
						maxTime["month"] = parseInt(time.substring(5, 7));
						maxTime["day"] = parseInt(time.substring(8));
					});
					 /*dc0=$("<div style='border-style: solid;border-color: black;border-width: thin; '></div>")*//*用于调整趋势图位置*/
					var	dc1=$('<div id="detail-trend-m-container">');
		 		 	//dc0.css({"padding-top":"35px"});
		 		 	dc1.css({"margin-top":"55px",/*"margin-left":"-20px",*//*"width":"680px",*/"height":"220px"})/*.appendTo(dc0)*/;
		 		 //	$("#_alarm_trend_cg_id").empty();
		 		 	$("#_alarm_trend_cg_id").css({/*"width":"680px",*/"height":"240px"});
		 		 	$("#_alarm_trend_cg_id").append(dc1);
					loadMTrand(dc1, _values, minTime, maxTime,function(event){//图形拖动事件处理
						
					},"告警数");
				}
			});
	}
	
	if(ct==1){
		alarmCgDisplayer.load({
			"time" : time
		},function(data, _this){
			var c2d={'categories':[],'data':[]};
			$.each(data,function(k,_val){
				c2d.categories.push(_val.time);
				c2d.data.push(_val.total);
			});
			
			loadTrendColums(_this.mainboxId,c2d,function(c,y){
				
			}
		  );
		});
	}
	
	if (ct==2) {
		alarmCgDisplayer.load({
			"time" : time
		},function(data, _this){
			if (data) {
				var $table=$("<table></table>").appendTo(_this.mainboxId),fvalues=[];
				$.each(data, function(i, c) {
					fvalues.push({time:c["time"],total:c["total"]});
				});
				for(var i=0;i<fvalues.length;i++){
					var p0=fvalues[i].time;
					for (var j = i+1; j < fvalues.length; j++) {
						var p1=fvalues[j].time;
						if(parseInt(p0.replace("-", ""))<parseInt(p1.replace("-", ""))){
							var temp=fvalues[i];
							fvalues[i]=fvalues[j];
							fvalues[j]=temp;
						}
					}
				}
				$table.datagrid({   
					singleSelect : true,
				   	data:fvalues,
				    columns:[[   
				        {field:'time',title:'时间',width:180},   
				        {field:'total',title:'数量',width:200},   
				    ]],
				    onClickRow:function(rowIndex, rowData){
				    	
				    }
				});
			}
		},true);
	}
}

(function(){
	doAlarmLevel(0);
	//去掉按设备统计
//	doAlarmDev(0);
	doAlarmTrend(0);
}());


  