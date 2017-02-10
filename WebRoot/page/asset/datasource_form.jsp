<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 本页面组件id命名全部使用前辍dsf(dataSourceForm)统一命名 -->
<form id="dsf_form" class="form-horizontal hgap3" style="margin-top: 5px;">
  
  <input id="dsf_isJob" type="hidden" name="isJob" value='${empty dataSource.timerType ? "true" : "false"}'>
  <input id="dsf_dataObjectType" type="hidden" name="dataObjectType" value="${dataSource.dataObjectType}">
  <input id="dsf_dataSourceId" type="hidden" name="id" value="${dataSource.resourceId}">
  <input id="dsf_operation" type="hidden" name="operation" value="${operation}" initComplete="false">
 　<input id="dsf_componentId" type="hidden" name="componentId" value="${dataSource.componentId}">
  <input id="dsf_auditorNodeId" type="hidden" name="auditorNodeId" value="${dataSource.auditorNodeId}">
  <input id="dsf_nodeId" type="hidden" name="collectNode" value="${dataSource.nodeId}"/>  
  <input id="dsf_scanNodeId" type="hidden" value="${scanNodeId}"/>  
  
  <div class="control-group">
    <label class="control-label" for="dsf_ip"><span style="color:red;">*</span>IP地址：</label>
    <div class="controls">
      <select id="dsf_ip" name="ip" class="easyui-combobox" ${operation eq "edit" ? "readonly" : ""}
          data-options="height:24,width:200,url:'/sim/assetlist/getAllAsset',value:'${ip}',
          				onSelect:dsf.ipClick,valueField:'value',textField:'value'"/>
      <a href="javascript:void(0)"><span class="sim-icon-help" title="只允许输入下拉列表中的IP地址" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_name"><span style="color:red;">*</span>日志源名称：</label>
    <div class="controls">
      <input type="text" id="dsf_name" value="${name}" name="name" placeholder="日志源名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_dataSourceType"><span style="color:red;">*</span>日志源类型：</label>
    <div class="controls">
      <input type="hidden" id="dsf_dataSourceTypeName" name="dataSourceTypeName" >
      <select id="dsf_dataSourceType" name="dataSourceType" ${operation eq "edit" ? "readonly" : ""}
      		  class="easyui-combotree"
      		  selectValue = "${dataSource.securityObjectType}"
      		  data-options="height:24,width:200,url:'/sim/datasource/dataSourceTree?deviceType=${deviceType}',
      		                value:'${dataSource.securityObjectType}',
      		                onBeforeSelect:dsf.beforeDataSourceSelect,onSelect:dsf.dataSourceTypeClick,
      		                onHidePanel:dsf.displayPathName"/>
      <a href="javascript:void(0);" onclick="dsf.deviceTypeHelp()" ><span class="sim-icon-help" title="帮助" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_collectType"><span style="color:red;">*</span>收集方式：</label>
    <div class="controls">
    	<c:choose>
	    	<c:when test="${operation eq 'edit' }">
	    		<select id="dsf_collectType" disabled selectValue="${dataSource.collectMethod}" onchange="dsf.collectTypeChangeHandler()"/>
	    		<input name="collectType" value="${dataSource.collectMethod}" type="hidden">
	    	</c:when>
	    	<c:otherwise>
		      	<select id="dsf_collectType" name="collectType" selectValue="${dataSource.collectMethod}" onchange="dsf.collectTypeChangeHandler()"/>
	    	</c:otherwise>
    	</c:choose>
    </div>
  </div>    
  <div class="control-group">
    <label class="control-label" for="dsf_enable">日志源状态：</label>
    <div class="controls">
		<label class="checkbox" style="display:inline-block;width:50px;">
		  <input type="checkbox" id="dsf_enable" name="enabled" value="1" ${empty dataSource ? 'checked' : (dataSource.available == 1 ? 'checked' : '')} />　启用
		</label>
		<span class="msg-box" for="enabled"></span>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_save_raw_log">存储原始日志：</label>
    <div class="controls">
      	<select id="dsf_save_raw_log" name="saveRawLog">
     		<option value="1" ${(empty dataSource or dataSource.saveRawLog == 1 )? "selected" : ""}>是</option>
     		<option value="0" ${dataSource.saveRawLog == 0 ? "selected" : ""}>否</option>
     	</select>
    </div>
  </div>     
  <div class="control-group">
    <label class="control-label" for="dsf_rewrite_time">覆盖日志时间：</label>
    <div class="controls">
      	<select id="dsf_rewrite_time" name="overwriteLogTime">
     		<option value="1" ${dataSource.overwriteEventTime == 1 ? "selected" : ""}>是</option>
     		<option value="0" ${(empty dataSource or dataSource.overwriteEventTime == 0) ? "selected" : ""}>否</option>
     	</select>
    </div>
  </div>
  <div id="durationDiv" class="control-group" style="display: none;">
    <label class="control-label" for="dsf_duration">活跃度：</label>
  	<div class="controls">
      <div class="input-append">
	      <input id="dsf_duration" type="text" class="easyui-numberbox" data-options="min:0,max:1440,precision:0" name="duration" value="${empty dataSource ? '30' : dataSource.duration/60/1000 }" placeholder="活跃度" style="width:166px">
	      <span class="add-on" style="width:32px;">分钟</span>
      </div>
    </div>
  </div>
  <div id="pollTimeDiv" class="control-group" style="display: none;">
    <label class="control-label" for="dsf_duration">轮询时间：</label>
  	<div class="controls">
  		<%@include file="./poll_time.jsp" %>
    </div>
  </div>    
  <div id="speedLimitDiv" class="control-group">
    <label class="control-label" for="dsf_speed_limit">限速：</label>
    <div class="controls">
    	<div class="input-append">
	     	<select id="dsf_speed_limit" name="rate" style="width:166px">
	     		<option value="">不限</option>
	     		<option value="1000" ${dataSource.rate == 1000 ? "selected" : "" }>1000</option>
	     		<option value="3000" ${dataSource.rate == 3000 ? "selected" : "" }>3000</option>
	     		<option value="5000" ${dataSource.rate == 5000 ? "selected" : "" }>5000</option>
	     		<option value="10000" ${dataSource.rate == 10000 ? "selected" : "" }>10000</option>
	     	</select>
	     	<span class="add-on" style="width:32px;">条/秒</span>
     	</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_archiveTime">日志保存：</label>
    <div class="controls">
     	<div class="input-append">
	     	<select id="dsf_archiveTime" name="archiveTime" style="width:166px">
	     		<option value="1m" ${dataSource.archiveTime eq "1m" ? "selected" : "" }>1</option>
	     		<option value="2m" ${dataSource.archiveTime eq "2m" ? "selected" : "" }>2</option>
	     		<option value="3m" ${dataSource.archiveTime eq "3m" ? "selected" : "" }>3</option>
	     		<option value="4m" ${dataSource.archiveTime eq "4m" ? "selected" : "" }>4</option>
	     		<option value="5m" ${dataSource.archiveTime eq "5m" ? "selected" : "" }>5</option>
	     		<option value="6m"  ${empty dataSource or dataSource.archiveTime eq "6m" ? "selected" : "" }>6</option>
	     		<option value="7m" ${dataSource.archiveTime eq "7m" ? "selected" : "" }>7</option>
	     		<option value="8m" ${dataSource.archiveTime eq "8m" ? "selected" : "" }>8</option>
	     		<option value="9m" ${dataSource.archiveTime eq "9m" ? "selected" : "" }>9</option>
	     		<option value="10m" ${dataSource.archiveTime eq "10m" ? "selected" : "" }>10</option>
	     		<option value="11m" ${dataSource.archiveTime eq "11m" ? "selected" : "" }>11</option>
	     		<option value="12m" ${dataSource.archiveTime eq "12m" ? "selected" : "" }>12</option>
	     		<option value="10000m" ${dataSource.archiveTime eq "10000m" ? "selected" : "" }>永远</option>
	     	</select>
		    <span class="add-on" style="width:32px;">个月</span>
		</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_reportKeepTime">报表保存：</label>
    <div class="controls">
     	<div class="input-append">
	     	<select id="dsf_reportKeepTime" name="reportKeepTime" style="width:166px">
	     		<c:forEach var="timeIndex" begin="1" end="12">
	     			<c:set var="monthValue" value="${timeIndex}m"></c:set>
	     			<c:set var="shouldSelected" value="${dataSource.reportKeepTime eq monthValue or (empty dataSource and timeIndex eq 6)}"/>
	     			<option value="${monthValue}" ${shouldSelected ? "selected" : ""}>${timeIndex}</option>
	     		</c:forEach>
	     		<option value="10000m" ${dataSource.reportKeepTime eq "10000m" ? "selected" : "" }>永远</option>
	     	</select>
		    <span class="add-on" style="width:32px;">个月</span>
		</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_dataSource_filter">日志过滤规则：</label>
    <div class="controls">
      <select id="dsf_dataSource_filter" selectValue="${dataSource.ruleId}" name="ruleId" ></select>
      <a href="javascript:void(0)"><span class="sim-icon-help" title="只有符合过滤条件的日志才会参与关联分析" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="dsf_dataSource_aggregator">日志归并规则：</label>
    <div class="controls">
      <select id="dsf_dataSource_aggregator" selectValue="${dataSource.aggregatorId}" name="aggregatorId" ></select>
      <a href="javascript:void(0)"><span class="sim-icon-help" title="将多条日志归并为一条，再参与关联分析" style="margin-bottom:-4px;height:16px;width:16px;display:inline-block;"></span></a>
    </div>
  </div>
  <ul class="nav nav-list">
   <li class="divider"/>           
  </ul>
  <div id="dsf_configParamDiv" class="hgap3"/>
</form>
<script src="/js/sim/asset/datasource_form.js"></script>
<script src="/js/sim/asset/asset_detail_datasource_monitor.js"></script>
<script src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/asset/form.js"></script>

