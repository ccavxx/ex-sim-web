<!-- 资产连接数信息 -->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script type="text/javascript" src="/js/flipcountdown/jquery.flipcountdown.js"></script>
<link rel="stylesheet" type="text/css" href="/js/flipcountdown/jquery.flipcountdown.css" />
<div id="adt_sessions${param.tabSeq}" style="position:relative;"/>
<script>
function loadSessionData(ip, id) {
	assetChart.getChartData(ip, "sessions", function(result) {
		if(result && result.status) {
			if(!$(id).hasClass("xdsoft")){
				$(id).html("") ;
			}
			$(id).css({"text-align" : "center", "top" : "30%"}).flipcountdown({
				tick : function() {
					return parseInt(result.result);
				}
			});
		} else {
			var msg = "未取得信息！";
			if(result && result.message != null) {
				msg = result.message;
			}
			$(id).css({"text-align" : "left", "top" : "0%"}).html("<div class='alert alert-warning' role='alert'><h4>提示</h4>" + msg + "</div>");
		}
	}) ;
}
$(function() {
	loadSessionData("${param.ip}", "#adt_sessions${param.tabSeq}");
	createTimer(function() {
		loadSessionData("${param.ip}", "#adt_sessions${param.tabSeq}");
	}, 15000, ${param.tabSeq});
});
</script>