<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link type="text/css" rel="stylesheet" href="/css/report.css"/>
<div class="easyui-layout sim" fit="true">
	<div data-options="region:'north',collapsible:false,height:63" title="${title}" style="overflow: hidden">
		<div class="datagrid-toolbar" style="height:30px;">
			<div class="pull-left input-prepend input-append" style="padding:3px 5px 0px 5px" >
				<select id="dvcAddress" style="width: 133px;" disabled>
					<c:forEach items="${dslist }" var="dsource">
						<option value="${dsource.nodeId},${dsource.deviceIp }" >${dsource.deviceIp }</option>
					</c:forEach>
				</select>
			</div>
			<div id="paramItems" class="pull-left" style="margin-left:8px;display:none;">
				<form id="form_more_report" method="post">  
		    		<div class="input-prepend input-append">
						${viewParamItem }
					</div>
					<div class="input-prepend input-append">
						<span class="add-on">查询时间</span>
						<input class="cursorHand" style="width: 133px;" id="talStartTime" type="text" value="${bean.talStartTime }" readonly="readonly">
						<span class="add-on">-</span>
						<input class="cursorHand" style="width: 133px;" id="talEndTime" type="text"  value="${bean.talEndTime }" readonly="readonly">
						<button class="btn" type="button" id="receipt_time"><i class="icon-calendar"></i></button>
					</div>
					<div class="input-prepend input-append" style="margin-left: 5px;">
					<a href="javascript:void(0)" class="easyui-linkbutton" class="add-on" onclick="simsuperiorReport.moreRptQuery('${moreUrl}');">查询</a>
					</div>
				</form>
			</div>
			<div id="exportBtns" class="pull-right">
				<a href="javascript:void(0)" class="easyui-linkbutton add-on" iconCls='icon-goback' onclick="simsuperiorReport.goBack('${goUrl}')" >返回</a>
			</div>
		</div>
	</div>
	<div data-options="region:'center'" class="sim" >
		<table id="moreReport" />
	</div>
</div>
<script type="text/javascript">
	$(function(){
		$("#moreReport").datagrid({
		   	fit:true,
		   	striped:true,
		   	scrollbarSize:0,
		   	border:false,
			columns:[${tableOptions.columns}],
			data:${tableOptions.data},
			pagination:false,
			pageNumber:${pageIndex},
			pageSize:${bean.pagesize}
		});
	});
</script>

