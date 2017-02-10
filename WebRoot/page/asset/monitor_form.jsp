<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- 本页面组件id命名全部使用前辍mf(monitorForm)统一命名 -->
<form id="mf_form" class="form-horizontal hgap3" style="margin-top: 5px;">
  <input id="mf_isJob" type="hidden" name="isJob" value='${empty monitor.timerType ? "true" : "false"}'>
  <input id="mf_dataObjectType" type="hidden" name="dataObjectType" value="${monitor.dataObjectType}">
  <input id="mf_monitorId" type="hidden" name="id" value="${monitor.resourceId}">
  <input id="mf_operation" type="hidden" name="operation" value="${operation}" initComplete="false">
 　<input id="mf_componentId" type="hidden" name="componentId" value="${monitor.componentId}">
  <input id="mf_auditorNodeId" type="hidden" name="auditorNodeId" value="${monitor.auditorNodeId}">
  <input id="mf_nodeId" type="hidden" name="collectNode" value="${monitor.nodeId}"/>
  <input id="mf_scanNodeId" type="hidden" value="${scanNodeId}"/>
  <div class="control-group">
    <label class="control-label" for="mf_ip"><span style="color:red;">*</span>IP地址：</label>
    <div class="controls">
      <select id="mf_ip" name="ip" class="easyui-combobox" ${operation eq "edit" ? "readonly" : ""}
          data-options="height:24,width:200,url:'/sim/assetlist/getAllAsset?type=monitor',value:'${ip}',
          				onSelect:mf.ipClick,valueField:'value',textField:'value'"/>
      <a href="javascript:void(0)"><span class="sim-icon-help" title="只允许输入下拉列表中的IP地址" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="mf_name"><span style="color:red;">*</span>监视对象名称：</label>
    <div class="controls">
      <input type="text" id="mf_name" value="${name}" name="name" placeholder="监视对象名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="mf_monitorType"><span style="color:red;">*</span>监视对象类型：</label>
    <div class="controls">
      <input type="hidden" id="mf_monitorTypeName" name="dataSourceTypeName">
      <select id="mf_monitorType" name="dataSourceType" 
      		  class="easyui-combotree" ${operation eq "edit" ? "readonly" : ""}
		  	  selectValue = "${monitor.securityObjectType}"
      		  data-options="height:24,width:200,url:'/sim/monitor/monitorCategory?deviceType=${deviceType}',
		  value:'${monitor.securityObjectType}',
		  onBeforeSelect:mf.beforemonitorSelect,onSelect:mf.monitorTypeClick"/>
      	<a href="javascript:void(0);" onclick="mf.deviceTypeHelp()" ><span class="sim-icon-help" title="帮助" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="mf_collectType"><span style="color:red;">*</span>收集方式：</label>
    <div class="controls">
		<c:choose>
	    	<c:when test="${operation eq 'edit' }">
	    		<select id="mf_collectType" disabled selectValue="${monitor.collectMethod}" onchange="mf.collectTypeChangeHandler()"/>
	    		<input name="collectType" value="${monitor.collectMethod}" type="hidden">
	    	</c:when>
	    	<c:otherwise>
		      	<select id="mf_collectType" name="collectType" selectValue="${monitor.collectMethod}" onchange="mf.collectTypeChangeHandler()"/>
	    	</c:otherwise>
    	</c:choose>    
    </div>
  </div>    
  <div class="control-group">
    <label class="control-label" for="mf_enable">状态：</label>
    <div class="controls">
		<label class="checkbox" style="display:inline-block;width:50px;">
		  <input type="checkbox" id="mf_enable" name="enabled" value="1" ${empty monitor ? 'checked' : (monitor.available == 1 ? 'checked' : '')} />　启用
		</label>
		<span class="msg-box" for="enabled"></span>
    </div>
  </div>   
  <div id="mf_pollTimeDiv" class="control-group" style="display: none;">
    <label class="control-label" for="mf_duration">轮询时间：</label>
  	<div class="controls">
  		<%@include file="./poll_time.jsp" %>
    </div>
  </div>    
  <ul class="nav nav-list">
   <li class="divider"/>           
  </ul>
  <div id="mf_configParamDiv" class="hgap3">
  </div> 
</form>
<script src="/js/sim/asset/asset_detail_datasource_monitor.js"></script>
<script src="/js/sim/asset/monitor_form.js"></script>
<script src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/asset/form.js"></script>
