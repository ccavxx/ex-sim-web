<%@ page language="java" pageEncoding="utf-8"%>
<div id="adt_memory_trend${param.tabSeq}"/>
<script>
$(function(){
	function loadMemoryTrendData(ip,chartId){
		var time = new Date().getTime() ;
		$.getJSON("/sim/assetdetail/stateList?ip="+ip+"&stateName=MemUsedPercent&_time="+time,function(data){
			var chartData = new Array(data.length) ;
			$.each(data,function(index,record){
				var memoryUsed = record.MemUsedPercent == undefined ? 0 : record.MemUsedPercent ;
				chartData[index] = new Array(record.StartTime,memoryUsed) ;
			}) ;
			chartData.reverse() ;
			$(chartId).highcharts().series[0].setData(chartData) ;
			$(chartId).highcharts().redraw() ;
		}) ;
	}
	assetChart.drawTrendChart("#adt_memory_trend${param.tabSeq}",null,"使用率",function(){
		loadMemoryTrendData("${param.ip}","#adt_memory_trend${param.tabSeq}") ;
		createTimer(function(){
			loadMemoryTrendData("${param.ip}","#adt_memory_trend${param.tabSeq}") ;
		},20000+Math.random()*5000,${param.tabSeq}) ;
	}) ;
}) ;
</script>