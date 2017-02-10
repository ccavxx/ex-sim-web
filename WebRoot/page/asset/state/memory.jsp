<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<div id="adt_memory${param.tabSeq}"/>
<script>

function loadMemoryData(ip,id){
	assetChart.getChartData(ip,"memory",function(result){
		if(result){
			var data = new Array() ;
			if(result.status){
				data.push(result.result.toFixed(1)) ;
			}else{
				data.push(0) ;
			}
			var memoryChart = $(id).highcharts() ;
			memoryChart.series[0].points[0].update(data) ;
			memoryChart.redraw() ;
		}
	}) ;
}
$(function(){
	assetChart.drawSolidGauge("#adt_memory${param.tabSeq}","内存使用率",function(){
		var ip = "${param.ip}" ;
		var chartId = "#adt_memory${param.tabSeq}";
		loadMemoryData(ip,chartId) ;
		createTimer(function(){
			loadMemoryData(ip,chartId) ;
		},35000,${param.tabSeq}) ;
	}) ;
}) ;

</script>