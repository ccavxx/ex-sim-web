<%@ page language="java" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style>
.table td,.table th{vertical-align:middle;text-align:center;}
.borderNoTable td{border:none;}
.borderNoTable .progressbar-value,.borderNoTable .progressbar{-webkit-border-radius:0;border-radius:0;}
</style>

<div id="superiorListContainerId" style="padding:0;overflow:auto;" >
	<table class="table" style="border:1px solid #dddddd;" >
		<thead>
			<tr>
				<th style="width:50px;">名称</th>
				<th style="width:35px;">状态</th>
				<th style="width:80px;">IP地址</th>
				<th style="width:100px;">CPU</th>
				<th style="width:100px;">内存</th>
				<th style="width:100px;">日志流量</th>
				<th style="width:120px;">磁盘使用率</th>
				<th style="width:80px;">资产数量</th>
				<%--<th style="width:80px;">不在线资产数量</th>
				--%><%-- <th style="width:50px;">告警数量</th> --%>
				<th style="width:60px;">事件数量</th>
				<th style="width:190px;">操作</th>
			</tr>
		</thead>
		<tbody id="tbody_for_add">
			<!-- 华丽的分割线-start{ -->
			<tr><td colspan="11" style="padding:0;"></td></tr>
			<!-- 华丽的分割线-end} -->
		</tbody>
	</table>
	<div>
		<div id="horizonPaginationText" style="text-align:right;padding-right:20px;"></div>
		<div style="text-align:center">
			<div class="pagination pagination-small">
			  <ul id="horizonPagination"></ul>
			</div>
		</div>
	</div>
</div>
<script>
var horizonPaginationPageNo = "${pageNo}";
var horizonPaginationPageSize = "${pageSize}";
</script>
<script type="text/javascript" src="/js/sim/sysconfig/sysconfig_superiorList.js" />
