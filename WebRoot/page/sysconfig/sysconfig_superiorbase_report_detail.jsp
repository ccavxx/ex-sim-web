<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="easyui-layout sim" fit="true">
	<div data-options="region:'north',collapsible:false" title="${ bean.dvctypeCnName}${title }" fit="true">
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
			<div id="paramItems" class="pull-left" style="position: relative;top:3px;display:${bean.viewItem=='1,2,3'?'':'none'};">
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
					<input class="cursorHand" style="width: 133px;" id="talStartTime" type="text" onclick="simHandler.showDaterangepickerWin('receipt_time');" value="${bean.talStartTime }" readonly="readonly">
					<span class="add-on">-</span>
					<input class="cursorHand" style="width: 133px;" id="talEndTime" type="text" onclick="simHandler.showDaterangepickerWin('receipt_time');" value="${bean.talEndTime }" readonly="readonly">
					<button class="btn" type="button" id="receipt_time"><i class="icon-calendar"></i></button>
				</div>
			</div>
			<div id="queryBtn" class="pull-left" style="padding:3px 5px 3px 5px">
			<c:if test="${!empty dslist || !empty bean.tops}">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-search" onclick="simsuperiorReport.query('${bean.talCategories}','${bean.mstrptid }');">查询</a>
			</c:if>
			</div>
			<div id="exportBtns" class="pull-right" style="height:100%;">
				<c:if test="${not empty bean.talCategory}">
					<a href="javascript:void(0)" class="easyui-linkbutton add-on" iconCls="icon-goback" style="position: relative;top:3px;float: left;" onclick="simsuperiorReport.goBack('${superiorUrl}')" >返回</a>
				</c:if> 
			</div>
		</div>
		<div id="chartContainer" style="clear:both;">
		    ${layout}
		</div>
	</div>
</div>
<script src="/js/sim/sysconfig/sysconfig_superiorHighcharts.js" type="text/javascript"></script>
<script type="text/javascript">
$(function(){
	simsuperiorReport.init() ;
});
</script>
