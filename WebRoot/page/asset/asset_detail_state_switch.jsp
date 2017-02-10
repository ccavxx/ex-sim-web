<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script src="/js/sim/asset/asset_detail_chart.js"></script>
<div class="easyui-panel" data-options="fit:true,border:false">
	<div style="padding: 5px 12px 5px 5px;">
		<div class="row-fluid" style="min-width: 800px;height: 50%;">
			<div class="span6 sim" style="height: 100%;">
				<div class="easyui-panel" title="资产状态" fit="true">
				    <div style="width: 99%;float: left;"><%@include file="state/cpu.jsp" %></div>
				</div>
			</div>
			<div class="span6 sim" style="padding: 0px 5px;height: 100%;">
				<div class="easyui-panel" title="背板使用率" fit="true" style="width: 99%;"><%@include file="state/backboard.jsp" %></div>
			</div>
		</div>
		<div class="row-fluid" style="min-width: 800px;height: 50%;">
			<div class="span6 sim" style="padding-top: 5px;">
				<div class="easyui-panel" title="CPU、内存趋势" fit="true" style="overflow: hidden;"><%@include file="state/cpu_trend.jsp" %></div>
			</div>
			<div class="span6 sim" style="padding:5px 5px 0px 5px;">
				<div class="easyui-panel" title="流量趋势" fit="true" style="overflow: hidden;"><%@include file="state/flow_trend.jsp" %></div>
			</div>
		</div>	
	</div>	
</div>

