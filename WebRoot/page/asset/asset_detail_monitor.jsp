<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="adt_asset_monitor_tb${param.tabSeq}" style="background:#F5F5F5;height:26px;padding:2px 0px 2px 5px;${not empty monitor ? 'display:none;' : ''}">
	<a class="easyui-linkbutton" iconCls="icon-add" href="javascript:mnt.addMonitor('${param.tabSeq}','${param.ip}');">新建</a>
</div>
<c:if test="${not empty monitor}">
	<div id="adt_asset_monitor_accordion_tb${param.tabSeq}" >
		<a iconCls="icon-edit" title="编辑" onclick="mnt.editMonitor('${monitor.resourceId}');" href="javascript:void(0);"></a>
		<a iconCls="icon-remove" title="删除" onclick="mnt.deleteMonitor('${monitor.resourceId}', '${monitor.resourceName}');" href="javascript:void(0);"></a>
	</div>
</c:if>
<div id="adt_asset_monitor${param.tabSeq}" class="easyui-accordion sim" border="false" style="height:${not empty monitor ? '290px' : '270px'};margin:0px;padding:0px;">
	<c:if test="${not empty monitor}">
		<div style="padding:5px;border:none;" data-options="
			title:'${monitor.resourceName}',
			collapsible:false,
			fit:true,
			tools:'#adt_asset_monitor_accordion_tb${param.tabSeq}'">
		   	<table class="table table-condensed table-bordered" style="margin-bottom: 0px;">
		    	<tbody>
		    		<tr>
		    			<td width="55">名称</td>
		    			<td>${monitor.resourceName}</td>
		    		</tr>
		    		<tr>
		    			<td>类型</td>
		    			<td>${monitor.deviceTypeName}</td>
		    		</tr>
		    		<tr>
		    			<td>收集方式</td>
		    			<td>${monitor.collectMethod}</td>
		    		</tr>
		    		<tr>
		    			<td>状态</td>
		    			<td>
							<input name="available" monitorId="${monitor.resourceId}" type="checkbox" data-on-label="启用" data-off-label="禁用" class="switch-mini" ${monitor.available==1 ? "checked" : "" } data-on="success" data-off="danger"/>
		    			</td>
		    		</tr>		    		
		    	</tbody>
		   	</table>
		</div>
	</c:if>
</div>
<script src="/js/sim/asset/asset_detail_monitor.js"></script>
<script type="text/javascript">
	var deviceTypeGlobal = "${deviceType}";
	$(function(){
		$('#adt_asset_monitor${param.tabSeq} .switch-mini').bootstrapSwitch();
		$("#adt_asset_monitor${param.tabSeq} .switch-mini").on("switch-change", function (e, data) {
			var el = $(data.el) ;
			var available = data.value ;
			$.getJSON("/sim/monitor/switchState",
					{id:el.attr("monitorId"),
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
