<%@ page language="java" pageEncoding="utf-8"%>
<table style="min-height: 100px;" class="easyui-datagrid" 
       data-options="height:300,fitColumns:true,singleSelect:true,url:'/sim/assetdetail/alarm?ip=${param.ip}',
                     pagePosition:'top',pagination:true,onLoadSuccess:generalLoadSuccess">
	<thead>  
		<tr>  
			<th data-options="field:'endTime',width:10">时间</th>  
			<th data-options="field:'name',width:15">告警名称</th>  
			<th data-options="field:'devAddr',width:10">设备地址</th>  
			<th data-options="field:'srcAddr',width:10">源地址</th>  
			<th data-options="field:'destAddr',width:10">目的地址</th>  
			<th data-options="field:'priority',width:5,formatter:simHandler.levelFormatter">等级</th>  
			<th data-options="field:'message',width:40">告警描述</th>  
		</tr>  
	</thead>  
</table>
