<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="adt_asset_datasource_tb${param.tabSeq}" style="background:#F5F5F5;height:26px;padding:2px 0px 2px 5px">
	<a class="easyui-linkbutton" iconCls="icon-add" href="javascript:ds.addDataSource(${param.tabSeq},'${param.ip}');">新建</a>
</div>
<div id="adt_asset_datasource${param.tabSeq}" class="easyui-accordion" border="false" style="height:270px;margin:0px;padding:0px">
	<c:forEach var="assetDatasource" items="${assetDatasourceJson}">
		<div style="padding:5px;border-width: 0px;"
			 data-options="title: '${assetDatasource.resourceName}',
			tools:'#adt_asset_datasource_accordion_tb${param.tabSeq}_${assetDatasource.resourceId}'">
			<input type="hidden"  name="deviceType" value="${assetDatasource.deviceType}"/>
			<input type="hidden"  name="nodeId" value="${assetDatasource.nodeId}"/>
			<input type="hidden"  name="resourceId" value="${assetDatasource.resourceId}"/>
		   	<table class="table table-condensed table-bordered" style="margin-bottom:5px;">
		    	<tbody>
		    		<tr>
		    			<td name="resourceName">名称</td>
		    			<td name="resourceName_value">${assetDatasource.resourceName}</td>
		    		</tr>
		    		<tr>
		    			<td name="deviceTypeName">类型</td>
		    			<td name="deviceTypeName_value">${assetDatasource.deviceTypeName}</td>
		    		</tr>
		    		<tr>
		    			<td name="collectMethod">收集方式</td>
		    			<td name="collectMethod_value">${assetDatasource.collectMethod}</td>
		    		</tr>
		    		<tr>
		    			<td name="saveRawLog">存储原始日志</td>
		    			<td name="saveRawLogValue">${assetDatasource.saveRawLog == 0 ? "否" : "是" }</td>
		    		</tr>
		    		<tr>
		    			<td name="archiveTime">日志保存时间</td>
		    			<td name="archiveTime_value">
		    				<c:choose>
		    					<c:when test="${assetDatasource.archiveTime eq '10000m'}">永远</c:when>
		    					<c:otherwise>${fn:replace(assetDatasource.archiveTime, 'm', '')}个月</c:otherwise>
		    				</c:choose>
		    			</td>
		    		</tr>
		    		<tr>
		    			<td name="rule">过滤规则</td>
		    			<td name="rule_value">${assetDatasource.rule}</td>
		    		</tr>
		    		<tr>
		    			<td name="aggregator">归并规则</td>
		    			<td name="aggregator_value">${assetDatasource.aggregator}</td>
		    		</tr>
		    		<tr>
		    			<td name="available">状态</td>
		    			<td name="available_format">
							<input datasourceId="${assetDatasource.resourceId}" type="checkbox" data-on-label="启用" data-off-label="禁用" class="switch-mini" ${assetDatasource.available==1 ? "checked" : "" } data-on="success" data-off="danger"/>
		    			</td>
		    		</tr>
		    	</tbody>
		   	</table>
		</div>
		<div id="adt_asset_datasource_accordion_tb${param.tabSeq}_${assetDatasource.resourceId}" >
			<a iconCls="icon-edit" title="编辑" onclick="ds.editDataSource('${assetDatasource.resourceId}');" href="javascript:void(0);"></a>
			<a iconCls="icon-remove" title="删除" onclick="ds.deleteDataSource('${assetDatasource.resourceId}','${assetDatasource.resourceName}');" href="javascript:void(0);"></a>
		</div>
	</c:forEach>
</div>

<div id="adt_add_datasource${param.tabSeq}"></div>
<script src="/js/sim/asset/asset_detail_datasource.js" ></script>
<script type="text/javascript">
	var deviceTypeGlobal = "${deviceType}";
	$(function(){
		$("#adt_asset_datasource${param.tabSeq} input.switch-mini").bootstrapSwitch();
		$("#adt_asset_datasource${param.tabSeq} input.switch-mini").on("switch-change", function (e, data) {
			var el = $(data.el) ;
			var available = data.value ;
			$.getJSON("/sim/datasource/switchState",
					{id:el.attr("datasourceId"),
					 available:available,
					 _time:new Date().getTime()
					},
					function(result){
						if(!result.status){
							showErrorMessage(result.message) ;
						}						 
					}
			) ;
		});
	});
</script>