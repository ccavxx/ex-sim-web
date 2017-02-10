<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
	<head>
		<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
	</head>
	<body style="overflow: hidden;height: 100%">
		<form id="traceTools" class="form-inline" style="padding-left: 10px;margin: 5px 0px;">
			<fieldset>
				<input id="securityObjectType" name="deviceType" value="${sessionScope.logSearchObject.deviceType}" type="hidden">
				<input id="host" name="host" value="${sessionScope.logSearchObject.host}" type="hidden">
				<input id="queryStartDate" name="queryStartDate" value="${sessionScope.logSearchObject.queryStartDate}" type="hidden">
				<input id="queryEndDate" name="queryEndDate" value="${sessionScope.logSearchObject.queryEndDate}" type="hidden">
				<input id="group" name="group" value="${sessionScope.logSearchObject.group}" type="hidden">
				<input id="traceField" name="traceField" value="${sessionScope.logSearchObject.traceField}" type="hidden">
				<input id="traceGroupFields" name="traceGroupFields" value="" type="hidden">
				<input id="filterField" name="filterField" value="" type="hidden">
				<span style="margin-left: -10px;"><h4 style="display: inline;">分组字段</h4></span>
				<select onchange="buildIpTraceTree()" class="groupFields" name="${fd.name}" style="width: 150px;">
					<option value="">无</option>
					<c:forEach var="fd" items="${fields}">
				  		<option value="${fd.name}=${fd.name}">${fd.alias}</option>
					</c:forEach>
				</select>
			</fieldset>
		</form>
		<div id="traceTreeContainer" style="width:100%;overflow: auto;">
			<div id="logSearchResultIpTraceTree" style="width:100%;"/>
		</div>
	</body>
	<script src="/js/global/jquery-1.8.3.js"></script>
	<script src="/js/echart/echarts.js"></script>
	<script src="/js/sim/log/log_echarts.js"></script>
	<script src="/js/sim/log/log_ip_trace.js"></script>
	<script src="/js/global/bootstrap.js"></script>
	<script src="/js/global/cryptico.js"></script>
	<script src="/js/global/system.js"></script>
	<script src="/js/global/json2.js"></script>
</html>
