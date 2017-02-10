<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<!-- 本页面组件id命名全部使用前辍dsa(dataSourceAdd)统一命名 -->
<style>
<!--
.form-horizontal .control-group {
	margin-bottom: 10px;
}
-->
</style>
<form id="dsa_form" class="form-horizontal" style="margin-top: 5px;">
  <input type="hidden" name="ip" value="${asset.masterIp}">
  <input type="hidden" name="ownerGroup" value="${ownerGroup}">
  <input id="dsa_isJob" type="hidden" name="isJob">
  <input id="dsa_dataObjectType" type="hidden" name="dataObjectType">
  <div class="control-group">
    <label class="control-label" for="dsa_name">日志源名称：</label>
    <div class="controls">
      <input type="text" id="dsa_name" value="${asset.name}" name="name" placeholder="日志源名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_dataSourceType">日志源类型：</label>
    <div class="controls">
      <input type="hidden" id="dsa_dataSourceTypeName" name="dataSourceTypeName">
      <select id="dsa_dataSourceType" name="dataSourceType"
      		  class="easyui-combotree"
      		  data-options="height:24,width:200,url:'/sim/datasource/dataSourceTree?deviceType=${asset.deviceType}',
      		                onBeforeSelect:dsa.beforeDataSourceSelect,onClick:dsa.dataSourceTypeClick,onHidePanel:ds.displayPathName"/>
      	
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_collectType">收集方式：</label>
    <div class="controls">
      <select id="dsa_collectType" name="collectType" onchange="dsa.collectTypeChangeHandler()"/>
    </div>
  </div>    
  <div class="control-group">
    <label class="control-label" for="dsa_nodes">收集节点：</label>
    <div class="controls">
      <input id="dsa_componentId" type="hidden" name="componentId">
      <input id="dsa_auditorNodeId" type="hidden" name="auditorNodeId">
      <select id="dsa_nodes" name="collectNode" onchange="dsa.onCollectNodeChange()"/>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_enable">日志源状态：</label>
    <div class="controls">
      	<select id="dsa_enable" name="enabled">
     		<option value="1">启用</option>
     		<option value="0">禁用</option>
     	</select>
    </div>
  </div>   
  <div class="control-group">
    <label class="control-label" for="dsa_rewrite_time">覆盖日志时间：</label>
    <div class="controls">
      	<select id="dsa_rewrite_time" name="overwriteLogTime">
     		<option value="1" selected>是</option>
     		<option value="0">否</option>
     	</select>
    </div>
  </div>
  <div id="durationDiv" class="control-group" style="display: none;">
    <label class="control-label" for="dsa_duration">活跃度：</label>
  	<div class="controls">
      <div class="input-append">
	      <input id="dsa_duration" type="text" name="duration" value="30" placeholder="活跃度" style="width:166px">
	      <span class="add-on" style="width:32px;">分钟</span>
      </div>
    </div>
  </div>
  <div id="pollTimeDiv" class="control-group" style="display: none;">
    <label class="control-label" for="dsa_duration">轮询时间：</label>
  	<div class="controls">
  		<%@include file="./poll_time.jsp" %>
    </div>
  </div>    
  <div id="speedLimitDiv" class="control-group">
    <label class="control-label" for="dsa_speed_limit">限速：</label>
    <div class="controls">
    	<div class="input-append">
	     	<select id="dsa_speed_limit" name="rate" style="width:166px">
	     		<option value="">不限</option>
	     		<option value="1000">1000</option>
	     		<option value="3000">3000</option>
	     		<option value="5000">5000</option>
	     	</select>
	     	<span class="add-on" style="width:32px;">条/秒</span>
     	</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_archiveTime">日志保存：</label>
    <div class="controls">
     	<div class="input-append">
	     	<select id="dsa_archiveTime" name="archiveTime" style="width:166px">
	     		<option value="1m">1</option>
	     		<option value="3m">3</option>
	     		<option value="6m" selected>6</option>
	     		<option value="12m">12</option>
	     		<option value="10000m">永远</option>
	     	</select>
		    <span class="add-on" style="width:32px;">个月</span>
		</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_dataSource_filter">过滤规则：</label>
    <div class="controls">
      <select id="dsa_dataSource_filter" name="ruleId" ></select>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsa_dataSource_aggregator">归并规则：</label>
    <div class="controls">
      <select id="dsa_dataSource_aggregator" name="aggregatorId" ></select>
    </div>
  </div>
  <ul class="nav nav-list">
   <li class="divider"/>           
  </ul>
  <div id="dsa_configParamDiv"/>
</form>
<script src="/js/sim/asset/asset_detail_add_datasource.js"></script>
<script src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/asset/form.js"></script>
