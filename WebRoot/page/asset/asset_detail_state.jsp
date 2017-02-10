<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script src="/js/sim/asset/asset_detail_chart.js"></script>
<div class="easyui-panel" data-options="fit:true,border:false">
	<div class="row-fluid" style="min-width: 800px;">
		<div class="span3">
			<%@include file="state/cpu.jsp" %>
		</div>
		<div class="span3">
			<%@include file="state/memory.jsp" %>
		</div>
		<div class="span6">
			<%@include file="state/disk.jsp" %>
		</div>
	</div>
	<div class="row-fluid" style="min-width: 800px;">
		<div class="span3">
			<%@include file="state/interface.jsp" %>
		</div>
		<div class="span3">
			<%@include file="state/sessions.jsp" %>
		</div>
		<div class="span6">
			<%@include file="state/backboard.jsp" %>
		</div>
	</div>
	<div class="row-fluid" style="min-width: 800px;padding-top: 20px">
		<div class="span6">
			<div id="adt_cpu_trend${param.tabSeq}"/>
		</div>
		<div class="span6">
			<div id="adt_memory_trend${param.tabSeq}"/>
		</div>
	</div>
</div>

