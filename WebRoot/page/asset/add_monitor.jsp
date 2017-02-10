<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- 本页面组件id命名全部使用前辍ma(monitorAdd)统一命名 -->
<form id="ma_form" class="form-horizontal hgap3" style="margin-top: 5px;">
  <input id="ma_ip" type="hidden" name="ip" value="${ip}">
  <input id="ma_isJob" type="hidden" name="isJob" value='${empty monitor.timerType ? "true" : "false"}'>
  <input id="ma_dataObjectType" type="hidden" name="dataObjectType" value="${monitor.dataObjectType}">
  <input id="ma_monitorId" type="hidden" name="id" value="${monitor.resourceId}">
  <input id="ma_operation" type="hidden" name="operation" value="${operation}" initComplete="false">
 　<input id="ma_componentId" type="hidden" name="componentId" value="${monitor.componentId}">
  <input id="ma_auditorNodeId" type="hidden" name="auditorNodeId" value="${monitor.auditorNodeId}">
  <input id="ma_nodeId" type="hidden" name="collectNode" value="${monitor.nodeId}"/>    
  <input id="dsa_scanNodeId" type="hidden" value="${scanNodeId}"/>    
  <div class="control-group">
    <label class="control-label" for="ma_name"><span style="color:red;">*</span>名称：</label>
    <div class="controls">
      <input type="text" id="ma_name" value="${name}" name="name" placeholder="监视对象名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="ma_monitorType"><span style="color:red;">*</span>监视对象类型：</label>
    <div class="controls">
      <input type="hidden" id="ma_monitorTypeName" name="dataSourceTypeName">
      <select id="ma_monitorType" name="dataSourceType" 
      		  class="easyui-combotree" ${operation eq "edit" ? "readonly" : ""}
		  	  selectValue = "${monitor.securityObjectType}"
      		  data-options="height:24,width:200,url:'/sim/monitor/monitorCategory?deviceType=${deviceType}',
		  value:'${monitor.securityObjectType}',
		  onBeforeSelect:ma.beforemonitorSelect,onSelect:ma.monitorTypeClick"/>
      	<a href="javascript:void(0);" onclick="mnt.deviceTypeHelp()" ><span class="sim-icon-help" title="帮助" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="ma_collectType"><span style="color:red;">*</span>收集方式：</label>
    <div class="controls">
		<c:choose>
	    	<c:when test="${operation eq 'edit' }">
	    		<select id="ma_collectType" disabled selectValue="${monitor.collectMethod}" onchange="ma.collectTypeChangeHandler()"/>
	    		<input name="collectType" value="${monitor.collectMethod}" type="hidden">
	    	</c:when>
	    	<c:otherwise>
		      	<select id="ma_collectType" name="collectType" selectValue="${monitor.collectMethod}" onchange="ma.collectTypeChangeHandler()"/>
	    	</c:otherwise>
    	</c:choose>    
    </div>
  </div>    
  <div class="control-group">
    <label class="control-label" for="ma_enable">状态：</label>
    <div class="controls">
		<label class="checkbox" style="display:inline-block;width:50px;">
		  <input type="checkbox" id="ma_enable" name="enabled" value="1" ${empty monitor ? 'checked' : (monitor.available == 1 ? 'checked' : '')} />　启用
		</label>
		<span class="msg-box" for="enabled"></span>
    </div>
  </div>   
  <div id="ma_pollTimeDiv" class="control-group" style="display: none;">
    <label class="control-label" for="ma_duration">轮询时间：</label>
  	<div class="controls">
  		<%@include file="./poll_time.jsp" %>
    </div>
  </div>    
  <ul class="nav nav-list">
   <li class="divider"/>           
  </ul>
  <div id="ma_configParamDiv" class="hgap3">
  </div> 
</form>
<script src="/js/sim/asset/asset_detail_datasource_monitor.js"></script>
<script src="/js/sim/asset/asset_detail_add_monitor.js"></script>
<script src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/asset/form.js"></script>
