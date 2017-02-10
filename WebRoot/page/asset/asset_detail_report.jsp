<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- 
本页面组件id命名全部使用前辍adt_report(asset detail report)统一命名，并且附加后台传递的tabSeq参数值为后辍，避免页面组件id重复 
 注意：在资产列表页面打开资产详细时，由于可以同时打开多个资产的详细页面
 为了避免各个资产之间组件id重复，必须为当前页面中的组件的id加入一个${tabSeq}的后辍
 -->
<table style="min-width:600px;width:100%;">
	<tr>
		<td style="width:32%">
			<div id="adt_report_logCountCtn${param.tabSeq}" class="sim" >
				<div class="easyui-panel" title="日志数量统计">
					<div id="adt_report_logCount${param.tabSeq}" style="height:210px;"/>
				</div>
			</div>
		</td>
		<td style="width:32%">
			<div id="adt_report_eventAlarmCountCtn${param.tabSeq}" class="sim">
				<div class="easyui-panel" title="事件数量统计">
					<div id="adt_report_eventAlarmCount${param.tabSeq}" style="height:210px;"/>
				</div>
			</div>
		</td>
		<td style="width:34%">
			<div id="adt_report_eventCountCtn${param.tabSeq}" class="sim">
				<div class="easyui-panel" title="事件等级统计">
					<div id="adt_report_eventCount${param.tabSeq}" style="height:210px;"/>
				</div>
			</div>
		</td>
		<td style="width:18px"></td>
	</tr>
</table>
<script type="text/javascript">
$(function(){
	var log = JSON.parse('${log}');
	$('#adt_report_logCount${param.tabSeq}').css('width', $("#adt_report_eventAlarmCount${param.tabSeq}").width()-10);
	/**日志数量统计*/
	require(['echarts', 'echarts/chart/bar'], function(echarts) {
		var logNameChart = new EchartsModel().buildChart(echarts, "#adt_report_logCount${param.tabSeq}", function(){
			return option = {
				tooltip : {
					trigger: 'item'
				},
				xAxis: {
		        	type : 'category',
		        	data : log.categories
		        },
		        yAxis: {
		            type : 'value',
		            axisLabel:{
		            	 formatter:function(val){
		            		 var label;
		            		 if(val >= 1000000){
		            			 label = (val/1000000)+'m'; 
		            		 } else if(val >= 1000){
		            			 label = (val/1000)+'k'; 
		            		 } else {
		            			 label = val; 
		            		 }
		            		 return label;
		            	 }
		             }
		        },
		        grid:{
			    	x:50,
			    	y:30,
			    	x2:30,
			    	y2:30
			    },
				series: [{name:'日志数',type: 'bar',barMaxWidth:50,data:[log.series[0].y]}]
			};
		});
		logNameChart.on('hover', function(param) {
   			if (param.type == 'hover' && param.event.target) {
   				param.event.target.style.cursor='default';  
   			}
   		});
	});
	/**事件告警数量统计*/
	var eventNameData = ${eventStat.event};
	$('#adt_report_eventAlarmCount${param.tabSeq}').css('width', $("#adt_report_eventAlarmCount${param.tabSeq}").width()-10);
	require(['echarts', 'echarts/chart/bar'], function(echarts) {
		var eventNameChart = new EchartsModel().buildChart(echarts, "#adt_report_eventAlarmCount${param.tabSeq}", function(){
			return option = {
				tooltip : {
					trigger: 'item'
				},
				xAxis: {
		        	type : 'category',
		        	data : log.categories
		        },
		        yAxis: {
		            type : 'value',
		            axisLabel:{
		            	 formatter:function(val){
		            		 var label;
		            		 if(val >= 1000000){
		            			 label = (val/1000000)+'m'; 
		            		 } else if(val >= 1000){
		            			 label = (val/1000)+'k'; 
		            		 } else {
		            			 label = val; 
		            		 }
		            		 return label;
		            	 }
		             }
		        },
		        grid:{
			    	x:50,
			    	y:30,
			    	x2:30,
			    	y2:30
			    },
				series: [{name:'事件数',type: 'bar',barMaxWidth:50,data:[eventNameData] }]
			};
		});
		eventNameChart.on('hover', function(param) {
   			if (param.type == 'hover' && param.event.target) {
   				param.event.target.style.cursor='default';  
   			}
   		});
	});
	
	/**事件等级统计*/
	$('#adt_report_eventCount${param.tabSeq}').css('width', $("#adt_report_eventCount${param.tabSeq}").width()-10);
	require(['echarts', 'echarts/chart/pie'], function(echarts) {
		var eventLevelChart = new EchartsModel().buildChart(echarts, "#adt_report_eventCount${param.tabSeq}", function(){
			return option = {
				tooltip : {
					trigger: 'item'
				},
				legend : {
					x:'right',
					y:'center',
					orient:'vertical',
					data:function(){
						var temp = [];
						var list = ${eventPriorityStat};
						for ( var node in list) {
							temp.push(list[node][0]);
						}
						return temp;
					}()
				},
				series: [{
					name:'事件等级',
					type: 'pie',
					itemStyle : {
		                normal : {
		                    label : {
		                        show : false
		                    },
		                    labelLine : {
		                        show : false
		                    }
		                }
		            },
					data:function(){
						var temp = [];
						var list = ${eventPriorityStat};
						for ( var node in list) {
							temp.push({name:list[node][0], value:list[node][1] });
						}
						return temp;
					}()
				}]
			};
		});
		eventLevelChart.on('hover', function(param) {
   			if (param.type == 'hover' && param.event.target) {
   				param.event.target.style.cursor='default';  
   			}
   		});
	});
}) ;

</script>
