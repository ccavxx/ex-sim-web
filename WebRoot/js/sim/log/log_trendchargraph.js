var year;
var month;
var day;
//初始化报表数据
function initCharData(newdate, host, devicetype, nodeid) {
	$.ajax({
		url : '/sim/logSearch/getLogChart?date=' + newdate + "&host=" + host + "&deviceType=" + devicetype + "&nodeId=" + nodeid,
		type : 'post',
		dataType : 'json',
		async : false,
		success : function(resultData) {
				var logCountJson = resultData.logCountJson;
				var bardata =[]; 
				var barxaix = Array();
				var piexaix = Array();
				var barSeries = [];
				var pieCount = [];
				var pieSize = [];
				var barSize = [];
				var maxSize = 0;
				var result = [];
				if(logCountJson != null && logCountJson != "") {
					for(var n = 0; n < logCountJson.length; n++) {
						if(maxSize < logCountJson[n].logSize2){
							maxSize = logCountJson[n].logSize2
						}
					}
					//根据最大值确定图表用什么单位
					result = formatterUtil(maxSize);
					for(var n = 0; n < logCountJson.length; n++) {
						barxaix[n] = logCountJson[n].deviceTypeName;
						piexaix[2*n] = logCountJson[n].deviceTypeName + "大小";
						piexaix[2*n+1] = logCountJson[n].deviceTypeName + "数量";
						pieCount.push({value:logCountJson[n].logCount,name:piexaix[2*n+1]});
						pieSize.push({value:(logCountJson[n].logSize2/result[0]).toFixed(2),name:piexaix[2*n]});
						barSeries.push(logCountJson[n].logCount);
						barSize.push((logCountJson[n].logSize2/result[0]).toFixed(2));
					}
				} else {
					logCountJson = [];
					result[1] = "";
				}
				bardata.push({name:'日志数量',type: 'bar',barMaxWidth:30,yAxisIndex: 0,data:barSeries});
				bardata.push({name:'日志大小',type: 'bar',barMaxWidth:30,yAxisIndex: 1,data:barSize});
//				日志概要列表
				showLogTable(logCountJson);
//				日志概要柱状图
				showLogBar(bardata, barxaix,result[1],piexaix, pieCount,pieSize);
//				日志概要饼状图
//				showLogPie(bardata, barxaix,result[1],piexaix, pieCount,pieSize);
				
				var data = resultData.partitions;
				var xaixs = new  Array();
				var logData= [];
				var countseries = [];
				var logsizeseries = [] ;
				var maxSize2=0;
				for(var i = 0; i < data.length; i++) {
					if(maxSize2 < data[i].logSize){
						maxSize2 = data[i].logSize;
					}
				}
				var result2 = formatterUtil(maxSize2);
				for(var i = 0; i < data.length; i++) {
					xaixs[i] = data[i].name;
					countseries[i] = parseInt(data[i].count);
					logsizeseries[i] = (data[i].logSize/result2[0]).toFixed(2);
				}
				logData.push({name:'日志数量',type:'line',yAxisIndex: 0,data:countseries});
				logData.push({name:'日志大小',type:'bar',yAxisIndex: 1,
								itemStyle: {
									normal: {color: function(params) {return '#B5C334'}}
								},
								data:logsizeseries
							});
				//日志统计
				showTable(data, newdate);
				showCharts('log_size_chart',xaixs, logData,result2[1]);
				$('#log_stic').tabs({    
				    onSelect:function(title){    
				    	if(title == '日志概要图形统计' ){
				    		//日志概要列表
							showLogTable(logCountJson);
							//日志概要柱状图
							showLogBar(bardata, barxaix,result[1],piexaix, pieCount,pieSize);
//							showLogPie(bardata, barxaix,result[1],piexaix, pieCount,pieSize);
				    	}
			            if(title == '日志大小数量趋势图' ){
			            	showTable(data, newdate);
							showCharts('log_size_chart',xaixs, logData,result2[1]);
				    	}
				    }    
				}); 
			}
	
		});
}
function searchLogData(){
	 //判断月份选择的所有还是固定月份
	 var search_month = $("#logmonth").combobox("getValue");
	 var search_year = $("#logyear").combobox("getValue");
	 var search_day = $("#logday").combobox("getValue");
	 var search_newdate = "";
	 if(search_month == 0){
	   search_newdate = search_year;
	 }else{
	   search_month = parseInt(search_month) < 10 ? "0" + search_month : search_month;
	   if(search_day == 0){
		 search_newdate = search_year + "-" + search_month;
	   }else{
		 search_newdate = search_year + "-" + search_month + "-" + search_day;
	   }
	 }
	 var deviceType = "";
	 var nodeid = "";
	 var host = "";
	 var logTree= $('#logdata').combotree('tree');
	 var node= logTree.tree('getSelected');	
	 if(node != "" && node != null){
		 deviceType = node.attributes.deviceType;
		 if(deviceType == "" || deviceType == "ALL/ALL/Default"){
			  nodeid ="";
			  host = "127.0.0.1";
		 }else{
			 if(node.attributes.host == undefined){
				 host = "";
			 }else{
				 host = node.attributes.host;
			 }
			 nodeid = node.attributes.nodeId;
		 }
	 }else{
		  nodeid = "";
		  host = "127.0.0.1";
		  deviceType = "ALL/ALL/Default";
	 }
	 initCharData(search_newdate,host,deviceType,nodeid);
}

//日志概要列表
function showLogTable(data){
	var treeGriddata = [];
	var totalFormatcount = 0;
	var totalLogSize = 0;
	var i = 0
	for(;i < data.length;i++){
		var deviceTypeName = data[i].deviceTypeName;
		var formatcount = data[i].logCount;
		totalFormatcount += formatcount;
		var logSize = data[i].logSize2;
		totalLogSize += logSize;
		var childrenData = [];
		$.each(data[i].children,function(index,item){
			childrenData.push({id:10*i+index,deviceTypeName:item.deviceTypeName,formatcount:countFormatter(item.logCount,1),logSize:bytesFormatter(item.logSize2,1)});
		})
		treeGriddata.push({id:i,deviceTypeName:deviceTypeName,formatcount:countFormatter(formatcount,1),logSize: bytesFormatter(logSize,1),children:childrenData});
	}
	if(i > 1){
		treeGriddata.push({id:i,deviceTypeName:"总计",formatcount:countFormatter(totalFormatcount,1),logSize: bytesFormatter(totalLogSize,1)});
	}
	$("#logtotal_table").treegrid({
		idField:'id',
		treeField:'deviceTypeName',
		border:false,
		columns:[[
		            {field:'deviceTypeName',title:'名称',width:175},
		            {field:'formatcount',title:'数量(条)',width:85},
		            {field:'logSize',title:'大小',width:85},
		        ]],
		data:treeGriddata
	});
}

//日志概要饼状图
function showLogPie(seriesdata,barxaix,util,piexaix,pieCount,pieSize){
	option = {
		    tooltip : {
		        trigger: 'item',
		        formatter: "{a} <br/>{b} : {c} ({d}%)"
		    },
		    legend: {
		        orient : 'vertical',
		        x : 'left',
		        data:piexaix
		    },
		    toolbox: {
		        show : true,
		        feature : {
		        	barSrc : {
		                show : true,	
		                title : '柱状图',
		                icon : 'image://../../img/icons/bar.png',
		                onclick : function (){
		                	showLogBar(seriesdata,barxaix,util,piexaix,pieCount,pieSize);
		                }
		            },
		            saveAsImage : {show: true}
		        }
		    },
		    series : [
		        {
		            type:'pie',
		            selectedMode: 'single',
		            radius : [0, 70],
		            x: '20%',
		            width: '40%',
		            funnelAlign: 'right',
		            itemStyle : {
		                normal : {
		                    label : {show:false},
		                    labelLine : {show : false}
		                }
		            },
		            data:pieCount
		        },
		        {
		            type:'pie',
		            itemStyle : {
		                normal : {
		                    label : {show:false},
		                    labelLine : {show : false}
		                }
		            },
		            radius : [100, 140],
		            x: '60%',
		            width: '35%',
		            funnelAlign: 'left',
		            data:pieSize
		        }
		    ]
		};
	log_echarts.init("log_chart", option);                    
}

//日志概要柱状图
function showLogBar(seriesdata,barxaix,util,piexaix,pieCount,pieSize){
    var option = {
        tooltip: {trigger: 'axis'},
        toolbox: {
		       show : true,
		       feature : {
		        	pieSrc : {
		                show : true,
		                title : '饼状图',
		                icon : 'image://../../img/icons/pie.png',
		                onclick : function (){
		                	showLogPie(seriesdata,barxaix,util,piexaix,pieCount,pieSize);
		                }
		            },
		            saveAsImage : {show: true}
		        }
		    },
	    grid:{
       	 y:100
       	},
        legend: {
        	y:'bottom',
            data:['日志数量','日志大小']
        },
        xAxis: {
            data: barxaix
        },
        yAxis : [
 		         {
 		             name : '数量',
 		             axisLabel : {
 		            	 formatter: '{value}'
 		             }
 		         },{
 		            name : '大小',
 		            axisLabel : {
 		                formatter: '{value}'+util
 		            }
 		        }
 		    ],
 		   noDataLoadingOption: {
               text: '暂无数据',
               effect: 'bubble',
               effectOption: {
                   effect: {
                       n: 0
                   }
               }
 		   },
        series:seriesdata
    };
	log_echarts.init("log_chart", option);
}
//日志大小数量列表
function showTable(data,newdate){
	$('#log_datagrid_id').datagrid({ 
		    columns:[[
		              {field:'name',title:'日期',width:200},
		              {field:'count',title:'数量(条)',width:100,formatter:function(val,rec){
		            	  if(parseInt(val) == 0){
			            		 return 0;
			            	  }else{
			            		  return countFormatter(val, 1);
			            	  }
			              }},
		              {field:'logSize',title:'大小',width:100,formatter:function(val,rec){
		            	  if(parseInt(val) == 0){
		            		 return 0;
		            	  }else{
		            		  return bytesFormatter(val, 1);
		            	  }
		              }}
		            ]],
			data:data,
			fit:true,
			fitColumns:true,
			singleSelect:true,
			border:true,
			loadMsg:'数据加载中请稍后'
	});
}
//日志大小数量趋势图
function showCharts(container,xaxis, seriesdata,util) {
	option = {
		    tooltip : {
		       trigger: 'axis'
		    },
		    toolbox: {
		       show : true,
		       feature : {
		           magicType: {show: true, type: ['line', 'bar']},
		           saveAsImage : {show: true}
		       }
		    },
		    calculable : true,
		    legend: {
		    	y:'bottom',
		        data:['日志数量','日志大小']
		    },
		    xAxis : [
		        {
		            type : 'category',
		            data : xaxis
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value',
		            name : '数量',
		            axisLabel : {
		                formatter: '{value}'
		            }
		        },
		        {
		            type : 'value',
		            name : '大小',
		            axisLabel : {
		                formatter: '{value}'+util
		            }
		        }
		    ],
		    series : seriesdata
		};
	log_echarts.init("log_size_chart", option);
}

//格式化日志大小
function formatterLogSize(value,row,index){
	return bytesFormatter(value, 1);
}
//获取最近五年时间
function getLogYear(){
	var year_data = [];
	for(var i = 0 ; i < 5 ; i++){
		var newyear = parseInt(year) - parseInt(i);
		if(i == 0){
			year_data.push({label:newyear,value:newyear + "年",selected:true});
		}else{
			year_data.push({label:newyear,value:newyear + "年"});
		}
	}
	$("#logyear").combobox({
		 valueField: 'label',
		 textField: 'value',
		 data:year_data,
		 height:24,
		 panelHeight:145,
		 editable:false
	});
	var month_data = [];
	for(var i = 0 ; i < 13 ; i++){
		if(i == 0){
			month_data.push({label:i,value:"所有"});
		}
		else{
			if(i == month){
				month_data.push({label:i,value:(i + "月"),selected:true});
			}else{
				month_data.push({label:i,value:(i + "月")});
			}
		}
	}
	var selected_year;
	var selected_month;
	$("#logmonth").combobox({
		 valueField: 'label',
		 textField: 'value',
		 data:month_data,
		 height:24,
		 editable:false,
		 onSelect:function(rec){
			 if(rec.label == "0"){
				 $("#logday_div").hide();
			 }else{
				 $("#logday_div").show();
				  selected_year = $("#logyear").combobox("getValue");
				  selected_month = $("#logmonth").combobox("getValue");
				 initDay(selected_year,selected_month);
			 }
		 }
	});
	 selected_year = $("#logyear").combobox("getValue");
	 selected_month = $("#logmonth").combobox("getValue");
	 initDay(selected_year,selected_month);
}
function initDay(year,month){
	var day_data = [];
	var totalday = new Date(Date.UTC(year,month, 0)).getUTCDate();
	for(var j = 0 ; j <= totalday ; j++){
		if(j == 0){
			day_data.push({label:j,value:"所有"});
		}else{
				day_data.push({label:(j < 10 ? ("0" + j) : j),value:(j + "日")});
		}
	}
	$("#logday").combobox({
		 valueField: 'label',
		 textField: 'value',
		 data:day_data,
		 height:24,
		 editable:false
	});
}
$(function () {
	//初始化年月份
	 var nowdate = simHandler.newServerDate();
	 year = nowdate.getFullYear();
     month = parseInt(nowdate.getMonth()+1);
     day = nowdate.getDate();
	 getLogYear();
	 var date = month < 10 ? (year + "-" + "0" + month) : date = year + "-" + month;
	 
	//初始化报表数据
	initCharData(date,'127.0.0.1','ALL/ALL/Default','');
	//日志摘要中  选择不同日志源自动调用查询方法
	$('#logdata').combotree({
		onChange : function(a,b){
			searchLogData('');
		}
	});
});
//格式化echarts图表y坐标单位
function formatterUtil(maxSize){
	var result = [];
	if(maxSize < 1024){
		result[0] = 1;
		result[1] = "B";
	}else if(maxSize<1024*1024){
		result[0] = 1024;
		result[1] = "KB";
	}else if(maxSize<(1024*1024*1024)){
		result[0] = 1024*1024;
		result[1] = "MB";
	}else if(maxSize<(1024*1024*1024*1024)){
		result[0] = 1024*1024*1024;
		result[1] = "GB";
	}else{
		result[0] = 1024*1024*1024*1024;
		result[1] = "TB";
	}
	return result;
}
//zhu_zengwen
function exportReportWord(){
	var searchCondition = getSearchCondition() ;
	var url = "/sim/logSearch/exportQueryResultWord?date="+searchCondition.search_newdate+
												  "&deviceType="+searchCondition.deviceType+
												  "&selectNodeText="+searchCondition.selectNodeText+
												  "&nodeid="+searchCondition.nodeid+
												  "&host="+searchCondition.host;
	window.open(encodeURI(url)) ;
};
function getSearchCondition(){
	 //判断月份选择的所有还是固定月份
	 var searchCondition = new Object() ;
	 var search_month = $("#logmonth").combobox("getValue");
	 var search_year = $("#logyear").combobox("getValue");
	 var search_day = $("#logday").combobox("getValue");
	 var search_newdate = "";
	 if(search_month == 0){
	   search_newdate = search_year;
	 }else{
	   search_month = parseInt(search_month) < 10 ? "0" + search_month : search_month;
	   if(search_day == 0){
		 search_newdate = search_year + "-" + search_month;
	   }else{
		 search_newdate = search_year + "-" + search_month + "-" + search_day;
	   }
	 }
	 searchCondition.search_newdate = search_newdate ;
	 var deviceType = "";
	 var nodeid = "";
	 var host = "";
	 var logTree= $('#logdata').combotree('tree');
	 var node= logTree.tree('getSelected');	
	 var selectNodeText = "所有设备";
	 if(node != "" && node != null){
		 deviceType = node.attributes.deviceType;
		 if(node.attributes.host != undefined){
			 host = node.attributes.host ;
		 }
		 selectNodeText = node.text;
	 }
	 searchCondition.deviceType = deviceType ;
	 searchCondition.nodeid = nodeid ;
	 searchCondition.host = host ;
	 searchCondition.selectNodeText = selectNodeText ;
	 return searchCondition ;
}
