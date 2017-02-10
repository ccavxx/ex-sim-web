<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="easyui-layout sim" fit="true">
	<div data-options="region:'north',collapsible:false" title="${selectDataSourceName}${title}" fit="true">
		<div class="datagrid-toolbar" style="height:30px;" >
			<div class="pull-left input-prepend input-append" style="padding:3px 5px 3px 5px" >
				<c:if test="${!empty dslist }">
				<select id="dvcAddress" style="width:117px;" ${empty bean.talCategory ? "" : "disabled"}>
					<c:forEach items="${dslist }" var="dsource">
						<option value="${dsource.auditorNodeId},${dsource.deviceIp}" ${ dsource.resourceId eq selectDataSourceId ? "selected" : "" }>${dsource.deviceIp }</option>
					</c:forEach>
				</select>
				</c:if>
			</div>
			<div class="pull-left" style="position: relative;top:3px;display:${bean.viewItem=='1,2,3'?'':'none'};">
				<div class="input-prepend input-append">
					<span class="add-on">Top(N)</span>
					<select id="talTop" style="width: 117px;padding: 0px;margin-bottom: 0px;">
						<c:forEach items="${bean.tops }" var="top">
							<option value="${top.key }" ${top.key eq bean.talTop ?"selected" : ""  }>${top.value }</option>
						</c:forEach>
					</select>
				</div> 
				<div class="input-prepend input-append" style="margin-left:3px;">
					<span class="add-on">查询时间</span>
					<input style="width: 133px;" class="cursorHand" id="talStartTime" type="text" onclick="simHandler.showDaterangepickerWin('receipt_time');" value="${bean.talStartTime }" readonly="readonly">
					<span class="add-on">-</span>
					<input style="width: 133px;" class="cursorHand" id="talEndTime" type="text" onclick="simHandler.showDaterangepickerWin('receipt_time');" value="${bean.talEndTime }" readonly="readonly">
					<button class="btn" type="button" id="receipt_time"><i class="icon-calendar"></i></button>
				</div>
			</div>
			<div id="queryBtn" class="pull-left" style="padding:3px 5px 3px 5px">
			<c:if test="${!empty dslist || !empty bean.viewItem}">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-search" onclick="assReport.query('${bean.talCategories}','${bean.mstrptid }');">查询</a>
			</c:if>
			</div>
			
			<div id="configurationBtns" class="pull-left" style="height:100%;">
				<c:if test="${!empty bean.dvctype && 'DynamicComprehensiveReport' eq bean.dvctype}">
					<a href="#" onClick="assReport.configurationShowReport();" class="export"><img title="配置显示报表" src="/img/skin/nav/sysconfig.png" /> </a>
				</c:if>
			</div>
			
			<!--<div id="assConfBtns" class="pull-left" style="height:100%;">
				<c:if test="${'comprehensiveReportIndex' ne bean.reportType}">
					<a href="#" onClick="assReport.assConfShowReport();" class="export"><img title="配置设备显示报表" src="/img/skin/nav/sysconfig.png" /> </a>
				</c:if>
			</div>-->
			
			<div id="exportBtns" class="pull-right" style="height:100%;">
				<c:if test="${'comprehensiveReportIndex' ne bean.reportType}">
					<a href="#" onClick="assReport.exportReport('${expUrl}','docx','${bean.mstrptid }','${bean.talStartTime }','${bean.talEndTime }','${bean.talTop }')" class="export"><img title="导出doc" src="/img/report/word.gif" /> </a>
					<a href="#" onClick="assReport.exportReport('${expUrl}','pdf','${bean.mstrptid }','${bean.talStartTime }','${bean.talEndTime }','${bean.talTop }')" class="export"><img title="导出Pdf" src="/img/report/pdf.gif" /> </a> 
					<a href="#" onClick="assReport.exportReport('${expUrl}','excel','${bean.mstrptid }','${bean.talStartTime }','${bean.talEndTime }','${bean.talTop }')" class="export"><img title="导出excel"src="/img/report/excel.gif" /> </a> 
					<a href="#" onClick="assReport.exportReport('${expUrl}','html','${bean.mstrptid }','${bean.talStartTime }','${bean.talEndTime }','${bean.talTop }')" class="export"><img title="导出html"src="/img/report/html.gif" /> </a>
				</c:if>
			</div>
		</div>
		<div id="chartContainer" style="clear:both;">
		    ${layout}
		</div>
		
	</div>
	
</div>
<script src="/js/sim/report/highcharts.js" type="text/javascript"></script>
<script src="/js/sim/report/report.js" type="text/javascript"></script>