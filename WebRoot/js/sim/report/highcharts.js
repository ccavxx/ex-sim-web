var highcharts = {
		
};
var statusMonth=false;
var statusDay=false;
Date.prototype.format=function(format){
	var o={
			"M+":this.getMonth()+1,
			"d+":this.getDate(),
			"h+":this.getHours(),
			"m+":this.getMinutes(),
			"s+":this.getSeconds(),
			"q+":Math.floor((this.getMonth()+3)/3),
			"s+":this.getMilliseconds()
	};
	if(/(y+)/.test(format)){
		format=format.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length));
	}
	for(var k in o){
		if(new RegExp("("+k+")").test(format)){
			format=format.replace(RegExp.$1,RegExp.$1.length==1?o[k]:("00"+o[k]).substr((""+o[k]).length));
		}
	}
	return format;
};
highcharts.getFormatDateByLong=function(lval,pattern){
	return highcharts.getFormatDate(new Date(lval),pattern);
};

highcharts.getFormatDate=function(date,pattern){
	if(undefined==date){
		date=simHandler.newServerDate();
	}
	if(undefined==pattern){
		pattern="yyyy-MM-dd hh:mm:ss";
	}
	return date.format(pattern);
};
/**
 * 加载报表数据
 * @param url url地址
 */
highcharts.loadReportData = function(url,top,isExistTable){
	$.getJSON(encodeURI(url),{_time:new Date().getTime()},function(result){
		highcharts.showChart(result,top,isExistTable) ;
	}) ;
} ;

/**
 * 展示小主题报表
 * @param data 数据
 */
highcharts.showChart = function(data,top,isExistTable){
	if(data!=null&&undefined!=data){
		var type = data.type;	
		var subId = data.subID;
		var chartTableId = data.chartTableId;
		if(null !=chartTableId && undefined!=chartTableId){
			subId=chartTableId;
		}
		switch(type){
			case "column":
				highcharts.createBasicColumn("chart_"+subId,data.chart);
				break;
			case "line" :
				highcharts.createBasicLine("chart_"+subId,data.chart);
				break;
			case "pie" :
				highcharts.createPie("chart_"+subId,data.chart);
				break;
			case "spline" :
				var reportType=data.chart.title.toString();
				var timeType=reportType.indexOf("时趋势")>0||reportType.indexOf("时总趋势")>0?"hour":(reportType.indexOf("日趋势")>0?"day":(reportType.indexOf("月趋势")>0?"month":undefined));
				highcharts.createSpline("chart_"+subId,data.chart,timeType);
				break;	
		}
		if(isExistTable){
			highcharts.modifyTableInfo(subId,data,top,type);
		}
	}
	data=null;
};

/**
 * 创建highcharts柱状图
 * @param container 组件ID
 * @param data 数据
 */
highcharts.createBasicColumn = function(container,data){
	var spacing = data.unit==""?-10:-2;
	$("#"+container).highcharts({
        chart: {
            type: 'column',
            height:216,
            spacingLeft:spacing,
            options3d: {
                enabled: true,
                alpha:2,
                beta: 12,
                depth: 80,
                viewDistance: 25,
                frame: {
                    bottom: { size: 1, color: '#F1F1F3' },
                    back: { size: 1, color: '#F1F1F3' },
                    side: { size: 1, color: 'rgba(0,0,0,0.06)' }
                }
            }
        },
        title: {
            text: ""
        },
        credits : { // 禁止显示LOGO
			enabled : false
		},
		legend : { // 禁止显示图例
			enabled : false
		},
        xAxis: {
        	categories:data.xAxis.categories,
        	labels:{
        		enabled:false
        	}
        },
        yAxis: {
            min: 0,
            title: {
            	margin:-7,
                text: data.unit
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                '<td style="padding:0"><b>{point.y}</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointWidth: 20,
                borderWidth: 0
            }
        },//['#FFA940','#ACD25E','#FCDE3D','#CCFFFF','#94E4AE','#F4A0A0','#409FFF','#DDE6E6','#F2D8C2','#FEF5C8','#CBD8E9','#F1EAA4','#AFD8F8','#F6BD0F','#8BBA00','#FF8E46','#008E8E','#D64646','#8E468E','#588526','#B3AA00','#4169E1']
        colors:simHandler.colors,
        series: data.series
    });
	data=null;
};


/**
 * 创建highcharts饼图
 * @param container 组件ID
 * @param data 数据
 */
highcharts.createPie = function(container,data){
	$("#"+container).highcharts({
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            height:216,
            type: 'pie',
            options3d: {
				enabled: true,
                alpha: 45,
                beta: 0
            }
        },
        title: {
            text: ''
        },
        credits : { // 禁止显示LOGO
			enabled : false
		},
		legend : { // 禁止显示图例
			enabled : false
		},
        tooltip: {
        	headerFormat: '<small></small>',
    	    pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
        },//{series.name}
        plotOptions: {
            pie: {
                allowPointSelect: true,
                depth:45,
                innerSize:35,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                }
            }
        },//['#FFA940','#ACD25E','#FCDE3D','#CCFFFF','#94E4AE','#F4A0A0','#409FFF','#DDE6E6','#F2D8C2','#FEF5C8','#CBD8E9','#F1EAA4','#AFD8F8','#F6BD0F','#8BBA00','#FF8E46','#008E8E','#D64646','#8E468E','#588526','#B3AA00','#4169E1']
        colors: simHandler.colors,
        series: [data.series]
    });
	data=null;
};

/**
 * 创建highcharts折线图
 * @param container 组件ID
 * @param data 数据
 */
highcharts.createBasicLine = function(container,data){
	
	$("#"+container).highcharts({
        xAxis: data.xAxis,
        yAxis: {
            title: {
                text: data.unit
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: data.unit
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },//['#FFA940','#ACD25E','#FCDE3D','#CCFFFF','#94E4AE','#F4A0A0','#409FFF','#DDE6E6','#F2D8C2','#FEF5C8','#CBD8E9','#F1EAA4','#AFD8F8','#F6BD0F','#8BBA00','#FF8E46','#008E8E','#D64646','#8E468E','#588526','#B3AA00','#4169E1']
        colors: simHandler.colors,
        series: data.series
    });
	data=null;
};
/**
 * 创建highstock曲线图
 * @param container 组件ID
 * @param data 数据
 */
highcharts.createSpline = function(container,data,timeType){
	try{
		var min=5405267200000;
		var max=-99990;
		var dat=data.series;
		for(var i=0;i<dat.length;i++){
			var xydat=dat[i].data;
			for(var j=0;j<xydat.length;j++){
				if(min>xydat[j].x){min=xydat[j].x;}
				if(max<xydat[j].x){max=xydat[j].x;}
			}
			break;
		}
		if(max-min>24*3600*10*1000){statusMonth=true;}
		if(max-min<24*3600*1000){statusDay=true;}
	}catch(e){}
	$("#"+container).highcharts( {
		title: {
            text: ''
        },
		chart: {
			spacingTop: 10,
       	 	marginRight:10,
       	 	type: 'column',
       	 	height:220,
			backgroundColor: {
					linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
					stops: [
						[0, '#D5E1F1'],
						[0.4, '#E7EEF7'],
						[1, '#fff']
					]
				 }
	    },
	    credits : { // 禁止显示LOGO
			enabled : false
		},
        colors: simHandler.colors,
		legend : { // 禁止显示图例
			enabled : false
		},
		xAxis: {
            labels: {
                rotation:0,
                align: 'right',
                useHTML:true,
                formatter: function() {
                	//获取到刻度值
                	var tsval = this.value;
                	return foramtTimeBylong(tsval,timeType);
            	}
            },
            title: {
                text: ''
            }
        },
	    yAxis: {
	    	min:0,
            title: {
                text: ''
            }
	    },
        plotOptions: {
            spline: {
                lineWidth: 3,
                states: {
                    hover: {
                        lineWidth: 0
                    }
                },
                marker: {
                    enabled: false
                },
                pointInterval: 3600000, // one hour
                pointStart: Date.UTC(2009, 9, 6, 0, 0, 0)
            }
        },
	    tooltip: {
	    	formatter: function() {
	    		var tempname=(this.series.name==null||this.series.name==undefined||this.series.name=="Series 1"
	                    ||this.series.name=="Series 2"||this.series.name=="Series 3"
	                    	||this.series.name=="Series 4"||this.series.name=="Series 5")?"个数":(this.series.name) ;
                return foramtTimeBylong(this.x,timeType)+'<br/><span style="color:'+this.series.color+'">'+tempname +'</span>'
                		 +': <b>'+ this.y +'</b>';
            },
	    	headerFormat: '',//foramtTimeBylong("{point.x}",timeType)
	    	pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> <br/>',
	    	valueDecimals: 2
	    },//['#FFA940','#ACD25E','#FCDE3D','#CCFFFF','#94E4AE','#F4A0A0','#409FFF','#DDE6E6','#F2D8C2','#FEF5C8','#CBD8E9','#F1EAA4','#AFD8F8','#F6BD0F','#8BBA00','#FF8E46','#008E8E','#D64646','#8E468E','#588526','#B3AA00','#4169E1']
        series: data.series
    });
	data=null;
	statusMonth=false;
	statusDay=false;
};
var tempTime='';
var stringTTime='1';

function foramtTimeBylong(lval,timeType){
	try{
		if("hour"==timeType){
			return highcharts.getFormatDateByLong(lval,"hh:mm:ss");
		}else if("day"==timeType){
			return highcharts.getFormatDateByLong(lval,"hh:mm");
		}else if("month"==timeType){
			return highcharts.getFormatDateByLong(lval,"MM-dd");
		}
		var tTime=highcharts.getFormatDateByLong(lval,"MM-dd hh:mm");
		stringTTime=tTime.toString();
		if(statusMonth){
			return highcharts.getFormatDateByLong(lval,"MM-dd");
		}else if(statusDay){
			return highcharts.getFormatDateByLong(lval,"hh:mm");
		}else if(undefined==tempTime||tempTime.length<=1){
			tempTime=tTime.toString();
			return tTime;
		}else if(tempTime.substring(0, 5)==stringTTime.substring(0, 5)){
			tempTime=tTime.toString();
			return highcharts.getFormatDateByLong(lval,"hh:mm");
		}
		return tTime;
	}catch(e){
		return "";
	}
}
/**
 * 更新表格信息
 * @param moduleId 组件ID
 * @param data 数据
 */
highcharts.modifyTableInfo = function(moduleId,data,top,type){
	var table = $("#table_"+moduleId);
	table.find("thead").remove();
	table.find("tbody").remove();
	var tdata = data.table;
	var header = tdata.header;
	var sheader =null;
	if(undefined==tdata.attrsMap.onlyTable){
		sheader = highcharts.addThead(header);
	}else{
		sheader = highcharts.addOnlyTableThead(header);
	}
	var sbody = highcharts.addTbody(tdata,top,type,moduleId);
	table.append(sheader).append(sbody);
};

/**
 * 添加表格头部信息
 * @param header 头部信息
 */
highcharts.addThead = function(header){
	var tr = highcharts.addTr("center","tableHead");
	for(var i=0;i<header.length;i++){
		tr.append(highcharts.addTh("table-header",header[i]));
	}
	var header = $("<thead/>");
	header.append(tr);
	tr=null;
	return header;
};

highcharts.addOnlyTableThead = function(header){
	var tr = highcharts.addTr("center","");
	for(var i=0;i<header.length;i++){
		tr.append(highcharts.addTh("",header[i]));
	}
	var header = $("<thead/>");
	header.append(tr);
	tr=null;
	return header;
};

/**
 * 添加表格内容信息
 * @param data 表格列集内容
 * @param fields 表格列集
 */
highcharts.addTbody = function(tdata,top,type,moduleId){
	var tbody=$("<tbody/>");
	var clazz= "subjectDatagrid";
	var bodyList = tdata.bodyList;
	var len = Math.min(top,bodyList.length);
	var hasLogQueryC=false;
	for(var i=0;i<len;i++){
		if(undefined==bodyList[i].data.TOTAL)bodyList[i].data.TOTAL=0;
		var tr = highcharts.addTrContext(tdata.drill,bodyList[i].data,tdata.fields,bodyList[i].href,clazz) ;
		tbody.append(tr);
		if(type == "column" && (undefined==tdata.attrsMap.onlyTable)){
			var td = tr.find("td:first") ;
			var times=0;
			for(var j=tdata.fields.length-2;j>=0;j--){
				if(times>=1)break;
				var span = $("<span index='"+j+"' chartIndex='"+i+"' class='column-block hand' style=\"background-color:"+simHandler.colors[i]+"\"/>") ;
				span.mouseover(function(){
					var chart = $("#chart_"+moduleId).highcharts() ;
					var chartIndex = parseInt($(this).attr("chartIndex")) ;
					var index = parseInt($(this).attr("index")) ;
					chart.series[index].data[chartIndex].setState('hover');
			        chart.tooltip.refresh([chart.series[index].data[chartIndex]]);
				}) ;
				span.mouseout(function(){
					var chart = $("#chart_"+moduleId).highcharts() ;
					var chartIndex = parseInt($(this).attr("chartIndex")) ;
					var index = parseInt($(this).attr("index")) ;
					chart.series[index].data[chartIndex].setState();
					chart.tooltip.refresh([chart.series[index].data[chartIndex]]);
				});
				td.prepend(span) ;
				times++;
			}
		}
		var data=bodyList[i].data;
		if(!hasLogQueryC && undefined!=data["logQueryUrl"]){
			
			for(var j =1;j<tdata.fields.length;j++){
				var logQueryObject=null;
				try{
					logQueryObject=data[tdata.fields[j]+"LogSearchObject"];
				}catch(e){logQueryObject=null;}
				if(null!=logQueryObject){
					hasLogQueryC=true;
					break;
				}
			}
		}
	}
	if(len<tdata.total){
		tbody.append(highcharts.addMergeTd(tdata.fields.length,tdata.moreUrl));
	}
	
	if(undefined!=tdata.subSummarize && len>0 && (undefined==tdata.attrsMap.onlyTable) && hasLogQueryC){
		tbody.append(highcharts.addSubSummarizeTd(tdata.fields.length,tdata.subSummarize));
	}
	tdata=null;
	top=null;
	return tbody ;
};

/**
 * 添加表格一行内容
 * @param data 一行列集内容
 * @param fields 列集
 * @param url url地址
 * @param tdClazz td样式
 */
highcharts.addTrContext = function(drill,data,fields,url,tdClazz){
	var trStr= highcharts.addTr("","tableOddTd");
	for(var i =0;i<fields.length;i++){
		var td ;
		if(drill==true&&i==0){
			td = highcharts.addHrefTd(tdClazz, data[fields[i]], "ablue", url);
		}else{
			var logQueryObject=null;
			try{
				logQueryObject=data[fields[i]+"LogSearchObject"];
			}catch(e){}
			if(null!=logQueryObject){
				var logQueryUrl=data["logQueryUrl"];
				var eventQueryUrl=data["eventQueryUrl"];
				if(undefined !=logQueryUrl){
					td = highcharts.addLogQuery(tdClazz, data[fields[i]], "aorange", logQueryUrl,logQueryObject);
				}else if(undefined !=eventQueryUrl){
					td = highcharts.addEventQuery(tdClazz, data[fields[i]], "ared", eventQueryUrl,logQueryObject);
				}
				
			}else{
				td = highcharts.addTd(tdClazz,data[fields[i]]);
			}
		}
		trStr.append(td) ;
	}
	return trStr ;
};

/**
 * 添加一个带有超链接的td内容
 * @param clazz td样式
 * @param innerHTM td里面的内容
 * @param aclazz 超链接的样式
 * @param url url地址
 */
highcharts.addHrefTd = function(clazz,innerHTML,aclazz,url){
	return highcharts.addTd(clazz,"").append($("<a/>").attr({"title":'下钻报表'}).bind("click",function(){report.drillReport(url);}).addClass(aclazz).css("cursor","pointer").html(innerHTML)) ;
};

highcharts.addLogQuery = function(clazz,innerHTML,aclazz,url,logQueryObject){
	var logQueryParams=report.formatReportLogQueryObj(logQueryObject);
	return highcharts.addTd(clazz,"").append($("<a/>").attr({"title":'查询日志',target:"_blank",href:'/page/forward.jsp?'+logQueryParams}).addClass(aclazz).css("cursor","pointer").html(innerHTML)) ;
	//.bind("click",function(){report.reportLogQuery(url,logQueryObject);})
};

highcharts.addEventQuery = function(clazz,innerHTML,aclazz,url,eventQueryObject){
	return highcharts.addTd(clazz,"").append($("<a/>").attr({"title":'查询事件'}).bind("click",function(){report.reportEventQuery(url,eventQueryObject);}).addClass(aclazz).css("cursor","pointer").html(innerHTML)) ;
};

highcharts.addMergeTd = function(column,moreUrl){
	var trStr= highcharts.addTr("right","tableOddTd");
	trStr.append($("<td/>").attr("colspan",column).append($("<a/>").bind("click",function(){report.moreReport(moreUrl);}).addClass("").css("cursor","pointer").html("更多") ));
	return trStr;
};

highcharts.addSubSummarizeTd = function(column,content){
	var trStr= highcharts.addTr("left","tableOddTd");
	trStr.append($("<td/>").attr("colspan",column).css("cursor","pointer").html(content));
	return trStr;
};

/**
 * 添加一个tr
 * @param align tr对齐方式
 * @param clazz 样式
 */
highcharts.addTr = function(align,clazz){
	return $("<tr/>").addClass(clazz).attr("align",align) ;
};

/**
 * 添加一个td
 * @param clazz 样式
 * @param innerHTML 
 */
highcharts.addTd = function(clazz,innerHTML){
	return $("<td/>").addClass(clazz).html(innerHTML) ;
};

/**
 * 添加一个th
 * @param clazz 样式
 * @param innerHTML 
 */
highcharts.addTh = function(clazz,innerHTML){
	var span = $("<span/>").html(innerHTML);
	var div = $("<div/>").append(span);
	return $("<th/>").addClass(clazz).append(div);
};
