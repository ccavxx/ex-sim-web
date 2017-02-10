<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!-- 
本页面组件id命名全部使用前辍adt(asset detail)统一命名，并且附加后台传递的tabSeq参数值为后辍，避免页面组件id重复 
 注意：在资产列表页面打开资产详细时，由于可以同时打开多个资产的详细页面
 为了避免各个资产之间组件id重复，必须为当前页面中的组件的id加入一个${tabSeq}的后辍
 -->
<div id="adt_idConatiner${tabSeq}" class="easyui-panel noTopBorder" fit="true">
	<div class="easyui-panel" title='资产信息'
		 data-options="height:330,collapsible:true,border:false,iconCls:'icon-panel-open',noheader:true" >
		<div class="easyui-layout" fit="true" border="false" style="margin-top: -1px;">
			<div class="sim" data-options="region:'west',width:300,height:330" style="border-top:none;border-bottom:none;border-left:none;">
				<div class="easyui-tabs" data-options="tabPosition:'top',fit:true,border:false" >
					<div title="基本信息" class="noTopBorder" style="height:310px;"><%@include file="/page/asset/asset_detail_base.jsp"%></div>
					<div id="adt_datasource${tabSeq}" class="noTopBorder" title="日志源管理" href="/sim/datasource/assetDataSource?tabSeq=${tabSeq}&ip=${ip}"/>
					<div id="adt_monitor${tabSeq}" class="noTopBorder" title="监视对象管理" href="/sim/monitor/assetMonitor?tabSeq=${tabSeq}&ip=${ip}"/>
				</div>
			</div>
			<c:choose>
				<c:when test="${fn:toLowerCase(assetCategory) eq 'os'}">
					<div data-options="region:'center',border:false" class="onlyBottomBorder commonBorderColor" href="/page/asset/asset_detail_state_os.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
				</c:when>
				<c:when test="${fn:toLowerCase(assetCategory) eq 'switch'}">
					<div data-options="region:'center',border:false" class="onlyBottomBorder commonBorderColor" href="/page/asset/asset_detail_state_switch.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
				</c:when>
				<c:when test="${fn:toLowerCase(assetCategory) eq 'audit'}">
					<div data-options="region:'center',border:false" class="onlyBottomBorder commonBorderColor" href="/page/asset/asset_detail_state_audit.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
				</c:when>
				<c:otherwise>
					<div data-options="region:'center',border:false" class="onlyBottomBorder commonBorderColor" href="/page/asset/asset_detail_state_firewall.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
				</c:otherwise>
			</c:choose>
		</div>	
	</div>
	<div data-options="tabPosition:'top',border:false" class="easyui-tabs">
		<div title="端口服务" href="/page/asset/asset_detail_port.jsp?ip=${ip}&tabSeq=${tabSeq}"/>
		<div title="进程" href="/sim/assetdetail/process?ip=${ip}&tabSeq=${tabSeq}"/>
		<div title="服务列表" href="/sim/assetdetail/win32Service?ip=${ip}&tabSeq=${tabSeq}"/>
		<div title="网卡" href="/sim/assetdetail/networkCard?ip=${ip}&tabSeq=${tabSeq}"/>
		<div title="事件信息" href="/page/asset/asset_detail_event.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
		<div title="日志信息" href="/sim/assetdetail/showLog?tabSeq=${tabSeq}&ip=${ip}" />
		<c:if test="${not empty interface}">
			<div title="接口" href="/page/asset/state/interface.jsp?tabSeq=${tabSeq}&ip=${ip}"/>
		</c:if>
		<div title="报表" href="/sim/assetdetail/report?ip=${ip}&tabSeq=${tabSeq}"/>
	</div>
</div>
<script src="/js/sim/asset/asset_detail.js"></script>

