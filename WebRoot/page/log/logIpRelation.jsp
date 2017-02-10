<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<html>
<body style="overflow: hidden;">
	<form id="exportExcel_IPRelation" style="display: none;margin: 0px;padding: 0px" >
		<input id="securityObjectType" name="deviceType" value="${sessionScope.logSearchObject.deviceType}" type="hidden">
		<input id="host" name="host" value="${sessionScope.logSearchObject.host}" type="hidden">
		<input id="queryStartDate" name="queryStartDate" value="${sessionScope.logSearchObject.queryStartDate}" type="hidden">
		<input id="queryEndDate" name="queryEndDate" value="${sessionScope.logSearchObject.queryEndDate}" type="hidden">
		<input id="group" name="group" value="${sessionScope.logSearchObject.group}" type="hidden">
	</form>
	<div style="width:100%;height:99%;overflow: auto;">
		<div id="logSearchResultIpRelationTree" style="width:100%;height:100%;"/>
	</div>
</body>
<script src="/js/global/jquery-1.8.3.js"></script>
<script src="/js/echart/echarts.js"></script>
<script src="/js/sim/log/log_echarts.js"></script>
<script src="/js/sim/log/log_ip_relation.js"></script>
<script src="/js/global/cryptico.js"></script>
<script src="/js/global/system.js"></script>
<script src="/js/global/json2.js"></script>
</html>
